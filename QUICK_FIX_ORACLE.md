# Quick Fix: Oracle Credentials Setup

**Problem**: Setup fails on other machines with "Could not connect as SYSTEM"

**Solution**: Configure your Oracle SYSTEM credentials before running setup.

---

## 🚀 Quick Start (Choose Your OS)

### 🐧 Linux/Mac
```bash
# 1. Copy the config template
cp .env.setup.example .env.setup

# 2. Edit with your Oracle SYSTEM password
nano .env.setup
# Change: LMS_SYSTEM_PASSWORD=oracle → your actual password

# 3. Run setup
./setup-wizard.sh
```

### 🪟 Windows
```batch
REM 1. Copy the config template
copy .env.setup.example .env.setup

REM 2. Edit .env.setup with Notepad
notepad .env.setup
REM Change: LMS_SYSTEM_PASSWORD=oracle → your actual password

REM 3. Run setup
lms-setup-env.bat
```

---

## ❓ Don't know your SYSTEM password?

**Ask your Oracle DBA** for the SYSTEM user password. They can:
1. Provide you the password, OR
2. Create the PRJ2531H user themselves

---

## 📝 Configuration File

`.env.setup` should look like:
```
LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe
LMS_SYSTEM_USER=system
LMS_SYSTEM_PASSWORD=your_actual_password    ← Change this!
LMS_DB_USER=PRJ2531H
LMS_DB_PASSWORD=PRJ2531H
```

---

## 📚 Full Documentation

See `ORACLE_CREDENTIALS_SETUP.md` for:
- Detailed troubleshooting
- Different scenarios (CI/CD, team setups, etc.)
- Security best practices

---

## ✅ Verify It Works

After setup completes successfully:
```bash
# Run the application
./run.sh           # Linux/Mac
# or
run.bat            # Windows
```

You should see the LMS login screen.
