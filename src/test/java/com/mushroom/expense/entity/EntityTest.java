package com.mushroom.expense.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;

class EntityTest {

    @Test
    void testUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("pass");
        user.setRole("ROLE_USER");
        user.setEnabled(true);

        assertEquals(1L, user.getId());
        assertEquals("test", user.getUsername());
        assertEquals("pass", user.getPassword());
        assertEquals("ROLE_USER", user.getRole());
        assertTrue(user.isEnabled());

        User user2 = new User("user2", "pass2", "ROLE_ADMIN");
        assertEquals("user2", user2.getUsername());
    }

    @Test
    void testCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");

        assertEquals(1L, category.getId());
        assertEquals("Food", category.getName());
    }

    @Test
    void testSubCategory() {
        Category category = new Category();
        SubCategory subCategory = new SubCategory();
        subCategory.setId(1L);
        subCategory.setName("Lunch");
        subCategory.setCategory(category);

        assertEquals(1L, subCategory.getId());
        assertEquals("Lunch", subCategory.getName());
        assertEquals(category, subCategory.getCategory());
    }

    @Test
    void testExpense() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Lunch");
        expense.setAmount(50.0);
        expense.setDate(LocalDate.now());
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setReceiptImage("img.jpg");
        expense.setPaymentMode(PaymentMode.CASH);
        expense.setTaxPercentage(5.0);
        expense.setBatchId("BATCH-001");

        User user = new User();
        expense.setUser(user);

        Category category = new Category();
        expense.setCategory(category);

        SubCategory subCategory = new SubCategory();
        expense.setSubCategory(subCategory);

        expense.setAttachments(new ArrayList<>());
        expense.setComments(new ArrayList<>());

        assertEquals(1L, expense.getId());
        assertEquals("Lunch", expense.getDescription());
        assertEquals(50.0, expense.getAmount());
        assertNotNull(expense.getDate());
        assertEquals(ExpenseStatus.DRAFT, expense.getStatus());
        assertEquals("img.jpg", expense.getReceiptImage());
        assertEquals(PaymentMode.CASH, expense.getPaymentMode());
        assertEquals(5.0, expense.getTaxPercentage());
        assertEquals("BATCH-001", expense.getBatchId());
        assertEquals(user, expense.getUser());
        assertEquals(category, expense.getCategory());
        assertEquals(subCategory, expense.getSubCategory());
        assertNotNull(expense.getAttachments());
        assertNotNull(expense.getComments());
    }

    @Test
    void testExpenseAttachment() {
        Expense expense = new Expense();
        ExpenseAttachment attachment = new ExpenseAttachment();
        attachment.setId(1L);
        attachment.setFileName("file.jpg");
        attachment.setExpense(expense);

        assertEquals(1L, attachment.getId());
        assertEquals("file.jpg", attachment.getFileName());
        assertEquals(expense, attachment.getExpense());

        ExpenseAttachment attachment2 = new ExpenseAttachment("file2.jpg", expense);
        assertEquals("file2.jpg", attachment2.getFileName());
    }

    @Test
    void testExpenseComment() {
        Expense expense = new Expense();
        User user = new User();
        ExpenseComment comment = new ExpenseComment();
        comment.setId(1L);
        comment.setMessage("Test comment");
        comment.setTimestamp(java.time.LocalDateTime.now());
        comment.setExpense(expense);
        comment.setUser(user);

        assertEquals(1L, comment.getId());
        assertEquals("Test comment", comment.getMessage());
        assertNotNull(comment.getTimestamp());
        assertEquals(expense, comment.getExpense());
        assertEquals(user, comment.getUser());

        ExpenseComment comment2 = new ExpenseComment(expense, user, "Msg");
        assertEquals("Msg", comment2.getMessage());
    }
}
