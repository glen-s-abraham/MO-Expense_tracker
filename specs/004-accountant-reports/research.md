# Research: Accountant Reports Export

## R1. Excel generation library
- **Decision**: Apache POI `poi-ooxml` using `SXSSFWorkbook` (streaming).
- **Rationale**: De-facto Java standard; supports styles, number formats, freeze panes, auto-filter, tab colours, print setup, footers. Streaming keeps memory low for 50k rows.
- **Alternatives**: FastExcel (lighter, weaker styling/print support); JExcel (obsolete); CSV only (fails "modern and pretty").
- **Note**: SXSSF cannot auto-size columns after flush; use explicit column widths. The totals/summary blocks are written first, so no random access to flushed rows is needed.

## R2. CSV delivery
- **Decision**: Single `.zip` (JDK `ZipOutputStream`) with `summary.csv` + 4 pillar CSVs, UTF-8 with BOM so Excel renders ₹ and non-English text.
- **Rationale**: Spec requires one download; no new dependency. Hand-rolled RFC 4180 quoting (commas, quotes, newlines).
- **Alternatives**: OpenCSV (extra dependency for trivial need).

## R3. Financial year handling
- **Decision**: Reuse the dashboard's `fy` convention (`"2025-26"`, 1 Apr – 31 Mar) and `year`/`month` params; `FinancialYear` helper derives start/end, label and April–March month index.
- **Rationale**: Reports must reconcile with dashboard numbers; the dashboard already uses this logic.
- **Month labelling**: a month's FY = year if month ≥ April else year−1.

## R4. Data inclusion rule
- **Decision**: Approved-only (`ExpenseStatus.APPROVED` / `IncomeStatus.APPROVED`), same as dashboard queries; the front page shows a count of excluded non-approved records in the period.
- **Rationale**: FR-011, SC-002.

## R5. Loan balances
- **Decision**: Opening outstanding = approved borrowed − repaid before period start; closing = opening + borrowed − repaid in period; computed per loan via range sums on `LoanTransactionRepository` (new before-date sum query).
- **Alternatives**: Use current outstanding from `LoanService` (rejected: not period-aware).

## R6. Capital balance
- **Decision**: Net capital = introduced − drawings for period; opening/closing computed the same way as loans, so accountants can build the balance sheet equity section.

## R7. Tax data
- **Decision**: Expense and Income rows include `taxPercentage` and computed tax component (amount × pct / (100 + pct) if the stored amount is tax-inclusive; to be confirmed against how the app stores amounts — default: show the percentage and derived tax amount columns). Totals include total tax.
- **Rationale**: Taxation is a stated purpose; entities already hold `taxPercentage`.

## R8. Authorization
- **Decision**: ADMIN and ACCOUNTANT only. The dashboard redirects MANAGER away, and Supervisor is excluded per spec assumption. Enforce with `@PreAuthorize` and hide the panel via `sec:authorize`.

## R9. Formula injection
- **Decision**: Prefix text cells starting with `=`, `+`, `-`, `@`, tab or CR with `'`, and write text via string cells only. Numeric cells are real numbers.

## R10. UI placement
- **Decision**: A compact "Reports" card in the existing Monthly and Yearly panes (inside `dashboard.html`); a small form (GET) with format select (Excel/CSV) and a download button, hidden fields carry the pane's current month/year or FY. No new page or nav item.

## R11. Lifetime Financials in Beginning Pages
- **Decision**: Include a dedicated "Lifetime Financials" tab as Sheet 2 (right after Sheet 1 "Period Summary") in the Excel workbook, and as `00_Lifetime_Financials.csv` in the CSV archive.
- **Rationale**: Meets user requirement for lifetime financials tabbed at the beginning of the report. Provides accountants with cumulative figures essential for balance sheet preparation (all-time equity/drawings, cumulative debt, all-time operating surplus/deficit, and net cash position) without having to manually aggregate past period reports.
- **Source of Data**: Reuses the exact same calculation patterns as Section 1 ("Global Lifetime Totals") in `DashboardController`: `expenseRepository.findSumByCategory(ExpenseStatus.APPROVED)`, `incomeRepository.findSumByCategory(IncomeStatus.APPROVED)`, `capitalService.getTotalApprovedIntroduced()`, `capitalService.getTotalApprovedDrawings()`, `capitalService.getNetCapitalPool()`, `loanService.getTotalApprovedBorrowed()`, `loanService.getTotalApprovedRepaid()`, `loanService.getOutstandingLoanPool()`, and `loanService.getAllLoanSummaries()`.
