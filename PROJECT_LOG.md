# Project Log

Use this file to record every code, schema, UI, or structural change made to the project.

## Entry Format
- Date:
- Area:
- Files:
- Summary:
- Snippet:

## Entries

### 2026-04-06 (Latest)
- Area: Setup Wizard UI & Installation - Button contrast and file copy fixes
- Files: `src/com/library/setup/SetupWizard.java`, `src/com/library/setup/DependencyInstaller.java`
- Summary: Fixed critical UI contrast issues preventing button text visibility on Windows & Linux. Added setOpaque(true), setBorderPainted(true), and BasicButtonUI to all buttons in SetupWizard and DependencyInstaller. Enhanced prepareInstallationDirectory() to properly copy source files, scripts, and libraries to installation location. Fixed install log text color (now COLOR_TEXT_DARK on light background). Improved cross-platform button rendering with solid borders. Compilation verified successful.
- Snippet:
```java
// Before (buttons blended with text invisible):
private void styleButton(JButton btn, Color bg) {
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
}

// After (proper contrast on all platforms):
private void styleButton(JButton btn, Color bg) {
    btn.setBackground(bg);
    btn.setForeground(Color.WHITE);
    btn.setOpaque(true);
    btn.setBorderPainted(true);
    btn.setBorder(BorderFactory.createLineBorder(bg, 2));
    btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
}

// Install log now visible with proper colors:
installLog.setForeground(COLOR_TEXT_DARK);  // (40,40,40)
installLog.setBackground(new Color(245, 245, 245));

// Directory preparation now copies source files:
copyDirectory(srcSourceDir, srcDestDir);  // Copies src/
copyDirectory(libSourceDir, libDestDir);  // Copies lib/
copyFile(sqlFile, destFile);              // Copies script.sql, dummy.sql
```

### 2026-04-06
- Area: Build system - JDK 8 compatibility for setup JAR
- Files: `run.sh`, `package-setup.sh`, `MANIFEST-SETUP.MF`, `LMS-Setup.jar`
- Summary: Analyzed project and confirmed Java 8+ compatibility (no modern language features like var, records, or sealed classes). Updated build scripts to compile with `--release 8` flag, ensuring LMS-Setup.jar runs on systems with Java 8+ without requiring JDK 26. Updated manifest with Build-JDK and Target-Compatibility metadata. Final JAR bytecode version verified as 0x34 (Java 8 standard).
- Snippet:
```bash
# Updated build commands in run.sh and package-setup.sh:
javac --release 8 -d bin -cp "lib/*" src/com/library/**/*.java

# Manifest updated with compatibility info:
Build-JDK: 8
Target-Compatibility: Java 8+

# Verified bytecode: hexdump shows ca fe ba be 00 00 00 34 (Java 8)
```

### 2026-04-05 (Latest)
- Area: Bug fix - User creation & forgot password
- Files: `src/com/library/dao/AdminDAO.java`, `src/com/library/dao/UserDAO.java`, `BUG_FIX_REPORT.md` (new)
- Summary: Fixed critical bugs preventing user creation and forgot password functionality. Root cause: Oracle CHAR(5) padding on USER_ID column caused regex mismatch and string comparison failures. Applied TRIM() to all USER_ID queries in login/authentication flows. Added 8 modified methods across 2 DAO files with consistent TRIM handling for database-to-Java comparisons.
- Snippet:
```java
// Before (FAILS for padded CHAR values like 'U001 '):
WHERE REGEXP_LIKE(USER_ID, '^U[0-9]{3}$')
WHERE USER_ID = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'

// After (WORKS with TRIM):
WHERE REGEXP_LIKE(TRIM(USER_ID), '^U[0-9]{3}$')
WHERE TRIM(USER_ID) = ? AND NVL(STATUS,'ACTIVE') = 'ACTIVE'

// Result: User creation now generates U001, U002, U003... correctly
// Forgot password now finds users by ID successfully
```

### 2026-04-05
- Files: `LMS-Setup.jar`, `package-setup.sh`
- Summary: Rebuilt installer JAR to include all audit logging changes across Circulation, Student, Book, and Procurement modules. All 18 modified Java files recompiled and packaged. JAR is ready for production deployment.
- Snippet:
```bash
./package-setup.sh
# ✓ All dependencies extracted
# ✓ JAR created successfully (112M)
# JAR includes all audit logging implementations
```

### 2026-04-01
- Area: Audit logging - Procurement module
- Files: `src/com/library/dao/SellerDAO.java`, `src/com/library/dao/OrderDAO.java`, `src/com/library/ui/ProcurementController.java`
- Summary: Completed comprehensive audit logging for Procurement module. Added audit trail to seller operations (addSeller, updateSellerAudited) and order operations (createOrder, updateOrderReplaceDetails). All operations now log who performed the action, what was changed, and when. Applied transaction-safe audit logging pattern consistent with other modules.
- Snippet:
```java
// SellerDAO - new updateSellerAudited method with audit logging
AuditLogger.logAction(conn, performedBy, "Procurement", "Updated seller " + seller.getSellerId());

// OrderDAO - updated method with performedBy parameter
AuditLogger.logAction(conn, performedBy, "Procurement", "Updated order " + orderId + " with " + details.size() + " items");

// ProcurementController - all DAO calls now pass CurrentUserContext.getUserId()
sellerDAO.addSeller(seller, CurrentUserContext.getUserId());
orderDAO.updateOrderReplaceDetails(orderId, sellerId, orderDate, details, CurrentUserContext.getUserId());
```

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
- Area: Setup & Installation System - Environment Detection
- Files: `src/com/library/setup/SystemEnvironment.java`
- Summary: Created cross-platform environment scanner that detects OS type (Windows/Linux/Mac), checks for Java 8 installation, verifies Oracle 10g XE presence, and provides comprehensive warnings and compatibility reports.
- Snippet:
```java
public enum OSType { WINDOWS, LINUX, MAC, UNKNOWN }

private void detectOS() {
    String osLower = osName.toLowerCase();
    if (osLower.contains("win")) osType = OSType.WINDOWS;
    else if (osLower.contains("nix") || osLower.contains("nux")) osType = OSType.LINUX;
}

public boolean isEnvironmentReady() {
    return javaInstalled && oracleInstalled && warnings.isEmpty();
}
```

### 2026-04-04
- Area: Setup & Installation System - GUI Wizard
- Files: `src/com/library/setup/SetupWizard.java`
- Summary: Implemented comprehensive GUI-based setup wizard using Java Swing with CardLayout for multi-page wizard flow. Includes Welcome, Location Selection, Environment Check (with dependency installation instructions), Database Setup, Admin User Creation, Progress, and Completion pages. Cross-platform compatible with OS-specific instructions for dependency installation.
- Snippet:
```java
private CardLayout cardLayout;
private SystemEnvironment environment;
private File installLocation;

// Wizard pages: WELCOME → LOCATION → ENVIRONMENT → DATABASE → ADMIN → PROGRESS → COMPLETE

private JPanel createEnvironmentPage() {
    // Shows system environment summary
    // Detects Java 8 and Oracle 10g XE
    // Provides "Install Java 8" and "Install Oracle 10g XE" buttons with OS-specific instructions
    txtSummary.setText(environment.getSummary());
}
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

### 2026-04-04
- Area: Git packaging cleanup
- Files: `.gitignore`, `PROJECT_LOG.md`
- Summary: Excluded the generated root installer binary `LMS-Setup.jar` from version control so GitHub push stays within the 100 MB limit while preserving the local build artifact outside git history.
- Snippet:
```gitignore
/LMS-Setup.jar
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

### 2026-03-31 (Phase 2 Installer System)
- Area: Cross-platform installer - Admin user creation
- Files: `src/com/library/setup/AdminUserCreator.java`
- Summary: Created AdminUserCreator component with username validation, password strength checking (6+ chars, letter+number recommended), SHA-256 hashing via PasswordHasher, and ID generation using TBL_ID_COUNTER. Inserts admin user into TBL_USER with ADMIN role.
- Snippet:
```java
public String createAdminUser(String username, String password, String fullName, String email) throws Exception {
    String hashedPassword = PasswordHasher.hashPassword(password);
    String userId = getNextUserId(); // U-0001, U-0002, etc.
    // INSERT INTO TBL_USER (USER_ID, USERNAME, PASSWORD_HASH, ROLE, ...)
}

public static PasswordValidation validatePassword(String password) {
    if (password.length() < 6) {
        result.isValid = false;
        result.message = "Password must be at least 6 characters";
    }
}
```

### 2026-03-31 (Phase 2 Installer System)
- Area: Cross-platform installer - Launcher generation
- Files: `src/com/library/setup/LauncherGenerator.java`
- Summary: Created LauncherGenerator that compiles all Java sources once during installation and generates OS-specific launcher scripts. Windows: LMS-Launcher.bat with error handling and pause on failure. Linux: LMS-Launcher.sh with chmod +x and POSIX permissions. Both scripts check for Java before launching.
- Snippet:
```java
public void compileAllSources() throws Exception {
    String javacCmd = "javac -d " + binDir + " -cp \"" + classpath + "\" " + javaFiles;
    Process process = Runtime.getRuntime().exec(javacCmd);
}

private File generateLinuxLauncher() throws IOException {
    script.append("#!/bin/bash\n");
    script.append("java -cp \"bin:lib/*\" com.library.ui.DashboardFrame\n");
    makeExecutable(launcherFile); // chmod +x
}
```

### 2026-03-31 (Phase 2 Installer System)
- Area: Cross-platform installer - Desktop shortcuts
- Files: `src/com/library/setup/ShortcutCreator.java`
- Summary: Created ShortcutCreator for desktop/menu integration. Linux: Generates .desktop files in ~/.local/share/applications and ~/Desktop with icon support. Windows: Uses VBScript to create .lnk shortcuts on desktop and Start Menu with custom icons.
- Snippet:
```java
private void createLinuxDesktopFile() {
    content.append("[Desktop Entry]\n");
    content.append("Type=Application\n");
    content.append("Name=Library Management System\n");
    content.append("Exec=" + launcherScript.getAbsolutePath() + "\n");
}

private void createWindowsShortcut() {
    vbsContent.append("Set oLink = oWS.CreateShortcut(sLinkFile)\n");
    vbsContent.append("oLink.TargetPath = \"" + launcherScript.getAbsolutePath() + "\"\n");
    Runtime.getRuntime().exec("cscript //NoLogo " + vbsScript.getAbsolutePath());
}
```

### 2026-03-31 (Phase 2 Installer System)
- Area: Cross-platform installer - Uninstaller
- Files: `src/com/library/setup/Uninstaller.java`
- Summary: Created GUI uninstaller with optional data retention. Removes desktop shortcuts (.desktop/.lnk), Start Menu entries, bin/ directory, and launcher scripts. Optionally drops database schema with manual SQL instructions. Progress bar shows real-time status.
- Snippet:
```java
private JCheckBox chkRemoveData = new JCheckBox("Remove database schema and all data (Cannot be undone!)");

private void performUninstall() {
    removeShortcuts(); // Delete .desktop or .lnk files
    if (chkRemoveData.isSelected()) {
        removeDatabaseSchema(); // Show DROP TABLE instructions
    }
    removeFiles(); // Delete bin/, launchers, keep src/lib
}
```

### 2026-03-31 (Phase 2 Installer System)
- Area: Cross-platform installer - Setup Wizard integration
- Files: `src/com/library/setup/SetupWizard.java`, `src/com/library/setup/DatabaseInitializer.java`, `src/com/library/setup/SystemEnvironment.java`
- Summary: Integrated all installer components into SetupWizard. Added database configuration page with connection testing, admin creation page with password strength validation, and progress page with real-time installation log. Added performInstallation() orchestrating: compile sources → initialize DB → create admin → generate launchers → create shortcuts. Added getConnection() to DatabaseInitializer and detectOSType() static method to SystemEnvironment.
- Snippet:
```java
private void performInstallation() {
    launcher.compileAllSources(); // Step 1: Compile once
    dbInit.performFreshInstall(scriptSql, dummySql, callback); // Step 2: DB setup
    adminCreator.createAdminUser(username, password, fullName, email); // Step 3: Admin
    launcher.generateLauncher(); // Step 4: Scripts
    shortcutCreator.createShortcut(); // Step 5: Shortcuts
    cardLayout.show(contentPanel, PAGE_COMPLETE); // Step 6: Done
}

// Database page with test connection
btnTest.addActionListener(e -> {
    DatabaseInitializer dbInit = new DatabaseInitializer(url, username, password);
    if (dbInit.testConnection()) {
        txtLog.append("✓ Connection successful!\n");
    }
});

// Admin page with password validation
txtPassword.getDocument().addDocumentListener(...);
AdminUserCreator.PasswordValidation validation = AdminUserCreator.validatePassword(pwd);
lblPasswordStrength.setText(validation.message);
```

### 2026-04-04 (Phase 2 Installer System)
- Area: Cross-platform installer - Dependency auto-installer
- Files: `src/com/library/setup/DependencyInstaller.java`
- Summary: Created DependencyInstaller with automated Java 8 installation using system package managers (pacman for Arch, apt-get for Debian/Ubuntu, yum for RHEL, chocolatey for Windows). Oracle Database shows manual instructions and Docker alternative. Integrated into SetupWizard environment page with "Install" buttons that show progress dialog with auto-install or manual instructions.
- Snippet:
```java
private boolean installJavaLinux(JTextArea log) throws Exception {
    if (commandExists("pacman")) {
        return executeCommand("sudo pacman -S --noconfirm jdk8-openjdk", log);
    }
    if (commandExists("apt-get")) {
        executeCommand("sudo apt-get update", log);
        return executeCommand("sudo apt-get install -y openjdk-8-jdk", log);
    }
}

public static void showInstallInstructions(Component parent, String dependency, OSType osType) {
    // Interactive dialog with Auto Install button and real-time log
    btnInstall.addActionListener(e -> {
        boolean success = installer.installJava(txtLog); // or installOracle
    });
}
```

### 2026-04-04 (Phase 2 Installer System)
- Area: Cross-platform installer - Final packaging
- Files: `MANIFEST-SETUP.MF`, `package-setup.sh`, `package-setup.bat`, `LMS-Setup.jar`
- Summary: Created fat JAR packaging system that compiles all sources, extracts dependencies, bundles source code and SQL scripts, and creates executable LMS-Setup.jar (112MB). Includes manifest with Main-Class: SetupWizard. Scripts for Linux (package-setup.sh) and Windows (package-setup.bat) automate the 4-step build process. Final JAR is self-contained and runnable with `java -jar LMS-Setup.jar`.
- Snippet:
```bash
# package-setup.sh - 4-step packaging process
[1/4] Compiling all sources...
[2/4] Preparing JAR contents (setup classes, src/, lib/, *.sql)...
[3/4] Creating fat JAR (extracting dependencies)...
[4/4] Building LMS-Setup.jar with manifest...

# MANIFEST-SETUP.MF
Main-Class: com.library.setup.SetupWizard

# Result: 112MB executable JAR with:
# - Compiled setup wizard classes
# - All source code for installation-time compilation
# - Bundled libraries (Oracle JDBC, iText, POI, FlatLaf, etc.)
# - SQL initialization scripts (script.sql, dummy.sql)
```

### 2026-04-05 (Admin Module Enhancement)
- Area: Admin Module - Import/Export System Enhancement
- Files: `src/com/library/ui/DataImportExportPanel.java`, `src/com/library/ui/AdminController.java`, `src/com/library/service/ExcelService.java`, `src/com/library/dao/AdminDAO.java`
- Summary: Enhanced the Admin module's import/export functionality to support both selective (single table) and bulk (all tables) operations. Added radio button mode selectors for Export ("Selective Table" vs "All Tables (ZIP)") and Import ("Selective Table" vs "All Tables (ZIP)"). Selective mode exports/imports single table to/from Excel (.xlsx). All Tables mode exports multiple tables to a ZIP file containing separate Excel files per table, and imports from such ZIP files. Added dynamic UI visibility - table combo is only enabled when either export or import is in selective mode. All operations are fully logged in the audit log with detailed statistics.
- Snippet:
```java
// UI - Radio button groups for mode selection
private final ButtonGroup exportModeGroup = new ButtonGroup();
private final JRadioButton rbExportAll = new JRadioButton("All Tables (ZIP)", false);
private final JRadioButton rbExportSelective = new JRadioButton("Selective Table", true);

// Controller - Mode-based export routing
private void exportToExcel() {
    if (dataPanel.getRbExportAll().isSelected()) {
        exportAllTablesToZip();
    } else {
        exportSelectiveTableToExcel();
    }
}

// ExcelService - ZIP export with multiple Excel files
public void exportMultipleTablesToZip(Map<String, List<Map<String, Object>>> tablesData, 
                                       Path outputZipFile, String dateStamp) throws IOException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(outputZipFile))) {
        for (Map.Entry<String, List<Map<String, Object>>> entry : tablesData.entrySet()) {
            byte[] excelBytes = createExcelBytes(entry.getKey(), entry.getValue());
            String excelFileName = entry.getKey() + "_" + dateStamp + ".xlsx";
            ZipEntry zipEntry = new ZipEntry(excelFileName);
            zos.putNextEntry(zipEntry);
            zos.write(excelBytes);
            zos.closeEntry();
        }
    }
}

// AdminDAO - Bulk fetch and import
public Map<String, List<Map<String, Object>>> fetchAllTablesData() throws SQLException {
    Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
    for (String uiTableName : getSupportedTableNames()) {
        result.put(uiTableName, fetchTableData(uiTableName));
    }
    return result;
}
```

### 2026-04-05 (Setup Wizard UI Enhancement)
- Area: Setup Wizard - Text Visibility Improvement
- Files: `src/com/library/setup/SetupWizard.java`, `src/com/library/setup/DependencyInstaller.java`
- Summary: Fixed text visibility issues in the setup wizard where text was blending with the background making it hard to read. Added a new dark text color constant (COLOR_TEXT_DARK = RGB 40,40,40) and applied it to all text labels, form labels, text areas, and informational messages throughout the setup wizard. Created helper methods createFormLabel() and createInfoLabel() to ensure consistent dark text styling. All user-facing text elements now have explicit dark foreground colors for maximum readability against light backgrounds.
- Snippet:
```java
// Color constant for dark readable text
private static final Color COLOR_TEXT_DARK = new Color(40, 40, 40);

// Helper methods for consistent text styling
private JLabel createFormLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    label.setForeground(COLOR_TEXT_DARK);
    return label;
}

// Applied to all form labels
formPanel.add(createFormLabel("Host:"), gbc);
formPanel.add(createFormLabel("Port:"), gbc);
formPanel.add(createFormLabel("Username:"), gbc);

// Applied to text areas
txtLog.setForeground(COLOR_TEXT_DARK);
txtSummary.setForeground(COLOR_TEXT_DARK);

// Applied to info labels
lblInfo.setForeground(COLOR_TEXT_DARK);
```
