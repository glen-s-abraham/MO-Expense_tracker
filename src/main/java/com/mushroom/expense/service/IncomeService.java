package com.mushroom.expense.service;

import com.mushroom.expense.entity.Income;
import com.mushroom.expense.entity.IncomeComment;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.repository.IncomeCommentRepository;
import com.mushroom.expense.repository.IncomeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final IncomeCommentRepository incomeCommentRepository;
    private final com.mushroom.expense.repository.IncomeAttachmentRepository incomeAttachmentRepository;
    private final FileStorageService fileStorageService;

    public IncomeService(IncomeRepository incomeRepository, IncomeCommentRepository incomeCommentRepository,
            com.mushroom.expense.repository.IncomeAttachmentRepository incomeAttachmentRepository,
            FileStorageService fileStorageService) {
        this.incomeRepository = incomeRepository;
        this.incomeCommentRepository = incomeCommentRepository;
        this.incomeAttachmentRepository = incomeAttachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Income> findAllIncomes() {
        return incomeRepository.findAll();
    }

    public Page<Income> findAllIncomes(Pageable pageable) {
        return incomeRepository.findAll(pageable);
    }

    public List<Income> findIncomesByUser(User user) {
        return incomeRepository.findByUser(user);
    }

    public Page<Income> findIncomesByUser(User user, Pageable pageable) {
        return incomeRepository.findByUser(user, pageable);
    }

    public Page<Income> findIncomesByUserAndStatus(User user, IncomeStatus status, Pageable pageable) {
        return incomeRepository.findByUserAndStatus(user, status, pageable);
    }

    public Page<Income> findIncomesByUserAndStatusIn(User user, List<IncomeStatus> statuses, Pageable pageable) {
        return incomeRepository.findByUserAndStatusIn(user, statuses, pageable);
    }

    public List<Income> findIncomesByStatus(IncomeStatus status) {
        return incomeRepository.findByStatus(status);
    }

    public Page<Income> findIncomesByStatus(IncomeStatus status, Pageable pageable) {
        return incomeRepository.findByStatus(status, pageable);
    }

    public Page<Income> getIncomes(User user, List<IncomeStatus> statuses, String keyword,
            LocalDate startDate, LocalDate endDate, Long categoryId, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Income> spec = com.mushroom.expense.specification.IncomeSpecification
                .filterIncomes(user, statuses, keyword, startDate, endDate, categoryId);
        return incomeRepository.findAll(spec, pageable);
    }

    public Optional<Income> findById(Long id) {
        return incomeRepository.findById(id);
    }

    public Income saveIncome(Income income, List<MultipartFile> files, List<Long> deleteAttachmentIds,
            boolean deletePrimaryImage) throws IOException {
        if (income.getDate() == null) {
            income.setDate(LocalDate.now());
        }

        // Ensure attachments list is initialized
        if (income.getAttachments() == null) {
            income.setAttachments(new java.util.ArrayList<>());
        }

        // Handle deletions
        if (deletePrimaryImage && income.getReceiptImage() != null) {
            fileStorageService.deleteFile(income.getReceiptImage());
            income.setReceiptImage(null);
        }

        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            List<com.mushroom.expense.entity.IncomeAttachment> attachmentsToRemove = new java.util.ArrayList<>();
            for (com.mushroom.expense.entity.IncomeAttachment attachment : income.getAttachments()) {
                if (deleteAttachmentIds.contains(attachment.getId())) {
                    fileStorageService.deleteFile(attachment.getFileName());
                    incomeAttachmentRepository.delete(attachment); // Explicit delete
                    attachmentsToRemove.add(attachment);
                }
            }
            income.getAttachments().removeAll(attachmentsToRemove);
        }

        // Migration: Move legacy receiptImage to attachments if present
        if (income.getReceiptImage() != null && !income.getReceiptImage().isEmpty()) {
            boolean alreadyExists = income.getAttachments().stream()
                    .anyMatch(a -> a.getFileName().equals(income.getReceiptImage()));

            if (!alreadyExists) {
                com.mushroom.expense.entity.IncomeAttachment attachment = new com.mushroom.expense.entity.IncomeAttachment(
                        income.getReceiptImage(), income);
                income.getAttachments().add(attachment);
            }
            income.setReceiptImage(null); // Clear legacy field
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);

                    com.mushroom.expense.entity.IncomeAttachment attachment = new com.mushroom.expense.entity.IncomeAttachment(
                            fileName, income);
                    // Add to the list so cascade/orphanRemoval works correctly
                    income.getAttachments().add(attachment);
                }
            }
        }

        return incomeRepository.save(income);
    }

    public Income updateIncomeStatus(Long incomeId, IncomeStatus status) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income Id:" + incomeId));
        income.setStatus(status);
        return incomeRepository.save(income);
    }

    public void addComment(Long incomeId, User user, String message) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid income Id:" + incomeId));

        IncomeComment comment = new IncomeComment(income, user, message);
        incomeCommentRepository.save(comment);

        // Auto-update status to QUERIES_RAISED if not already
        if (income.getStatus() != IncomeStatus.QUERIES_RAISED) {
            income.setStatus(IncomeStatus.QUERIES_RAISED);
            incomeRepository.save(income);
        }
    }

    public List<IncomeComment> getComments(Long incomeId) {
        return incomeCommentRepository.findByIncomeId(incomeId);
    }

    public void deleteIncome(Long id) {
        Optional<Income> incomeOptional = incomeRepository.findById(id);
        if (incomeOptional.isPresent()) {
            Income income = incomeOptional.get();

            // Delete primary receipt image
            if (income.getReceiptImage() != null) {
                fileStorageService.deleteFile(income.getReceiptImage());
            }

            // Delete all attachments
            if (income.getAttachments() != null) {
                for (com.mushroom.expense.entity.IncomeAttachment attachment : income.getAttachments()) {
                    fileStorageService.deleteFile(attachment.getFileName());
                }
            }

            incomeRepository.deleteById(id);
        }
    }

    public void deleteAttachment(Long attachmentId) {
        Optional<com.mushroom.expense.entity.IncomeAttachment> attachmentOptional = incomeAttachmentRepository
                .findById(attachmentId);
        if (attachmentOptional.isPresent()) {
            com.mushroom.expense.entity.IncomeAttachment attachment = attachmentOptional.get();

            // Delete file from storage
            fileStorageService.deleteFile(attachment.getFileName());

            // Remove from parent income to ensure consistency if income is loaded in session
            Income income = attachment.getIncome();
            if (income != null && income.getAttachments() != null) {
                income.getAttachments().remove(attachment);
                incomeRepository.save(income); // Save income to update the collection
            } else {
                incomeAttachmentRepository.delete(attachment);
            }
        }
    }
}
