@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==========================================
REM  LMS Setup Wizard - Production Launcher
REM  Version 2.0.2 - With environment config
REM ==========================================

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║       📚 LMS Setup Wizard - Version 2.0 📚                  ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM ==========================================
REM  LOAD ENVIRONMENT CONFIGURATION
REM ==========================================

if exist "%SCRIPT_DIR%\.env.setup" (
    echo → Loading database credentials from .env.setup
    for /f "usebackq tokens=* delims=" %%A in ("%SCRIPT_DIR%\.env.setup") do (
        if not "%%A"=="" if not "%%A:~0,1%"=="#" (
            set "%%A"
        )
    )
    echo ✓ Environment variables loaded
    echo.
) else if exist "%SCRIPT_DIR%\.env.setup.example" (
    echo ⚠ Found .env.setup.example but not .env.setup
    echo → Setup requires Oracle SYSTEM credentials
    echo.
    echo To proceed, do the following:
    echo   1. Copy .env.setup.example to .env.setup
    echo   2. Open .env.setup and edit with your Oracle SYSTEM credentials
    echo   3. Run this batch file again
    echo.
    echo Would you like to copy .env.setup.example to .env.setup now? (Y/N)
    set /p COPY_FILE=
    if /i "%COPY_FILE%"=="Y" (
        copy "%SCRIPT_DIR%\.env.setup.example" "%SCRIPT_DIR%\.env.setup"
        echo ✓ File copied. Please edit .env.setup and run this script again.
    )
    pause
    exit /b 1
) else (
    echo ℹ Using default or environment variables for database connection
    echo.
)

REM ==========================================
REM  PRE-FLIGHT CHECKS
REM ==========================================

echo → Running pre-flight checks...
echo.

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    echo ✗ Java is not installed
    echo   Please install Java 8 or higher and try again.
    echo   Download from: https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%v
    goto :java_version_found
)
:java_version_found
set JAVA_VERSION=%JAVA_VERSION:"=%
echo ✓ Java detected: %JAVA_VERSION%

REM Check if JAR exists
if not exist "%SCRIPT_DIR%\LMS-Setup.jar" (
    echo ✗ LMS-Setup.jar not found in %SCRIPT_DIR%
    echo   Please ensure all files are present.
    pause
    exit /b 1
)
echo ✓ Setup JAR found

REM Check if lib directory exists
if not exist "%SCRIPT_DIR%\lib" (
    echo ✗ lib directory not found in %SCRIPT_DIR%
    echo   Make sure you run this script from the LMS project directory.
    pause
    exit /b 1
)
echo ✓ Dependencies directory found

REM Check for Oracle JDBC driver
if not exist "%SCRIPT_DIR%\lib\ojdbc6.jar" (
    echo ⚠ Warning: ojdbc6.jar not found in lib\
    echo   Database connection may fail.
)

REM Check for Oracle Database installed locally on Windows
REM Oracle typically uses service name like "OracleServiceXE" or port 1521
echo Checking for Oracle Database installation...

REM Check if Oracle TNS Listener is running (port 1521)
netstat -ano 2>nul | findstr ":1521" >nul
if not errorlevel 1 (
    echo ✓ Oracle Database detected (port 1521 is listening)
) else (
    echo ⚠ Oracle Database not detected on port 1521
    echo   Make sure Oracle 10g or later is installed and running.
    echo   If using Oracle XE: Start "Oracle Database 10g Express Edition" service
    echo   Check: Services (services.msc) - Look for OracleServiceXE or OracleService...
    echo.
)
echo.
echo Starting LMS Setup Wizard...
echo ═══════════════════════════════════════════════════════════════
echo.

REM ==========================================
REM  LAUNCH SETUP WIZARD
REM ==========================================

REM Change to script directory to ensure relative paths work
cd /d "%SCRIPT_DIR%"

REM Run the setup wizard with:
REM - Oracle timezone fix for JDBC connection
REM - Proper classpath from manifest
java -Doracle.jdbc.timezoneAsRegion=false -jar LMS-Setup.jar

set EXIT_CODE=%ERRORLEVEL%

echo.
if %EXIT_CODE% equ 0 (
    echo ✓ Setup completed successfully!
) else (
    echo ✗ Setup failed with exit code: %EXIT_CODE%
)

pause
exit /b %EXIT_CODE%
