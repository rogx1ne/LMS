# 🚀 LMS Application - User-Friendly Launchers

## For End Users - No CLI Required!

### Windows Users
Simply **double-click** one of these files to run the application:

#### Option 1: Direct Launcher
- **File**: `run.bat`
- **Location**: In the LMS installation folder
- **Action**: Double-click → Application launches

#### Option 2: Desktop Shortcut
1. Double-click **`create-shortcut.bat`** to create a Desktop shortcut
2. Then simply **double-click** the **`LMS`** shortcut on your Desktop anytime

### Linux/Mac Users  
Simply **double-click** the launcher file:

#### Option 1: Direct Launcher
- **File**: `run.sh`
- **Location**: In the LMS installation folder
- **Action**: Double-click → Application launches
- (May need to set as executable if double-click doesn't work)

#### Option 2: Desktop Shortcut
1. Open terminal and run: `./create-shortcut.sh`
2. Then simply **double-click** the **`LMS`** shortcut on your Desktop anytime

#### Option 3: Terminal (Alternative)
```bash
./run.sh
```

---

## What Happens When You Launch

The launcher script automatically:

✅ **Checks Java Installation**
- Verifies Java 8+ is installed
- Shows clear error if Java is missing

✅ **Validates Installation**
- Checks that database tables were created
- Ensures all required files are present

✅ **Connects to Database**
- Automatically connects to PRJ2531H schema
- Shows error if database isn't running

✅ **Launches the GUI**
- Opens the Library Management System
- Ready to use!

---

## Troubleshooting

### Problem: "Java is not installed"
**Solution**: Install Java
- **Windows**: Download from oracle.com/java
- **Linux**: Run `sudo apt-get install default-jre`
- **Mac**: Run `brew install openjdk`

### Problem: "Database connection failed"
**Solution**: Start Oracle database
- Check Oracle is running on `localhost:1521`
- On Docker: `podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g`
- Check no firewall is blocking port 1521

### Problem: "bin/ directory not found"
**Solution**: Run setup first
```bash
java -jar LMS-Setup.jar
```

### Problem: Double-click doesn't work on Linux
**Solution**: Make it executable first
```bash
chmod +x run.sh
./run.sh
```

---

## Quick Start

1. **First Time Only**: Run `java -jar LMS-Setup.jar`
2. **After Setup**: Double-click `run.sh` (Linux) or `run.bat` (Windows)
3. **Log in** with credentials from setup
4. **Done!** Application is ready to use

---

## Files Overview

| File | Purpose | Windows | Linux/Mac |
|------|---------|---------|----------|
| `run.bat` | Direct launcher | ✅ | ❌ |
| `run.sh` | Direct launcher | ❌ | ✅ |
| `create-shortcut.bat` | Create Desktop icon | ✅ | ❌ |
| `create-shortcut.sh` | Create Desktop icon | ❌ | ✅ |
| `LMS-Setup.jar` | Initial setup (one-time) | ✅ | ✅ |

---

## No More Command Line!

After setup, you never need to open a terminal again:
- **Windows**: Just double-click `run.bat`
- **Linux/Mac**: Just double-click `run.sh`

That's it! 🎉

---

**Status**: ✅ Production Ready
**Last Updated**: 2026-04-09
