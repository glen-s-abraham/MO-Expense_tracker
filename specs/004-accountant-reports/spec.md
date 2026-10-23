# Feature Specification: Accountant Reports Export (Monthly & Yearly)

**Feature Branch**: `[004-accountant-reports]`

**Created**: 2026-10-23

**Status**: Draft

**Input**: User description: "Include a reports section (xlsx or csv) to share valid data with accountants for auditing, taxation and similar purposes. Available from the dashboard for monthly as well as yearly periods. Each pillar gets its own tab. On top of each sheet show the totals, below it the descriptions and time frame; the front page includes the dashboard details. The data must let accountants prepare documents such as a balance sheet. Keep it modern and pretty."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Download a Yearly/Monthly Audit Workbook (Priority: P1)

An authorized user (Admin/Accountant) opens the dashboard and, in the Reports panel embedded on the dashboard itself (no separate page), picks a period (a specific month or a financial year) and clicks "Download Report" choosing Excel (.xlsx). They receive one workbook to hand to the accountant, containing a cover sheet plus one tab per pillar: Expenses, Income, Capital, Loans.

**Why this priority**: This is the core value: a single, audit-ready file for the accountant.

**Independent Test**: Select a month with data in all four pillars, download the .xlsx, and verify it has beginning overview tabs (Period Summary and Lifetime Financials) plus four pillar tabs whose figures match the dashboard for that period and lifetime metrics.

**Acceptance Scenarios**:

1. **Given** a month with approved transactions in all pillars, **When** the user downloads the monthly Excel report, **Then** the workbook contains a front page and separate tabs for Expenses, Income, Capital and Loans.
2. **Given** a year is selected, **When** the user downloads the yearly report, **Then** every tab covers the selected financial year (1 April – 31 March) and includes a month-wise breakdown.
3. **Given** a pillar has no data in the period, **When** the report is generated, **Then** its tab is still present, shows zero totals and a "No transactions in this period" note.

---

### User Story 2 - Front Pages: Period Dashboard Summary & Lifetime Financials (Priority: P1)

The beginning pages of the report provide the high-level executive and balance sheet context:
1. **Period Summary (Sheet 1)**: Business name, report type (Monthly/Yearly FY), period covered, generation timestamp, generating user, the period's dashboard metrics (expenses, income, net operational balance, financing flows, net cash flow), and a pillar-wise summary reconciling with detail tabs.
2. **Lifetime Financials (Sheet 2)**: All-time cumulative financial position as of the report date, providing the complete balance sheet foundation. Includes lifetime operational P&L (total lifetime revenue, expenses, profit/loss), financing pool equity (cumulative capital introduced, drawings, net capital pool), debt pool (cumulative borrowings, repayments, outstanding loans balance), net overall cash position, lifetime category distributions, and a facility-by-facility loan status table.

**Why this priority**: Accountants need both the period-specific movements (P&L and cash flow for the month/FY) and the all-time cumulative financial position (for complete balance sheet preparation).

**Independent Test**: Verify the first two tabs in the workbook: the Period Summary reconciles with the period detail tabs and the Lifetime Financials tab matches the dashboard's Global Lifetime Totals.

**Acceptance Scenarios**:

1. **Given** any monthly or yearly report generation, **When** the workbook is opened, **Then** the first tab displays the Period Summary and the second tab displays the Lifetime Financials overview.
2. **Given** the Lifetime Financials tab, **When** reviewed, **Then** it shows all-time revenue, expenses, net profit, net capital pool, outstanding loan pool, net cash position, category summaries, and the active loan pool status.
3. **Given** the front page period totals, **When** compared to each pillar tab's top totals, **Then** they match exactly.
4. **Given** loans exist, **When** the report is generated, **Then** the front page shows opening outstanding, borrowed, repaid and closing outstanding principal for the period, and the Lifetime Financials shows total cumulative outstanding balances.

---

### User Story 3 - Pillar Tab Layout: Totals on Top, Detail Below (Priority: P1)

Each pillar tab starts with a totals block (record count, total amount, breakdowns by category/type/payment mode as relevant), then below it the period/time frame and the detailed transaction list with descriptions (date, reference, category/sub-category or type, description, amount, payment mode, status, submitted/approved by, attachment presence).

**Why this priority**: Explicit user layout requirement and needed for audit traceability.

**Independent Test**: Open any pillar tab; totals appear at the top, followed by the time frame and a row per transaction.

**Acceptance Scenarios**:

1. **Given** any pillar tab, **When** opened, **Then** totals are first, then time frame and descriptions, then the detailed rows.
2. **Given** only approved records should count, **When** the report is generated, **Then** only approved/finalized records are included in totals and detail rows.
3. **Given** long tables, **When** scrolled, **Then** header rows stay visible and columns are filterable.

---

### User Story 4 - CSV Alternative (Priority: P2)

A user can choose CSV instead of Excel. CSV output provides separate files for Lifetime Financials and Period Summary plus one file per pillar (6 CSV files total), delivered together in a single zip archive.

**Why this priority**: Some accounting tools import only CSV; Excel covers the main need.

**Independent Test**: Download CSV for a period and confirm the archive has summary + four pillar files that open correctly in a spreadsheet application with ₹ and non-English text intact.

**Acceptance Scenarios**:

1. **Given** CSV is selected, **When** downloaded, **Then** the user gets one archive containing a summary file and four pillar files.
2. **Given** descriptions contain commas, quotes or line breaks, **When** opened in a spreadsheet tool, **Then** columns remain intact.

---

### User Story 5 - Modern, Polished Appearance (Priority: P2)

The Excel workbook looks professional: consistent branded colour scheme, styled title banners, distinct header rows, zebra striping, right-aligned currency with Indian digit grouping (₹), date formatting, sensible column widths, coloured pillar tabs, and print-ready settings. The Reports panel on the dashboard is clean and matches the existing UI.

**Why this priority**: Requested but does not change the data.

**Independent Test**: Visual review of the workbook and print preview on A4 landscape.

**Acceptance Scenarios**:

1. **Given** a generated workbook, **When** opened, **Then** each tab has a banner title, styled headers, formatted currency/dates and a distinct tab colour.
2. **Given** print preview, **When** viewed, **Then** each sheet fits page width with repeated header rows.

---

### Edge Cases

- Period with no data in any pillar: report still generates with zero totals.
- Records in DRAFT/SUBMITTED/REJECTED/QUERIES_RAISED status: excluded from totals; handling in the report is stated in Assumptions.
- Transactions on period boundaries (first/last day, midnight) belong to exactly one period.
- Loans spanning periods: opening and closing balances reflect activity before and within the period.
- Very large periods (tens of thousands of rows): generation completes without timeout or data loss.
- Cell values beginning with `=`, `+`, `-` or `@` must not execute as formulas in the file (spreadsheet injection).
- Descriptions with special characters/emoji or non-English text are preserved.
- Unauthorized users cannot access or request report downloads.
- Future periods are rejected with a clear message.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The dashboard page itself MUST contain a Reports panel (not a separate page or navigation entry) where users select report type (Monthly or Yearly), the period (month+year or financial year), and format (Excel or CSV).
- **FR-002**: The Excel report MUST be a single workbook structured with beginning overview tabs (Period Summary and Lifetime Financials) followed by separate detailed tabs for each pillar (Expenses, Income, Capital, Loans).
- **FR-002b**: The Lifetime Financials tab MUST present all-time cumulative figures as of the generation date: lifetime operational totals (revenue, expenses, net balance), equity pool (introduced, drawings, net capital), debt pool (borrowed, repaid, total outstanding), net cash position, all-time category breakdowns, and a loan facility status table.
- **FR-003**: The front sheet MUST show business name, report type, period covered, generation timestamp, generating user, and dashboard figures for the period.
- **FR-004**: The front sheet MUST include a pillar-wise summary reconciling exactly with each pillar tab's totals, and a consolidated view that supports preparing a balance sheet and profit & loss (total income, total expenses, net result, capital in/out, loans borrowed/repaid/outstanding).
- **FR-005**: Each pillar tab MUST place a totals block at the top, followed by time frame and descriptions, followed by the detailed transaction list.
- **FR-006**: Expenses tab MUST include date, reference, category, sub-category, description, amount, payment mode, status, submitter, approver and attachment indicator, with totals by category.
- **FR-007**: Income tab MUST include equivalent fields with totals by income source/type.
- **FR-008**: Capital tab MUST include contributions and withdrawals with totals per type and net capital.
- **FR-009**: Loans tab MUST include per-loan details (lender, facility, status), borrowings and repayments in the period, and opening/closing outstanding principal.
- **FR-010**: Yearly reports MUST cover the financial year (1 April – 31 March), labelled e.g. "FY 2025-26", and include a month-wise breakdown (April to March) per pillar in the totals area.
- **FR-011**: Reports MUST include only approved/finalized records in totals and detail lists; consistent with the dashboard.
- **FR-012**: All monetary values MUST be stored as numbers (not text) in Excel so accountants can sum/pivot; shown in ₹ with Indian grouping and two decimals.
- **FR-013**: Excel sheets MUST have frozen header rows, filters on detail tables, and consistent professional styling with coloured pillar tabs.
- **FR-014**: Excel sheets MUST be print-ready (landscape, fit to page width, repeated headers, page numbers and footer with period and generation date).
- **FR-015**: CSV export MUST deliver a single archive with lifetime financials, period summary, and one file per pillar (6 CSV files total), maintaining column consistency and handling special characters.
- **FR-016**: Report access MUST be limited to authorized roles (Admin, Manager, Accountant); others MUST NOT see the section nor access download endpoints.
- **FR-017**: Cell content MUST be sanitized to prevent formula injection.
- **FR-018**: Each report generation MUST be logged with user, period, format and outcome.
- **FR-019**: Generated files MUST have a descriptive name including business, report type, period and format.
- **FR-020**: Periods with no data MUST still produce a valid report with zero totals; future periods MUST be rejected.

### Key Entities

- **Report Request**: report type (monthly/yearly), period, format, requesting user, timestamp.
- **Report Summary**: consolidated period figures per pillar and overall.
- **Pillar Section**: totals block, time frame, detailed transaction rows for one pillar (Expenses, Income, Capital, Loans).
- **Existing entities**: Expense, Income, Capital Transaction, Loan and Loan Transaction, with their categories, attachments and approval status.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can generate and download a monthly or yearly report in under 30 seconds of interaction (within 3 clicks from the dashboard).
- **SC-002**: 100% of totals on the front page equal the sum of the detail rows on the corresponding tabs and match the dashboard for the same period.
- **SC-003**: Reports for up to 50,000 transactions in a period are generated in under 60 seconds.
- **SC-004**: An accountant can prepare a balance sheet and profit & loss from the file without asking the business for additional data in at least 90% of review sessions.
- **SC-005**: 100% of monetary cells can be summed directly without conversion.
- **SC-006**: 90% of first-time users rate the report layout as clear and professional.
- **SC-007**: 0 unauthorized successful report downloads.

## Assumptions

- Authorized roles are Admin and Accountant (Supervisor also sees the dashboard but is excluded from reports); Managers are redirected away from the dashboard today and so have no access.
- Only approved/finalized records are reported; pending/rejected records are not included in totals (an optional separate "excluded records" count on the front page may be shown).
- Yearly reports use the Indian financial year (April–March) exclusively; calendar-year reports are out of scope. Monthly reports are labelled with their financial year too.
- Currency is Indian Rupees (₹) with Indian digit grouping.
- The Reports panel lives within the existing dashboard page and follows its current look and role-based visibility.
- Reports are generated on demand and downloaded directly; storing history of generated files, emailing, or scheduling is out of scope.
- Attachments themselves are not embedded; only an indicator/reference is included.
- The four pillars are those established in feature 003 (Expenses, Income, Capital, Loans) and data comes from existing records.
- PDF output is out of scope; the accountant derives formal statements from the data.
