package pckDatabase;

import pckModels.User;
import java.sql.*;

/**
 * UserDAO.java
 * Handles all database queries related to user authentication.
 * Used by AuthService.java to verify login credentials.
 */
public class UserDAO {

    // -------------------------
    // DB Connection
    // -------------------------
    private final Connection connection;

    public UserDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // -------------------------
    // Find user by email and password
    // Returns a User object if credentials match, null if not found.
    // Called by AuthService.login()
    // -------------------------
    public User getUserByEmailAndPassword(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] ERROR: Failed to execute login query.");
            e.printStackTrace();
        }

        return null; // No match found
    }

    // -------------------------
    // Find user by email only
    // Useful for checking if an email already exists
    // -------------------------
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
                );
            }

        } catch (SQLException e) {
            System.err.println("[UserDAO] ERROR: Failed to find user by email.");
            e.printStackTrace();
        }

        return null;
    }
}
