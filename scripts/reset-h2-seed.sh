#!/usr/bin/env bash
# ==============================================================================
# reset-h2-seed.sh
# Wipes local file-backed H2 database files (./data/mushroomdb*) so that the
# next application startup automatically loads src/main/resources/seed/prod-seed.sql.
#
# Options:
#   --refresh-dump, -r   Pull a fresh dump from production PostgreSQL first
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

REFRESH_DUMP=false
for arg in "$@"; do
    case "${arg}" in
        --refresh-dump|-r)
            REFRESH_DUMP=true
            ;;
        --help|-h)
            echo "Usage: $0 [--refresh-dump|-r]"
            exit 0
            ;;
        *)
            echo "Unknown option: ${arg}" >&2
            echo "Usage: $0 [--refresh-dump|-r]" >&2
            exit 1
            ;;
    esac
done

if [[ "${REFRESH_DUMP}" == "true" ]]; then
    echo "[reset-h2-seed] Refreshing production dump..."
    "${SCRIPT_DIR}/dump-prod-to-seed.sh"
fi

echo "[reset-h2-seed] Removing existing local H2 database files in ${PROJECT_ROOT}/data/..."
rm -f "${PROJECT_ROOT}"/data/mushroomdb*.db
mkdir -p "${PROJECT_ROOT}/data"

echo "[reset-h2-seed] Local H2 database files cleared. Production seed data (classpath:seed/prod-seed.sql) will be loaded automatically on next application startup."
