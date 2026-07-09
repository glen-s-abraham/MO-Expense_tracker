package com.mushroom.expense.config;

import com.mushroom.expense.entity.Category;
import com.mushroom.expense.entity.SubCategory;
import com.mushroom.expense.entity.User;
import com.mushroom.expense.repository.CategoryRepository;
import com.mushroom.expense.repository.SubCategoryRepository;
import com.mushroom.expense.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.core.env.Environment env) {
        return args -> {
            String[] activeProfiles = env.getActiveProfiles();
            boolean isProd = java.util.Arrays.asList(activeProfiles).contains("prod");

            if (isProd) {
                // Prod Mode: Only create admin if not exists
                if (userRepository.findByUsername("admin").isEmpty()) {
                    userRepository.save(new User("admin", passwordEncoder.encode("password"), "ROLE_ADMIN"));
                    System.out.println("Temporary admin user created for production.");
                }
            } else {
                // Dev Mode: Load all sample data
                // Create Users
                if (userRepository.count() == 0) {
                    userRepository.save(new User("admin", passwordEncoder.encode("password"), "ROLE_ADMIN"));
                    userRepository.save(new User("manager", passwordEncoder.encode("password"), "ROLE_MANAGER"));
                    userRepository.save(new User("accountant", passwordEncoder.encode("password"), "ROLE_ACCOUNTANT"));
                    userRepository.save(new User("supervisor", passwordEncoder.encode("password"), "ROLE_SUPERVISOR"));
                }

                // Create Categories
                if (categoryRepository.count() == 0) {
                    Category rawMaterials = new Category();
                    rawMaterials.setName("Raw Materials");
                    rawMaterials.setType(com.mushroom.expense.entity.TransactionType.EXPENSE);
                    categoryRepository.save(rawMaterials);

                    Category utilities = new Category();
                    utilities.setName("Utilities");
                    utilities.setType(com.mushroom.expense.entity.TransactionType.EXPENSE);
                    categoryRepository.save(utilities);

                    Category capital = new Category();
                    capital.setName("Capital Introduced");
                    capital.setType(com.mushroom.expense.entity.TransactionType.INCOME);
                    categoryRepository.save(capital);

                    Category sales = new Category();
                    sales.setName("Sales");
                    sales.setType(com.mushroom.expense.entity.TransactionType.INCOME);
                    categoryRepository.save(sales);

                    Category loan = new Category();
                    loan.setName("Loan");
                    loan.setType(com.mushroom.expense.entity.TransactionType.INCOME);
                    categoryRepository.save(loan);

                    Category others = new Category();
                    others.setName("Others (Income)");
                    others.setType(com.mushroom.expense.entity.TransactionType.INCOME);
                    categoryRepository.save(others);

                    // Create SubCategories
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
        };
    }
}
