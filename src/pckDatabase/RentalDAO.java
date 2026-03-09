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

    /**
     * Maps a single row from the ResultSet to a Rental object.
     */
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

    /**
     * Fetches all rentals for a specific customer.
     * Essential for the MyRentalsGUI.
     */
    public List<Rental> getRentalsByCustomerId(int customerId) {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT * FROM rentals WHERE customer_id = ? ORDER BY start_date DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                rentals.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error fetching rentals for customer: " + e.getMessage());
        }
        return rentals;
    }

    /**
     * Inserts a new rental record into the database.
     * Used when a customer completes the reservation process.
     */
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
                // Set the generated ID back to the object
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        rental.setRentalId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[RentalDAO] Error creating rental: " + e.getMessage());
        }
        return false;
    }

    /**
     * Updates the status of an existing rental (e.g., from PENDING to ACTIVE).
     */
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
}