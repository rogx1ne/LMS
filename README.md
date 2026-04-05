# Library Management System (LMS) - Java Swing + Oracle

A robust, enterprise-grade desktop Library Management System designed for efficiency, accuracy, and comprehensive accountability. Built with a modular, layered architecture to handle high-volume library operations with complete audit trail coverage.

**Status:** ✅ Production Ready | **Version:** 2.0 | **Last Updated:** April 5, 2026

## 🚀 Key Features

### 🔐 Security & Access Control
- **Role-Based Access (RBAC):** Dedicated views for `ADMIN` and `LIBRARIAN` roles.
- **Secure Authentication:** Password hashing using SHA-256 via `PasswordHasher`.
- **Password Recovery:** SMTP-integrated OTP reset flow (requires `.env` configuration).
- **Comprehensive Audit Trails:** 100% coverage of all critical operations across 6 modules with transaction-safe logging and user attribution.

### 📚 Book & Catalog Management
- **Manual Accession:** Direct entry of physical copies into the library register.
- **Real-Time Stock Monitoring:** Automated stock summaries and low-stock alerts.
- **Accession Register:** Searchable grid of all physical assets with field-level filtering.
- **Operation Logging:** All book additions and updates (single and bulk) logged to audit trail with details.

### 💳 Student & ID Services
- **Registration:** Validated entry with automated ID and receipt generation.
- **Library Cards:** High-quality PDF and PNG card generation for students.
- **Borrow History:** Instant lookup of a student's past and current transactions.
- **Operation Logging:** Student registration and updates fully tracked in audit trail.

### 📑 Procurement & Transaction Lifecycle
The system now supports a complete **Procure-to-Shelf** workflow with full audit coverage:
1. **Seller Master:** Centralized database of book suppliers with operation tracking (add/update).
2. **Order Placement:** Formal procurement orders with PDF/Email receipt support and audit logging.
3. **Bill Entry:** Record official invoices with unit prices and tax, all tracked to audit trail.
4. **Bill Report:** Searchable audit trail of all financial transactions and spending.
5. **Bill Accession:** Bulk-import books from received bills directly into the catalog with operation logging.

All procurement operations (sellers, orders, bills) are fully tracked for financial accountability.

### 🔄 Circulation
- **Smart Issue/Return:** Validates student limits and book availability.
- **Fine Management:** Automated calculation of overdue fines based on library policy.
- **Transaction Logging:** All circulation operations (issue/return) logged to audit trail with transaction safety.

### 📊 Standardized Reporting
All modules utilize the **`PdfReportService`** for professional document output:
- **Customizable:** Users select which fields to include and choose between Portrait/Landscape.
- **Branded:** Professional college banners and timestamped headers.
- **Authenticated:** Secure footers with "Prepared By" and "Authorized By" signature lines.

### 📋 Comprehensive Audit Logging (New!)
Complete accountability and compliance with 100% operation coverage:
- **All 6 Modules Tracked:** Circulation, Student, Book, Procurement (Orders, Sellers, Bills), Admin
- **25+ Operations Logged:** Every critical operation (create, update) is recorded with full details
- **User Attribution:** Every action attributed to the user who performed it via CurrentUserContext
- **Transaction Safety:** Operations and audit logs committed/rolled back atomically - no orphaned entries
- **Audit Trail Viewer:** Admin module includes complete audit log search and filtering interface
- **Non-Repudiation:** Complete proof that users cannot deny their actions
- **Compliance:** Meets audit trail requirements for regulatory compliance

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

### Core Documentation
- **`architecture.md`**: System design, layered architecture, and runtime flow
- **`database-and-operations.md`**: Table structures, schema design, and maintenance
- **`module-workflows.md`**: Detailed step-by-step operational guides for each module
- **`verification-checklist.md`**: Pre-release testing protocol and validation procedures

### Audit Logging Documentation (New!)
- **`PROJECT_LOG.md`**: Complete changelog with all implementation details
- **`AUDIT_LOGGING_COMPLETION_SUMMARY.md`**: Comprehensive overview of audit logging implementation with coverage statistics and benefits
- **`AUDIT_LOGGING_CHANGES_REFERENCE.md`**: Quick reference guide with before/after code examples and implementation patterns
- **`AUDIT_LOGGING_STATUS.txt`**: High-level status report with verification checklist
- **`IMPLEMENTATION_DELIVERABLES.md`**: Complete deliverables list, files modified, and deployment instructions

### Installation & Setup
- **`INSTALLER_GUIDE.md`**: Step-by-step installation wizard documentation
- **`INSTALLER_STATUS.md`**: Current installer build and deployment status

---

## 📦 Current Build & Version

**Latest Build:** April 5, 2026  
**Installer:** `LMS-Setup.jar` (112 MB) - Ready for Production ✅  
**Java Compilation:** All 13 modified files compile successfully ✅  
**Audit Logging Coverage:** 100% (25+ operations across 6 modules) ✅

---

## 🔄 Module Feature Matrix

| Module | Register | Create | Update | Delete | Issue/Return | Audit Log |
|--------|----------|--------|--------|--------|--------------|-----------|
| **Circulation** | - | ✅ Issue | - | - | ✅ | ✅ |
| **Student** | ✅ | ✅ | ✅ | - | - | ✅ |
| **Book** | - | ✅ | ✅ | - | - | ✅ |
| **Procurement** | ✅ Seller | ✅ Order/Bill | ✅ | - | - | ✅ |
| **Admin** | ✅ User | ✅ | ✅ | ✅ | - | ✅ |

---

## 🎯 Recent Updates (v2.0 Release)

### Audit Logging Enhancement (April 2026)
- ✅ Implemented comprehensive audit logging across all 6 modules
- ✅ 100% coverage of critical operations (25+ operations)
- ✅ Transaction-safe logging with user attribution
- ✅ Complete audit trail available in Admin module
- ✅ 13 Java files modified for production-ready implementation

### Setup Wizard Improvements
- ✅ Enhanced text visibility with dark font colors for better readability
- ✅ Improved UI labels and information displays

### Procurement Module Features
- ✅ Complete Procure-to-Shelf workflow
- ✅ Seller management with audit tracking
- ✅ Order placement with bill support
- ✅ Financial document tracking

---

## ⚠️ Important Notes

- **Database:** Oracle 10g or higher required
- **Java:** 8+ recommended for best compatibility
- **PDF/Excel:** iText and Apache POI libraries included and configured
- **Audit Trail:** All sensitive operations are logged and can be viewed in Admin → Audit Log
- **Backward Compatibility:** All changes maintain backward compatibility with existing data

---

## 🤝 Support & Maintenance

For detailed information on specific features or technical implementation:
1. Check the `docs/` folder for module-specific documentation
2. Review `PROJECT_LOG.md` for a complete change history
3. Consult `AUDIT_LOGGING_COMPLETION_SUMMARY.md` for audit logging details
4. See `IMPLEMENTATION_DELIVERABLES.md` for deployment information

---

**Last Updated:** April 5, 2026  
**Status:** ✅ Production Ready  
**Version:** 2.0
