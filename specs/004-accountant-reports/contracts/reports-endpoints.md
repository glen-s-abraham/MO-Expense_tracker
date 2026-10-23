# Contract: Report Endpoints

## GET `/reports/download`
Authorization: `ROLE_ADMIN` or `ROLE_ACCOUNTANT`; others receive 403, anonymous users are redirected to login.

| Param | Required | Values |
|---|---|---|
| type | yes | `MONTHLY` \| `YEARLY` |
| fy | yes | `YYYY-YY`, e.g. `2025-26` |
| month | monthly | 1–12 (calendar month inside the FY) |
| year | monthly | calendar year of the month |
| format | yes | `XLSX` \| `CSV` |

### Responses
- **200 XLSX**: `Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, `Content-Disposition: attachment; filename="MushroomFarm_Monthly_Oct-2025_FY2025-26.xlsx"`
- **200 CSV**: `Content-Type: application/zip`, filename `MushroomFarm_Yearly_FY2025-26_CSV.zip` containing `00_Lifetime_Financials.csv`, `01_Period_Summary.csv`, `02_Expenses.csv`, `03_Income.csv`, `04_Capital.csv`, `05_Loans.csv`.
- **400**: invalid/missing parameters, month outside FY, or future period (plain error message / redirect to dashboard with flash message).
- **403**: role not permitted.

### Workbook structure (Beginning Overview Tabs + 4 Pillar Tabs)
| Sheet Index | Sheet Name | Tab Colour | Content Order |
|---|---|---|---|
| 1 | Period Summary | Brand Purple/Navy | Banner, report metadata, period dashboard figures, pillar reconciliation table, period balance-sheet support |
| 2 | Lifetime Financials | Slate Blue | Lifetime overview banner (as of report date), operational P&L, financing equity pool, debt pool, net cash position, all-time category breakdowns, loan facility summary table |
| 3 | Expenses | Coral Red | Totals on top (by category, total tax) → period time frame & descriptions → detail transaction table |
| 4 | Income | Emerald Green | Totals on top (by category, total tax) → period time frame & descriptions → detail transaction table |
| 5 | Capital | Royal Blue | Totals on top (by type & owner) → period time frame & descriptions → detail transaction table |
| 6 | Loans | Amber Gold | Totals on top (borrowings & repayments in period) → period time frame & descriptions → detail transaction table + period loan facility status |

Frozen header on detail tables, auto-filter, ₹ Indian-grouped number format, landscape/fit-to-width print, footer with period + page numbers.

## UI contract (dashboard.html)
Reports card in the Monthly and Yearly panes, visible only with `sec:authorize="hasAnyRole('ADMIN','ACCOUNTANT')"`; GET form posting to `/reports/download` with hidden `type`, `fy`, `month`, `year` from the pane's selection and a `format` select.
