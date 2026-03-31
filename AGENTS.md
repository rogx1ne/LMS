# LMS Agent Guide (Technical Reference)

This guide is for developers and AI agents working on the Library Management System. It defines the technical boundaries, architectural standards, and operational flows.

## 1. Technical Scope
- **Framework:** Java Swing (Desktop application).
- **Database:** Oracle Database (optimized for XE 10g).
- **Communication:** JDBC (Direct connection).
- **Reporting:** iText (PDF generation) and Apache POI (Excel integration).

## 2. Architectural Layers
- **UI (`src/com/library/ui`)**: Uses `JPanel` and `JDialog`. Communication with logic happens via Controllers.
- **DAO (`src/com/library/dao`)**: Pure JDBC. Handles SQL queries, transactions, and batch processing.
- **Service (`src/com/library/service`)**: 
  - **Logic:** `BookLogic`, `StudentLogic` handle validation.
  - **Reporting:** `PdfReportService` (Grid reports), `StudentPdfService` (IDs/Receipts).
  - **Utilities:** `PasswordHasher`, `AuditLogger`.
- **Model (`src/com/library/model`)**: Plain Old Java Objects (POJOs) for data mapping.

## 3. Product Shape & Module Structure
- **Login**: Role-based (ADMIN/LIBRARIAN) with SMTP-based OTP reset.
- **Book Module**: Manual accession, catalog management, and stock monitoring.
- **Transaction Module**: Fully integrated lifecycle:
  - Seller Management -> Order Placement -> Official Bill Entry -> Bill Reporting -> Bill Accession (Auto-generating book copies).
- **Student Module**: Registration, Fee management, and ID Card generation.
- **Circulation**: Issue/Return logic with automated fine calculation.
- **Admin**: User management, Audit Log viewing, and Excel Data Migration.

## 4. Operational Mandates
- **Oracle 10g Compatibility:** Avoid `STANDARD_HASH` or modern SQL analytical functions. Use `SYSTIMESTAMP` for logs.
- **ID Generation:** Always use `TBL_ID_COUNTER` via `IdCounterService`. Never use `MAX(ID) + 1`.
- **Reporting:** All new grid-based reports **MUST** use `PdfReportService` to ensure consistent branding and user-controlled field selection.
- **Security:** Never log or store plain-text passwords. Use SHA-256 via `PasswordHasher`.

## 5. Verification Protocols
- **Compilation:** `./run.sh` must be clean.
- **Database:** Schema changes must be reflected in `script.sql` and `dummy.sql`.
- **Audit:** All sensitive writes (Deletes, Updates, User creation) must be logged via `AuditLogger`.

## 6. Change Logging
- **Project Log Required:** After any code change, schema update, UI update, or structural refactor, update `PROJECT_LOG.md` in the same turn.
- **Log Format:** Each entry must include the date, affected module/files, a short summary of what changed, and a small representative snippet or SQL/code fragment showing the exact nature of the change.
- **Purpose:** The log is the canonical quick-reference for understanding what was changed without reconstructing it from diffs alone.
