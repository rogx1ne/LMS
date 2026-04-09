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
        script.append("REM LMS Launcher\n");
        script.append("cd /d \"%~dp0\"\n");
        script.append("set LMS_DB_URL=jdbc:oracle:thin:@localhost:1521:xe\n");
        script.append("set LMS_DB_USER=PRJ2531H\n");
        script.append("set LMS_DB_PASSWORD=PRJ2531H\n");
        script.append("java -cp \"bin;lib\\*\" -Doracle.jdbc.timezoneAsRegion=false Main\n");
        
        Files.write(launcherFile.toPath(), script.toString().getBytes());
    }
    
    private void createUnixLauncher() throws IOException {
        File launcherFile = new File(installDir, "run.sh");
        StringBuilder script = new StringBuilder();
        script.append("#!/bin/bash\n");
        script.append("# LMS Launcher\n");
        script.append("cd \"$(dirname \"$0\")\"\n");
        script.append("export LMS_DB_URL=\"jdbc:oracle:thin:@localhost:1521:xe\"\n");
        script.append("export LMS_DB_USER=\"PRJ2531H\"\n");
        script.append("export LMS_DB_PASSWORD=\"PRJ2531H\"\n");
        script.append("java -cp \"bin:lib/*\" -Doracle.jdbc.timezoneAsRegion=false Main\n");
        
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
        
        // Retry logic for connection (handles transient failures)
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                log("✓ Database connection established");
                
                // Execute script.sql to initialize schema
                executeScriptSQL(conn);
                
                // Verify tables exist (check all_tables since we're connecting as SYSTEM)
                String query = "SELECT COUNT(*) FROM all_tables WHERE table_name = 'TBL_CREDENTIALS'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        log("✓ Database schema initialized and verified");
                    } else {
                        throw new Exception("TBL_CREDENTIALS not created after script execution");
                    }
                }
                conn.close();
                return; // Success
                
            } catch (SQLException e) {
                lastException = e;
                retryCount++;
                
                if (retryCount < maxRetries) {
                    log("⚠ Connection attempt " + retryCount + " failed, retrying...");
                    try {
                        Thread.sleep(1000 * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    log("✗ Database connection failed after " + maxRetries + " attempts");
                }
            }
        }
        
        // All retries failed
        throw new Exception("Failed to connect to Oracle after " + maxRetries + " attempts: " + 
                          (lastException != null ? lastException.getMessage() : "Unknown error") +
                          "\n\nSolutions:\n" +
                          "1. Verify Podman Oracle is running:\n" +
                          "   podman ps | grep oracle\n" +
                          "2. If not running, start it:\n" +
                          "   podman run -d --name oracle10g -p 1521:1521 wnameless/oracle-xe-11g\n" +
                          "3. Check connection string:\n" +
                          "   jdbc:oracle:thin:@localhost:1521:xe\n" +
                          "4. Verify credentials: PRJ2531H / PRJ2531H\n");
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

                // 007 SECURITY FIX: Track PL/SQL block start
                if (!inPlSqlBlock && isPlSqlStart(upperLine)) {
                    inPlSqlBlock = true;
                }
                
                // 007 FIX: Handle "/" delimiter for PL/SQL blocks (must come BEFORE appending line)
                if (inPlSqlBlock && line.equals("/")) {
                    if (statement.length() > 0) {
                        executeStatement(conn, statement.toString(), true);
                        statement.setLength(0);
                    }
                    inPlSqlBlock = false;
                    continue;
                }
                
                // Append current line to statement
                statement.append(rawLine).append("\n");
                
                // 007 FIX: Execute statement if not in PL/SQL and line ends with semicolon
                if (!inPlSqlBlock && line.endsWith(";")) {
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

        // Additional validation for PL/SQL blocks
        if (isPlSqlBlock && !sql.endsWith("/")) {
            sql = sql + "/";
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
        
        // 007 SECURITY: Comprehensive expected error handling
        return msg.contains("already exists")
            || msg.contains("ORA-00942")   // table/view does not exist
            || msg.contains("ORA-02289")   // sequence does not exist
            || msg.contains("ORA-00955")   // name is already used
            || msg.contains("ORA-01918")   // user does not exist
            || msg.contains("ORA-01920")   // user name conflicts with another
            || msg.contains("ORA-00001")   // duplicate value
            || msg.contains("ORA-01031")   // insufficient privileges
            || msg.contains("ORA-00911")   // invalid character (expected for malformed SQL)
            || msg.contains("ORA-00000")   // successful completion
            || errorCode == 942    // table/view does not exist
            || errorCode == 955;   // name already used by object
    }
    
    private void log(String message) {
        System.out.println(message);
        if (listener != null) {
            listener.onProgress(message);
        }
    }
}
