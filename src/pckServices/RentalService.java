package pckServices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pckDatabase.PaymentDAO;
import pckDatabase.RentalDAO;
import pckModels.Payment;
import pckModels.Rental;
import pckUtils.DateUtil;

public class RentalService {

    private static final RentalDAO  rentalDAO  = new RentalDAO();
    private static final PaymentDAO paymentDAO = new PaymentDAO();

    public static List<Rental> getCustomerRentals(int customerId) {
        return rentalDAO.getRentalsByCustomerId(customerId);
    }

    public static boolean bookRental(int customerId, int carId, LocalDate start, LocalDate end, BigDecimal dailyRate) {
        if (!rentalDAO.isCarAvailableForDates(carId, start, end)) {
            System.err.println("[RentalService] Car " + carId + " is not available for " + start + " to " + end);
            return false;
        }
        Rental rental = new Rental(customerId, carId, start, end);
        long days = DateUtil.calculateDays(start, end);
        if (days <= 0) days = 1;
        rental.setTotalAmount(dailyRate.multiply(BigDecimal.valueOf(days)));
        return rentalDAO.createRental(rental);
    }

    public static boolean updateRentalStatus(int rentalId, String status) {
        return rentalDAO.updateStatus(rentalId, status);
    }

    public static boolean cancelRental(int rentalId) {
        return rentalDAO.updateStatus(rentalId, "CANCELLED");
    }

    public static boolean completeRental(int rentalId) {
        return rentalDAO.updateStatus(rentalId, "COMPLETED");
    }

    public static List<Rental> getAllRentals() {
        return rentalDAO.getAllRentals();
    }

    public static List<Rental> getRecentRentals(int limit) {
        return rentalDAO.getRecentRentals(limit);
    }

    public static int        getActiveCount()  { return rentalDAO.getActiveCount();  }
    public static int        getPendingCount() { return rentalDAO.getPendingCount(); }
    public static BigDecimal getTotalRevenue() { return rentalDAO.getTotalRevenue(); }

    public static List<Payment> getAllPayments()       { return paymentDAO.getAllPayments();       }
    public static List<Payment> getPendingPayments()   { return paymentDAO.getPendingPayments();   }
    public static boolean       approvePayment(int id) { return paymentDAO.approvePayment(id);     }
}
