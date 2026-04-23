# LMS Setup: Oracle Credentials Configuration Guide

## Problem Summary

When installing LMS on different machines, the setup wizard fails with:
```
Could not connect as SYSTEM to reset PRJ2531H user. 
Tried system/oracle: ORA-01017 Invalid username/password; logon denied
```

**Root Cause**: The setup wizard was hardcoding `system/oracle` credentials, which only work on your development machine. Other machines have Oracle installed with different SYSTEM credentials.

## Solution Overview

The setup wizard now supports **configurable Oracle credentials** through environment variables and the `.env.setup` configuration file. You have three options:

### Option 1: Use .env.setup Configuration File (Recommended for Teams)

This is the **easiest and most portable** approach:

#### On Linux/Mac:
```bash
# 1. Copy the example file
cp .env.setup.example .env.setup

# 2. Edit it with your Oracle SYSTEM credentials
nano .env.setup
# Change:
#   LMS_SYSTEM_USER=system
#   LMS_SYSTEM_PASSWORD=your_actual_password

# 3. Run setup (environment is auto-loaded)
./setup-wizard.sh
```

#### On Windows:
```batch
REM 1. Copy the example file
copy .env.setup.example .env.setup

REM 2. Edit .env.setup with Notepad
notepad .env.setup
REM Change:
REM   LMS_SYSTEM_USER=system
REM   LMS_SYSTEM_PASSWORD=your_actual_password

REM 3. Run setup (environment is auto-loaded)
lms-setup-env.bat
```

### Option 2: Set Environment Variables Before Running Setup

#### On Linux/Mac:
```bash
export LMS_SYSTEM_USER='system'
export LMS_SYSTEM_PASSWORD='your_actual_password'
export LMS_DB_URL='jdbc:oracle:thin:@localhost:1521:xe'

./setup-wizard.sh
```

#### On Windows Command Prompt:
```batch
set LMS_SYSTEM_USER=system
set LMS_SYSTEM_PASSWORD=your_actual_password
set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe

lms-setup-env.bat
```

#### On Windows PowerShell:
```powershell
$env:LMS_SYSTEM_USER = 'system'
$env:LMS_SYSTEM_PASSWORD = 'your_actual_password'
$env:LMS_DB_URL = 'jdbc:oracle:thin:@localhost:1521:xe'

.\lms-setup-env.bat
```

### Option 3: Ask Your Oracle DBA

If you don't have the SYSTEM credentials:

1. **Ask your DBA to provide SYSTEM password** - This is the standard approach
2. **Alternatively**, ask them to run this SQL as SYSTEM:
   ```sql
   -- Create PRJ2531H user with all necessary privileges
   CREATE USER PRJ2531H IDENTIFIED BY "PRJ2531H";
   GRANT CONNECT, RESOURCE, DBA TO PRJ2531H;
   GRANT CREATE TABLE TO PRJ2531H;
   GRANT CREATE SEQUENCE TO PRJ2531H;
   GRANT CREATE TRIGGER TO PRJ2531H;
   GRANT CREATE ANY TRIGGER TO PRJ2531H;
   GRANT EXECUTE ON DBMS_SQL TO PRJ2531H;
   ```
   Then you can skip the setup and run the application directly.

## Configuration File Reference

The `.env.setup` file contains all database connection settings:

```bash
# Oracle connection details
LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe

# Oracle SYSTEM user credentials (REQUIRED for setup)
LMS_SYSTEM_USER=system
LMS_SYSTEM_PASSWORD=oracle        # Change this!

# PRJ2531H user credentials (application user)
LMS_DB_USER=PRJ2531H
LMS_DB_PASSWORD=PRJ2531H
```

### When to Use Different URLs

| Scenario | LMS_DB_URL |
|----------|-----------|
| Oracle XE (default) | `jdbc:oracle:thin:@localhost:1521:xe` |
| Oracle 11g Standard | `jdbc:oracle:thin:@localhost:1521:orcl` |
| Oracle on different port | `jdbc:oracle:thin:@localhost:1522:orcl` |
| Oracle on different host | `jdbc:oracle:thin:@192.168.1.100:1521:xe` |

## Security Considerations

⚠️ **Important**: 

1. **Never commit `.env.setup` to version control** - It contains database credentials
   - The `.env.setup` file is already in `.gitignore`
   - Only `.env.setup.example` (with placeholder values) is committed

2. **Different credentials per environment**:
   - Development: `system/oracle` (development machine)
   - Testing: `system/<test_password>` (test server)
   - Production: `system/<prod_password>` (production server)

3. **After setup completes**, you can delete `.env.setup` (or keep it for future re-installations)

## Troubleshooting

### Error: "Cannot find .env.setup.example"

**Solution**: Make sure you're running the setup from the project root directory where `.env.setup.example` is located.

### Error: "ORA-01017 Invalid username/password"

**Solution**: Your SYSTEM credentials are incorrect. Verify:
1. The username is correct (usually `system`)
2. The password is the actual Oracle SYSTEM password
3. Special characters in password need to be properly escaped

### Error: "ORA-01521 Cannot logon with SYSTEM"

**Solution**: The SYSTEM account is locked. Ask your DBA to unlock it:
```sql
ALTER USER system ACCOUNT UNLOCK;
```

### Error: "Cannot connect to Oracle at localhost:1521"

**Solution**: Oracle is not running or not listening on that port. 

Check:
- **Linux**: `lsof -i :1521` or `netstat -an | grep 1521`
- **Windows**: Check Services (services.msc) for OracleServiceXE or similar

Start Oracle:
- **Linux**: `sudo systemctl start oracle` (or similar)
- **Windows**: Start "OracleServiceXE" or "Oracle Database" service

## New Files Added

This update includes two new files:

1. **`.env.setup.example`** - Template for database credentials
   - Shows all available configuration options
   - Includes helpful comments and examples
   - Copy and edit this file to create `.env.setup`

2. **`lms-setup-env.bat`** (Windows) - Enhanced setup launcher
   - Automatically loads `.env.setup` if it exists
   - Better error messages and troubleshooting
   - Replaces the old `lms-setup.bat` for credential support

On Linux, the existing `setup-wizard.sh` has been updated to support `.env.setup` loading.

## Installation Steps Summary

### First Time Installation

```bash
# 1. Clone or download LMS project
# 2. Copy and configure credentials
cp .env.setup.example .env.setup
# Edit .env.setup with your Oracle SYSTEM credentials

# 3. Run setup
./setup-wizard.sh                  # Linux/Mac
# or
lms-setup-env.bat                  # Windows

# 4. Follow the GUI wizard
```

### Subsequent Installations (Same Machine)

If you already created `.env.setup`, just run:
```bash
./setup-wizard.sh                  # Linux/Mac
# or
lms-setup-env.bat                  # Windows
```

The credentials will be automatically loaded from `.env.setup`.

## See Also

- `APPLICATION_LAUNCH_GUIDE.md` - How to run LMS after installation
- `INSTALLATION_GUIDE.md` - Detailed installation documentation
- `.env.setup.example` - Configuration file with all options
