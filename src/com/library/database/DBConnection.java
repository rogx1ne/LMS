package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Oracle 10g standard URL structure
    // Replace 'localhost' with your IP if DB is remote
    // Replace 'xe' with your SID if different (often 'orcl' or 'xe')
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:xe";

    // YOUR Oracle Credentials
    private static final String USER = "PRJ2531H"; // or your created user
    private static final String PASSWORD = "PRJ2531H";

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // Load Oracle Driver
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Establish Connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Connection Successful!"); // Uncomment for debugging

        } catch (ClassNotFoundException e) {
            System.err.println("Oracle Driver not found! Add ojdbc.jar to classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection Failed! Check URL, User, or Password.");
            e.printStackTrace();
        }
        return conn;
    }
}