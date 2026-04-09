# LMS Installation Guide

## Prerequisites
1. Oracle Database 10g Express Edition or newer running
2. Database accessible at: `jdbc:oracle:thin:@localhost:1521:xe`
3. Java 8+ installed

## First-Time Installation

### Step 1: Clean Database (if reinstalling)
If tables from a previous installation exist, you must clean them first:

**Option A: Using SQL*Plus**
```bash
sqlplus system/manager@localhost:1521:xe

SQL> BEGIN
  2    EXECUTE IMMEDIATE 'DROP USER PRJ2531H CASCADE';
  3  EXCEPTION
  4    WHEN OTHERS THEN
  5      IF SQLCODE != -1918 THEN RAISE; END IF;
  6  END;
  7  /
SQL> EXIT;
```

**Option B: Let script.sql handle it**
If running script.sql directly via SQL*Plus, it includes session killing logic.

### Step 2: Run Setup Wizard
```bash
java -jar LMS-Setup.jar
```

The wizard will:
1. Create PRJ2531H user (if it doesn't exist)
2. Create all tables and indexes
3. Set up admin credentials
4. Generate launcher scripts

### Step 3: Verify Installation
```bash
java -Duser.timezone=UTC -cp bin:lib/ojdbc6.jar TestDB
```

Output should show:
```
✓ Connected to PRJ2531H schema
  Tables: 13
  Sequences: 2
  Admin records: 1
✓ Database verification successful!
```

### Step 4: Run Application
```bash
./run.sh       # Linux/macOS
.\run.bat      # Windows
```

## Troubleshooting

### Error: ORA-00955: name is already used by an existing object
**Cause**: Tables exist from a previous failed installation
**Solution**: Drop the user and retry:
```bash
sqlplus system/oracle@localhost:1521:xe
SQL> BEGIN
  2    FOR sess IN (SELECT sid, serial# FROM v$session WHERE username = 'PRJ2531H') LOOP
  3      EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || sess.sid || ',' || sess.serial# || ''' IMMEDIATE';
  4    END LOOP;
  5  END;
  6  /
SQL> DROP USER PRJ2531H CASCADE;
SQL> EXIT;
```

Then retry: `java -jar LMS-Setup.jar`

### Error: IO Error: Connection refused
**Cause**: Oracle database not running
**Solution**: Start Oracle:
```bash
podman ps | grep oracle  # Check if running
podman run -d --name oracle-xe -p 1521:1521 gvenzl/oracle-xe
```

### Error: ORA-01918: user does not exist (on script.sql)
This is **NOT an error** - it means it's the first installation. The setup wizard handles this automatically.

## Environment Variables

Customize connection:
```bash
export LMS_DB_URL="jdbc:oracle:thin:@myserver:1521:xe"
export LMS_DB_USER="PRJ2531H"
export LMS_DB_PASSWORD="PRJ2531H"
java -jar LMS-Setup.jar
```

## Security Notes

⚠️ **WARNING**: Credentials PRJ2531H/PRJ2531H are defaults for development.
For production, change them in your database:
```bash
sqlplus system/manager@localhost:1521:xe
SQL> ALTER USER PRJ2531H IDENTIFIED BY strong_password;
```

Then update the launcher scripts with the new password.
