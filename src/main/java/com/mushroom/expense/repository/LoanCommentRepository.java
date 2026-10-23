package com.mushroom.expense.repository;

import com.mushroom.expense.entity.LoanComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanCommentRepository extends JpaRepository<LoanComment, Long> {
    List<LoanComment> findByLoanTransactionId(Long loanTransactionId);
}
