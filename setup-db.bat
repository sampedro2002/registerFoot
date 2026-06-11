@echo off
setlocal enabledelayedexpansion

REM ======================================================================
REM  RegisterFoot — Script de creacion de base de datos (Windows)
REM  Crea la base de datos, ejecuta schema.sql y seed.sql
REM ======================================================================

set SCRIPT_DIR=%~dp0
set SQL_DIR=%SCRIPT_DIR%src\main\resources\db

if not defined DB_HOST set DB_HOST=localhost
if not defined DB_PORT set DB_PORT=3306
if not defined DB_USER set DB_USER=root
if not defined DB_PASSWORD set DB_PASSWORD=root
set DB_NAME=registerfoot

echo === RegisterFoot — Inicializando base de datos ===
echo   Host:     %DB_HOST%
echo   Puerto:   %DB_PORT%
echo   Usuario:  %DB_USER%
echo   BD:       %DB_NAME%
echo.

where mysql >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: mysql (cliente de MySQL) no encontrado. Instalelo primero.
    exit /b 1
)

echo [1/3] Creando base de datos '%DB_NAME%' (si no existe)...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --password=%DB_PASSWORD% -e "CREATE DATABASE IF NOT EXISTS \`%DB_NAME%\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if %ERRORLEVEL% neq 0 (
    echo ERROR al crear la base de datos
    exit /b 1
)
echo       OK

echo [2/3] Ejecutando schema.sql...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --password=%DB_PASSWORD% %DB_NAME% < "%SQL_DIR%\schema.sql"
if %ERRORLEVEL% neq 0 (
    echo ERROR al ejecutar schema.sql
    exit /b 1
)
echo       OK

echo [3/3] Ejecutando seed.sql...
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% --password=%DB_PASSWORD% %DB_NAME% < "%SQL_DIR%\seed.sql"
if %ERRORLEVEL% neq 0 (
    echo ERROR al ejecutar seed.sql
    exit /b 1
)
echo       OK

echo.
echo === Base de datos '%DB_NAME%' lista ===
echo NOTA: los usuarios del sistema (admin/admin123, etc.) se crean
echo       automaticamente al primer arranque de la aplicacion Java.

endlocal
