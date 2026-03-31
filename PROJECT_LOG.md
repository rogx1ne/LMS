# Project Log

Use this file to record every code, schema, UI, or structural change made to the project.

## Entry Format
- Date:
- Area:
- Files:
- Summary:
- Snippet:

## Entries

### 2026-03-31
- Area: Retrospective sync
- Files: `PROJECT_LOG.md`
- Summary: Added backfilled entries below based on the current workspace state and the changes made earlier in this chat, so the project log reflects work that was already completed before the logging rule existed.
- Snippet:
```md
Backfilled from chat history + current repository state.
```

### 2026-03-31
- Area: Books module
- Files: `src/com/library/ui/AddBookPanel.java`, `src/com/library/ui/BookEditDialog.java`, `src/com/library/ui/BookController.java`, `src/com/library/service/BookLogic.java`
- Summary: Merged publisher and publication place into one UI field, `Publication (Publisher, Place)`, while preserving separate database storage through parsing and formatting logic.
- Snippet:
```java
BookLogic.PublicationParts publication = BookLogic.splitPublication(addPanel.getTxtPublication().getText());
```

### 2026-03-31
- Area: Books module
- Files: `src/com/library/ui/AccessionRegisterPanel.java`, `src/com/library/ui/BookController.java`, `src/com/library/ui/StockPanel.java`
- Summary: Merged publisher and place into a single `Publication` display in books tables and filters, including the accession register and stock-related views.
- Snippet:
```java
String[] cols = {"Book Title", "Author", "Edition", "Publication", "Quantity"};
```

### 2026-03-31
- Area: Shared table UX
- Files: `src/com/library/ui/ModuleTheme.java`, `src/com/library/ui/AccessionRegisterPanel.java`, `src/com/library/ui/StockPanel.java`, `src/com/library/ui/StudentView.java`, `src/com/library/ui/SellerPanel.java`, `src/com/library/ui/OrderViewPanel.java`, `src/com/library/ui/BillReportPanel.java`, `src/com/library/ui/ReturnPanel.java`, `src/com/library/ui/AuditLogPanel.java`, `src/com/library/ui/CirculationReportPanel.java`
- Summary: Added dynamic empty-state messaging across modules so searches and filters with zero matches show `Record Not Found`, and fixed the shared rendering so the message actually appears for empty filtered tables.
- Snippet:
```java
table.setFillsViewportHeight(true);
() -> hasActiveFilters() ? "Record Not Found" : "No audit logs available."
```

### 2026-03-31
- Area: Circulation module
- Files: `src/com/library/ui/CirculationModulePanel.java`, `src/com/library/ui/CirculationReportPanel.java`, `src/com/library/ui/CirculationController.java`, `src/com/library/dao/CirculationDAO.java`, `src/com/library/model/CirculationReportRow.java`
- Summary: Added a new `CIRCULATION REPORT` section that shows issue and return history with borrower, book, due date, return date, fine, and status details.
- Snippet:
```java
JButton navReport = ModuleTheme.createNavButton("CIRCULATION REPORT");
```

### 2026-03-31
- Area: Circulation issue flow
- Files: `src/com/library/ui/IssuePanel.java`, `src/com/library/ui/AvailableBookSearchDialog.java`, `src/com/library/ui/CirculationController.java`, `src/com/library/dao/CirculationDAO.java`, `src/com/library/model/AvailableBookRow.java`
- Summary: Added available-book lookup in the issue form so users can search available titles and auto-fill the accession number instead of switching back and forth manually.
- Snippet:
```java
private final JButton btnSearchBook = new JButton("Search Available");
```

### 2026-03-31
- Area: Circulation module
- Files: `src/com/library/ui/IssuePanel.java`, `src/com/library/ui/ReturnPanel.java`, `src/com/library/ui/CirculationReportPanel.java`, `src/com/library/ui/CirculationController.java`, `src/com/library/dao/CirculationDAO.java`, `src/com/library/model/IssueTransaction.java`, `src/com/library/model/CirculationReportRow.java`, `src/com/library/service/CirculationService.java`, `script.sql`, `dummy.sql`
- Summary: Extended circulation to support faculty borrowing in addition to student borrowing. Added borrower type, faculty name, and faculty contact to issue records, updated issue/return/report screens, and revised schema and seed data accordingly.
- Snippet:
```sql
BORROWER_TYPE VARCHAR2(10) DEFAULT 'STUDENT' NOT NULL,
FACULTY_NAME VARCHAR2(80),
FACULTY_CONTACT VARCHAR2(15)
```

### 2026-03-31
- Area: Seed data
- Files: `dummy.sql`
- Summary: Fixed the dummy bill seed that exceeded the `TBL_BILL.B_ID` width by replacing the oversized value with a valid 10-character bill ID.
- Snippet:
```sql
VALUES ('BILL260004', 'SID0000001', 'Learning Java', 'John Doe', 5, 500.00, DATE '2026-03-18', 10, 2500.00, 2750.00);
```

### 2026-03-31
- Area: Circulation return flow
- Files: `src/com/library/ui/ReturnPanel.java`, `src/com/library/ui/CirculationController.java`, `src/com/library/dao/CirculationDAO.java`, `src/com/library/service/CirculationService.java`, `src/com/library/model/IssueReturnResult.java`, `src/com/library/model/IssueTransaction.java`, `src/com/library/model/CirculationReportRow.java`, `src/com/library/ui/CirculationReportPanel.java`, `script.sql`, `dummy.sql`
- Summary: Added physical inspection during book return with `GOOD`, `DAMAGED`, and `LOST` outcomes. Good returns restore the book normally, damaged returns add an extra fine equal to the student's late fine while faculty get no damage fine, and lost returns charge the book price. The return condition is now stored on `TBL_ISSUE` and shown in circulation reporting.
- Snippet:
```sql
RETURN_CONDITION VARCHAR2(10), -- GOOD / DAMAGED / LOST
CONSTRAINT CHK_ISSUE_RETURN_CONDITION CHECK (RETURN_CONDITION IS NULL OR RETURN_CONDITION IN ('GOOD','DAMAGED','LOST'))
```

### 2026-03-31
- Area: Shared confirmation previews
- Files: `src/com/library/ui/ModuleTheme.java`, `src/com/library/ui/BookController.java`, `src/com/library/ui/StudentController.java`, `src/com/library/ui/CirculationController.java`, `src/com/library/ui/ProcurementController.java`, `src/com/library/ui/AdminController.java`
- Summary: Added a reusable confirmation-preview dialog and enforced it before major write actions such as issue/return book, add/update book, register/update student, add/update seller, add/remove publication items in procurement, place/update order, and create/deactivate user.
- Snippet:
```java
if (!ModuleTheme.confirmPreview(parent, "Confirm Book Issue", "Issue Book", ...)) {
    return;
}
```

### 2026-03-31
- Area: Project instructions
- Files: `AGENTS.md`, `PROJECT_LOG.md`
- Summary: Added a mandatory rule requiring every future code or structure change to also update the project log with a short summary and representative snippet.
- Snippet:
```md
## 6. Change Logging
- **Project Log Required:** After any code change, schema update, UI update, or structural refactor, update `PROJECT_LOG.md` in the same turn.
```
