package com.mushroom.expense.repository;

import com.mushroom.expense.entity.IncomeStatus;
import com.mushroom.expense.entity.LoanTransaction;
import com.mushroom.expense.entity.LoanTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long>, JpaSpecificationExecutor<LoanTransaction> {

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM LoanTransaction t WHERE t.loan.id = :loanId AND t.type = :type AND t.status = :status")
    Double sumAmountByLoanIdAndTypeAndStatus(
            @Param("loanId") Long loanId,
            @Param("type") LoanTransactionType type,
            @Param("status") IncomeStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM LoanTransaction t WHERE t.type = :type AND t.status = :status")
    Double sumAmountByTypeAndStatus(@Param("type") LoanTransactionType type, @Param("status") IncomeStatus status);

    @Query("SELECT COALESCE(SUM(t.amount), 0.0) FROM LoanTransaction t WHERE t.type = :type AND t.status = :status AND t.date >= :startDate AND t.date <= :endDate")
    Double sumAmountByTypeAndStatusAndDateBetween(
            @Param("type") LoanTransactionType type,
            @Param("status") IncomeStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT t.date, SUM(t.amount) FROM LoanTransaction t WHERE t.type = :type AND t.status = :status AND t.date >= :startDate AND t.date <= :endDate GROUP BY t.date ORDER BY t.date")
    List<Object[]> findDailySumByTypeAndDateBetween(
            @Param("type") LoanTransactionType type,
            @Param("status") IncomeStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<LoanTransaction> findByLoanIdOrderByDateDesc(Long loanId);

    List<LoanTransaction> findByTypeAndStatusAndDateBetweenOrderByDateDesc(
            LoanTransactionType type, IncomeStatus status, LocalDate startDate, LocalDate endDate);
}
