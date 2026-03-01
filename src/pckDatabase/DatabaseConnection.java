package pckDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection.java
 * Singleton class that manages the MySQL database connection.
 * Only one connection instance is created and reused throughout the app.
 *
 * ⚠️ IMPORTANT FOR GITHUB:
 * Do NOT push real credentials to GitHub.
 * Before committing, replace USER and PASS with placeholder values:
 *   USER = "your_username"
 *   PASS = "your_password"
 * Share the real credentials privately with members only.
 */
public class DatabaseConnection {

    // -------------------------
    // Database Credentials
    // -------------------------
    private static final String URL  = "jdbc:mysql://localhost:3306/car_rental_db";
    private static final String USER = " ";
    private static final String PASS = " ";

    // -------------------------
    // Singleton Instance
    // -------------------------
    private static DatabaseConnection instance    = null;
    private        Connection         connection  = null;

    // -------------------------
    // Private Constructor
    // Prevents external instantiation — use getInstance() instead.
    // -------------------------
    private DatabaseConnection() {
        try {
            // Load the MySQL JDBC driver (required for Java 21)
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            this.connection = DriverManager.getConnection(URL, USER, PASS);

            System.out.println("[DB] Connection established successfully.");

        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ERROR: MySQL JDBC Driver not found.");
            System.err.println("     Make sure mysql-connector-j is added to your project libraries.");
            e.printStackTrace();

        } catch (SQLException e) {
            System.err.println("[DB] ERROR: Could not connect to the database.");
            System.err.println("     Check that MySQL is running and your credentials are correct.");
            e.printStackTrace();
        }
    }

    // -------------------------
    // Get Singleton Instance
    // Call this from any DAO class to get the connection.
    // Usage: Connection conn = DatabaseConnection.getInstance().getConnection();
    // -------------------------
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // -------------------------
    // Get the Active Connection
    // -------------------------
    public Connection getConnection() {
        try {
            // Reconnect automatically if the connection was dropped
            if (connection == null || connection.isClosed()) {
                System.out.println("[DB] Connection lost — reconnecting...");
                instance   = null;
                instance   = new DatabaseConnection();
                connection = instance.getConnection();
            }
        } catch (SQLException e) {
            System.err.println("[DB] ERROR: Failed to check connection status.");
            e.printStackTrace();
        }
        return connection;
    }

    // -------------------------
    // Close the Connection
    // Call this when the application exits.
    // -------------------------
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                instance = null;
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] ERROR: Failed to close connection.");
                e.printStackTrace();
            }
        }
    }
}
