@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==========================================
REM  LMS Uninstaller - Windows
REM  Version 2.0.1
REM ==========================================

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║                                                              ║
echo ║       📚 LMS Uninstaller - Version 0.1 📚                    ║
echo ║                                                              ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Check if running from correct directory
if not exist "%SCRIPT_DIR%\LMS-Setup.jar" (
    echo ✗ LMS-Setup.jar not found in %SCRIPT_DIR%
    echo   Please run this script from the LMS project directory.
    pause
    exit /b 1
)

echo Uninstalling LMS...
echo.

REM Check Java
where java >nul 2>nul
if errorlevel 1 (
    echo ✗ Java is not installed
    echo   Cannot run uninstaller without Java.
    pause
    exit /b 1
)

echo Checking for installed LMS instances...
echo.

REM Try to find installed LMS locations
REM Common locations: %LOCALAPPDATA%\LMS, %PROGRAMFILES%\LMS, etc.

set "INSTALL_LOCATIONS=0"

if exist "%LOCALAPPDATA%\LMS" (
    echo Found installation at: %LOCALAPPDATA%\LMS
    set /a INSTALL_LOCATIONS+=1
)

if exist "%PROGRAMFILES%\LMS" (
    echo Found installation at: %PROGRAMFILES%\LMS
    set /a INSTALL_LOCATIONS+=1
)

if exist "%ProgramFiles(x86)%\LMS" (
    echo Found installation at: %ProgramFiles(x86)%\LMS
    set /a INSTALL_LOCATIONS+=1
)

REM Check if installation directory is set via environment
if defined LMS_INSTALL_DIR (
    if exist "!LMS_INSTALL_DIR!" (
        echo Found installation at: !LMS_INSTALL_DIR!
        set /a INSTALL_LOCATIONS+=1
    )
)

echo.

if %INSTALL_LOCATIONS% equ 0 (
    echo ⚠ No LMS installations found in standard locations.
    echo.
    echo To completely remove LMS:
    echo   1. Delete the installation directory manually
    echo   2. Delete application shortcuts from Start Menu
    echo   3. Delete any desktop shortcuts
    echo   4. If desired, delete the database tables from Oracle
    echo.
    pause
    exit /b 0
)

echo.
echo ⚠ WARNING: This will remove the LMS application!
echo.
echo Please choose:
echo   1 - Uninstall LMS application (keep database)
echo   2 - Uninstall LMS and remove database tables
echo   3 - Cancel
echo.

set /p CHOICE="Enter your choice (1-3): "

if "%CHOICE%"=="3" (
    echo Uninstallation cancelled.
    exit /b 0
)

if "%CHOICE%"=="1" (
    echo Removing LMS application files...
    
    if exist "%LOCALAPPDATA%\LMS" (
        echo Removing: %LOCALAPPDATA%\LMS
        rmdir /s /q "%LOCALAPPDATA%\LMS" >nul 2>&1
    )
    
    if exist "%PROGRAMFILES%\LMS" (
        echo Removing: %PROGRAMFILES%\LMS
        rmdir /s /q "%PROGRAMFILES%\LMS" >nul 2>&1
    )
    
    if exist "%ProgramFiles(x86)%\LMS" (
        echo Removing: %ProgramFiles(x86)%\LMS
        rmdir /s /q "%ProgramFiles(x86)%\LMS" >nul 2>&1
    )
    
    echo.
    echo ✓ LMS application uninstalled successfully.
    echo   Database remains intact if further uninstallation is needed.
    echo.
    pause
    exit /b 0
)

if "%CHOICE%"=="2" (
    echo.
    echo ⚠ WARNING: This will delete the LMS database!
    echo   All book records, student records, and transaction history will be LOST.
    echo.
    set /p CONFIRM="Type 'YES' to confirm complete removal: "
    
    if not "!CONFIRM!"=="YES" (
        echo Uninstallation cancelled.
        exit /b 0
    )
    
    echo Removing LMS application files...
    
    if exist "%LOCALAPPDATA%\LMS" (
        echo Removing: %LOCALAPPDATA%\LMS
        rmdir /s /q "%LOCALAPPDATA%\LMS" >nul 2>&1
    )
    
    if exist "%PROGRAMFILES%\LMS" (
        echo Removing: %PROGRAMFILES%\LMS
        rmdir /s /q "%PROGRAMFILES%\LMS" >nul 2>&1
    )
    
    if exist "%ProgramFiles(x86)%\LMS" (
        echo Removing: %ProgramFiles(x86)%\LMS
        rmdir /s /q "%ProgramFiles(x86)%\LMS" >nul 2>&1
    )
    
    echo.
    echo Removing Oracle database tables...
    echo   This requires manual Oracle database cleanup:
    echo   1. Connect to Oracle as SYSTEM user
    echo   2. Run: DROP USER PRJ2531H CASCADE;
    echo   3. Or delete individual tables if needed
    echo.
    echo ✓ LMS application uninstalled.
    echo   Please manually clean up Oracle database using SQL commands above.
    echo.
    pause
    exit /b 0
)

echo Invalid choice. Cancelling uninstallation.
pause
exit /b 1
