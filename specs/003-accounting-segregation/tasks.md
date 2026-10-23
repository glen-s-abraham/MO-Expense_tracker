# Tasks: 4-Pillar Accounting Segregation (Expenses, Income, Capital & Loans)

**Input**: Design documents from `/specs/003-accounting-segregation/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/web-routes.md`, `quickstart.md`

## Phase 1: Setup & Domain Model Foundations

**Purpose**: Create the core enums, JPA entities, and repositories required for Capital, Loans, and enhanced Income.

- [X] T001 [P] Create `CapitalTransactionType` enum (`INTRODUCED`, `DRAWINGS`) in `src/main/java/com/mushroom/expense/entity/CapitalTransactionType.java`
- [X] T002 [P] Create `LoanStatus` (`ACTIVE`, `CLOSED`) and `LoanTransactionType` (`BORROWED`, `REPAYMENT`) enums in `src/main/java/com/mushroom/expense/entity/LoanStatus.java` and `src/main/java/com/mushroom/expense/entity/LoanTransactionType.java`
- [X] T003 [P] Create `CapitalTransaction`, `CapitalComment`, and `CapitalAttachment` JPA entities in `src/main/java/com/mushroom/expense/entity/CapitalTransaction.java`, `src/main/java/com/mushroom/expense/entity/CapitalComment.java`, and `src/main/java/com/mushroom/expense/entity/CapitalAttachment.java`
- [X] T004 [P] Create `Loan`, `LoanTransaction`, `LoanComment`, and `LoanAttachment` JPA entities in `src/main/java/com/mushroom/expense/entity/Loan.java`, `src/main/java/com/mushroom/expense/entity/LoanTransaction.java`, `src/main/java/com/mushroom/expense/entity/LoanComment.java`, and `src/main/java/com/mushroom/expense/entity/LoanAttachment.java`

---

## Phase 2: Foundational Repositories & Specifications

**Purpose**: Implement Spring Data JPA repositories and dynamic filter specifications for Capital, Loans, and Income.

- [X] T005 [P] Create `CapitalTransactionRepository`, `CapitalCommentRepository`, `CapitalAttachmentRepository`, and `CapitalSpecification` in `src/main/java/com/mushroom/expense/repository/` and `src/main/java/com/mushroom/expense/specification/`
- [X] T006 [P] Create `LoanRepository`, `LoanTransactionRepository`, `LoanCommentRepository`, `LoanAttachmentRepository`, and `LoanSpecification` in `src/main/java/com/mushroom/expense/repository/` and `src/main/java/com/mushroom/expense/specification/`
- [X] T007 Update `Income` entity and `IncomeSpecification` to support optional `SubCategory subCategory` in `src/main/java/com/mushroom/expense/entity/Income.java` and `src/main/java/com/mushroom/expense/specification/IncomeSpecification.java`

---

## Phase 3: User Story 1 - Lossless Historical Data Migration & Pure Operational Income (Priority: P1) 🎯 MVP

**Goal**: Automatically migrate `Capital Introduced` (7 rows, ₹1,90,000) and `Loan` (1 row, ₹1,70,000) from `incomes` to `capital_transactions` and `loans`/`loan_transactions` with comments/attachments and checksum verification, seed `Income From Property Sales` and income subcategories, and support subcategories in Income UI.

**Independent Test**: Run `AccountingMigrationServiceTest` and `DataInitializerTest` to confirm all 8 financing records migrate cleanly, `incomes` retains 25 operational sales rows (`₹29,562.00`), and `Income` supports subcategories.

- [X] T008 [US1] Implement `AccountingMigrationService` with transactional migration, comment/attachment migration, mathematical checksum verification, and new Income category/subcategory seeding in `src/main/java/com/mushroom/expense/service/AccountingMigrationService.java`
- [X] T009 [US1] Wire `AccountingMigrationService` into `DataInitializer` in `src/main/java/com/mushroom/expense/config/DataInitializer.java` and update `scripts/dump-prod-to-seed.sh` for the new tables
- [X] T010 [US1] Update `IncomeController`, `income_form.html`, `income_dashboard.html`, and `income_view.html` to support `subCategory` selection, display, filtering, and CSV export

---

## Phase 4: User Story 2 - Capital Ledger with Capital Introduced & Owner Drawings (Priority: P1)

**Goal**: Provide a complete `/capital` section with **Capital Introduced** (`INTRODUCED`) and **Drawings** (`DRAWINGS`) tabs, Net Capital balance calculation, approval workflow, comments, attachments, and CSV export.

**Independent Test**: Navigate to `/capital`, view migrated `INTRODUCED` entries, create & approve a `DRAWINGS` entry, and verify Net Capital (`Introduced - Drawings`).

- [X] T011 [US2] Implement `CapitalService` in `src/main/java/com/mushroom/expense/service/CapitalService.java`
- [X] T012 [US2] Implement `CapitalController` in `src/main/java/com/mushroom/expense/controller/CapitalController.java`
- [X] T013 [US2] Create Thymeleaf templates `src/main/resources/templates/capital/dashboard.html`, `src/main/resources/templates/capital/form.html`, and `src/main/resources/templates/capital/view.html`

---

## Phase 5: User Story 3 - Individual Loan Facilities, Inward Borrowings & Loan Repayments (Priority: P1)

**Goal**: Provide a complete `/loans` section tracking individual `Loan` accounts, **Loans Inward** (`BORROWED`), and **Loan Repayments** (`REPAYMENT`) with over-repayment guardrails and automatic `ACTIVE`/`CLOSED` status transitions.

**Independent Test**: Navigate to `/loans`, inspect *"Unsecured Loan From Father"*, verify over-repayment is blocked, and verify valid repayment reduces the loan's outstanding balance.

- [X] T014 [US3] Implement `LoanService` with individual loan balance tracking and over-repayment validation in `src/main/java/com/mushroom/expense/service/LoanService.java`
- [X] T015 [US3] Implement `LoanController` in `src/main/java/com/mushroom/expense/controller/LoanController.java`
- [X] T016 [US3] Create Thymeleaf templates `src/main/resources/templates/loan/dashboard.html`, `src/main/resources/templates/loan/form.html`, and `src/main/resources/templates/loan/view.html`

---

## Phase 6: User Story 4 - Executive Dashboard with Lifetime, Monthly & Yearly (FY) Setups and Expandable Pool Modals (Priority: P1)

**Goal**: Update `/dashboard` across Lifetime, Monthly Snapshot, and Yearly Snapshot (FY) to separate Operational P&L from Financing & Liquidity (`Net Capital Pool`, `Outstanding Loan Pool`, `Net Cash Position`) and provide expandable drilldown modals when clicking the Capital Pool or Loan Pool cards.

**Independent Test**: Load `/dashboard`, verify Lifetime, Monthly, and Yearly (FY) metrics, and click **Outstanding Loan Pool** and **Net Capital Pool** to verify the expanded modal breakdowns.

- [X] T017 [US4] Update `DashboardController` and `DashboardRestController` in `src/main/java/com/mushroom/expense/controller/DashboardController.java` and `src/main/java/com/mushroom/expense/controller/DashboardRestController.java` with 4-pillar Lifetime, Monthly, Yearly (FY), and pool breakdown data
- [X] T018 [US4] Update `src/main/resources/templates/dashboard.html` with Operational P&L vs. Financing & Liquidity cards, expandable **Net Capital Pool** and **Outstanding Loan Pool** modals, and updated Monthly/Yearly (FY) snapshots

---

## Phase 7: User Story 5 - Decluttered Navigation & Unified 4-Pillar Approvals (Priority: P2)

**Goal**: Update `layout.html` navbar with clean links for Expenses, Income, Capital, and Loans, and update `/approvals` to support all 4 pillars.

**Independent Test**: Verify navbar links and approve pending Expense, Income, Capital, and Loan entries from `/approvals`.

- [X] T019 [US5] Update `src/main/resources/templates/layout.html`, `src/main/java/com/mushroom/expense/controller/ExpenseController.java`, `src/main/resources/templates/accountant/approvals.html`, and `src/main/resources/templates/supervisor/approvals.html`

---

## Phase 8: Polish & Automated Verification

**Purpose**: Comprehensive unit and integration tests, JaCoCo verification, and local H2 seed verification.

- [X] T020 [P] Write unit and integration tests in `src/test/java/com/mushroom/expense/service/AccountingMigrationServiceTest.java`, `src/test/java/com/mushroom/expense/service/CapitalServiceTest.java`, `src/test/java/com/mushroom/expense/service/LoanServiceTest.java`, `src/test/java/com/mushroom/expense/controller/CapitalControllerTest.java`, `src/test/java/com/mushroom/expense/controller/LoanControllerTest.java`, and update `src/test/java/com/mushroom/expense/config/DataInitializerTest.java`
- [X] T021 Run `mvn clean verify` and `./scripts/reset-h2-seed.sh` to validate all tests, coverage, and local H2 seed state
