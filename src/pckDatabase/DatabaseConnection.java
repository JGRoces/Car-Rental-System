package pckDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL  = "jdbc:mysql://localhost:3306/car_rental_db?autoReconnect=true&useSSL=false";
    private static final String USER = "carrentaluser";
    private static final String PASS = "carrentalpass";

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        return new DatabaseConnection();
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("[DB] Connection established successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] ERROR: MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("[DB] ERROR: Could not connect to the database.");
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] ERROR: Failed to close connection.");
                e.printStackTrace();
            }
        }
    }
}
