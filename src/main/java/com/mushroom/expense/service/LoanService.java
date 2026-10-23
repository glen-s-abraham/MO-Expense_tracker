package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.specification.LoanSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LoanService {

    public record LoanSummary(
            Long id,
            String loanName,
            String lenderName,
            String description,
            LocalDate startDate,
            LoanStatus status,
            double totalBorrowed,
            double totalRepaid,
            double outstandingBalance,
            int repaymentProgressPercent) {
    }

    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanCommentRepository loanCommentRepository;
    private final LoanAttachmentRepository loanAttachmentRepository;
    private final FileStorageService fileStorageService;

    public LoanService(
            LoanRepository loanRepository,
            LoanTransactionRepository loanTransactionRepository,
            LoanCommentRepository loanCommentRepository,
            LoanAttachmentRepository loanAttachmentRepository,
            FileStorageService fileStorageService) {
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.loanCommentRepository = loanCommentRepository;
        this.loanAttachmentRepository = loanAttachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Loan> findAllLoans() {
        return loanRepository.findAll();
    }

    public Optional<Loan> findLoanById(Long id) {
        return loanRepository.findById(id);
    }

    public Loan createOrGetLoanFacility(String loanName, String lenderName, String description, LocalDate startDate) {
        return loanRepository.findByLoanNameIgnoreCase(loanName.trim())
                .orElseGet(() -> loanRepository.save(new Loan(
                        loanName.trim(),
                        lenderName != null && !lenderName.isBlank() ? lenderName.trim() : "Lender",
                        description,
                        startDate != null ? startDate : LocalDate.now())));
    }

    public List<LoanSummary> getAllLoanSummaries() {
        List<Loan> loans = loanRepository.findAll();
        List<LoanSummary> summaries = new ArrayList<>();
        for (Loan loan : loans) {
            double borrowed = getLoanApprovedTotal(loan.getId(), LoanTransactionType.BORROWED);
            double repaid = getLoanApprovedTotal(loan.getId(), LoanTransactionType.REPAYMENT);
            double outstanding = Math.max(0.0, borrowed - repaid);
            int progress = borrowed > 0 ? (int) Math.min(100, Math.round((repaid / borrowed) * 100.0)) : 0;
            summaries.add(new LoanSummary(
                    loan.getId(),
                    loan.getLoanName(),
                    loan.getLenderName(),
                    loan.getDescription(),
                    loan.getStartDate(),
                    loan.getStatus(),
                    borrowed,
                    repaid,
                    outstanding,
                    progress));
        }
        return summaries;
    }

    public double getLoanApprovedTotal(Long loanId, LoanTransactionType type) {
        Double val = loanTransactionRepository.sumAmountByLoanIdAndTypeAndStatus(loanId, type, IncomeStatus.APPROVED);
        return val != null ? val : 0.0;
    }

    public double getLoanOutstandingBalance(Long loanId) {
        return getLoanApprovedTotal(loanId, LoanTransactionType.BORROWED)
                - getLoanApprovedTotal(loanId, LoanTransactionType.REPAYMENT);
    }

    public Page<LoanTransaction> getLoanTransactions(
            User user,
            LoanTransactionType type,
            Long loanId,
            List<IncomeStatus> statuses,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Specification<LoanTransaction> spec = LoanSpecification.filterLoanTransactions(
                user, type, loanId, statuses, keyword, startDate, endDate);
        return loanTransactionRepository.findAll(spec, pageable);
    }

    public Optional<LoanTransaction> findById(Long id) {
        return loanTransactionRepository.findById(id);
    }

    public LoanTransaction saveLoanTransaction(
            LoanTransaction transaction,
            List<MultipartFile> files,
            List<Long> deleteAttachmentIds,
            boolean deletePrimaryImage) throws IOException {
        if (transaction.getLoan() == null || transaction.getLoan().getId() == null) {
            throw new IllegalArgumentException("A loan facility must be selected for loan transactions.");
        }
        Loan managedLoan = loanRepository.findById(transaction.getLoan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Loan facility not found: " + transaction.getLoan().getId()));
        transaction.setLoan(managedLoan);

        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }
        if (transaction.getType() == null) {
            transaction.setType(LoanTransactionType.BORROWED);
        }

        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Loan transaction amount must be greater than zero.");
        }

        if (transaction.getType() == LoanTransactionType.REPAYMENT) {
            validateRepaymentAmount(transaction.getLoan().getId(), transaction.getAmount(), transaction.getLoan().getLoanName());
        }

        if (transaction.getAttachments() == null) {
            transaction.setAttachments(new ArrayList<>());
        }

        if (deletePrimaryImage && transaction.getReceiptImage() != null) {
            fileStorageService.deleteFile(transaction.getReceiptImage());
            transaction.setReceiptImage(null);
        }

        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            List<LoanAttachment> toRemove = new ArrayList<>();
            for (LoanAttachment att : transaction.getAttachments()) {
                if (deleteAttachmentIds.contains(att.getId())) {
                    fileStorageService.deleteFile(att.getFileName());
                    loanAttachmentRepository.delete(att);
                    toRemove.add(att);
                }
            }
            transaction.getAttachments().removeAll(toRemove);
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    transaction.getAttachments().add(new LoanAttachment(fileName, transaction));
                }
            }
        }

        LoanTransaction saved = loanTransactionRepository.save(transaction);
        recalculateLoanStatus(saved.getLoan());
        return saved;
    }

    public LoanTransaction updateStatus(Long id, IncomeStatus status) {
        LoanTransaction tx = loanTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loan transaction Id: " + id));

        if (status == IncomeStatus.APPROVED
                && tx.getStatus() != IncomeStatus.APPROVED
                && tx.getType() == LoanTransactionType.REPAYMENT) {
            validateRepaymentAmount(tx.getLoan().getId(), tx.getAmount(), tx.getLoan().getLoanName());
        }

        tx.setStatus(status);
        LoanTransaction saved = loanTransactionRepository.save(tx);
        recalculateLoanStatus(saved.getLoan());
        return saved;
    }

    private void validateRepaymentAmount(Long loanId, Double amount, String loanName) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero.");
        }
        double outstanding = getLoanOutstandingBalance(loanId);
        if (amount - outstanding > 0.001) {
            throw new IllegalArgumentException(String.format(
                    "Repayment amount (₹%.2f) exceeds the outstanding principal balance (₹%.2f) for loan '%s'.",
                    amount, outstanding, loanName));
        }
    }

    public void recalculateLoanStatus(Loan loan) {
        if (loan == null || loan.getId() == null) {
            return;
        }
        Loan managedLoan = loanRepository.findById(loan.getId()).orElse(loan);
        double borrowed = getLoanApprovedTotal(managedLoan.getId(), LoanTransactionType.BORROWED);
        double repaid = getLoanApprovedTotal(managedLoan.getId(), LoanTransactionType.REPAYMENT);
        double outstanding = borrowed - repaid;

        if (borrowed > 0 && outstanding <= 0.001) {
            managedLoan.setStatus(LoanStatus.CLOSED);
        } else {
            managedLoan.setStatus(LoanStatus.ACTIVE);
        }
        loanRepository.save(managedLoan);
    }

    public void addComment(Long id, User user, String message) {
        LoanTransaction tx = loanTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid loan transaction Id: " + id));
        LoanComment comment = new LoanComment(tx, user, message);
        loanCommentRepository.save(comment);

        if (tx.getStatus() != IncomeStatus.QUERIES_RAISED) {
            tx.setStatus(IncomeStatus.QUERIES_RAISED);
            loanTransactionRepository.save(tx);
        }
    }

    public List<LoanComment> getComments(Long id) {
        return loanCommentRepository.findByLoanTransactionId(id);
    }

    public void deleteLoanTransaction(Long id) {
        loanTransactionRepository.findById(id).ifPresent(tx -> {
            Loan loan = tx.getLoan();
            if (tx.getReceiptImage() != null) {
                fileStorageService.deleteFile(tx.getReceiptImage());
            }
            if (tx.getAttachments() != null) {
                for (LoanAttachment att : tx.getAttachments()) {
                    fileStorageService.deleteFile(att.getFileName());
                }
            }
            loanTransactionRepository.deleteById(id);
            recalculateLoanStatus(loan);
        });
    }

    public void deleteAttachment(Long attachmentId) {
        loanAttachmentRepository.findById(attachmentId).ifPresent(att -> {
            fileStorageService.deleteFile(att.getFileName());
            LoanTransaction tx = att.getLoanTransaction();
            if (tx != null && tx.getAttachments() != null) {
                tx.getAttachments().remove(att);
                loanTransactionRepository.save(tx);
            } else {
                loanAttachmentRepository.delete(att);
            }
        });
    }

    public double getTotalApprovedBorrowed() {
        Double val = loanTransactionRepository.sumAmountByTypeAndStatus(
                LoanTransactionType.BORROWED, IncomeStatus.APPROVED);
        return val != null ? val : 0.0;
    }

    public double getTotalApprovedRepaid() {
        Double val = loanTransactionRepository.sumAmountByTypeAndStatus(
                LoanTransactionType.REPAYMENT, IncomeStatus.APPROVED);
        return val != null ? val : 0.0;
    }

    public double getOutstandingLoanPool() {
        return Math.max(0.0, getTotalApprovedBorrowed() - getTotalApprovedRepaid());
    }
}
