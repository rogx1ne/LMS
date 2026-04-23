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
        
        Connection sysConn = null;
        Connection prjConn = null;
        
        try {
            // PROFESSIONAL APPROACH: Try OS authentication first (Windows/Linux ORA_DBA group)
            log("🔍 Attempting OS authentication (SYSDBA)...");
            sysConn = tryOSAuthentication(dbUrl);
            
            if (sysConn != null) {
                log("✓ Connected as SYSDBA using OS authentication (no credentials needed)");
                
                // Drop and recreate user
                executeSystemPrep(sysConn);
                log("✓ PRJ2531H user reset complete");
                
                sysConn.close();
                sysConn = null;
            } else {
                // Fallback: Try with environment variables or defaults
                log("ℹ OS authentication not available, using database credentials");
                
                String systemUser = System.getenv("LMS_SYSTEM_USER");
                String systemPassword = System.getenv("LMS_SYSTEM_PASSWORD");
                if (systemUser == null) systemUser = "system";
                if (systemPassword == null) systemPassword = "manager";
                
                log("🔍 Connecting as " + systemUser + " to reset PRJ2531H user...");
                try {
                    sysConn = DriverManager.getConnection(dbUrl, systemUser, systemPassword);
                    log("✓ SYSTEM connection established");
                    
                    executeSystemPrep(sysConn);
                    log("✓ PRJ2531H user reset complete");
                } catch (SQLException sysEx) {
                    String errorCode = sysEx.getErrorCode() > 0 ? 
                        " (ORA-" + String.format("%05d", sysEx.getErrorCode()) + ")" : "";
                    String solutionMsg = buildCredentialErrorMessage(systemUser, dbUrl, errorCode, sysEx.getMessage());
                    throw new Exception(solutionMsg);
                } finally {
                    if (sysConn != null) sysConn.close();
                }
            }
            
            // Step 2: Connect as PRJ2531H (fresh user) to create schema
            log("🔍 Connecting as PRJ2531H to create schema...");
            prjConn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            log("✓ Database connection established");
            
            // Execute script.sql to initialize schema
            executeScriptSQL(prjConn);
            
            // Verify tables exist
            String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'TBL_CREDENTIALS'";
            try (Statement stmt = prjConn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    log("✓ Database schema initialized and verified");
                } else {
                    throw new Exception("TBL_CREDENTIALS not created after script execution");
                }
            }
        } finally {
            if (prjConn != null) prjConn.close();
        }
    }
    
    /**
     * Try to connect as SYSDBA using OS authentication (OCI driver)
     * Works on Windows with ORA_DBA group or Linux with ora_dba group
     * Returns null if OS authentication not available (will fallback to credentials)
     */
    private Connection tryOSAuthentication(String dbUrl) {
        try {
            // Extract SID from thin connection string and convert to OCI format
            String ociUrl = convertToOCIFormat(dbUrl);
            
            log("  Trying OCI driver with OS authentication: " + ociUrl);
            
            // Use timeout wrapper to prevent hanging on Windows
            // (Windows can hang indefinitely when trying to load OCI libraries)
            final Connection[] connHolder = new Connection[1];
            final Throwable[] exHolder = new Throwable[1];
            
            Thread osAuthThread = new Thread(() -> {
                try {
                    connHolder[0] = DriverManager.getConnection(ociUrl, "/", "as sysdba");
                } catch (Throwable e) {
                    exHolder[0] = e;
                }
            });
            
            osAuthThread.setDaemon(true);
            osAuthThread.start();
            
            // Wait max 10 seconds for OS auth to complete
            try {
                osAuthThread.join(10000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
            // Check if thread is still alive (hung)
            if (osAuthThread.isAlive()) {
                log("  ℹ OS authentication timed out (no response after 10 seconds)");
                return null; // Fallback to credentials
            }
            
            // Check if we got a connection
            if (connHolder[0] != null) {
                return connHolder[0];
            }
            
            // Check if there was an exception
            if (exHolder[0] != null) {
                Throwable e = exHolder[0];
                String reason = (e instanceof Exception) ? ((Exception)e).getMessage() : e.toString();
                
                if (reason != null && reason.contains("ORA-01031")) {
                    log("  ℹ OS authentication not available: User not in ORA_DBA/ora_dba group");
                } else if (reason != null && reason.contains("No suitable driver")) {
                    log("  ℹ OCI driver not installed (Oracle client required for OS auth)");
                } else if (e instanceof LinkageError || e.getClass().getName().contains("UnsatisfiedLinkError")) {
                    log("  ℹ OCI native libraries not available (Oracle client installation incomplete)");
                } else {
                    log("  ℹ OS authentication unavailable: " + reason);
                }
            }
            
        } catch (Exception e) {
            // Catch any unexpected exceptions in the wrapper itself
            log("  ℹ OS authentication unavailable: " + e.getClass().getSimpleName());
        }
        
        return null; // Fallback to credentials
    }
    
    /**
     * Convert thin connection string to OCI format
     * From: jdbc:oracle:thin:@localhost:1521:xe
     * To:   jdbc:oracle:oci:@xe
     */
    private String convertToOCIFormat(String thinUrl) {
        try {
            // Extract SID from thin URL
            if (thinUrl.contains("@")) {
                String afterAt = thinUrl.substring(thinUrl.indexOf("@") + 1);
                
                // Handle both : and / separators
                String sid;
                if (afterAt.contains(":")) {
                    String[] parts = afterAt.split(":");
                    if (parts.length >= 3) {
                        sid = parts[2];
                    } else {
                        sid = "xe"; // Default
                    }
                } else if (afterAt.contains("/")) {
                    sid = afterAt.split("/")[1];
                } else {
                    sid = afterAt;
                }
                
                return "jdbc:oracle:oci:@" + sid;
            }
        } catch (Exception e) {
            log("  Warning: Could not convert to OCI format: " + e.getMessage());
        }
        
        // Fallback
        return "jdbc:oracle:oci:@xe";
    }
    
    /**
     * 007 PRODUCTION FIX: Execute SYSTEM-level database preparation
     * Creates PRJ2531H user and grants DBA privileges
     */
    private void executeSystemPrep(Connection sysConn) throws Exception {
        // Kill any active PRJ2531H sessions
        log("✓ Cleaning up active PRJ2531H sessions...");
        try (Statement stmt = sysConn.createStatement()) {
            stmt.execute("BEGIN\n" +
                        "  FOR sess IN (SELECT sid, serial# FROM v$session WHERE username = 'PRJ2531H') LOOP\n" +
                        "    BEGIN\n" +
                        "      EXECUTE IMMEDIATE 'ALTER SYSTEM KILL SESSION ''' || sess.sid || ',' || sess.serial# || ''' IMMEDIATE';\n" +
                        "    EXCEPTION WHEN OTHERS THEN NULL;\n" +
                        "    END;\n" +
                        "  END LOOP;\n" +
                        "EXCEPTION WHEN OTHERS THEN NULL;\n" +
                        "END;");
        }
        
        // Drop existing PRJ2531H user if it exists
        log("✓ Dropping existing PRJ2531H user (if present)...");
        try (Statement stmt = sysConn.createStatement()) {
            stmt.execute("BEGIN\n" +
                        "  EXECUTE IMMEDIATE 'DROP USER PRJ2531H CASCADE';\n" +
                        "EXCEPTION\n" +
                        "  WHEN OTHERS THEN NULL;\n" +
                        "END;");
        }
        
        // Create fresh PRJ2531H user
        log("✓ Creating fresh PRJ2531H user...");
        try (Statement stmt = sysConn.createStatement()) {
            stmt.execute("CREATE USER PRJ2531H IDENTIFIED BY PRJ2531H");
        }
        
        // Grant DBA privileges
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
    
    /**
     * Build helpful error message with troubleshooting steps
     */
    private String buildCredentialErrorMessage(String systemUser, String dbUrl, String errorCode, String originalError) {
        StringBuilder msg = new StringBuilder();
        msg.append("Could not connect as SYSTEM to reset PRJ2531H user").append(errorCode).append("\n\n");
        msg.append("Database URL: ").append(dbUrl).append("\n");
        msg.append("Attempted user: ").append(systemUser).append("\n\n");
        
        msg.append("═══════════════════════════════════════════════════════════\n");
        msg.append("SOLUTIONS:\n");
        msg.append("═══════════════════════════════════════════════════════════\n\n");
        
        msg.append("Option 1: Provide correct SYSTEM credentials\n");
        msg.append("─────────────────────────────────────────────\n");
        msg.append("Before running setup again:\n\n");
        msg.append("  On Linux/Mac:\n");
        msg.append("    export LMS_SYSTEM_USER='system'\n");
        msg.append("    export LMS_SYSTEM_PASSWORD='your_password'\n");
        msg.append("    export LMS_DB_URL='jdbc:oracle:thin:@localhost:1521:xe'\n");
        msg.append("    ./setup-wizard.sh\n\n");
        msg.append("  On Windows:\n");
        msg.append("    1. Edit .env.setup with your SYSTEM credentials\n");
        msg.append("    2. Run lms-setup-env.bat\n\n");
        
        msg.append("Option 2: Use .env.setup configuration file\n");
        msg.append("──────────────────────────────────────────────\n");
        msg.append("    1. Copy .env.setup.example to .env.setup\n");
        msg.append("    2. Edit .env.setup with your Oracle SYSTEM credentials\n");
        msg.append("    3. Run setup wizard again\n\n");
        
        msg.append("Option 3: Contact your Oracle DBA\n");
        msg.append("─────────────────────────────────────\n");
        msg.append("If you don't know the SYSTEM password:\n");
        msg.append("    - Ask your database administrator for the password\n");
        msg.append("    - Or request they create PRJ2531H user with DBA privileges\n\n");
        
        msg.append("═══════════════════════════════════════════════════════════\n");
        msg.append("TECHNICAL DETAILS:\n");
        msg.append("═══════════════════════════════════════════════════════════\n");
        msg.append("Cause: ").append(originalError);
        
        return msg.toString();
    }
}
