# Database Operations & Maintenance Guide

This document is for database administrators and developers to manage the Oracle backend.

## 1. Schema & Data Files
### `script.sql` (Full Schema)
- **Purpose:** Destructive reinitialization. Drops the `PRJ2531H` user, recreation, and table setup.
- **Tables Created:** 
  - `TBL_CREDENTIALS`, `TBL_ID_COUNTER`, `TBL_AUDIT_LOG`
  - `TBL_BOOK_CATALOG`, `TBL_BOOK_INFORMATION`, `TBL_BOOK_STOCK`, `TBL_BOOK_ALERT_LOG`
  - `TBL_STUDENT`, `TBL_SELLER`, `TBL_ORDER_HEADER`, `TBL_ORDER_DETAILS`, `TBL_BILL`, `TBL_ISSUE`

### `dummy.sql` (Demo Data)
- **Purpose:** Loads 200+ rows of demo data across all modules (Students, Books, Sellers, Bills).
- **Primary Bills:** Includes `BILL260001`, `BILL260002`, `BILL260003`, and `DUMMY_BILL_2026` for procurement testing.

## 2. Configuration & Runtime
- **Primary Config:** `src/com/library/database/DBConnection.java`.
- **Environment Overrides:** 
  - `LMS_DB_URL`: JDBC thin URL (Default: `jdbc:oracle:thin:@localhost:1521:xe`)
  - `LMS_DB_USER`: Username (Default: `PRJ2531H`)
  - `LMS_DB_PASSWORD`: Password (Default: `PRJ2531H`)

## 3. Seed Credentials (Default Logins)
- **Admin:** `ADMIN / ADMIN`
- **Librarians:** `LIB01 / LIB01`, `LIB02 / LIB02`
- **User:** `USR01 / USR01PASS`

## 4. Operational Maintenance
### Critical Database Rules
1. **Oracle 10g Compatibility:** 
   - Never use modern analytical functions (e.g., `LISTAGG`, `ROW_NUMBER() OVER`). 
   - Avoid `STANDARD_HASH` as it’s only available in newer Oracle versions.
   - Use `SHA256$...` prefix for hashed passwords stored in `TBL_CREDENTIALS`.
2. **ID Consistency:** 
   - `TBL_ID_COUNTER` is the source of truth for all sequential IDs (Accession No, Student ID, Seller ID). 
   - If the counter is manually modified, the application logic may fail due to duplicate key constraints.
3. **Transaction Integrity:** 
   - Most writes are performed with `AUTOCOMMIT = FALSE` to ensure atomicity.

## 5. DB Refresh Procedure
1. Log in to SQL*Plus as `SYSTEM` or `SYSDBA`.
2. Execute `@script.sql`.
3. (Optional) Execute `@dummy.sql`.
4. Run the application using `./run.sh`.
