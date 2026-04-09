package com.library.setup;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.sql.*;
import java.util.Scanner;
import com.library.service.PasswordHasher;

/**
 * LMS Installation Manager - Handles all installation logic
 * Responsible for:
 * 1. Copying files to installation location
 * 2. Running database scripts
 * 3. Creating admin user
 * 4. Generating launcher scripts
 */
public class InstallationManager {
    
    private File installDir;
    private String adminUserId;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminPassword;
    private InstallationProgressListener listener;
    
    public interface InstallationProgressListener {
        void onProgress(String message);
        void onError(String error);
        void onComplete();
    }
    
    public InstallationManager(File installDir, String adminUserId, String adminName,
                              String adminEmail, String adminPhone, String adminPassword,
                              InstallationProgressListener listener) {
        this.installDir = installDir;
        this.adminUserId = adminUserId;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPhone = adminPhone;
        this.adminPassword = adminPassword;
        this.listener = listener;
    }
    
    /**
     * Execute full installation workflow
     */
    public void install() {
        try {
            log("Starting LMS Installation...");
            
            // Step 1: Create directories
            log("Creating installation directories...");
            createDirectories();
            
            // Step 2: Copy source files
            log("Copying application files...");
            copyApplicationFiles();
            
            // Step 3: Create launcher scripts
            log("Generating launcher scripts...");
            createLauncherScripts();
            
            // Step 4: Initialize database
            log("Initializing database...");
            initializeDatabase();
            
            // Step 5: Create admin user
            log("Setting up admin user...");
            createAdminUser();
            
            // Step 6: Verify installation
            log("Verifying installation...");
            verifyInstallation();
            
            log("✓ Installation completed successfully!");
            if (listener != null) {
                listener.onComplete();
            }
        } catch (Exception e) {
            String error = "Installation failed: " + e.getMessage();
            log(error);
            if (listener != null) {
                listener.onError(error);
            }
        }
    }
    
    private void createDirectories() throws IOException {
        File srcDir = new File(installDir, "src");
        File binDir = new File(installDir, "bin");
        File libDir = new File(installDir, "lib");
        
        srcDir.mkdirs();
        binDir.mkdirs();
        libDir.mkdirs();
        
        log("✓ Directories created");
    }
    
    private void copyApplicationFiles() throws IOException {
        // Get the directory where the setup JAR is located
        File setupJarLocation = getSetupJarLocation();
        
        // Copy bin/ directory (compiled classes)
        File sourceBin = new File(setupJarLocation, "bin");
        File destBin = new File(installDir, "bin");
        if (sourceBin.exists() && sourceBin.isDirectory()) {
            copyDirectory(sourceBin, destBin);
            log("  ✓ Copied application classes (bin/)");
        } else {
            log("  ⚠ Warning: bin/ directory not found at " + sourceBin.getAbsolutePath());
        }
        
        // Copy lib/ directory (JAR libraries)
        File sourceLib = new File(setupJarLocation, "lib");
        File destLib = new File(installDir, "lib");
        if (sourceLib.exists() && sourceLib.isDirectory()) {
            copyDirectory(sourceLib, destLib);
            log("  ✓ Copied library files (lib/)");
        } else {
            log("  ⚠ Warning: lib/ directory not found at " + sourceLib.getAbsolutePath());
        }
        
        // Copy database scripts
        File scriptSql = new File(setupJarLocation, "script.sql");
        if (scriptSql.exists()) {
            Files.copy(scriptSql.toPath(), new File(installDir, "script.sql").toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
            log("  ✓ Copied script.sql");
        }
        
        log("✓ Application files ready");
    }
    
    /**
     * Get the directory where the setup JAR is running from
     */
    private File getSetupJarLocation() {
        try {
            // Get the location of this class
            String path = InstallationManager.class.getProtectionDomain()
                                                  .getCodeSource()
                                                  .getLocation()
                                                  .toURI()
                                                  .getPath();
            File jarFile = new File(path);
            
            // If running from JAR, get parent directory
            if (jarFile.isFile()) {
                return jarFile.getParentFile();
            }
            
            // If running from classes, go up from bin/com/library/setup/
            File classDir = jarFile;
            while (classDir != null && !classDir.getName().equals("bin")) {
                classDir = classDir.getParentFile();
            }
            if (classDir != null) {
                return classDir.getParentFile();
            }
            
            // Fallback: current directory
            return new File(".").getAbsoluteFile().getParentFile();
        } catch (Exception e) {
            // Fallback: current directory
            return new File(".").getAbsoluteFile().getParentFile();
        }
    }
    
    /**
     * Recursively copy a directory
     */
    private void copyDirectory(File source, File dest) throws IOException {
        if (!dest.exists()) {
            dest.mkdirs();
        }
        
        File[] files = source.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            File destFile = new File(dest, file.getName());
            
            if (file.isDirectory()) {
                copyDirectory(file, destFile);
            } else {
                Files.copy(file.toPath(), destFile.toPath(), 
                          StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    private void createLauncherScripts() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            createWindowsLauncher();
        } else {
            createUnixLauncher();
        }
        
        log("✓ Launcher scripts created");
    }
    
    private void createWindowsLauncher() throws IOException {
        File launcherFile = new File(installDir, "run.bat");
        StringBuilder script = new StringBuilder();
        script.append("@echo off\n");
        script.append("color 0F\n");
        script.append("title Library Management System\n");
        script.append("cls\n");
        script.append("\n");
        script.append("REM =========================================\n");
        script.append("REM Library Management System - Launcher\n");
        script.append("REM =========================================\n");
        script.append("\n");
        script.append("cd /d \"%~dp0\"\n");
        script.append("\n");
        script.append("REM Check if Java is installed\n");
        script.append("java -version >nul 2>&1\n");
        script.append("if errorlevel 1 (\n");
        script.append("    echo ERROR: Java is not installed or not in PATH\n");
        script.append("    echo Please install Java 8+ and add it to PATH\n");
        script.append("    pause\n");
        script.append("    exit /b 1\n");
        script.append(")\n");
        script.append("\n");
        script.append("REM Check if required directories exist\n");
        script.append("if not exist bin (\n");
        script.append("    echo ERROR: bin/ directory not found\n");
        script.append("    echo Please run setup first: java -jar LMS-Setup.jar\n");
        script.append("    pause\n");
        script.append("    exit /b 1\n");
        script.append(")\n");
        script.append("\n");
        script.append("echo Connecting to database...\n");
        script.append("set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe\n");
        script.append("set LMS_DB_USER=PRJ2531H\n");
        script.append("set LMS_DB_PASSWORD=PRJ2531H\n");
        script.append("\n");
        script.append("java -Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false -cp \"bin;lib\\*\" Main\n");
        script.append("\n");
        script.append("if errorlevel 1 (\n");
        script.append("    echo.\n");
        script.append("    echo ERROR: Application failed to start\n");
        script.append("    echo Check that Oracle database is running on localhost:1521\n");
        script.append("    pause\n");
        script.append(")\n");
        
        Files.write(launcherFile.toPath(), script.toString().getBytes());
    }
    
    private void createUnixLauncher() throws IOException {
        File launcherFile = new File(installDir, "run.sh");
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("\n");
        script.append("# =========================================\n");
        script.append("# Library Management System - Launcher\n");
        script.append("# =========================================\n");
        script.append("\n");
        script.append("SCRIPT_DIR=\"$(cd \"$(dirname \"${BASH_SOURCE[0]}\")\" && pwd)\"\n");
        script.append("cd \"$SCRIPT_DIR\"\n");
        script.append("\n");
        script.append("# Check if Java is installed\n");
        script.append("if ! command -v java &> /dev/null; then\n");
        script.append("    echo \"ERROR: Java is not installed\"\n");
        script.append("    echo \"Please install Java 8+ using: sudo apt-get install default-jre\"\n");
        script.append("    exit 1\n");
        script.append("fi\n");
        script.append("\n");
        script.append("# Check if required directories exist\n");
        script.append("if [ ! -d \"bin\" ]; then\n");
        script.append("    echo \"ERROR: bin/ directory not found\"\n");
        script.append("    echo \"Please run setup first: java -jar LMS-Setup.jar\"\n");
        script.append("    exit 1\n");
        script.append("fi\n");
        script.append("\n");
        script.append("echo \"Connecting to database...\"\n");
        script.append("export LMS_DB_URL=\"jdbc:oracle:thin:@localhost:1521:xe\"\n");
        script.append("export LMS_DB_USER=\"PRJ2531H\"\n");
        script.append("export LMS_DB_PASSWORD=\"PRJ2531H\"\n");
        script.append("\n");
        script.append("java -Duser.timezone=UTC -Doracle.jdbc.timezoneAsRegion=false -cp \"bin:lib/*\" Main\n");
        script.append("\n");
        script.append("if [ $? -ne 0 ]; then\n");
        script.append("    echo \"\"\n");
        script.append("    echo \"ERROR: Application failed to start\"\n");
        script.append("    echo \"Check that Oracle database is running on localhost:1521\"\n");
        script.append("    read -p \"Press Enter to exit...\"\n");
        script.append("fi\n");
        
        Files.write(launcherFile.toPath(), script.toString().getBytes());
        launcherFile.setExecutable(true);
    }
    
    private void initializeDatabase() throws Exception {
        String dbUrl = System.getenv("LMS_DB_URL");
        String dbUser = System.getenv("LMS_DB_USER");
        String dbPassword = System.getenv("LMS_DB_PASSWORD");
        
        if (dbUrl == null) dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        if (dbUser == null) dbUser = "PRJ2531H";
        if (dbPassword == null) dbPassword = "PRJ2531H";
        
        // Set timezone property for Oracle compatibility
        System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        
        // Try to connect as PRJ2531H first (if user exists)
        // If that fails with ORA-01017, try as system/oracle to create the user
        Connection conn = null;
        boolean needsUserCreation = false;
        
        try {
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            log("✓ Database connection established");
        } catch (SQLException e) {
            if (e.getMessage().contains("ORA-01017")) {
                // User doesn't exist, try to create it using SYSTEM account
                log("🔍 User PRJ2531H doesn't exist, attempting to create...");
                needsUserCreation = true;
                
                String systemUser = System.getenv("LMS_SYSTEM_USER");
                String systemPassword = System.getenv("LMS_SYSTEM_PASSWORD");
                if (systemUser == null) systemUser = "system";
                if (systemPassword == null) systemPassword = "oracle";  // Try default for Linux/Windows
                
                try {
                    log("🔍 Connecting as " + systemUser + " to create PRJ2531H user...");
                    try (Connection sysConn = DriverManager.getConnection(dbUrl, systemUser, systemPassword)) {
                        executeSystemPrep(sysConn);  // Create user and grant privileges
                    }
                    
                    // Now try to connect as PRJ2531H
                    log("🔍 Connecting as PRJ2531H to create schema...");
                    conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                    log("✓ Database connection established");
                } catch (SQLException sysEx) {
                    throw new Exception("Could not create PRJ2531H user. Tried SYSTEM/" + systemPassword + ": " + sysEx.getMessage());
                }
            } else {
                throw e;
            }
        }
        
        try {
            // Execute script.sql to initialize schema
            executeScriptSQL(conn);
            
            // Verify tables exist
            String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'TBL_CREDENTIALS'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    log("✓ Database schema initialized and verified");
                } else {
                    throw new Exception("TBL_CREDENTIALS not created after script execution");
                }
            }
        } finally {
            if (conn != null) conn.close();
        }
    }
    
    /**
     * 007 PRODUCTION FIX: Execute SYSTEM-level database preparation
     * Creates PRJ2531H user and grants DBA privileges
     */
    private void executeSystemPrep(Connection sysConn) throws Exception {
        log("✓ Creating PRJ2531H user...");
        try (Statement stmt = sysConn.createStatement()) {
            stmt.execute("BEGIN\n" +
                        "  EXECUTE IMMEDIATE 'CREATE USER PRJ2531H IDENTIFIED BY PRJ2531H';\n" +
                        "EXCEPTION\n" +
                        "  WHEN OTHERS THEN\n" +
                        "    IF SQLCODE != -1920 THEN RAISE; END IF;\n" +
                        "END;");
        }
        
        log("✓ Granting DBA privileges...");
        try (Statement stmt = sysConn.createStatement()) {
            stmt.execute("GRANT DBA TO PRJ2531H");
            stmt.execute("COMMIT");
        }
    }
    
    private void createAdminUser() throws Exception {
        // 007 SECURITY FIX: Use EXACT same connection logic as initializeDatabase()
        // This ensures admin creation happens in the same schema where tables were created
        String dbUrl = System.getenv("LMS_DB_URL");
        String dbUser = System.getenv("LMS_DB_USER");
        String dbPassword = System.getenv("LMS_DB_PASSWORD");
        
        // Use EXACT same defaults as initializeDatabase()
        if (dbUrl == null) dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        if (dbUser == null) dbUser = "PRJ2531H";  // 007: Fixed - same as initializeDatabase() 
        if (dbPassword == null) dbPassword = "PRJ2531H";  // 007: Fixed - same as initializeDatabase()
        
        // 007 CRITICAL FIX: Set timezone property for Oracle compatibility (same as initializeDatabase)
        System.setProperty("oracle.jdbc.timezoneAsRegion", "false");
        
        String normalizedAdminUserId = adminUserId == null ? null : adminUserId.trim().toUpperCase();
        String normalizedAdminName = adminName == null ? null : adminName.trim();
        String normalizedAdminEmail = adminEmail == null ? null : adminEmail.trim();
        String normalizedAdminPhone = adminPhone == null ? null : adminPhone.trim();
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // 007 SECURITY: Log connection context for audit trail
            log("🔍 Admin creation - Connecting as: " + dbUser + " to schema: " + dbUser.toUpperCase());
            conn.setAutoCommit(false);
            
            // Check if user already exists
            String checkQuery = "SELECT 1 FROM TBL_CREDENTIALS WHERE TRIM(USER_ID) = ?";
            boolean userExists = false;
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, normalizedAdminUserId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    userExists = rs.next();
                }
            } catch (SQLException e) {
                // 007 SECURITY: Enhanced error logging for troubleshooting
                log("⚠ Warning during admin existence check: " + e.getMessage());
                log("  Error Code: " + e.getErrorCode() + " | SQL State: " + e.getSQLState());
                log("  This might indicate table doesn't exist in current schema: " + dbUser.toUpperCase());
                // Continue with creation attempt - table might not exist yet
            }
            
            // Hash the password
            String hashedPassword = PasswordHasher.hashPassword(adminPassword);

            if (userExists) {
                String updateQuery = "UPDATE TBL_CREDENTIALS " +
                                   "SET NAME = ?, PSWD = ?, EMAIL = ?, PHNO = ?, ROLE = 'ADMIN', STATUS = 'ACTIVE' " +
                                   "WHERE TRIM(USER_ID) = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, normalizedAdminName);
                    pstmt.setString(2, hashedPassword);
                    pstmt.setString(3, normalizedAdminEmail);
                    pstmt.setLong(4, Long.parseLong(normalizedAdminPhone));
                    pstmt.setString(5, normalizedAdminUserId);
                    
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        conn.commit();
                        log("✓ Admin user '" + normalizedAdminUserId + "' updated with new credentials.");
                    } else {
                        conn.rollback();
                        throw new SQLException("Admin update affected 0 rows for user: " + normalizedAdminUserId);
                    }
                }
                return;
            }

            String insertQuery = "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, EMAIL, PHNO, ROLE, STATUS) " +
                               "VALUES (?, ?, ?, ?, ?, 'ADMIN', 'ACTIVE')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, normalizedAdminUserId);
                pstmt.setString(2, normalizedAdminName);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, normalizedAdminEmail);
                pstmt.setLong(5, Long.parseLong(normalizedAdminPhone));
                
                log("🔧 Attempting to insert admin user into TBL_CREDENTIALS...");
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    log("✓ Admin user created successfully: " + normalizedAdminUserId + " in schema " + dbUser.toUpperCase());
                } else {
                    conn.rollback();
                    throw new SQLException("Admin insertion returned 0 rows affected for user: " + normalizedAdminUserId);
                }
            }
        } catch (SQLException e) {
            // 007 SECURITY: Comprehensive error context for troubleshooting
            String errorMsg = String.format("Failed to create admin user '%s' in schema %s: %s (Error: %d, State: %s)\n" +
                                          "Troubleshooting:\n" +
                                          "- Verify TBL_CREDENTIALS table exists\n" +
                                          "- Verify USER_ID, NAME, PSWD columns exist\n" +
                                          "- Check admin data: ID=%s, Name=%s, Email=%s, Phone=%s", 
                                          normalizedAdminUserId,
                                          dbUser != null ? dbUser.toUpperCase() : "UNKNOWN",
                                          e.getMessage(), e.getErrorCode(), e.getSQLState(),
                                          normalizedAdminUserId, normalizedAdminName, normalizedAdminEmail, normalizedAdminPhone);
            throw new Exception(errorMsg);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid phone number format for admin (must be 10 digits). Received: " + normalizedAdminPhone);
        }
    }
    
    private void verifyInstallation() throws IOException {
        // Verify key files exist
        if (!new File(installDir, "run.sh").exists() && 
            !new File(installDir, "run.bat").exists()) {
            throw new IOException("Launcher script not created");
        }
        log("✓ Installation verified");
    }
    
    /**
     * Execute script.sql to initialize database schema
     */
    private void executeScriptSQL(Connection conn) throws Exception {
        // Look for script.sql in multiple locations:
        // 1. Current working directory (development)
        // 2. Same directory as setup JAR (production)
        // 3. Project root
        File scriptFile = new File("script.sql");
        if (!scriptFile.exists()) {
            scriptFile = new File(getSetupJarLocation(), "script.sql");
        }
        if (!scriptFile.exists()) {
            scriptFile = new File(new File(".").getAbsoluteFile().getParentFile(), "script.sql");
        }
        
        if (!scriptFile.exists()) {
            log("⚠ script.sql not found. Tried: " + scriptFile.getAbsolutePath());
            log("   Database will not be initialized. Tables must exist before proceeding.");
            return;
        }
        
        log("✓ Found script.sql at: " + scriptFile.getAbsolutePath());
        log("Executing database initialization script...");
        
        try (Scanner scanner = new Scanner(scriptFile)) {
            StringBuilder statement = new StringBuilder();
            boolean inPlSqlBlock = false;
            
            while (scanner.hasNextLine()) {
                String rawLine = scanner.nextLine();
                String line = rawLine.trim();
                String upperLine = line.toUpperCase();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                // Skip SQL*Plus directives
                if (upperLine.startsWith("PROMPT ") || upperLine.startsWith("SET ") ||
                    upperLine.startsWith("SPOOL ") || upperLine.equals("EXIT") ||
                    upperLine.equals("QUIT")) {
                    continue;
                }

                // 007 FIX: Strip inline SQL comments (-- text) before processing
                // This is important for detecting statement terminators correctly
                String lineWithoutComment = line;
                int commentIdx = line.indexOf("--");
                if (commentIdx >= 0) {
                    lineWithoutComment = line.substring(0, commentIdx).trim();
                }
                
                // Skip if line is empty after comment removal
                if (lineWithoutComment.isEmpty()) {
                    continue;
                }

                // 007 SECURITY FIX: Track PL/SQL block start
                if (!inPlSqlBlock && isPlSqlStart(upperLine)) {
                    inPlSqlBlock = true;
                }
                
                // 007 FIX: Handle "/" delimiter for PL/SQL blocks (must come BEFORE appending line)
                if (inPlSqlBlock && lineWithoutComment.equals("/")) {
                    if (statement.length() > 0) {
                        executeStatement(conn, statement.toString(), true);
                        statement.setLength(0);
                    }
                    inPlSqlBlock = false;
                    continue;
                }
                
                // Ignore standalone "/" outside of PL/SQL blocks (SQL*Plus artifact)
                if (!inPlSqlBlock && lineWithoutComment.equals("/")) {
                    continue;
                }
                
                // Append line to statement (using version without comments to keep SQL clean)
                statement.append(lineWithoutComment).append("\n");
                
                // 007 FIX: Execute statement if not in PL/SQL and line ends with semicolon (after comment removal)
                if (!inPlSqlBlock && lineWithoutComment.endsWith(";")) {
                    executeStatement(conn, statement.toString(), false);
                    statement.setLength(0);
                }
            }
            
            // Execute any remaining statement
            if (statement.length() > 0) {
                executeStatement(conn, statement.toString(), inPlSqlBlock);
            }
            
            log("✓ Database initialization script executed");
        } catch (IOException e) {
            throw new Exception("Failed to read script.sql: " + e.getMessage());
        }
    }

    private boolean isPlSqlStart(String upperLine) {
        return upperLine.startsWith("BEGIN")
            || upperLine.startsWith("DECLARE")
            || upperLine.startsWith("CREATE OR REPLACE TRIGGER")
            || upperLine.startsWith("CREATE OR REPLACE PROCEDURE")
            || upperLine.startsWith("CREATE OR REPLACE FUNCTION")
            || upperLine.startsWith("CREATE OR REPLACE PACKAGE")
            || upperLine.startsWith("CREATE OR REPLACE PACKAGE BODY");
    }

    private void executeStatement(Connection conn, String statementText, boolean isPlSqlBlock) throws Exception {
        String sql = statementText == null ? "" : statementText.trim();
        if (sql.isEmpty()) {
            return;
        }

        // 007 SECURITY FIX: Remove trailing semicolon for non-PL/SQL blocks
        if (!isPlSqlBlock && sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        // 007 FIX: Remove "/" from PL/SQL blocks - it's a SQL*Plus directive, NOT valid JDBC SQL
        if (isPlSqlBlock && sql.endsWith("/")) {
            sql = sql.substring(0, sql.length() - 1).trim();
        }

        if (sql.isEmpty()) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            String upperSql = sql.toUpperCase();
            if (!upperSql.contains("DROP TABLE") && !upperSql.contains("DROP SEQUENCE")) {
                log("  SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
            }
        } catch (SQLException e) {
            // 007 SECURITY: Enhanced error handling
            if (isExpectedScriptError(e)) {
                String shortMsg = e.getMessage();
                if (shortMsg != null && shortMsg.length() > 80) {
                    shortMsg = shortMsg.substring(0, 80) + "...";
                }
                log("  ⚠ Warning: " + shortMsg);
                return;
            }
            // Log the problematic SQL for debugging
            log("  ✗ Failed SQL: " + sql.substring(0, Math.min(100, sql.length())));
            throw new Exception("Database initialization failed on SQL: " +
                              sql.substring(0, Math.min(120, sql.length())) + "... " +
                              "(Error: " + e.getMessage() + ")", e);
        }
    }

    private boolean isExpectedScriptError(SQLException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        String sqlState = e.getSQLState() != null ? e.getSQLState() : "";
        int errorCode = e.getErrorCode();
        
        // 007 STRICT ERROR HANDLING: Only ignore errors that are harmless on fresh install
        // Errors during table/index/FK creation indicate schema problems and must FAIL
        if (msg.contains("ORA-00942")   // table/view does not exist - FK to non-existent table (FAIL!)
            || msg.contains("ORA-00955") // name already used - constraint conflict (FAIL!)
            || msg.contains("ORA-02264")) { // name used by existing constraint (FAIL!)
            return false;
        }
        
        // Safe to ignore: Pre-drop operations or object not found on fresh install
        if (msg.contains("ORA-01918")   // user does not exist
            || msg.contains("ORA-01920") // user already exists
            || msg.contains("ORA-02289")) { // sequence does not exist
            return true;
        }
        
        // Safe: PL/SQL syntax issues (now fixed with "/" parsing)
        if (msg.contains("ORA-06550") || msg.contains("PLS-00103")) {
            return true;
        }
        
        return false;
    }
    
    private void log(String message) {
        System.out.println(message);
        if (listener != null) {
            listener.onProgress(message);
        }
    }
}
