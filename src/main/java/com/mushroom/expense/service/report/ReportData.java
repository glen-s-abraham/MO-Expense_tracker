package com.mushroom.expense.service.report;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory DTO models representing compiled report data for Excel and CSV export.
 */
public final class ReportData {

    private ReportData() {
    }

    public enum ReportType {
        MONTHLY,
        YEARLY
    }

    public enum ReportFormat {
        XLSX,
        CSV
    }

    public static class ReportRequest {
        private ReportType type;
        private String fy;
        private Integer year;
        private Integer month;
        private ReportFormat format;
        private String requestedBy;
        private LocalDate startDate;
        private LocalDate endDate;
        private String periodLabel;

        public ReportType getType() { return type; }
        public void setType(ReportType type) { this.type = type; }
        public String getFy() { return fy; }
        public void setFy(String fy) { this.fy = fy; }
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        public ReportFormat getFormat() { return format; }
        public void setFormat(ReportFormat format) { this.format = format; }
        public String getRequestedBy() { return requestedBy; }
        public void setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public String getPeriodLabel() { return periodLabel; }
        public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
    }

    public static class CategorySummary {
        private String name;
        private double amount;

        public CategorySummary() {}
        public CategorySummary(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }

    public static class ScheduleSummaryItem {
        private String scheduleName;
        private long recordCount;
        private double totalAmount;

        public ScheduleSummaryItem() {}
        public ScheduleSummaryItem(String scheduleName, long recordCount, double totalAmount) {
            this.scheduleName = scheduleName;
            this.recordCount = recordCount;
            this.totalAmount = totalAmount;
        }

        public String getScheduleName() { return scheduleName; }
        public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
        public String getPillarName() { return scheduleName; }
        public void setPillarName(String pillarName) { this.scheduleName = pillarName; }
        public long getRecordCount() { return recordCount; }
        public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    }

    public static class PillarSummaryItem extends ScheduleSummaryItem {
        public PillarSummaryItem() { super(); }
        public PillarSummaryItem(String name, long count, double amount) { super(name, count, amount); }
    }

    public static class PeriodSummary {
        private String businessName = "Mushroom Farm";
        private ReportType reportType;
        private String periodLabel;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime generatedAt;
        private String generatedBy;

        // Dashboard Metrics (Period)
        private double totalExpense;
        private double totalIncome;
        private double netBalance; // Income - Expense
        private double capitalIntroduced;
        private double capitalDrawings;
        private double netCapitalFlow;
        private double loansBorrowed;
        private double loansRepaid;
        private double netLoanFlow;
        private double netCashFlow;

        // Balance sheet support
        private double openingCapital;
        private double closingCapital;
        private double openingLoansOutstanding;
        private double closingLoansOutstanding;

        private List<ScheduleSummaryItem> scheduleSummaries = new ArrayList<>();
        private long excludedRecordCount;

        // Getters & Setters
        public String getBusinessName() { return businessName; }
        public void setBusinessName(String businessName) { this.businessName = businessName; }
        public ReportType getReportType() { return reportType; }
        public void setReportType(ReportType reportType) { this.reportType = reportType; }
        public String getPeriodLabel() { return periodLabel; }
        public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

        public double getTotalExpense() { return totalExpense; }
        public void setTotalExpense(double totalExpense) { this.totalExpense = totalExpense; }
        public double getTotalIncome() { return totalIncome; }
        public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }
        public double getNetBalance() { return netBalance; }
        public void setNetBalance(double netBalance) { this.netBalance = netBalance; }

        public double getCapitalIntroduced() { return capitalIntroduced; }
        public void setCapitalIntroduced(double capitalIntroduced) { this.capitalIntroduced = capitalIntroduced; }
        public double getCapitalDrawings() { return capitalDrawings; }
        public void setCapitalDrawings(double capitalDrawings) { this.capitalDrawings = capitalDrawings; }
        public double getNetCapitalFlow() { return netCapitalFlow; }
        public void setNetCapitalFlow(double netCapitalFlow) { this.netCapitalFlow = netCapitalFlow; }

        public double getLoansBorrowed() { return loansBorrowed; }
        public void setLoansBorrowed(double loansBorrowed) { this.loansBorrowed = loansBorrowed; }
        public double getLoansRepaid() { return loansRepaid; }
        public void setLoansRepaid(double loansRepaid) { this.loansRepaid = loansRepaid; }
        public double getNetLoanFlow() { return netLoanFlow; }
        public void setNetLoanFlow(double netLoanFlow) { this.netLoanFlow = netLoanFlow; }

        public double getNetCashFlow() { return netCashFlow; }
        public void setNetCashFlow(double netCashFlow) { this.netCashFlow = netCashFlow; }

        public double getOpeningCapital() { return openingCapital; }
        public void setOpeningCapital(double openingCapital) { this.openingCapital = openingCapital; }
        public double getClosingCapital() { return closingCapital; }
        public void setClosingCapital(double closingCapital) { this.closingCapital = closingCapital; }

        public double getOpeningLoansOutstanding() { return openingLoansOutstanding; }
        public void setOpeningLoansOutstanding(double openingLoansOutstanding) { this.openingLoansOutstanding = openingLoansOutstanding; }
        public double getClosingLoansOutstanding() { return closingLoansOutstanding; }
        public void setClosingLoansOutstanding(double closingLoansOutstanding) { this.closingLoansOutstanding = closingLoansOutstanding; }

        public List<ScheduleSummaryItem> getScheduleSummaries() { return scheduleSummaries; }
        public void setScheduleSummaries(List<ScheduleSummaryItem> scheduleSummaries) { this.scheduleSummaries = scheduleSummaries; }

        public List<PillarSummaryItem> getPillarSummaries() {
            List<PillarSummaryItem> list = new ArrayList<>();
            for (ScheduleSummaryItem item : scheduleSummaries) {
                if (item instanceof PillarSummaryItem) {
                    list.add((PillarSummaryItem) item);
                } else {
                    list.add(new PillarSummaryItem(item.getScheduleName(), item.getRecordCount(), item.getTotalAmount()));
                }
            }
            return list;
        }
        public void setPillarSummaries(List<PillarSummaryItem> pillarSummaries) {
            this.scheduleSummaries = new ArrayList<>(pillarSummaries);
        }
        public long getExcludedRecordCount() { return excludedRecordCount; }
        public void setExcludedRecordCount(long excludedRecordCount) { this.excludedRecordCount = excludedRecordCount; }
    }

    public static class LoanFacilityReportItem {
        private String loanName;
        private String lenderName;
        private double openingOutstanding;
        private double borrowedInPeriod;
        private double repaidInPeriod;
        private double closingOutstanding;
        private String status;

        public String getLoanName() { return loanName; }
        public void setLoanName(String loanName) { this.loanName = loanName; }
        public String getLenderName() { return lenderName; }
        public void setLenderName(String lenderName) { this.lenderName = lenderName; }
        public double getOpeningOutstanding() { return openingOutstanding; }
        public void setOpeningOutstanding(double openingOutstanding) { this.openingOutstanding = openingOutstanding; }
        public double getBorrowedInPeriod() { return borrowedInPeriod; }
        public void setBorrowedInPeriod(double borrowedInPeriod) { this.borrowedInPeriod = borrowedInPeriod; }
        public double getRepaidInPeriod() { return repaidInPeriod; }
        public void setRepaidInPeriod(double repaidInPeriod) { this.repaidInPeriod = repaidInPeriod; }
        public double getClosingOutstanding() { return closingOutstanding; }
        public void setClosingOutstanding(double closingOutstanding) { this.closingOutstanding = closingOutstanding; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class LifetimeFinancials {
        private LocalDateTime asOfDate;
        private String generatedBy;

        private double globalTotalExpense;
        private double globalTotalIncome;
        private double globalNetBalance;

        private double globalCapitalIntroduced;
        private double globalCapitalDrawings;
        private double globalNetCapital;

        private double globalLoansBorrowed;
        private double globalLoansRepaid;
        private double globalOutstandingLoans;

        private double globalNetCashPosition;

        private List<CategorySummary> expensesByCategory = new ArrayList<>();
        private List<CategorySummary> incomesByCategory = new ArrayList<>();
        private List<LoanFacilityReportItem> loanFacilities = new ArrayList<>();

        public LocalDateTime getAsOfDate() { return asOfDate; }
        public void setAsOfDate(LocalDateTime asOfDate) { this.asOfDate = asOfDate; }
        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

        public double getGlobalTotalExpense() { return globalTotalExpense; }
        public void setGlobalTotalExpense(double globalTotalExpense) { this.globalTotalExpense = globalTotalExpense; }
        public double getGlobalTotalIncome() { return globalTotalIncome; }
        public void setGlobalTotalIncome(double globalTotalIncome) { this.globalTotalIncome = globalTotalIncome; }
        public double getGlobalNetBalance() { return globalNetBalance; }
        public void setGlobalNetBalance(double globalNetBalance) { this.globalNetBalance = globalNetBalance; }

        public double getGlobalCapitalIntroduced() { return globalCapitalIntroduced; }
        public void setGlobalCapitalIntroduced(double globalCapitalIntroduced) { this.globalCapitalIntroduced = globalCapitalIntroduced; }
        public double getGlobalCapitalDrawings() { return globalCapitalDrawings; }
        public void setGlobalCapitalDrawings(double globalCapitalDrawings) { this.globalCapitalDrawings = globalCapitalDrawings; }
        public double getGlobalNetCapital() { return globalNetCapital; }
        public void setGlobalNetCapital(double globalNetCapital) { this.globalNetCapital = globalNetCapital; }

        public double getGlobalLoansBorrowed() { return globalLoansBorrowed; }
        public void setGlobalLoansBorrowed(double globalLoansBorrowed) { this.globalLoansBorrowed = globalLoansBorrowed; }
        public double getGlobalLoansRepaid() { return globalLoansRepaid; }
        public void setGlobalLoansRepaid(double globalLoansRepaid) { this.globalLoansRepaid = globalLoansRepaid; }
        public double getGlobalOutstandingLoans() { return globalOutstandingLoans; }
        public void setGlobalOutstandingLoans(double globalOutstandingLoans) { this.globalOutstandingLoans = globalOutstandingLoans; }

        public double getGlobalNetCashPosition() { return globalNetCashPosition; }
        public void setGlobalNetCashPosition(double globalNetCashPosition) { this.globalNetCashPosition = globalNetCashPosition; }

        public List<CategorySummary> getExpensesByCategory() { return expensesByCategory; }
        public void setExpensesByCategory(List<CategorySummary> expensesByCategory) { this.expensesByCategory = expensesByCategory; }
        public List<CategorySummary> getIncomesByCategory() { return incomesByCategory; }
        public void setIncomesByCategory(List<CategorySummary> incomesByCategory) { this.incomesByCategory = incomesByCategory; }
        public List<LoanFacilityReportItem> getLoanFacilities() { return loanFacilities; }
        public void setLoanFacilities(List<LoanFacilityReportItem> loanFacilities) { this.loanFacilities = loanFacilities; }
    }

    public static class ReportRow {
        private Long id;
        private LocalDate date;
        private String reference;
        private String category;
        private String subCategory;
        private String transactionType; // e.g. INTRODUCED, DRAWINGS, BORROWED, REPAYMENT
        private String description;
        private double amount;
        private double taxPercentage;
        private double taxAmount;
        private String paymentMode;
        private String status;
        private String submittedBy;
        private String approvedBy;
        private boolean hasAttachment;
        private String batchId;
        private String ownerName;
        private String loanName;
        private String lenderName;

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getSubCategory() { return subCategory; }
        public void setSubCategory(String subCategory) { this.subCategory = subCategory; }
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
        public double getTaxPercentage() { return taxPercentage; }
        public void setTaxPercentage(double taxPercentage) { this.taxPercentage = taxPercentage; }
        public double getTaxAmount() { return taxAmount; }
        public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
        public String getPaymentMode() { return paymentMode; }
        public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSubmittedBy() { return submittedBy; }
        public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
        public String getApprovedBy() { return approvedBy; }
        public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
        public boolean isHasAttachment() { return hasAttachment; }
        public void setHasAttachment(boolean hasAttachment) { this.hasAttachment = hasAttachment; }
        public String getBatchId() { return batchId; }
        public void setBatchId(String batchId) { this.batchId = batchId; }
        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
        public String getLoanName() { return loanName; }
        public void setLoanName(String loanName) { this.loanName = loanName; }
        public String getLenderName() { return lenderName; }
        public void setLenderName(String lenderName) { this.lenderName = lenderName; }
    }

    public static class ScheduleSection {
        private String scheduleName;
        private long recordCount;
        private double totalAmount;
        private double totalTax;
        private Map<String, Double> categoryBreakdown = new LinkedHashMap<>();
        private List<Double> monthlyBreakdown = new ArrayList<>(); // 12 elements (Apr-Mar) for yearly
        private List<ReportRow> rows = new ArrayList<>();
        private List<LoanFacilityReportItem> loanFacilities = new ArrayList<>();

        public String getScheduleName() { return scheduleName; }
        public void setScheduleName(String scheduleName) { this.scheduleName = scheduleName; }
        public String getPillarName() { return scheduleName; }
        public void setPillarName(String pillarName) { this.scheduleName = pillarName; }
        public long getRecordCount() { return recordCount; }
        public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
        public double getTotalAmount() { return totalAmount; }
        public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
        public double getTotalTax() { return totalTax; }
        public void setTotalTax(double totalTax) { this.totalTax = totalTax; }
        public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
        public void setCategoryBreakdown(Map<String, Double> categoryBreakdown) { this.categoryBreakdown = categoryBreakdown; }
        public List<Double> getMonthlyBreakdown() { return monthlyBreakdown; }
        public void setMonthlyBreakdown(List<Double> monthlyBreakdown) { this.monthlyBreakdown = monthlyBreakdown; }
        public List<ReportRow> getRows() { return rows; }
        public void setRows(List<ReportRow> rows) { this.rows = rows; }
        public List<LoanFacilityReportItem> getLoanFacilities() { return loanFacilities; }
        public void setLoanFacilities(List<LoanFacilityReportItem> loanFacilities) { this.loanFacilities = loanFacilities; }
    }

    public static class PillarSection extends ScheduleSection {}

    public static class ReportDataContainer {
        private ReportRequest request;
        private PeriodSummary summary;
        private LifetimeFinancials lifetimeFinancials;
        private ScheduleSection expensesSection;
        private ScheduleSection incomeSection;
        private ScheduleSection capitalSection;
        private ScheduleSection loansSection;

        public ReportRequest getRequest() { return request; }
        public void setRequest(ReportRequest request) { this.request = request; }
        public PeriodSummary getSummary() { return summary; }
        public void setSummary(PeriodSummary summary) { this.summary = summary; }
        public LifetimeFinancials getLifetimeFinancials() { return lifetimeFinancials; }
        public void setLifetimeFinancials(LifetimeFinancials lifetimeFinancials) { this.lifetimeFinancials = lifetimeFinancials; }
        public ScheduleSection getExpensesSection() { return expensesSection; }
        public void setExpensesSection(ScheduleSection expensesSection) { this.expensesSection = expensesSection; }
        public ScheduleSection getIncomeSection() { return incomeSection; }
        public void setIncomeSection(ScheduleSection incomeSection) { this.incomeSection = incomeSection; }
        public ScheduleSection getCapitalSection() { return capitalSection; }
        public void setCapitalSection(ScheduleSection capitalSection) { this.capitalSection = capitalSection; }
        public ScheduleSection getLoansSection() { return loansSection; }
        public void setLoansSection(ScheduleSection loansSection) { this.loansSection = loansSection; }
    }
}
