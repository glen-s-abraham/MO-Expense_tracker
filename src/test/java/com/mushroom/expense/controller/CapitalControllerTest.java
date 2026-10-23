package com.mushroom.expense.controller;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.service.CapitalService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CapitalController.class)
class CapitalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CapitalService capitalService;

    @MockBean
    private UserService userService;

    private User adminUser;
    private CapitalTransaction capitalTx;

    @BeforeEach
    void setUp() {
        adminUser = new User("admin", "password", "ROLE_ADMIN");
        adminUser.setId(1L);

        capitalTx = new CapitalTransaction();
        capitalTx.setId(10L);
        capitalTx.setType(CapitalTransactionType.INTRODUCED);
        capitalTx.setAmount(50000.0);
        capitalTx.setDate(LocalDate.now());
        capitalTx.setPaymentMode(PaymentMode.BANK_TRANSFER);
        capitalTx.setDescription("Initial Capital");
        capitalTx.setStatus(IncomeStatus.DRAFT);
        capitalTx.setUser(adminUser);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void capitalDashboard_RendersSuccessfully() throws Exception {
        when(userService.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(capitalService.getCapitalTransactions(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());
        when(capitalService.getTotalApprovedIntroduced()).thenReturn(190000.0);
        when(capitalService.getTotalApprovedDrawings()).thenReturn(0.0);
        when(capitalService.getNetCapitalPool()).thenReturn(190000.0);

        mockMvc.perform(get("/capital"))
                .andExpect(status().isOk())
                .andExpect(view().name("capital/dashboard"))
                .andExpect(model().attributeExists("totalIntroduced", "totalDrawings", "netCapitalPool"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void approveCapital_RedirectsToApprovals() throws Exception {
        when(capitalService.updateStatus(10L, IncomeStatus.APPROVED)).thenReturn(capitalTx);

        mockMvc.perform(post("/capital/approve/10").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/approvals"));
    }
}
