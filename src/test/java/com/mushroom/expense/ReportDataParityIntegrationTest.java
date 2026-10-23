package com.mushroom.expense;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.CapitalService;
import com.mushroom.expense.service.LoanService;
import com.mushroom.expense.service.ReportService;
import com.mushroom.expense.service.report.ReportData;
import com.mushroom.expense.service.report.ReportData.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ReportDataParityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CapitalTransactionRepository capitalTransactionRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private LoanService loanService;

    private User accountantUser;
    private Category farmCategory;
    private Loan sampleLoan;

    private final LocalDate testDate = LocalDate.of(2025, 10, 15);
    private final String testFy = "2025-26";

    @BeforeEach
    void setupData() {
        accountantUser = userRepository.findByUsername("accountant")
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername("accountant");
                    u.setPassword("password");
                    u.setRole("ROLE_ACCOUNTANT");
                    return userRepository.save(u);
                });

        farmCategory = categoryRepository.findByNameIgnoreCase("Supplies")
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName("Supplies");
                    c.setType(TransactionType.EXPENSE);
                    return categoryRepository.save(c);
                });

        SubCategory subCat = new SubCategory();
        subCat.setName("Spawn Supplies");
        subCat.setCategory(farmCategory);
        subCat = subCategoryRepository.save(subCat);

        sampleLoan = loanRepository.findByLoanNameIgnoreCase("Facility-Parity")
                .orElseGet(() -> {
                    Loan l = new Loan();
                    l.setLoanName("Facility-Parity");
                    l.setLenderName("HDFC Agri");
                    l.setStartDate(LocalDate.of(2025, 4, 1));
                    l.setStatus(LoanStatus.ACTIVE);
                    return loanRepository.save(l);
                });

        // Seed Approved Expense (within Oct 2025)
        Expense e = new Expense();
        e.setDate(testDate);
        e.setAmount(15000.0);
        e.setTaxPercentage(18.0);
        e.setStatus(ExpenseStatus.APPROVED);
        e.setDescription("Mushroom Spawn Substrate");
        e.setPaymentMode(PaymentMode.BANK_TRANSFER);
        e.setCategory(farmCategory);
        e.setSubCategory(subCat);
        e.setUser(accountantUser);
        expenseRepository.save(e);

        // Seed Unapproved Expense (DRAFT - should be excluded from totals)
        Expense eDraft = new Expense();
        eDraft.setDate(testDate);
        eDraft.setAmount(5000.0);
        eDraft.setStatus(ExpenseStatus.DRAFT);
        eDraft.setDescription("Draft order");
        eDraft.setPaymentMode(PaymentMode.CASH);
        eDraft.setCategory(farmCategory);
        eDraft.setSubCategory(subCat);
        eDraft.setUser(accountantUser);
        expenseRepository.save(eDraft);

        // Seed Approved Income (within Oct 2025)
        Income inc = new Income();
        inc.setDate(testDate);
        inc.setAmount(45000.0);
        inc.setTaxPercentage(5.0);
        inc.setStatus(IncomeStatus.APPROVED);
        inc.setDescription("Fresh Button Mushrooms Batch #101");
        inc.setPaymentMode(PaymentMode.UPI);
        inc.setCategory(farmCategory);
        inc.setSubCategory(subCat);
        inc.setUser(accountantUser);
        incomeRepository.save(inc);

        // Seed Approved Capital Introduced (within Oct 2025)
        CapitalTransaction capIntro = new CapitalTransaction();
        capIntro.setType(CapitalTransactionType.INTRODUCED);
        capIntro.setDate(testDate);
        capIntro.setAmount(50000.0);
        capIntro.setStatus(IncomeStatus.APPROVED);
        capIntro.setOwnerName("John Doe");
        capIntro.setDescription("Working Capital Injection");
        capIntro.setPaymentMode(PaymentMode.BANK_TRANSFER);
        capIntro.setUser(accountantUser);
        capitalTransactionRepository.save(capIntro);

        // Seed Approved Capital Drawings (within Oct 2025)
        CapitalTransaction capDraw = new CapitalTransaction();
        capDraw.setType(CapitalTransactionType.DRAWINGS);
        capDraw.setDate(testDate);
        capDraw.setAmount(10000.0);
        capDraw.setStatus(IncomeStatus.APPROVED);
        capDraw.setOwnerName("John Doe");
        capDraw.setDescription("Personal Drawing");
        capDraw.setPaymentMode(PaymentMode.CASH);
        capDraw.setUser(accountantUser);
        capitalTransactionRepository.save(capDraw);

        // Seed Approved Loan Borrowed (within Oct 2025)
        LoanTransaction ltBorrow = new LoanTransaction();
        ltBorrow.setLoan(sampleLoan);
        ltBorrow.setType(LoanTransactionType.BORROWED);
        ltBorrow.setDate(testDate);
        ltBorrow.setAmount(80000.0);
        ltBorrow.setStatus(IncomeStatus.APPROVED);
        ltBorrow.setDescription("Equipment loan tranche");
        ltBorrow.setPaymentMode(PaymentMode.BANK_TRANSFER);
        ltBorrow.setUser(accountantUser);
        loanTransactionRepository.save(ltBorrow);

        // Seed Approved Loan Repayment (within Oct 2025)
        LoanTransaction ltRepay = new LoanTransaction();
        ltRepay.setLoan(sampleLoan);
        ltRepay.setType(LoanTransactionType.REPAYMENT);
        ltRepay.setDate(testDate);
        ltRepay.setAmount(20000.0);
        ltRepay.setStatus(IncomeStatus.APPROVED);
        ltRepay.setDescription("Monthly EMI repayment");
        ltRepay.setPaymentMode(PaymentMode.BANK_TRANSFER);
        ltRepay.setUser(accountantUser);
        loanTransactionRepository.save(ltRepay);
    }

    @Test
    @DisplayName("Verify 100% mathematical parity between Dashboard calculations and Report Data")
    void testMathematicalParityWithDashboard() {
        ReportRequest req = new ReportRequest();
        req.setType(ReportType.MONTHLY);
        req.setYear(2025);
        req.setMonth(10);
        req.setRequestedBy("accountant");

        ReportDataContainer report = reportService.compileReportData(req);

        // 1. Period Totals Parity
        PeriodSummary sum = report.getSummary();
        assertEquals(45000.0, sum.getTotalIncome(), "Total Income must match approved income sum");
        assertEquals(15000.0, sum.getTotalExpense(), "Total Expense must match approved expense sum");
        assertEquals(30000.0, sum.getNetBalance(), "Net Operating Balance must equal Income - Expense");

        assertEquals(50000.0, sum.getCapitalIntroduced(), "Capital Introduced must match approved intro sum");
        assertEquals(10000.0, sum.getCapitalDrawings(), "Capital Drawings must match approved drawing sum");
        assertEquals(40000.0, sum.getNetCapitalFlow(), "Net Capital Flow must equal Intro - Drawings");

        assertEquals(80000.0, sum.getLoansBorrowed(), "Loans Borrowed must match approved borrowed sum");
        assertEquals(20000.0, sum.getLoansRepaid(), "Loans Repaid must match approved repayment sum");
        assertEquals(60000.0, sum.getNetLoanFlow(), "Net Loan Flow must equal Borrowed - Repaid");

        double expectedNetCashFlow = sum.getNetBalance() + sum.getNetCapitalFlow() + sum.getNetLoanFlow();
        assertEquals(130000.0, expectedNetCashFlow);
        assertEquals(expectedNetCashFlow, sum.getNetCashFlow(), "Net Cash Flow must equal Operating + Capital + Loans");

        // Excluded Count Check
        assertTrue(sum.getExcludedRecordCount() >= 1, "Excluded records must include the draft expense");

        // 2. Schedule Tab Row Sums Reconciliation with Sheet 1
        double expensesRowSum = report.getExpensesSection().getRows().stream().mapToDouble(ReportRow::getAmount).sum();
        assertEquals(sum.getTotalExpense(), expensesRowSum, "Expenses tab detail rows sum must exactly match Sheet 1");

        double incomeRowSum = report.getIncomeSection().getRows().stream().mapToDouble(ReportRow::getAmount).sum();
        assertEquals(sum.getTotalIncome(), incomeRowSum, "Income tab detail rows sum must exactly match Sheet 1");

        // 3. Lifetime Parity
        LifetimeFinancials lf = report.getLifetimeFinancials();
        assertEquals(capitalService.getTotalApprovedIntroduced(), lf.getGlobalCapitalIntroduced());
        assertEquals(capitalService.getTotalApprovedDrawings(), lf.getGlobalCapitalDrawings());
        assertEquals(capitalService.getNetCapitalPool(), lf.getGlobalNetCapital());
        assertEquals(loanService.getTotalApprovedBorrowed(), lf.getGlobalLoansBorrowed());
        assertEquals(loanService.getTotalApprovedRepaid(), lf.getGlobalLoansRepaid());
        assertEquals(loanService.getOutstandingLoanPool(), lf.getGlobalOutstandingLoans());
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    @DisplayName("Verify end-to-end Excel download HTTP 200, tab presence, and cell numeric accuracy")
    void testEndToEndExcelDownload() throws Exception {
        MvcResult result = mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2025")
                        .param("month", "10")
                        .param("format", "XLSX"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("MushroomFarm_MONTHLY_")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 2000);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertEquals(5, wb.getNumberOfSheets());
            assertEquals("Lifetime Financials", wb.getSheetName(0));
            assertEquals("Expenses", wb.getSheetName(1));
            assertEquals("Income", wb.getSheetName(2));
            assertEquals("Capital", wb.getSheetName(3));
            assertEquals("Loans", wb.getSheetName(4));

            XSSFSheet expensesSheet = wb.getSheetAt(1);
            assertNotNull(expensesSheet);
            // Verify freeze pane exists
            assertNotNull(expensesSheet.getPaneInformation());
            assertTrue(expensesSheet.getPaneInformation().isFreezePane());

            // Verify Approved Transaction Count has no rupee symbol
            org.apache.poi.ss.usermodel.Row countRow = expensesSheet.getRow(4);
            assertNotNull(countRow);
            assertEquals("Approved Transaction Count", countRow.getCell(0).getStringCellValue());
            assertFalse(countRow.getCell(1).getCellStyle().getDataFormatString().contains("₹"),
                    "Approved Transaction Count format must not contain currency symbol ₹");
        }
    }

    @Test
    @WithMockUser(username = "accountant", roles = "ACCOUNTANT")
    @DisplayName("Verify end-to-end CSV zip download contains all 5 files with valid content")
    void testEndToEndCsvZipDownload() throws Exception {
        MvcResult result = mockMvc.perform(get("/reports/download")
                        .param("type", "MONTHLY")
                        .param("year", "2025")
                        .param("month", "10")
                        .param("format", "CSV"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("MushroomFarm_MONTHLY_")))
                .andExpect(content().contentType("application/zip"))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 500);

        int zipFileCount = 0;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(content), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                zipFileCount++;
                byte[] entryBytes = zis.readAllBytes();
                String text = new String(entryBytes, StandardCharsets.UTF_8);
                assertTrue(text.startsWith("\uFEFF"), "File " + entry.getName() + " must have UTF-8 BOM");
                zis.closeEntry();
            }
        }

        assertEquals(5, zipFileCount, "CSV zip archive must contain all 5 CSV files");
    }
}
