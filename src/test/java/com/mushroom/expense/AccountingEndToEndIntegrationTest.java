package com.mushroom.expense;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.CapitalService;
import com.mushroom.expense.service.ExpenseService;
import com.mushroom.expense.service.IncomeService;
import com.mushroom.expense.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full End-to-End integration test suite for the 4-Pillar Financial Accounting Architecture:
 * 1. Capital (Introduced, Drawings, Net Math, Commenting, Approvals, Edge Validations)
 * 2. Loans (Facility, Borrowed, Repayment, Over-repayment prevention, Auto-Closure, Reactivation)
 * 3. Operational Income & Expenses (P&L separation)
 * 4. Dashboard Metrics, FY/Monthly Breakdown & Drilldown REST APIs
 * 5. RBAC & Manager auto-redirection
 * 6. Pagination & Static Resources
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AccountingEndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CapitalTransactionRepository capitalTransactionRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Pillar 1: Complete Capital Lifecycle (Introduced, Query, Submit, Approve, Drawings, Zero/Negative Validation)")
    @WithMockUser(username = "admin", roles = {"ADMIN", "ACCOUNTANT"})
    void testCapitalCompleteLifecycleAndCalculations() throws Exception {
        double initialIntroduced = capitalService.getTotalApprovedIntroduced();
        double initialDrawings = capitalService.getTotalApprovedDrawings();
        double initialNet = capitalService.getNetCapitalPool();

        // 1. Validation Edge Case: Attempt saving zero or negative capital amount
        mockMvc.perform(post("/capital")
                        .with(csrf())
                        .param("type", "INTRODUCED")
                        .param("ownerName", "Glen")
                        .param("amount", "-5000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Negative capital injection"))
                .andExpect(status().isOk())
                .andExpect(view().name("capital/form"))
                .andExpect(model().attributeExists("errorMessage"));

        // 2. Create Valid Capital Introduced as DRAFT
        mockMvc.perform(post("/capital")
                        .with(csrf())
                        .param("type", "INTRODUCED")
                        .param("ownerName", "Glen Partner")
                        .param("amount", "50000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Q4 Expansion Capital"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/capital?tab=INTRODUCED"));

        List<CapitalTransaction> introList = capitalTransactionRepository.findAll().stream()
                .filter(c -> c.getType() == CapitalTransactionType.INTRODUCED && c.getStatus() == IncomeStatus.DRAFT)
                .toList();
        CapitalTransaction introTx = introList.stream()
                .filter(c -> "Q4 Expansion Capital".equals(c.getDescription()))
                .findFirst().orElseThrow();
        assertEquals(IncomeStatus.DRAFT, introTx.getStatus());
        assertEquals(50000.0, introTx.getAmount(), 0.01);

        // While DRAFT, net capital pool must remain unaffected
        assertEquals(initialNet, capitalService.getNetCapitalPool(), 0.01);

        // 3. Reviewer queries the draft/submitted transaction
        mockMvc.perform(post("/capital/query/" + introTx.getId())
                        .with(csrf())
                        .param("message", "Please attach bank transfer receipt")
                        .param("redirectTo", "/approvals"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/approvals"));

        introTx = capitalService.findById(introTx.getId()).orElseThrow();
        assertEquals(IncomeStatus.QUERIES_RAISED, introTx.getStatus());
        assertEquals(1, capitalService.getComments(introTx.getId()).size());
        assertEquals("Please attach bank transfer receipt", capitalService.getComments(introTx.getId()).get(0).getMessage());

        // 4. Submitter submits the capital transaction
        mockMvc.perform(post("/capital/submit/" + introTx.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/capital?tab=INTRODUCED"));

        introTx = capitalService.findById(introTx.getId()).orElseThrow();
        assertEquals(IncomeStatus.SUBMITTED, introTx.getStatus());

        // 5. Reviewer approves the capital transaction
        mockMvc.perform(post("/capital/approve/" + introTx.getId())
                        .with(csrf())
                        .param("redirectTo", "/approvals"))
                .andExpect(status().is3xxRedirection());

        introTx = capitalService.findById(introTx.getId()).orElseThrow();
        assertEquals(IncomeStatus.APPROVED, introTx.getStatus());

        // Net capital increases by ₹50,000
        assertEquals(initialIntroduced + 50000.0, capitalService.getTotalApprovedIntroduced(), 0.01);
        assertEquals(initialNet + 50000.0, capitalService.getNetCapitalPool(), 0.01);

        // 6. Record Capital DRAWINGS (Owner Withdrawal)
        mockMvc.perform(post("/capital")
                        .with(csrf())
                        .param("type", "DRAWINGS")
                        .param("ownerName", "Glen Partner")
                        .param("amount", "12000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Personal Tax Withdrawal"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/capital?tab=DRAWINGS"));

        List<CapitalTransaction> drawList = capitalTransactionRepository.findAll().stream()
                .filter(c -> c.getType() == CapitalTransactionType.DRAWINGS && c.getStatus() == IncomeStatus.DRAFT)
                .toList();
        CapitalTransaction drawTx = drawList.stream()
                .filter(c -> "Personal Tax Withdrawal".equals(c.getDescription()))
                .findFirst().orElseThrow();

        // Submit drawings
        mockMvc.perform(post("/capital/submit/" + drawTx.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Approve drawings
        mockMvc.perform(post("/capital/approve/" + drawTx.getId())
                        .with(csrf())
                        .param("redirectTo", "/approvals"))
                .andExpect(status().is3xxRedirection());

        // Net capital decreases by ₹12,000
        assertEquals(initialDrawings + 12000.0, capitalService.getTotalApprovedDrawings(), 0.01);
        assertEquals(initialNet + 50000.0 - 12000.0, capitalService.getNetCapitalPool(), 0.01);
    }

    @Test
    @DisplayName("Pillar 2: Complete Loan Lifecycle (Facility, Borrowed, Over-Repayment Prevention, Auto-Close, Reactivate)")
    @WithMockUser(username = "admin", roles = {"ADMIN", "ACCOUNTANT"})
    void testLoanCompleteLifecycleAndOverRepaymentPrevention() throws Exception {
        // 1. Create a brand new loan facility
        mockMvc.perform(post("/loans/facility")
                        .with(csrf())
                        .param("loanName", "Canara Bank Farm Term Loan")
                        .param("lenderName", "Canara Bank")
                        .param("description", "Solar Setup 5-Year Loan"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loans"));

        Loan loan = loanRepository.findAll().stream()
                .filter(l -> "Canara Bank Farm Term Loan".equals(l.getLoanName()))
                .findFirst().orElseThrow();
        final Long loanId = loan.getId();
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(0.0, loanService.getLoanOutstandingBalance(loanId), 0.01);

        // 2. Edge Case: Attempt saving zero or negative loan transaction amount
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "BORROWED")
                        .param("amount", "0.0")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Zero disbursement"))
                .andExpect(status().isOk())
                .andExpect(view().name("loan/form"))
                .andExpect(model().attributeExists("errorMessage"));

        // 3. Record valid BORROWED of ₹80,000
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "BORROWED")
                        .param("amount", "80000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Disbursement tranche 1"))
                .andExpect(status().is3xxRedirection());

        List<LoanTransaction> borrowedList = loanTransactionRepository.findAll().stream()
                .filter(t -> t.getLoan().getId().equals(loanId) && t.getType() == LoanTransactionType.BORROWED && t.getStatus() == IncomeStatus.DRAFT)
                .toList();
        LoanTransaction borrowTx = borrowedList.get(0);

        // Submit & Approve borrowing
        mockMvc.perform(post("/loans/submit/" + borrowTx.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/loans/approve/" + borrowTx.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Verify outstanding is ₹80,000 and status is ACTIVE
        assertEquals(80000.0, loanService.getLoanOutstandingBalance(loanId), 0.01);
        loan = loanService.findLoanById(loanId).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());

        // 4. CRITICAL EDGE CASE: Over-repayment prevention (Attempt ₹90,000 repayment against ₹80,000 balance)
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "REPAYMENT")
                        .param("amount", "90000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Over-repayment attempt"))
                .andExpect(status().isOk())
                .andExpect(view().name("loan/form"))
                .andExpect(model().attribute("errorMessage",
                        containsString("exceeds the outstanding principal balance")));

        // Outstanding balance remains strictly ₹80,000.00
        assertEquals(80000.0, loanService.getLoanOutstandingBalance(loanId), 0.01);

        // 5. Valid partial repayment of ₹30,000
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "REPAYMENT")
                        .param("amount", "30000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Tranche 1 repayment"))
                .andExpect(status().is3xxRedirection());

        List<LoanTransaction> repayList1 = loanTransactionRepository.findAll().stream()
                .filter(t -> t.getLoan().getId().equals(loanId) && t.getType() == LoanTransactionType.REPAYMENT && t.getStatus() == IncomeStatus.DRAFT)
                .toList();
        LoanTransaction repayTx1 = repayList1.get(0);

        mockMvc.perform(post("/loans/submit/" + repayTx1.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/loans/approve/" + repayTx1.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Outstanding is now ₹50,000
        assertEquals(50000.0, loanService.getLoanOutstandingBalance(loanId), 0.01);
        loan = loanService.findLoanById(loanId).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());

        // 6. Complete settlement repayment of remaining ₹50,000 -> Loan status MUST auto-transition to CLOSED
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "REPAYMENT")
                        .param("amount", "50000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Full final settlement"))
                .andExpect(status().is3xxRedirection());

        List<LoanTransaction> repayList2 = loanTransactionRepository.findAll().stream()
                .filter(t -> t.getLoan().getId().equals(loanId) && t.getType() == LoanTransactionType.REPAYMENT && t.getStatus() == IncomeStatus.DRAFT)
                .toList();
        LoanTransaction repayTx2 = repayList2.get(0);

        mockMvc.perform(post("/loans/submit/" + repayTx2.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/loans/approve/" + repayTx2.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        // Outstanding is now exactly 0, loan status MUST be CLOSED
        assertEquals(0.0, loanService.getLoanOutstandingBalance(loanId), 0.01);
        loan = loanService.findLoanById(loanId).orElseThrow();
        assertEquals(LoanStatus.CLOSED, loan.getStatus());

        // 7. Post-Closure Borrowing -> Re-activates the closed loan
        mockMvc.perform(post("/loans")
                        .with(csrf())
                        .param("loan.id", loanId.toString())
                        .param("type", "BORROWED")
                        .param("amount", "25000.00")
                        .param("paymentMode", "BANK_TRANSFER")
                        .param("description", "Reopening line of credit"))
                .andExpect(status().is3xxRedirection());

        List<LoanTransaction> borrowList2 = loanTransactionRepository.findAll().stream()
                .filter(t -> t.getLoan().getId().equals(loanId) && t.getType() == LoanTransactionType.BORROWED && t.getStatus() == IncomeStatus.DRAFT)
                .toList();
        LoanTransaction borrowTx2 = borrowList2.get(0);

        mockMvc.perform(post("/loans/submit/" + borrowTx2.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/loans/approve/" + borrowTx2.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        assertEquals(25000.0, loanService.getLoanOutstandingBalance(loan.getId()), 0.01);
        loan = loanService.findLoanById(loan.getId()).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    @DisplayName("Pillars 3 & 4: Operational Income and Expense Workflows")
    @WithMockUser(username = "admin", roles = {"ADMIN", "ACCOUNTANT"})
    void testOperationalIncomeAndExpenseLifecycle() throws Exception {
        Category incomeCat = categoryRepository.findByType(TransactionType.INCOME).stream().findFirst().orElseGet(() -> {
            Category c = new Category();
            c.setName("Sales Income E2E");
            c.setType(TransactionType.INCOME);
            return categoryRepository.save(c);
        });

        Category expenseCat = categoryRepository.findByType(TransactionType.EXPENSE).stream()
                .filter(c -> c.getSubCategories() != null && !c.getSubCategories().isEmpty())
                .findFirst().orElseGet(() -> {
                    Category c = new Category();
                    c.setName("Farm Supplies E2E");
                    c.setType(TransactionType.EXPENSE);
                    Category saved = categoryRepository.save(c);
                    SubCategory sub = new SubCategory();
                    sub.setName("Sub Supplies E2E");
                    sub.setCategory(saved);
                    saved.getSubCategories().add(sub);
                    return categoryRepository.save(saved);
                });
        SubCategory expenseSubCat = expenseCat.getSubCategories().get(0);

        // 1. Record Operational Income
        mockMvc.perform(multipart("/income")
                        .with(csrf())
                        .param("category.id", incomeCat.getId().toString())
                        .param("amount", "15000.00")
                        .param("paymentMode", "CASH")
                        .param("description", "Wholesale mushroom crate sale"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-incomes"));

        Income income = incomeService.getIncomes(null, List.of(IncomeStatus.DRAFT), null, null, null, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .filter(i -> "Wholesale mushroom crate sale".equals(i.getDescription()))
                .findFirst().orElseThrow();

        // Submit & Approve income
        mockMvc.perform(post("/income/submit/" + income.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/income/approve/" + income.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        Income approvedIncome = incomeService.findById(income.getId()).orElseThrow();
        assertEquals(IncomeStatus.APPROVED, approvedIncome.getStatus());

        // 2. Record Operational Expense
        mockMvc.perform(multipart("/expense")
                        .with(csrf())
                        .param("category.id", expenseCat.getId().toString())
                        .param("subCategory.id", expenseSubCat.getId().toString())
                        .param("amount", "4500.00")
                        .param("paymentMode", "CASH")
                        .param("description", "Spawn and substrate bags"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-expenses"));

        Expense expense = expenseService.getExpenses(null, List.of(ExpenseStatus.DRAFT), null, null, null, null,
                org.springframework.data.domain.Pageable.unpaged()).getContent().stream()
                .filter(e -> "Spawn and substrate bags".equals(e.getDescription()))
                .findFirst().orElseThrow();

        mockMvc.perform(post("/expense/submit/" + expense.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/expense/approve/" + expense.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection());

        Expense approvedExpense = expenseService.findById(expense.getId()).orElseThrow();
        assertEquals(ExpenseStatus.APPROVED, approvedExpense.getStatus());
    }

    @Test
    @DisplayName("Dashboard: Global P&L, Balance Sheet, Drilldown Modals and REST Endpoints")
    @WithMockUser(username = "madhuri", roles = "ACCOUNTANT")
    void testDashboardMetricsAndRestEndpoints() throws Exception {
        // 1. Graphical Dashboard View
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists(
                        "globalTotalExpense",
                        "globalTotalIncome",
                        "globalNetBalance",
                        "globalCapitalIntroduced",
                        "globalCapitalDrawings",
                        "globalNetCapital",
                        "globalLoansBorrowed",
                        "globalLoansRepaid",
                        "globalOutstandingLoans",
                        "globalNetCashPosition"
                ));

        // 2. REST API: Loan Pool Drilldown
        mockMvc.perform(get("/api/dashboard/loan-pool-breakdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", notNullValue()));

        // 3. REST API: Capital Pool Drilldown
        mockMvc.perform(get("/api/dashboard/capital-pool-breakdown"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalIntroduced", notNullValue()))
                .andExpect(jsonPath("$.totalDrawings", notNullValue()))
                .andExpect(jsonPath("$.netCapitalPool", notNullValue()));

        // 4. REST API: Category Breakdown
        mockMvc.perform(get("/api/dashboard/category-breakdown")
                        .param("type", "INCOME")
                        .param("category", "Income From Sales")
                        .param("startDate", "2020-01-01")
                        .param("endDate", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Security & RBAC: Manager Auto-Redirection and Approvals Queues across Roles")
    void testRbacAndAutoRedirection() throws Exception {
        // 1. Manager accessing /dashboard MUST be gracefully redirected to /my-expenses (not 403 Forbidden)
        mockMvc.perform(get("/dashboard")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("liya").roles("MANAGER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-expenses"));

        // 2. Manager accessing /approvals MUST be redirected to /my-expenses
        mockMvc.perform(get("/approvals")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("liya").roles("MANAGER")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-expenses"));

        // 3. Accountant accessing /approvals renders unified view with all 4 pillars
        mockMvc.perform(get("/approvals")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("madhuri").roles("ACCOUNTANT")))
                .andExpect(status().isOk())
                .andExpect(view().name("accountant/approvals"))
                .andExpect(model().attributeExists("submittedExpenses", "submittedIncomes", "submittedCapitals", "submittedLoans"));

        // 4. Supervisor accessing /approvals renders supervisor view with all 4 pillars
        mockMvc.perform(get("/approvals")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("glen-sup").roles("SUPERVISOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("supervisor/approvals"))
                .andExpect(model().attributeExists("submittedExpenses", "submittedIncomes", "submittedCapitals", "submittedLoans"));

        // 5. Admin accessing /approvals and /admin/approvals renders successfully
        mockMvc.perform(get("/approvals")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("accountant/approvals"));

        mockMvc.perform(get("/admin/approvals")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("accountant/approvals"));

        // 6. Unauthenticated request to protected endpoint redirects to /login
        mockMvc.perform(get("/capital"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("Pagination & Client Script Serving")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void testPaginationAndClientScript() throws Exception {
        // 1. Pagination script is served
        mockMvc.perform(get("/js/pagination.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Truncates long Bootstrap pagination lists")));

        // 2. Pagination query parameters render without template exceptions
        mockMvc.perform(get("/capital")
                        .param("draftsPage", "1")
                        .param("pendingPage", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("capital/dashboard"));

        mockMvc.perform(get("/loans")
                        .param("draftsPage", "1")
                        .param("pendingPage", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("loan/dashboard"));
    }
}
