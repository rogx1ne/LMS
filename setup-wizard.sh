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

# Load environment from .env.setup if it exists
if [ -f ".env.setup" ]; then
    echo -e "${YELLOW}→ Loading database credentials from .env.setup${NC}"
    export $(cat .env.setup | grep -v '^#' | xargs)
    echo -e "${GREEN}✓ Environment variables loaded${NC}"
elif [ -f ".env.setup.example" ]; then
    echo -e "${YELLOW}⚠ Found .env.setup.example but not .env.setup${NC}"
    echo -e "${YELLOW}→ Setup requires Oracle SYSTEM credentials${NC}"
    echo ""
    echo "To proceed, do the following:"
    echo "  1. cp .env.setup.example .env.setup"
    echo "  2. Edit .env.setup with your Oracle SYSTEM credentials"
    echo "  3. Run this script again"
    echo ""
    read -p "Press Enter to open .env.setup.example for editing, or Ctrl+C to cancel..."
    ${EDITOR:-nano} .env.setup.example
    exit 1
else
    echo -e "${YELLOW}ℹ Using default or environment variables for database connection${NC}"
fi
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
