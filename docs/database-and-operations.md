# Database And Operations

## Schema Files
- `script.sql`
  - Creates or reinitializes the LMS schema objects inside `PRJ2531H`
  - Designed for reruns on Oracle XE 10g
- `dummy.sql`
  - Clears LMS rows and inserts demo-safe operational data

## Runtime DB Configuration
- Defaults are in `DBConnection.java`
- Environment overrides:
  - `LMS_DB_URL`
  - `LMS_DB_USER`
  - `LMS_DB_PASSWORD`

## Seed Credentials
- `ADMIN / ADMIN`
- `LIB01 / LIB01`
- `LIB02 / LIB02`
- `USR01 / USR01PASS`

## Operational Tables
- `TBL_CREDENTIALS`
- `TBL_ID_COUNTER`
- `TBL_AUDIT_LOG`
- `TBL_BOOK_CATALOG`
- `TBL_BOOK_INFORMATION`
- `TBL_BOOK_STOCK`
- `TBL_BOOK_ALERT_LOG`
- `TBL_STUDENT`
- `TBL_SELLER`
- `TBL_ORDER_HEADER`
- `TBL_ORDER_DETAILS`
- `TBL_BILL`
- `TBL_ISSUE`

## Important Operational Rules
- Passwords in new/reset flows are hashed in application code.
- SQL seed users use `SHA256$...` values for Oracle 10g compatibility.
- ID generation uses `TBL_ID_COUNTER`; do not revert to `COUNT(*) + 1`.
- Transaction module is seller/order oriented; bill-entry may exist as code but should not be reintroduced into navigation without an intentional design decision.

## DB Refresh Procedure
1. Run `script.sql`.
2. Run `dummy.sql` if demo data is needed.
3. Start the app with `./run.sh`.

## When Updating SQL
- Keep Oracle 10g compatibility unless the project explicitly upgrades DB version.
- Avoid `STANDARD_HASH` and similar unsupported functions in Oracle XE 10g.
- Prefer rerunnable patterns where possible.

