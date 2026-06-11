#!/usr/bin/env bash
set -euo pipefail

# ======================================================================
#  RegisterFoot — Script de creacion de base de datos
#  Crea la base de datos, ejecuta schema.sql y seed.sql
# ======================================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SQL_DIR="$SCRIPT_DIR/src/main/resources/db"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-BN2002sg}"
DB_NAME="registerfoot"

MYSQL_OPTS=(-h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" --password="$DB_PASSWORD")

echo "=== RegisterFoot — Inicializando base de datos ==="
echo "  Host:     $DB_HOST"
echo "  Puerto:   $DB_PORT"
echo "  Usuario:  $DB_USER"
echo "  BD:       $DB_NAME"
echo ""

if ! command -v mysql &>/dev/null; then
    echo "ERROR: mysql (cliente de MySQL) no encontrado. Instalelo primero."
    exit 1
fi

echo "[1/3] Creando base de datos '$DB_NAME' (si no existe)..."
mysql "${MYSQL_OPTS[@]}" -e "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "      OK"

echo "[2/3] Ejecutando schema.sql..."
mysql "${MYSQL_OPTS[@]}" "$DB_NAME" < "$SQL_DIR/schema.sql"
echo "      OK"

echo "[3/3] Ejecutando seed.sql..."
mysql "${MYSQL_OPTS[@]}" "$DB_NAME" < "$SQL_DIR/seed.sql"
echo "      OK"

echo ""
echo "=== Base de datos '$DB_NAME' lista ==="
echo "NOTA: los usuarios del sistema (admin/admin123, etc.) se crean"
echo "      automaticamente al primer arranque de la aplicacion Java."
