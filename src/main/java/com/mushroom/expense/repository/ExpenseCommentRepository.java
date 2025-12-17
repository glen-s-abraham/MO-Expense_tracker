package com.mushroom.expense.repository;

import com.mushroom.expense.entity.ExpenseComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExpenseCommentRepository extends JpaRepository<ExpenseComment, Long> {
    List<ExpenseComment> findByExpenseId(Long expenseId);
}
