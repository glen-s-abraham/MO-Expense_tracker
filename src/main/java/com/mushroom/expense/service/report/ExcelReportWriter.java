package com.mushroom.expense.service.report;

import com.mushroom.expense.service.report.ReportData.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Modern, beautifully-styled Excel Report Generator producing an audit-ready multi-tab workbook:
 * Tab 1: Lifetime Financials (Cumulative Balance Sheet foundation)
 * Tab 2: Expenses (Schedule of Expenses + Itemized Ledger)
 * Tab 3: Income (Schedule of Revenue & Operating Income + Itemized Ledger)
 * Tab 4: Capital (Schedule of Capital & Equity + Itemized Ledger)
 * Tab 5: Loans (Schedule of Borrowings & Debt + Facility Status + Itemized Ledger)
 */
public class ExcelReportWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    // Brand Colors
    private static final byte[] COLOR_NAVY = new byte[]{(byte) 30, (byte) 41, (byte) 59};      // #1E293B
    private static final byte[] COLOR_INDIGO = new byte[]{(byte) 79, (byte) 70, (byte) 229};   // #4F46E5
    private static final byte[] COLOR_CORAL = new byte[]{(byte) 220, (byte) 38, (byte) 38};    // #DC2626
    private static final byte[] COLOR_EMERALD = new byte[]{(byte) 5, (byte) 150, (byte) 105};  // #059669
    private static final byte[] COLOR_BLUE = new byte[]{(byte) 37, (byte) 99, (byte) 235};     // #2563EB
    private static final byte[] COLOR_AMBER = new byte[]{(byte) 217, (byte) 119, (byte) 6};    // #D97706
    private static final byte[] COLOR_ZEBRA = new byte[]{(byte) 248, (byte) 250, (byte) 252};  // #F8FAFC
    private static final byte[] COLOR_CARD = new byte[]{(byte) 241, (byte) 245, (byte) 249};   // #F1F5F9
    private static final byte[] COLOR_BORDER = new byte[]{(byte) 226, (byte) 232, (byte) 240};// #E2E8F0

    public byte[] writeWorkbook(ReportDataContainer data) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            StylesHolder styles = new StylesHolder(workbook);

            // 1. Lifetime Financials (Sheet 1 - Front Page / Cumulative Position)
            createLifetimeFinancialsSheet(workbook, styles, data);

            // 2. Operating Expenses Schedule (Sheet 2)
            createExpensesSheet(workbook, styles, data);

            // 3. Revenue & Operating Income Schedule (Sheet 3)
            createIncomeSheet(workbook, styles, data);

            // 4. Capital & Equity Schedule (Sheet 4)
            createCapitalSheet(workbook, styles, data);

            // 5. Borrowings & Debt Schedule (Sheet 5)
            createLoansSheet(workbook, styles, data);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // SHEET 2: LIFETIME FINANCIALS
    // ---------------------------------------------------------------------------------------------
    private void createLifetimeFinancialsSheet(XSSFWorkbook wb, StylesHolder styles, ReportDataContainer data) {
        XSSFSheet sheet = wb.createSheet("Lifetime Financials");
        sheet.setTabColor(new XSSFColor(COLOR_NAVY, null));
        setupPrint(sheet, "Lifetime Financials", "All-Time Cumulative Financials");

        LifetimeFinancials lf = data.getLifetimeFinancials();
        int rowIdx = 0;

        rowIdx = createTitleBanner(sheet, styles, rowIdx, "LIFETIME FINANCIAL OVERVIEW & BALANCE SHEET FOUNDATION",
                "Cumulative position as of " + lf.getAsOfDate().format(TS_FMT) + " | Prepared for Taxation & Audit",
                COLOR_NAVY, 6);

        rowIdx++;

        // Section 1: All-time P&L and Financing
        createSectionHeader(sheet, styles, rowIdx++, "1. ALL-TIME OPERATIONAL AND FINANCING METRICS", COLOR_INDIGO, 6);

        createMetricRow(sheet, styles, rowIdx++, "Lifetime Total Income", lf.getGlobalTotalIncome(), "Lifetime Total Expenses", lf.getGlobalTotalExpense());
        createMetricRow(sheet, styles, rowIdx++, "Lifetime Net Operating Balance", lf.getGlobalNetBalance(), "Lifetime Capital Introduced", lf.getGlobalCapitalIntroduced());
        createMetricRow(sheet, styles, rowIdx++, "Lifetime Capital Drawings", lf.getGlobalCapitalDrawings(), "Net Capital Pool (Equity)", lf.getGlobalNetCapital());
        createMetricRow(sheet, styles, rowIdx++, "Lifetime Loans Borrowed", lf.getGlobalLoansBorrowed(), "Lifetime Loan Repayments", lf.getGlobalLoansRepaid());
        createMetricRow(sheet, styles, rowIdx++, "Current Outstanding Loan Pool", lf.getGlobalOutstandingLoans(), "Overall Net Cash Position", lf.getGlobalNetCashPosition());

        rowIdx++;

        // Section 2: Lifetime Category Breakdowns (Income & Expense)
        createSectionHeader(sheet, styles, rowIdx++, "2. LIFETIME CATEGORY DISTRIBUTIONS", COLOR_INDIGO, 6);

        Row catHead = sheet.createRow(rowIdx++);
        createHeaderCell(catHead, 0, "Expense Category", styles.headerStyle(COLOR_CORAL));
        createHeaderCell(catHead, 1, "Cumulative Amount", styles.headerStyle(COLOR_CORAL));
        createHeaderCell(catHead, 2, "", styles.headerStyle(COLOR_NAVY));
        createHeaderCell(catHead, 3, "Income Category", styles.headerStyle(COLOR_EMERALD));
        createHeaderCell(catHead, 4, "Cumulative Amount", styles.headerStyle(COLOR_EMERALD));
        createHeaderCell(catHead, 5, "", styles.headerStyle(COLOR_NAVY));

        int expSize = lf.getExpensesByCategory().size();
        int incSize = lf.getIncomesByCategory().size();
        int maxRows = Math.max(expSize, incSize);

        for (int i = 0; i < maxRows; i++) {
            Row r = sheet.createRow(rowIdx++);
            CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
            CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;

            if (i < expSize) {
                CategorySummary csExp = lf.getExpensesByCategory().get(i);
                Cell c0 = r.createCell(0); c0.setCellValue(csExp.getName()); c0.setCellStyle(cs);
                Cell c1 = r.createCell(1); c1.setCellValue(csExp.getAmount()); c1.setCellStyle(csCur);
            } else {
                r.createCell(0).setCellStyle(cs);
                r.createCell(1).setCellStyle(cs);
            }

            r.createCell(2).setCellStyle(styles.normalText); // separator

            if (i < incSize) {
                CategorySummary csInc = lf.getIncomesByCategory().get(i);
                Cell c3 = r.createCell(3); c3.setCellValue(csInc.getName()); c3.setCellStyle(cs);
                Cell c4 = r.createCell(4); c4.setCellValue(csInc.getAmount()); c4.setCellStyle(csCur);
            } else {
                r.createCell(3).setCellStyle(cs);
                r.createCell(4).setCellStyle(cs);
            }
            r.createCell(5).setCellStyle(styles.normalText);
        }

        rowIdx++;

        // Section 3: Facility-by-Facility Loan Status
        createSectionHeader(sheet, styles, rowIdx++, "3. ALL LOAN FACILITIES STATUS & CUMULATIVE BALANCES", COLOR_INDIGO, 6);

        Row loanHead = sheet.createRow(rowIdx++);
        createHeaderCell(loanHead, 0, "Loan Facility Name", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(loanHead, 1, "Lender Name", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(loanHead, 2, "Cumulative Borrowed", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(loanHead, 3, "Cumulative Repaid", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(loanHead, 4, "Current Outstanding", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(loanHead, 5, "Facility Status", styles.headerStyle(COLOR_AMBER));

        for (int i = 0; i < lf.getLoanFacilities().size(); i++) {
            LoanFacilityReportItem item = lf.getLoanFacilities().get(i);
            Row r = sheet.createRow(rowIdx++);
            CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
            CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;

            Cell c0 = r.createCell(0); c0.setCellValue(item.getLoanName()); c0.setCellStyle(cs);
            Cell c1 = r.createCell(1); c1.setCellValue(item.getLenderName()); c1.setCellStyle(cs);
            Cell c2 = r.createCell(2); c2.setCellValue(item.getBorrowedInPeriod()); c2.setCellStyle(csCur);
            Cell c3 = r.createCell(3); c3.setCellValue(item.getRepaidInPeriod()); c3.setCellStyle(csCur);
            Cell c4 = r.createCell(4); c4.setCellValue(item.getClosingOutstanding()); c4.setCellStyle(csCur);
            Cell c5 = r.createCell(5); c5.setCellValue(item.getStatus()); c5.setCellStyle(cs);
        }

        autoSizeColumns(sheet, 6);
    }

    // ---------------------------------------------------------------------------------------------
    // SHEET 3: EXPENSES SCHEDULE
    // ---------------------------------------------------------------------------------------------
    private void createExpensesSheet(XSSFWorkbook wb, StylesHolder styles, ReportDataContainer data) {
        XSSFSheet sheet = wb.createSheet("Expenses");
        sheet.setTabColor(new XSSFColor(COLOR_CORAL, null));
        setupPrint(sheet, "Expenses", data.getRequest().getPeriodLabel());

        ScheduleSection sec = data.getExpensesSection();
        int rowIdx = 0;

        // Title Banner
        rowIdx = createTitleBanner(sheet, styles, rowIdx, "EXPENSES LEDGER - " + data.getRequest().getPeriodLabel(),
                "Timeframe: " + data.getRequest().getStartDate().format(DATE_FMT) + " to " + data.getRequest().getEndDate().format(DATE_FMT),
                COLOR_CORAL, 12);

        rowIdx++;

        // 1. Totals Block on Top
        rowIdx = createScheduleTotalsBlock(sheet, styles, rowIdx, "OPERATING EXPENSES SCHEDULE TOTALS", "Total Expenditure", sec, COLOR_CORAL, 12);

        // Month-wise breakdown for yearly
        if (data.getRequest().getType() == ReportType.YEARLY) {
            rowIdx = createMonthlyBreakdownBlock(sheet, styles, rowIdx, sec.getMonthlyBreakdown(), COLOR_CORAL);
        }

        rowIdx++;

        // 2. Detail Rows Below
        createSectionHeader(sheet, styles, rowIdx++, "ITEMIZED EXPENSE TRANSACTIONS", COLOR_NAVY, 12);

        int tableHeaderRow = rowIdx++;
        Row th = sheet.createRow(tableHeaderRow);
        String[] headers = {"Date", "Reference", "Category", "Sub-Category", "Description", "Amount", "Tax %", "Tax Amount", "Payment Mode", "Status", "Submitted By", "Attachment"};
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(th, i, headers[i], styles.headerStyle(COLOR_CORAL));
        }

        if (sec.getRows().isEmpty()) {
            Row emptyRow = sheet.createRow(rowIdx++);
            Cell c = emptyRow.createCell(0);
            c.setCellValue("No approved expenses recorded in this period.");
            c.setCellStyle(styles.normalText);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 11));
        } else {
            for (int i = 0; i < sec.getRows().size(); i++) {
                ReportRow rData = sec.getRows().get(i);
                Row r = sheet.createRow(rowIdx++);
                CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
                CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;
                CellStyle csDate = (i % 2 == 1) ? styles.zebraDate : styles.normalDate;

                Cell c0 = r.createCell(0); c0.setCellValue(rData.getDate() != null ? rData.getDate().format(DATE_FMT) : "-"); c0.setCellStyle(csDate);
                Cell c1 = r.createCell(1); c1.setCellValue(rData.getReference()); c1.setCellStyle(cs);
                Cell c2 = r.createCell(2); c2.setCellValue(CellSanitizer.sanitize(rData.getCategory())); c2.setCellStyle(cs);
                Cell c3 = r.createCell(3); c3.setCellValue(CellSanitizer.sanitize(rData.getSubCategory())); c3.setCellStyle(cs);
                Cell c4 = r.createCell(4); c4.setCellValue(CellSanitizer.sanitize(rData.getDescription())); c4.setCellStyle(cs);
                Cell c5 = r.createCell(5); c5.setCellValue(rData.getAmount()); c5.setCellStyle(csCur);
                Cell c6 = r.createCell(6); c6.setCellValue(rData.getTaxPercentage()); c6.setCellStyle(cs);
                Cell c7 = r.createCell(7); c7.setCellValue(rData.getTaxAmount()); c7.setCellStyle(csCur);
                Cell c8 = r.createCell(8); c8.setCellValue(rData.getPaymentMode()); c8.setCellStyle(cs);
                Cell c9 = r.createCell(9); c9.setCellValue(rData.getStatus()); c9.setCellStyle(cs);
                Cell c10 = r.createCell(10); c10.setCellValue(rData.getSubmittedBy()); c10.setCellStyle(cs);
                Cell c11 = r.createCell(11); c11.setCellValue(rData.isHasAttachment() ? "Yes" : "No"); c11.setCellStyle(cs);
            }

            sheet.setAutoFilter(new CellRangeAddress(tableHeaderRow, rowIdx - 1, 0, headers.length - 1));
        }

        sheet.createFreezePane(0, tableHeaderRow + 1);
        autoSizeColumns(sheet, 12);
    }

    // ---------------------------------------------------------------------------------------------
    // SHEET 4: REVENUE & INCOME SCHEDULE
    // ---------------------------------------------------------------------------------------------
    private void createIncomeSheet(XSSFWorkbook wb, StylesHolder styles, ReportDataContainer data) {
        XSSFSheet sheet = wb.createSheet("Income");
        sheet.setTabColor(new XSSFColor(COLOR_EMERALD, null));
        setupPrint(sheet, "Income", data.getRequest().getPeriodLabel());

        ScheduleSection sec = data.getIncomeSection();
        int rowIdx = 0;

        rowIdx = createTitleBanner(sheet, styles, rowIdx, "INCOME & REVENUE LEDGER - " + data.getRequest().getPeriodLabel(),
                "Timeframe: " + data.getRequest().getStartDate().format(DATE_FMT) + " to " + data.getRequest().getEndDate().format(DATE_FMT),
                COLOR_EMERALD, 12);

        rowIdx++;

        rowIdx = createScheduleTotalsBlock(sheet, styles, rowIdx, "REVENUE & OPERATING INCOME TOTALS", "Total Revenue", sec, COLOR_EMERALD, 12);

        if (data.getRequest().getType() == ReportType.YEARLY) {
            rowIdx = createMonthlyBreakdownBlock(sheet, styles, rowIdx, sec.getMonthlyBreakdown(), COLOR_EMERALD);
        }

        rowIdx++;

        createSectionHeader(sheet, styles, rowIdx++, "ITEMIZED INCOME TRANSACTIONS", COLOR_NAVY, 12);

        int tableHeaderRow = rowIdx++;
        Row th = sheet.createRow(tableHeaderRow);
        String[] headers = {"Date", "Reference", "Category", "Sub-Category", "Description", "Amount", "Tax %", "Tax Amount", "Payment Mode", "Status", "Submitted By", "Attachment"};
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(th, i, headers[i], styles.headerStyle(COLOR_EMERALD));
        }

        if (sec.getRows().isEmpty()) {
            Row emptyRow = sheet.createRow(rowIdx++);
            Cell c = emptyRow.createCell(0);
            c.setCellValue("No approved income recorded in this period.");
            c.setCellStyle(styles.normalText);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 11));
        } else {
            for (int i = 0; i < sec.getRows().size(); i++) {
                ReportRow rData = sec.getRows().get(i);
                Row r = sheet.createRow(rowIdx++);
                CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
                CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;
                CellStyle csDate = (i % 2 == 1) ? styles.zebraDate : styles.normalDate;

                Cell c0 = r.createCell(0); c0.setCellValue(rData.getDate() != null ? rData.getDate().format(DATE_FMT) : "-"); c0.setCellStyle(csDate);
                Cell c1 = r.createCell(1); c1.setCellValue(rData.getReference()); c1.setCellStyle(cs);
                Cell c2 = r.createCell(2); c2.setCellValue(CellSanitizer.sanitize(rData.getCategory())); c2.setCellStyle(cs);
                Cell c3 = r.createCell(3); c3.setCellValue(CellSanitizer.sanitize(rData.getSubCategory())); c3.setCellStyle(cs);
                Cell c4 = r.createCell(4); c4.setCellValue(CellSanitizer.sanitize(rData.getDescription())); c4.setCellStyle(cs);
                Cell c5 = r.createCell(5); c5.setCellValue(rData.getAmount()); c5.setCellStyle(csCur);
                Cell c6 = r.createCell(6); c6.setCellValue(rData.getTaxPercentage()); c6.setCellStyle(cs);
                Cell c7 = r.createCell(7); c7.setCellValue(rData.getTaxAmount()); c7.setCellStyle(csCur);
                Cell c8 = r.createCell(8); c8.setCellValue(rData.getPaymentMode()); c8.setCellStyle(cs);
                Cell c9 = r.createCell(9); c9.setCellValue(rData.getStatus()); c9.setCellStyle(cs);
                Cell c10 = r.createCell(10); c10.setCellValue(rData.getSubmittedBy()); c10.setCellStyle(cs);
                Cell c11 = r.createCell(11); c11.setCellValue(rData.isHasAttachment() ? "Yes" : "No"); c11.setCellStyle(cs);
            }

            sheet.setAutoFilter(new CellRangeAddress(tableHeaderRow, rowIdx - 1, 0, headers.length - 1));
        }

        sheet.createFreezePane(0, tableHeaderRow + 1);
        autoSizeColumns(sheet, 12);
    }

    // ---------------------------------------------------------------------------------------------
    // SHEET 5: CAPITAL & EQUITY SCHEDULE
    // ---------------------------------------------------------------------------------------------
    private void createCapitalSheet(XSSFWorkbook wb, StylesHolder styles, ReportDataContainer data) {
        XSSFSheet sheet = wb.createSheet("Capital");
        sheet.setTabColor(new XSSFColor(COLOR_BLUE, null));
        setupPrint(sheet, "Capital", data.getRequest().getPeriodLabel());

        ScheduleSection sec = data.getCapitalSection();
        int rowIdx = 0;

        rowIdx = createTitleBanner(sheet, styles, rowIdx, "CAPITAL TRANSACTIONS LEDGER - " + data.getRequest().getPeriodLabel(),
                "Timeframe: " + data.getRequest().getStartDate().format(DATE_FMT) + " to " + data.getRequest().getEndDate().format(DATE_FMT),
                COLOR_BLUE, 9);

        rowIdx++;

        createSectionHeader(sheet, styles, rowIdx++, "CAPITAL & OWNER EQUITY SUMMARY", COLOR_BLUE, 9);
        PeriodSummary sum = data.getSummary();
        createMetricRow(sheet, styles, rowIdx++, "Opening Capital Balance", sum.getOpeningCapital(), "Closing Capital Balance", sum.getClosingCapital());
        createMetricRow(sheet, styles, rowIdx++, "Capital Introduced in Period", sum.getCapitalIntroduced(), "Drawings in Period", sum.getCapitalDrawings());
        createCountAndCurrencyRow(sheet, styles, rowIdx++, "Approved Transaction Count", sec.getRecordCount(), "Net Capital Movement", sec.getTotalAmount());

        if (data.getRequest().getType() == ReportType.YEARLY) {
            rowIdx = createMonthlyBreakdownBlock(sheet, styles, rowIdx, sec.getMonthlyBreakdown(), COLOR_BLUE);
        }

        rowIdx++;

        createSectionHeader(sheet, styles, rowIdx++, "ITEMIZED CAPITAL TRANSACTIONS", COLOR_NAVY, 9);

        int tableHeaderRow = rowIdx++;
        Row th = sheet.createRow(tableHeaderRow);
        String[] headers = {"Date", "Reference", "Type", "Owner Name", "Description", "Amount", "Payment Mode", "Status", "Submitted By"};
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(th, i, headers[i], styles.headerStyle(COLOR_BLUE));
        }

        if (sec.getRows().isEmpty()) {
            Row emptyRow = sheet.createRow(rowIdx++);
            Cell c = emptyRow.createCell(0);
            c.setCellValue("No approved capital transactions in this period.");
            c.setCellStyle(styles.normalText);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 8));
        } else {
            for (int i = 0; i < sec.getRows().size(); i++) {
                ReportRow rData = sec.getRows().get(i);
                Row r = sheet.createRow(rowIdx++);
                CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
                CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;
                CellStyle csDate = (i % 2 == 1) ? styles.zebraDate : styles.normalDate;

                Cell c0 = r.createCell(0); c0.setCellValue(rData.getDate() != null ? rData.getDate().format(DATE_FMT) : "-"); c0.setCellStyle(csDate);
                Cell c1 = r.createCell(1); c1.setCellValue(rData.getReference()); c1.setCellStyle(cs);
                Cell c2 = r.createCell(2); c2.setCellValue(rData.getTransactionType()); c2.setCellStyle(cs);
                Cell c3 = r.createCell(3); c3.setCellValue(CellSanitizer.sanitize(rData.getOwnerName())); c3.setCellStyle(cs);
                Cell c4 = r.createCell(4); c4.setCellValue(CellSanitizer.sanitize(rData.getDescription())); c4.setCellStyle(cs);
                Cell c5 = r.createCell(5); c5.setCellValue(rData.getAmount()); c5.setCellStyle(csCur);
                Cell c6 = r.createCell(6); c6.setCellValue(rData.getPaymentMode()); c6.setCellStyle(cs);
                Cell c7 = r.createCell(7); c7.setCellValue(rData.getStatus()); c7.setCellStyle(cs);
                Cell c8 = r.createCell(8); c8.setCellValue(rData.getSubmittedBy()); c8.setCellStyle(cs);
            }

            sheet.setAutoFilter(new CellRangeAddress(tableHeaderRow, rowIdx - 1, 0, headers.length - 1));
        }

        sheet.createFreezePane(0, tableHeaderRow + 1);
        autoSizeColumns(sheet, 9);
    }

    // ---------------------------------------------------------------------------------------------
    // SHEET 6: BORROWINGS & DEBT SCHEDULE
    // ---------------------------------------------------------------------------------------------
    private void createLoansSheet(XSSFWorkbook wb, StylesHolder styles, ReportDataContainer data) {
        XSSFSheet sheet = wb.createSheet("Loans");
        sheet.setTabColor(new XSSFColor(COLOR_AMBER, null));
        setupPrint(sheet, "Loans", data.getRequest().getPeriodLabel());

        ScheduleSection sec = data.getLoansSection();
        int rowIdx = 0;

        rowIdx = createTitleBanner(sheet, styles, rowIdx, "LOANS & DEBT LEDGER - " + data.getRequest().getPeriodLabel(),
                "Timeframe: " + data.getRequest().getStartDate().format(DATE_FMT) + " to " + data.getRequest().getEndDate().format(DATE_FMT),
                COLOR_AMBER, 10);

        rowIdx++;

        createSectionHeader(sheet, styles, rowIdx++, "BORROWINGS & DEBT OBLIGATIONS SUMMARY", COLOR_AMBER, 10);
        PeriodSummary sum = data.getSummary();
        createMetricRow(sheet, styles, rowIdx++, "Opening Loan Debt", sum.getOpeningLoansOutstanding(), "Closing Loan Debt", sum.getClosingLoansOutstanding());
        createMetricRow(sheet, styles, rowIdx++, "Loans Borrowed in Period", sum.getLoansBorrowed(), "Loan Repayments in Period", sum.getLoansRepaid());
        createCountAndCurrencyRow(sheet, styles, rowIdx++, "Approved Transaction Count", sec.getRecordCount(), "Net Debt Movement", sec.getTotalAmount());

        if (data.getRequest().getType() == ReportType.YEARLY) {
            rowIdx = createMonthlyBreakdownBlock(sheet, styles, rowIdx, sec.getMonthlyBreakdown(), COLOR_AMBER);
        }

        rowIdx++;

        // Facility Status in Period
        createSectionHeader(sheet, styles, rowIdx++, "PERIOD LOAN FACILITY BALANCES (OPENING vs CLOSING)", COLOR_NAVY, 10);
        Row facHead = sheet.createRow(rowIdx++);
        createHeaderCell(facHead, 0, "Facility Name", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 1, "Lender", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 2, "Opening Balance", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 3, "Borrowed in Period", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 4, "Repaid in Period", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 5, "Closing Balance", styles.headerStyle(COLOR_AMBER));
        createHeaderCell(facHead, 6, "Status", styles.headerStyle(COLOR_AMBER));

        for (int i = 0; i < sec.getLoanFacilities().size(); i++) {
            LoanFacilityReportItem item = sec.getLoanFacilities().get(i);
            Row r = sheet.createRow(rowIdx++);
            CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
            CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;

            Cell c0 = r.createCell(0); c0.setCellValue(item.getLoanName()); c0.setCellStyle(cs);
            Cell c1 = r.createCell(1); c1.setCellValue(item.getLenderName()); c1.setCellStyle(cs);
            Cell c2 = r.createCell(2); c2.setCellValue(item.getOpeningOutstanding()); c2.setCellStyle(csCur);
            Cell c3 = r.createCell(3); c3.setCellValue(item.getBorrowedInPeriod()); c3.setCellStyle(csCur);
            Cell c4 = r.createCell(4); c4.setCellValue(item.getRepaidInPeriod()); c4.setCellStyle(csCur);
            Cell c5 = r.createCell(5); c5.setCellValue(item.getClosingOutstanding()); c5.setCellStyle(csCur);
            Cell c6 = r.createCell(6); c6.setCellValue(item.getStatus()); c6.setCellStyle(cs);
        }

        rowIdx++;

        createSectionHeader(sheet, styles, rowIdx++, "ITEMIZED LOAN TRANSACTIONS", COLOR_NAVY, 10);

        int tableHeaderRow = rowIdx++;
        Row th = sheet.createRow(tableHeaderRow);
        String[] headers = {"Date", "Reference", "Type", "Loan Facility", "Lender", "Description", "Amount", "Payment Mode", "Status", "Submitted By"};
        for (int i = 0; i < headers.length; i++) {
            createHeaderCell(th, i, headers[i], styles.headerStyle(COLOR_AMBER));
        }

        if (sec.getRows().isEmpty()) {
            Row emptyRow = sheet.createRow(rowIdx++);
            Cell c = emptyRow.createCell(0);
            c.setCellValue("No approved loan transactions in this period.");
            c.setCellStyle(styles.normalText);
            sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 9));
        } else {
            for (int i = 0; i < sec.getRows().size(); i++) {
                ReportRow rData = sec.getRows().get(i);
                Row r = sheet.createRow(rowIdx++);
                CellStyle cs = (i % 2 == 1) ? styles.zebraText : styles.normalText;
                CellStyle csCur = (i % 2 == 1) ? styles.zebraCurrency : styles.normalCurrency;
                CellStyle csDate = (i % 2 == 1) ? styles.zebraDate : styles.normalDate;

                Cell c0 = r.createCell(0); c0.setCellValue(rData.getDate() != null ? rData.getDate().format(DATE_FMT) : "-"); c0.setCellStyle(csDate);
                Cell c1 = r.createCell(1); c1.setCellValue(rData.getReference()); c1.setCellStyle(cs);
                Cell c2 = r.createCell(2); c2.setCellValue(rData.getTransactionType()); c2.setCellStyle(cs);
                Cell c3 = r.createCell(3); c3.setCellValue(rData.getLoanName()); c3.setCellStyle(cs);
                Cell c4 = r.createCell(4); c4.setCellValue(rData.getLenderName()); c4.setCellStyle(cs);
                Cell c5 = r.createCell(5); c5.setCellValue(CellSanitizer.sanitize(rData.getDescription())); c5.setCellStyle(cs);
                Cell c6 = r.createCell(6); c6.setCellValue(rData.getAmount()); c6.setCellStyle(csCur);
                Cell c7 = r.createCell(7); c7.setCellValue(rData.getPaymentMode()); c7.setCellStyle(cs);
                Cell c8 = r.createCell(8); c8.setCellValue(rData.getStatus()); c8.setCellStyle(cs);
                Cell c9 = r.createCell(9); c9.setCellValue(rData.getSubmittedBy()); c9.setCellStyle(cs);
            }

            sheet.setAutoFilter(new CellRangeAddress(tableHeaderRow, rowIdx - 1, 0, headers.length - 1));
        }

        sheet.createFreezePane(0, tableHeaderRow + 1);
        autoSizeColumns(sheet, 10);
    }

    // ---------------------------------------------------------------------------------------------
    // SHARED BUILDING BLOCKS
    // ---------------------------------------------------------------------------------------------
    private int createTitleBanner(XSSFSheet sheet, StylesHolder styles, int rowIdx, String title, String subtitle, byte[] color, int mergeCols) {
        Row rTitle = sheet.createRow(rowIdx++);
        rTitle.setHeightInPoints(28);
        Cell cTitle = rTitle.createCell(0);
        cTitle.setCellValue(title);
        cTitle.setCellStyle(styles.titleBanner(color));
        for (int i = 1; i < mergeCols; i++) {
            rTitle.createCell(i).setCellStyle(styles.titleBanner(color));
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, mergeCols - 1));

        Row rSub = sheet.createRow(rowIdx++);
        rSub.setHeightInPoints(18);
        Cell cSub = rSub.createCell(0);
        cSub.setCellValue(subtitle);
        cSub.setCellStyle(styles.subtitleBanner(color));
        for (int i = 1; i < mergeCols; i++) {
            rSub.createCell(i).setCellStyle(styles.subtitleBanner(color));
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, mergeCols - 1));

        return rowIdx;
    }

    private void createSectionHeader(XSSFSheet sheet, StylesHolder styles, int rowIdx, String header, byte[] color, int mergeCols) {
        Row r = sheet.createRow(rowIdx);
        r.setHeightInPoints(20);
        Cell c = r.createCell(0);
        c.setCellValue(header);
        c.setCellStyle(styles.sectionHeader(color));
        for (int i = 1; i < mergeCols; i++) {
            r.createCell(i).setCellStyle(styles.sectionHeader(color));
        }
        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, mergeCols - 1));
    }

    private int createScheduleTotalsBlock(XSSFSheet sheet, StylesHolder styles, int rowIdx, String headerTitle, String totalMetricLabel, ScheduleSection sec, byte[] color, int totalCols) {
        createSectionHeader(sheet, styles, rowIdx++, headerTitle, color, Math.min(totalCols, 6));

        createCountAndCurrencyRow(sheet, styles, rowIdx++, "Approved Transaction Count", sec.getRecordCount(), totalMetricLabel, sec.getTotalAmount());
        if (sec.getTotalTax() > 0) {
            createMetricRow(sheet, styles, rowIdx++, "Total Applicable Tax (GST)", sec.getTotalTax(), "Net (Excl. Tax)", sec.getTotalAmount() - sec.getTotalTax());
        }

        // Breakdown entries
        for (Map.Entry<String, Double> entry : sec.getCategoryBreakdown().entrySet()) {
            Row r = sheet.createRow(rowIdx++);
            Cell c0 = r.createCell(0); c0.setCellValue("   • " + entry.getKey()); c0.setCellStyle(styles.normalText);
            Cell c1 = r.createCell(1); c1.setCellValue(entry.getValue()); c1.setCellStyle(styles.normalCurrency);
        }

        return rowIdx;
    }

    private int createMonthlyBreakdownBlock(XSSFSheet sheet, StylesHolder styles, int rowIdx, List<Double> monthlyAmounts, byte[] color) {
        createSectionHeader(sheet, styles, rowIdx++, "FINANCIAL YEAR MONTH-BY-MONTH BREAKDOWN (APRIL TO MARCH)", color, 12);

        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < 12; i++) {
            createHeaderCell(headerRow, i, FinancialYear.FY_MONTH_NAMES.get(i), styles.headerStyle(color));
        }

        Row valRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < 12; i++) {
            Cell c = valRow.createCell(i);
            double val = (i < monthlyAmounts.size()) ? monthlyAmounts.get(i) : 0.0;
            c.setCellValue(val);
            c.setCellStyle(styles.normalCurrency);
        }

        return rowIdx;
    }

    private void createMetricRow(XSSFSheet sheet, StylesHolder styles, int rowIdx, String label1, double val1, String label2, double val2) {
        Row r = sheet.createRow(rowIdx);
        r.setHeightInPoints(20);

        Cell c0 = r.createCell(0); c0.setCellValue(label1); c0.setCellStyle(styles.cardLabel);
        Cell c1 = r.createCell(1); c1.setCellValue(val1); c1.setCellStyle(styles.cardValue);

        Cell c2 = r.createCell(2); c2.setCellStyle(styles.normalText); // separator

        Cell c3 = r.createCell(3); c3.setCellValue(label2); c3.setCellStyle(styles.cardLabel);
        Cell c4 = r.createCell(4); c4.setCellValue(val2); c4.setCellStyle(styles.cardValue);
    }

    private void createCountAndCurrencyRow(XSSFSheet sheet, StylesHolder styles, int rowIdx, String countLabel, long countVal, String currLabel, double currVal) {
        Row r = sheet.createRow(rowIdx);
        r.setHeightInPoints(20);

        Cell c0 = r.createCell(0); c0.setCellValue(countLabel); c0.setCellStyle(styles.cardLabel);
        Cell c1 = r.createCell(1); c1.setCellValue((double) countVal); c1.setCellStyle(styles.cardCount);

        Cell c2 = r.createCell(2); c2.setCellStyle(styles.normalText); // separator

        Cell c3 = r.createCell(3); c3.setCellValue(currLabel); c3.setCellStyle(styles.cardLabel);
        Cell c4 = r.createCell(4); c4.setCellValue(currVal); c4.setCellStyle(styles.cardValue);
    }

    private void createHeaderCell(Row r, int col, String val, CellStyle style) {
        Cell c = r.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private void setupPrint(XSSFSheet sheet, String title, String period) {
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(true);
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);

        Footer footer = sheet.getFooter();
        footer.setLeft("Mushroom Farm | " + title + " (" + period + ")");
        footer.setRight("Page &P of &N");
    }

    private void autoSizeColumns(XSSFSheet sheet, int maxCols) {
        for (int i = 0; i < maxCols; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(currentWidth + 1200, 3500));
        }
    }

    // ---------------------------------------------------------------------------------------------
    // STYLES CONTAINER
    // ---------------------------------------------------------------------------------------------
    private static class StylesHolder {
        private final XSSFWorkbook wb;
        private final DataFormat df;

        final CellStyle normalText;
        final CellStyle normalCurrency;
        final CellStyle normalDate;
        final CellStyle zebraText;
        final CellStyle zebraCurrency;
        final CellStyle zebraDate;
        final CellStyle cardLabel;
        final CellStyle cardValue;
        final CellStyle cardCount;
        final CellStyle badgeSuccess;

        StylesHolder(XSSFWorkbook wb) {
            this.wb = wb;
            this.df = wb.createDataFormat();

            Font fontNormal = wb.createFont();
            fontNormal.setFontName("Calibri");
            fontNormal.setFontHeightInPoints((short) 11);

            Font fontBold = wb.createFont();
            fontBold.setFontName("Calibri");
            fontBold.setFontHeightInPoints((short) 11);
            fontBold.setBold(true);

            // INR currency format
            short currFmt = df.getFormat("\"₹\"#,##0.00;(\"₹\"#,##0.00);\"-\"");
            short countFmt = df.getFormat("#,##0");
            short dateFmt = df.getFormat("dd-mmm-yyyy");

            // Normal Text
            normalText = wb.createCellStyle();
            normalText.setFont(fontNormal);
            setThinBorders(normalText);

            // Normal Currency
            normalCurrency = wb.createCellStyle();
            normalCurrency.setFont(fontNormal);
            normalCurrency.setDataFormat(currFmt);
            normalCurrency.setAlignment(HorizontalAlignment.RIGHT);
            setThinBorders(normalCurrency);

            // Normal Date
            normalDate = wb.createCellStyle();
            normalDate.setFont(fontNormal);
            normalDate.setDataFormat(dateFmt);
            setThinBorders(normalDate);

            // Zebra Rows
            XSSFColor zebraFill = new XSSFColor(COLOR_ZEBRA, null);

            zebraText = wb.createCellStyle();
            zebraText.setFont(fontNormal);
            ((XSSFCellStyle) zebraText).setFillForegroundColor(zebraFill);
            zebraText.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(zebraText);

            zebraCurrency = wb.createCellStyle();
            zebraCurrency.setFont(fontNormal);
            zebraCurrency.setDataFormat(currFmt);
            zebraCurrency.setAlignment(HorizontalAlignment.RIGHT);
            ((XSSFCellStyle) zebraCurrency).setFillForegroundColor(zebraFill);
            zebraCurrency.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(zebraCurrency);

            zebraDate = wb.createCellStyle();
            zebraDate.setFont(fontNormal);
            zebraDate.setDataFormat(dateFmt);
            ((XSSFCellStyle) zebraDate).setFillForegroundColor(zebraFill);
            zebraDate.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(zebraDate);

            // Cards
            XSSFColor cardFill = new XSSFColor(COLOR_CARD, null);

            cardLabel = wb.createCellStyle();
            cardLabel.setFont(fontBold);
            ((XSSFCellStyle) cardLabel).setFillForegroundColor(cardFill);
            cardLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(cardLabel);

            cardValue = wb.createCellStyle();
            cardValue.setFont(fontBold);
            cardValue.setDataFormat(currFmt);
            cardValue.setAlignment(HorizontalAlignment.RIGHT);
            ((XSSFCellStyle) cardValue).setFillForegroundColor(cardFill);
            cardValue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(cardValue);

            cardCount = wb.createCellStyle();
            cardCount.setFont(fontBold);
            cardCount.setDataFormat(countFmt);
            cardCount.setAlignment(HorizontalAlignment.RIGHT);
            ((XSSFCellStyle) cardCount).setFillForegroundColor(cardFill);
            cardCount.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(cardCount);

            // Badge
            Font fontSuccess = wb.createFont();
            fontSuccess.setFontName("Calibri");
            fontSuccess.setFontHeightInPoints((short) 10);
            fontSuccess.setBold(true);
            fontSuccess.setColor(IndexedColors.DARK_GREEN.getIndex());

            badgeSuccess = wb.createCellStyle();
            badgeSuccess.setFont(fontSuccess);
            badgeSuccess.setAlignment(HorizontalAlignment.CENTER);
            setThinBorders(badgeSuccess);
        }

        CellStyle titleBanner(byte[] rgb) {
            XSSFCellStyle cs = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 14);
            font.setBold(true);
            font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            cs.setFont(font);
            cs.setFillForegroundColor(new XSSFColor(rgb, null));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            return cs;
        }

        CellStyle subtitleBanner(byte[] rgb) {
            XSSFCellStyle cs = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 10);
            font.setColor(new XSSFColor(new byte[]{(byte) 241, (byte) 245, (byte) 249}, null));
            cs.setFont(font);
            cs.setFillForegroundColor(new XSSFColor(rgb, null));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            return cs;
        }

        CellStyle sectionHeader(byte[] rgb) {
            XSSFCellStyle cs = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 11);
            font.setBold(true);
            font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            cs.setFont(font);
            cs.setFillForegroundColor(new XSSFColor(rgb, null));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            return cs;
        }

        CellStyle headerStyle(byte[] rgb) {
            XSSFCellStyle cs = wb.createCellStyle();
            XSSFFont font = wb.createFont();
            font.setFontName("Calibri");
            font.setFontHeightInPoints((short) 11);
            font.setBold(true);
            font.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, null));
            cs.setFont(font);
            cs.setFillForegroundColor(new XSSFColor(rgb, null));
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setAlignment(HorizontalAlignment.LEFT);
            cs.setVerticalAlignment(VerticalAlignment.CENTER);
            setThinBorders(cs);
            return cs;
        }

        private void setThinBorders(CellStyle cs) {
            cs.setBorderTop(BorderStyle.THIN);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        }
    }
}
