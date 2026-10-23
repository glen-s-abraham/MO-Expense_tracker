package com.mushroom.expense.repository;

import com.mushroom.expense.entity.Loan;
import com.mushroom.expense.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByLoanNameIgnoreCase(String loanName);
    List<Loan> findByStatusOrderByIdAsc(LoanStatus status);
}
