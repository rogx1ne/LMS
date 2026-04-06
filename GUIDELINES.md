# LMS Development Guidelines & Standards

**Last Updated:** 2026-04-06  
**Version:** 2.0.2

---

## Table of Contents
1. [Code Conventions](#1-code-conventions)
2. [Database Rules](#2-database-rules)
3. [Security Requirements](#3-security-requirements)
4. [Operational Mandates](#4-operational-mandates)
5. [Architecture Patterns](#5-architecture-patterns)
6. [Testing & Verification](#6-testing--verification)
7. [Deployment Checklist](#7-deployment-checklist)

---

## 1. Code Conventions

### Java Style
- **Naming:** CamelCase for classes, camelCase for variables
- **Constants:** UPPER_SNAKE_CASE
- **Method names:** Verb-based (getName, setStatus, calculateFine)
- **Comment:** Only clarify complex logic, not obvious statements
- **String quotes:** Double quotes (standard Java)
- **Indentation:** 4 spaces (no tabs)

### File Organization
```
src/com/library/
├── Main.java                (Entry point - no package)
├── dao/                     (Data Access Objects)
│   ├── BookDAO.java
│   ├── StudentDAO.java
│   ├── UserDAO.java
│   ├── CirculationDAO.java
│   ├── AdminDAO.java
│   ├── IdCounterService.java
│   └── TransactionDAO.java
├── model/                   (POJOs - Plain Old Java Objects)
│   ├── Book.java
│   ├── Student.java
│   ├── User.java
│   ├── Issue.java
│   └── AuditLog.java
├── service/                 (Business Logic & Services)
│   ├── BookLogic.java
│   ├── StudentLogic.java
│   ├── CirculationLogic.java
│   ├── PasswordHasher.java
│   ├── AuditLogger.java
│   ├── PdfReportService.java
│   ├── StudentPdfService.java
│   └── ExcelExportService.java
├── ui/                      (Java Swing UI Components)
│   ├── LoginFrame.java
│   ├── DashboardPanel.java
│   ├── BookManagementUI.java
│   ├── StudentManagementUI.java
│   ├── CirculationPanel.java
│   └── AdminPanel.java
├── setup/                   (Installation & Setup)
│   ├── LMSSetupWizard.java
│   ├── InstallationManager.java
│   ├── UninstallationManager.java
│   └── SetupWizard.java (deprecated)
└── database/                (Database utilities)
    └── DatabaseUtil.java
```

### Package Guidelines
- **dao:** Direct JDBC operations, connection management
- **model:** Data transfer objects, minimal logic
- **service:** Business rules, validation, logging, reporting
- **ui:** Swing components, user interaction, display logic
- **setup:** Installation/configuration logic

---

## 2. Database Rules

### General Principles
- **Compatibility:** Oracle 10g XE target (avoid modern features)
- **IDs:** Always use TBL_ID_COUNTER, never MAX(ID)+1
- **Timestamps:** Use SYSTIMESTAMP (automatic timezone handling)
- **Transactions:** BEGIN/COMMIT/ROLLBACK for multi-step operations
- **Null Handling:** Explicit IS NULL / IS NOT NULL checks

### User ID Handling
- **CHAR(5) Padding:** USER_ID is CHAR(5) - pads with spaces
- **Comparison Rule:** Always use TRIM() in WHERE clauses
- **Example:**
  ```sql
  -- WRONG: Fails if USER_ID has trailing spaces
  SELECT * FROM TBL_CREDENTIALS WHERE USER_ID = 'ADMIN'
  
  -- RIGHT: TRIM handles padding
  SELECT * FROM TBL_CREDENTIALS WHERE USER_ID = TRIM('ADMIN')
  ```

### Constraint Naming
- **Primary Key:** `<TABLE>_PK` (e.g., `BOOK_CAT_PK`)
- **Foreign Key:** `FK_<TABLE>_<REF_TABLE>` (e.g., `FK_STOCK_CATALOG`)
- **Unique:** `UK_<TABLE>_<COLS>` (e.g., `UK_STUD_ROLL_COURSE_SESS`)
- **Check:** `CHK_<TABLE>_<CONDITION>` (e.g., `CHK_BOOK_STATUS`)
- **Index:** `IDX_<TABLE>_<COLS>` (e.g., `IDX_AUDIT_USER`)

### Schema Safety
- Use anonymous PL/SQL blocks for drop-if-exists:
  ```sql
  BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE TBL_BOOK';
  EXCEPTION
    WHEN OTHERS THEN
      IF SQLCODE != -942 THEN
        RAISE;
      END IF;
  END;
  /
  ```
- Never hardcode DROP SCHEMA statements
- Always add constraint checks (CHK_*) for status values

### Audit Trail
- **Mandatory logging for:** INSERTs, UPDATEs, DELETEs
- **Table:** TBL_AUDIT_LOG
- **Fields:** LOG_ID, USER_ID, MODULE, ACTION_DESCRIPTION, LOG_TS
- **Automation:** Use triggers or explicit logging in DAO
- **Timestamp:** Always SYSTIMESTAMP (never SYSDATE for new code)

---

## 3. Security Requirements

### Password Security
- **Algorithm:** SHA-256 (via PasswordHasher.hashPassword())
- **Storage:** Never store plain-text in database
- **Never Log:** Don't log passwords even in debug mode
- **Hashing Tool:** `com.library.service.PasswordHasher`
  ```java
  String hashedPassword = PasswordHasher.hashPassword("admin123");
  ```

### Input Validation
- **All User Input:** Validate before DB operations
- **SQL Injection:** Always use PreparedStatement (never string concatenation)
- **File Upload:** Validate file type, size, content
- **Cross-Site Scripting (XSS):** Escape HTML characters in reports

### Secrets Management
- **No Hardcoded Credentials:** Use environment variables
- **Example:**
  ```java
  String dbUrl = System.getenv("LMS_DB_URL");
  String dbUser = System.getenv("LMS_DB_USER");
  String dbPassword = System.getenv("LMS_DB_PASSWORD");
  ```
- **Defaults:** Only for development
  ```java
  if (dbUrl == null) dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
  ```

### Role-Based Access Control (RBAC)
- **Roles:** ADMIN, LIBRARIAN
- **Check:** Always verify ROLE before sensitive operations
- **Example:**
  ```java
  if (!"ADMIN".equals(currentUser.getRole())) {
    throw new UnauthorizedException("Admin access required");
  }
  ```

### Audit Logging
- **Sensitive Operations:** Log all admin actions
- **Service:** Use AuditLogger.log(userId, module, action)
- **Examples:**
  - New user creation
  - Role changes
  - Report generation
  - System configuration changes
  - Book deletion
  - Student fee adjustments

---

## 4. Operational Mandates

### Build & Compilation
- **Java Target:** `--release 8` (Java 8 bytecode, runs on Java 8+)
- **Build Command:**
  ```bash
  javac --release 8 -d bin -cp "lib/*" src/com/library/**/*.java
  ```
- **JAR Creation:** Use MANIFEST-SETUP.MF with correct Main-Class
- **Fat JAR:** All dependencies embedded (no external lib/ needed at runtime)

### Setup Wizard Execution
- **Installation:** Runs 6-step wizard
  1. Welcome
  2. Installation path selection
  3. System prerequisites check
  4. Admin credentials
  5. Installation progress
  6. Completion
- **File Copying:** Setup copies bin/ and lib/ from source
- **Database:** Executes script.sql automatically
- **User Creation:** Deletes old users, creates only setup-provided admin

### Database Connection
- **Connection String:** `jdbc:oracle:thin:@localhost:1521:xe`
- **Default Credentials:** `PRJ2531H/PRJ2531H`
- **Timezone Fix:** `-Doracle.jdbc.timezoneAsRegion=false` (JVM flag)
- **Connection Timeout:** Implement retry logic (max 3 attempts)

### Application Execution
- **Linux/macOS:** `cd /installation/path && ./run.sh`
- **Windows:** `cd /installation/path && run.bat`
- **Environment Variables:** Set automatically by launcher scripts

### Maintenance
- **Audit Log Cleanup:** Periodic deletion of old audit records
- **Schema Backup:** Daily backup of TBL_* tables
- **Log Files:** Rotate application logs monthly
- **Dependencies:** Check for security updates quarterly

---

## 5. Architecture Patterns

### DAO Pattern
- Each DAO handles one entity (BookDAO, StudentDAO, etc.)
- Methods: get(), getAll(), add(), update(), delete()
- Use PreparedStatement for all queries
- Manage connection lifecycle per operation

### Service Layer Pattern
- Validation before DAO calls
- Transaction handling (multiple DAO calls)
- Business logic (calculations, eligibility checks)
- Audit logging
- Exception handling and re-throwing

### MVC-like Pattern
- **Model:** POJOs (Book, Student, Issue)
- **View:** Swing UI panels and dialogs
- **Controller:** Service layer logic
- **Data:** DAO layer to database

### Error Handling
- Checked exceptions for expected errors
- Runtime exceptions for unexpected errors
- Always log exceptions with context
- Show user-friendly messages in UI

---

## 6. Testing & Verification

### Compilation Test
```bash
cd /home/abhiadi/mine/clg/LMS
./run.sh  # Builds and runs Main.java
```

### Database Connectivity
```bash
# Verify Oracle is running
podman ps | grep oracle

# Check connection
java -cp "bin:lib/*" TestConnection
```

### Setup Wizard Test
```bash
cd /home/abhiadi/mine/clg/LMS
java -jar LMS-Setup.jar
# Follow 6-step wizard
```

### Application Test
```bash
# After installation
cd /home/user/LMS
./run.sh
# Login with setup credentials
# Verify all modules work
```

### Audit Trail Verification
```sql
-- Check audit logs
SELECT COUNT(*) FROM TBL_AUDIT_LOG;
SELECT * FROM TBL_AUDIT_LOG ORDER BY LOG_TS DESC FETCH FIRST 10 ROWS ONLY;
```

---

## 7. Deployment Checklist

### Pre-Deployment
- [ ] All source files in `src/` folder
- [ ] All dependencies in `lib/` folder
- [ ] `script.sql` has no hardcoded user credentials
- [ ] `dummy.sql` has test data only
- [ ] Compilation successful: `javac --release 8 -d bin -cp "lib/*" src/com/library/**/*.java`
- [ ] No plain-text passwords in code or comments
- [ ] All audit logging implemented for sensitive operations
- [ ] Security audit completed (see SECURITY_AUDIT_REPORT.md)

### JAR Packaging
- [ ] MANIFEST-SETUP.MF has correct Main-Class: `com.library.setup.LMSSetupWizard`
- [ ] MANIFEST-SETUP.MF has all dependencies in Class-Path
- [ ] FAT JAR created: `jar cfm LMS-Setup.jar MANIFEST-SETUP.MF -C temp .`
- [ ] JAR size >= 100MB (includes all dependencies)
- [ ] Made executable: `chmod +x LMS-Setup.jar`

### Distribution Package
- [ ] Create `LMS-Setup-Distribution.zip` with:
  - [ ] `LMS-Setup.jar` (112M)
  - [ ] `bin/` folder (compiled classes)
  - [ ] `lib/` folder (all JARs)
  - [ ] `script.sql`
  - [ ] `dummy.sql`
  - [ ] `README.md`
  - [ ] `SETUP_QUICK_START.md`
  - [ ] `WINDOWS_COMPATIBILITY.md`

### Post-Deployment Verification
- [ ] Extract ZIP to test directory
- [ ] Run setup wizard: `java -jar LMS-Setup.jar`
- [ ] Follow 6-step installation
- [ ] Verify installed directory has:
  - [ ] `bin/` with .class files
  - [ ] `lib/` with .jar files
  - [ ] `run.sh` or `run.bat`
- [ ] Launch application: `./run.sh`
- [ ] Login with setup credentials
- [ ] Test key workflows:
  - [ ] Book issue/return
  - [ ] Student registration
  - [ ] Report generation
  - [ ] User management (admin)
- [ ] Verify audit logs created
- [ ] Check database constraints working

### Documentation
- [ ] CHANGELOG.md updated with release notes
- [ ] README.md reflects current version
- [ ] ARCHITECTURE.md up-to-date
- [ ] GUIDELINES.md (this file) current
- [ ] User guides in `docs/`

---

## Version Control

### Git Workflow
- Commit frequently with clear messages
- Use meaningful branch names: `feature/xyz`, `bugfix/xyz`
- Include issue numbers in commit messages
- Never commit `.env`, `bin/`, `*.jar`, `.idea/`

### Commit Message Format
```
[AREA] Brief description

Detailed explanation:
- What changed
- Why it changed
- Any special notes

Fixes: #issue-number
```

### .gitignore Essentials
```
# Compiled
bin/
*.class

# JARs (except lib/)
LMS-Setup.jar
*.jar
!lib/*.jar

# Environment
.env
*.log

# IDE
.idea/
*.iml
.vscode/

# OS
.DS_Store
Thumbs.db
```

---

## Quick Reference

### File Locations
- **Source:** `/src/com/library/`
- **Classes:** `/bin/com/library/`
- **Dependencies:** `/lib/`
- **Database:** `script.sql`, `dummy.sql`
- **Documentation:** `/docs/` + root `.md` files
- **Launchers:** `run.sh`, `run.bat`

### Key Classes
- **Entry:** `Main` (no package)
- **Setup:** `LMSSetupWizard`, `InstallationManager`
- **Security:** `PasswordHasher`, `AuditLogger`
- **Services:** `BookLogic`, `StudentLogic`, `CirculationLogic`
- **DAOs:** `BookDAO`, `StudentDAO`, `CirculationDAO`, `UserDAO`

### Database Connection Info
- **Default URL:** `jdbc:oracle:thin:@localhost:1521:xe`
- **Default User:** `PRJ2531H`
- **Default Pass:** `PRJ2531H`
- **SID:** `xe` (XE 10g)

### Build Commands
```bash
# Compile
javac --release 8 -d bin -cp "lib/*" src/com/library/**/*.java

# Run application
cd /installation/path && ./run.sh

# Create setup JAR
./package-setup.sh

# Clean
rm -rf bin/* temp-setup-jar/
```

---

## Support & References

- **Architecture:** See `ARCHITECTURE.md`
- **Setup Guide:** See `docs/SETUP_WIZARD_COMPLETE.md`
- **Security:** See `docs/SECURITY_AUDIT_REPORT.md`
- **Production:** See `docs/PRODUCTION_SETUP_GUIDE.md`
- **Troubleshooting:** See `docs/SETUP_QUICK_START.md`
- **Windows:** See `docs/WINDOWS_COMPATIBILITY.md`

