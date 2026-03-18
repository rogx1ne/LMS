# LMS Module Workflows (Detailed)

This document outlines the step-by-step operational flow and data transitions for each module in the Library Management System.

---

## 1. Authentication & Session
**Goal:** Secure access and role-based UI personalization.

1. **User Input:** User enters Credentials (ID/Password).
2. **Validation:** `UserDAO` queries `TBL_CREDENTIALS`. Passwords are compared using `PasswordHasher` (SHA-256).
3. **Session Setup:** On success, `CurrentUserContext` stores the `User` object (ID, Name, Role).
4. **Dashboard Routing:** 
   - `DashboardFrame` initializes.
   - If `Role != 'ADMIN'`, the **Admin Module** button is hidden/disabled.
   - Audit Log records: "User [ID] logged in."

---

## 2. Book Management
**Goal:** Maintain a searchable catalog and track individual physical copies.

### A. Adding Books (Manual)
1. **Catalog Check:** System checks if the Author/Title already exists in `TBL_BOOK_CATALOG`.
2. **Copy Insertion:** `BookDAO` generates a unique **Accession Number** and inserts the physical copy into `TBL_BOOK_INFORMATION`.
3. **Stock Update:** The `TBL_BOOK_STOCK` view automatically reflects the new quantity.

### B. Accession Register & Stock
1. **Grid Load:** Accession Register loads all rows from `TBL_BOOK_INFORMATION`.
2. **Filtering:** Users can filter by Author, Title, or Bill No.
3. **Reporting:** Users can export filtered views to PDF via `PdfReportService` (supports field selection and orientation).

---

## 3. Procurement & Transaction (The "Procure-to-Shelf" Flow)
**Goal:** Track the acquisition of books from sellers to the library shelves.

### Phase 1: Seller & Order
1. **Seller Setup:** Register book suppliers in `Seller Master`.
2. **Order Placement:** Create an **Order Header** (Date, Seller) and **Order Details** (Titles, Quantities).
3. **Documentation:** Generate an Order PDF and optionally email it to the seller using `ProcurementEmailService`.

### Phase 2: Bill Receipt & Accessioning
1. **Bill Entry:** When books arrive, enter the official **Bill/Invoice** into `Bill Entry`. This stores unit prices and tax.
2. **Bill Report:** Use `Bill Report` to verify all received invoices and audit total spending.
3. **Bill Accession (Integration):** 
   - Search for a Bill ID in the `Bill Accession` panel.
   - System loads the items and quantities received.
   - **Validation:** Ensures you don't accession more books than were actually on the bill.
   - **Execution:** System automatically generates physical copy records (Accession Numbers) for all items on the bill in bulk.

---

## 4. Student Management
**Goal:** Manage student eligibility and identify card generation.

1. **Registration:** Collect Name, Roll, Course, and Session.
2. **ID Generation:** `StudentIdGenerator` creates a unique Card ID.
3. **Validation:** `StudentLogic` ensures the Roll Number matches the session format (e.g., Session 2024 starts with "24").
4. **Outputs:** 
   - **Library Card:** Generated as a PNG or PDF for the student.
   - **Receipt:** A PDF document confirming registration and fees paid.

---

## 5. Circulation (Issue & Return)
**Goal:** Control the movement of books and enforce library policy.

### A. Issuing a Book
1. **Student Check:** System verifies the Student is "ACTIVE" and has not exceeded their **Book Limit**.
2. **Book Check:** System verifies the physical copy status is "AVAILABLE".
3. **Transaction:** Create a record in `TBL_ISSUE` with an `ISSUE_DATE` and `DUE_DATE` (Default: 14 days).
4. **State Change:** Update Book status to "ISSUED".

### B. Returning a Book
1. **Lookup:** Search by Card ID or Accession Number to find the active issue.
2. **Fine Calculation:** System calculates `Current Date - Due Date`. If > 0, a fine is applied per day.
3. **Closing:** Update `TBL_ISSUE` with `RETURN_DATE` and `FINE_PAID`.
4. **Restoration:** Update Book status back to "AVAILABLE".

---

## 6. Admin & Maintenance
**Goal:** System oversight and data portability.

1. **User Management:** Admin creates/deactivates Librarian accounts.
2. **Audit Trails:** View `TBL_AUDIT_LOG` to see which user performed which action (e.g., "Deleted Book", "Reset Password").
3. **Data Migration:** Use `ExcelService` to bulk import Students or Books from `.xlsx` files to save time during initial setup.
4. **Report Standardization:** All modules utilize the `PdfReportService` for consistent branding (College Banner, Timestamped Headers, Signature Footers).
