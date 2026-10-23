# Quickstart: Validating Accountant Reports

## Prerequisites
- Java 21, Maven; app running locally with H2 seed data (`mvn spring-boot:run`) or tests via `mvn test`.
- Users: an ADMIN or ACCOUNTANT, plus a SUPERVISOR for negative checks.

## Automated
```bash
mvn clean verify
```
Expect all new tests in `ReportServiceTest`, `FinancialYearTest`, `ExcelReportWriterTest`, `CsvReportWriterTest`, `CellSanitizerTest`, `ReportControllerTest` to pass with Jacoco gates intact.

## Manual scenarios
1. **Monthly Excel**: log in as accountant → Dashboard → Monthly Snapshot → Reports card → Excel → download. Verify: Beginning tabs (Period Summary + Lifetime Financials) + 4 pillar tabs, totals on top, figures equal dashboard cards.
2. **Yearly FY**: Yearly Snapshot (FY) pane → download; verify label `FY 2025-26`, Apr–Mar breakdown, totals equal dashboard FY cards.
3. **Reconciliation**: Summary pillar table totals == each tab totals == sum of detail rows.
4. **CSV**: choose CSV → zip with 6 files (00_Lifetime_Financials.csv, 01_Period_Summary.csv + 4 pillar files); open in a spreadsheet, check ₹/special characters and commas in descriptions.
5. **Empty period**: choose a period without data → valid workbook with zero totals and "No transactions" notes.
6. **Security**: as supervisor/manager the panel is absent and `/reports/download` returns 403/redirect; anonymous → login.
7. **Injection**: add an expense description `=1+1`; the workbook shows it as literal text.
8. **Print**: print preview shows landscape, headers repeated, footer with period.

See [contracts/reports-endpoints.md](contracts/reports-endpoints.md) and [data-model.md](data-model.md).
