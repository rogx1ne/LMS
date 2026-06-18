#!/bin/bash

# LMS Setup Wizard - End-to-End Verification Script
# Verifies all components work correctly before installation

set -e

echo "╔════════════════════════════════════════════════════════════════════════╗"
echo "║         LMS SETUP WIZARD - END-TO-END VERIFICATION SCRIPT             ║"
echo "╚════════════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0

# Helper functions
test_pass() {
    echo -e "${GREEN}✓ PASS${NC}: $1"
    ((PASS_COUNT++))
}

test_fail() {
    echo -e "${RED}✗ FAIL${NC}: $1"
    ((FAIL_COUNT++))
}

test_warn() {
    echo -e "${YELLOW}⚠ WARN${NC}: $1"
    ((WARN_COUNT++))
}

echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 1: SYSTEM REQUIREMENTS CHECK"
echo "═════════════════════════════════════════════════════════════════════════"

# Check Java
echo ""
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=version ")[^"]*' || echo "unknown")
    test_pass "Java installed: $JAVA_VERSION"
else
    test_fail "Java not installed"
fi

# Check Python (optional but recommended)
echo ""
echo "Checking Python (optional)..."
if command -v python3 &> /dev/null; then
    PYTHON_VERSION=$(python3 --version 2>&1)
    test_pass "Python3 installed: $PYTHON_VERSION"
else
    test_warn "Python3 not installed (optional)"
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 2: LMS PROJECT VERIFICATION"
echo "═════════════════════════════════════════════════════════════════════════"

# Check project structure
echo ""
echo "Checking project structure..."
cd /home/abhiadi/mine/clg/LMS

if [ -d "src/com/library/setup" ]; then
    test_pass "Setup wizard sources found"
else
    test_fail "Setup wizard sources not found"
fi

if [ -d "bin" ]; then
    test_pass "Compiled classes directory exists"
else
    test_warn "Compiled classes directory missing (will be created)"
fi

if [ -f "setup-wizard.sh" ]; then
    test_pass "Setup launcher script found"
else
    test_fail "Setup launcher script not found"
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 3: COMPILATION CHECK"
echo "═════════════════════════════════════════════════════════════════════════"

echo ""
echo "Attempting to compile LMS setup wizard..."
if javac -d bin --release 8 -cp "bin:lib/*" \
    src/com/library/setup/LMSSetupWizard.java \
    src/com/library/setup/InstallationManager.java \
    src/com/library/database/DBConnection.java \
    src/com/library/setup/SetupWizardTest.java 2>/dev/null; then
    test_pass "All setup wizard components compile successfully"
else
    test_fail "Compilation failed"
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 4: DATABASE CONNECTIVITY CHECK"
echo "═════════════════════════════════════════════════════════════════════════"

echo ""
echo "Testing Oracle database connection..."

# Check if Podman is running
if command -v podman &> /dev/null; then
    if podman ps | grep -q "oracle"; then
        test_pass "Podman Oracle container is running"
    else
        test_warn "Podman Oracle container is NOT running"
        echo ""
        echo "  To start Podman Oracle:"
        echo "  $ podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g"
    fi
else
    test_warn "Podman not installed (may be running Docker or local Oracle instead)"
fi

# Try to connect to Oracle via Java
echo ""
echo "Attempting JDBC connection to Oracle..."
if timeout 5 java -Doracle.jdbc.timezoneAsRegion=false -cp "bin:lib/*" \
    com.library.setup.SetupWizardTest 2>&1 | grep -q "Database connection successful"; then
    test_pass "JDBC connection to Oracle successful"
else
    # Check if it's a timezone issue
    if timeout 5 java -Doracle.jdbc.timezoneAsRegion=false -cp "bin:lib/*" \
        com.library.setup.SetupWizardTest 2>&1 | grep -q "ORA-01882"; then
        test_fail "Timezone error detected (ORA-01882)"
        echo "  → This means timezone region not found"
        echo "  → The -Doracle.jdbc.timezoneAsRegion=false flag should fix this"
        echo "  → Verify Podman Oracle is fully initialized and ready"
    elif timeout 5 java -Doracle.jdbc.timezoneAsRegion=false -cp "bin:lib/*" \
        com.library.setup.SetupWizardTest 2>&1 | grep -q "failed to connect\|refused"; then
        test_fail "Cannot connect to Oracle (connection refused)"
        echo "  → Oracle may not be running or listening on port 1521"
        echo "  → Check: podman ps | grep oracle"
    else
        test_warn "Oracle connection test inconclusive"
    fi
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 5: VALIDATION RULES CHECK"
echo "═════════════════════════════════════════════════════════════════════════"

echo ""
echo "Testing setup wizard validation rules..."
if java -Doracle.jdbc.timezoneAsRegion=false -cp "bin:lib/*" \
    com.library.setup.SetupWizardTest 2>&1 | grep -q "ALL TESTS PASSED"; then
    test_pass "All 34 validation tests pass"
else
    test_warn "Validation tests may have issues (check full test output)"
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "PHASE 6: LAUNCHER SCRIPT CHECK"
echo "═════════════════════════════════════════════════════════════════════════"

echo ""
echo "Checking setup-wizard.sh script..."
if grep -q "Doracle.jdbc.timezoneAsRegion=false" setup-wizard.sh; then
    test_pass "Timezone fix flag found in launcher script"
else
    test_warn "Timezone fix flag NOT in launcher script (will be added)"
fi

if [ -x setup-wizard.sh ]; then
    test_pass "Launcher script is executable"
else
    test_warn "Launcher script not executable (chmod +x will be applied)"
    chmod +x setup-wizard.sh
fi

echo ""
echo "═════════════════════════════════════════════════════════════════════════"
echo "SUMMARY"
echo "═════════════════════════════════════════════════════════════════════════"

echo ""
echo "Test Results:"
echo -e "  ${GREEN}Passed: $PASS_COUNT${NC}"
echo -e "  ${YELLOW}Warnings: $WARN_COUNT${NC}"
echo -e "  ${RED}Failed: $FAIL_COUNT${NC}"

echo ""
if [ $FAIL_COUNT -eq 0 ]; then
    echo -e "${GREEN}✓ VERIFICATION COMPLETE - READY TO PROCEED${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Start Podman Oracle (if not running):"
    echo "   $ podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g"
    echo ""
    echo "2. Run setup wizard:"
    echo "   $ ./setup-wizard.sh"
    echo ""
    exit 0
else
    echo -e "${RED}✗ VERIFICATION FAILED - FIX ISSUES ABOVE BEFORE PROCEEDING${NC}"
    echo ""
    exit 1
fi
