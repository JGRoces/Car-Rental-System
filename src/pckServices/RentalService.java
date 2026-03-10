package pckServices;

import pckDatabase.CarDAO;
import pckDatabase.PaymentDAO;
import pckDatabase.RentalDAO;
import pckModels.Car;
import pckModels.Payment;
import pckModels.Rental;
import pckUtils.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RentalService {

    private static final RentalDAO  rentalDAO  = new RentalDAO();
    private static final CarDAO     carDAO     = new CarDAO();
    private static final PaymentDAO paymentDAO = new PaymentDAO();

    // ── CREATE ────────────────────────────────────────────────

    /**
     * Books a rental. Calculates total from daily rate × days,
     * inserts the rental, and creates a PENDING payment record.
     * Returns true on success.
     */
    public static boolean bookRental(int customerId, int carId,
                                     LocalDate start, LocalDate end,
                                     BigDecimal dailyRate) {
        if (customerId <= 0 || carId <= 0 || start == null || end == null) return false;

        long days = DateUtil.calculateDays(start, end);
        if (days <= 0) days = 1;

        Rental rental = new Rental(customerId, carId, start, end);
        rental.setTotalAmount(dailyRate.multiply(BigDecimal.valueOf(days)));

        boolean ok = rentalDAO.createRental(rental);
        if (ok && rental.getRentalId() > 0) {
            paymentDAO.insertPayment(new Payment(rental.getRentalId(),
                rental.getTotalAmount(), "CASH"));
        }
        return ok;
    }

    // ── READ ──────────────────────────────────────────────────

    public static List<Rental> getAllRentals()                        { return rentalDAO.getAllRentals();              }
    public static List<Rental> getCustomerRentals(int customerId)    { return rentalDAO.getRentalsByCustomerId(customerId); }
    public static List<Rental> getRecentRentals(int limit)           { return rentalDAO.getRecentRentals(limit);     }
    public static Rental       getRentalById(int rentalId)           { return rentalDAO.getRentalById(rentalId);     }

    // ── UPDATE ────────────────────────────────────────────────

    public static boolean updateRentalStatus(int rentalId, String status) { return rentalDAO.updateStatus(rentalId, status); }
    public static boolean activateRental(int rentalId)                    { return rentalDAO.updateStatus(rentalId, "ACTIVE");    }
    public static boolean completeRental(int rentalId)                    { return rentalDAO.updateStatus(rentalId, "COMPLETED"); }
    public static boolean cancelRental(int rentalId)                      { return rentalDAO.updateStatus(rentalId, "CANCELLED"); }

    // ── STATS ─────────────────────────────────────────────────

    public static int        getActiveCount()   { return rentalDAO.getActiveCount();   }
    public static int        getPendingCount()  { return rentalDAO.getPendingCount();  }
    public static BigDecimal getTotalRevenue()  { return rentalDAO.getTotalRevenue();  }

    // ── PAYMENTS ──────────────────────────────────────────────

    public static List<Payment> getAllPayments()       { return paymentDAO.getAllPayments();       }
    public static List<Payment> getPendingPayments()   { return paymentDAO.getPendingPayments();   }
    public static boolean       approvePayment(int id) { return paymentDAO.approvePayment(id);     }
    public static boolean       refundPayment(int id)  { return paymentDAO.refundPayment(id);      }
    public static BigDecimal    getPaymentRevenue()    { return paymentDAO.getTotalRevenue();      }
}
