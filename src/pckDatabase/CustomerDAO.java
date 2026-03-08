package pckDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pckModels.Customer;

/**
 * CustomerDAO.java
 * Handles all database queries for the customers table.
 * Used by RentalService and ManageCustomersGUI (via a Service).
 *
 * Rule: Never call this directly from a GUI — always go through a Service.
 */
public class CustomerDAO {

    // -------------------------
    // DB Connection
    // -------------------------
    private final Connection connection;

    public CustomerDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // -------------------------
    // INSERT — Add a new customer
    // Inserts into both `users` and `customers` tables.
    // Returns true if successful, false otherwise.
    // Called by a Service when registering a new customer.
    // -------------------------
    public boolean addCustomer(Customer customer) {
        String insertUser     = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";
        String insertCustomer = "INSERT INTO customers (user_id, phone_number, address, license_number) VALUES (?, ?, ?, ?)";

        try {
            // Disable auto-commit for transaction safety
            connection.setAutoCommit(false);

            // Step 1: Insert into users table
            try (PreparedStatement userStmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, customer.getFullName());
                userStmt.setString(2, customer.getEmail());
                userStmt.setString(3, customer.getPassword());
                userStmt.setString(4, "CUSTOMER");
                userStmt.executeUpdate();

                // Get the generated user_id
                ResultSet keys = userStmt.getGeneratedKeys();
                if (!keys.next()) {
                    connection.rollback();
                    return false;
                }
                int generatedUserId = keys.getInt(1);

                // Step 2: Insert into customers table using the generated user_id
                try (PreparedStatement custStmt = connection.prepareStatement(insertCustomer)) {
                    custStmt.setInt(1, generatedUserId);
                    custStmt.setString(2, customer.getPhoneNumber());
                    custStmt.setString(3, customer.getAddress());
                    custStmt.setString(4, customer.getLicenseNumber());
                    custStmt.executeUpdate();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to add customer.");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;

        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // -------------------------
    // SELECT ALL — Get all customers
    // Returns a list of all Customer objects.
    // Called by ManageCustomersGUI (via Service) to populate the table.
    // -------------------------
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT u.user_id, u.full_name, u.email, u.password, " +
                       "c.customer_id, c.phone_number, c.address, c.license_number " +
                       "FROM users u " +
                       "JOIN customers c ON u.user_id = c.user_id " +
                       "ORDER BY c.customer_id ASC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to fetch all customers.");
            e.printStackTrace();
        }

        return customers;
    }

    // -------------------------
    // SELECT ONE — Get customer by customer_id
    // Returns a Customer object, or null if not found.
    // Called by RentalService to validate the customer before booking.
    // -------------------------
    public Customer getCustomerById(int customerId) {
        String query = "SELECT u.user_id, u.full_name, u.email, u.password, " +
                       "c.customer_id, c.phone_number, c.address, c.license_number " +
                       "FROM users u " +
                       "JOIN customers c ON u.user_id = c.user_id " +
                       "WHERE c.customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to fetch customer by ID.");
            e.printStackTrace();
        }

        return null;
    }

    // -------------------------
    // SELECT ONE — Get customer by user_id
    // Returns a Customer object, or null if not found.
    // Called by SessionManager after login to load the full customer profile.
    // -------------------------
    public Customer getCustomerByUserId(int userId) {
        String query = "SELECT u.user_id, u.full_name, u.email, u.password, " +
                       "c.customer_id, c.phone_number, c.address, c.license_number " +
                       "FROM users u " +
                       "JOIN customers c ON u.user_id = c.user_id " +
                       "WHERE u.user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to fetch customer by user ID.");
            e.printStackTrace();
        }

        return null;
    }

    // -------------------------
    // UPDATE — Edit an existing customer
    // Updates both `users` and `customers` tables.
    // Returns true if successful, false otherwise.
    // -------------------------
    public boolean updateCustomer(Customer customer) {
        String updateUser     = "UPDATE users SET full_name = ?, email = ? WHERE user_id = ?";
        String updateCustomer = "UPDATE customers SET phone_number = ?, address = ?, license_number = ? WHERE customer_id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement userStmt = connection.prepareStatement(updateUser)) {
                userStmt.setString(1, customer.getFullName());
                userStmt.setString(2, customer.getEmail());
                userStmt.setInt(3, customer.getUserId());
                userStmt.executeUpdate();
            }

            try (PreparedStatement custStmt = connection.prepareStatement(updateCustomer)) {
                custStmt.setString(1, customer.getPhoneNumber());
                custStmt.setString(2, customer.getAddress());
                custStmt.setString(3, customer.getLicenseNumber());
                custStmt.setInt(4, customer.getCustomerId());
                custStmt.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to update customer.");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;

        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // -------------------------
    // DELETE — Remove a customer
    // Deletes from `customers` first, then `users` (FK constraint order).
    // Returns true if successful, false otherwise.
    // -------------------------
    public boolean deleteCustomer(int customerId, int userId) {
        String deleteCustomer = "DELETE FROM customers WHERE customer_id = ?";
        String deleteUser     = "DELETE FROM users WHERE user_id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement custStmt = connection.prepareStatement(deleteCustomer)) {
                custStmt.setInt(1, customerId);
                custStmt.executeUpdate();
            }

            try (PreparedStatement userStmt = connection.prepareStatement(deleteUser)) {
                userStmt.setInt(1, userId);
                userStmt.executeUpdate();
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to delete customer.");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;

        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // -------------------------
    // CHECK — Does email already exist?
    // Used by ValidationUtil before inserting a new customer.
    // -------------------------
    public boolean emailExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to check email existence.");
            e.printStackTrace();
        }
        return false;
    }

    // -------------------------
    // CHECK — Does license number already exist?
    // Used by ValidationUtil before inserting a new customer.
    // -------------------------
    public boolean licenseExists(String licenseNumber) {
        String query = "SELECT COUNT(*) FROM customers WHERE license_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, licenseNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: Failed to check license existence.");
            e.printStackTrace();
        }
        return false;
    }

    // -------------------------
    // HELPER — Map a ResultSet row to a Customer object
    // Keeps all SELECT methods consistent and avoids code duplication.
    // -------------------------
    private Customer mapResultSet(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("user_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getInt("customer_id"),
            rs.getString("phone_number"),
            rs.getString("address"),
            rs.getString("license_number")
        );
    }
}