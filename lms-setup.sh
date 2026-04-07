#!/bin/bash

# LMS Setup Wizard - Production Launcher
# Ensures proper environment and runs from correct directory

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get the script directory (where JAR and lib are located)
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║       📚 LMS Setup Wizard - Version 2.0.1 📚                ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Pre-flight checks
echo -e "${YELLOW}→ Running pre-flight checks...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java is not installed${NC}"
    echo "  Please install Java 8 or higher and try again."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | grep -oP '(?<=version ")[^"]*' || echo "unknown")
echo -e "${GREEN}✓ Java detected: $JAVA_VERSION${NC}"

# Check if JAR exists
if [ ! -f "$SCRIPT_DIR/LMS-Setup.jar" ]; then
    echo -e "${RED}✗ LMS-Setup.jar not found in $SCRIPT_DIR${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Setup JAR found${NC}"

# Check if lib directory exists
if [ ! -d "$SCRIPT_DIR/lib" ]; then
    echo -e "${RED}✗ lib directory not found in $SCRIPT_DIR${NC}"
    echo "  Make sure you run this script from the LMS project directory."
    exit 1
fi
echo -e "${GREEN}✓ Dependencies directory found${NC}"

# Check for Oracle JDBC driver
if [ ! -f "$SCRIPT_DIR/lib/ojdbc6.jar" ]; then
    echo -e "${YELLOW}⚠ Warning: ojdbc6.jar not found in lib/${NC}"
    echo "  Database connection may fail."
fi

# Check if Podman Oracle is running (optional check)
if command -v podman &> /dev/null; then
    if podman ps | grep -q oracle; then
        echo -e "${GREEN}✓ Oracle container is running${NC}"
    else
        echo -e "${YELLOW}⚠ Oracle container not detected${NC}"
        echo "  If you need database connection, start Oracle with:"
        echo "  podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g"
    fi
fi

echo ""
echo -e "${GREEN}Starting LMS Setup Wizard...${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
echo ""

# Change to script directory to ensure relative paths work
cd "$SCRIPT_DIR"

# Run the setup wizard with:
# - Oracle timezone fix for JDBC connection
# - Proper classpath from manifest
java -Doracle.jdbc.timezoneAsRegion=false \
     -jar LMS-Setup.jar

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ Setup completed successfully!${NC}"
else
    echo -e "${RED}✗ Setup failed with exit code: $EXIT_CODE${NC}"
fi

exit $EXIT_CODE
