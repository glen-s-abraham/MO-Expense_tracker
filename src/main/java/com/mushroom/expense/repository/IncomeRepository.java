package com.mushroom.expense.repository;

import com.mushroom.expense.entity.Income;
import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncomeRepository extends JpaRepository<Income, Long>, JpaSpecificationExecutor<Income> {
    List<Income> findByUser(User user);

    Page<Income> findByUser(User user, Pageable pageable);

    Page<Income> findByUserAndStatus(User user, IncomeStatus status, Pageable pageable);

    Page<Income> findByUserAndStatusIn(User user, List<IncomeStatus> statuses, Pageable pageable);

    List<Income> findByStatus(IncomeStatus status);

    Page<Income> findByStatus(IncomeStatus status, Pageable pageable);
}
