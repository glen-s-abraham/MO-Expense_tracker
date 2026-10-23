# Quickstart & Verification Guide: 4-Pillar Accounting Segregation

## 1. Build & Run Full Automated Test Suite

```bash
mvn clean verify
```

Expected outcome:
- All unit and integration tests pass (`0` failures).
- `AccountingMigrationServiceTest` verifies that the 7 `Capital Introduced` rows (`₹1,90,000.00`) and 1 `Loan` row (`₹1,70,000.00`) migrate cleanly from `incomes` to `capital_transactions` and `loans`/`loan_transactions` with comments preserved and `₹29,562.00` operational sales income remaining.

## 2. Reset Local H2 Database & Start Application

```bash
./scripts/reset-h2-seed.sh
mvn spring-boot:run
```

## 3. Interactive Verification Steps

1. Log in at `http://localhost:8080/login` with username `admin` and password `password`.
2. **Dashboard (`/dashboard`)**:
   - Verify **Operational Income** is `₹29,562.00`, **Operational Expenses** is `₹3,74,904.66`, and **Net Operating Profit** is `-₹3,45,342.66`.
   - Verify **Net Capital Pool** is `₹1,90,000.00` and **Outstanding Loan Pool** is `₹1,70,000.00`.
   - Click the **Outstanding Loan Pool** card and confirm the modal expands to show *"Unsecured Loan From Father"* (`Borrowed: ₹1,70,000.00`, `Repaid: ₹0.00`, `Outstanding: ₹1,70,000.00`).
   - Click the **Net Capital Pool** card and confirm the modal shows `Capital Introduced: ₹1,90,000.00` and `Drawings: ₹0.00`.
   - Test **Monthly Snapshot** and **Yearly Snapshot (FY)** selectors to verify dynamic updates across all 4 pillars.
3. **Capital (`/capital`)**:
   - View the 7 migrated records under the **Capital Introduced** tab.
   - Switch to the **Drawings** tab, add a Drawing entry, approve it, and verify the Net Capital balance adjusts downward.
4. **Loans (`/loans`)**:
   - View the *"Unsecured Loan From Father"* loan card and its inward borrowing under **Loans Inward**.
   - Switch to **Loan Repayments**, record a repayment against *"Unsecured Loan From Father"*, and verify the loan's outstanding balance decreases.
