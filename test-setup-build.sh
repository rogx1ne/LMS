#!/bin/bash
echo "=== Testing LMS Setup Build ==="

# Test 1: Verify compilation
echo "[1/3] Testing compilation..."
javac -d bin -cp ".:lib/*:bin" src/com/library/setup/LMSSetupWizard.java 2>&1
if [ $? -eq 0 ]; then
    echo "✓ LMSSetupWizard.java compiles successfully"
else
    echo "✗ Compilation failed"
    exit 1
fi

javac -d bin -cp ".:lib/*:bin" src/com/library/setup/InstallationManager.java 2>&1
if [ $? -eq 0 ]; then
    echo "✓ InstallationManager.java compiles successfully"
else
    echo "✗ Compilation failed"
    exit 1
fi

# Test 2: Check class files exist
echo ""
echo "[2/3] Checking compiled class files..."
if [ -f "bin/com/library/setup/LMSSetupWizard.class" ]; then
    echo "✓ LMSSetupWizard.class exists"
else
    echo "✗ LMSSetupWizard.class not found"
    exit 1
fi

if [ -f "bin/com/library/setup/InstallationManager.class" ]; then
    echo "✓ InstallationManager.class exists"
else
    echo "✗ InstallationManager.class not found"
    exit 1
fi

# Test 3: Verify script.sql exists
echo ""
echo "[3/3] Checking script.sql..."
if [ -f "script.sql" ]; then
    echo "✓ script.sql found at $(pwd)/script.sql"
    lines=$(wc -l < script.sql)
    echo "  Contains $lines lines"
else
    echo "✗ script.sql not found"
    exit 1
fi

echo ""
echo "=== All Tests Passed ==="
echo ""
echo "Setup is ready to use. To run the setup wizard:"
echo "  java -cp \"bin:lib/*\" com.library.setup.LMSSetupWizard"
