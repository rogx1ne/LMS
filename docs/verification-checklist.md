# LMS Verification Checklist

Use this checklist to verify system health after major updates or before a release.

## 1. Source-Level Verification
- [ ] Run `./run.sh` or `./run.bat`.
- [ ] Confirm all Java sources compile without errors.
- [ ] Check for deprecation warnings using `javac -Xlint:deprecation`.
- [ ] Ensure all necessary `.jar` files in `lib/` are present on the classpath.

## 2. Database Verification
- [ ] Run `script.sql` (Check for ORA- errors).
- [ ] Run `dummy.sql` (Check for ORA- errors).
- [ ] Connect to Oracle and verify table counts (Should be > 0 for all TBLs).
- [ ] Confirm `TBL_ID_COUNTER` is seeded with initial values (e.g., SID0000000).

## 3. Functional Smoke Checks
### Login & Dashboard
- [ ] Login as `ADMIN` (Displays Admin module).
- [ ] Login as `LIB01` (Admin module is hidden).
- [ ] Test "Forgot Password" OTP flow (Requires `.env` SMTP config).

### Procurement (Transaction) Module
- [ ] Add a new Seller in `Seller Master`.
- [ ] Place a new order with multiple book items in `Add Order`.
- [ ] Generate and download an Order PDF.
- [ ] Enter a new official Bill/Invoice in `Bill Entry`.
- [ ] Run a `Bill Report` and verify data accuracy.
- [ ] Use `Bill Accession` to import books from a Bill into the catalog.

### Student Module
- [ ] Register a new student (Verify Roll No logic).
- [ ] Generate a student Library Card (PDF and PNG).
- [ ] Generate a Registration Receipt (PDF).

### Book Module
- [ ] Add a physical copy manually in `Add Book`.
- [ ] Search for the new book in the `Accession Register`.
- [ ] Verify `Stock` counts reflect current inventory.

### Circulation Module
- [ ] Issue a book to an active student.
- [ ] Verify book status is now "ISSUED".
- [ ] Return the book and check for fine calculation (if overdue).
- [ ] Verify book status is now "AVAILABLE".

### Admin Tools
- [ ] View `Audit Logs` (Filter by User, Module, and Date).
- [ ] Test `Excel Data Migration` (Import/Export Students).

## 4. Reporting Quality
- [ ] Verify PDF reports have the **College Banner**.
- [ ] Verify PDF reports have **Timestamped Headers** (Generated On: Date | Time).
- [ ] Verify PDF reports have **Signature Footers** (Prepared By / Authorized By).
- [ ] Test **Field Selection** (Deselect columns and verify they are omitted from PDF).
