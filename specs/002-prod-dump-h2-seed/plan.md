# Implementation Plan: Production Dump H2 Seed Data

**Branch**: `[002-prod-dump-h2-seed]` | **Date**: 2026-10-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-prod-dump-h2-seed/spec.md`

## Summary

Extract a production database dump of the 9 Mushroom Expense Tracker tables from the shared PostgreSQL production database (`192.168.1.50:5432/mariasorganicsdb`), sanitize the SQL for H2 compatibility (including identity sequence resets), store it at `src/main/resources/seed/prod-seed.sql`, provide automation scripts (`scripts/dump-prod-to-seed.sh` and `scripts/reset-h2-seed.sh`), and update `DataInitializer` so that the H2 database is automatically seeded with the production snapshot on application startup in non-production profiles.

## Technical Context

**Language/Version**: Java 21, Bash (POSIX/GNU tools: `pg_dump`, `sed`, `grep`)

**Primary Dependencies**: Spring Boot 3.2.0 (Spring Data JPA, Spring JDBC `ResourceDatabasePopulator`, Spring Security), H2 Database (local/test), PostgreSQL Driver (prod)

**Storage**: PostgreSQL 14/16 (production source), H2 file-based `./data/mushroomdb` (local dev target) and in-memory H2 (integration tests)

**Testing**: JUnit 5, Mockito, Spring Boot Test, JaCoCo (`mvn clean verify`)

**Target Platform**: Linux development & server environments

**Project Type**: Spring Boot Web Application

**Performance Goals**: Seed execution on local H2 startup completes in < 2 seconds

**Constraints**: Must never overwrite or run seed scripts when `prod` profile is active; must not cause primary key collisions on subsequent H2 inserts or across restarts

**Scale/Scope**: 9 relational tables (~250 total rows in current production snapshot)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **I. Test-Driven and Validated**: PASS — Unit and integration tests will verify `DataInitializer` seeding from `prod-seed.sql`, sequence progression on new inserts, and fallback behavior, maintaining JaCoCo coverage.
- **II. Secure by Default**: PASS — No security routes are weakened; production seed loading is strictly gated to `!prod` profiles.
- **III. Separation of Concerns**: PASS — Seeding logic remains encapsulated in `DataInitializer` (`config` package) and shell scripts in `scripts/`.
- **IV. Observability and Debuggability**: PASS — `DataInitializer` will use SLF4J structured logging to log seed execution status and counts.
- **V. Relational Data Integrity**: PASS — Dump script translates PostgreSQL data into H2-compatible SQL and resets H2 identity sequences to preserve referential and sequence integrity.

## Project Structure

### Documentation (this feature)

```text
specs/002-prod-dump-h2-seed/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md  # Spec quality checklist
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
scripts/
├── dump-prod-to-seed.sh # Connects to prod PostgreSQL, dumps 9 tables, sanitizes for H2
└── reset-h2-seed.sh     # Wipes ./data/mushroomdb* and optionally refreshes dump

src/main/
├── java/com/mushroom/expense/config/
│   └── DataInitializer.java          # Loads classpath:seed/prod-seed.sql into H2 on startup
└── resources/
    ├── application-dev.properties    # Configures H2 and app.seed.* properties
    └── seed/
        └── prod-seed.sql             # Sanitized production data dump for H2

src/test/
└── java/com/mushroom/expense/config/
    └── DataInitializerTest.java      # Tests SQL seed loading, sequence resets, force-reset, and prod guard
```

**Structure Decision**: Standard single-module Spring Boot layout with a dedicated `scripts/` directory for operational shell scripts and `src/main/resources/seed/prod-seed.sql` for classpath bundling.

## Complexity Tracking

No constitution violations.
