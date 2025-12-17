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
            PasswordEncoder passwordEncoder) {
        return args -> {
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
                categoryRepository.save(rawMaterials);

                Category utilities = new Category();
                utilities.setName("Utilities");
                categoryRepository.save(utilities);

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
        };
    }
}
