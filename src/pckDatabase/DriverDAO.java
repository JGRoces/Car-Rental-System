package pckDatabase;

import pckModels.Driver;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverDAO.java
 * All database operations for Driver accounts.
 *
 * Flow for sign-up:
 *   DriverSignUpGUI → DriverService → DriverDAO → DB
 *
 * Two-step insert (same pattern as CustomerDAO):
 *   1. Insert into users   → get generated user_id
 *   2. Insert into drivers using that user_id
 *   Both in one transaction.
 *
 * Admin verification flow:
 *   AdminDashboardGUI → DriverService → DriverDAO.updateStatus()
 */
public class DriverDAO {

    private final Connection connection;

    public DriverDAO() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    // =========================================================
    //  CREATE — Register a new driver account
    //  Returns the created Driver with IDs filled in, or null.
    // =========================================================
    public Driver createAccount(Driver driver) {
        String insertUser   = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, 'DRIVER')";
        String insertDriver = """
            INSERT INTO drivers
                (user_id, phone, license_number, license_expiry, vehicle_type, status, photo_path)
            VALUES (?, ?, ?, ?, ?, 'PENDING', ?)
            """;

        try {
            connection.setAutoCommit(false);

            // Step 1 — insert into users
            int userId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    insertUser, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, driver.getFullName());
                stmt.setString(2, driver.getEmail());
                stmt.setString(3, driver.getPassword());
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user_id generated.");
                userId = keys.getInt(1);
                driver.setUserId(userId);
            }

            // Step 2 — insert into drivers
            int driverId;
            try (PreparedStatement stmt = connection.prepareStatement(
                    insertDriver, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setInt(1, userId);
                stmt.setString(2, driver.getPhone());
                stmt.setString(3, driver.getLicenseNumber());
                // Convert "MM/DD/YYYY" → SQL Date "YYYY-MM-DD"
                stmt.setString(4, convertDateToSql(driver.getLicenseExpiry()));
                stmt.setString(5, driver.getVehicleType());
                stmt.setString(6, driver.getPhotoPath());   // nullable
                stmt.executeUpdate();

                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No driver_id generated.");
                driverId = keys.getInt(1);
                driver.setDriverId(driverId);
            }

            connection.commit();
            System.out.println("[DriverDAO] Account created — userId=" + userId
                + ", driverId=" + driverId + " | Status: PENDING");
            return driver;

        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: createAccount failed — rolling back.");
            e.printStackTrace();
            try { connection.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return null;

        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // =========================================================
    //  READ — Get all PENDING drivers
    //  Used by AdminDashboard to list drivers awaiting verification.
    // =========================================================
    public List<Driver> getPendingDrivers() {
        return getDriversByStatus(Driver.STATUS_PENDING);
    }

    // =========================================================
    //  READ — Get all drivers by status
    //  Pass Driver.STATUS_PENDING / VERIFIED / REJECTED
    // =========================================================
    public List<Driver> getDriversByStatus(String status) {
        String query = """
            SELECT u.user_id, d.driver_id, u.full_name, u.email, u.password,
                   d.phone, d.license_number, d.license_expiry, d.vehicle_type,
                   d.status, d.photo_path, d.verified_at
            FROM users u
            JOIN drivers d ON u.user_id = d.user_id
            WHERE d.status = ?
            ORDER BY u.created_at ASC
            """;

        List<Driver> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: getDriversByStatus failed.");
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================
    //  READ — Get driver by user_id
    // =========================================================
    public Driver getDriverByUserId(int userId) {
        String query = """
            SELECT u.user_id, d.driver_id, u.full_name, u.email, u.password,
                   d.phone, d.license_number, d.license_expiry, d.vehicle_type,
                   d.status, d.photo_path, d.verified_at
            FROM users u
            JOIN drivers d ON u.user_id = d.user_id
            WHERE u.user_id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: getDriverByUserId failed.");
            e.printStackTrace();
        }
        return null;
    }

    // =========================================================
    //  UPDATE — Admin verifies or rejects a driver
    //  status must be Driver.STATUS_VERIFIED or STATUS_REJECTED
    // =========================================================
    public boolean updateStatus(int driverId, String status) {
        String query = status.equals(Driver.STATUS_VERIFIED)
            ? "UPDATE drivers SET status = ?, verified_at = CURRENT_TIMESTAMP WHERE driver_id = ?"
            : "UPDATE drivers SET status = ?, verified_at = NULL WHERE driver_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, driverId);
            int rows = stmt.executeUpdate();
            System.out.println("[DriverDAO] Driver " + driverId + " status → " + status);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: updateStatus failed.");
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    //  CHECK — Email or license already registered?
    // =========================================================
    public boolean emailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: emailExists check failed.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean licenseExists(String licenseNumber) {
        String query = "SELECT 1 FROM drivers WHERE license_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, licenseNumber);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: licenseExists check failed.");
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    //  PRIVATE — Map ResultSet row to Driver object
    // =========================================================
    private Driver mapRow(ResultSet rs) throws SQLException {
        return new Driver(
            rs.getInt("user_id"),
            rs.getInt("driver_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("phone"),
            rs.getString("license_number"),
            rs.getString("license_expiry"),
            rs.getString("vehicle_type"),
            rs.getString("status"),
            rs.getString("photo_path"),
            rs.getString("verified_at")
        );
    }

    // =========================================================
    //  PRIVATE — Convert "MM/DD/YYYY" → "YYYY-MM-DD" for SQL
    // =========================================================
    private String convertDateToSql(String display) {
        try {
            String[] parts = display.split("/");
            return parts[2] + "-" + parts[0] + "-" + parts[1];
        } catch (Exception e) {
            System.err.println("[DriverDAO] WARN: Could not parse date '" + display + "' — using raw.");
            return display;
        }
    }
}
