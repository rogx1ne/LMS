package com.library.setup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles database initialization, schema creation, and Fresh vs Repair installation
 */
public class DatabaseInitializer {
    
    private String host;
    private int port;
    private String sid;
    private String username;
    private String password;
    private Connection connection;
    
    public enum InstallationType {
        FRESH_INSTALL,  // Drop all, recreate schema, load sample data
        REPAIR          // Keep data, update schema if needed
    }
    
    public DatabaseInitializer(String host, int port, String sid, String username, String password) {
        this.host = host;
        this.port = port;
        this.sid = sid;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Constructor with JDBC URL
     */
    public DatabaseInitializer(String url, String username, String password) throws SQLException {
        this.username = username;
        this.password = password;
        
        // Parse URL to extract host, port, sid
        // Format: jdbc:oracle:thin:@host:port:sid
        String[] parts = url.replace("jdbc:oracle:thin:@", "").split(":");
        if (parts.length >= 3) {
            this.host = parts[0];
            this.port = Integer.parseInt(parts[1]);
            this.sid = parts[2];
        } else {
            throw new SQLException("Invalid JDBC URL format");
        }
    }
    
    /**
     * Get the database connection
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            testConnection();
        }
        return connection;
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
            connection = DriverManager.getConnection(url, username, password);
            return connection != null && !connection.isClosed();
        } catch (ClassNotFoundException e) {
            throw new SQLException("Oracle JDBC driver not found: " + e.getMessage());
        }
    }
    
    /**
     * Check if LMS schema exists (check for key tables)
     */
    public boolean schemaExists() throws SQLException {
        if (connection == null || connection.isClosed()) {
            testConnection();
        }
        
        String[] keyTables = {"TBL_USER", "TBL_ACCESSION", "TBL_STUDENT", "TBL_ISSUE"};
        
        DatabaseMetaData meta = connection.getMetaData();
        int foundTables = 0;
        
        for (String table : keyTables) {
            ResultSet rs = meta.getTables(null, username.toUpperCase(), table, new String[]{"TABLE"});
            if (rs.next()) {
                foundTables++;
            }
            rs.close();
        }
        
        return foundTables == keyTables.length;
    }
    
    /**
     * Check if any user data exists in TBL_USER
     */
    public boolean hasExistingUsers() throws SQLException {
        if (connection == null || connection.isClosed()) {
            testConnection();
        }
        
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TBL_USER");
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            // Table might not exist
            return false;
        }
    }
    
    /**
     * Drop all LMS tables (for fresh install)
     */
    public void dropAllTables() throws SQLException {
        if (connection == null || connection.isClosed()) {
            testConnection();
        }
        
        // Get all tables owned by this user
        List<String> tables = new ArrayList<>();
        DatabaseMetaData meta = connection.getMetaData();
        ResultSet rs = meta.getTables(null, username.toUpperCase(), "%", new String[]{"TABLE"});
        
        while (rs.next()) {
            String tableName = rs.getString("TABLE_NAME");
            if (tableName.startsWith("TBL_") || tableName.startsWith("CIRC_")) {
                tables.add(tableName);
            }
        }
        rs.close();
        
        // Drop tables
        Statement stmt = connection.createStatement();
        for (String table : tables) {
            try {
                stmt.execute("DROP TABLE " + table + " CASCADE CONSTRAINTS");
            } catch (SQLException e) {
                // Table might not exist or have dependencies, continue
                System.err.println("Warning: Could not drop table " + table + ": " + e.getMessage());
            }
        }
        stmt.close();
    }
    
    /**
     * Execute SQL script file
     */
    public void executeScript(File scriptFile, ProgressCallback callback) throws Exception {
        if (connection == null || connection.isClosed()) {
            testConnection();
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
        StringBuilder currentStatement = new StringBuilder();
        String line;
        int lineNumber = 0;
        int statementsExecuted = 0;
        int totalLines = countLines(scriptFile);
        
        Statement stmt = connection.createStatement();
        
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            line = line.trim();
            
            // Skip comments and empty lines
            if (line.isEmpty() || line.startsWith("--") || line.startsWith("//")) {
                continue;
            }
            
            currentStatement.append(line).append(" ");
            
            // Check if statement is complete (ends with ;)
            if (line.endsWith(";")) {
                String sql = currentStatement.toString().trim();
                sql = sql.substring(0, sql.length() - 1); // Remove trailing semicolon
                
                try {
                    stmt.execute(sql);
                    statementsExecuted++;
                    
                    if (callback != null) {
                        int progress = (lineNumber * 100) / totalLines;
                        callback.onProgress(progress, "Executed statement " + statementsExecuted);
                    }
                } catch (SQLException e) {
                    System.err.println("Error executing SQL at line " + lineNumber + ": " + e.getMessage());
                    System.err.println("SQL: " + sql);
                    // Continue with next statement
                }
                
                currentStatement = new StringBuilder();
            }
        }
        
        reader.close();
        stmt.close();
        
        if (callback != null) {
            callback.onProgress(100, "Script executed: " + statementsExecuted + " statements");
        }
    }
    
    /**
     * Perform fresh installation
     */
    public void performFreshInstall(File scriptSql, ProgressCallback callback) throws Exception {
        if (callback != null) {
            callback.onProgress(0, "Starting fresh installation...");
        }
        
        // Step 1: Drop existing tables
        if (callback != null) {
            callback.onProgress(10, "Dropping existing tables...");
        }
        dropAllTables();
        
        // Step 2: Execute schema creation script
        if (callback != null) {
            callback.onProgress(20, "Creating database schema...");
        }
        executeScript(scriptSql, new ProgressCallback() {
            @Override
            public void onProgress(int percent, String message) {
                if (callback != null) {
                    int adjustedPercent = 20 + (percent * 80 / 100); // 20-100%
                    callback.onProgress(adjustedPercent, "Schema: " + message);
                }
            }
        });
        
        if (callback != null) {
            callback.onProgress(100, "Fresh installation completed!");
        }
    }
    
    /**
     * Perform repair installation
     */
    public void performRepair(File scriptSql, ProgressCallback callback) throws Exception {
        if (callback != null) {
            callback.onProgress(0, "Starting repair...");
        }
        
        // For repair, we only update schema without dropping data
        // This is a simplified version - in production, you'd want more sophisticated migration logic
        
        if (callback != null) {
            callback.onProgress(20, "Checking schema integrity...");
        }
        
        // Execute script but ignore errors for tables that already exist
        executeScript(scriptSql, new ProgressCallback() {
            @Override
            public void onProgress(int percent, String message) {
                if (callback != null) {
                    int adjustedPercent = 20 + (percent * 80 / 100);
                    callback.onProgress(adjustedPercent, "Repair: " + message);
                }
            }
        });
        
        if (callback != null) {
            callback.onProgress(100, "Repair completed!");
        }
    }
    
    private int countLines(File file) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();
        return lines;
    }
    
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Callback interface for progress updates
     */
    public interface ProgressCallback {
        void onProgress(int percent, String message);
    }
}
