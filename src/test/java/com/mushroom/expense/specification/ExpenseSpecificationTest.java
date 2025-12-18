package com.mushroom.expense.specification;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ExpenseSpecificationTest {

    @Test
    void filterExpenses_AllParams() {
        User user = new User();
        List<ExpenseStatus> statuses = List.of(ExpenseStatus.DRAFT);
        String keyword = "test";
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now();
        Long categoryId = 1L;

        Specification<Expense> spec = ExpenseSpecification.filterExpenses(user, statuses, keyword, startDate, endDate,
                categoryId);
        assertNotNull(spec);

        Root<Expense> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        // Mock path traversals
        Path userPath = mock(Path.class);
        when(root.get("user")).thenReturn(userPath);

        Path statusPath = mock(Path.class);
        when(root.get("status")).thenReturn(statusPath);

        Path descPath = mock(Path.class);
        when(root.get("description")).thenReturn(descPath);

        Path datePath = mock(Path.class);
        when(root.get("date")).thenReturn(datePath);

        Path catPath = mock(Path.class);
        Path catIdPath = mock(Path.class);
        Path catNamePath = mock(Path.class);
        when(root.get("category")).thenReturn(catPath);
        when(catPath.get("id")).thenReturn(catIdPath);
        when(catPath.get("name")).thenReturn(catNamePath);

        Path batchPath = mock(Path.class);
        when(root.get("batchId")).thenReturn(batchPath);

        Path subCatPath = mock(Path.class);
        Path subCatNamePath = mock(Path.class);
        when(root.get("subCategory")).thenReturn(subCatPath);
        when(subCatPath.get("name")).thenReturn(subCatNamePath);

        // Execute
        spec.toPredicate(root, query, cb);
    }

    @Test
    void filterExpenses_NullParams() {
        Specification<Expense> spec = ExpenseSpecification.filterExpenses(null, null, null, null, null, null);
        assertNotNull(spec);

        Root<Expense> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        spec.toPredicate(root, query, cb);
    }
}
