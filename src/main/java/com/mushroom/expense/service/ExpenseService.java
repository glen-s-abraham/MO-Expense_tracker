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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Value;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCommentRepository expenseCommentRepository;

    // Upload directory
    @Value("${app.upload.dir}")
    private String UPLOAD_DIR;

    public ExpenseService(ExpenseRepository expenseRepository, ExpenseCommentRepository expenseCommentRepository) {
        this.expenseRepository = expenseRepository;
        this.expenseCommentRepository = expenseCommentRepository;
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
        if (deletePrimaryImage) {
            expense.setReceiptImage(null);
        }

        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            expense.getAttachments().removeIf(attachment -> deleteAttachmentIds.contains(attachment.getId()));
        }

        if (files != null && !files.isEmpty()) {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // For backward compatibility
                    if (expense.getReceiptImage() == null) {
                        expense.setReceiptImage(fileName);
                    }

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
        expenseRepository.deleteById(id);
    }
}
