package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.LoanService;
import com.mushroom.expense.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private UserService userService;

    private User adminUser;
    private Loan loan;
    private LoanTransaction loanTx;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "password", "ROLE_ADMIN");
        adminUser.setId(1L);

        loan = new Loan("Unsecured Loan From Father", "Father", "Seed loan", LocalDate.now());
        loan.setId(1L);

        loanTx = new LoanTransaction();
        loanTx.setId(5L);
        loanTx.setLoan(loan);
        loanTx.setType(LoanTransactionType.BORROWED);
        loanTx.setAmount(170000.0);
        loanTx.setDate(LocalDate.now());
        loanTx.setPaymentMode(PaymentMode.BANK_TRANSFER);
        loanTx.setDescription("Unsecured Loan From Father");
        loanTx.setStatus(IncomeStatus.APPROVED);
        loanTx.setUser(adminUser);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void loanDashboard_RendersSuccessfully() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(loanService.getLoanTransactions(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(loanService.getAllLoanSummaries()).thenReturn(List.of(
                new LoanService.LoanSummary(1L, "Unsecured Loan From Father", "Father", "Desc",
                        LocalDate.now(), LoanStatus.ACTIVE, 170000.0, 0.0, 170000.0, 0)));
        when(loanService.getTotalApprovedBorrowed()).thenReturn(170000.0);
        when(loanService.getTotalApprovedRepaid()).thenReturn(0.0);
        when(loanService.getOutstandingLoanPool()).thenReturn(170000.0);

        mockMvc.perform(get("/loans"))
                .andExpect(status().isOk())
                .andExpect(view().name("loan/dashboard"))
                .andExpect(model().attributeExists("loanSummaries", "totalBorrowed", "totalRepaid", "outstandingLoanPool"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void approveLoan_RedirectsToApprovals() throws Exception {
        when(loanService.updateStatus(5L, IncomeStatus.APPROVED)).thenReturn(loanTx);

        mockMvc.perform(post("/loans/approve/5").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/approvals"));
    }
}
