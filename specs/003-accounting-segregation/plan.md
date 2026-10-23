# Implementation Plan: 4-Pillar Accounting Segregation (Expenses, Income, Capital & Loans)

**Branch**: `[003-accounting-segregation]` | **Date**: 2026-10-22 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/003-accounting-segregation/spec.md`

## Summary

Segregate balance-sheet equity (`Capital`) and debt (`Loans`) from operational revenue (`Income`) while keeping `Expenses` as the core operational expense module. Implement dedicated JPA entities, repositories, specifications, services, controllers, and Thymeleaf views for `/capital` (with **Capital Introduced** and **Drawings** tabs) and `/loans` (with individual `Loan` facility tracking, **Loans Inward** tab, and **Loan Repayments** tab with over-repayment validation). Add optional `SubCategory` support to `Income`, execute an automatic transactional startup migration (`AccountingMigrationService`) that moves the 7 `Capital Introduced` records (₹1,90,000) and 1 `Loan` record (₹1,70,000) out of `incomes` with 100% checksum verification, and update `/dashboard` across **Lifetime**, **Monthly Snapshot**, and **Yearly Snapshot (FY)** views with expandable modal drilldowns for **Net Capital Pool** and **Outstanding Loan Pool**.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.2.0 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, `thymeleaf-extras-springsecurity6`, Apache POI 5.2.5)

**Storage**: H2 Database (local dev/test) and PostgreSQL (production) via Spring Data JPA / Hibernate 6.3.1

**Testing**: JUnit 5, Spring Boot Test (`@SpringBootTest`, `@WebMvcTest`), Spring Security Test, JaCoCo coverage gate (`mvn clean verify`)

**Target Platform**: Linux server / JVM 21

**Project Type**: Spring Boot MVC Web Application (Thymeleaf + Bootstrap 5 + Chart.js)

**Performance Goals**: Dashboard and ledger views render in <200ms locally; startup migration completes in <500ms

**Constraints**: Zero data loss during migration (`₹1,90,000 + ₹1,70,000 == ₹3,60,000` checksum enforced in `@Transactional` boundary); compatible with both H2 and PostgreSQL; strict role-based security

**Scale/Scope**: 4 accounting pillars (`Expenses`, `Income`, `Capital`, `Loans`), 6 new JPA entities (`CapitalTransaction`, `CapitalComment`, `CapitalAttachment`, `Loan`, `LoanTransaction`, `LoanComment`, `LoanAttachment`), 2 new controllers/services, updated Dashboard (Lifetime + Monthly + Yearly FY + Pool drilldown modals) and Approvals

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Test-Driven and Validated**: PASS — Unit and integration tests will be added for `AccountingMigrationService`, `CapitalService`, `LoanService`, `CapitalController`, `LoanController`, `DashboardController`, and `DataInitializerTest`. `mvn clean verify` + JaCoCo will be verified.
- **II. Secure by Default**: PASS — All `/capital/**` and `/loans/**` endpoints use `@PreAuthorize` matching existing role rules (`USER`, `SUPERVISOR`, `ACCOUNTANT`, `MANAGER`, `ADMIN`), and Thymeleaf templates use `sec:authorize`.
- **III. Separation of Concerns**: PASS — Strict separation across `model`, `repository`, `service`, `controller`, and `templates`.
- **IV. Observability and Debuggability**: PASS — SLF4J structured logging in `AccountingMigrationService`, `CapitalService`, and `LoanService`.
- **V. Relational Data Integrity**: PASS — Standard JPA entities compatible with both H2 and PostgreSQL; transactional migration with mathematical checksum verification before deleting migrated rows from `incomes`.

## Project Structure

### Documentation (this feature)

```text
specs/003-accounting-segregation/
├── plan.md              # Implementation plan
├── research.md          # Phase 0 architectural & domain decisions
├── data-model.md        # Phase 1 entity & schema definitions
├── quickstart.md        # Phase 1 verification & run guide
├── contracts/           # Phase 1 web & REST endpoint contracts
│   └── web-routes.md
└── tasks.md             # Phase 2 actionable task list
```

### Source Code (repository root)

```text
src/main/java/com/mushroom/expense/
├── config/
│   └── DataInitializer.java
├── controller/
│   ├── AdminController.java
│   ├── ApiRestController.java
│   ├── CapitalController.java
│   ├── DashboardController.java
│   ├── DashboardRestController.java
│   ├── ExpenseController.java
│   ├── IncomeController.java
│   └── LoanController.java
├── model/
│   ├── CapitalAttachment.java
│   ├── CapitalComment.java
│   ├── CapitalTransaction.java
│   ├── CapitalTransactionType.java
│   ├── Income.java
│   ├── Loan.java
│   ├── LoanAttachment.java
│   ├── LoanComment.java
│   ├── LoanStatus.java
│   ├── LoanTransaction.java
│   └── LoanTransactionType.java
├── repository/
│   ├── CapitalAttachmentRepository.java
│   ├── CapitalCommentRepository.java
│   ├── CapitalSpecification.java
│   ├── CapitalTransactionRepository.java
│   ├── IncomeRepository.java
│   ├── IncomeSpecification.java
│   ├── LoanAttachmentRepository.java
│   ├── LoanCommentRepository.java
│   ├── LoanRepository.java
│   ├── LoanSpecification.java
│   └── LoanTransactionRepository.java
└── service/
    ├── AccountingMigrationService.java
    ├── CapitalService.java
    ├── ExcelExportService.java
    ├── IncomeService.java
    └── LoanService.java

src/main/resources/templates/
├── layout.html
├── dashboard.html
├── income_dashboard.html
├── income_form.html
├── income_view.html
├── accountant/approvals.html
├── supervisor/approvals.html
├── capital/
│   ├── dashboard.html
│   ├── form.html
│   └── view.html
└── loan/
    ├── dashboard.html
    ├── form.html
    └── view.html

src/test/java/com/mushroom/expense/
├── config/DataInitializerTest.java
├── controller/CapitalControllerTest.java
├── controller/LoanControllerTest.java
└── service/
    ├── AccountingMigrationServiceTest.java
    ├── CapitalServiceTest.java
    └── LoanServiceTest.java
```

**Structure Decision**: Standard single-module Spring Boot MVC layout matching existing `com.mushroom.expense` packages.
