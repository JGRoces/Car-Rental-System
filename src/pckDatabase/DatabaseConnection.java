package pckDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/car_rental_db";
    private static final String USER = "carrentaluser";
    private static final String PASS = "carrentalpass";

    private static Connection sharedConnection = null;

    private DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        return new DatabaseConnection();
    }

    public Connection getConnection() {
        try {
            if (sharedConnection == null || sharedConnection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                sharedConnection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DB] Connection established.");
            }
            return sharedConnection;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] FATAL: MySQL JDBC Driver not found — ensure mysql-connector-j is on the classpath.");
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            System.err.println("[DB] Connection failed or closed — attempting reconnect. Reason: " + e.getMessage());
            try {
                sharedConnection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DB] Reconnected successfully.");
                return sharedConnection;
            } catch (SQLException ex) {
                System.err.println("[DB] FATAL: Reconnect failed — " + ex.getMessage());
                throw new RuntimeException("Database connection unavailable.", ex);
            }
        }
    }

    public void closeConnection() {
        if (sharedConnection != null) {
            try {
                sharedConnection.close();
                sharedConnection = null;
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Warning: Failed to close connection — " + e.getMessage());
            }
        }
    }
}
