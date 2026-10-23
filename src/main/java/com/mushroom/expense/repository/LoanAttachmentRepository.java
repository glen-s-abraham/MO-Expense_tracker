package com.mushroom.expense.repository;

import com.mushroom.expense.entity.LoanAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanAttachmentRepository extends JpaRepository<LoanAttachment, Long> {
}
