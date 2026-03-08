package pckServices;

import pckDatabase.DriverDAO;
import pckModels.Driver;

/**
 * DriverService.java
 * Business logic for driver account registration and verification.
 *
 * Registration flow:
 *   DriverSignUpGUI → DriverService.register() → DriverDAO → DB
 *
 * Verification flow (Step 6 — AdminGUI):
 *   AdminDashboardGUI → DriverService.verify() / reject() → DriverDAO → DB
 */
public class DriverService {

    private static final DriverDAO driverDAO = new DriverDAO();

    private DriverService() {}

    // =========================================================
    //  Result wrapper
    // =========================================================
    public enum RegisterResult {
        SUCCESS,
        EMAIL_TAKEN,
        LICENSE_TAKEN,
        INVALID_EMAIL,
        PASSWORD_TOO_SHORT,
        PASSWORDS_DO_NOT_MATCH,
        MISSING_FIELDS,
        DATABASE_ERROR
    }

    // =========================================================
    //  REGISTER — validate and create a new driver account
    //
    //  Usage in DriverSignUpGUI:
    //    RegisterResult result = DriverService.register(
    //        name, email, phone, password, confirmPassword,
    //        licenseNumber, licenseExpiry, vehicleType, photoPath);
    // =========================================================
    public static RegisterResult register(String fullName, String email,
                                          String phone, String password,
                                          String confirmPassword,
                                          String licenseNumber, String licenseExpiry,
                                          String vehicleType, String photoPath) {
        // 1. Required fields
        if (isBlank(fullName) || isBlank(email) || isBlank(password)
                || isBlank(confirmPassword) || isBlank(licenseNumber) || isBlank(licenseExpiry))
            return RegisterResult.MISSING_FIELDS;

        // 2. Email format
        if (!email.contains("@") || !email.contains("."))
            return RegisterResult.INVALID_EMAIL;

        // 3. Password length
        if (password.length() < 8)
            return RegisterResult.PASSWORD_TOO_SHORT;

        // 4. Passwords match
        if (!password.equals(confirmPassword))
            return RegisterResult.PASSWORDS_DO_NOT_MATCH;

        // 5. Duplicate checks
        if (driverDAO.emailExists(email))
            return RegisterResult.EMAIL_TAKEN;

        if (driverDAO.licenseExists(licenseNumber))
            return RegisterResult.LICENSE_TAKEN;

        // 6. Create account — always PENDING
        Driver driver = new Driver(fullName, email, password,
                                   phone.isBlank() ? null : phone,
                                   licenseNumber, licenseExpiry,
                                   vehicleType, photoPath);
        Driver created = driverDAO.createAccount(driver);

        return created != null ? RegisterResult.SUCCESS : RegisterResult.DATABASE_ERROR;
    }

    // =========================================================
    //  VERIFY — Admin approves a driver
    //  Returns true if update succeeded.
    // =========================================================
    public static boolean verify(int driverId) {
        boolean ok = driverDAO.updateStatus(driverId, Driver.STATUS_VERIFIED);
        System.out.println("[DriverService] Driver " + driverId
            + (ok ? " VERIFIED." : " verification FAILED."));
        return ok;
    }

    // =========================================================
    //  REJECT — Admin rejects a driver
    // =========================================================
    public static boolean reject(int driverId) {
        boolean ok = driverDAO.updateStatus(driverId, Driver.STATUS_REJECTED);
        System.out.println("[DriverService] Driver " + driverId
            + (ok ? " REJECTED." : " rejection FAILED."));
        return ok;
    }

    // =========================================================
    //  Friendly message for each result
    // =========================================================
    public static String getMessage(RegisterResult result) {
        return switch (result) {
            case SUCCESS                -> "Account created! Awaiting admin verification.";
            case EMAIL_TAKEN            -> "This email is already registered.";
            case LICENSE_TAKEN          -> "This license number is already registered.";
            case INVALID_EMAIL          -> "Please enter a valid email address.";
            case PASSWORD_TOO_SHORT     -> "Password must be at least 8 characters.";
            case PASSWORDS_DO_NOT_MATCH -> "Passwords do not match.";
            case MISSING_FIELDS         -> "Please fill in all required fields.";
            case DATABASE_ERROR         -> "Something went wrong. Please try again.";
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
