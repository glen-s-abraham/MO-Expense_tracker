package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.report.FinancialYear;
import com.mushroom.expense.service.report.ReportData;
import com.mushroom.expense.service.report.ReportData.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final CapitalTransactionRepository capitalTransactionRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final LoanRepository loanRepository;
    private final CapitalService capitalService;
    private final LoanService loanService;

    public ReportService(
            ExpenseRepository expenseRepository,
            IncomeRepository incomeRepository,
            CapitalTransactionRepository capitalTransactionRepository,
            LoanTransactionRepository loanTransactionRepository,
            LoanRepository loanRepository,
            CapitalService capitalService,
            LoanService loanService) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.capitalTransactionRepository = capitalTransactionRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.loanRepository = loanRepository;
        this.capitalService = capitalService;
        this.loanService = loanService;
    }

    /**
     * Compiles complete report data container based on the provided request parameters.
     */
    public ReportDataContainer compileReportData(ReportRequest request) {
        validateAndResolveDates(request);

        ReportDataContainer container = new ReportDataContainer();
        container.setRequest(request);

        // 1. Lifetime Financials (All-Time Position)
        container.setLifetimeFinancials(compileLifetimeFinancials(request.getRequestedBy()));

        // 2. Financial Schedules (Ledger Details)
        ScheduleSection expensesSection = compileExpensesSection(request);
        ScheduleSection incomeSection = compileIncomeSection(request);
        ScheduleSection capitalSection = compileCapitalSection(request);
        ScheduleSection loansSection = compileLoansSection(request);

        container.setExpensesSection(expensesSection);
        container.setIncomeSection(incomeSection);
        container.setCapitalSection(capitalSection);
        container.setLoansSection(loansSection);

        // 3. Period Summary (Cover & Reconciliation)
        container.setSummary(compilePeriodSummary(request, expensesSection, incomeSection, capitalSection, loansSection));

        return container;
    }

    private void validateAndResolveDates(ReportRequest request) {
        if (request.getType() == null) {
            request.setType(ReportType.MONTHLY);
        }
        if (request.getFormat() == null) {
            request.setFormat(ReportFormat.XLSX);
        }

        if (request.getType() == ReportType.MONTHLY) {
            if (request.getYear() == null || request.getMonth() == null) {
                LocalDate now = LocalDate.now();
                request.setYear(now.getYear());
                request.setMonth(now.getMonthValue());
            }
            if (request.getMonth() < 1 || request.getMonth() > 12) {
                throw new IllegalArgumentException("Invalid month: " + request.getMonth());
            }

            String derivedFy = FinancialYear.ofYearMonth(request.getYear(), request.getMonth());
            if (request.getFy() != null && !request.getFy().isBlank()) {
                if (!FinancialYear.containsMonth(request.getFy(), request.getYear(), request.getMonth())) {
                    throw new IllegalArgumentException(String.format("Month %02d/%d does not belong to Financial Year %s",
                            request.getMonth(), request.getYear(), request.getFy()));
                }
            } else {
                request.setFy(derivedFy);
            }

            LocalDate start = LocalDate.of(request.getYear(), request.getMonth(), 1);
            LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
            request.setStartDate(start);
            request.setEndDate(end);

            String monthName = Month.of(request.getMonth()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            request.setPeriodLabel(String.format("%s %d (FY %s)", monthName, request.getYear(), request.getFy()));

        } else { // YEARLY
            if (request.getFy() == null || request.getFy().isBlank()) {
                request.setFy(FinancialYear.currentFy());
            }
            request.setStartDate(FinancialYear.getStartDate(request.getFy()));
            request.setEndDate(FinancialYear.getEndDate(request.getFy()));
            request.setPeriodLabel("FY " + request.getFy());
        }

        if (request.getStartDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot generate report for future period: " + request.getPeriodLabel());
        }
    }

    private LifetimeFinancials compileLifetimeFinancials(String requestedBy) {
        LifetimeFinancials lf = new LifetimeFinancials();
        lf.setAsOfDate(LocalDateTime.now());
        lf.setGeneratedBy(requestedBy != null ? requestedBy : "System");

        List<Object[]> globalExpenses = expenseRepository.findSumByCategory(ExpenseStatus.APPROVED);
        List<Object[]> globalIncomes = incomeRepository.findSumByCategory(IncomeStatus.APPROVED);

        double totalExpense = globalExpenses.stream().mapToDouble(obj -> ((Number) obj[1]).doubleValue()).sum();
        double totalIncome = globalIncomes.stream().mapToDouble(obj -> ((Number) obj[1]).doubleValue()).sum();
        double netBalance = totalIncome - totalExpense;

        double capIntro = capitalService.getTotalApprovedIntroduced();
        double capDraw = capitalService.getTotalApprovedDrawings();
        double netCap = capitalService.getNetCapitalPool();

        double loansBorrowed = loanService.getTotalApprovedBorrowed();
        double loansRepaid = loanService.getTotalApprovedRepaid();
        double outstandingLoans = loanService.getOutstandingLoanPool();

        double netCashPosition = netBalance + netCap + outstandingLoans;

        lf.setGlobalTotalExpense(totalExpense);
        lf.setGlobalTotalIncome(totalIncome);
        lf.setGlobalNetBalance(netBalance);
        lf.setGlobalCapitalIntroduced(capIntro);
        lf.setGlobalCapitalDrawings(capDraw);
        lf.setGlobalNetCapital(netCap);
        lf.setGlobalLoansBorrowed(loansBorrowed);
        lf.setGlobalLoansRepaid(loansRepaid);
        lf.setGlobalOutstandingLoans(outstandingLoans);
        lf.setGlobalNetCashPosition(netCashPosition);

        // Category breakdowns
        globalExpenses.sort((a, b) -> Double.compare(((Number) b[1]).doubleValue(), ((Number) a[1]).doubleValue()));
        for (Object[] obj : globalExpenses) {
            lf.getExpensesByCategory().add(new CategorySummary((String) obj[0], ((Number) obj[1]).doubleValue()));
        }

        globalIncomes.sort((a, b) -> Double.compare(((Number) b[1]).doubleValue(), ((Number) a[1]).doubleValue()));
        for (Object[] obj : globalIncomes) {
            lf.getIncomesByCategory().add(new CategorySummary((String) obj[0], ((Number) obj[1]).doubleValue()));
        }

        // Loan summaries
        List<LoanService.LoanSummary> summaries = loanService.getAllLoanSummaries();
        for (LoanService.LoanSummary s : summaries) {
            LoanFacilityReportItem item = new LoanFacilityReportItem();
            item.setLoanName(s.loanName());
            item.setLenderName(s.lenderName());
            item.setBorrowedInPeriod(s.totalBorrowed());
            item.setRepaidInPeriod(s.totalRepaid());
            item.setClosingOutstanding(s.outstandingBalance());
            item.setStatus(s.status() != null ? s.status().name() : "ACTIVE");
            lf.getLoanFacilities().add(item);
        }

        return lf;
    }

    private ScheduleSection compileExpensesSection(ReportRequest request) {
        ScheduleSection section = new ScheduleSection();
        section.setScheduleName("Operating Expenses");

        List<Expense> expenses = expenseRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(
                ExpenseStatus.APPROVED, request.getStartDate(), request.getEndDate());

        double totalAmount = 0.0;
        double totalTax = 0.0;
        Map<String, Double> categoryMap = new LinkedHashMap<>();
        List<Double> monthlyBreakdown = new ArrayList<>(Collections.nCopies(12, 0.0));

        for (Expense e : expenses) {
            double amt = e.getAmount() != null ? e.getAmount() : 0.0;
            totalAmount += amt;

            double taxPct = e.getTaxPercentage() != null ? e.getTaxPercentage() : 0.0;
            double taxAmt = (taxPct > 0) ? (amt * (taxPct / 100.0)) : 0.0;
            totalTax += taxAmt;

            String catName = e.getCategory() != null ? e.getCategory().getName() : "Uncategorized";
            categoryMap.put(catName, categoryMap.getOrDefault(catName, 0.0) + amt);

            if (request.getType() == ReportType.YEARLY && e.getDate() != null) {
                int fyIdx = FinancialYear.toFyMonthIndex(e.getDate().getMonthValue());
                monthlyBreakdown.set(fyIdx, monthlyBreakdown.get(fyIdx) + amt);
            }

            ReportRow row = new ReportRow();
            row.setId(e.getId());
            row.setDate(e.getDate());
            row.setReference("EXP-" + e.getId());
            row.setCategory(catName);
            row.setSubCategory(e.getSubCategory() != null ? e.getSubCategory().getName() : "-");
            row.setDescription(e.getDescription());
            row.setAmount(amt);
            row.setTaxPercentage(taxPct);
            row.setTaxAmount(taxAmt);
            row.setPaymentMode(e.getPaymentMode() != null ? e.getPaymentMode().name() : "-");
            row.setStatus(e.getStatus() != null ? e.getStatus().name() : "APPROVED");
            row.setSubmittedBy(e.getUser() != null ? e.getUser().getUsername() : "-");
            row.setApprovedBy("Approved");
            row.setHasAttachment((e.getAttachments() != null && !e.getAttachments().isEmpty()) ||
                    (e.getReceiptImage() != null && !e.getReceiptImage().isBlank()));
            row.setBatchId(e.getBatchId() != null ? e.getBatchId() : "-");

            section.getRows().add(row);
        }

        section.setRecordCount(expenses.size());
        section.setTotalAmount(totalAmount);
        section.setTotalTax(totalTax);
        section.setCategoryBreakdown(categoryMap);
        if (request.getType() == ReportType.YEARLY) {
            section.setMonthlyBreakdown(monthlyBreakdown);
        }

        return section;
    }

    private ScheduleSection compileIncomeSection(ReportRequest request) {
        ScheduleSection section = new ScheduleSection();
        section.setScheduleName("Revenue & Operating Income");

        List<Income> incomes = incomeRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(
                IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());

        double totalAmount = 0.0;
        double totalTax = 0.0;
        Map<String, Double> categoryMap = new LinkedHashMap<>();
        List<Double> monthlyBreakdown = new ArrayList<>(Collections.nCopies(12, 0.0));

        for (Income inc : incomes) {
            double amt = inc.getAmount() != null ? inc.getAmount() : 0.0;
            totalAmount += amt;

            double taxPct = inc.getTaxPercentage() != null ? inc.getTaxPercentage() : 0.0;
            double taxAmt = (taxPct > 0) ? (amt * (taxPct / 100.0)) : 0.0;
            totalTax += taxAmt;

            String catName = inc.getCategory() != null ? inc.getCategory().getName() : "Uncategorized";
            categoryMap.put(catName, categoryMap.getOrDefault(catName, 0.0) + amt);

            if (request.getType() == ReportType.YEARLY && inc.getDate() != null) {
                int fyIdx = FinancialYear.toFyMonthIndex(inc.getDate().getMonthValue());
                monthlyBreakdown.set(fyIdx, monthlyBreakdown.get(fyIdx) + amt);
            }

            ReportRow row = new ReportRow();
            row.setId(inc.getId());
            row.setDate(inc.getDate());
            row.setReference("INC-" + inc.getId());
            row.setCategory(catName);
            row.setSubCategory(inc.getSubCategory() != null ? inc.getSubCategory().getName() : "-");
            row.setDescription(inc.getDescription());
            row.setAmount(amt);
            row.setTaxPercentage(taxPct);
            row.setTaxAmount(taxAmt);
            row.setPaymentMode(inc.getPaymentMode() != null ? inc.getPaymentMode().name() : "-");
            row.setStatus(inc.getStatus() != null ? inc.getStatus().name() : "APPROVED");
            row.setSubmittedBy(inc.getUser() != null ? inc.getUser().getUsername() : "-");
            row.setApprovedBy("Approved");
            row.setHasAttachment((inc.getAttachments() != null && !inc.getAttachments().isEmpty()) ||
                    (inc.getReceiptImage() != null && !inc.getReceiptImage().isBlank()));
            row.setBatchId(inc.getBatchId() != null ? inc.getBatchId() : "-");

            section.getRows().add(row);
        }

        section.setRecordCount(incomes.size());
        section.setTotalAmount(totalAmount);
        section.setTotalTax(totalTax);
        section.setCategoryBreakdown(categoryMap);
        if (request.getType() == ReportType.YEARLY) {
            section.setMonthlyBreakdown(monthlyBreakdown);
        }

        return section;
    }

    private ScheduleSection compileCapitalSection(ReportRequest request) {
        ScheduleSection section = new ScheduleSection();
        section.setScheduleName("Capital & Equity");

        List<CapitalTransaction> list = capitalTransactionRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(
                IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());

        double totalIntroduced = 0.0;
        double totalDrawings = 0.0;
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Capital Introduced", 0.0);
        breakdown.put("Drawings", 0.0);
        List<Double> monthlyBreakdown = new ArrayList<>(Collections.nCopies(12, 0.0));

        for (CapitalTransaction c : list) {
            double amt = c.getAmount() != null ? c.getAmount() : 0.0;
            if (c.getType() == CapitalTransactionType.INTRODUCED) {
                totalIntroduced += amt;
                breakdown.put("Capital Introduced", breakdown.get("Capital Introduced") + amt);
            } else {
                totalDrawings += amt;
                breakdown.put("Drawings", breakdown.get("Drawings") + amt);
            }

            if (request.getType() == ReportType.YEARLY && c.getDate() != null) {
                int fyIdx = FinancialYear.toFyMonthIndex(c.getDate().getMonthValue());
                double netMovement = (c.getType() == CapitalTransactionType.INTRODUCED) ? amt : -amt;
                monthlyBreakdown.set(fyIdx, monthlyBreakdown.get(fyIdx) + netMovement);
            }

            ReportRow row = new ReportRow();
            row.setId(c.getId());
            row.setDate(c.getDate());
            row.setReference("CAP-" + c.getId());
            row.setTransactionType(c.getType() != null ? c.getType().getDisplayName() : "INTRODUCED");
            row.setCategory(row.getTransactionType());
            row.setOwnerName(c.getOwnerName() != null ? c.getOwnerName() : "-");
            row.setDescription(c.getDescription());
            row.setAmount(amt);
            row.setPaymentMode(c.getPaymentMode() != null ? c.getPaymentMode().name() : "-");
            row.setStatus(c.getStatus() != null ? c.getStatus().name() : "APPROVED");
            row.setSubmittedBy(c.getUser() != null ? c.getUser().getUsername() : "-");
            row.setApprovedBy("Approved");
            row.setHasAttachment((c.getAttachments() != null && !c.getAttachments().isEmpty()) ||
                    (c.getReceiptImage() != null && !c.getReceiptImage().isBlank()));
            row.setBatchId(c.getBatchId() != null ? c.getBatchId() : "-");

            section.getRows().add(row);
        }

        section.setRecordCount(list.size());
        section.setTotalAmount(totalIntroduced - totalDrawings);
        section.setCategoryBreakdown(breakdown);
        if (request.getType() == ReportType.YEARLY) {
            section.setMonthlyBreakdown(monthlyBreakdown);
        }

        return section;
    }

    private ScheduleSection compileLoansSection(ReportRequest request) {
        ScheduleSection section = new ScheduleSection();
        section.setScheduleName("Borrowings & Debt Obligations");

        List<LoanTransaction> list = loanTransactionRepository.findByStatusAndDateBetweenOrderByDateAscIdAsc(
                IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());

        double totalBorrowed = 0.0;
        double totalRepaid = 0.0;
        Map<String, Double> breakdown = new LinkedHashMap<>();
        breakdown.put("Loans Inward", 0.0);
        breakdown.put("Loan Repayment", 0.0);
        List<Double> monthlyBreakdown = new ArrayList<>(Collections.nCopies(12, 0.0));

        for (LoanTransaction lt : list) {
            double amt = lt.getAmount() != null ? lt.getAmount() : 0.0;
            if (lt.getType() == LoanTransactionType.BORROWED) {
                totalBorrowed += amt;
                breakdown.put("Loans Inward", breakdown.get("Loans Inward") + amt);
            } else {
                totalRepaid += amt;
                breakdown.put("Loan Repayment", breakdown.get("Loan Repayment") + amt);
            }

            if (request.getType() == ReportType.YEARLY && lt.getDate() != null) {
                int fyIdx = FinancialYear.toFyMonthIndex(lt.getDate().getMonthValue());
                double netMovement = (lt.getType() == LoanTransactionType.BORROWED) ? amt : -amt;
                monthlyBreakdown.set(fyIdx, monthlyBreakdown.get(fyIdx) + netMovement);
            }

            ReportRow row = new ReportRow();
            row.setId(lt.getId());
            row.setDate(lt.getDate());
            row.setReference("LOAN-TX-" + lt.getId());
            row.setTransactionType(lt.getType() != null ? lt.getType().getDisplayName() : "BORROWED");
            row.setCategory(row.getTransactionType());
            row.setLoanName(lt.getLoan() != null ? lt.getLoan().getLoanName() : "-");
            row.setLenderName(lt.getLoan() != null ? lt.getLoan().getLenderName() : "-");
            row.setDescription(lt.getDescription());
            row.setAmount(amt);
            row.setPaymentMode(lt.getPaymentMode() != null ? lt.getPaymentMode().name() : "-");
            row.setStatus(lt.getStatus() != null ? lt.getStatus().name() : "APPROVED");
            row.setSubmittedBy(lt.getUser() != null ? lt.getUser().getUsername() : "-");
            row.setApprovedBy("Approved");
            row.setHasAttachment((lt.getAttachments() != null && !lt.getAttachments().isEmpty()) ||
                    (lt.getReceiptImage() != null && !lt.getReceiptImage().isBlank()));
            row.setBatchId(lt.getBatchId() != null ? lt.getBatchId() : "-");

            section.getRows().add(row);
        }

        section.setRecordCount(list.size());
        section.setTotalAmount(totalBorrowed - totalRepaid);
        section.setCategoryBreakdown(breakdown);
        if (request.getType() == ReportType.YEARLY) {
            section.setMonthlyBreakdown(monthlyBreakdown);
        }

        // Facility level status table for period
        List<Loan> allLoans = loanRepository.findAll();
        for (Loan l : allLoans) {
            double borrowedBefore = loanTransactionRepository.sumAmountByLoanIdAndTypeAndStatusAndDateBefore(
                    l.getId(), LoanTransactionType.BORROWED, IncomeStatus.APPROVED, request.getStartDate());
            double repaidBefore = loanTransactionRepository.sumAmountByLoanIdAndTypeAndStatusAndDateBefore(
                    l.getId(), LoanTransactionType.REPAYMENT, IncomeStatus.APPROVED, request.getStartDate());
            double openingBal = borrowedBefore - repaidBefore;

            double borrowedPeriod = loanTransactionRepository.sumAmountByLoanIdAndTypeAndStatusAndDateBetween(
                    l.getId(), LoanTransactionType.BORROWED, IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());
            double repaidPeriod = loanTransactionRepository.sumAmountByLoanIdAndTypeAndStatusAndDateBetween(
                    l.getId(), LoanTransactionType.REPAYMENT, IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());
            double closingBal = openingBal + borrowedPeriod - repaidPeriod;

            LoanFacilityReportItem item = new LoanFacilityReportItem();
            item.setLoanName(l.getLoanName());
            item.setLenderName(l.getLenderName());
            item.setOpeningOutstanding(openingBal);
            item.setBorrowedInPeriod(borrowedPeriod);
            item.setRepaidInPeriod(repaidPeriod);
            item.setClosingOutstanding(closingBal);
            item.setStatus(l.getStatus() != null ? l.getStatus().name() : "ACTIVE");

            section.getLoanFacilities().add(item);
        }

        return section;
    }

    private PeriodSummary compilePeriodSummary(
            ReportRequest request,
            ScheduleSection expenses,
            ScheduleSection income,
            ScheduleSection capital,
            ScheduleSection loans) {
        PeriodSummary s = new PeriodSummary();
        s.setBusinessName("Mushroom Farm");
        s.setReportType(request.getType());
        s.setPeriodLabel(request.getPeriodLabel());
        s.setStartDate(request.getStartDate());
        s.setEndDate(request.getEndDate());
        s.setGeneratedAt(LocalDateTime.now());
        s.setGeneratedBy(request.getRequestedBy() != null ? request.getRequestedBy() : "System");

        double totalExp = expenses.getTotalAmount();
        double totalInc = income.getTotalAmount();
        double netBal = totalInc - totalExp;

        double capIntro = capital.getCategoryBreakdown().getOrDefault("Capital Introduced", 0.0);
        double capDraw = capital.getCategoryBreakdown().getOrDefault("Drawings", 0.0);
        double netCap = capIntro - capDraw;

        double loanBorrowed = loans.getCategoryBreakdown().getOrDefault("Loans Inward", 0.0);
        double loanRepaid = loans.getCategoryBreakdown().getOrDefault("Loan Repayment", 0.0);
        double netLoan = loanBorrowed - loanRepaid;

        double netCashFlow = netBal + netCap + netLoan;

        s.setTotalExpense(totalExp);
        s.setTotalIncome(totalInc);
        s.setNetBalance(netBal);
        s.setCapitalIntroduced(capIntro);
        s.setCapitalDrawings(capDraw);
        s.setNetCapitalFlow(netCap);
        s.setLoansBorrowed(loanBorrowed);
        s.setLoansRepaid(loanRepaid);
        s.setNetLoanFlow(netLoan);
        s.setNetCashFlow(netCashFlow);

        // Opening & closing balance sheet support
        double capIntroBefore = capitalTransactionRepository.sumAmountByTypeAndStatusAndDateBefore(
                CapitalTransactionType.INTRODUCED, IncomeStatus.APPROVED, request.getStartDate());
        double capDrawBefore = capitalTransactionRepository.sumAmountByTypeAndStatusAndDateBefore(
                CapitalTransactionType.DRAWINGS, IncomeStatus.APPROVED, request.getStartDate());
        double openCap = capIntroBefore - capDrawBefore;
        s.setOpeningCapital(openCap);
        s.setClosingCapital(openCap + netCap);

        double loanBorrowBefore = loanTransactionRepository.sumAmountByTypeAndStatusAndDateBefore(
                LoanTransactionType.BORROWED, IncomeStatus.APPROVED, request.getStartDate());
        double loanRepaidBefore = loanTransactionRepository.sumAmountByTypeAndStatusAndDateBefore(
                LoanTransactionType.REPAYMENT, IncomeStatus.APPROVED, request.getStartDate());
        double openLoans = loanBorrowBefore - loanRepaidBefore;
        s.setOpeningLoansOutstanding(openLoans);
        s.setClosingLoansOutstanding(openLoans + netLoan);

        // Financial Schedules Reconciliation items
        s.getScheduleSummaries().add(new ScheduleSummaryItem("Operating Expenses Schedule", expenses.getRecordCount(), totalExp));
        s.getScheduleSummaries().add(new ScheduleSummaryItem("Revenue & Operating Income Schedule", income.getRecordCount(), totalInc));
        s.getScheduleSummaries().add(new ScheduleSummaryItem("Owner's Equity & Capital (Net Flow)", capital.getRecordCount(), netCap));
        s.getScheduleSummaries().add(new ScheduleSummaryItem("Borrowings & Debt Obligations (Net Flow)", loans.getRecordCount(), netLoan));

        // Excluded count
        long excluded = expenseRepository.countByStatusNotAndDateBetween(ExpenseStatus.APPROVED, request.getStartDate(), request.getEndDate())
                + incomeRepository.countByStatusNotAndDateBetween(IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate())
                + capitalTransactionRepository.countByStatusNotAndDateBetween(IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate())
                + loanTransactionRepository.countByStatusNotAndDateBetween(IncomeStatus.APPROVED, request.getStartDate(), request.getEndDate());
        s.setExcludedRecordCount(excluded);

        return s;
    }
}
