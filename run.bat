@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ==========================================
REM  Library Management System - Build Script
REM  (Windows)
REM ==========================================

REM 1. SETUP PATHS
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
set "SRC_DIR=%PROJECT_ROOT%\src"
set "LIB_DIR=%PROJECT_ROOT%\lib"
set "OUT_DIR=%PROJECT_ROOT%\bin"

REM 1.1 LOAD ENV (if present)
if exist "%PROJECT_ROOT%\.env" (
    for /f "usebackq tokens=* delims=" %%A in ("%PROJECT_ROOT%\.env") do (
        set "line=%%A"
        if not "!line!"=="" (
            if not "!line:~0,1!"=="#" (
                for /f "tokens=1* delims==" %%K in ("!line!") do (
                    if not "%%K"=="" set "%%K=%%L"
                )
            )
        )
    )
)

REM 2. CLEANUP
if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

REM 3. COMPILATION
echo Compiling Java sources...
dir /b /s "%SRC_DIR%\*.java" > "%PROJECT_ROOT%\sources_list.txt"

javac -d "%OUT_DIR%" -cp "%LIB_DIR%\*" @"%PROJECT_ROOT%\sources_list.txt"

if errorlevel 1 (
    echo Compilation Failed!
    del /q "%PROJECT_ROOT%\sources_list.txt" >nul 2>&1
    exit /b 1
) else (
    echo Compilation Successful.
    del /q "%PROJECT_ROOT%\sources_list.txt" >nul 2>&1
)

REM 4. EXECUTION
echo ----------------------------------------
echo Starting Application...
echo ----------------------------------------

java -cp "%OUT_DIR%;%LIB_DIR%\*" -Doracle.jdbc.timezoneAsRegion=false Main

endlocal
