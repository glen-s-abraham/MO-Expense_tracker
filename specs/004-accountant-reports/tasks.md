# Tasks: Accountant Reports Export (Monthly & Yearly)

**Input**: Design documents from `/specs/004-accountant-reports/`
**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/reports-endpoints.md](contracts/reports-endpoints.md), [quickstart.md](quickstart.md)
**Constitution Compliance**: Automated unit and integration tests are mandatory per Principle I (JUnit 5 + MockMvc, Jacoco coverage).

## Format: `- [X] [TaskID] [P?] [Story?] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Maps to user stories [US1]..[US5] from spec.md
- File paths are relative to repository root

---

## Phase 1: Setup (Dependencies & Infrastructure)

**Purpose**: Add required Excel generation library and configure baseline utilities.

- [X] T001 Add Apache POI `poi-ooxml` dependency to pom.xml
- [X] T002 [P] Implement formula injection guard `CellSanitizer` in src/main/java/com/mushroom/expense/service/report/CellSanitizer.java
- [X] T003 [P] Add unit test for `CellSanitizerTest` in src/test/java/com/mushroom/expense/service/report/CellSanitizerTest.java

---

## Phase 2: Foundational (Date & Financial Year Resolution, Data Models, Repositories)

**Purpose**: Core data models, period helpers, and repository queries required by all stories.

**⚠️ CRITICAL**: Must complete before user story implementations.

- [X] T004 Implement `FinancialYear` helper for Indian FY (Apr-Mar) date math and month labeling in src/main/java/com/mushroom/expense/service/report/FinancialYear.java
- [X] T005 [P] Create unit test for `FinancialYearTest` in src/test/java/com/mushroom/expense/service/report/FinancialYearTest.java
- [X] T006 [P] Create report DTO models (`ReportRequest`, `ReportSummary`, `LifetimeFinancials`, `PillarSection`, `ReportRow`, `LoanReportSummary`) in src/main/java/com/mushroom/expense/service/report/ReportData.java
- [X] T007 Add required date-range and before-date transaction query methods in src/main/java/com/mushroom/expense/repository/ExpenseRepository.java and src/main/java/com/mushroom/expense/repository/IncomeRepository.java
- [X] T008 Add date-range, before-date, and status query methods in src/main/java/com/mushroom/expense/repository/CapitalTransactionRepository.java and src/main/java/com/mushroom/expense/repository/LoanTransactionRepository.java

**Checkpoint**: Foundation ready - period math, models, and repository queries in place.

---

## Phase 3: User Story 1 - Download a Yearly/Monthly Audit Workbook (Priority: P1) 🎯 MVP

**Goal**: Deliver a functioning Excel report generation and streaming download endpoint accessible to Admin and Accountant roles.

**Independent Test**: Request `GET /reports/download?type=MONTHLY&fy=2025-26&year=2025&month=10&format=XLSX` as an Accountant; verify an Excel file is returned with HTTP 200 and attachment header.

### Tests for User Story 1
- [X] T009 [P] [US1] Unit test for ReportService period validation and data aggregation in src/test/java/com/mushroom/expense/service/ReportServiceTest.java
- [X] T010 [P] [US1] WebMvc integration test for `ReportControllerTest` authorization and download endpoint in src/test/java/com/mushroom/expense/controller/ReportControllerTest.java

### Implementation for User Story 1
- [X] T011 [US1] Implement `ReportService` for compiling period-based report data from repositories in src/main/java/com/mushroom/expense/service/ReportService.java
- [X] T012 [US1] Implement baseline `ExcelReportWriter` workbook scaffolding with tab creation in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T013 [US1] Implement `ReportController` with role protection (`@PreAuthorize("hasAnyRole('ADMIN', 'ACCOUNTANT')")`) in src/main/java/com/mushroom/expense/controller/ReportController.java
- [X] T014 [US1] Add structured SLF4J audit logging for report downloads in src/main/java/com/mushroom/expense/controller/ReportController.java

**Checkpoint**: User Story 1 operational - authorized users can download an initial multi-tab Excel file.

---

## Phase 4: User Story 2 - Front Pages: Period Dashboard Summary & Lifetime Financials (Priority: P1)

**Goal**: Generate the two beginning sheets: Sheet 1 (Period Dashboard Summary with reconciliation) and Sheet 2 (Lifetime Financials with operational P&L, equity pool, loan pool, and category distributions).

**Independent Test**: Verify Sheet 1 matches the dashboard cards for the period and Sheet 2 matches the dashboard Global Lifetime Totals.

### Tests for User Story 2
- [X] T015 [P] [US2] Unit test verifying Period Summary & Lifetime Financials data compilation in src/test/java/com/mushroom/expense/service/ReportServiceTest.java
- [X] T016 [P] [US2] Integration test validating Sheet 1 and Sheet 2 Excel workbook output in src/test/java/com/mushroom/expense/service/report/ExcelReportWriterTest.java

### Implementation for User Story 2
- [X] T017 [US2] Integrate `CapitalService`, `LoanService`, and global category aggregation for Lifetime Financials into src/main/java/com/mushroom/expense/service/ReportService.java
- [X] T018 [US2] Implement Sheet 1 (Period Summary) layout and dashboard reconciliation table in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T019 [US2] Implement Sheet 2 (Lifetime Financials) layout with operational P&L, equity pool, debt pool, category breakdowns, and loan facility status in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java

**Checkpoint**: Front overview pages are complete and match dashboard period and lifetime metrics.

---

## Phase 5: User Story 3 - Pillar Tab Layout: Totals on Top, Detail Below (Priority: P1)

**Goal**: Implement the 4 pillar tabs (Expenses, Income, Capital, Loans) with the totals block on top, time frame & descriptions, followed by the itemized detail ledger rows.

**Independent Test**: Open the 4 pillar tabs; verify totals are at rows 1-8, followed by metadata, followed by transaction rows; verify totals equal the sum of rows and match Sheet 1.

### Tests for User Story 3
- [X] T020 [P] [US3] Unit test validating pillar data extraction, monthly breakdown (Apr-Mar), and opening/closing balances in src/test/java/com/mushroom/expense/service/ReportServiceTest.java
- [X] T021 [P] [US3] Excel workbook structure test verifying totals block on top and detail rows below in src/test/java/com/mushroom/expense/service/report/ExcelReportWriterTest.java

### Implementation for User Story 3
- [X] T022 [US3] Implement Expenses tab (category summary block on top, Apr-Mar monthly breakdown for FY, detail rows below) in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T023 [US3] Implement Income tab (income source summary block on top, Apr-Mar monthly breakdown for FY, detail rows below) in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T024 [US3] Implement Capital tab (introduced vs drawings summary on top, net capital, detail rows below) in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T025 [US3] Implement Loans tab (borrowings vs repayments, opening/closing balances, facility summary table, detail rows below) in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java

**Checkpoint**: All 4 pillar tabs populated with totals on top and detailed audit transactions below.

---

## Phase 6: User Story 4 - CSV Alternative (Priority: P2)

**Goal**: Allow users to download a ZIP file containing 6 CSVs (`00_Lifetime_Financials.csv`, `01_Period_Summary.csv`, `02_Expenses.csv`, `03_Income.csv`, `04_Capital.csv`, `05_Loans.csv`).

**Independent Test**: Request report with `format=CSV`; verify a zip archive is returned containing the 6 valid CSV files with UTF-8 BOM encoding.

### Tests for User Story 4
- [X] T026 [P] [US4] Unit test for `CsvReportWriterTest` verifying CSV zip generation and escaping in src/test/java/com/mushroom/expense/service/report/CsvReportWriterTest.java

### Implementation for User Story 4
- [X] T027 [US4] Implement `CsvReportWriter` generating the 6 CSV files with RFC-4180 quoting and UTF-8 BOM in src/main/java/com/mushroom/expense/service/report/CsvReportWriter.java
- [X] T028 [US4] Wire CSV formatting option into `ReportController` in src/main/java/com/mushroom/expense/controller/ReportController.java

**Checkpoint**: CSV export fully functional and parity-checked with Excel data.

---

## Phase 7: User Story 5 - Modern, Polished Appearance & Dashboard Integration (Priority: P2)

**Goal**: Apply elegant professional styling to the Excel workbook (colors, fonts, borders, Indian rupee currency format `₹ #,##,##0.00`, freeze panes, auto-filters, print setup) and embed the Reports panel directly on the dashboard.

**Independent Test**: View generated Excel workbook (formatted currency, colored tabs, frozen headers) and view Dashboard UI (reports panel visible to Admin/Accountant in both Monthly and Yearly panes).

### Tests for User Story 5
- [X] T029 [P] [US5] Test verifying Excel cell formatting, number format string, tab colors, and freeze panes in src/test/java/com/mushroom/expense/service/report/ExcelReportWriterTest.java
- [X] T030 [P] [US5] Integration test verifying dashboard rendering and role-based visibility of reports panel in src/test/java/com/mushroom/expense/controller/DashboardControllerTest.java

### Implementation for User Story 5
- [X] T031 [US5] Add professional styling, tab colors, freeze panes, auto-filter, and print setup to `ExcelReportWriter.java` in src/main/java/com/mushroom/expense/service/report/ExcelReportWriter.java
- [X] T032 [US5] Embed Reports trigger cards in both Monthly Snapshot and Yearly Snapshot panes in src/main/resources/templates/dashboard.html
- [X] T033 [US5] Secure Reports card in dashboard.html with `sec:authorize="hasAnyRole('ADMIN', 'ACCOUNTANT')"` in src/main/resources/templates/dashboard.html

**Checkpoint**: Workbook is polished and modern; Reports panel is seamlessly embedded in dashboard UI.

---

## Phase 8: Polish, Parity Verification & Quality Gates

**Purpose**: End-to-end data parity verification, error handling, documentation, and Jacoco coverage validation.

- [X] T034 [P] End-to-end integration test validating exact mathematical parity between Dashboard figures and Report figures in src/test/java/com/mushroom/expense/ReportDataParityIntegrationTest.java
- [X] T035 Handle error scenarios (invalid FY, future dates, empty periods) with user-friendly messages in src/main/java/com/mushroom/expense/controller/ReportController.java
- [X] T036 Run `mvn clean verify` and ensure zero failures and Jacoco coverage standards are met
- [X] T037 Validate all quickstart scenarios from specs/004-accountant-reports/quickstart.md

---

## Dependencies & Execution Order

### Phase Dependencies
- **Phase 1 (Setup)**: Can start immediately.
- **Phase 2 (Foundational)**: Depends on Phase 1; blocks all User Stories.
- **Phase 3 (US1 - MVP)**: Depends on Phase 2.
- **Phase 4 (US2 - Front Pages & Lifetime)**: Depends on Phase 3.
- **Phase 5 (US3 - Pillar Tabs)**: Depends on Phase 4.
- **Phase 6 (US4 - CSV Alternative)**: Depends on Phase 4/5.
- **Phase 7 (US5 - Polish & Dashboard UI)**: Depends on Phase 5.
- **Phase 8 (Verification & Parity)**: Depends on all prior phases.

### Parallel Opportunities
- Setup & Foundational: T002, T003, T005, T006 can run in parallel.
- Test tasks across each user story can be written alongside or prior to implementation.
- US4 (CSV) and US5 (Polished styling & UI) can proceed in parallel once data compilation in US2/US3 is established.
