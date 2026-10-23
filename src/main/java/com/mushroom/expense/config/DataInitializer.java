package com.mushroom.expense.config;

import com.mushroom.expense.entity.Category;
import com.mushroom.expense.entity.SubCategory;
import com.mushroom.expense.entity.TransactionType;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.repository.CategoryRepository;
import com.mushroom.expense.repository.SubCategoryRepository;
import com.mushroom.expense.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEFAULT_SEED_LOCATION = "classpath:seed/prod-seed.sql";

    private static final List<String> TRUNCATE_ORDER = List.of(
            "loan_comments",
            "loan_attachments",
            "loan_transactions",
            "loans",
            "capital_comments",
            "capital_attachments",
            "capital_transactions",
            "income_comments",
            "income_attachments",
            "incomes",
            "expense_comments",
            "expense_attachments",
            "expenses",
            "sub_categories",
            "categories",
            "users"
    );

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            com.mushroom.expense.service.AccountingMigrationService accountingMigrationService,
            PasswordEncoder passwordEncoder,
            Environment env,
            DataSource dataSource,
            ResourceLoader resourceLoader) {
        return args -> {
            String[] activeProfiles = env.getActiveProfiles();
            boolean isProd = Arrays.asList(activeProfiles).contains("prod");

            if (isProd) {
                // Prod Mode: Only create admin if not exists
                if (userRepository.findByUsername("admin").isEmpty()) {
                    userRepository.save(new User("admin", passwordEncoder.encode("password"), "ROLE_ADMIN"));
                    log.info("Temporary admin user created for production.");
                }
                accountingMigrationService.migrateLegacyCapitalAndLoans();
                return;
            }

            boolean seedEnabled = env.getProperty("app.seed.enabled", Boolean.class, true);
            if (!seedEnabled) {
                log.info("Database seeding is disabled via app.seed.enabled=false.");
                return;
            }

            boolean forceReset = env.getProperty("app.seed.force-reset", Boolean.class, false);
            String seedLocation = env.getProperty("app.seed.location", DEFAULT_SEED_LOCATION);

            if (forceReset) {
                log.info("Force-reset enabled (app.seed.force-reset=true). Clearing local H2 tables before seeding...");
                clearAllTables(dataSource);
            }

            boolean isDatabaseEmpty = userRepository.count() == 0 && categoryRepository.count() == 0;
            if (!isDatabaseEmpty) {
                ensureDefaultDevUsers(userRepository, passwordEncoder);
                accountingMigrationService.migrateLegacyCapitalAndLoans();
                log.info("Local H2 database already contains data (users={}, categories={}). Skipping seed load.",
                        userRepository.count(), categoryRepository.count());
                return;
            }

            Resource seedResource = resourceLoader.getResource(seedLocation);
            if (seedResource.exists()) {
                log.info("Loading production seed dump into H2 from {}...", seedLocation);
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                        false, false, StandardCharsets.UTF_8.name(), seedResource);
                populator.execute(dataSource);

                ensureDefaultDevUsers(userRepository, passwordEncoder);
                accountingMigrationService.migrateLegacyCapitalAndLoans();
                log.info("H2 seed load complete (users={}, categories={}, subCategories={}).",
                        userRepository.count(), categoryRepository.count(), subCategoryRepository.count());
            } else {
                log.warn("Seed SQL file not found at {}. Falling back to minimal programmatic sample data.",
                        seedLocation);
                loadFallbackSampleData(userRepository, categoryRepository, subCategoryRepository, passwordEncoder);
                accountingMigrationService.migrateLegacyCapitalAndLoans();
            }
        };
    }

    private void clearAllTables(DataSource dataSource) throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String table : TRUNCATE_ORDER) {
                stmt.executeUpdate("DELETE FROM " + table);
            }
        }
    }

    private void ensureDefaultDevUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        ensureUserIfMissing(userRepository, passwordEncoder, "admin", "ROLE_ADMIN");
        ensureUserIfMissing(userRepository, passwordEncoder, "manager", "ROLE_MANAGER");
        ensureUserIfMissing(userRepository, passwordEncoder, "accountant", "ROLE_ACCOUNTANT");
        ensureUserIfMissing(userRepository, passwordEncoder, "supervisor", "ROLE_SUPERVISOR");
    }

    private void ensureUserIfMissing(UserRepository userRepository, PasswordEncoder passwordEncoder,
            String username, String role) {
        userRepository.findByUsername(username).ifPresentOrElse(existing -> {
            existing.setPassword(passwordEncoder.encode("password"));
            userRepository.save(existing);
        }, () -> userRepository.save(new User(username, passwordEncoder.encode("password"), role)));
    }

    private void loadFallbackSampleData(UserRepository userRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            PasswordEncoder passwordEncoder) {
        if (userRepository.count() == 0) {
            ensureDefaultDevUsers(userRepository, passwordEncoder);
        }

        if (categoryRepository.count() == 0) {
            Category rawMaterials = new Category();
            rawMaterials.setName("Raw Materials");
            rawMaterials.setType(TransactionType.EXPENSE);
            categoryRepository.save(rawMaterials);

            Category utilities = new Category();
            utilities.setName("Utilities");
            utilities.setType(TransactionType.EXPENSE);
            categoryRepository.save(utilities);

            Category capital = new Category();
            capital.setName("Capital Introduced");
            capital.setType(TransactionType.INCOME);
            categoryRepository.save(capital);

            Category sales = new Category();
            sales.setName("Sales");
            sales.setType(TransactionType.INCOME);
            categoryRepository.save(sales);

            Category loan = new Category();
            loan.setName("Loan");
            loan.setType(TransactionType.INCOME);
            categoryRepository.save(loan);

            Category others = new Category();
            others.setName("Others (Income)");
            others.setType(TransactionType.INCOME);
            categoryRepository.save(others);

            SubCategory seeds = new SubCategory();
            seeds.setName("Seeds / Spores");
            seeds.setCategory(rawMaterials);
            subCategoryRepository.save(seeds);

            SubCategory compost = new SubCategory();
            compost.setName("Compost");
            compost.setCategory(rawMaterials);
            subCategoryRepository.save(compost);

            SubCategory electricity = new SubCategory();
            electricity.setName("Electricity");
            electricity.setCategory(utilities);
            subCategoryRepository.save(electricity);
        }
    }
}
