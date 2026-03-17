package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:oracle:thin:@localhost:1521:xe";
    private static final String DEFAULT_USER = "PRJ2531H";
    private static final String DEFAULT_PASSWORD = "PRJ2531H";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(resolve("LMS_DB_URL", DEFAULT_URL), resolve("LMS_DB_USER", DEFAULT_USER), resolve("LMS_DB_PASSWORD", DEFAULT_PASSWORD));
        } catch (ClassNotFoundException e) {
            System.err.println("Oracle Driver not found! Add ojdbc.jar to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection Failed! Check URL, User, or Password.");
            e.printStackTrace();
        }
        return conn;
    }

    private static String resolve(String envKey, String fallback) {
        String value = System.getenv(envKey);
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }
}
