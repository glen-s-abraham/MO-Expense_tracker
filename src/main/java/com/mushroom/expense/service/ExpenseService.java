package com.mushroom.expense.service;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseComment;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.repository.ExpenseCommentRepository;
import com.mushroom.expense.repository.ExpenseRepository;
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
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCommentRepository expenseCommentRepository;
    private final com.mushroom.expense.repository.ExpenseAttachmentRepository expenseAttachmentRepository;
    private final FileStorageService fileStorageService;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseCommentRepository expenseCommentRepository,
            com.mushroom.expense.repository.ExpenseAttachmentRepository expenseAttachmentRepository,
            FileStorageService fileStorageService) {
        this.expenseRepository = expenseRepository;
        this.expenseCommentRepository = expenseCommentRepository;
        this.expenseAttachmentRepository = expenseAttachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<Expense> findAllExpenses() {
        return expenseRepository.findAll();
    }

    public Page<Expense> findAllExpenses(Pageable pageable) {
        return expenseRepository.findAll(pageable);
    }

    public List<Expense> findExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public Page<Expense> findExpensesByUser(User user, Pageable pageable) {
        return expenseRepository.findByUser(user, pageable);
    }

    public Page<Expense> findExpensesByUserAndStatus(User user, ExpenseStatus status, Pageable pageable) {
        return expenseRepository.findByUserAndStatus(user, status, pageable);
    }

    public Page<Expense> findExpensesByUserAndStatusIn(User user, List<ExpenseStatus> statuses, Pageable pageable) {
        return expenseRepository.findByUserAndStatusIn(user, statuses, pageable);
    }

    public List<Expense> findExpensesByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status);
    }

    public Page<Expense> findExpensesByStatus(ExpenseStatus status, Pageable pageable) {
        return expenseRepository.findByStatus(status, pageable);
    }

    public Page<Expense> getExpenses(User user, List<ExpenseStatus> statuses, String keyword,
            LocalDate startDate, LocalDate endDate, Long categoryId, Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Expense> spec = com.mushroom.expense.specification.ExpenseSpecification
                .filterExpenses(user, statuses, keyword, startDate, endDate, categoryId);
        return expenseRepository.findAll(spec, pageable);
    }

    public Optional<Expense> findById(Long id) {
        return expenseRepository.findById(id);
    }

    public Expense saveExpense(Expense expense, List<MultipartFile> files, List<Long> deleteAttachmentIds,
            boolean deletePrimaryImage) throws IOException {
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }

        // Ensure attachments list is initialized
        if (expense.getAttachments() == null) {
            expense.setAttachments(new java.util.ArrayList<>());
        }

        // Handle deletions
        if (deletePrimaryImage && expense.getReceiptImage() != null) {
            fileStorageService.deleteFile(expense.getReceiptImage());
            expense.setReceiptImage(null);
        }

        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            List<com.mushroom.expense.entity.ExpenseAttachment> attachmentsToRemove = new java.util.ArrayList<>();
            for (com.mushroom.expense.entity.ExpenseAttachment attachment : expense.getAttachments()) {
                if (deleteAttachmentIds.contains(attachment.getId())) {
                    fileStorageService.deleteFile(attachment.getFileName());
                    expenseAttachmentRepository.delete(attachment); // Explicit delete
                    attachmentsToRemove.add(attachment);
                }
            }
            expense.getAttachments().removeAll(attachmentsToRemove);
        }

        // Migration: Move legacy receiptImage to attachments if present
        if (expense.getReceiptImage() != null && !expense.getReceiptImage().isEmpty()) {
            boolean alreadyExists = expense.getAttachments().stream()
                    .anyMatch(a -> a.getFileName().equals(expense.getReceiptImage()));

            if (!alreadyExists) {
                com.mushroom.expense.entity.ExpenseAttachment attachment = new com.mushroom.expense.entity.ExpenseAttachment(
                        expense.getReceiptImage(), expense);
                expense.getAttachments().add(attachment);
            }
            expense.setReceiptImage(null); // Clear legacy field
        }

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileStorageService.storeFile(file);

                    com.mushroom.expense.entity.ExpenseAttachment attachment = new com.mushroom.expense.entity.ExpenseAttachment(
                            fileName, expense);
                    // Add to the list so cascade/orphanRemoval works correctly
                    expense.getAttachments().add(attachment);
                }
            }
        }

        return expenseRepository.save(expense);
    }

    public Expense updateExpenseStatus(Long expenseId, ExpenseStatus status) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expense Id:" + expenseId));
        expense.setStatus(status);
        return expenseRepository.save(expense);
    }

    public void addComment(Long expenseId, User user, String message) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid expense Id:" + expenseId));

        ExpenseComment comment = new ExpenseComment(expense, user, message);
        expenseCommentRepository.save(comment);

        // Auto-update status to QUERIES_RAISED if not already
        if (expense.getStatus() != ExpenseStatus.QUERIES_RAISED) {
            expense.setStatus(ExpenseStatus.QUERIES_RAISED);
            expenseRepository.save(expense);
        }
    }

    public List<ExpenseComment> getComments(Long expenseId) {
        return expenseCommentRepository.findByExpenseId(expenseId);
    }

    public void deleteExpense(Long id) {
        Optional<Expense> expenseOptional = expenseRepository.findById(id);
        if (expenseOptional.isPresent()) {
            Expense expense = expenseOptional.get();

            // Delete primary receipt image
            if (expense.getReceiptImage() != null) {
                fileStorageService.deleteFile(expense.getReceiptImage());
            }

            // Delete all attachments
            if (expense.getAttachments() != null) {
                for (com.mushroom.expense.entity.ExpenseAttachment attachment : expense.getAttachments()) {
                    fileStorageService.deleteFile(attachment.getFileName());
                }
            }

            expenseRepository.deleteById(id);
        }
    }

    public void deleteAttachment(Long attachmentId) {
        Optional<com.mushroom.expense.entity.ExpenseAttachment> attachmentOptional = expenseAttachmentRepository
                .findById(attachmentId);
        if (attachmentOptional.isPresent()) {
            com.mushroom.expense.entity.ExpenseAttachment attachment = attachmentOptional.get();

            // Delete file from storage
            fileStorageService.deleteFile(attachment.getFileName());

            // Remove from parent expense to ensure consistency if expense is loaded in
            // session
            Expense expense = attachment.getExpense();
            if (expense != null && expense.getAttachments() != null) {
                expense.getAttachments().remove(attachment);
                expenseRepository.save(expense); // Save expense to update the collection
            } else {
                // Fallback direct delete if no parent link (shouldn't happen with correct
                // mapping)
                expenseAttachmentRepository.delete(attachment);
            }
        }
    }
}
