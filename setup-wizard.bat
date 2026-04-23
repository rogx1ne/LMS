@echo off
REM LMS Setup Wizard Launcher
REM Compiles and runs the setup wizard with proper environment setup

setlocal enabledelayedexpansion

color 0A

echo.
echo ╔════════════════════════════════════════╗
echo ║   LMS Setup Wizard                     ║
echo ╚════════════════════════════════════════╝
echo.

REM Get the current directory
cd /d "%~dp0"

REM Load environment from .env.setup if it exists
if exist ".env.setup" (
    echo → Loading database credentials from .env.setup
    for /f "usebackq tokens=1* delims==" %%A in (".env.setup") do (
        if not "%%A"=="" (
            if not "%%A:~0,1%"=="#" (
                set %%A=%%B
            )
        )
    )
    echo ✓ Environment variables loaded
) else if exist ".env.setup.example" (
    echo ⚠ Found .env.setup.example but not .env.setup
    echo → Setup requires Oracle SYSTEM credentials
    echo.
    echo To proceed, do the following:
    echo   1. Copy .env.setup.example to .env.setup
    echo   2. Edit .env.setup with your Oracle SYSTEM credentials
    echo   3. Run this script again
    echo.
    pause
    exit /b 1
) else (
    echo ℹ Using default or environment variables for database connection
)
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    color 0C
    echo ✗ Error: Java is not installed
    echo Please install Java 8 or higher and try again.
    pause
    exit /b 1
)

for /f "tokens=3" %%A in ('java -version 2^>^&1 ^| find /i "version"') do (
    set JAVA_VERSION=%%A
)
echo → Java Version: %JAVA_VERSION%
echo.

REM Create bin directory if it doesn't exist
if not exist "bin" (
    echo → Creating bin directory...
    mkdir bin
)

REM Compile setup wizard classes
echo → Compiling setup wizard...
javac -encoding UTF-8 -d bin --release 8 -cp "bin;lib\*" ^
    src\com\library\setup\LMSSetupWizard.java ^
    src\com\library\setup\InstallationManager.java ^
    src\com\library\database\DBConnection.java

if errorlevel 1 (
    color 0C
    echo ✗ Compilation failed - Retrying with verbose output...
    javac -encoding UTF-8 -d bin --release 8 -cp "bin;lib\*" ^
        src\com\library\setup\LMSSetupWizard.java ^
        src\com\library\setup\InstallationManager.java ^
        src\com\library\database\DBConnection.java
    pause
    exit /b 1
)

color 0A
echo ✓ Compilation successful
echo.
echo Starting Setup Wizard...
echo.

REM Run the setup wizard with timezone fix for Oracle
java -Doracle.jdbc.timezoneAsRegion=false ^
  -cp "bin;lib\*" ^
  com.library.setup.LMSSetupWizard

exit /b 0
