package com.mushroom.expense.controller;

import com.mushroom.expense.entity.Income;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.service.CategoryService;
import com.mushroom.expense.service.IncomeService;
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

@WebMvcTest(IncomeController.class)
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncomeService incomeService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserService userService;

    private User managerUser;
    private User accountantUser;
    private Income income;

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

        income = new Income();
        income.setId(1L);
        income.setStatus(IncomeStatus.DRAFT);
        income.setUser(managerUser);
        income.setCategory(new com.mushroom.expense.entity.Category());
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void myIncomes_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(incomeService.getIncomes(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/my-incomes"))
                .andExpect(status().isOk())
                .andExpect(view().name("income_dashboard"))
                .andExpect(model().attributeExists("myDrafts"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void adminIncomeApprovals_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));
        when(incomeService.getIncomes(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/admin/income-approvals"))
                .andExpect(status().isOk())
                .andExpect(view().name("income_approvals"))
                .andExpect(model().attributeExists("submittedIncomes"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void viewIncome_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(incomeService.findById(1L)).thenReturn(Optional.of(income));

        mockMvc.perform(get("/income/view/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("income_view"))
                .andExpect(model().attributeExists("income"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void approveIncome_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/income/approve/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?*"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void deleteIncome_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));

        mockMvc.perform(get("/income/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/my-incomes?*"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void rejectIncome_WithComment() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/income/reject/1")
                .param("message", "Rejection Reason")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?*"));
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    void queryIncome_Success() throws Exception {
        when(userService.findByUsername("accountant")).thenReturn(Optional.of(accountantUser));

        mockMvc.perform(post("/income/query/1")
                .param("message", "Query Message")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/dashboard?*"));
    }

    @Test
    @WithMockUser(username = "manager", roles = "MANAGER")
    void exportIncomes_Success() throws Exception {
        when(userService.findByUsername("manager")).thenReturn(Optional.of(managerUser));
        when(incomeService.getIncomes(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/income/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"));
    }
}
