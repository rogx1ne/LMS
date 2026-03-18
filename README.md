# Library Management System (LMS) - Java Swing + Oracle

A robust, enterprise-grade desktop Library Management System designed for efficiency and accuracy. Built with a modular, layered architecture to handle high-volume library operations.

## 🚀 Key Features

### 🔐 Security & Access Control
- **Role-Based Access (RBAC):** Dedicated views for `ADMIN` and `LIBRARIAN` roles.
- **Secure Authentication:** Password hashing using SHA-256 via `PasswordHasher`.
- **Password Recovery:** SMTP-integrated OTP reset flow (requires `.env` configuration).
- **Audit Trails:** Comprehensive logging of all critical operations (Deletes, Updates, User actions).

### 📚 Book & Catalog Management
- **Manual Accession:** Direct entry of physical copies into the library register.
- **Real-Time Stock Monitoring:** Automated stock summaries and low-stock alerts.
- **Accession Register:** Searchable grid of all physical assets with field-level filtering.

### 💳 Student & ID Services
- **Registration:** Validated entry with automated ID and receipt generation.
- **Library Cards:** High-quality PDF and PNG card generation for students.
- **Borrow History:** Instant lookup of a student's past and current transactions.

### 📑 Procurement & Transaction Lifecycle (New!)
The system now supports a complete **Procure-to-Shelf** workflow:
1. **Seller Master:** Centralized database of book suppliers.
2. **Order Placement:** Formal procurement orders with PDF/Email receipt support.
3. **Bill Entry:** Record official invoices, including unit prices and tax.
4. **Bill Report:** Searchable audit trail of all financial transactions and spending.
5. **Bill Accession:** Bulk-import books from received bills directly into the catalog.

### 🔄 Circulation
- **Smart Issue/Return:** Validates student limits and book availability.
- **Fine Management:** Automated calculation of overdue fines based on library policy.

### 📊 Standardized Reporting
All modules utilize the new **`PdfReportService`** for professional document output:
- **Customizable:** Users select which fields to include and choose between Portrait/Landscape.
- **Branded:** Professional college banners and timestamped headers.
- **Authenticated:** Secure footers with "Prepared By" and "Authorized By" signature lines.

---

## 🏗 Architecture

```text
src/com/library/
  ├── ui/         # Swing UI Panels, Dialogs, and Controllers
  ├── dao/        # JDBC Persistence (SQL Queries & Transactions)
  ├── service/    # Business Logic, Validation, and PDF/Excel Engines
  ├── model/      # Data Models (POJOs)
  └── database/   # Database Connection Management
```

---

## 🛠 Prerequisites

- **Java 8+ (JDK)**
- **Oracle XE 10g** (or higher)
- **SQL*Plus** or similar Oracle client
- **Libraries (included):** iText (PDF), Apache POI (Excel), Log4j (Logging).

---

## ⚙️ Setup & Installation

### 1) Database Initialization
Run the schema script as a privileged user to create the `PRJ2531H` workspace:
```sql
@script.sql
```
(Optional) Load demo data for testing the procurement and bill flows:
```sql
@dummy.sql
```

### 2) Connection Configuration
Update `src/com/library/database/DBConnection.java` or use environment variables:
- `LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe`
- `LMS_DB_USER=PRJ2531H`
- `LMS_DB_PASSWORD=PRJ2531H`

### 3) Email OTP (.env)
Copy `.env.example` to `.env` and provide your SMTP details for the "Forgot Password" feature.

### 4) Execution
**Linux:** `./run.sh`  
**Windows:** `./run.bat`

---

## 👨‍💻 Default Credentials
- **Admin:** `ADMIN / ADMIN`
- **Librarian:** `LIB01 / LIB01`
- **User:** `USR01 / USR01PASS`

---

## 📂 Documentation
For deeper technical details, refer to the `docs/` folder:
- `architecture.md`: System design and runtime flow.
- `database-and-operations.md`: Table structures and maintenance.
- `module-workflows.md`: Detailed step-by-step operational guides.
- `verification-checklist.md`: Pre-release testing protocol.
