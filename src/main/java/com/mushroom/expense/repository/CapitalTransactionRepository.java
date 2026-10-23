package com.mushroom.expense.repository;

import com.mushroom.expense.entity.CapitalTransaction;
import com.mushroom.expense.entity.CapitalTransactionType;
import com.mushroom.expense.entity.IncomeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CapitalTransactionRepository extends JpaRepository<CapitalTransaction, Long>, JpaSpecificationExecutor<CapitalTransaction> {

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM CapitalTransaction c WHERE c.type = :type AND c.status = :status")
    Double sumAmountByTypeAndStatus(@Param("type") CapitalTransactionType type, @Param("status") IncomeStatus status);

    @Query("SELECT COALESCE(SUM(c.amount), 0.0) FROM CapitalTransaction c WHERE c.type = :type AND c.status = :status AND c.date >= :startDate AND c.date <= :endDate")
    Double sumAmountByTypeAndStatusAndDateBetween(
            @Param("type") CapitalTransactionType type,
            @Param("status") IncomeStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT c.date, SUM(c.amount) FROM CapitalTransaction c WHERE c.type = :type AND c.status = :status AND c.date >= :startDate AND c.date <= :endDate GROUP BY c.date ORDER BY c.date")
    List<Object[]> findDailySumByTypeAndDateBetween(
            @Param("type") CapitalTransactionType type,
            @Param("status") IncomeStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<CapitalTransaction> findByStatusOrderByDateDesc(IncomeStatus status);

    List<CapitalTransaction> findByTypeAndStatusAndDateBetweenOrderByDateDesc(
            CapitalTransactionType type, IncomeStatus status, LocalDate startDate, LocalDate endDate);
}
