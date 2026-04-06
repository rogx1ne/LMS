#!/bin/bash

# ==========================================
#  LMS - Run with Environment Variables
# ==========================================
# This script sets up the environment and launches the LMS application

# Load environment variables for Podman Oracle
export LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"

# Verify Podman Oracle is running
echo "Checking Oracle connection..."
podman ps | grep -q oracle10g || {
    echo "⚠ WARNING: Oracle Podman container (oracle10g) is not running!"
    echo "Start it with: podman start oracle10g"
    exit 1
}

echo "✓ Oracle is running"
echo "Starting LMS application..."

# Run with ./run.sh which uses these env vars
cd "$(dirname "$0")" || exit 1
./run.sh
