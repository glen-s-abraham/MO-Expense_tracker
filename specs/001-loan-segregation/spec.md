# Feature Specification: Loan Segregation

**Feature Branch**: `[001-loan-segregation]`

**Created**: 2026-08-06

**Status**: Draft

**Input**: User description: "/home/glen-personal/projects/MO-Expense_tracker/feature-inputs/accounting_refactor_roadmap.md build specification fro phase1"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Cash Inflow & Outflow Navigation Refactor (Priority: P1)

As a user, I want the sidebar navigation to clearly distinguish between Cash Inflows and Cash Outflows rather than just "Income" and "Expenses", so that I can easily find where to record sales vs loans.

**Why this priority**: The terminology update is the foundation for separating operations from financing. It establishes the mental model for the user before they interact with the data.

**Independent Test**: Can be fully tested by logging into the application and inspecting the sidebar navigation menus.

**Acceptance Scenarios**:

1. **Given** a logged-in user, **When** they view the sidebar, **Then** they see "Cash Inflows" and "Cash Outflows" instead of "Income" and "Expenses".
2. **Given** the "Cash Inflows" menu, **When** expanded, **Then** it shows "Sales & Others (Income)" and "Loan Inward".
3. **Given** the "Cash Outflows" menu, **When** expanded, **Then** it shows "General Expenses" and "Loan Repayments".

---

### User Story 2 - Recording Loan Transactions (Priority: P1)

As a user, I want to record money received from loans and money paid towards loan repayments as a distinct transaction type, so that they are not mixed up with my true operational income and expenses.

**Why this priority**: This is the core functionality. Extracting loans from standard income/expenses prevents profit inflation.

**Independent Test**: Can be fully tested by submitting a new Loan Inward and a new Loan Repayment via their respective forms and verifying they appear in the loan transaction list.

**Acceptance Scenarios**:

1. **Given** the "Loan Inward" form, **When** a user submits a loan receipt of $5000, **Then** the loan transaction is saved and does not appear in the standard Income list.
2. **Given** the "Loan Repayment" form, **When** a user submits a loan repayment of $1000, **Then** the loan transaction is saved and does not appear in the standard Expense list.

---

### User Story 3 - Dashboard Financial Metrics Updates (Priority: P1)

As a business owner, I want my dashboard to show "Net Profit" calculated only from operational income and expenses, and a separate "Net Cash Flow" metric that includes loans, so that I have a true picture of my business profitability versus bank balance.

**Why this priority**: Without updating the dashboard, the data segregation is useless to the end-user. The dashboard is the primary consumption point for this data.

**Independent Test**: Can be fully tested by creating standard income/expenses alongside loans, and verifying the dashboard totals reflect the correct mathematical formulas.

**Acceptance Scenarios**:

1. **Given** a dashboard with $10,000 Sales, $4,000 Expenses, and $5,000 Loan Inward, **When** the dashboard loads, **Then** Net Profit displays $6,000.
2. **Given** the same data, **When** the dashboard loads, **Then** Net Cash Flow displays $11,000.

### Edge Cases

- What happens if a user tries to delete a loan that has already been approved? The system should either prevent deletion or require it to go through an explicit cancellation/reversal workflow, similar to standard expenses.
- How does the system handle historical reports when switching navigation terminology? It should consistently apply "Net Profit" vs "Cash Flow" retrospectively to all previously recorded true income/expenses.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide a dedicated data model for `LoanTransaction` that tracks amount, date, description, payment mode, status, and transaction type (Inward vs Repayment).
- **FR-002**: System MUST remove "Loan" from the standard operational Category list.
- **FR-003**: System MUST provide UI forms to create, read, update, and delete Loan Transactions.
- **FR-004**: System MUST calculate Dashboard "Net Profit" strictly as `(Total True Income) - (Total General Expenses)`.
- **FR-005**: System MUST calculate Dashboard "Net Cash Flow" (or Cash Position) as `(Net Profit) + (Total Loan Inward) - (Total Loan Repayment)`.
- **FR-006**: System MUST update the global layout navigation to reflect the new "Cash Inflow" and "Cash Outflow" terminology and sub-menus.

### Key Entities

- **LoanTransaction**: Represents a movement of loan money. Attributes include `amount`, `date`, `description`, `paymentMode`, `status`, and `type` (INWARD or REPAYMENT).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of recorded loans (inward and outward) are excluded from the Dashboard's "Net Profit" calculation.
- **SC-002**: Users can navigate to the Loan forms within 2 clicks from the main dashboard using the new Cash Inflow/Outflow taxonomy.
- **SC-003**: No regressions in existing Income and Expense operational workflows.

## Assumptions

- We assume no existing "Loan" records in the legacy `Income` and `Expense` tables need to be migrated in this phase; or if they do, a database reset is acceptable for the current development environment.
- We assume Loan transactions will use the same approval workflow (Draft -> Approved) as standard Income/Expenses.
