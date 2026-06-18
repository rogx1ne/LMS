#!/bin/bash

# =========================================
# Library Management System - Launcher
# =========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed"
    echo "Please install Java 8+ using: sudo apt-get install default-jre"
    exit 1
fi

# Check if required directories exist
if [ ! -d "bin" ]; then
    echo "ERROR: bin/ directory not found"
    echo "Please run setup first: java -jar LMS-Setup.jar"
    exit 1
fi

# Recompile application classes with current Java version for compatibility
echo "Compiling application for current Java version..."
javac -encoding UTF-8 -d bin --release 8 -cp "bin:lib/*" src/com/library/Main.java 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Compilation failed. Please ensure Java 8+ is installed."
    exit 1
fi

echo "Connecting to database..."
export LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"

java -Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false -cp "bin:lib/*" Main

if [ $? -ne 0 ]; then
    echo ""
    echo "ERROR: Application failed to start"
    echo "Check that Oracle database is running on localhost:1521"
    read -p "Press Enter to exit..."
fi
