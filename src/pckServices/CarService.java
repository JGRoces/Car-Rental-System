package pckServices;

import java.math.BigDecimal;
import java.util.List;

import pckDatabase.CarDAO;
import pckModels.Car;

/**
 * CarService.java
 * Business logic layer for car-related operations.
 * All GUI classes talk to this — never directly to CarDAO.
 *
 * Architecture: GUI → CarService → CarDAO → Database
 */
public class CarService {

    private static final CarDAO carDAO = new CarDAO();

    // ====================================================
    //  READ
    // ====================================================

    /** Returns all cars in the inventory */
    public static List<Car> getAllCars() {
        return carDAO.getAllCars();
    }

    /** Returns only cars with status AVAILABLE */
    public static List<Car> getAvailableCars() {
        return carDAO.getAvailableCars();
    }

    /** Returns a single car by ID, or null if not found */
    public static Car getCarById(int carId) {
        return carDAO.getCarById(carId);
    }

    /** Returns all cars matching a given category */
    public static List<Car> getCarsByCategory(String category) {
        return carDAO.getCarsByCategory(category);
    }

    /** Returns all cars matching a given status */
    public static List<Car> getCarsByStatus(String status) {
        return carDAO.getCarsByStatus(status);
    }

    /** Returns distinct brand names from the cars table — used for dynamic dropdown */
    public static List<String> getDistinctBrands() {
        return carDAO.getDistinctBrands();
    }

    /** Returns distinct color names from the cars table — used for dynamic dropdown */
    public static List<String> getDistinctColors() {
        return carDAO.getDistinctColors();
    }

    // ====================================================
    //  CREATE
    // ====================================================

    /**
     * Adds a new car to the inventory.
     * Returns the generated car_id on success, -1 on failure.
     */
    public static int addCar(Car car) {
        if (isBlank(car.getBrand()))       { System.err.println("[CarService] Brand is required.");         return -1; }
        if (isBlank(car.getModel()))       { System.err.println("[CarService] Model is required.");         return -1; }
        if (car.getYear() < 1900)          { System.err.println("[CarService] Invalid year.");              return -1; }
        if (isBlank(car.getPlateNumber())) { System.err.println("[CarService] Plate number is required.");  return -1; }
        if (isBlank(car.getCategory()))    { System.err.println("[CarService] Category is required.");      return -1; }
        if (isBlank(car.getTransmission())){ System.err.println("[CarService] Transmission is required.");  return -1; }
        if (car.getSeatCapacity() <= 0)    { System.err.println("[CarService] Seat capacity must be > 0."); return -1; }
        if (car.getDailyRate() == null || car.getDailyRate().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("[CarService] Daily rate must be greater than 0.");
            return -1;
        }
        if (carDAO.plateExists(car.getPlateNumber())) {
            System.err.println("[CarService] Plate number '" + car.getPlateNumber() + "' already exists.");
            return -1;
        }
        return carDAO.insertCar(car);
    }

    // ====================================================
    //  UPDATE
    // ====================================================

    /**
     * Updates all fields of an existing car.
     * Returns true on success, false on failure.
     */
    public static boolean updateCar(Car car) {
        if (car.getCarId() <= 0)           { System.err.println("[CarService] Invalid car ID.");            return false; }
        if (isBlank(car.getBrand()))       { System.err.println("[CarService] Brand is required.");         return false; }
        if (isBlank(car.getModel()))       { System.err.println("[CarService] Model is required.");         return false; }
        if (car.getYear() < 1900)          { System.err.println("[CarService] Invalid year.");              return false; }
        if (isBlank(car.getPlateNumber())) { System.err.println("[CarService] Plate number is required.");  return false; }
        if (isBlank(car.getCategory()))    { System.err.println("[CarService] Category is required.");      return false; }
        if (isBlank(car.getTransmission())){ System.err.println("[CarService] Transmission is required.");  return false; }
        if (car.getSeatCapacity() <= 0)    { System.err.println("[CarService] Seat capacity must be > 0."); return false; }
        if (car.getDailyRate() == null || car.getDailyRate().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("[CarService] Daily rate must be greater than 0.");
            return false;
        }
        return carDAO.updateCar(car);
    }

    /**
     * Updates only the status of a car.
     * Valid values: AVAILABLE, RENTED, MAINTENANCE
     */
    public static boolean updateCarStatus(int carId, String status) {
        if (carId <= 0) {
            System.err.println("[CarService] Invalid car ID.");
            return false;
        }
        if (!status.equals("AVAILABLE") && !status.equals("RENTED") && !status.equals("MAINTENANCE")) {
            System.err.println("[CarService] Invalid status: " + status);
            return false;
        }
        return carDAO.updateCarStatus(carId, status);
    }

    // ====================================================
    //  DELETE
    // ====================================================

    /**
     * Deletes a car from the inventory.
     * Returns true on success.
     * Will fail if the car has existing rentals (FK constraint).
     */
    public static boolean deleteCar(int carId) {
        if (carId <= 0) {
            System.err.println("[CarService] Invalid car ID.");
            return false;
        }
        return carDAO.deleteCar(carId);
    }

    // ====================================================
    //  PRIVATE HELPERS
    // ====================================================
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}