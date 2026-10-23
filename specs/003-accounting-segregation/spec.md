# Feature Specification: 4-Pillar Accounting Segregation (Expenses, Income, Capital & Loans)

**Feature Branch**: `[003-accounting-segregation]`

**Created**: 2026-10-22

**Status**: Approved

**Input**: User description: "Migrate Capital and Loans out of Income into dedicated Capital (Capital Introduced & Drawings) and Loans (Loans Inward & Loan Repayments against individual loans) sections, keep Expenses as the original feature and Income strictly for revenue categories (Sales, Property Sales, Other Income with subcategories), include transactional database migration of existing Capital Introduced and Loan records from incomes, and update the Dashboard (including Lifetime, Monthly, and Yearly FY setups) with expandable Capital and Loan Pool cards."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Lossless Historical Data Migration & Pure Operational Income (Priority: P1)

Finance managers and business owners need historical equity injections (`Capital Introduced`) and debt facilities (`Loan`) removed from the operational `Income` ledger and migrated into dedicated Capital and Loan accounts without losing a single rupee, comment, or attachment, while keeping the `Income` module focused strictly on business and asset revenue (`Income From Sales`, `Income From Property Sales`, `Other Income`) with subcategory support.

**Why this priority**: Accurate financial reporting depends on separating balance-sheet financing (Capital and Loans) from Profit & Loss revenue (Income). Migrating existing data first prevents distorted profit metrics and establishes clean ledgers.

**Independent Test**: Can be fully tested by starting the application with the production seed data and verifying that the 7 `Capital Introduced` entries (₹1,90,000.00) and 1 `Loan` entry (₹1,70,000.00 — *"Unsecured Loan From Father"*), along with their comments and attachments, are automatically migrated into the Capital and Loan ledgers, removed from `Income`, and that only operational income (25 rows, ₹29,562.00) remains in `Income`.

**Acceptance Scenarios**:

1. **Given** the database contains legacy `Income` records categorized under `Capital Introduced` (7 records, ₹1,90,000.00) and `Loan` (1 record, ₹1,70,000.00), **When** the application starts up and executes the accounting migration, **Then** all 7 capital records are moved to the Capital ledger as `INTRODUCED`, the loan record creates an individual loan account (`Unsecured Loan From Father`) with an inward `BORROWED` transaction of ₹1,70,000.00, all comments and attachments are preserved, the migrated records and legacy categories (`Capital Introduced`, `Loan`) are deleted from the `Income` ledger, and the total ₹3,60,000.00 checksum is verified.
2. **Given** the migration has already completed once, **When** the application restarts, **Then** the migration runs idempotently with zero duplicate records created.
3. **Given** an authenticated user navigates to `/my-incomes/new`, **When** they select an Income Category (such as `Income From Sales` or `Income From Property Sales`), **Then** they can optionally select a corresponding Sub-Category and submit the operational revenue record through the standard approval workflow.

---

### User Story 2 - Capital Ledger with Capital Introduced & Owner Drawings (Priority: P1)

Business owners, managers, and accountants need a dedicated **Capital** section (`/capital`) with two distinct tabs—**Capital Introduced** and **Drawings**—where owner investments increase the Net Capital pool and owner withdrawals (`Drawings`) adjust against the Net Capital pool (`Net Capital = Approved Capital Introduced - Approved Drawings`).

**Why this priority**: Tracking owner equity injections and personal drawings separately from operational expenses and income is essential for accurate balance-sheet equity tracking.

**Independent Test**: Can be fully tested by navigating to `/capital`, viewing the migrated ₹1,90,000.00 under the **Capital Introduced** tab, recording and approving a ₹15,000.00 entry under the **Drawings** tab, and verifying that the Net Capital Pool updates to ₹1,75,000.00.

**Acceptance Scenarios**:

1. **Given** an authenticated user is on `/capital`, **When** they switch between the **Capital Introduced** tab and the **Drawings** tab, **Then** they see filtered transactions for each type along with a summary banner showing **Total Capital Introduced**, **Total Drawings**, and **Net Capital Balance**.
2. **Given** an authenticated user records a new Capital transaction (`INTRODUCED` or `DRAWINGS`) with optional file attachments and submits it, **When** an Accountant, Manager, or Admin approves it, **Then** the transaction status becomes `APPROVED` and adjusts the Net Capital balance accordingly.
3. **Given** an authenticated user filters the Capital ledger and clicks **Export Excel**, **When** the download completes, **Then** the exported spreadsheet contains all matching Capital transactions.

---

### User Story 3 - Individual Loan Facilities, Inward Borrowings & Loan Repayments (Priority: P1)

Business owners, managers, and accountants need a dedicated **Loans** section (`/loans`) where individual loan accounts (e.g., *"Unsecured Loan From Father"*) are tracked, inward borrowings (`BORROWED`) increase the loan's principal balance, and principal repayments (`REPAYMENT`) under the **Loan Repayments** tab reduce that specific loan's outstanding balance with automatic over-repayment prevention.

**Why this priority**: Every loan has its own lender and principal balance. Repayments must reduce the specific loan they apply to and prevent accidental over-repayment beyond the outstanding balance.

**Independent Test**: Can be fully tested by navigating to `/loans`, viewing the *"Unsecured Loan From Father"* facility (₹1,70,000.00 borrowed, ₹0.00 repaid, ₹1,70,000.00 outstanding), attempting to record a repayment of ₹2,00,000.00 (which is blocked by validation), and then recording and approving a ₹20,000.00 repayment, verifying the loan's outstanding balance drops to ₹1,50,000.00.

**Acceptance Scenarios**:

1. **Given** an authenticated user is on `/loans`, **When** they view the page, **Then** they see individual Loan cards showing lender/loan name, total borrowed, total repaid, outstanding balance, and status (`ACTIVE` or `CLOSED`), along with two transaction tabs: **Loans Inward** (`BORROWED`) and **Loan Repayments** (`REPAYMENT`).
2. **Given** a loan facility has an outstanding balance of ₹1,70,000.00, **When** a user attempts to submit or approve a `REPAYMENT` transaction of ₹1,80,000.00 against that loan, **Then** the system rejects the transaction with a clear validation message stating that the repayment exceeds the loan's outstanding principal balance.
3. **Given** a loan facility has an outstanding balance of ₹50,000.00, **When** an approved `REPAYMENT` of ₹50,000.00 is recorded against it, **Then** the loan's outstanding balance becomes ₹0.00 and its status automatically transitions to `CLOSED`.

---

### User Story 4 - Executive Dashboard with Lifetime, Monthly & Yearly (FY) Setups and Expandable Pool Modals (Priority: P1)

Admins, Managers, and Accountants need the executive dashboard (`/dashboard`) to preserve its **Lifetime**, **Monthly Snapshot**, and **Yearly Snapshot (FY)** views while cleanly separating **Operational P&L** (`Operational Income`, `Operational Expenses`, `Net Operating Profit`) from **Financing & Liquidity** (`Net Capital Pool`, `Outstanding Loan Pool`, `Net Cash Position`), and allowing users to click the **Outstanding Loan Pool** or **Net Capital Pool** cards to expand a detailed breakdown modal.

**Why this priority**: The executive dashboard is the primary financial command center. Users explicitly require both the existing Monthly/Yearly (FY) breakdown adjusted to the 4-pillar model and an expandable pool view on the dashboard that reveals individual loans and capital movements on click.

**Independent Test**: Can be fully tested by opening `/dashboard`, verifying the Lifetime, Monthly Snapshot, and Yearly Snapshot (FY) metrics reflect the 4-pillar separation, and clicking the **Outstanding Loan Pool** card to inspect the individual loan table in the expanded modal.

**Acceptance Scenarios**:

1. **Given** an authorized user views `/dashboard`, **When** the page loads, **Then** the Lifetime section displays two distinct groups: **Operational P&L** (`Operational Income`, `Operational Expenses`, `Net Operating Profit`) and **Financing & Liquidity** (`Net Capital Pool`, `Outstanding Loan Pool`, `Net Cash Position`).
2. **Given** an authorized user clicks the **Outstanding Loan Pool** card on `/dashboard`, **When** the breakdown modal opens, **Then** it lists every individual loan with its lender, total borrowed, total repaid, outstanding balance, repayment progress bar, and status.
3. **Given** an authorized user clicks the **Net Capital Pool** card on `/dashboard`, **When** the breakdown modal opens, **Then** it displays the total Capital Introduced, total Drawings, Net Capital balance, and recent capital movements.
4. **Given** an authorized user switches months in the **Monthly Snapshot** or changes the financial year in the **Yearly Snapshot (FY)** on `/dashboard`, **Then** the snapshot cards and charts dynamically update with that period's Operational Income, Operational Expenses, Net Operating Profit, Capital Introduced, Drawings, Net Capital change, Loans Borrowed, Loan Repayments, and Net Cash Flow.

---

### User Story 5 - Decluttered Navigation & Unified 4-Pillar Approvals (Priority: P2)

All users need a clean navigation bar with direct access to **Expenses**, **Income**, **Capital**, and **Loans**, and Supervisors/Accountants/Managers/Admins need `/approvals` to support reviewing, querying, approving, and rejecting submissions across all four pillars.

**Why this priority**: Streamlines daily navigation and ensures governance controls apply uniformly to Expenses, Income, Capital, and Loans.

**Independent Test**: Can be fully tested by checking the top navigation bar across roles and approving pending Expense, Income, Capital, and Loan entries from `/approvals`.

**Acceptance Scenarios**:

1. **Given** an authenticated user views the navigation bar, **When** they inspect the links, **Then** they see dedicated links for **Dashboard** (role-permitted), **Expenses**, **Income**, **Capital**, **Loans**, **Approvals** (role-permitted), and **Master Data** (Admin/Manager).
2. **Given** a Supervisor, Accountant, Manager, or Admin opens `/approvals`, **When** there are pending items across Expenses, Income, Capital, or Loans, **Then** they can switch tabs between the 4 pillars and approve, reject, or raise queries on each item.

---

### Edge Cases

- **Over-repayment of an individual loan**: If a user enters a `REPAYMENT` amount greater than the selected loan's current outstanding balance (`Approved Borrowed - Approved Repaid`), the system blocks submission/approval with a descriptive error.
- **Loan auto-reopen / auto-close**: If a loan reaches ₹0.00 outstanding, its status automatically updates to `CLOSED`; if an additional inward borrowing is later approved on that loan, its status returns to `ACTIVE`.
- **Interest vs. Principal separation**: On the Loan Repayment form, a visible guidance banner reminds users that only principal repayments belong in `/loans`, while loan interest charges belong in `/my-expenses` under `Financial -> Interest Payments`.
- **Null subcategory on legacy Income records**: Existing migrated/seeded `Income` rows have `sub_category_id = NULL`; all views, specifications, and exports handle `subCategory == null` gracefully without errors.
- **Idempotent startup migration**: Running the application multiple times against an already-migrated database must detect that no legacy `Capital Introduced` or `Loan` records remain in `incomes` and skip migration cleanly.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a dedicated **Capital** module (`/capital`) supporting two transaction types: `INTRODUCED` (Capital Introduced) and `DRAWINGS` (Owner Drawings), complete with status workflow (`DRAFT`, `SUBMITTED`, `UNDER_QUERY`, `APPROVED`, `REJECTED`), comments, file attachments, filtering, pagination, and Excel export.
- **FR-002**: System MUST calculate **Net Capital Pool** as `Sum(APPROVED INTRODUCED) - Sum(APPROVED DRAWINGS)`.
- **FR-003**: System MUST provide a dedicated **Loans** module (`/loans`) supporting named individual `Loan` facilities and `LoanTransaction` records of type `BORROWED` (Loans Inward) and `REPAYMENT` (Loan Repayments), complete with status workflow, comments, file attachments, filtering, pagination, and Excel export.
- **FR-004**: System MUST calculate each individual loan's outstanding balance as `Sum(APPROVED BORROWED for loan) - Sum(APPROVED REPAYMENT for loan)` and the **Outstanding Loan Pool** as the sum of outstanding balances across all loans.
- **FR-005**: System MUST prevent submitting or approving a `REPAYMENT` transaction whose amount exceeds the target loan's current outstanding balance.
- **FR-006**: System MUST automatically transition a `Loan` facility's status to `CLOSED` when its outstanding balance reaches `0` (and back to `ACTIVE` if new borrowings increase its balance above `0`).
- **FR-007**: System MUST enhance the **Income** module (`/my-incomes`) with an optional `SubCategory` relationship, seed the `Income From Property Sales` category and default income sub-categories, and restrict Income categories strictly to `TYPE = 'INCOME'` revenue sources (`Income From Sales`, `Income From Property Sales`, `Other Income`).
- **FR-008**: System MUST execute an automatic, transactional, idempotent startup data migration that moves all `incomes` rows with category `Capital Introduced` (and their comments/attachments) to `capital_transactions` (`INTRODUCED`), moves all `incomes` rows with category `Loan` (and their comments/attachments) into individual `loans` and `loan_transactions` (`BORROWED`), verifies that `Migrated Capital + Migrated Loans == Deleted Income Total`, deletes the migrated rows from `incomes`, and deletes `Capital Introduced` and `Loan` from `categories`.
- **FR-009**: System MUST update `/dashboard` across **Lifetime**, **Monthly Snapshot**, and **Yearly Snapshot (FY)** views to separate **Operational P&L** (`Operational Income`, `Operational Expenses`, `Net Operating Profit`) from **Financing & Liquidity** (`Net Capital Pool`, `Outstanding Loan Pool`, `Net Cash Position`), where `Net Cash Position = Net Operating Profit + Net Capital Pool + Outstanding Loan Pool`.
- **FR-010**: System MUST provide interactive drilldown modals on `/dashboard` when clicking the **Outstanding Loan Pool** card (listing each individual loan's borrowed, repaid, outstanding balance, and status) and the **Net Capital Pool** card (showing Capital Introduced vs. Drawings breakdown).
- **FR-011**: System MUST update the top navigation bar (`layout.html`) and unified approvals view (`/approvals`) to include **Expenses**, **Income**, **Capital**, and **Loans**.
- **FR-012**: System MUST update `scripts/dump-prod-to-seed.sh` so future database seed dumps include the new Capital and Loan tables.

### Key Entities

- **CapitalTransaction**: Represents an equity movement by an owner/partner (`type`: `INTRODUCED` or `DRAWINGS`, `amount`, `transactionDate`, `paymentMethod`, `description`, `status`, audit fields, comments, attachments).
- **Loan**: Represents an individual loan facility or debt account (`loanName`, `lenderName`, `description`, `startDate`, `status`: `ACTIVE` or `CLOSED`, audit fields).
- **LoanTransaction**: Represents a financial movement against a specific `Loan` (`loan`, `type`: `BORROWED` or `REPAYMENT`, `amount`, `transactionDate`, `paymentMethod`, `description`, `status`, audit fields, comments, attachments).
- **Income**: Represents operational or asset-sale revenue (`category`, optional `subCategory`, `amount`, `incomeDate`, `source`, `paymentMethod`, `description`, `status`, comments, attachments).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of historical `Capital Introduced` (7 records, ₹1,90,000.00) and `Loan` (1 record, ₹1,70,000.00) records, along with 100% of their comments and attachments, are migrated with zero data loss and a ₹0.00 checksum variance.
- **SC-002**: Operational Income on the dashboard after migration accurately reports ₹29,562.00 (25 sales records) instead of the previously inflated ₹3,89,562.00.
- **SC-003**: 100% of attempted loan over-repayments (repayment > loan outstanding balance) are blocked with a clear user-facing validation message.
- **SC-004**: Dashboard users can view Lifetime, Monthly, and Yearly (FY) metrics across all 4 pillars and expand the Capital Pool or Loan Pool breakdown modal in a single click.
- **SC-005**: All automated unit and integration tests pass via `mvn clean verify` with zero regressions.

## Assumptions

- Existing role permissions for Income (`USER`, `SUPERVISOR`, `ACCOUNTANT`, `MANAGER`, `ADMIN`) apply identically to the new Capital and Loans modules.
- Only `APPROVED` transactions impact the Net Capital Pool, Outstanding Loan Pool, Net Operating Profit, and Net Cash Position KPIs on the dashboard.
- Loan interest payments continue to be recorded as operational expenses under `Expenses -> Financial -> Interest Payments` so they properly reduce Net Operating Profit.
