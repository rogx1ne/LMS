# LMS Architecture & Workflow

**Last Updated:** 2026-04-06  
**Version:** 2.0.2

---

## 1. System Overview

The Library Management System (LMS) is a Java Swing desktop application designed for managing library operations with a focus on books, students, circulation, and transactions.

### Technology Stack
- **Frontend:** Java Swing with FlatLaf Modern UI
- **Backend:** Pure Java (no frameworks)
- **Database:** Oracle Database 10g XE
- **Communication:** JDBC Direct Connection
- **Build:** Maven-compatible structure
- **JDK Target:** Java 8+ (compiled with `--release 8`)

---

## 2. Architectural Layers

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│        (com.library.ui - Java Swing)                │
│  ├─ LoginFrame                                      │
│  ├─ DashboardPanel                                  │
│  ├─ BookManagement                                  │
│  ├─ StudentManagement                               │
│  ├─ CirculationModule                               │
│  └─ AdminPanel                                      │
├─────────────────────────────────────────────────────┤
│              Service/Logic Layer                    │
│   (com.library.service - Business Rules)            │
│  ├─ BookLogic (validation, calculations)            │
│  ├─ StudentLogic (fee calc, eligibility)            │
│  ├─ CirculationLogic (fine calculation)             │
│  ├─ AuditLogger (audit trail)                       │
│  ├─ PasswordHasher (security)                       │
│  └─ ReportingServices (PDF/Excel)                   │
├─────────────────────────────────────────────────────┤
│               DAO Layer (Data Access)               │
│    (com.library.dao - Pure JDBC)                    │
│  ├─ BookDAO                                         │
│  ├─ StudentDAO                                      │
│  ├─ CirculationDAO                                  │
│  ├─ AdminDAO                                        │
│  ├─ UserDAO                                         │
│  ├─ IdCounterService (sequence generation)          │
│  └─ TransactionDAO                                  │
├─────────────────────────────────────────────────────┤
│                Model Layer (POJOs)                  │
│   (com.library.model - Data Transfer Objects)       │
│  ├─ Book, BookCatalog, BookStock                    │
│  ├─ Student, StudentFee                             │
│  ├─ User, Credentials                               │
│  ├─ Issue, Transaction                              │
│  └─ AuditLog                                        │
├─────────────────────────────────────────────────────┤
│            Database Layer (Oracle 10g XE)           │
│  ├─ TBL_CREDENTIALS (users)                         │
│  ├─ TBL_BOOK_* (book management)                    │
│  ├─ TBL_STUDENT (student records)                   │
│  ├─ TBL_ISSUE (circulation)                         │
│  ├─ TBL_ORDER_* (procurement)                       │
│  ├─ TBL_AUDIT_LOG (audit trail)                     │
│  └─ TBL_ID_COUNTER (sequence mgmt)                  │
└─────────────────────────────────────────────────────┘
```

### Layer Responsibilities

**UI Layer:**
- User interaction via Swing panels/dialogs
- Input validation and user feedback
- Navigation between modules
- Communicates with Service layer (no direct DB access)

**Service Layer:**
- Business logic and rules
- Validation (e.g., student eligibility, fine calculation)
- Audit logging for sensitive operations
- Reporting (PDF/Excel generation)
- Security (password hashing)

**DAO Layer:**
- Pure JDBC - no ORM frameworks
- SQL query execution
- Connection management
- Transaction handling
- Direct database calls

**Model Layer:**
- Plain Old Java Objects (POJOs)
- No business logic
- Used for data transfer between layers

---

## 3. Module Structure

### 3.1 Authentication & Authorization
- **Entry Point:** `LoginFrame` (UI)
- **Service:** User credential validation via `UserDAO`
- **Database:** `TBL_CREDENTIALS` table
- **Security:** SHA-256 password hashing via `PasswordHasher`
- **Roles:** ADMIN, LIBRARIAN

### 3.2 Book Management
- **Components:**
  - `BookManagementUI` - Add/edit books, manage stock
  - `BookLogic` - Validation, calculations
  - `BookDAO` - Database operations
  
- **Related Tables:**
  - `TBL_BOOK_CATALOG` - Book metadata (Author, Title, Edition)
  - `TBL_BOOK_INFORMATION` - Individual copies (Accession#)
  - `TBL_BOOK_STOCK` - Stock tracking

- **Key Features:**
  - Manual accession number entry
  - Stock level monitoring
  - Book search by title/author
  - Bill tracking

### 3.3 Student Management
- **Components:**
  - `StudentPanel` - Register/manage students
  - `StudentLogic` - Fee calculations, eligibility
  - `StudentDAO` - Database operations
  
- **Related Tables:**
  - `TBL_STUDENT` - Student records
  
- **Key Features:**
  - Library card generation (PPU-YYxxx format)
  - Fee management
  - ID card printing
  - Eligibility tracking

### 3.4 Circulation Module
- **Components:**
  - `CirculationPanel` - Issue/Return books
  - `CirculationLogic` - Fine calculation
  - `CirculationDAO` - Database operations
  
- **Related Tables:**
  - `TBL_ISSUE` - Issue/return transactions
  
- **Key Features:**
  - Book issue to students/faculty
  - Return processing
  - Automatic fine calculation (₹1/day overdue)
  - Due date tracking (7 days default)
  - Book condition tracking

### 3.5 Transaction Management
- **Workflow:** Seller → Order → Bill → Accession
  - Seller Management: Register book suppliers
  - Order Placement: Create purchase orders
  - Bill Entry: Official bill processing
  - Bill Reporting: Generate bill reports
  - Auto-Accession: Automatically generate book copies from bills
  
- **Related Tables:**
  - `TBL_SELLER` - Supplier information
  - `TBL_ORDER_HEADER`, `TBL_ORDER_DETAILS` - Order records
  - `TBL_BILL` - Bill records

### 3.6 Admin Module
- **Features:**
  - User management (create/edit users)
  - Audit log viewing
  - Excel data import/export
  - System configuration

---

## 4. Database Schema

### Core Tables

**TBL_CREDENTIALS**
```sql
USER_ID     CHAR(5) PRIMARY KEY
NAME        VARCHAR2(50)
PSWD        VARCHAR2(255) -- SHA-256 hashed
EMAIL       VARCHAR2(80)
PHNO        NUMBER(10)
ROLE        VARCHAR2(15) -- ADMIN/LIBRARIAN
STATUS      VARCHAR2(10) -- ACTIVE/INACTIVE
```

**TBL_BOOK_INFORMATION** (Accessions)
```sql
ACCESS_NO   CHAR(6) PRIMARY KEY
AUTHOR_NAME VARCHAR2(50)
BK_TITLE    VARCHAR2(100)
EDITION     NUMBER(2)
PUBLISHER   VARCHAR2(40)
U_PRICE     NUMBER(6,2)
STATUS      VARCHAR2(10) -- ACTIVE/WITHDRAWN/LOST/DAMAGED
CIRC_STATUS VARCHAR2(10) -- AVAILABLE/ISSUED
```

**TBL_STUDENT**
```sql
CARD_ID     CHAR(9) PRIMARY KEY -- PPU-YYxxx
ROLL        NUMBER(5)
NAME        VARCHAR2(50)
COURSE      CHAR(3)
ACAD_SESSION CHAR(9)
FEE         NUMBER(5,2)
BOOK_LIMIT  NUMBER(1) -- Max books can issue
STATUS      VARCHAR2(9) -- ACTIVE/INACTIVE
```

**TBL_ISSUE** (Circulation)
```sql
ISSUE_ID    CHAR(10) PRIMARY KEY -- LIDYYxxxxx
CARD_ID     CHAR(9) -- Student
ACCESSION_NO CHAR(6) -- Book
ISSUE_DATE  DATE
DUE_DATE    DATE
RETURN_DATE DATE
FINE        NUMBER(8,2)
STATUS      VARCHAR2(10) -- ISSUED/RETURNED
```

**TBL_AUDIT_LOG**
```sql
LOG_ID              NUMBER PRIMARY KEY
USER_ID             VARCHAR2(20)
MODULE              VARCHAR2(30)
ACTION_DESCRIPTION  VARCHAR2(400)
LOG_TS              TIMESTAMP
```

See `script.sql` for complete schema definition.

---

## 5. Data Flow

### Issue a Book Flow
```
1. Student/Librarian requests book
2. CirculationPanel → CirculationDAO.issue()
3. DAO checks:
   - Student eligibility (status, book limit)
   - Book availability
   - Student fee status
4. If valid:
   - Insert into TBL_ISSUE (status='ISSUED')
   - Update book CIRC_STATUS='ISSUED'
   - Calculate due_date = today + 7 days
5. Generate receipt
6. Audit log entry
```

### Return a Book Flow
```
1. Student returns book
2. Librarian enters accession# and condition
3. CirculationPanel → CirculationDAO.return()
4. DAO calculates:
   - Days overdue = return_date - due_date
   - Fine = max(0, days_overdue * 1)
5. If book damaged:
   - Fine += damage_fee
   - Mark book status='DAMAGED'
6. Update TBL_ISSUE (status='RETURNED')
7. Generate receipt with fine details
8. Audit log entry
```

### Add Book Flow
```
1. Librarian enters book details
2. BookManagementUI → BookLogic.validate()
3. If valid → BookDAO.addBook()
4. DAO:
   - Insert into TBL_BOOK_CATALOG (if new)
   - Get next accession# from TBL_ID_COUNTER
   - Insert into TBL_BOOK_INFORMATION
5. Stock management module updates TBL_BOOK_STOCK
6. Audit log entry
```

---

## 6. Operational Mandates

### Security
- Never store plain-text passwords → Use SHA-256 hashing
- All sensitive operations must be logged → Use AuditLogger
- Role-based access control → Check ROLE in TBL_CREDENTIALS
- Input validation on all user inputs

### Database
- Oracle 10g XE compatibility required
- No modern SQL features (no analytical functions)
- Use SYSTIMESTAMP for all timestamps
- No hardcoded credentials in code

### ID Generation
- Never use MAX(ID)+1
- Always use TBL_ID_COUNTER via IdCounterService
- Format: User=5 chars, Issue=10 chars (LIDYYxxxxx), Card=9 chars (PPU-YYxxx)

### Reporting
- All new grid-based reports must use PdfReportService
- Ensure user-controlled field selection
- Support both PDF and Excel output

### Audit Trail
- Log all creates, updates, deletes
- Log user actions (issue, return, fine payment)
- Store timestamp and user_id automatically

---

## 7. Workflow Diagrams

### Authentication Workflow
```
User Input → LoginFrame → UserDAO.authenticate()
                             ↓
                        Hash input password
                             ↓
                        Compare with DB
                             ↓
                    [Valid] → Load Dashboard
                    [Invalid] → Show error
```

### Installation Workflow
```
java -jar LMS-Setup.jar
        ↓
LMSSetupWizard (UI):
  1. Welcome
  2. Choose installation path
  3. System check (Java + Oracle)
  4. Create admin credentials
  5. Install progress
  6. Complete
        ↓
InstallationManager:
  1. Copy bin/ (classes)
  2. Copy lib/ (JARs)
  3. Create run.sh/run.bat
  4. Execute script.sql
  5. Create admin user
  6. Verify installation
        ↓
/home/user/LMS/
├── bin/
├── lib/
├── run.sh
└── run.bat
```

---

## 8. Deployment Architecture

### Development
```
Local IDE (IntelliJ/Eclipse)
    ↓
Source: src/
Build: bin/ (compiled classes)
Test: LMS v0 (testing ground)
```

### Production
```
Setup Wizard (JAR 112M)
    ↓
Installation Manager
    ↓
Target: /home/user/LMS/
├── bin/ (app classes)
├── lib/ (dependencies)
├── script.sql (schema)
└── run.sh/run.bat (launchers)
    ↓
Daily Usage: ./run.sh → Login → Dashboard
```

### Distribution
```
LMS-Setup-Distribution.zip
├── LMS-Setup.jar (112M setup wizard)
├── bin/ (pre-compiled classes)
├── lib/ (all JARs - 70MB+)
├── script.sql
└── docs/ (user documentation)
```

---

## 9. Key Design Decisions

1. **Pure Java Swing:** No web frameworks, direct database access
2. **JDBC Only:** No ORM (Hibernate/JPA), explicit SQL control
3. **Single JAR Distribution:** Fat JAR with all dependencies
4. **Oracle 10g Focus:** Older DB compatibility = broader user base
5. **SHA-256 Hashing:** Industry standard for password security
6. **Timestamp Logging:** All actions trackable for audit
7. **Modular Structure:** Clear separation between UI, Service, DAO

---

## 10. Performance Considerations

- Connection pooling not implemented (single-connection per operation)
- Suitable for small-to-medium libraries (<10 concurrent users)
- Book search uses database indexes
- Audit logs grow over time (periodic cleanup recommended)
- Report generation may take time with large datasets (100k+ books)

---

## 11. Future Enhancement Opportunities

- Multi-threaded connection pool for concurrency
- RESTful API layer for mobile integration
- Real-time notifications (book holds, due date reminders)
- Advanced analytics dashboard
- ISBN barcode scanning
- Email notifications
- Mobile app client

