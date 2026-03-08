package pckDatabase;

import pckModels.Customer;
import java.sql.*;

/**
 * CustomerDAO.java
 * All database operations for Customer accounts.
 *
 * Flow for sign-up:
 *   CustomerSignUpGUI → CustomerService → CustomerDAO → DB
 *
 * Two-step insert:
 *   1. Insert into users   → get generated user_id
 *   2. Insert into customers using that user_id
 *   Both inside one transaction — if either fails, both roll back.
 */
public class CustomerDAO {

    private final Connection connection;

    public CustomerDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // =========================================================
    //  CREATE — Register a new customer account
    //  Returns the created Customer with IDs filled in,
    //  or null if something failed.
    // =========================================================
    public Customer createAccount(Customer customer) {
        String insertUser     = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, 'CUSTOMER')";
        String insertCustomer = "INSERT INTO customers (user_id, phone, photo_path) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);    // start transaction

            // Step 1 — insert into users
            int userId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    insertUser, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, customer.getFullName());
                stmt.setString(2, customer.getEmail());
                stmt.setString(3, customer.getPassword());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user_id generated.");
                userId = keys.getInt(1);
                customer.setUserId(userId);
            }

            // Step 2 — insert into customers
            int customerId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    insertCustomer, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, userId);
                stmt.setString(2, customer.getPhone());
                stmt.setString(3, customer.getPhotoPath());     // nullable
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No customer_id generated.");
                customerId = keys.getInt(1);
                customer.setCustomerId(customerId);
            }

            connection.commit();
            System.out.println("[CustomerDAO] Account created — userId=" + userId
                + ", customerId=" + customerId);
            return customer;

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: createAccount failed — rolling back.");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return null;

        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // =========================================================
    //  READ — Get customer by user_id
    //  Used after login to load the full customer profile.
    // =========================================================
    public Customer getCustomerByUserId(int userId) {
        String query = """
            SELECT u.user_id, c.customer_id, u.full_name, u.email, u.password,
                   c.phone, c.photo_path
            FROM users u
            JOIN customers c ON u.user_id = c.user_id
            WHERE u.user_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Customer(
                    rs.getInt("user_id"),
                    rs.getInt("customer_id"),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("phone"),
                    rs.getString("photo_path")
                );
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: getCustomerByUserId failed.");
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================
    //  CHECK — Email already registered?
    //  Used by CustomerService before inserting a new account.
    // =========================================================
    public boolean emailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: emailExists check failed.");
            e.printStackTrace();
        }
        return false;
    }
}
