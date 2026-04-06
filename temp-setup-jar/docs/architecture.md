# LMS Architecture & Design Document

## 1. Application Runtime Flow
1. **Bootstrap:** `Main.java` sets the Look-and-Feel and launches `LoginFrame`.
2. **Authentication:** `UserDAO` validates the user.
3. **Session Management:** `CurrentUserContext` (Singleton pattern) stores the logged-in user's role and identity.
4. **Shell Initialization:** `DashboardFrame` acts as the primary shell. It uses a `CardLayout` to switch between modules and a sidebar for navigation.
5. **Role-Based Control:** Upon loading, the sidebar visibility is dynamically adjusted based on `CurrentUserContext.isAdministrator()`.

## 2. Layered Architecture
### UI Layer (`com.library.ui`)
- **View Panels:** Standalone `JPanel` subclasses (e.g., `AddBookPanel`).
- **Controllers:** Manage event listeners, bridge UI and Service/DAO, and update views.
- **Theme:** `ModuleTheme` provides centralized styling (Fonts, Colors, Buttons).

### Service Layer (`com.library.service`)
- **Business Logic:** `BookLogic` and `StudentLogic` ensure data integrity before database insertion.
- **Utilities:** `PasswordHasher` (SHA-256), `IdCounterService` (Thread-safe ID generation).
- **Reporting Engine:** 
  - `PdfReportService`: Standardizes grid-based PDF exports (used across Students, Books, and Bills).
  - `ExcelService`: Handles `.xlsx` file parsing using Apache POI.

### DAO Layer (`com.library.dao`)
- **Persistence:** Handles pure JDBC operations. 
- **Transactions:** Complex operations (like `placeOrder` or `addBookCopies`) use manual `commit()` and `rollback()` logic to ensure ACID compliance.

### Model Layer (`com.library.model`)
- **Data Carriers:** Immutable or POJO classes mapping directly to database tables (e.g., `Student`, `BookCopy`).

## 3. Standardization & Quality
- **PDF Reporting:** All reports follow a 3-part design:
  - **Banner:** College header image (`header.png`).
  - **Meta:** Standard header with date/time of generation.
  - **Auth:** Footer with "Prepared By" and "Authorized By" signature lines.
- **Error Handling:** Centralized through `ValidationException` and displayed via `JOptionPane` on the UI.
- **Audit Logging:** Every critical write operation (Add, Update, Delete) is recorded in `TBL_AUDIT_LOG`.

## 4. Known Risks
- **Oracle XE 10g Limitations:** Memory and CPU limits; code must prioritize efficient queries and closing connections/statements immediately in `finally` blocks.
- **ID Generation:** Must never be performed by code (`MAX(ID) + 1`) to avoid race conditions; always use the `TBL_ID_COUNTER` table.
- **Circulation Integrity:** Special care is taken when transitioning books between `TBL_ISSUE` and `TBL_BOOK_INFORMATION` to prevent state mismatch.
