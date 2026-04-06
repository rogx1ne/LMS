package com.library.setup;

import java.sql.*;
import com.library.database.DBConnection;

public class ConnectionTester {
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        LMS DATABASE CONNECTION TESTER                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        System.out.println("Testing database connection with timezone fix...");
        System.out.println("Timezone Region Flag: " + System.getProperty("oracle.jdbc.timezoneAsRegion", "NOT SET"));
        System.out.println();
        
        try {
            System.out.print("Connecting to Oracle...");
            Connection conn = DBConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println(" ✓ SUCCESS");
                System.out.println();
                System.out.println("Connection Details:");
                
                // Get metadata
                DatabaseMetaData metadata = conn.getMetaData();
                System.out.println("  Database URL: " + metadata.getURL());
                System.out.println("  Database User: " + metadata.getUserName());
                System.out.println("  Database Product: " + metadata.getDatabaseProductName());
                System.out.println("  Database Version: " + metadata.getDatabaseProductVersion());
                System.out.println();
                
                // Test a simple query
                System.out.print("Testing simple query (SELECT 1)...");
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL");
                
                if (rs.next()) {
                    System.out.println(" ✓ SUCCESS");
                    System.out.println("  Result: " + rs.getInt(1));
                }
                
                rs.close();
                stmt.close();
                conn.close();
                
                System.out.println();
                System.out.println("═══════════════════════════════════════════════════════════");
                System.out.println("✓ DATABASE CONNECTION TEST PASSED");
                System.out.println("═══════════════════════════════════════════════════════════");
            } else {
                System.out.println(" ✗ FAILED (connection is closed)");
                System.exit(1);
            }
            
        } catch (SQLException e) {
            System.out.println(" ✗ FAILED");
            System.out.println();
            System.out.println("Error Details:");
            System.out.println("  Code: " + e.getErrorCode());
            System.out.println("  State: " + e.getSQLState());
            System.out.println("  Message: " + e.getMessage());
            System.out.println();
            
            // Specific error handling
            if (e.getMessage().contains("ORA-01882")) {
                System.out.println("DIAGNOSIS: Timezone region not found");
                System.out.println("SOLUTION: The -Doracle.jdbc.timezoneAsRegion=false flag");
                System.out.println("          may not be working. Try:");
                System.out.println("          1. Restart the Oracle container");
                System.out.println("          2. Check Oracle initialization logs");
                System.out.println("          3. Verify Java timezone settings");
            } else if (e.getMessage().contains("Connection refused")) {
                System.out.println("DIAGNOSIS: Cannot connect to Oracle");
                System.out.println("SOLUTION: Make sure Podman Oracle is running:");
                System.out.println("          $ podman ps | grep oracle");
            } else if (e.getMessage().contains("IO Error")) {
                System.out.println("DIAGNOSIS: Network or IO error");
                System.out.println("SOLUTION: Check Oracle container network:");
                System.out.println("          $ podman inspect oracle10g");
            }
            
            System.exit(1);
        }
    }
}
