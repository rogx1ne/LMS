# Library Management System (LMS) - Java Swing + Oracle

A desktop Library Management System built with:
- **Frontend:** Java Swing
- **Backend:** Oracle Database (JDBC)
- **Architecture:** Layered (`ui`, `dao`, `service`, `model`)

## Modules

- **Login**
  - Role-aware login (`ADMIN`, `LIBRARIAN`)
  - Forgot password with OTP email flow (User ID -> OTP -> reset)
- **Book**
  - Add book copy, accession register, stock view, low-stock alerts
- **Transaction / Procurement**
  - Seller management, order header/details, PDF receipt
- **Student**
  - Registration, validation, card/receipt generation, active student view
- **Circulation**
  - Issue/return with due date and weekly fine logic
- **Admin (ADMIN only)**
  - User management
  - Excel import/export (Apache POI)
  - Audit log viewer

## Project Structure

```text
src/
  Main.java
  com/library/
    ui/        # Swing screens + controllers
    dao/       # DB access and transactions
    service/   # business logic, validation, utilities
    model/     # data models
    database/  # DB connection
```

## Prerequisites

- Java 11+ (JDK)
- Oracle XE / Oracle DB running and reachable
- SQL*Plus (or any Oracle SQL client)

## 1) Database Setup

The full schema is in:
- `script.sql`

Run it once (as a privileged user, because it drops/creates user):

```sql
LMS/script.sql
```

`script.sql` creates:
- User `PRJ2531H` / password `PRJ2531H`
- All LMS tables (credentials, books, students, orders, issue, audit logs, etc.)
- Seed users:
  - `ADMIN / ADMIN` (ROLE = `ADMIN`)
  - `LIB01 / LIB01` (ROLE = `LIBRARIAN`)

## 2) Configure DB Connection

Update Oracle connection if needed in:
- `src/com/library/database/DBConnection.java`

Current defaults:
- URL: `jdbc:oracle:thin:@localhost:1521:xe`
- USER: `PRJ2531H`
- PASSWORD: `PRJ2531H`

## 3) Configure Email OTP (.env)

Forgot-password OTP uses SMTP env vars.

1. Copy `.env.example` to `.env` (already created in this project)
2. Fill values in `.env`:

```env
LMS_SMTP_HOST=smtp.gmail.com
LMS_SMTP_PORT=587
LMS_SMTP_USER=your-email@gmail.com
LMS_SMTP_PASS=your-app-password
LMS_SMTP_FROM=your-email@gmail.com
```

> `run.sh`(for linux) auto-loads `.env` before starting the app or `run.bat`(for windows).

## 4) Run the Application

```bash
./run.sh
```
or

```bash
./run.bat
```

This script:
- Compiles all Java files to `bin/`
- Runs the app with `lib/*` classpath

## Notes

- **Admin menu is visible only for `ROLE = ADMIN`.**
- Librarian users do not get admin controls.
- If DB is down/unreachable, app will show connection errors from Oracle.

## Common Troubleshooting

- **ORA-17800 / connection read error**
  - Oracle listener/DB not reachable
  - Verify DB host/port/SID and `DBConnection.java`
- **Invalid credentials**
  - Ensure `script.sql` has been executed successfully
- **OTP email not sent**
  - Check `.env` SMTP values and provider app-password settings
