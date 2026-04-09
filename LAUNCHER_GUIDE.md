# Library Management System - Quick Start Guide

## Installation

### Prerequisites
- **Java**: Java 8 or higher
- **Oracle Database**: Oracle 10g XE or higher running on `localhost:1521`
- **System Access**: To create the PRJ2531H database user

### Step 1: Run Setup Wizard
```bash
# On Windows
java -jar LMS-Setup.jar

# On Linux/Mac
java -jar LMS-Setup.jar
```

The setup wizard will:
- ✅ Create the PRJ2531H database user
- ✅ Initialize all 13 database tables
- ✅ Create the admin user account
- ✅ Generate application launcher scripts
- ✅ Optionally create Desktop shortcuts

### Step 2: Launch the Application

#### Windows
Double-click one of these:
- **`run.bat`** - Command window launcher (shows logs)
- **`LMS.bat`** - Desktop shortcut (if created)

Or from command line:
```cmd
run.bat
```

#### Linux/Mac
From terminal:
```bash
./run.sh
```

Or make it executable and double-click (if your file manager supports it):
```bash
chmod +x run.sh
./run.sh
```

## Features

✅ **Automatic Database Connection** - Connects to PRJ2531H schema
✅ **Error Handling** - Clear error messages if database isn't running
✅ **Timezone Support** - Properly configured for Oracle compatibility
✅ **Java Version Check** - Validates Java 8+ is installed
✅ **Directory Validation** - Confirms setup was completed successfully

## Troubleshooting

### "ERROR: Java is not installed"
Install Java:
- **Windows**: Download from https://www.oracle.com/java/
- **Linux**: Run `sudo apt-get install default-jre`
- **Mac**: Run `brew install openjdk`

### "ERROR: Database connection failed"
Ensure Oracle database is running:
```bash
# Check if running
podman ps | grep oracle
# or
docker ps | grep oracle

# Start if not running (Linux/Docker)
podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g
```

### "ERROR: bin/ directory not found"
Run the setup wizard again:
```bash
java -jar LMS-Setup.jar
```

### "Connection refused" or timeout
- Check Oracle is listening on `localhost:1521`
- Verify no firewall blocking port 1521
- Check Oracle service is running

## Admin Credentials

After setup, use these credentials to log in:
- **User ID**: ADMIN
- **Name**: Adi (or as entered during setup)
- **Password**: As entered during setup

## Configuration

To connect to a different Oracle database:

### Windows
Edit `run.bat` and change:
```batch
set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe
set LMS_DB_USER=PRJ2531H
set LMS_DB_PASSWORD=PRJ2531H
```

### Linux/Mac
Edit `run.sh` and change:
```bash
export LMS_DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"
```

## Reinstalling

To reinstall the database (loses all data):

### Prepare
```bash
# Connect as SYSTEM user and run cleanup
sqlplus system/oracle @cleanup_prj2531h.sql
```

### Re-run Setup
```bash
java -jar LMS-Setup.jar
```

## Support

For issues or questions:
1. Check the error message output
2. Verify Oracle database is running
3. Ensure Java is properly installed
4. Review logs in the terminal/command window

## File Structure
```
LMS/
├── run.sh                    # Linux/Mac launcher
├── run.bat                   # Windows launcher
├── LMS-Setup.jar            # Setup wizard
├── cleanup_prj2531h.sql     # Database cleanup script
├── bin/                      # Compiled Java classes
├── lib/                      # Required libraries
├── script.sql               # Database schema
└── src/                      # Source code
```

---

**Status**: ✅ Production Ready
**Last Updated**: 2026-04-09
**Version**: 1.0
