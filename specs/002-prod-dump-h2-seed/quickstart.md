# Quickstart: Production Dump & H2 Seed Data

## Prerequisites

- PostgreSQL client tools (`pg_dump`, `psql`) installed (`which pg_dump`).
- Network access to the production PostgreSQL database (`192.168.1.50:5432`, database `mariasorganicsdb`) when refreshing the dump.
- Java 21 and Maven (`mvn`) for building and running the application.

## 1. Take a Fresh Production Dump & Generate H2 Seed SQL

Run the dump script from the repository root:

```bash
./scripts/dump-prod-to-seed.sh
```

You can also override connection parameters via environment variables if needed:

```bash
PGHOST=192.168.1.50 PGPORT=5432 PGDATABASE=mariasorganicsdb PGUSER=root PGPASSWORD=root ./scripts/dump-prod-to-seed.sh
```

**Expected Outcome**:
- `src/main/resources/seed/prod-seed.sql` is generated/updated with all rows from the 9 Expense Tracker tables and H2 identity sequence resets.

## 2. Reset Local H2 Database & Re-Seed (Optional Helper)

To wipe the local `./data/mushroomdb*` files (and optionally pull a fresh production dump with `--refresh-dump`):

```bash
# Wipe local H2 database files so the next app startup seeds from prod-seed.sql
./scripts/reset-h2-seed.sh

# Or refresh the dump from production AND wipe local H2 files in one step:
./scripts/reset-h2-seed.sh --refresh-dump
```

## 3. Start the Application & Verify H2 Seeding

Start the application in the default `dev` profile:

```bash
mvn spring-boot:run
```

Or force a re-seed on startup without manually deleting `./data/mushroomdb*`:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--app.seed.force-reset=true"
```

**Expected Outcome**:
- On startup, `DataInitializer` detects an empty H2 database (or `app.seed.force-reset=true`) and loads `classpath:seed/prod-seed.sql`.
- All 14 categories, 43 sub-categories, 107 expenses, 33 incomes, attachments, comments, and users from production are immediately available in H2.

## 4. Run Automated Tests

```bash
mvn clean verify
```
