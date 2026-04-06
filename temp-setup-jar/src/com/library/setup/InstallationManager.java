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
        File dummySql = new File(setupJarLocation, "dummy.sql");
        if (scriptSql.exists()) {
            Files.copy(scriptSql.toPath(), new File(installDir, "script.sql").toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
            log("  ✓ Copied script.sql");
        }
        if (dummySql.exists()) {
            Files.copy(dummySql.toPath(), new File(installDir, "dummy.sql").toPath(), 
                      StandardCopyOption.REPLACE_EXISTING);
            log("  ✓ Copied dummy.sql");
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
        String dbUrl = System.getenv("LMS_DB_URL");
        String dbUser = System.getenv("LMS_DB_USER");
        String dbPassword = System.getenv("LMS_DB_PASSWORD");
        
        if (dbUrl == null) dbUrl = "jdbc:oracle:thin:@localhost:1521:xe";
        if (dbUser == null) dbUser = "PRJ2531H";
        if (dbPassword == null) dbPassword = "PRJ2531H";
        
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
            // Check if user already exists
            String checkQuery = "SELECT USER_ID FROM TBL_CREDENTIALS WHERE USER_ID = TRIM(?)";
            try (PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {
                pstmt.setString(1, adminUserId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        log("⚠ Admin user '" + adminUserId + "' already exists. Skipping creation.");
                        return;
                    }
                }
            }
            
            // Hash the password
            String hashedPassword = PasswordHasher.hashPassword(adminPassword);
            
            // Insert admin user
            String insertQuery = "INSERT INTO TBL_CREDENTIALS (USER_ID, NAME, PSWD, EMAIL, PHNO, ROLE, STATUS) " +
                               "VALUES (?, ?, ?, ?, ?, 'ADMIN', 'ACTIVE')";
            try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                pstmt.setString(1, adminUserId);
                pstmt.setString(2, adminName);
                pstmt.setString(3, hashedPassword);
                pstmt.setString(4, adminEmail);
                pstmt.setLong(5, Long.parseLong(adminPhone));
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    log("✓ Admin user created: " + adminUserId);
                }
            }
        } catch (SQLException e) {
            throw new Exception("Failed to create admin user: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new Exception("Invalid phone number format (must be numeric): " + e.getMessage());
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
        // Read script.sql from project root
        File scriptFile = new File("script.sql");
        if (!scriptFile.exists()) {
            log("⚠ script.sql not found at: " + scriptFile.getAbsolutePath());
            return;
        }
        
        log("Executing database initialization script...");
        
        try (Scanner scanner = new Scanner(scriptFile)) {
            StringBuilder statement = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) continue;
                
                statement.append(line).append("\n");
                
                // Execute when statement ends with /
                if (line.endsWith("/")) {
                    String sql = statement.toString().replace("/", "").trim();
                    if (!sql.isEmpty()) {
                        try {
                            Statement stmt = conn.createStatement();
                            stmt.execute(sql);
                            stmt.close();
                        } catch (SQLException e) {
                            // Some statements may fail if objects already exist
                            // This is expected and handled by script's exception handlers
                            if (!e.getMessage().contains("already exists")) {
                                log("  SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                            }
                        }
                    }
                    statement = new StringBuilder();
                }
            }
            
            log("✓ Database initialization script executed");
        } catch (IOException e) {
            throw new Exception("Failed to read script.sql: " + e.getMessage());
        }
    }
    
    private void log(String message) {
        System.out.println(message);
        if (listener != null) {
            listener.onProgress(message);
        }
    }
}
