# Phase 0 Research: 4-Pillar Accounting Segregation

## Decision 1: Dedicated Domain Tables vs. Polymorphic Single Ledger
- **Decision**: Create dedicated tables (`capital_transactions`, `loans`, `loan_transactions`) and their respective comment/attachment tables rather than overloading `incomes` and `expenses`.
- **Rationale**:
  - `LoanTransaction` requires a mandatory foreign key (`loan_id`) to an individual `Loan` facility and strict over-repayment validation against that loan's balance.
  - `CapitalTransaction` represents equity movements (`INTRODUCED`, `DRAWINGS`) that have no `Category`/`SubCategory` hierarchy but adjust against a unified Net Capital pool.
  - Dedicated tables prevent accidental inclusion of equity/debt in P&L queries (`SELECT SUM(amount) FROM incomes`).
- **Alternatives Considered**: Adding a `ledger_type` discriminator column on `incomes`/`expenses` — rejected because Drawings and Loan Repayments are outflows (not income) and mixing them in `incomes` or `expenses` complicates every repository aggregate query.

## Decision 2: Individual Loan Tracking with Dashboard Pool Expansion
- **Decision**: Track every loan facility individually via a `Loan` master entity (`loans`) and child `LoanTransaction` entries (`loan_transactions` with `BORROWED` and `REPAYMENT`), while displaying the aggregated **Outstanding Loan Pool** card on `/dashboard` that expands a modal showing individual loans on click (and similarly for **Net Capital Pool**).
- **Rationale**: User explicitly requested individual loan tracking with an expandable pool card on the dashboard, plus preserving the Lifetime, Monthly, and Yearly (FY) dashboard breakdowns.
- **Alternatives Considered**: Global unlinked loan pool — rejected because repayments cannot be reconciled against a specific lender's principal balance.

## Decision 3: Transactional Startup Migration with Checksum Guard
- **Decision**: Implement `AccountingMigrationService.migrateLegacyCapitalAndLoans()` invoked inside `@Transactional` on startup (right after seed initialization in `DataInitializer` and also safe for production PostgreSQL startup).
- **Rationale**:
  - Seed SQL (`prod-seed.sql`) and production DB currently hold 7 `Capital Introduced` rows (`category_id = 12`, ₹1,90,000.00) and 1 `Loan` row (`category_id = 13`, ₹1,70,000.00, description `"Unsecured Loan From Father"`), plus comments (`id=1` on `income_id=1`, `id=2` on `income_id=14`).
  - Migrating via a Spring `@Transactional` service guarantees that if the post-migration checksum (`migratedCapital + migratedLoan != deletedIncomeTotal`) fails, the entire transaction rolls back.
- **Alternatives Considered**: Hand-editing `prod-seed.sql` only — rejected because production PostgreSQL also needs the automatic migration when deployed.
