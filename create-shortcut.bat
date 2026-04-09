@echo off
REM =========================================
REM Create Desktop Shortcut for LMS
REM =========================================

setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

set "DESKTOP=%USERPROFILE%\Desktop"
set "SHORTCUT_PATH=%DESKTOP%\LMS.bat"

if not exist "%DESKTOP%" (
    echo ERROR: Desktop folder not found at %DESKTOP%
    pause
    exit /b 1
)

REM Create the shortcut file
(
    echo @echo off
    echo cd /d "%SCRIPT_DIR%"
    echo start run.bat
) > "%SHORTCUT_PATH%"

if exist "%SHORTCUT_PATH%" (
    echo ✓ Desktop shortcut created: %SHORTCUT_PATH%
    echo.
    echo You can now double-click "LMS.bat" on your Desktop to launch the application!
) else (
    echo ✗ Failed to create desktop shortcut
    exit /b 1
)

pause
