@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==========================================
REM  LMS - Run with Environment Variables
REM  Windows Version
REM ==========================================

REM Set environment variables for Oracle Database connection
set "LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe"
set "LMS_DB_USER=PRJ2531H"
set "LMS_DB_PASSWORD=PRJ2531H"

echo Checking Oracle connection...

REM Check if Oracle is running on port 1521
netstat -ano 2>nul | findstr ":1521" >nul
if errorlevel 1 (
    echo ✗ Oracle Database is not running on port 1521!
    echo.
    echo To start Oracle Database on Windows:
    echo   1. Open Services (services.msc)
    echo   2. Find "OracleServiceXE" or "OracleService..."
    echo   3. Right-click and select "Start"
    echo.
    echo Or use Command Prompt ^(as Administrator^):
    echo   net start OracleServiceXE
    echo.
    pause
    exit /b 1
)

echo ✓ Oracle is running
echo Starting LMS application...
echo.

REM Change to script directory
cd /d "%~dp0"

REM Run the application with environment variables
REM run.bat sets classpath and uses the env vars above
call run.bat

endlocal
