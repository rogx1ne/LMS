# LMS Configuration Guide

## System Configuration Overview

This document describes all configurable aspects of the Library Management System and how to set them up for your deployment.

---

## 1. Database Configuration

### Oracle Database Setup

**Minimum Requirements:**
- Oracle Database 10g XE or higher
- User account with privileges to create tables and sequences
- TCP/IP connectivity on port 1521 (default)

### Environment Variables

Set these variables before running the application:

```bash
# Database Connection
export LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe
export LMS_DB_USER=PRJ2531H
export LMS_DB_PASSWORD=PRJ2531H

# Or in Windows (CMD)
set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe
set LMS_DB_USER=PRJ2531H
set LMS_DB_PASSWORD=PRJ2531H
```

### Manual Configuration (Code-based)

Edit `src/com/library/database/DBConnection.java`:

```java
// Database Configuration
private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";
private static final String DB_USER = "PRJ2531H";
private static final String DB_PASSWORD = "PRJ2531H";
```

### Initial Schema Setup

1. Connect to Oracle as a DBA user:
   ```bash
   sqlplus / as sysdba
   ```

2. Create the tablespace and user:
   ```sql
   CREATE TABLESPACE ts_lms 
   DATAFILE '/path/to/datafile/ts_lms.dbf' 
   SIZE 100M;
   
   CREATE USER PRJ2531H IDENTIFIED BY PRJ2531H
   DEFAULT TABLESPACE ts_lms
   QUOTA UNLIMITED ON ts_lms;
   
   GRANT CONNECT, RESOURCE TO PRJ2531H;
   ```

3. Run the schema script:
   ```bash
   sqlplus PRJ2531H/PRJ2531H@xe
   @script.sql
   ```

4. (Optional) Load demo data:
   ```bash
   @dummy.sql
   ```

---

## 2. Email Configuration (OTP Recovery)

### .env Setup

Copy `.env.example` to `.env` and configure your SMTP settings:

```bash
cp .env.example .env
```

### .env Configuration

Edit `.env` with your SMTP provider details:

```properties
# SMTP Configuration for Password Recovery OTP
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
SMTP_FROM_EMAIL=lms-system@your-domain.com
SMTP_FROM_NAME=LMS Administrator

# For Gmail, use App Password (not regular password)
# Enable 2FA and generate App Password at myaccount.google.com/apppasswords

# For Office 365
# SMTP_HOST=smtp.office365.com
# SMTP_PORT=587
# SMTP_USERNAME=your-email@your-organization.com

# For Corporate SMTP
# SMTP_HOST=smtp.your-organization.com
# SMTP_PORT=25  (or 587 with TLS)
# SMTP_USERNAME=username
# SMTP_PASSWORD=password
```

### Testing Email Configuration

After configuring `.env`:
1. Launch the application
2. Try "Forgot Password" from login screen
3. Verify email is received at the registered email address

---

## 3. Application Configuration

### Memory Settings

For large library operations, adjust JVM memory:

**Linux:**
```bash
# In run.sh, modify:
java -Xms512m -Xmx2g -cp "lib/*:bin" com.library.ui.MainFrame
```

**Windows:**
```batch
REM In run.bat, modify:
java -Xms512m -Xmx2g -cp "lib/*;bin" com.library.ui.MainFrame
```

**Recommendation:**
- `-Xms512m` : Initial heap size (512 MB)
- `-Xmx2g` : Maximum heap size (2 GB)
- Adjust based on your system resources

### Library Policy Configuration

Edit `src/com/library/service/CirculationPolicy.java` to customize:

```java
// Book Issue Settings
public static final int DEFAULT_ISSUE_DAYS = 14;
public static final int MAX_BOOKS_STUDENT = 5;
public static final int MAX_BOOKS_REFERENCE = 0;

// Fine Settings
public static final BigDecimal FINE_PER_DAY = new BigDecimal("2.00");
public static final BigDecimal MAX_FINE = new BigDecimal("100.00");

// Holiday Configuration
public static final LocalDate[] HOLIDAYS = {
    LocalDate.of(2026, 1, 26),  // Republic Day
    LocalDate.of(2026, 3, 8),   // Women's Day
    // Add more holidays as needed
};
```

### Reporting Configuration

#### PDF Report Branding

Edit `src/com/library/service/PdfReportService.java`:

```java
// Header Configuration
private static final String COLLEGE_NAME = "Your College Name";
private static final String COLLEGE_LOGO_PATH = "path/to/logo.png";
private static final String REPORT_HEADER = "Library Management System";

// Footer Configuration
private static final String PREPARED_BY = "System Administrator";
private static final String AUTHORIZED_BY = "Library Director";
```

#### Excel Export Settings

Configure in `src/com/library/service/ExcelService.java`:

```java
// Excel Sheet Formatting
private static final String SHEET_HEADER_COLOR = "FFD966";  // Yellow
private static final String SHEET_BORDER_COLOR = "000000";   // Black
private static final int DEFAULT_COLUMN_WIDTH = 15;
```

---

## 4. Audit Logging Configuration

### Audit Log Retention

Configure in `src/com/library/service/AuditLogger.java`:

```java
// Audit Log Retention Period (days)
private static final int RETENTION_DAYS = 365;  // 1 year

// Log Level Configuration
public enum LogLevel {
    INFO,      // Normal operations
    WARNING,   // Unusual activities
    ERROR,     // Failed operations
    CRITICAL   // Security incidents
}
```

### Audit Log Table

The system automatically creates and maintains `TBL_AUDIT_LOG` with:

```sql
CREATE TABLE TBL_AUDIT_LOG (
    AUDIT_ID NUMBER PRIMARY KEY,
    USER_ID VARCHAR2(50),
    OPERATION VARCHAR2(100),
    MODULE VARCHAR2(50),
    OPERATION_DATE TIMESTAMP,
    DESCRIPTION VARCHAR2(500),
    STATUS VARCHAR2(20)
);
```

---

## 5. UI Configuration

### Look & Feel

#### Theme Configuration

Edit `src/com/library/ui/ModuleTheme.java`:

```java
// Color Scheme
public static final Color PRIMARY_COLOR = new Color(41, 128, 185);      // Blue
public static final Color SECONDARY_COLOR = new Color(44, 62, 80);      // Dark Gray
public static final Color ACCENT_COLOR = new Color(231, 76, 60);        // Red
public static final Color SUCCESS_COLOR = new Color(39, 174, 96);       // Green
public static final Color WARNING_COLOR = new Color(241, 196, 15);      // Yellow
public static final Color ERROR_COLOR = new Color(192, 57, 43);         // Dark Red

// Font Configuration
public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 16);
public static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 10);
```

#### Window Size Configuration

Edit `src/com/library/ui/DashboardFrame.java`:

```java
// Main Window Dimensions
private static final int DEFAULT_WIDTH = 1400;
private static final int DEFAULT_HEIGHT = 900;
private static final boolean RESIZABLE = true;
```

### Logging Configuration

Edit `src/log4j2.xml` (or create if not present):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <File name="FileAppender" fileName="logs/lms.log">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c{1} - %m%n"/>
        </File>
        <Console name="ConsoleAppender" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="FileAppender"/>
            <AppenderRef ref="ConsoleAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

---

## 6. Performance Tuning

### Database Connection Pool

Configure in `src/com/library/database/DBConnection.java`:

```java
// Connection Pool Settings
private static final int MAX_CONNECTIONS = 20;
private static final int MIN_CONNECTIONS = 5;
private static final int CONNECTION_TIMEOUT = 30000;  // 30 seconds

// Connection String with Pool Parameters
String connString = "jdbc:oracle:thin:@localhost:1521:xe" +
    "?MaxPoolSize=" + MAX_CONNECTIONS +
    "&MinPoolSize=" + MIN_CONNECTIONS +
    "&ConnectionTimeout=" + CONNECTION_TIMEOUT;
```

### Query Optimization

For better performance with large datasets:

1. **Indexing:** Ensure critical tables have indexes:
   ```sql
   CREATE INDEX idx_book_title ON TBL_BOOK(BOOK_TITLE);
   CREATE INDEX idx_student_email ON TBL_STUDENT(EMAIL);
   CREATE INDEX idx_transaction_date ON TBL_COPY_TRANSACTION(TRANS_DATE);
   ```

2. **Query Pagination:** Results are paginated by default (50 records per page)

3. **Caching:** Consider enabling query result caching for read-only reports

---

## 7. Backup & Recovery

### Database Backup

**Daily Backup Script:**

```bash
#!/bin/bash
BACKUP_DIR="/backups/lms"
BACKUP_FILE="$BACKUP_DIR/lms_backup_$(date +%Y%m%d_%H%M%S).dmp"

expdp PRJ2531H/PRJ2531H@xe \
  DIRECTORY=backup_dir \
  DUMPFILE=lms_backup_$(date +%Y%m%d_%H%M%S).dmp \
  LOGFILE=lms_backup_$(date +%Y%m%d_%H%M%S).log

echo "Backup completed: $BACKUP_FILE"
```

### Audit Log Archival

Archive old audit logs monthly:

```sql
-- Archive audit logs older than 6 months
CREATE TABLE TBL_AUDIT_LOG_ARCHIVE AS
SELECT * FROM TBL_AUDIT_LOG
WHERE OPERATION_DATE < TRUNC(SYSDATE - 180);

DELETE FROM TBL_AUDIT_LOG
WHERE OPERATION_DATE < TRUNC(SYSDATE - 180);

COMMIT;
```

---

## 8. Security Configuration

### Password Policy

Configure in `src/com/library/service/PasswordHasher.java`:

```java
// Password Requirements
private static final int MIN_PASSWORD_LENGTH = 8;
private static final boolean REQUIRE_UPPERCASE = true;
private static final boolean REQUIRE_NUMBERS = true;
private static final boolean REQUIRE_SPECIAL_CHARS = false;

// Hash Configuration
private static final String HASH_ALGORITHM = "SHA-256";
private static final int ITERATIONS = 10000;
```

### Session Configuration

Edit `src/com/library/service/CurrentUserContext.java`:

```java
// Session Timeout (in minutes)
private static final int SESSION_TIMEOUT = 30;

// Concurrent Login Limit
private static final int MAX_CONCURRENT_SESSIONS = 1;
```

### SSL/TLS Configuration (Optional)

For remote Oracle connections:

```bash
# Update DBConnection.java
jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCPS)(HOST=server)(PORT=2484))(CONNECT_DATA=(SERVICE_NAME=xe)))
```

---

## 9. Deployment Configuration

### Installation Package

The system is packaged as `LMS-Setup.jar` with embedded dependencies.

**To rebuild the installer:**

```bash
./package-setup.sh          # Linux
package-setup.bat           # Windows
```

### Deployment Checklist

Before deploying to production:

- [ ] Database schema initialized with `script.sql`
- [ ] `.env` configured with SMTP settings
- [ ] Database backups scheduled
- [ ] Audit log archival configured
- [ ] Memory settings optimized for your hardware
- [ ] Security policy enforced
- [ ] Test all critical workflows
- [ ] Documentation reviewed with stakeholders

---

## 10. Troubleshooting Configuration Issues

### Database Connection Issues

**Problem:** "Connection Refused"
- Check Oracle is running: `lsnrctl status`
- Verify host, port, SID in connection string
- Check firewall allows port 1521

**Problem:** "Invalid Username/Password"
- Verify user exists: `SELECT * FROM dba_users WHERE username='PRJ2531H';`
- Reset password if needed
- Check for extra spaces in credentials

### Email Configuration Issues

**Problem:** OTP email not received
- Check `.env` configuration
- Verify SMTP credentials are correct
- Check spam folder
- Enable "Less secure apps" if using Gmail

### Performance Issues

**Problem:** Application runs slowly
- Increase Java heap size (`-Xmx`)
- Add database indexes as shown above
- Check Oracle is not out of memory
- Profile queries for optimization

---

## Configuration Summary

| Component | Location | Default | Notes |
|-----------|----------|---------|-------|
| Database | DBConnection.java | localhost:1521 | Change for remote DB |
| Email | .env file | Not configured | Required for OTP |
| Heap Size | run.sh/run.bat | 2GB | Adjust for available RAM |
| Theme Colors | ModuleTheme.java | Professional Blue | Customize as needed |
| Audit Retention | AuditLogger.java | 365 days | Configure for compliance |
| Session Timeout | CurrentUserContext.java | 30 minutes | Adjust for security |
| Max Connections | DBConnection.java | 20 | Tune for load |

---

**Last Updated:** April 5, 2026  
**Version:** 2.0  
**Status:** Production Ready
