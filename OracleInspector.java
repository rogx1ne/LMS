import java.sql.*;

/**
 * 007 Security Forensics - Oracle Database State Inspector
 * Investigates what data exists in the Oracle database
 */
public class OracleInspector {
    
    public static void main(String[] args) {
        System.out.println("🔍 007 ORACLE FORENSICS - Database State Inspector");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        String[] connections = {
            "PRJ2531H:PRJ2531H",
            "SYSTEM:system",  
            "sys:system"
        };
        
        for (String connStr : connections) {
            String[] parts = connStr.split(":");
            String user = parts[0];
            String pass = parts[1];
            
            System.out.println("\n🔌 Testing connection: " + user + "/" + pass);
            String separator = "";
            for (int i = 0; i < 50; i++) separator += "─";
            System.out.println(separator);
            
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:1521:xe", user, pass)) {
                
                System.out.println("✅ Connected as: " + user);
                
                // Check current schema
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT USER FROM DUAL");
                    if (rs.next()) {
                        System.out.println("📋 Current schema: " + rs.getString(1));
                    }
                }
                
                // List all tables accessible to this user
                System.out.println("\n📊 Tables accessible to " + user + ":");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT OWNER, TABLE_NAME FROM all_tables " +
                        "WHERE TABLE_NAME LIKE 'TBL_%' " +
                        "ORDER BY OWNER, TABLE_NAME"
                    );
                    
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        System.out.println("  " + rs.getString("OWNER") + "." + rs.getString("TABLE_NAME"));
                    }
                    if (!found) {
                        System.out.println("  (No TBL_* tables found)");
                    }
                }
                
                // Check TBL_CREDENTIALS data if table exists
                System.out.println("\n👤 TBL_CREDENTIALS data check:");
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery(
                        "SELECT USER_ID, NAME, ROLE FROM TBL_CREDENTIALS"
                    );
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        System.out.println("  User: " + rs.getString("USER_ID").trim() + 
                                         " | Name: " + rs.getString("NAME") + 
                                         " | Role: " + rs.getString("ROLE"));
                    }
                    if (!hasData) {
                        System.out.println("  (No user data found)");
                    }
                } catch (SQLException e) {
                    System.out.println("  ❌ TBL_CREDENTIALS not accessible: " + e.getMessage());
                }
                
            } catch (SQLException e) {
                System.out.println("❌ Connection failed: " + e.getMessage());
            }
        }
        
        System.out.println("\n🏁 Oracle forensics complete.");
    }
}