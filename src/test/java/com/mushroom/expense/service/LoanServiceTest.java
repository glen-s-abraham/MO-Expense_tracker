package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void repaymentReducesLoanBalanceAndPreventsOverRepayment() throws IOException {
        User admin = userRepository.findByUsername("admin").orElseThrow();
        Loan fatherLoan = loanService.findAllLoans().get(0);
        assertEquals(170000.0, loanService.getLoanOutstandingBalance(fatherLoan.getId()), 0.01);

        // 1. Attempt over-repayment of ₹2,00,000 against ₹1,70,000 outstanding -> blocked!
        LoanTransaction overRepay = new LoanTransaction();
        overRepay.setLoan(fatherLoan);
        overRepay.setType(LoanTransactionType.REPAYMENT);
        overRepay.setDescription("Excess repayment");
        overRepay.setAmount(200000.0);
        overRepay.setDate(LocalDate.now());
        overRepay.setPaymentMode(PaymentMode.BANK_TRANSFER);
        overRepay.setUser(admin);

        assertThrows(IllegalArgumentException.class,
                () -> loanService.saveLoanTransaction(overRepay, List.of(), null, false));

        // 2. Valid partial repayment of ₹50,000
        LoanTransaction partialRepay = new LoanTransaction();
        partialRepay.setLoan(fatherLoan);
        partialRepay.setType(LoanTransactionType.REPAYMENT);
        partialRepay.setDescription("First installment repayment");
        partialRepay.setAmount(50000.0);
        partialRepay.setDate(LocalDate.now());
        partialRepay.setPaymentMode(PaymentMode.BANK_TRANSFER);
        partialRepay.setUser(admin);

        LoanTransaction savedPartial = loanService.saveLoanTransaction(partialRepay, List.of(), null, false);
        loanService.updateStatus(savedPartial.getId(), IncomeStatus.APPROVED);

        assertEquals(120000.0, loanService.getLoanOutstandingBalance(fatherLoan.getId()), 0.01);
        assertEquals(LoanStatus.ACTIVE, loanService.findLoanById(fatherLoan.getId()).orElseThrow().getStatus());

        // 3. Repay remaining ₹1,20,000 -> loan automatically transitions to CLOSED
        LoanTransaction finalRepay = new LoanTransaction();
        finalRepay.setLoan(fatherLoan);
        finalRepay.setType(LoanTransactionType.REPAYMENT);
        finalRepay.setDescription("Full settlement");
        finalRepay.setAmount(120000.0);
        finalRepay.setDate(LocalDate.now());
        finalRepay.setPaymentMode(PaymentMode.BANK_TRANSFER);
        finalRepay.setUser(admin);

        LoanTransaction savedFinal = loanService.saveLoanTransaction(finalRepay, List.of(), null, false);
        loanService.updateStatus(savedFinal.getId(), IncomeStatus.APPROVED);

        assertEquals(0.0, loanService.getLoanOutstandingBalance(fatherLoan.getId()), 0.01);
        assertEquals(LoanStatus.CLOSED, loanService.findLoanById(fatherLoan.getId()).orElseThrow().getStatus());
    }
}
