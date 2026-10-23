# Feature Specification: Production Dump H2 Seed Data

**Feature Branch**: `[002-prod-dump-h2-seed]`

**Created**: 2026-10-22

**Status**: Approved

**Input**: User description: "use the speckit tools and take a production sump and load the same to our h2 db on application load as seed data. and script whatever"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Automated Production Data Extraction & Sanitization Script (Priority: P1)

As a developer, I want an automated script that extracts only the Expense Tracker application's tables from the shared production database and converts the dump into a clean, H2-compatible seed file, so that I can refresh local seed data with a single command whenever needed.

**Why this priority**: Taking a reliable, repeatable dump of the production data without pulling unrelated tables from the shared database is the prerequisite for loading real data locally.

**Independent Test**: Can be fully tested by executing the dump script and verifying that the generated SQL seed file exists, contains all 9 Expense Tracker tables (`categories`, `sub_categories`, `users`, `expenses`, `expense_attachments`, `expense_comments`, `incomes`, `income_attachments`, `income_comments`), excludes non-application tables (`bill_*`, `it_*`, `sk_*`), and contains valid SQL syntax with identity sequence resets.

**Acceptance Scenarios**:

1. **Given** connectivity to the production database, **When** the developer runs the dump script, **Then** a sanitized SQL seed file is generated in the project's resources directory containing all rows from the 9 application tables in foreign-key dependency order.
2. **Given** a generated seed file, **When** inspected, **Then** database-specific directives that are incompatible with local embedded storage are stripped and identity sequence restart statements are appended for all tables.

---

### User Story 2 - Automatic H2 Seed Data Loading on Application Startup (Priority: P1)

As a developer running the application in local/development mode, I want the H2 database to automatically load the production dump as seed data on application startup (when the database is empty or reset), so that I can immediately work with realistic categories, users, incomes, and expenses without manual database setup.

**Why this priority**: Automatically seeding the local database on startup delivers the core developer experience improvement requested by the user.

**Independent Test**: Can be fully tested by starting the application in the `dev` profile against a fresh H2 database (or with a reset script/flag) and verifying that all production users, categories, subcategories, expenses, incomes, attachments, and comments are present and that new records can be inserted without primary key collisions.

**Acceptance Scenarios**:

1. **Given** an empty local H2 database in the `dev` profile, **When** the Spring Boot application starts up, **Then** the production seed SQL is automatically executed, populating all 9 tables with the production snapshot data.
2. **Given** an already-populated local H2 database in the `dev` profile, **When** the application restarts, **Then** the existing local data is preserved without duplicate key errors, unless an explicit reset is requested.
3. **Given** a local H2 database seeded from the production dump, **When** a user creates a new expense, income, category, or user in the application, **Then** the new record is assigned the next available ID without any primary key or sequence collision.
4. **Given** the application running in the `prod` profile, **When** the application starts up, **Then** the H2 seed script is never executed against the production database.

---

### User Story 3 - Local Database Reset & Refresh Helper Script (Priority: P2)

As a developer, I want a helper script to optionally pull a fresh production dump, wipe the local H2 database files, and start/prepare the application with clean production seed data, so that resetting my local environment is effortless.

**Why this priority**: Since the local H2 database is file-backed (`./data/mushroomdb`), developers need a convenient one-step script to clear stale local H2 files and re-seed from the production snapshot.

**Independent Test**: Can be fully tested by running the reset/refresh script and verifying that existing `./data/mushroomdb*` files are removed and the database is freshly seeded on next startup.

**Acceptance Scenarios**:

1. **Given** an existing local H2 database file in `./data/`, **When** the developer runs the reset/refresh script, **Then** the local H2 database files are cleaned up and the latest production seed data is ready to be loaded on startup.

### Edge Cases

- What happens when the local H2 database already has data from a previous run? The initializer checks if data already exists (e.g., `userRepository.count() > 0` or `categoryRepository.count() > 0`) before executing the seed script, preventing duplicate primary key violations on normal restarts (unless `app.db.seed-reset=true` is configured).
- What happens when new records are created after seeding explicit IDs? Identity columns in H2 must have their sequences explicitly restarted past `MAX(id)` for every table so subsequent JPA `IDENTITY` inserts succeed.
- What happens if no production dump seed file is found on the classpath? The initializer falls back gracefully to the default minimal sample data so tests and offline environments never fail.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST provide an executable shell script (`scripts/dump-prod-to-seed.sh`) that connects to the production PostgreSQL database and exports data from the 9 application tables (`categories`, `sub_categories`, `users`, `expenses`, `expense_attachments`, `expense_comments`, `incomes`, `income_attachments`, `income_comments`).
- **FR-002**: The dump script MUST sanitize the exported SQL so it is 100% compatible with H2 (removing PostgreSQL-specific `SET`, `pg_catalog`, `\restrict`/`\unrestrict` statements and `public.` schema prefixes if needed, and appending `ALTER TABLE ... ALTER COLUMN id RESTART WITH ...` statements).
- **FR-003**: System MUST store the sanitized production dump at `src/main/resources/seed/prod-seed.sql` so it is bundled on the application classpath.
- **FR-004**: On application startup in non-production profiles (`!prod`), `DataInitializer` MUST automatically load `classpath:seed/prod-seed.sql` into the H2 database when the database is empty (or when a configurable reset property `app.seed.force-reset=true` is enabled).
- **FR-005**: System MUST provide a convenience script (`scripts/reset-h2-seed.sh`) to wipe local H2 database files (`./data/mushroomdb*`) and optionally refresh the dump from production.
- **FR-006**: System MUST ensure default dev test accounts (`manager`, `accountant`, `supervisor`) remain available or are ensured after seeding if not present in the dump, and `admin` / production users can log in locally.

### Key Entities

- **Users (`users`)**: Application users and roles (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_ACCOUNTANT`, `ROLE_SUPERVISOR`).
- **Categories & SubCategories (`categories`, `sub_categories`)**: Expense and Income classification hierarchy.
- **Expenses (`expenses`, `expense_attachments`, `expense_comments`)**: Expense records, file attachment metadata, and review comments.
- **Incomes (`incomes`, `income_attachments`, `income_comments`)**: Income records, file attachment metadata, and review comments.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of rows across all 9 application tables in the production snapshot are loaded into the local H2 database on initial startup in under 5 seconds.
- **SC-002**: 0 primary key or sequence collision errors occur when inserting new entities into any of the 9 tables after seeding.
- **SC-003**: 100% of existing and new automated unit/integration tests pass (`mvn clean verify`).

## Assumptions

- Production PostgreSQL connection defaults match `application-prod.properties` (`192.168.1.50:5432/mariasorganicsdb`, user `root`), overridable via environment variables (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`).
- Only tables belonging to the Mushroom Farm Expense Tracker domain should be exported from the shared `mariasorganicsdb` database.
- Local H2 database files reside in `./data/mushroomdb.mv.db` as configured in `application-dev.properties`.
