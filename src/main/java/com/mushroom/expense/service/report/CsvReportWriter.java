package com.mushroom.expense.service.report;

import com.mushroom.expense.service.report.ReportData.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates an audit-ready zip archive containing 5 UTF-8 CSV files:
 * 00_Lifetime_Financials.csv
 * 01_Expenses.csv
 * 02_Income.csv
 * 03_Capital.csv
 * 04_Loans.csv
 */
public class CsvReportWriter {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] writeZipArchive(ReportDataContainer data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            addZipEntry(zos, "00_Lifetime_Financials.csv", generateLifetimeCsv(data.getLifetimeFinancials()));
            addZipEntry(zos, "01_Expenses.csv", generateExpensesCsv(data.getExpensesSection()));
            addZipEntry(zos, "02_Income.csv", generateIncomeCsv(data.getIncomeSection()));
            addZipEntry(zos, "03_Capital.csv", generateCapitalCsv(data.getCapitalSection(), data.getSummary()));
            addZipEntry(zos, "04_Loans.csv", generateLoansCsv(data.getLoansSection(), data.getSummary()));
        }
        return baos.toByteArray();
    }

    private void addZipEntry(ZipOutputStream zos, String filename, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        zos.write(content);
        zos.closeEntry();
    }

    private byte[] generateLifetimeCsv(LifetimeFinancials lf) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write("\uFEFF"); // UTF-8 BOM

            writer.println("LIFETIME FINANCIALS & BALANCE SHEET FOUNDATION");
            writer.println("As of Date," + escape(lf.getAsOfDate().format(TS_FMT)));
            writer.println("Prepared By," + escape(lf.getGeneratedBy()));
            writer.println();

            writer.println("OPERATIONAL & FINANCING TOTALS,Amount (INR)");
            writer.println("Lifetime Total Income," + lf.getGlobalTotalIncome());
            writer.println("Lifetime Total Expenses," + lf.getGlobalTotalExpense());
            writer.println("Lifetime Net Operating Balance," + lf.getGlobalNetBalance());
            writer.println("Lifetime Capital Introduced," + lf.getGlobalCapitalIntroduced());
            writer.println("Lifetime Capital Drawings," + lf.getGlobalCapitalDrawings());
            writer.println("Net Capital Pool (Equity)," + lf.getGlobalNetCapital());
            writer.println("Lifetime Loans Borrowed," + lf.getGlobalLoansBorrowed());
            writer.println("Lifetime Loan Repayments," + lf.getGlobalLoansRepaid());
            writer.println("Current Outstanding Loan Debt," + lf.getGlobalOutstandingLoans());
            writer.println("Overall Net Cash Position," + lf.getGlobalNetCashPosition());
            writer.println();

            writer.println("EXPENSES BY CATEGORY (LIFETIME)");
            writer.println("Category,Cumulative Amount");
            for (CategorySummary cs : lf.getExpensesByCategory()) {
                writer.println(escape(cs.getName()) + "," + cs.getAmount());
            }
            writer.println();

            writer.println("INCOME BY CATEGORY (LIFETIME)");
            writer.println("Category,Cumulative Amount");
            for (CategorySummary cs : lf.getIncomesByCategory()) {
                writer.println(escape(cs.getName()) + "," + cs.getAmount());
            }
            writer.println();

            writer.println("LOAN FACILITIES STATUS");
            writer.println("Loan Facility,Lender,Cumulative Borrowed,Cumulative Repaid,Current Outstanding,Status");
            for (LoanFacilityReportItem item : lf.getLoanFacilities()) {
                writer.println(String.join(",",
                        escape(item.getLoanName()),
                        escape(item.getLenderName()),
                        String.valueOf(item.getBorrowedInPeriod()),
                        String.valueOf(item.getRepaidInPeriod()),
                        String.valueOf(item.getClosingOutstanding()),
                        escape(item.getStatus())
                ));
            }
        }
        return out.toByteArray();
    }

    private byte[] generateExpensesCsv(ScheduleSection sec) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.println("OPERATING EXPENSES SCHEDULE TOTALS");
            writer.println("Approved Transaction Count," + sec.getRecordCount());
            writer.println("Total Expenditure (INR)," + sec.getTotalAmount());
            writer.println("Total Applicable Tax (GST)," + sec.getTotalTax());
            writer.println();

            writer.println("EXPENSES BY CATEGORY");
            for (Map.Entry<String, Double> e : sec.getCategoryBreakdown().entrySet()) {
                writer.println(escape(e.getKey()) + "," + e.getValue());
            }
            writer.println();

            writer.println("ITEMIZED EXPENSES");
            writer.println("Date,Reference,Category,SubCategory,Description,Amount,TaxPercentage,TaxAmount,PaymentMode,Status,SubmittedBy,Attachment");
            for (ReportRow r : sec.getRows()) {
                writer.println(String.join(",",
                        r.getDate() != null ? r.getDate().format(DATE_FMT) : "",
                        escape(r.getReference()),
                        escape(CellSanitizer.sanitize(r.getCategory())),
                        escape(CellSanitizer.sanitize(r.getSubCategory())),
                        escape(CellSanitizer.sanitize(r.getDescription())),
                        String.valueOf(r.getAmount()),
                        String.valueOf(r.getTaxPercentage()),
                        String.valueOf(r.getTaxAmount()),
                        escape(r.getPaymentMode()),
                        escape(r.getStatus()),
                        escape(r.getSubmittedBy()),
                        r.isHasAttachment() ? "Yes" : "No"
                ));
            }
        }
        return out.toByteArray();
    }

    private byte[] generateIncomeCsv(ScheduleSection sec) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.println("REVENUE & OPERATING INCOME TOTALS");
            writer.println("Approved Transaction Count," + sec.getRecordCount());
            writer.println("Total Revenue (INR)," + sec.getTotalAmount());
            writer.println("Total Applicable Tax (GST)," + sec.getTotalTax());
            writer.println();

            writer.println("INCOME BY CATEGORY");
            for (Map.Entry<String, Double> e : sec.getCategoryBreakdown().entrySet()) {
                writer.println(escape(e.getKey()) + "," + e.getValue());
            }
            writer.println();

            writer.println("ITEMIZED INCOME");
            writer.println("Date,Reference,Category,SubCategory,Description,Amount,TaxPercentage,TaxAmount,PaymentMode,Status,SubmittedBy,Attachment");
            for (ReportRow r : sec.getRows()) {
                writer.println(String.join(",",
                        r.getDate() != null ? r.getDate().format(DATE_FMT) : "",
                        escape(r.getReference()),
                        escape(CellSanitizer.sanitize(r.getCategory())),
                        escape(CellSanitizer.sanitize(r.getSubCategory())),
                        escape(CellSanitizer.sanitize(r.getDescription())),
                        String.valueOf(r.getAmount()),
                        String.valueOf(r.getTaxPercentage()),
                        String.valueOf(r.getTaxAmount()),
                        escape(r.getPaymentMode()),
                        escape(r.getStatus()),
                        escape(r.getSubmittedBy()),
                        r.isHasAttachment() ? "Yes" : "No"
                ));
            }
        }
        return out.toByteArray();
    }

    private byte[] generateCapitalCsv(ScheduleSection sec, PeriodSummary s) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.println("CAPITAL & OWNER EQUITY SUMMARY");
            writer.println("Approved Transaction Count," + sec.getRecordCount());
            if (s != null) {
                writer.println("Opening Capital Balance (INR)," + s.getOpeningCapital());
                writer.println("Capital Introduced in Period (INR)," + s.getCapitalIntroduced());
                writer.println("Drawings in Period (INR)," + s.getCapitalDrawings());
                writer.println("Net Capital Movement (INR)," + sec.getTotalAmount());
                writer.println("Closing Capital Balance (INR)," + s.getClosingCapital());
            } else {
                writer.println("Net Capital Flow (INR)," + sec.getTotalAmount());
            }
            writer.println();

            writer.println("FLOWS BY TYPE");
            for (Map.Entry<String, Double> e : sec.getCategoryBreakdown().entrySet()) {
                writer.println(escape(e.getKey()) + "," + e.getValue());
            }
            writer.println();

            writer.println("ITEMIZED CAPITAL TRANSACTIONS");
            writer.println("Date,Reference,Type,OwnerName,Description,Amount,PaymentMode,Status,SubmittedBy");
            for (ReportRow r : sec.getRows()) {
                writer.println(String.join(",",
                        r.getDate() != null ? r.getDate().format(DATE_FMT) : "",
                        escape(r.getReference()),
                        escape(r.getTransactionType()),
                        escape(CellSanitizer.sanitize(r.getOwnerName())),
                        escape(CellSanitizer.sanitize(r.getDescription())),
                        String.valueOf(r.getAmount()),
                        escape(r.getPaymentMode()),
                        escape(r.getStatus()),
                        escape(r.getSubmittedBy())
                ));
            }
        }
        return out.toByteArray();
    }

    private byte[] generateLoansCsv(ScheduleSection sec, PeriodSummary s) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.write("\uFEFF");

            writer.println("BORROWINGS & DEBT OBLIGATIONS SUMMARY");
            writer.println("Approved Transaction Count," + sec.getRecordCount());
            if (s != null) {
                writer.println("Opening Loan Debt (INR)," + s.getOpeningLoansOutstanding());
                writer.println("Loans Borrowed in Period (INR)," + s.getLoansBorrowed());
                writer.println("Principal Repaid in Period (INR)," + s.getLoansRepaid());
                writer.println("Net Debt Movement (INR)," + sec.getTotalAmount());
                writer.println("Closing Loan Debt (INR)," + s.getClosingLoansOutstanding());
            } else {
                writer.println("Net Debt Flow (INR)," + sec.getTotalAmount());
            }
            writer.println();

            writer.println("FLOWS BY TYPE");
            for (Map.Entry<String, Double> e : sec.getCategoryBreakdown().entrySet()) {
                writer.println(escape(e.getKey()) + "," + e.getValue());
            }
            writer.println();

            writer.println("LOAN FACILITY BALANCES IN PERIOD");
            writer.println("FacilityName,Lender,OpeningOutstanding,BorrowedInPeriod,RepaidInPeriod,ClosingOutstanding,Status");
            for (LoanFacilityReportItem item : sec.getLoanFacilities()) {
                writer.println(String.join(",",
                        escape(item.getLoanName()),
                        escape(item.getLenderName()),
                        String.valueOf(item.getOpeningOutstanding()),
                        String.valueOf(item.getBorrowedInPeriod()),
                        String.valueOf(item.getRepaidInPeriod()),
                        String.valueOf(item.getClosingOutstanding()),
                        escape(item.getStatus())
                ));
            }
            writer.println();

            writer.println("ITEMIZED LOAN TRANSACTIONS");
            writer.println("Date,Reference,Type,LoanName,Lender,Description,Amount,PaymentMode,Status,SubmittedBy");
            for (ReportRow r : sec.getRows()) {
                writer.println(String.join(",",
                        r.getDate() != null ? r.getDate().format(DATE_FMT) : "",
                        escape(r.getReference()),
                        escape(r.getTransactionType()),
                        escape(r.getLoanName()),
                        escape(r.getLenderName()),
                        escape(CellSanitizer.sanitize(r.getDescription())),
                        String.valueOf(r.getAmount()),
                        escape(r.getPaymentMode()),
                        escape(r.getStatus()),
                        escape(r.getSubmittedBy())
                ));
            }
        }
        return out.toByteArray();
    }

    private String escape(String input) {
        if (input == null) {
            return "";
        }
        if (input.contains(",") || input.contains("\"") || input.contains("\n") || input.contains("\r")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }
}
