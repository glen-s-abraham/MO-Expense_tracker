#!/usr/bin/env bash
# ==============================================================================
# dump-prod-to-seed.sh
# Connects to the production PostgreSQL database, extracts data for the 9
# Mushroom Expense Tracker tables in foreign-key dependency order, sanitizes
# the SQL for H2 compatibility, appends identity sequence resets, and writes
# the result to src/main/resources/seed/prod-seed.sql (or a custom path).
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

PGHOST="${PGHOST:-192.168.1.50}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-mariasorganicsdb}"
PGUSER="${PGUSER:-root}"
export PGPASSWORD="${PGPASSWORD:-root}"

OUTPUT_FILE="${1:-${PROJECT_ROOT}/src/main/resources/seed/prod-seed.sql}"
OUTPUT_DIR="$(dirname "${OUTPUT_FILE}")"

mkdir -p "${OUTPUT_DIR}"

echo "[dump-prod-to-seed] Checking connectivity to ${PGUSER}@${PGHOST}:${PGPORT}/${PGDATABASE}..."
if command -v pg_isready >/dev/null 2>&1; then
    pg_isready -h "${PGHOST}" -p "${PGPORT}" -U "${PGUSER}" -d "${PGDATABASE}" >/dev/null
fi

# Tables in strict parent-to-child foreign-key dependency order
TABLES=(
    "categories"
    "sub_categories"
    "users"
    "expenses"
    "expense_attachments"
    "expense_comments"
    "incomes"
    "income_attachments"
    "income_comments"
    "capital_transactions"
    "capital_attachments"
    "capital_comments"
    "loans"
    "loan_transactions"
    "loan_attachments"
    "loan_comments"
)

TABLE_ARGS=()
for tbl in "${TABLES[@]}"; do
    TABLE_ARGS+=("-t" "public.${tbl}")
done

TMP_RAW="$(mktemp)"
TMP_BODY="$(mktemp)"
trap 'rm -f "${TMP_RAW}" "${TMP_BODY}"' EXIT

echo "[dump-prod-to-seed] Exporting ${#TABLES[@]} application tables via pg_dump..."
pg_dump \
    -h "${PGHOST}" \
    -p "${PGPORT}" \
    -U "${PGUSER}" \
    -d "${PGDATABASE}" \
    --data-only \
    --column-inserts \
    --no-owner \
    --no-privileges \
    "${TABLE_ARGS[@]}" > "${TMP_RAW}"

echo "[dump-prod-to-seed] Sanitizing SQL dump for H2 compatibility..."
sed -E \
    -e '/^\\(restrict|unrestrict)/d' \
    -e '/^SET /d' \
    -e '/^SELECT pg_catalog\./d' \
    -e 's/^INSERT INTO public\./INSERT INTO /g' \
    -e 's/\(id, message, "timestamp",/(id, message, timestamp,/g' \
    "${TMP_RAW}" > "${TMP_BODY}"

{
    echo "-- ============================================================================="
    echo "-- Mushroom Expense Tracker - Production Seed Data for H2"
    echo "-- Source: ${PGHOST}:${PGPORT}/${PGDATABASE}"
    echo "-- Generated: $(date -Iseconds)"
    echo "-- ============================================================================="
    echo ""
    cat "${TMP_BODY}"
    echo ""
    echo "-- ============================================================================="
    echo "-- Synchronize H2 IDENTITY sequences with maximum imported IDs"
    echo "-- ============================================================================="
    for tbl in "${TABLES[@]}"; do
        NEXT_ID="$(awk -v t="${tbl}" '
            $0 ~ "^INSERT INTO " t " \\(" {
                if (match($0, /VALUES \([0-9]+/)) {
                    val = substr($0, RSTART + 8, RLENGTH - 8) + 0
                    if (val > max) max = val
                }
            }
            END { print (max ? max + 1 : 1) }
        ' "${TMP_BODY}")"
        echo "ALTER TABLE ${tbl} ALTER COLUMN id RESTART WITH ${NEXT_ID};"
    done
} > "${OUTPUT_FILE}"

INSERT_COUNT="$(grep -c '^INSERT INTO ' "${OUTPUT_FILE}" || true)"
echo "[dump-prod-to-seed] Done! Wrote ${INSERT_COUNT} INSERT statements to ${OUTPUT_FILE}"
