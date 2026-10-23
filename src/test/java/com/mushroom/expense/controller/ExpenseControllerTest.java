package com.mushroom.expense.controller;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.service.CategoryService;
import com.mushroom.expense.service.ExpenseService;
import com.mushroom.expense.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    @MockBean
    private com.mushroom.expense.service.IncomeService incomeService;

    @MockBean
    private com.mushroom.expense.service.CapitalService capitalService;

    @MockBean
    private com.mushroom.expense.service.LoanService loanService;

    private User managerUser;
    private User accountantUser;
    private Expense expense;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setId(1L);
        managerUser.setUsername("manager");
        managerUser.setRole("ROLE_MANAGER");

        accountantUser = new User();
        accountantUser.setId(2L);
        accountantUser.setUsername("accountant");
        accountantUser.setRole("ROLE_ACCOUNTANT");

        expense = new Expense();
        expense.setId(1L);
        expense.setStatus(ExpenseStatus.DRAFT);
        expense.setUser(managerUser); // Set user to avoid null pointer in view
        expense.setCategory(new com.mushroom.expense.entity.Category()); // Avoid null pointer
        expense.setSubCategory(new com.mushroom.expense.entity.SubCategory()); // Avoid null pointer
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void dashboard_Manager_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

        mockMvc.perform(get("/approvals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-expenses"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void myExpenses_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(expenseService.getExpenses(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty()); // Mock page return

        mockMvc.perform(get("/my-expenses"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager/approvals"))
                .andExpect(model().attributeExists("myDrafts"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void dashboard_Accountant_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));
        when(expenseService.getExpenses(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty()); // Mock page return
        when(incomeService.getIncomes(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(capitalService.getCapitalTransactions(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(loanService.getLoanTransactions(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/approvals"))
                .andExpect(status().isOk())
                .andExpect(view().name("accountant/approvals"))
                .andExpect(model().attributeExists("submittedExpenses", "submittedCapitals", "submittedLoans"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void viewExpense_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(expenseService.findById(1L)).thenReturn(Optional.of(expense));

        mockMvc.perform(get("/expense/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("expense_view"))
                .andExpect(model().attributeExists("expense"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void approveExpense_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/expense/approve/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/approvals?*"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void deleteExpense_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

        mockMvc.perform(get("/expense/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/my-expenses?*"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void rejectExpense_WithComment() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/expense/reject/1")
                .param("message", "Rejection Reason")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/approvals?*"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void queryExpense_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/expense/query/1")
                .param("message", "Query Message")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/approvals?*"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void exportExpenses_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(expenseService.getExpenses(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/expense/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
