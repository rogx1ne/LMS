#!/bin/bash

# ==========================================
#  Library Management System - Build Script
# ==========================================

# 1. SETUP PATHS
PROJECT_ROOT=$(pwd)
SRC_DIR="$PROJECT_ROOT/src"
LIB_DIR="$PROJECT_ROOT/lib"
OUT_DIR="$PROJECT_ROOT/bin"

# 2. CLEANUP
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

# 3. COMPILATION
echo "Compiling Java sources..."

# Find all .java files
find "$SRC_DIR" -name "*.java" > sources_list.txt

# Compile
# CHANGED: -cp "$LIB_DIR/*" 
# This wildcard tells Java to use EVERY jar file it finds in the lib folder.
javac -d "$OUT_DIR" -cp "$LIB_DIR/*" @sources_list.txt

# Check if compilation succeeded
if [ $? -eq 0 ]; then
    echo "✔ Compilation Successful."
    rm sources_list.txt
else
    echo "✘ Compilation Failed!"
    rm sources_list.txt
    exit 1
fi

# 4. EXECUTION
echo "----------------------------------------"
echo "Starting Application..."
echo "----------------------------------------"

# CHANGED: -cp "$OUT_DIR:$LIB_DIR/*"
# Matches the compile classpath so runtime works too.
java -cp "$OUT_DIR:$LIB_DIR/*" \
     -Doracle.jdbc.timezoneAsRegion=false \
     Main