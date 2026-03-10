package pckDatabase;

import pckModels.Driver;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DriverDAO {

    public Driver createAccount(Driver driver) {
        String insertUser   = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, 'DRIVER')";
        String insertDriver = """
            INSERT INTO drivers
                (user_id, phone, license_number, license_expiry, vehicle_type, status, photo_path)
            VALUES (?, ?, ?, ?, ?, 'PENDING', ?)
            """;
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            int userId;
            try (PreparedStatement stmt = connection.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, driver.getFullName());
                stmt.setString(2, driver.getEmail());
                stmt.setString(3, driver.getPassword());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No user_id generated.");
                userId = keys.getInt(1);
                driver.setUserId(userId);
            }
            int driverId;
            try (PreparedStatement stmt = connection.prepareStatement(insertDriver, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, userId);
                stmt.setString(2, driver.getPhone());
                stmt.setString(3, driver.getLicenseNumber());
                stmt.setString(4, convertDateToSql(driver.getLicenseExpiry()));
                stmt.setString(5, driver.getVehicleType());
                stmt.setString(6, driver.getPhotoPath());
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("No driver_id generated.");
                driverId = keys.getInt(1);
                driver.setDriverId(driverId);
            }
            connection.commit();
            System.out.println("[DriverDAO] Account created — userId=" + userId + ", driverId=" + driverId + " | Status: PENDING");
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

    public List<Driver> getPendingDrivers() {
        return getDriversByStatus(Driver.STATUS_PENDING);
    }

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
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: getDriversByStatus failed.");
            e.printStackTrace();
        }
        return list;
    }

    public Driver getDriverByUserId(int userId) {
        String query = """
            SELECT u.user_id, d.driver_id, u.full_name, u.email, u.password,
                   d.phone, d.license_number, d.license_expiry, d.vehicle_type,
                   d.status, d.photo_path, d.verified_at
            FROM users u
            JOIN drivers d ON u.user_id = d.user_id
            WHERE u.user_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: getDriverByUserId failed.");
            e.printStackTrace();
        }
        return null;
    }

    public Driver getDriverById(int driverId) {
        String query = """
            SELECT u.user_id, d.driver_id, u.full_name, u.email, u.password,
                   d.phone, d.license_number, d.license_expiry, d.vehicle_type,
                   d.status, d.photo_path, d.verified_at
            FROM users u
            JOIN drivers d ON u.user_id = d.user_id
            WHERE d.driver_id = ?
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, driverId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: getDriverById failed.");
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(int driverId, String status) {
        String query = status.equals(Driver.STATUS_VERIFIED)
            ? "UPDATE drivers SET status = ?, verified_at = CURRENT_TIMESTAMP WHERE driver_id = ?"
            : "UPDATE drivers SET status = ?, verified_at = NULL WHERE driver_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            stmt.setInt(2, driverId);
            int rows = stmt.executeUpdate();
            System.out.println("[DriverDAO] Driver " + driverId + " status \u2192 " + status);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: updateStatus failed.");
            e.printStackTrace();
        }
        return false;
    }

    public boolean emailExists(String email) {
        String query = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, licenseNumber);
            return stmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[DriverDAO] ERROR: licenseExists check failed.");
            e.printStackTrace();
        }
        return false;
    }

    private Driver mapRow(ResultSet rs) throws SQLException {
        return new Driver(
            rs.getInt("user_id"), rs.getInt("driver_id"),
            rs.getString("full_name"), rs.getString("email"), rs.getString("password"),
            rs.getString("phone"), rs.getString("license_number"), rs.getString("license_expiry"),
            rs.getString("vehicle_type"), rs.getString("status"),
            rs.getString("photo_path"), rs.getString("verified_at")
        );
    }

    private String convertDateToSql(String display) {
        try {
            String[] parts = display.split("/");
            return parts[2] + "-" + parts[0] + "-" + parts[1];
        } catch (Exception e) {
            return display;
        }
    }
}
