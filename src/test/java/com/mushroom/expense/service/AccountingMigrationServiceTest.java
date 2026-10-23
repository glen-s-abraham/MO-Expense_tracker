package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountingMigrationServiceTest {

    @Autowired
    private CapitalService capitalService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AccountingMigrationService accountingMigrationService;

    @Test
    void verifyMigratedBalancesAndIdempotency() {
        // After startup migration:
        // 7 Capital Introduced records totaling ₹1,90,000.00
        assertEquals(190000.0, capitalService.getTotalApprovedIntroduced(), 0.01);
        assertEquals(0.0, capitalService.getTotalApprovedDrawings(), 0.01);
        assertEquals(190000.0, capitalService.getNetCapitalPool(), 0.01);

        // 1 Loan facility ("Unsecured Loan From Father") totaling ₹1,70,000.00
        assertEquals(170000.0, loanService.getTotalApprovedBorrowed(), 0.01);
        assertEquals(0.0, loanService.getTotalApprovedRepaid(), 0.01);
        assertEquals(170000.0, loanService.getOutstandingLoanPool(), 0.01);

        List<LoanService.LoanSummary> summaries = loanService.getAllLoanSummaries();
        assertEquals(1, summaries.size());
        assertEquals("Unsecured Loan From Father", summaries.get(0).loanName());
        assertEquals("Father", summaries.get(0).lenderName());
        assertEquals(170000.0, summaries.get(0).outstandingBalance(), 0.01);

        // Operational Income remaining in incomes: ₹31,812.00 across 25 sales records
        double operationalIncomeSum = incomeRepository.findByStatus(IncomeStatus.APPROVED)
                .stream()
                .mapToDouble(Income::getAmount)
                .sum();
        assertEquals(31812.0, operationalIncomeSum, 0.01);

        // Legacy categories removed, new Property Sales category added
        assertTrue(categoryRepository.findByNameIgnoreCase("Capital Introduced").isEmpty());
        assertTrue(categoryRepository.findByNameIgnoreCase("Loan").isEmpty());
        assertTrue(categoryRepository.findByNameIgnoreCase("Income From Property Sales").isPresent());

        // Calling migration again is idempotent (0 records migrated)
        AccountingMigrationService.MigrationSummary secondRun = accountingMigrationService.migrateLegacyCapitalAndLoans();
        assertEquals(0, secondRun.migratedCapitalCount());
        assertEquals(0, secondRun.migratedLoanCount());
    }
}
