package com.mushroom.expense.config;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import com.mushroom.expense.service.AccountingMigrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CapitalTransactionRepository capitalTransactionRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanTransactionRepository loanTransactionRepository;

    @Autowired
    private ExpenseAttachmentRepository expenseAttachmentRepository;

    @Autowired
    private ExpenseCommentRepository expenseCommentRepository;

    @Autowired
    private IncomeCommentRepository incomeCommentRepository;

    @Autowired
    private CapitalCommentRepository capitalCommentRepository;

    @Autowired
    private LoanCommentRepository loanCommentRepository;

    @Autowired
    private AccountingMigrationService accountingMigrationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void initData_LoadsProductionSeedDataAndMigratesCapitalAndLoans() throws Exception {
        // Verify initial load from application startup seeded the production dump and ran 4-pillar migration
        assertTrue(categoryRepository.count() >= 13, "Should have 13 categories after migrating legacy Capital/Loan and adding Property Sales");
        assertTrue(subCategoryRepository.count() >= 51, "Should have at least 51 sub-categories including seeded income subcategories");
        assertTrue(expenseRepository.count() >= 107, "Should load all 107 production expenses");
        assertEquals(25L, incomeRepository.count(), "Should retain 25 operational sales incomes after migrating 7 Capital + 1 Loan");
        assertEquals(7L, capitalTransactionRepository.count(), "Should migrate 7 Capital Introduced records");
        assertEquals(1L, loanRepository.count(), "Should create 1 individual Loan facility (Unsecured Loan From Father)");
        assertEquals(1L, loanTransactionRepository.count(), "Should migrate 1 Loan Borrowed transaction");
        assertTrue(expenseAttachmentRepository.count() >= 39, "Should load all 39 expense attachments");
        assertTrue(expenseCommentRepository.count() >= 9, "Should load all 9 expense comments");
        assertEquals(1L, incomeCommentRepository.count(), "1 operational income comment remains in income_comments");
        assertEquals(1L, capitalCommentRepository.count(), "1 capital comment migrated to capital_comments");
        assertEquals(1L, loanCommentRepository.count(), "1 loan comment migrated to loan_comments");

        // Verify both production users and fallback dev users exist
        assertTrue(userRepository.findByUsername("admin").isPresent());
        assertTrue(userRepository.findByUsername("glen-man").isPresent());
        assertTrue(userRepository.findByUsername("liya").isPresent());
        assertTrue(userRepository.findByUsername("manager").isPresent());
        assertTrue(userRepository.findByUsername("accountant").isPresent());
        assertTrue(userRepository.findByUsername("supervisor").isPresent());

        // Verify H2 identity sequences were properly advanced by inserting new entities
        Category newCat = new Category();
        newCat.setName("Post-Seed Test Category");
        newCat.setType(TransactionType.EXPENSE);
        Category savedCat = categoryRepository.save(newCat);
        assertNotNull(savedCat.getId());
        assertTrue(savedCat.getId() >= 15L, "New category ID should not collide with seeded IDs");

        SubCategory newSub = new SubCategory();
        newSub.setName("Post-Seed Test SubCategory");
        newSub.setCategory(savedCat);
        SubCategory savedSub = subCategoryRepository.save(newSub);
        assertNotNull(savedSub.getId());
        assertTrue(savedSub.getId() >= 45L, "New sub-category ID should not collide with seeded IDs");

        User creator = userRepository.findByUsername("admin").orElseThrow();
        Expense newExpense = new Expense();
        newExpense.setDescription("Post-Seed Expense");
        newExpense.setAmount(250.0);
        newExpense.setDate(LocalDate.now());
        newExpense.setPaymentMode(PaymentMode.CASH);
        newExpense.setStatus(ExpenseStatus.DRAFT);
        newExpense.setUser(creator);
        newExpense.setCategory(savedCat);
        newExpense.setSubCategory(savedSub);
        Expense savedExpense = expenseRepository.save(newExpense);
        assertNotNull(savedExpense.getId());
        assertTrue(savedExpense.getId() >= 112L, "New expense ID should not collide with seeded IDs");
    }

    @Test
    void initData_IdempotentOnSubsequentStartupWithoutForceReset() throws Exception {
        long categoriesBefore = categoryRepository.count();
        long expensesBefore = expenseRepository.count();
        long capitalsBefore = capitalTransactionRepository.count();
        long loansBefore = loanTransactionRepository.count();

        DataInitializer initializer = new DataInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("app.seed.enabled", "true");
        env.setProperty("app.seed.force-reset", "false");

        CommandLineRunner runner = initializer.initData(
                userRepository,
                categoryRepository,
                subCategoryRepository,
                accountingMigrationService,
                passwordEncoder,
                env,
                dataSource,
                new DefaultResourceLoader()
        );
        runner.run();

        assertEquals(categoriesBefore, categoryRepository.count(), "Should not duplicate categories on restart");
        assertEquals(expensesBefore, expenseRepository.count(), "Should not duplicate expenses on restart");
        assertEquals(capitalsBefore, capitalTransactionRepository.count(), "Should not duplicate capital transactions on restart");
        assertEquals(loansBefore, loanTransactionRepository.count(), "Should not duplicate loan transactions on restart");
    }

    @Test
    void initData_ForceResetReloadsCleanProductionSeedAndReMigrates() throws Exception {
        DataInitializer initializer = new DataInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("app.seed.enabled", "true");
        env.setProperty("app.seed.force-reset", "true");
        env.setProperty("app.seed.location", "classpath:seed/prod-seed.sql");

        CommandLineRunner runner = initializer.initData(
                userRepository,
                categoryRepository,
                subCategoryRepository,
                accountingMigrationService,
                passwordEncoder,
                env,
                dataSource,
                new DefaultResourceLoader()
        );
        runner.run();

        assertEquals(13L, categoryRepository.count(), "Force reset + migration should result in 13 categories");
        assertEquals(51L, subCategoryRepository.count(), "Force reset + migration should result in 51 subcategories");
        assertEquals(107L, expenseRepository.count(), "Force reset should restore exact 107 production expenses");
        assertEquals(25L, incomeRepository.count(), "Force reset + migration should leave 25 operational incomes");
        assertEquals(7L, capitalTransactionRepository.count(), "Force reset + migration should produce 7 capital records");
        assertEquals(1L, loanTransactionRepository.count(), "Force reset + migration should produce 1 loan transaction");
    }

    @Test
    void initData_FallbackWhenSeedScriptMissing() throws Exception {
        DataInitializer initializer = new DataInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("dev");
        env.setProperty("app.seed.enabled", "true");
        env.setProperty("app.seed.force-reset", "true");
        env.setProperty("app.seed.location", "classpath:seed/non-existent-seed.sql");

        CommandLineRunner runner = initializer.initData(
                userRepository,
                categoryRepository,
                subCategoryRepository,
                accountingMigrationService,
                passwordEncoder,
                env,
                dataSource,
                new DefaultResourceLoader()
        );
        runner.run();

        assertEquals(7L, categoryRepository.count(), "Fallback + migration should create 7 operational categories");
        assertEquals(4L, userRepository.count(), "Fallback should create 4 default users");

        // Restore production seed after test
        env.setProperty("app.seed.location", "classpath:seed/prod-seed.sql");
        runner.run();
    }

    @Test
    void initData_ProdProfileDoesNotRunH2Seed() throws Exception {
        long expensesBefore = expenseRepository.count();

        DataInitializer initializer = new DataInitializer();
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("prod");

        CommandLineRunner runner = initializer.initData(
                userRepository,
                categoryRepository,
                subCategoryRepository,
                accountingMigrationService,
                passwordEncoder,
                env,
                dataSource,
                new DefaultResourceLoader()
        );
        runner.run();

        assertEquals(expensesBefore, expenseRepository.count());
        assertTrue(userRepository.findByUsername("admin").isPresent());
    }
}
