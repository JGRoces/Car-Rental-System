package pckDatabase;

import pckModels.Rental;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalDAO {

    private Rental mapRow(ResultSet rs) throws SQLException {
        return new Rental(
            rs.getInt("rental_id"),
            rs.getInt("customer_id"),
            rs.getInt("car_id"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getBigDecimal("total_amount"),
            rs.getString("status")
        );
    }

    // ── CREATE ────────────────────────────────────────────────

    public boolean createRental(Rental rental) {
        String sql = "INSERT INTO rentals (customer_id, car_id, start_date, end_date, total_amount, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, rental.getCustomerId());
            s.setInt(2, rental.getCarId());
            s.setDate(3, Date.valueOf(rental.getStartDate()));
            s.setDate(4, Date.valueOf(rental.getEndDate()));
            s.setBigDecimal(5, rental.getTotalAmount());
            s.setString(6, rental.getStatus());
            if (s.executeUpdate() > 0) {
                ResultSet keys = s.getGeneratedKeys();
                if (keys.next()) rental.setRentalId(keys.getInt(1));
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in createRental: " + e.getMessage());
        }
        return false;
    }

    // ── READ ──────────────────────────────────────────────────

    public List<Rental> getAllRentals() {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getAllRentals: " + e.getMessage());
        }
        return list;
    }

    public List<Rental> getRentalsByCustomerId(int customerId) {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ? ORDER BY start_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, customerId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getRentalsByCustomerId: " + e.getMessage());
        }
        return list;
    }

    public List<Rental> getRecentRentals(int limit) {
        List<Rental> list = new ArrayList<>();
        String sql = "SELECT * FROM rentals ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, limit);
            ResultSet rs = s.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getRecentRentals: " + e.getMessage());
        }
        return list;
    }

    public Rental getRentalById(int rentalId) {
        String sql = "SELECT * FROM rentals WHERE rental_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, rentalId);
            ResultSet rs = s.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getRentalById: " + e.getMessage());
        }
        return null;
    }

    // ── UPDATE ────────────────────────────────────────────────

    public boolean updateStatus(int rentalId, String newStatus) {
        String sql = "UPDATE rentals SET status = ? WHERE rental_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, newStatus);
            s.setInt(2, rentalId);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in updateStatus: " + e.getMessage());
        }
        return false;
    }

    // ── STATS ─────────────────────────────────────────────────

    public int getActiveCount() {
        return countByStatus("ACTIVE");
    }

    public int getPendingCount() {
        return countByStatus("PENDING");
    }

    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM rentals WHERE status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getTotalRevenue: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE status = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, status);
            ResultSet rs = s.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in countByStatus: " + e.getMessage());
        }
        return 0;
    }
}
