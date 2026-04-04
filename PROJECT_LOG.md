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

### 2026-04-04
- Area: Circulation Reports - Date Range Filtering
- Files: `src/com/library/ui/CirculationReportPanel.java`, `src/com/library/ui/CirculationController.java`
- Summary: Added date range filtering to Circulation Report using From Date and To Date fields with date picker integration. Filters circulation records by Issue Date (column 8) using SimpleDateFormat("dd/MM/yyyy") for date comparison.
- Snippet:
```java
private final JTextField txtFromDate = new JTextField(10);
private final JTextField txtToDate = new JTextField(10);
ModuleTheme.addDatePicker(txtFromDate);
ModuleTheme.addDatePicker(txtToDate);

private boolean isWithinDateRange(String dateStr, String fromDateStr, String toDateStr) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    sdf.setLenient(false);
    java.util.Date date = sdf.parse(dateStr);
    // ... date range validation
}
```

### 2026-04-04
- Area: Book Register - Date Range Filtering
- Files: `src/com/library/ui/AccessionRegisterPanel.java`, `src/com/library/ui/BookController.java`
- Summary: Added Bill Date range filtering to Accession Register (Book Register) using fltFromDate and fltToDate fields. Filters books by Bill Date (column 13) to show how many books were added within a specified date range.
- Snippet:
```java
private final JTextField fltFromDate = new JTextField(10);
private final JTextField fltToDate = new JTextField(10);
ModuleTheme.addDatePicker(fltFromDate);
ModuleTheme.addDatePicker(fltToDate);

// In BookController.java
filters.add(new RowFilter<Object, Object>() {
    @Override
    public boolean include(Entry<? extends Object, ? extends Object> entry) {
        String billDate = String.valueOf(entry.getValue(13));
        return isWithinDateRange(billDate, fromDateStr, toDateStr);
    }
});
```

### 2026-04-04
- Area: Advanced Search Toggle - Circulation Report
- Files: `src/com/library/ui/CirculationReportPanel.java`
- Summary: Implemented advanced search toggle pattern for Circulation Report. Simple search (txtSearch) is visible by default. Advanced filters (Status, From Date, To Date) are hidden in a collapsible panel controlled by chkAdvancedSearch checkbox.
- Snippet:
```java
private final JCheckBox chkAdvancedSearch = new JCheckBox("Advanced Search");
private final JPanel advancedPanel = new JPanel(new GridBagLayout());

chkAdvancedSearch.addActionListener(e -> {
    advancedPanel.setVisible(chkAdvancedSearch.isSelected());
    top.revalidate();
    top.repaint();
});

// Simple panel (always visible): Search field + Advanced Search checkbox
// Advanced panel (collapsible): Status, From Date, To Date
```

### 2026-04-04
- Area: Advanced Search Toggle - Accession Register
- Files: `src/com/library/ui/AccessionRegisterPanel.java`
- Summary: Implemented advanced search toggle for Accession Register (Book Register). Simple search shows only Unified Global Search by default. Advanced panel contains 10 column-specific filters (Accession No, Author, Title, Volume, Publication, Year, Source, Bill No, Bill From Date, Bill To Date) that are hidden until user clicks Advanced Search checkbox.
- Snippet:
```java
private final JCheckBox chkAdvancedSearch = new JCheckBox("Advanced Search");
private final JPanel advancedPanel = new JPanel(new GridBagLayout());

// Simple Panel (always visible):
// - Unified Search (Title/Author/Tags)
// - Advanced Search checkbox
// - Reset button

// Advanced Panel (collapsible):
// - 10 column-specific filters organized in 3 rows
advancedPanel.setVisible(false);

chkAdvancedSearch.addActionListener(e -> {
    advancedPanel.setVisible(chkAdvancedSearch.isSelected());
    filters.revalidate();
    filters.repaint();
});
```

### 2026-04-04
- Area: UI/UX - Search Simplification
- Files: Multiple panel files
- Summary: Applied advanced search toggle pattern to panels with multiple filters. Panels with only 1-2 simple filters (StockPanel, ReturnPanel, BillReportPanel, AvailableBookSearchDialog, OrderViewPanel) were kept as-is since they're already simple and don't benefit from advanced/simple toggle.
- Snippet:
```md
Decision matrix:
- CirculationReportPanel: 4 filters → Advanced toggle added ✓
- AccessionRegisterPanel: 11 filters → Advanced toggle added ✓
- StockPanel: 1 filter → Kept as-is
- ReturnPanel: 2 filters → Kept as-is
- BillReportPanel: 1 filter → Kept as-is
- OrderViewPanel: 2 filters → Kept as-is
```

### 2026-04-04
- Area: Advanced Search Toggle - Student Info
- Files: `src/com/library/ui/StudentView.java`
- Summary: Implemented advanced search toggle for Student Info panel. Simple search shows only Student Name field by default. Advanced panel contains 8 filters (Card ID, Roll, Course, Session, From Date, To Date, Issued By, Book Limit) that are hidden until user clicks Advanced Search checkbox. Date filters (From Date, To Date) were already present and now moved to advanced panel.
- Snippet:
```java
private JCheckBox chkAdvancedSearch;
private JPanel advancedFiltersPanel;

// Simple Panel (always visible):
// - Student Name search field
// - Advanced Search checkbox
// - Reset button

// Advanced Panel (collapsible - 8 filters):
// - Card ID, Roll, Course, Session
// - From Date, To Date, Issued By, Book Limit
advancedFiltersPanel.setVisible(false);

chkAdvancedSearch.addActionListener(e -> {
    advancedFiltersPanel.setVisible(chkAdvancedSearch.isSelected());
    pnlFilters.revalidate();
    pnlFilters.repaint();
});
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
