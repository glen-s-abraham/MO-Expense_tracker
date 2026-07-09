package com.mushroom.expense.repository;

import com.mushroom.expense.entity.IncomeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncomeCommentRepository extends JpaRepository<IncomeComment, Long> {
    List<IncomeComment> findByIncomeId(Long incomeId);
}
