package com.mushroom.expense.service.report;

import com.mushroom.expense.service.report.ReportData.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

class CsvReportWriterTest {

    @Test
    void testZipArchiveContainsAllFiveFiles() throws IOException {
        CsvReportWriter writer = new CsvReportWriter();
        ReportDataContainer container = createSampleData();

        byte[] zipBytes = writer.writeZipArchive(container);
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 500);

        Map<String, String> files = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                byte[] content = zis.readAllBytes();
                String text = new String(content, StandardCharsets.UTF_8);
                files.put(entry.getName(), text);
                zis.closeEntry();
            }
        }

        assertEquals(5, files.size());
        assertTrue(files.containsKey("00_Lifetime_Financials.csv"));
        assertTrue(files.containsKey("01_Expenses.csv"));
        assertTrue(files.containsKey("02_Income.csv"));
        assertTrue(files.containsKey("03_Capital.csv"));
        assertTrue(files.containsKey("04_Loans.csv"));

        // Check BOM
        for (Map.Entry<String, String> f : files.entrySet()) {
            assertTrue(f.getValue().startsWith("\uFEFF"), "File " + f.getKey() + " must start with UTF-8 BOM");
        }

        // Verify Lifetime contents
        String lifetimeCsv = files.get("00_Lifetime_Financials.csv");
        assertTrue(lifetimeCsv.contains("Lifetime Total Income,80000.0"));
        assertTrue(lifetimeCsv.contains("Overall Net Cash Position,210000.0"));

        // Verify RFC-4180 Escaping for commas/quotes in descriptions in Expenses CSV
        String expensesCsv = files.get("01_Expenses.csv");
        assertTrue(expensesCsv.contains("\"Wheat straw, premium \"\"A\"\" grade\""));

        // Verify Opening & Closing balances exist in Capital and Loans CSV
        String capitalCsv = files.get("03_Capital.csv");
        assertTrue(capitalCsv.contains("Opening Capital Balance (INR),50000.0"));
        assertTrue(capitalCsv.contains("Closing Capital Balance (INR),50000.0"));

        String loansCsv = files.get("04_Loans.csv");
        assertTrue(loansCsv.contains("Opening Loan Debt (INR),20000.0"));
        assertTrue(loansCsv.contains("Closing Loan Debt (INR),20000.0"));
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
        sum.setScheduleSummaries(List.of(new ScheduleSummaryItem("Operating Expenses Schedule", 1, 1500.0)));
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
        c.setLifetimeFinancials(lf);

        ScheduleSection exp = new ScheduleSection();
        exp.setScheduleName("Operating Expenses");
        exp.setRecordCount(1);
        exp.setTotalAmount(1500.0);
        exp.getCategoryBreakdown().put("Substrate", 1500.0);

        ReportRow r = new ReportRow();
        r.setId(1L);
        r.setDate(LocalDate.of(2025, 10, 15));
        r.setReference("EXP-1");
        r.setCategory("Substrate");
        r.setDescription("Wheat straw, premium \"A\" grade");
        r.setAmount(1500.0);
        r.setTaxPercentage(5.0);
        r.setTaxAmount(75.0);
        r.setPaymentMode("BANK_TRANSFER");
        r.setStatus("APPROVED");
        r.setSubmittedBy("accountant");
        exp.getRows().add(r);
        c.setExpensesSection(exp);

        c.setIncomeSection(new ScheduleSection());
        c.setCapitalSection(new ScheduleSection());
        c.setLoansSection(new ScheduleSection());

        return c;
    }
}
