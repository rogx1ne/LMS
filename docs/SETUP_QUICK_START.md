# LMS Setup Wizard - Quick Start Guide

**Cross-Platform:** Works on Windows, Linux, and macOS  
**Version:** 2.0.1

---

## ✅ Prerequisites Checklist

Before running the setup wizard, ensure you have:

### 1. **Java 8 or Higher**

**Linux/macOS:**
```bash
java -version
# Should show version 1.8.x or higher
```

**Windows:**
```batch
java -version
REM Should show version 1.8.x or higher
```

If Java is not installed, download from: https://www.oracle.com/java/technologies/downloads/

### 2. **Oracle Database Container Running**

**Linux/macOS (Podman):**
```bash
# Check if running
podman ps | grep oracle

# If not running, start it:
podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g

# Wait 30-60 seconds for Oracle to fully initialize
```

**Windows (Docker Desktop):**
```batch
REM Check if running
docker ps | findstr oracle

REM If not running, start it:
docker run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g

REM Wait 30-60 seconds for Oracle to fully initialize
```

3. **Required Files in Project Directory**
   - `LMS-Setup-2.0.jar` (394 KB)
   - `lib/` directory with all dependencies
   - `lib/ojdbc6.jar` (CRITICAL - Oracle JDBC driver)

---

## 🚀 Installation Steps

### Method 1: Using Launcher Script (RECOMMENDED)

**Linux/macOS:**
```bash
cd /path/to/LMS
./lms-setup.sh
```

**Windows:**
```batch
cd C:\path\to\LMS
lms-setup.bat
```

The launcher will:
- ✓ Check Java installation
- ✓ Verify JAR and dependencies
- ✓ Check Oracle container status
- ✓ Launch setup wizard with correct configuration

---

### Method 2: Direct JAR Execution

**Linux/macOS:**
```bash
# IMPORTANT: Run from LMS project directory (where lib/ folder is)
cd /path/to/LMS
java -Doracle.jdbc.timezoneAsRegion=false -jar LMS-Setup-2.0.jar
```

**Windows:**
```batch
REM IMPORTANT: Run from LMS project directory (where lib/ folder is)
cd C:\path\to\LMS
java -Doracle.jdbc.timezoneAsRegion=false -jar LMS-Setup-2.0.jar
```

**⚠️ CRITICAL:** You MUST run the JAR from the LMS project root directory where the `lib/` folder is located. The manifest uses relative paths (`lib/ojdbc6.jar`).

---

## 📋 Setup Wizard Steps

Follow these 6 steps in the wizard:

### Step 1: Welcome Screen
- Read the welcome message
- Click "Next"

### Step 2: Installation Location
- Default: `/home/<user>/LMS`
- Or click "Browse" to choose custom location
- Click "Next"

### Step 3: System Requirements Check
The wizard automatically checks:
- ✓ Java version (8+)
- ✓ Oracle database connection
- ✓ Required directories writable
- ✓ Sufficient disk space

**If Oracle check fails**, see Troubleshooting below.

### Step 4: Admin User Setup
- Enter admin username (e.g., `admin`)
- Enter password (min 6 characters)
- Confirm password
- Click "Next"

### Step 5: Installation Progress
Automatic execution:
1. Creating directories
2. Copying files
3. Generating launcher scripts
4. **Executing script.sql** (creates all tables)
5. Creating admin user

### Step 6: Completion
- ✓ Installation successful
- Click "Finish"

---

## 🎯 Post-Installation

After successful installation:

**Linux/macOS:**
```bash
# Navigate to installation directory
cd /home/<user>/LMS  # or /Users/<user>/LMS on macOS

# Launch LMS application
./run.sh
```

**Windows:**
```batch
REM Navigate to installation directory
cd C:\Users\<user>\LMS

REM Launch LMS application
run.bat
```

---

## 🔧 Troubleshooting

### ❌ Error: "No suitable driver found for jdbc:oracle:thin:@localhost:1521:xe"

**Cause:** Oracle JDBC driver not in classpath.

**Solution:**
1. Verify `lib/ojdbc6.jar` exists:
   ```bash
   ls -lh lib/ojdbc6.jar
   # Should show ~2.7MB file
   ```

2. **CRITICAL:** Run JAR from LMS project directory:
   ```bash
   # BAD (won't work):
   java -jar /path/to/LMS-Setup-2.0.jar
   
   # GOOD:
   cd /path/to/LMS
   java -jar LMS-Setup-2.0.jar
   ```

3. Use the launcher script instead:
   ```bash
   ./lms-setup.sh
   ```

---

### ❌ Error: "Failed to connect to Oracle after 3 attempts"

**Cause:** Oracle container not running or not ready.

**Solution:**

1. **Check if Oracle is running:**
   ```bash
   podman ps | grep oracle
   ```

2. **If not running, start it:**
   ```bash
   podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g
   ```

3. **Wait for Oracle to initialize** (30-60 seconds):
   ```bash
   # Watch the logs
   podman logs -f oracle10g
   # Wait for "DATABASE IS READY TO USE!"
   ```

4. **Test connection manually:**
   ```bash
   # Using SQLPlus (if installed)
   sqlplus PRJ2531H/PRJ2531H@localhost:1521/xe
   
   # Or test with Java
   java -cp "lib/*" com.library.database.DBConnection
   ```

5. **Retry the setup wizard**

---

### ❌ Error: "ORA-01882: timezone region not found"

**Cause:** Oracle 10g doesn't support modern timezone regions.

**Solution:** Use the launcher script which includes the fix:
```bash
./lms-setup.sh
```

Or manually add the JVM flag:
```bash
java -Doracle.jdbc.timezoneAsRegion=false -jar LMS-Setup-2.0.jar
```

---

### ❌ Error: "Permission denied" when creating directories

**Cause:** Insufficient write permissions on target directory.

**Solution:**
1. Choose a different installation directory where you have write access
2. Or grant permissions:
   ```bash
   mkdir -p /desired/path
   chmod 755 /desired/path
   ```

---

### ❌ Setup wizard window doesn't appear

**Cause:** No GUI environment (running on headless server).

**Solution:**
LMS requires a graphical environment. Use:
- Local machine with GUI
- VNC/Remote Desktop connection
- X11 forwarding over SSH:
  ```bash
  ssh -X user@server
  ./lms-setup.sh
  ```

---

## 🗑️ Uninstallation

To completely remove LMS:

```bash
cd /path/to/LMS
./uninstall.sh
```

This will:
- Drop all database tables and sequences
- Remove all application files
- Delete installation directory
- Clean up completely

**⚠️ WARNING:** This action is irreversible!

---

## 📞 Support

For issues not covered here:

1. Check the logs in installation directory:
   - `setup.log` (if created)
   - `installation.log`

2. Verify environment:
   ```bash
   java -version
   podman ps
   ls -lh lib/ojdbc6.jar
   pwd  # Should be in LMS directory
   ```

3. Review security audit report:
   - `SECURITY_AUDIT_REPORT.md`

---

## 📊 System Requirements

**Minimum:**
- Java: 8+ (JDK or JRE)
- RAM: 512 MB
- Disk: 100 MB for installation
- OS: Linux, macOS, Windows
- Database: Oracle 10g XE (in Podman container)

**Recommended:**
- Java: 11+
- RAM: 1 GB
- Disk: 500 MB for data
- Network: Localhost access to port 1521

---

## ✅ Success Indicators

Installation is successful when:

1. ✓ Setup wizard completes all 6 steps
2. ✓ "Installation Complete" message appears
3. ✓ `run.sh` script exists in installation directory
4. ✓ Application launches with `./run.sh`
5. ✓ Can login with admin credentials created during setup

---

## 🔑 Default Configuration

After installation:

- **Database Connection:**
  - Host: localhost
  - Port: 1521
  - SID: xe
  - Schema: PRJ2531H

- **Installation Directory:**
  - Default: `/home/<user>/LMS`
  - Contains: `bin/`, `lib/`, `src/`, `run.sh`

- **Admin User:**
  - Username: (as set during installation)
  - Password: (as set during installation, SHA-256 hashed)

---

**Version:** 2.0.1  
**Last Updated:** 2026-04-06  
**Status:** Production Ready
