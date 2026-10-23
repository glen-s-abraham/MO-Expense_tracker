package com.mushroom.expense.service;

import com.mushroom.expense.entity.*;
import com.mushroom.expense.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AccountingMigrationService {

    private static final Logger log = LoggerFactory.getLogger(AccountingMigrationService.class);

    private final IncomeRepository incomeRepository;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CapitalTransactionRepository capitalTransactionRepository;
    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;

    public AccountingMigrationService(
            IncomeRepository incomeRepository,
            CategoryRepository categoryRepository,
            SubCategoryRepository subCategoryRepository,
            CapitalTransactionRepository capitalTransactionRepository,
            LoanRepository loanRepository,
            LoanTransactionRepository loanTransactionRepository) {
        this.incomeRepository = incomeRepository;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.capitalTransactionRepository = capitalTransactionRepository;
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
    }

    public record MigrationSummary(
            int migratedCapitalCount,
            double migratedCapitalTotal,
            int migratedLoanCount,
            double migratedLoanTotal) {
    }

    @Transactional
    public MigrationSummary migrateLegacyCapitalAndLoans() {
        List<Income> capitalIncomes = incomeRepository.findByCategoryNameIgnoreCaseOrderByDateAscIdAsc("Capital Introduced");
        List<Income> loanIncomes = incomeRepository.findByCategoryNameIgnoreCaseOrderByDateAscIdAsc("Loan");

        double expectedCapitalSum = capitalIncomes.stream().mapToDouble(Income::getAmount).sum();
        double expectedLoanSum = loanIncomes.stream().mapToDouble(Income::getAmount).sum();
        double expectedTotal = expectedCapitalSum + expectedLoanSum;

        double actualCapitalSum = 0.0;
        double actualLoanSum = 0.0;

        for (Income income : capitalIncomes) {
            CapitalTransaction cap = new CapitalTransaction();
            cap.setType(CapitalTransactionType.INTRODUCED);
            cap.setDescription(income.getDescription());
            cap.setOwnerName(income.getDescription());
            cap.setAmount(income.getAmount());
            cap.setDate(income.getDate());
            cap.setPaymentMode(income.getPaymentMode());
            cap.setBatchId(income.getBatchId());
            cap.setReceiptImage(income.getReceiptImage());
            cap.setStatus(income.getStatus());
            cap.setUser(income.getUser());

            if (income.getAttachments() != null) {
                for (IncomeAttachment att : income.getAttachments()) {
                    cap.getAttachments().add(new CapitalAttachment(att.getFileName(), cap));
                }
            }
            if (income.getComments() != null) {
                for (IncomeComment comment : income.getComments()) {
                    CapitalComment cc = new CapitalComment(cap, comment.getUser(), comment.getMessage());
                    cc.setTimestamp(comment.getTimestamp());
                    cap.getComments().add(cc);
                }
            }

            capitalTransactionRepository.save(cap);
            actualCapitalSum += cap.getAmount();
        }

        for (Income income : loanIncomes) {
            String rawDesc = income.getDescription() != null && !income.getDescription().isBlank()
                    ? income.getDescription().trim()
                    : "General Loan Facility";
            String lenderName = extractLenderName(rawDesc);

            Loan loan = loanRepository.findByLoanNameIgnoreCase(rawDesc)
                    .orElseGet(() -> loanRepository.save(new Loan(rawDesc, lenderName, rawDesc, income.getDate())));

            LoanTransaction lt = new LoanTransaction();
            lt.setLoan(loan);
            lt.setType(LoanTransactionType.BORROWED);
            lt.setDescription(income.getDescription());
            lt.setAmount(income.getAmount());
            lt.setDate(income.getDate());
            lt.setPaymentMode(income.getPaymentMode());
            lt.setBatchId(income.getBatchId());
            lt.setReceiptImage(income.getReceiptImage());
            lt.setStatus(income.getStatus());
            lt.setUser(income.getUser());

            if (income.getAttachments() != null) {
                for (IncomeAttachment att : income.getAttachments()) {
                    lt.getAttachments().add(new LoanAttachment(att.getFileName(), lt));
                }
            }
            if (income.getComments() != null) {
                for (IncomeComment comment : income.getComments()) {
                    LoanComment lc = new LoanComment(lt, comment.getUser(), comment.getMessage());
                    lc.setTimestamp(comment.getTimestamp());
                    lt.getComments().add(lc);
                }
            }

            loanTransactionRepository.save(lt);
            actualLoanSum += lt.getAmount();
        }

        double actualTotal = actualCapitalSum + actualLoanSum;
        if (Math.abs(actualTotal - expectedTotal) > 0.001) {
            throw new IllegalStateException(String.format(
                    "Accounting migration checksum mismatch! Expected %.2f but migrated %.2f",
                    expectedTotal, actualTotal));
        }

        if (!capitalIncomes.isEmpty()) {
            incomeRepository.deleteAll(capitalIncomes);
        }
        if (!loanIncomes.isEmpty()) {
            incomeRepository.deleteAll(loanIncomes);
        }
        incomeRepository.flush();

        removeLegacyCategoryIfUnused("Capital Introduced");
        removeLegacyCategoryIfUnused("Loan");

        seedIncomeCategoriesAndSubCategories();

        if (!capitalIncomes.isEmpty() || !loanIncomes.isEmpty()) {
            log.info("Accounting migration completed: migrated {} Capital records (₹{}) and {} Loan records (₹{}).",
                    capitalIncomes.size(), actualCapitalSum, loanIncomes.size(), actualLoanSum);
        }

        return new MigrationSummary(capitalIncomes.size(), actualCapitalSum, loanIncomes.size(), actualLoanSum);
    }

    private String extractLenderName(String loanName) {
        int idx = loanName.toLowerCase().indexOf("from ");
        if (idx >= 0 && idx + 5 < loanName.length()) {
            return loanName.substring(idx + 5).trim();
        }
        return "Lender";
    }

    private void removeLegacyCategoryIfUnused(String categoryName) {
        Optional<Category> catOpt = categoryRepository.findByNameIgnoreCase(categoryName);
        if (catOpt.isPresent()) {
            Category cat = catOpt.get();
            List<SubCategory> subs = subCategoryRepository.findByCategoryId(cat.getId());
            if (!subs.isEmpty()) {
                subCategoryRepository.deleteAll(subs);
            }
            categoryRepository.delete(cat);
        }
    }

    private void seedIncomeCategoriesAndSubCategories() {
        Category sales = ensureIncomeCategory("Income From Sales");
        ensureSubCategory(sales, "Fresh Mushroom Sales");
        ensureSubCategory(sales, "Bulk / Wholesale Sales");
        ensureSubCategory(sales, "Compost / Spawn Sales");

        Category propertySales = ensureIncomeCategory("Income From Property Sales");
        ensureSubCategory(propertySales, "Land / Plot Sale");
        ensureSubCategory(propertySales, "Equipment / Machinery Sale");
        ensureSubCategory(propertySales, "Scrap / Salvage Sale");

        Category otherIncome = ensureIncomeCategory("Other Income");
        ensureSubCategory(otherIncome, "Government Subsidy / Grant");
        ensureSubCategory(otherIncome, "Miscellaneous Income");
    }

    private Category ensureIncomeCategory(String name) {
        return categoryRepository.findByNameIgnoreCase(name).orElseGet(() -> {
            Category c = new Category();
            c.setName(name);
            c.setType(TransactionType.INCOME);
            return categoryRepository.save(c);
        });
    }

    private void ensureSubCategory(Category category, String subCategoryName) {
        if (subCategoryRepository.findByNameIgnoreCaseAndCategoryId(subCategoryName, category.getId()).isEmpty()) {
            SubCategory sub = new SubCategory();
            sub.setName(subCategoryName);
            sub.setCategory(category);
            subCategoryRepository.save(sub);
        }
    }
}
