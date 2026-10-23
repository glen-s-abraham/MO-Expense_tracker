# Tasks: Production Dump H2 Seed Data

**Input**: Design documents from `/specs/002-prod-dump-h2-seed/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, quickstart.md

**Tests**: Automated tests are included in accordance with Constitution Principle I (Test-Driven and Validated).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create directory structure for automation scripts and classpath seed SQL resources

- [X] T001 Create `scripts/` and `src/main/resources/seed/` directories for dump automation and classpath seed files

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Configure application properties for seed path and force-reset controls in local development

- [X] T002 Add `app.seed.enabled`, `app.seed.location`, and `app.seed.force-reset` configuration defaults in `src/main/resources/application-dev.properties`

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Automated Production Data Extraction & Sanitization Script (Priority: P1) 🎯 MVP

**Goal**: Provide a repeatable shell script that extracts the 9 Expense Tracker tables from the shared production PostgreSQL database (`192.168.1.50:5432/mariasorganicsdb`), strips PostgreSQL-specific syntax, appends H2 identity sequence resets, and writes `src/main/resources/seed/prod-seed.sql`.

**Independent Test**: Run `./scripts/dump-prod-to-seed.sh` and verify `src/main/resources/seed/prod-seed.sql` is generated with all 9 tables' data and `ALTER TABLE ... ALTER COLUMN id RESTART WITH ...` statements.

### Implementation for User Story 1

- [X] T003 [US1] Create production dump and H2 sanitization script in `scripts/dump-prod-to-seed.sh`
- [X] T004 [US1] Execute `scripts/dump-prod-to-seed.sh` against production PostgreSQL to generate `src/main/resources/seed/prod-seed.sql`

**Checkpoint**: `src/main/resources/seed/prod-seed.sql` is populated with sanitized production data and H2 sequence resets.

---

## Phase 4: User Story 2 - Automatic H2 Seed Data Loading on Application Startup (Priority: P1)

**Goal**: Automatically execute `classpath:seed/prod-seed.sql` into the local H2 database on application startup in non-production profiles when the database is empty (or when `app.seed.force-reset=true`), ensuring dev role users exist and sequences are aligned.

**Independent Test**: Run `DataInitializerTest` and start the application against an empty H2 database to verify all production rows load cleanly and subsequent entity inserts succeed without primary key collisions.

### Tests for User Story 2

- [X] T005 [P] [US2] Create unit and H2 integration tests for production seed loading, sequence alignment, idempotency, force-reset, and prod profile guard in `src/test/java/com/mushroom/expense/config/DataInitializerTest.java`

### Implementation for User Story 2

- [X] T006 [US2] Update `src/main/java/com/mushroom/expense/config/DataInitializer.java` to load `classpath:seed/prod-seed.sql` via `ResourceDatabasePopulator` on startup in non-prod profiles, support `app.seed.force-reset`, and ensure dev fallback users

**Checkpoint**: Application loads production dump automatically into H2 on startup and all tests in `DataInitializerTest` pass.

---

## Phase 5: User Story 3 - Local Database Reset & Refresh Helper Script (Priority: P2)

**Goal**: Provide a helper script to wipe local file-backed H2 database files (`./data/mushroomdb*`), optionally refresh the dump from production (`--refresh-dump`), and re-seed H2 immediately.

**Independent Test**: Run `./scripts/reset-h2-seed.sh` and verify `./data/mushroomdb.mv.db` is freshly reset and re-seeded with production data on startup.

### Implementation for User Story 3

- [X] T007 [US3] Create local H2 reset and re-seed helper script in `scripts/reset-h2-seed.sh`
- [X] T008 [US3] Run `scripts/reset-h2-seed.sh` and verify local `./data/mushroomdb.mv.db` is freshly seeded with the production dump

**Checkpoint**: All user stories are independently functional and verified.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end validation and test suite verification

- [X] T009 Run `mvn clean verify` to validate all unit tests, integration tests, and JaCoCo coverage gates

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1
- **User Story 1 (Phase 3)**: Depends on Phase 1 - generates `src/main/resources/seed/prod-seed.sql`
- **User Story 2 (Phase 4)**: Depends on Phase 2 and User Story 1 (`prod-seed.sql` available on classpath)
- **User Story 3 (Phase 5)**: Depends on User Story 1 and User Story 2
- **Polish (Phase 6)**: Depends on all phases completing

### Parallel Opportunities

- `T002` and `T003` modify independent files (`application-dev.properties` and `scripts/dump-prod-to-seed.sh`) and can be developed in parallel.
- `T005` (`DataInitializerTest.java`) and `T007` (`scripts/reset-h2-seed.sh`) operate on independent files.

---

## Implementation Strategy

### MVP First (User Story 1 + User Story 2)

1. Complete Phase 1 & Phase 2: Setup directories and properties.
2. Complete Phase 3 (US1): Script and take the production dump into `src/main/resources/seed/prod-seed.sql`.
3. Complete Phase 4 (US2): Load `prod-seed.sql` on H2 startup in `DataInitializer` and verify with `DataInitializerTest`.
4. Complete Phase 5 (US3) & Phase 6: Provide `scripts/reset-h2-seed.sh`, refresh `./data/mushroomdb`, and run `mvn clean verify`.
