package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.ExpenseAttachmentRepository;
import com.mushroom.expense.repository.ExpenseCommentRepository;
import com.mushroom.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseCommentRepository expenseCommentRepository;

    @Mock
    private ExpenseAttachmentRepository expenseAttachmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Test Expense");
        expense.setAmount(100.0);
        expense.setDate(LocalDate.now());
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setUser(user);
        expense.setAttachments(new ArrayList<>());
    }

    @Test
    void saveExpense_NewExpense() throws IOException {
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense savedExpense = expenseService.saveExpense(expense, null, null, false);

        assertNotNull(savedExpense);
        assertEquals(ExpenseStatus.DRAFT, savedExpense.getStatus());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void saveExpense_WithAttachments() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(fileStorageService.storeFile(mockFile)).thenReturn("test-file.jpg");
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        List<MultipartFile> files = List.of(mockFile);
        Expense savedExpense = expenseService.saveExpense(expense, files, null, false);

        assertEquals(1, savedExpense.getAttachments().size());
        assertEquals("test-file.jpg", savedExpense.getAttachments().get(0).getFileName());
        verify(fileStorageService, times(1)).storeFile(mockFile);
    }

    @Test
    void deleteExpense_Success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        // Add an attachment to verify deletion
        ExpenseAttachment attachment = new ExpenseAttachment("test.jpg", expense);
        expense.getAttachments().add(attachment);

        expenseService.deleteExpense(1L);

        verify(fileStorageService, times(1)).deleteFile("test.jpg");
        verify(expenseRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateExpenseStatus_Success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense updatedExpense = expenseService.updateExpenseStatus(1L, ExpenseStatus.APPROVED);

        assertEquals(ExpenseStatus.APPROVED, updatedExpense.getStatus());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void addComment_Success() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.addComment(1L, user, "Test Comment");

        verify(expenseCommentRepository, times(1)).save(any(ExpenseComment.class));
        assertEquals(ExpenseStatus.QUERIES_RAISED, expense.getStatus());
        verify(expenseRepository, times(1)).save(expense);
    }

    @Test
    void deleteAttachment_Success() {
        ExpenseAttachment attachment = new ExpenseAttachment("test.jpg", expense);
        attachment.setId(10L);
        expense.getAttachments().add(attachment);

        when(expenseAttachmentRepository.findById(10L)).thenReturn(Optional.of(attachment));

        expenseService.deleteAttachment(10L);

        verify(fileStorageService, times(1)).deleteFile("test.jpg");
        verify(expenseRepository, times(1)).save(expense);
        assertFalse(expense.getAttachments().contains(attachment));
    }

    @Test
    void saveExpense_LegacyMigration() throws IOException {
        expense.setReceiptImage("legacy.jpg");
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense savedExpense = expenseService.saveExpense(expense, null, null, false);

        assertNull(savedExpense.getReceiptImage());
        assertEquals(1, savedExpense.getAttachments().size());
        assertEquals("legacy.jpg", savedExpense.getAttachments().get(0).getFileName());
    }

    @Test
    void saveExpense_DeleteAttachments() throws IOException {
        ExpenseAttachment attachment1 = new ExpenseAttachment("file1.jpg", expense);
        attachment1.setId(1L);
        ExpenseAttachment attachment2 = new ExpenseAttachment("file2.jpg", expense);
        attachment2.setId(2L);
        expense.getAttachments().add(attachment1);
        expense.getAttachments().add(attachment2);

        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        List<Long> deleteIds = List.of(1L);
        expenseService.saveExpense(expense, null, deleteIds, false);

        verify(fileStorageService, times(1)).deleteFile("file1.jpg");
        verify(expenseAttachmentRepository, times(1)).delete(attachment1);
        assertEquals(1, expense.getAttachments().size());
        assertEquals("file2.jpg", expense.getAttachments().get(0).getFileName());
    }

    @Test
    void saveExpense_DeletePrimaryImage() throws IOException {
        expense.setReceiptImage("primary.jpg");
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        expenseService.saveExpense(expense, null, null, true);

        verify(fileStorageService, times(1)).deleteFile("primary.jpg");
        assertNull(expense.getReceiptImage());
    }

    @Test
    void deleteExpense_NonExistent() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> expenseService.deleteExpense(99L));
        verify(expenseRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateExpenseStatus_NotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> expenseService.updateExpenseStatus(99L, ExpenseStatus.APPROVED));
    }

    @Test
    void addComment_NotFound() {
        when(expenseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> expenseService.addComment(99L, user, "Comment"));
    }

    @Test
    void deleteAttachment_NotFound() {
        when(expenseAttachmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> expenseService.deleteAttachment(99L));
        verify(fileStorageService, never()).deleteFile(anyString());
    }
}
