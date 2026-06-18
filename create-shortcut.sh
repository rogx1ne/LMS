#!/bin/bash

# =========================================
# Create Desktop Shortcut for LMS (Linux)
# =========================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DESKTOP_DIR="$HOME/Desktop"

# Create Desktop directory if it doesn't exist
if [ ! -d "$DESKTOP_DIR" ]; then
    mkdir -p "$DESKTOP_DIR"
    echo "✓ Created Desktop directory: $DESKTOP_DIR"
fi

# Create .desktop file
SHORTCUT_PATH="$DESKTOP_DIR/LMS.desktop"

cat > "$SHORTCUT_PATH" << EOF
[Desktop Entry]
Type=Application
Name=Library Management System
Exec=$SCRIPT_DIR/run.sh
Path=$SCRIPT_DIR
Icon=application-x-executable
Terminal=true
Categories=Utility;
EOF

# Make it executable
chmod +x "$SHORTCUT_PATH"

if [ -f "$SHORTCUT_PATH" ]; then
    echo "✓ Desktop shortcut created: $SHORTCUT_PATH"
    echo ""
    echo "You can now:"
    echo "  - Double-click 'LMS' on your Desktop to launch the application"
    echo "  - Or run: $SCRIPT_DIR/run.sh"
else
    echo "✗ Failed to create desktop shortcut"
    exit 1
fi
