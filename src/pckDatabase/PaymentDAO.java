package pckDatabase;

import pckModels.Payment;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PaymentDAO.java
 * All database operations for the payments table.
 * Used by PaymentService.java
 */
public class PaymentDAO {

    // =========================================================
    //  CREATE — Insert a new payment record
    //  Returns the generated payment_id, or -1 on failure.
    // =========================================================
    public int insertPayment(Payment payment) {
        String sql = """
            INSERT INTO payments (rental_id, amount_paid, payment_method, status)
            VALUES (?, ?, ?, 'PENDING')
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setInt(1, payment.getRentalId());
            s.setBigDecimal(2, payment.getAmountPaid());
            s.setString(3, payment.getPaymentMethod());
            s.executeUpdate();
            ResultSet keys = s.getGeneratedKeys();
            if (keys.next()) {
                int id = keys.getInt(1);
                System.out.println("[PaymentDAO] insertPayment() → payment_id = " + id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in insertPayment(): " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    // =========================================================
    //  READ — Get all payments
    // =========================================================
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in getAllPayments(): " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    //  READ — Get payments by rental_id
    // =========================================================
    public List<Payment> getPaymentsByRental(int rentalId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE rental_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setInt(1, rentalId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in getPaymentsByRental(): " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    //  READ — Get pending payments
    // =========================================================
    public List<Payment> getPendingPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE status = 'PENDING' ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in getPendingPayments(): " + e.getMessage());
        }
        return list;
    }

    // =========================================================
    //  UPDATE — Approve a payment (set status to PAID)
    // =========================================================
    public boolean approvePayment(int paymentId) {
        return updateStatus(paymentId, "PAID");
    }

    // =========================================================
    //  UPDATE — Refund a payment
    // =========================================================
    public boolean refundPayment(int paymentId) {
        return updateStatus(paymentId, "REFUNDED");
    }

    private boolean updateStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, status);
            s.setInt(2, paymentId);
            int rows = s.executeUpdate();
            System.out.println("[PaymentDAO] updateStatus(id=" + paymentId + ", status=" + status + ") → " + rows + " row(s)");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in updateStatus(): " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // =========================================================
    //  STATS
    // =========================================================
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount_paid), 0) FROM payments WHERE status = 'PAID'";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement s = conn.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            System.err.println("[PaymentDAO] ERROR in getTotalRevenue(): " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    // =========================================================
    //  PRIVATE — Map ResultSet row to Payment object
    // =========================================================
    private Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("payment_date");
        return new Payment(
            rs.getInt("payment_id"),
            rs.getInt("rental_id"),
            rs.getBigDecimal("amount_paid"),
            ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
            rs.getString("payment_method"),
            rs.getString("status")
        );
    }
}
