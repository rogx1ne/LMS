#!/bin/bash

# ==========================================
#  LMS Uninstaller - Linux/macOS
#  Version 2.0.1
# ==========================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo -e "${BLUE}╔══════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}║       📚 LMS Uninstaller - Version 0.1 📚                    ║${NC}"
echo -e "${BLUE}║                                                              ║${NC}"
echo -e "${BLUE}╚══════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if running from correct directory
if [ ! -f "$SCRIPT_DIR/LMS-Setup.jar" ]; then
    echo -e "${RED}✗ LMS-Setup.jar not found in $SCRIPT_DIR${NC}"
    echo "  Please run this script from the LMS project directory."
    exit 1
fi

echo "Uninstalling LMS..."
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}✗ Java is not installed${NC}"
    echo "  Cannot run uninstaller without Java."
    exit 1
fi

echo "Checking for installed LMS instances..."
echo ""

# Try to find installed LMS locations
INSTALL_LOCATIONS=()

# Check common locations
[ -d "$HOME/.local/lms" ] && INSTALL_LOCATIONS+=("$HOME/.local/lms")
[ -d "$HOME/LMS" ] && INSTALL_LOCATIONS+=("$HOME/LMS")
[ -d "/opt/lms" ] && INSTALL_LOCATIONS+=("/opt/lms")
[ -d "/usr/local/lms" ] && INSTALL_LOCATIONS+=("/usr/local/lms")

# Check if installation directory is set via environment
[ -n "$LMS_INSTALL_DIR" ] && [ -d "$LMS_INSTALL_DIR" ] && INSTALL_LOCATIONS+=("$LMS_INSTALL_DIR")

# Remove duplicates
INSTALL_LOCATIONS=($(printf '%s\n' "${INSTALL_LOCATIONS[@]}" | sort -u))

# Display found installations
if [ ${#INSTALL_LOCATIONS[@]} -eq 0 ]; then
    echo -e "${YELLOW}⚠ No LMS installations found in standard locations.${NC}"
    echo ""
    echo "To completely remove LMS:"
    echo "  1. Delete the installation directory manually"
    echo "  2. Delete any symbolic links"
    echo "  3. If desired, delete the database tables from Oracle"
    echo ""
    exit 0
fi

for location in "${INSTALL_LOCATIONS[@]}"; do
    echo "Found installation at: $location"
done

echo ""
echo -e "${YELLOW}⚠ WARNING: This will remove the LMS application!${NC}"
echo ""
echo "Please choose:"
echo "  1 - Uninstall LMS application (keep database)"
echo "  2 - Uninstall LMS and remove database tables"
echo "  3 - Cancel"
echo ""

read -p "Enter your choice (1-3): " CHOICE

case $CHOICE in
    3)
        echo "Uninstallation cancelled."
        exit 0
        ;;
    
    1)
        echo -e "${YELLOW}Removing LMS application files...${NC}"
        
        for location in "${INSTALL_LOCATIONS[@]}"; do
            if [ -d "$location" ]; then
                echo "Removing: $location"
                rm -rf "$location"
            fi
        done
        
        echo ""
        echo -e "${GREEN}✓ LMS application uninstalled successfully.${NC}"
        echo "  Database remains intact if further uninstallation is needed."
        echo ""
        exit 0
        ;;
    
    2)
        echo ""
        echo -e "${RED}⚠ WARNING: This will delete the LMS database!${NC}"
        echo "  All book records, student records, and transaction history will be LOST."
        echo ""
        read -p "Type 'YES' to confirm complete removal: " CONFIRM
        
        if [ "$CONFIRM" != "YES" ]; then
            echo "Uninstallation cancelled."
            exit 0
        fi
        
        echo -e "${YELLOW}Removing LMS application files...${NC}"
        
        for location in "${INSTALL_LOCATIONS[@]}"; do
            if [ -d "$location" ]; then
                echo "Removing: $location"
                rm -rf "$location"
            fi
        done
        
        echo ""
        echo -e "${YELLOW}Removing Oracle database tables...${NC}"
        echo "  This requires manual Oracle database cleanup:"
        echo "  1. Connect to Oracle as SYSTEM user"
        echo "  2. Run: DROP USER PRJ2531H CASCADE;"
        echo "  3. Or delete individual tables if needed"
        echo ""
        echo -e "${GREEN}✓ LMS application uninstalled.${NC}"
        echo "  Please manually clean up Oracle database using SQL commands above."
        echo ""
        exit 0
        ;;
    
    *)
        echo "Invalid choice. Cancelling uninstallation."
        exit 1
        ;;
esac
