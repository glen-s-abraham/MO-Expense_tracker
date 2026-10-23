# Implementation Plan: Accountant Reports Export (Monthly & Yearly)

**Branch**: `004-accountant-reports` | **Date**: 2026-10-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-accountant-reports/spec.md`

## Summary

Add a Reports panel inside the existing dashboard page (in both the Monthly and Yearly (FY) panes, reusing the selected month / FY). It downloads an audit-ready Excel workbook with beginning overview tabs (Period Summary and Lifetime Financials) followed by detailed pillar tabs (Expenses, Income, Capital, Loans), or an equivalent CSV zip archive. A new `ReportService` aggregates approved records per pillar and a `ReportController` streams the file. The workbook is built with Apache POI; the CSV zip uses the JDK only. Layout follows: totals on top, then time frame/description, then detail rows. Figures reuse the same approved-only repository queries as the dashboard so totals reconcile.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.2.0 (Web, Data JPA, Security, Thymeleaf); new: Apache POI `poi-ooxml` (managed version pinned in pom)

**Storage**: PostgreSQL (prod) / H2 (test). No schema changes.

**Testing**: JUnit 5 + Spring Boot Test + MockMvc, Jacoco; POI used to read generated workbooks in tests

**Target Platform**: Linux server (Docker), browser UI

**Project Type**: Web application (server-rendered Thymeleaf monolith)

**Performance Goals**: 50,000 rows report < 60 s (SC-003)

**Constraints**: Memory-safe for large periods (use streaming `SXSSFWorkbook`); no new tables; approved-only data

**Scale/Scope**: Small farm business; 1 panel, 1 controller, 1 service, a few helper classes

## Constitution Check

| Principle | Status |
|---|---|
| I. Test-Driven and Validated | PASS – unit tests for FY/period math and aggregation, MockMvc tests for endpoints/security, workbook content tests |
| II. Secure by Default | PASS – `@PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")` on endpoints; panel gated with `sec:authorize`; formula-injection sanitization |
| III. Separation of Concerns | PASS – Controller → ReportService → existing repositories; writers are separate helper classes; no logic in entities |
| IV. Observability | PASS – SLF4J log per generation (user, period, format, outcome) |
| V. Relational Data Integrity | PASS – read-only, no schema change, portable JPQL |
| Stack constraints | PASS – Thymeleaf only, no SPA; POI is a library addition (justified: no JDK xlsx support) |

Post-design re-check: PASS, no violations.

## Project Structure

### Documentation (this feature)

```text
specs/004-accountant-reports/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── reports-endpoints.md
└── tasks.md             # created by /speckit-tasks
```

### Source Code (repository root)

```text
pom.xml                                              # + poi-ooxml
src/main/java/com/mushroom/expense/
├── controller/ReportController.java                 # GET /reports/download
├── service/ReportService.java                       # period resolution + period & lifetime aggregation
├── service/report/ReportData.java                   # DTOs (period summary, lifetime financials, pillar sections, rows)
├── service/report/FinancialYear.java                # FY/period helper
├── service/report/ExcelReportWriter.java            # styled xlsx (Summary, Lifetime, 4 Pillars)
├── service/report/CsvReportWriter.java              # zip of csv files (Lifetime, Summary, 4 Pillars)
├── service/report/CellSanitizer.java                # formula-injection guard
└── repository/{Expense,Income,CapitalTransaction,LoanTransaction}Repository.java  # + date-range detail queries
src/main/resources/templates/dashboard.html          # Reports panel in both panes
src/test/java/com/mushroom/expense/
├── service/ReportServiceTest.java
├── service/report/{FinancialYearTest,ExcelReportWriterTest,CsvReportWriterTest,CellSanitizerTest}.java
└── controller/ReportControllerTest.java
```

**Structure Decision**: Extend the existing single Spring Boot project following its Controller/Service/Repository layering; report writers live in a `service/report` subpackage.

## Complexity Tracking

No constitution violations.
