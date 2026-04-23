@echo off
color 0F
title Library Management System
cls

REM =========================================
REM Library Management System - Launcher
REM =========================================

cd /d "%~dp0"

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java 8+ and add it to PATH
    pause
    exit /b 1
)

REM Check if required directories exist
if not exist bin (
    echo ERROR: bin/ directory not found
    echo Please run setup first: java -jar LMS-Setup.jar
    pause
    exit /b 1
)

echo Connecting to database...
set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe
set LMS_DB_USER=PRJ2531H
set LMS_DB_PASSWORD=PRJ2531H

java -Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false -cp "bin;lib\*" Main

if errorlevel 1 (
    echo.
    echo ERROR: Application failed to start
    echo Check that Oracle database is running on localhost:1521
    pause
)
