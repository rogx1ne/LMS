# LMS Installer - Quick Start Guide

## Installation

### Prerequisites
- Java 8 (JDK 1.8.x)
- Oracle Database 10g XE (or 11g/12c)

### Steps

1. **Download the installer**
   ```
   LMS-Setup.jar (112MB)
   ```

2. **Run the installer**
   ```bash
   # Linux
   java -jar LMS-Setup.jar
   
   # Windows
   java -jar LMS-Setup.jar
   # Or double-click LMS-Setup.jar
   ```

3. **Follow the wizard**
   - Page 1: Welcome (review system info)
   - Page 2: Choose installation directory
   - Page 3: Install missing dependencies (if needed)
   - Page 4: Configure database connection
   - Page 5: Create administrator account
   - Page 6: Wait for installation
   - Page 7: Launch LMS!

---

## Post-Installation

### Running LMS

**Linux:**
```bash
# From desktop shortcut
Click "Library Management System" icon

# From command line
cd /path/to/installation
./LMS-Launcher.sh
```

**Windows:**
```cmd
REM From desktop shortcut
Double-click "Library Management System"

REM From command line
cd C:\path\to\installation
LMS-Launcher.bat
```

### First Login
- Username: `admin` (or whatever you created during setup)
- Password: (the password you set)
- Role: ADMIN

---

## Uninstallation

Run the uninstaller:
```bash
# Linux
java -cp "bin:lib/*" com.library.setup.Uninstaller

# Windows
java -cp "bin;lib/*" com.library.setup.Uninstaller
```

Options:
- Keep database data ✓ (recommended)
- Remove database data ✗ (cannot be undone!)

---

## Troubleshooting

### "Could not find or load main class"
- Ensure Java 8 is installed: `java -version`
- Check JAVA_HOME environment variable

### "Connection failed" during database setup
- Verify Oracle is running: Check listener status
- Default connection: `localhost:1521:xe`
- Check firewall settings

### Dependencies not found
- Use the auto-installer in the wizard (Page 3)
- Or install manually:
  - **Linux**: `sudo pacman -S jdk8-openjdk` (Arch)
  - **Linux**: `sudo apt install openjdk-8-jdk` (Debian/Ubuntu)
  - **Windows**: Download from Oracle website

### Application won't launch
- Check logs in installation directory
- Ensure all dependencies are present in `lib/` folder
- Verify `bin/` contains compiled classes

---

## Developer Mode

### Rebuild the installer
```bash
# Linux
./package-setup.sh

# Windows
package-setup.bat
```

### Run without installing
```bash
./run.sh  # Compiles and runs
```

---

## File Locations

**Linux:**
- Installation: `/opt/lms` or `~/lms` (user choice)
- Desktop shortcut: `~/Desktop/LMS.desktop`
- Menu entry: `~/.local/share/applications/lms.desktop`
- Launcher: `/opt/lms/LMS-Launcher.sh`

**Windows:**
- Installation: `C:\Program Files\LMS` or custom location
- Desktop shortcut: `Desktop\Library Management System.lnk`
- Start Menu: `Start Menu\Programs\Library Management System\`
- Launcher: `C:\Program Files\LMS\LMS-Launcher.bat`

---

## Support

For issues or questions:
1. Check `PROJECT_LOG.md` for recent changes
2. Review `AGENTS.md` for system architecture
3. Examine `IMPLEMENTATION_SUMMARY.md` for feature documentation
