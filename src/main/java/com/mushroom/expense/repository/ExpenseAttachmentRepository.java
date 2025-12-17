package com.mushroom.expense.repository;

import com.mushroom.expense.entity.ExpenseAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseAttachmentRepository extends JpaRepository<ExpenseAttachment, Long> {
}
