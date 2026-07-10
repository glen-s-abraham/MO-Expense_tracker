package com.mushroom.expense.repository;

import com.mushroom.expense.entity.Expense;
import com.mushroom.expense.entity.ExpenseStatus;
import com.mushroom.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {
    List<Expense> findByUser(User user);

    Page<Expense> findByUser(User user, Pageable pageable);

    Page<Expense> findByUserAndStatus(User user, ExpenseStatus status, Pageable pageable);

    Page<Expense> findByUserAndStatusIn(User user, List<ExpenseStatus> statuses, Pageable pageable);

    List<Expense> findByStatus(ExpenseStatus status);

    Page<Expense> findByStatus(ExpenseStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.status = :status GROUP BY e.category.name")
    List<Object[]> findSumByCategory(@org.springframework.data.repository.query.Param("status") ExpenseStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.status = :status AND e.date >= :startDate AND e.date <= :endDate GROUP BY e.category.name")
    List<Object[]> findSumByCategoryAndDateBetween(@org.springframework.data.repository.query.Param("status") ExpenseStatus status, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);

    @org.springframework.data.jpa.repository.Query("SELECT e.date, SUM(e.amount) FROM Expense e WHERE e.status = :status AND e.date >= :startDate AND e.date <= :endDate GROUP BY e.date ORDER BY e.date")
    List<Object[]> findDailySumByDateBetween(@org.springframework.data.repository.query.Param("status") ExpenseStatus status, @org.springframework.data.repository.query.Param("startDate") java.time.LocalDate startDate, @org.springframework.data.repository.query.Param("endDate") java.time.LocalDate endDate);
}
