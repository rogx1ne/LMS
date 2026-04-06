#!/bin/bash

# LMS Setup Wizard Launcher
# Compiles and runs the setup wizard with proper environment setup

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║   LMS Setup Wizard                     ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════╝${NC}"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Error: Java is not installed${NC}"
    echo "Please install Java 8 or higher and try again."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=version ")[^"]*' || echo "unknown")
echo -e "${YELLOW}→ Java Version: $JAVA_VERSION${NC}"
echo ""

# Create bin directory if it doesn't exist
if [ ! -d "bin" ]; then
    echo -e "${YELLOW}→ Creating bin directory...${NC}"
    mkdir -p bin
fi

# Compile setup wizard classes
echo -e "${YELLOW}→ Compiling setup wizard...${NC}"
if javac -d bin --release 8 -cp "bin:lib/*" \
    src/com/library/setup/LMSSetupWizard.java \
    src/com/library/setup/InstallationManager.java \
    src/com/library/database/DBConnection.java \
    2>/dev/null; then
    echo -e "${GREEN}✓ Compilation successful${NC}"
else
    echo -e "${RED}✗ Compilation failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}Starting Setup Wizard...${NC}"
echo ""

# Run the setup wizard with timezone fix for Oracle
java -Doracle.jdbc.timezoneAsRegion=false \
  -cp "bin:lib/*" \
  com.library.setup.LMSSetupWizard

exit 0
