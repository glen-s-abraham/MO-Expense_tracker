package com.mushroom.expense.specification;

import com.mushroom.expense.entity.CapitalTransaction;
import com.mushroom.expense.entity.CapitalTransactionType;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CapitalSpecification {

    public static Specification<CapitalTransaction> filterCapitalTransactions(
            User user,
            CapitalTransactionType type,
            List<IncomeStatus> statuses,
            String keyword,
            LocalDate startDate,
            LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (user != null) {
                predicates.add(criteriaBuilder.equal(root.get("user"), user));
            }
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), endDate));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.toLowerCase() + "%";
                Predicate descriptionLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern);
                Predicate ownerLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("ownerName")), likePattern);
                Predicate batchLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("batchId")), likePattern);
                predicates.add(criteriaBuilder.or(descriptionLike, ownerLike, batchLike));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
