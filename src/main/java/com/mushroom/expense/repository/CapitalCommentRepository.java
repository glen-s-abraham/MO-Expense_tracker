package com.mushroom.expense.repository;

import com.mushroom.expense.entity.CapitalComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CapitalCommentRepository extends JpaRepository<CapitalComment, Long> {
    List<CapitalComment> findByCapitalTransactionId(Long capitalTransactionId);
}
