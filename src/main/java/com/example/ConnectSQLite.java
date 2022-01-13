package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.Cleanup;
import lombok.val;

public class ConnectSQLite {

    /**
     * Connect to a sample database
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:data/java-sqlite.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public static void execute(String sql) {

        try {
            @Cleanup
            val conn = ConnectSQLite.connect();
            @Cleanup
            val stmt = conn.createStatement();
            stmt.execute(sql);
            System.out.println("Create table finished.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void executeUpdate(String sql) {

        try {
            @Cleanup
            val conn = ConnectSQLite.connect();
            @Cleanup
            val pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "name");
            pstmt.setDouble(2, 0);
            pstmt.executeUpdate();
            System.out.println("Insert data finished.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void executeQuery(String sql) {
        try {
            @Cleanup
            val conn = ConnectSQLite.connect();
            @Cleanup
            val stmt = conn.createStatement();
            @Cleanup
            val rs = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getInt("id") + "\t" + rs.getString("name") + "\t" + rs.getDouble("capacity"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
