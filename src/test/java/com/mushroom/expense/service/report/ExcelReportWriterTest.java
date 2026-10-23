package com.mushroom.expense.service.report;

import com.mushroom.expense.service.report.ReportData.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelReportWriterTest {

    @Test
    void testWorkbookGenerationAndStructure() throws IOException {
        ExcelReportWriter writer = new ExcelReportWriter();

        ReportDataContainer container = createSampleData();

        byte[] bytes = writer.writeWorkbook(container);
        assertNotNull(bytes);
        assertTrue(bytes.length > 1000);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(5, wb.getNumberOfSheets());
            assertEquals("Lifetime Financials", wb.getSheetName(0));
            assertEquals("Expenses", wb.getSheetName(1));
            assertEquals("Income", wb.getSheetName(2));
            assertEquals("Capital", wb.getSheetName(3));
            assertEquals("Loans", wb.getSheetName(4));

            // Check Sheet 1 (Lifetime Financials)
            XSSFSheet lifetimeSheet = wb.getSheetAt(0);
            assertNotNull(lifetimeSheet.getRow(0)); // Banner Title
            String lifetimeTitle = lifetimeSheet.getRow(0).getCell(0).getStringCellValue();
            assertTrue(lifetimeTitle.contains("LIFETIME FINANCIAL"));

            // Check Sheet 2 (Expenses - Totals on top, detail rows below)
            XSSFSheet expSheet = wb.getSheetAt(1);
            assertNotNull(expSheet.getRow(0)); // Banner
            assertNotNull(expSheet.getPaneInformation(), "Freeze pane should be configured");
            assertTrue(expSheet.getPaneInformation().isFreezePane());

            // Check "Approved Transaction Count" formatting: must NOT contain rupee symbol ₹
            // Row 4 is the totals metric row for Approved Transaction Count
            org.apache.poi.ss.usermodel.Row countRow = expSheet.getRow(4);
            assertNotNull(countRow);
            assertEquals("Approved Transaction Count", countRow.getCell(0).getStringCellValue());
            assertEquals(1.0, countRow.getCell(1).getNumericCellValue());
            String dataFormatString = countRow.getCell(1).getCellStyle().getDataFormatString();
            assertFalse(dataFormatString.contains("₹"), "Approved Transaction Count format must not contain currency symbol ₹");
            assertEquals("#,##0", dataFormatString);

            // Check numeric values in detail rows are NUMERIC type (not string), satisfying SC-005
            int lastRowNum = expSheet.getLastRowNum();
            assertTrue(lastRowNum >= 10);
            // Detail row amount is at column 5
            assertEquals(CellType.NUMERIC, expSheet.getRow(lastRowNum).getCell(5).getCellType());
            assertEquals(1500.0, expSheet.getRow(lastRowNum).getCell(5).getNumericCellValue());
        }
    }

    private ReportDataContainer createSampleData() {
        ReportDataContainer c = new ReportDataContainer();

        ReportRequest req = new ReportRequest();
        req.setType(ReportType.MONTHLY);
        req.setPeriodLabel("Oct 2025 (FY 2025-26)");
        req.setStartDate(LocalDate.of(2025, 10, 1));
        req.setEndDate(LocalDate.of(2025, 10, 31));
        req.setRequestedBy("accountant");
        c.setRequest(req);

        PeriodSummary sum = new PeriodSummary();
        sum.setPeriodLabel(req.getPeriodLabel());
        sum.setStartDate(req.getStartDate());
        sum.setEndDate(req.getEndDate());
        sum.setGeneratedAt(LocalDateTime.now());
        sum.setGeneratedBy("accountant");
        sum.setTotalExpense(1500.0);
        sum.setTotalIncome(5000.0);
        sum.setNetBalance(3500.0);
        sum.setOpeningCapital(50000.0);
        sum.setClosingCapital(50000.0);
        sum.setOpeningLoansOutstanding(20000.0);
        sum.setClosingLoansOutstanding(20000.0);
        sum.setScheduleSummaries(List.of(
                new ScheduleSummaryItem("Operating Expenses Schedule", 1, 1500.0),
                new ScheduleSummaryItem("Revenue & Operating Income Schedule", 0, 0.0),
                new ScheduleSummaryItem("Owner's Equity & Capital (Net Flow)", 0, 0.0),
                new ScheduleSummaryItem("Borrowings & Debt Obligations (Net Flow)", 0, 0.0)
        ));
        c.setSummary(sum);

        LifetimeFinancials lf = new LifetimeFinancials();
        lf.setAsOfDate(LocalDateTime.now());
        lf.setGeneratedBy("accountant");
        lf.setGlobalTotalExpense(20000.0);
        lf.setGlobalTotalIncome(80000.0);
        lf.setGlobalNetBalance(60000.0);
        lf.setGlobalCapitalIntroduced(100000.0);
        lf.setGlobalNetCapital(100000.0);
        lf.setGlobalLoansBorrowed(50000.0);
        lf.setGlobalOutstandingLoans(50000.0);
        lf.setGlobalNetCashPosition(210000.0);
        lf.setExpensesByCategory(List.of(new CategorySummary("Spawn", 20000.0)));
        lf.setIncomesByCategory(List.of(new CategorySummary("Mushroom Sales", 80000.0)));
        c.setLifetimeFinancials(lf);

        // Expenses
        ScheduleSection exp = new ScheduleSection();
        exp.setScheduleName("Operating Expenses");
        exp.setRecordCount(1);
        exp.setTotalAmount(1500.0);
        exp.setTotalTax(270.0);
        exp.getCategoryBreakdown().put("Spawn", 1500.0);

        ReportRow r = new ReportRow();
        r.setId(1L);
        r.setDate(LocalDate.of(2025, 10, 15));
        r.setReference("EXP-1");
        r.setCategory("Spawn");
        r.setSubCategory("Oyster");
        r.setDescription("Spawn bags");
        r.setAmount(1500.0);
        r.setTaxPercentage(18.0);
        r.setTaxAmount(270.0);
        r.setPaymentMode("UPI");
        r.setStatus("APPROVED");
        r.setSubmittedBy("farmer");
        exp.getRows().add(r);
        c.setExpensesSection(exp);

        // Income
        ScheduleSection inc = new ScheduleSection();
        inc.setScheduleName("Revenue & Operating Income");
        c.setIncomeSection(inc);

        // Capital
        ScheduleSection cap = new ScheduleSection();
        cap.setScheduleName("Capital & Equity");
        c.setCapitalSection(cap);

        // Loans
        ScheduleSection loans = new ScheduleSection();
        loans.setScheduleName("Borrowings & Debt Obligations");
        c.setLoansSection(loans);

        return c;
    }
}
