package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.report.ReportData.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private CapitalTransactionRepository capitalTransactionRepository;
    @Mock
    private LoanTransactionRepository loanTransactionRepository;
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private CapitalService capitalService;
    @Mock
    private LoanService loanService;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
                expenseRepository,
                incomeRepository,
                capitalTransactionRepository,
                loanTransactionRepository,
                loanRepository,
                capitalService,
                loanService
        );
    }

    @Test
    void testFuturePeriodRejected() {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.MONTHLY);
        request.setYear(2099);
        request.setMonth(1);

        assertThrows(IllegalArgumentException.class, () -> reportService.compileReportData(request));
    }

    @Test
    void testInvalidMonthValidation() {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.MONTHLY);
        request.setYear(2025);
        request.setMonth(13);

        assertThrows(IllegalArgumentException.class, () -> reportService.compileReportData(request));
    }

    @Test
    void testMonthNotMatchingFyRejected() {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.MONTHLY);
        request.setFy("2024-25");
        request.setYear(2025);
        request.setMonth(5); // May 2025 belongs to 2025-26, not 2024-25

        assertThrows(IllegalArgumentException.class, () -> reportService.compileReportData(request));
    }

    @Test
    void testMonthlyReportCompilation() {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.MONTHLY);
        request.setYear(2025);
        request.setMonth(10);
        request.setRequestedBy("test_accountant");

        // Mock global lifetime totals
        when(expenseRepository.findSumByCategory(ExpenseStatus.APPROVED))
                .thenReturn(Collections.singletonList(new Object[]{"Spawn", 12000.0}));
        when(incomeRepository.findSumByCategory(IncomeStatus.APPROVED))
                .thenReturn(Collections.singletonList(new Object[]{"Mushroom Sales", 45000.0}));
        when(capitalService.getTotalApprovedIntroduced()).thenReturn(100000.0);
        when(capitalService.getTotalApprovedDrawings()).thenReturn(10000.0);
        when(capitalService.getNetCapitalPool()).thenReturn(90000.0);
        when(loanService.getTotalApprovedBorrowed()).thenReturn(50000.0);
        when(loanService.getTotalApprovedRepaid()).thenReturn(20000.0);
        when(loanService.getOutstandingLoanPool()).thenReturn(30000.0);
        when(loanService.getAllLoanSummaries()).thenReturn(Collections.emptyList());

        // Mock period expenses
        Expense exp = new Expense();
        exp.setId(101L);
        exp.setDate(LocalDate.of(2025, 10, 15));
        exp.setAmount(2500.0);
        exp.setTaxPercentage(18.0);
        exp.setStatus(ExpenseStatus.APPROVED);
        exp.setDescription("Compost bags");
        when(expenseRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(eq(ExpenseStatus.APPROVED), any(), any()))
                .thenReturn(Collections.singletonList(exp));

        // Mock period income
        Income inc = new Income();
        inc.setId(201L);
        inc.setDate(LocalDate.of(2025, 10, 20));
        inc.setAmount(8000.0);
        inc.setStatus(IncomeStatus.APPROVED);
        inc.setDescription("Fresh Button Mushrooms");
        when(incomeRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(eq(IncomeStatus.APPROVED), any(), any()))
                .thenReturn(Collections.singletonList(inc));

        // Mock period capital
        CapitalTransaction cap = new CapitalTransaction();
        cap.setId(301L);
        cap.setType(CapitalTransactionType.INTRODUCED);
        cap.setDate(LocalDate.of(2025, 10, 5));
        cap.setAmount(5000.0);
        cap.setStatus(IncomeStatus.APPROVED);
        when(capitalTransactionRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(eq(IncomeStatus.APPROVED), any(), any()))
                .thenReturn(Collections.singletonList(cap));

        // Mock period loans
        LoanTransaction loanTx = new LoanTransaction();
        loanTx.setId(401L);
        loanTx.setType(LoanTransactionType.REPAYMENT);
        loanTx.setDate(LocalDate.of(2025, 10, 28));
        loanTx.setAmount(3000.0);
        loanTx.setStatus(IncomeStatus.APPROVED);
        when(loanTransactionRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(eq(IncomeStatus.APPROVED), any(), any()))
                .thenReturn(Collections.singletonList(loanTx));

        when(loanRepository.findAll()).thenReturn(Collections.emptyList());

        // Run
        ReportDataContainer container = reportService.compileReportData(request);

        assertNotNull(container);
        assertNotNull(container.getLifetimeFinancials());
        assertNotNull(container.getSummary());
        assertNotNull(container.getExpensesSection());
        assertNotNull(container.getIncomeSection());
        assertNotNull(container.getCapitalSection());
        assertNotNull(container.getLoansSection());

        // Verify Lifetime Figures
        LifetimeFinancials lf = container.getLifetimeFinancials();
        assertEquals(45000.0, lf.getGlobalTotalIncome());
        assertEquals(12000.0, lf.getGlobalTotalExpense());
        assertEquals(33000.0, lf.getGlobalNetBalance());
        assertEquals(90000.0, lf.getGlobalNetCapital());
        assertEquals(30000.0, lf.getGlobalOutstandingLoans());
        assertEquals(33000.0 + 90000.0 + 30000.0, lf.getGlobalNetCashPosition());

        // Verify Period Summary figures
        PeriodSummary sum = container.getSummary();
        assertEquals(8000.0, sum.getTotalIncome());
        assertEquals(2500.0, sum.getTotalExpense());
        assertEquals(5500.0, sum.getNetBalance()); // 8000 - 2500
        assertEquals(5000.0, sum.getCapitalIntroduced());
        assertEquals(0.0, sum.getCapitalDrawings());
        assertEquals(5000.0, sum.getNetCapitalFlow());
        assertEquals(0.0, sum.getLoansBorrowed());
        assertEquals(3000.0, sum.getLoansRepaid());
        assertEquals(-3000.0, sum.getNetLoanFlow());
        assertEquals(5500.0 + 5000.0 - 3000.0, sum.getNetCashFlow()); // 7500.0

        // Verify Expenses Section
        ScheduleSection expSec = container.getExpensesSection();
        assertEquals(1, expSec.getRecordCount());
        assertEquals(2500.0, expSec.getTotalAmount());
        assertEquals(2500.0 * 0.18, expSec.getTotalTax());
    }

    @Test
    void testYearlyReportMonthlyBreakdown() {
        ReportRequest request = new ReportRequest();
        request.setType(ReportType.YEARLY);
        request.setFy("2024-25");
        request.setRequestedBy("admin");

        when(expenseRepository.findSumByCategory(any())).thenReturn(Collections.emptyList());
        when(incomeRepository.findSumByCategory(any())).thenReturn(Collections.emptyList());

        Expense aprExp = new Expense();
        aprExp.setId(1L);
        aprExp.setDate(LocalDate.of(2024, 4, 10)); // Apr -> idx 0
        aprExp.setAmount(1000.0);
        aprExp.setStatus(ExpenseStatus.APPROVED);

        Expense marExp = new Expense();
        marExp.setId(2L);
        marExp.setDate(LocalDate.of(2025, 3, 20)); // Mar -> idx 11
        marExp.setAmount(2000.0);
        marExp.setStatus(ExpenseStatus.APPROVED);

        when(expenseRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(eq(ExpenseStatus.APPROVED), any(), any()))
                .thenReturn(List.of(aprExp, marExp));

        ReportDataContainer container = reportService.compileReportData(request);

        ScheduleSection expSec = container.getExpensesSection();
        assertEquals(2, expSec.getRecordCount());
        assertEquals(3000.0, expSec.getTotalAmount());
        assertNotNull(expSec.getMonthlyBreakdown());
        assertEquals(12, expSec.getMonthlyBreakdown().size());
        assertEquals(1000.0, expSec.getMonthlyBreakdown().get(0));  // Apr
        assertEquals(0.0, expSec.getMonthlyBreakdown().get(1));     // May
        assertEquals(2000.0, expSec.getMonthlyBreakdown().get(11)); // Mar
    }
}
