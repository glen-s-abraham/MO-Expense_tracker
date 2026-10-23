package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.CapitalAttachmentRepository;
import com.mushroom.expense.repository.CapitalCommentRepository;
import com.mushroom.expense.repository.CapitalTransactionRepository;
import com.mushroom.expense.specification.CapitalSpecification;
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
public class CapitalService {

    private final CapitalTransactionRepository capitalTransactionRepository;
    private final CapitalCommentRepository capitalCommentRepository;
    private final CapitalAttachmentRepository capitalAttachmentRepository;
    private final FileStorageService fileStorageService;

    public CapitalService(
            CapitalTransactionRepository capitalTransactionRepository,
            CapitalCommentRepository capitalCommentRepository,
            CapitalAttachmentRepository capitalAttachmentRepository,
            FileStorageService fileStorageService) {
        this.capitalTransactionRepository = capitalTransactionRepository;
        this.capitalCommentRepository = capitalCommentRepository;
        this.capitalAttachmentRepository = capitalAttachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    public Page<CapitalTransaction> getCapitalTransactions(
            User user,
            CapitalTransactionType type,
            List<IncomeStatus> statuses,
            String keyword,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {
        Specification<CapitalTransaction> spec = CapitalSpecification.filterCapitalTransactions(
                user, type, statuses, keyword, startDate, endDate);
        return capitalTransactionRepository.findAll(spec, pageable);
    }

    public Optional<CapitalTransaction> findById(Long id) {
        return capitalTransactionRepository.findById(id);
    }

    public CapitalTransaction saveCapitalTransaction(
            CapitalTransaction transaction,
            List<MultipartFile> files,
            List<Long> deleteAttachmentIds,
            boolean deletePrimaryImage) throws IOException {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Capital amount must be greater than zero.");
        }
        if (transaction.getDate() == null) {
            transaction.setDate(LocalDate.now());
        }
        if (transaction.getType() == null) {
            transaction.setType(CapitalTransactionType.INTRODUCED);
        }
        if (transaction.getAttachments() == null) {
            transaction.setAttachments(new ArrayList<>());
        }

        if (deletePrimaryImage && transaction.getReceiptImage() != null) {
            fileStorageService.deleteFile(transaction.getReceiptImage());
            transaction.setReceiptImage(null);
        }

        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            List<CapitalAttachment> toRemove = new ArrayList<>();
            for (CapitalAttachment att : transaction.getAttachments()) {
                if (deleteAttachmentIds.contains(att.getId())) {
                    fileStorageService.deleteFile(att.getFileName());
                    capitalAttachmentRepository.delete(att);
                    toRemove.add(att);
                }
            }
            transaction.getAttachments().removeAll(toRemove);
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);
                    transaction.getAttachments().add(new CapitalAttachment(fileName, transaction));
                }
            }
        }

        return capitalTransactionRepository.save(transaction);
    }

    public CapitalTransaction updateStatus(Long id, IncomeStatus status) {
        CapitalTransaction tx = capitalTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid capital transaction Id: " + id));
        tx.setStatus(status);
        return capitalTransactionRepository.save(tx);
    }

    public void addComment(Long id, User user, String message) {
        CapitalTransaction tx = capitalTransactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid capital transaction Id: " + id));
        CapitalComment comment = new CapitalComment(tx, user, message);
        capitalCommentRepository.save(comment);

        if (tx.getStatus() != IncomeStatus.QUERIES_RAISED) {
            tx.setStatus(IncomeStatus.QUERIES_RAISED);
            capitalTransactionRepository.save(tx);
        }
    }

    public List<CapitalComment> getComments(Long id) {
        return capitalCommentRepository.findByCapitalTransactionId(id);
    }

    public void deleteCapitalTransaction(Long id) {
        capitalTransactionRepository.findById(id).ifPresent(tx -> {
            if (tx.getReceiptImage() != null) {
                fileStorageService.deleteFile(tx.getReceiptImage());
            }
            if (tx.getAttachments() != null) {
                for (CapitalAttachment att : tx.getAttachments()) {
                    fileStorageService.deleteFile(att.getFileName());
                }
            }
            capitalTransactionRepository.deleteById(id);
        });
    }

    public void deleteAttachment(Long attachmentId) {
        capitalAttachmentRepository.findById(attachmentId).ifPresent(att -> {
            fileStorageService.deleteFile(att.getFileName());
            CapitalTransaction tx = att.getCapitalTransaction();
            if (tx != null && tx.getAttachments() != null) {
                tx.getAttachments().remove(att);
                capitalTransactionRepository.save(tx);
            } else {
                capitalAttachmentRepository.delete(att);
            }
        });
    }

    public double getTotalApprovedIntroduced() {
        Double val = capitalTransactionRepository.sumAmountByTypeAndStatus(
                CapitalTransactionType.INTRODUCED, IncomeStatus.APPROVED);
        return val != null ? val : 0.0;
    }

    public double getTotalApprovedDrawings() {
        Double val = capitalTransactionRepository.sumAmountByTypeAndStatus(
                CapitalTransactionType.DRAWINGS, IncomeStatus.APPROVED);
        return val != null ? val : 0.0;
    }

    public double getNetCapitalPool() {
        return getTotalApprovedIntroduced() - getTotalApprovedDrawings();
    }
}
