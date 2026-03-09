package pckServices;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import pckDatabase.RentalDAO;
import pckModels.Rental;
import pckUtils.DateUtil;

public class RentalService {

    private static final RentalDAO rentalDAO = new RentalDAO();

    /**
     * Retrieves the rental history for a specific customer.
     * Used by MyRentalsGUI to populate its table.
     */
    public static List<Rental> getCustomerRentals(int customerId) {
        return rentalDAO.getRentalsByCustomerId(customerId);
    }

    /**
     * Logic for creating a new rental.
     * This handles the calculation of the total amount before saving to the DB.
     * * @param customerId The ID of the logged-in user
     * @param carId      The ID of the car being booked
     * @param start      The pick-up date
     * @param end        The return date
     * @param dailyRate  The price per day for the selected car
     * @return true if the booking was successful
     */
    public static boolean bookRental(int customerId, int carId, LocalDate start, LocalDate end, BigDecimal dailyRate) {
        // 1. Create the base rental object
        Rental rental = new Rental(customerId, carId, start, end);

        // 2. Calculate the duration using DateUtil
        long days = DateUtil.calculateDays(start, end);
        
        // Ensure at least 1 day is charged
        if (days <= 0) days = 1;

        // 3. Calculate total amount (Daily Rate * Number of Days)
        BigDecimal total = dailyRate.multiply(BigDecimal.valueOf(days));
        rental.setTotalAmount(total);

        // 4. Persistence via DAO
        return rentalDAO.createRental(rental);
    }

    /**
     * Updates the status of a rental. 
     * Useful for completing a trip or cancelling a pending reservation.
     */
    public static boolean updateRentalStatus(int rentalId, String status) {
        return rentalDAO.updateStatus(rentalId, status);
    }
}