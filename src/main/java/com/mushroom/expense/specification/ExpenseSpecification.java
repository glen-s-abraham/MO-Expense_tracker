package com.mushroom.expense.specification;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ExpenseSpecification {

    public static Specification<Expense> filterExpenses(User user, List<ExpenseStatus> statuses, String keyword,
            LocalDate startDate, LocalDate endDate, Long categoryId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by User
            if (user != null) {
                predicates.add(criteriaBuilder.equal(root.get("user"), user));
            }

            // Filter by Status(es)
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            // Filter by Date Range
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }

            // Filter by Category
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            // Filter by Keyword (Search)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                        likePattern);
                Predicate batchIdLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("batchId")), likePattern);
                // Join for category/subcategory names if needed, but simple fields first
                // To search category name, we need to access the joined path
                Predicate categoryNameLike = criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("category").get("name")), likePattern);
                Predicate subCategoryNameLike = criteriaBuilder
                        .like(criteriaBuilder.lower(root.get("subCategory").get("name")), likePattern);

                predicates.add(criteriaBuilder.or(descriptionLike, batchIdLike, categoryNameLike, subCategoryNameLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
