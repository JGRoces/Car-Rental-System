package pckDatabase;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import pckModels.Rental;

public class RentalDAO {

    // =========================================================
    //  PRIVATE — Map a ResultSet row to a Rental object
    // =========================================================
    private Rental mapRow(ResultSet rs) throws SQLException {
        return new Rental(
            rs.getInt("rental_id"),
            rs.getInt("customer_id"),
            rs.getInt("car_id"),
            rs.getInt("driver_id"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getBigDecimal("total_amount"),
            rs.getString("status")
        );
    }

    // =========================================================
    //  READ — Get ALL rentals (used by RentalsTab in AdminDashboard)
    //  Ordered by most recent start date first.
    // =========================================================
    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) rentals.add(mapRow(rs));
            System.out.println("[RentalDAO] getAllRentals() → " + rentals.size() + " rows");
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getAllRentals(): " + e.getMessage());
        }
        return rentals;
    }

    // =========================================================
    //  CHECK — Does this customer have an ACTIVE or PENDING rental?
    //  Used by CustomersTab to render the active rental indicator dot.
    // =========================================================
    public boolean hasActiveRental(int customerId) {
        String sql = "SELECT 1 FROM rentals WHERE customer_id = ? AND status IN ('ACTIVE','PENDING') LIMIT 1";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            return pstmt.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in hasActiveRental(): " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    //  READ — Get all rentals for a specific customer
    // =========================================================
    public List<Rental> getRentalsByCustomerId(int customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ? ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) rentals.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error fetching rentals for customer: " + e.getMessage());
        }
        return rentals;
    }

    // =========================================================
    //  CREATE — Insert a new rental record
    // =========================================================
    public boolean createRental(Rental rental) {
        String sql = "INSERT INTO rentals (customer_id, car_id, start_date, end_date, total_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, rental.getCustomerId());
            pstmt.setInt(2, rental.getCarId());
            pstmt.setDate(3, Date.valueOf(rental.getStartDate()));
            pstmt.setDate(4, Date.valueOf(rental.getEndDate()));
            pstmt.setBigDecimal(5, rental.getTotalAmount());
            pstmt.setString(6, rental.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) rental.setRentalId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error creating rental: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    //  UPDATE — Change rental status
    // =========================================================
    public boolean updateStatus(int rentalId, String newStatus) {
        String sql = "UPDATE rentals SET status = ? WHERE rental_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, rentalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error updating rental status: " + e.getMessage());
            return false;
        }
    }

    public boolean assignDriver(int rentalId, int driverId) {
        String sql = "UPDATE rentals SET driver_id = ? WHERE rental_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, driverId);
            pstmt.setInt(2, rentalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error assigning driver: " + e.getMessage());
            return false;
        }
    }

    public boolean updateDatesAndStatus(int rentalId, java.time.LocalDate startDate,
            java.time.LocalDate endDate, java.math.BigDecimal totalAmount, String status) {
        String sql = "UPDATE rentals SET start_date=?, end_date=?, total_amount=?, status=? WHERE rental_id=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            pstmt.setBigDecimal(3, totalAmount);
            pstmt.setString(4, status);
            pstmt.setInt(5, rentalId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in updateDatesAndStatus: " + e.getMessage());
            return false;
        }
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

    public int getActiveCount() {
        return countByStatus("ACTIVE");
    }

    public int getPendingCount() {
        return countByStatus("PENDING");
    }

    public java.math.BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM rentals WHERE status = 'COMPLETED'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error in getTotalRevenue: " + e.getMessage());
        }
        return java.math.BigDecimal.ZERO;
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
