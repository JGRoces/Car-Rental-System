package pckDatabase;

import pckModels.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public Customer createAccount(Customer customer) {
        String insertUser     = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, 'CUSTOMER')";
        String insertCustomer = "INSERT INTO customers (user_id, phone, photo_path) VALUES (?, ?, ?)";
        Connection connection = DatabaseConnection.getInstance().getConnection();
        if (connection == null) {
            System.err.println("[CustomerDAO] ERROR: createAccount failed — no DB connection.");
            return null;
        }
        try {
            connection.setAutoCommit(false);
            int userId;
            try (PreparedStatement stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, customer.getFullName());
                stmt.setString(2, customer.getEmail());
                stmt.setString(3, customer.getPassword());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user_id generated.");
                userId = keys.getInt(1);
                customer.setUserId(userId);
            }
            int customerId;
            try (PreparedStatement stmt = connection.prepareStatement(insertCustomer, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setString(2, customer.getPhone());
                stmt.setString(3, customer.getPhotoPath());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No customer_id generated.");
                customerId = keys.getInt(1);
                customer.setCustomerId(customerId);
            }
            connection.commit();
            System.out.println("[CustomerDAO] Account created — userId=" + userId + ", customerId=" + customerId);
            return customer;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: createAccount failed — rolling back. " + e.getMessage());
            try { connection.rollback(); } catch (SQLException ex) {
                System.err.println("[CustomerDAO] ERROR: Rollback failed — " + ex.getMessage());
            }
            return null;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) {
                System.err.println("[CustomerDAO] WARNING: Could not reset autoCommit — " + e.getMessage());
            }
        }
    }

    public Customer getCustomerByUserId(int userId) {
        String sql = """
            SELECT u.user_id, c.customer_id, u.full_name, u.email, u.password, c.phone, c.photo_path
            FROM users u JOIN customers c ON u.user_id = c.user_id
            WHERE u.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: getCustomerByUserId failed.");
            e.printStackTrace();
        }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = """
            SELECT c.customer_id, c.user_id, u.full_name, u.email, u.password, c.phone, c.photo_path
            FROM customers c JOIN users u ON c.user_id = u.user_id ORDER BY c.customer_id
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: getAllCustomers failed.");
            e.printStackTrace();
        }
        return list;
    }

    public List<Customer> getAllCustomerProfiles() {
        return getAllCustomers();
    }

    public Customer getCustomerById(int customerId) {
        String sql = """
            SELECT c.customer_id, c.user_id, u.full_name, u.email, u.password, c.phone, c.photo_path
            FROM customers c JOIN users u ON c.user_id = u.user_id
            WHERE c.customer_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: getCustomerById failed.");
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] ERROR: emailExists check failed.");
            e.printStackTrace();
        }
        return false;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("user_id"), rs.getInt("customer_id"),
            rs.getString("full_name"), rs.getString("email"),
            rs.getString("password"), rs.getString("phone"),
            rs.getString("photo_path")
        );
    }
}
