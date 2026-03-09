package pckServices;

import pckDatabase.CustomerDAO;
import pckModels.Customer;

/**
 * CustomerService.java
 * Business logic for customer account registration.
 *
 * Flow:
 *   CustomerSignUpGUI → CustomerService.register() → CustomerDAO → DB
 *
 * Validates input, checks for duplicates, then delegates to DAO.
 */
public class CustomerService {

    private static final CustomerDAO customerDAO = new CustomerDAO();

    private CustomerService() {}

    // =========================================================
    //  Result wrapper — tells the GUI exactly what went wrong
    // =========================================================
    public enum RegisterResult {
        SUCCESS,
        EMAIL_TAKEN,
        INVALID_EMAIL,
        PASSWORD_TOO_SHORT,
        PASSWORDS_DO_NOT_MATCH,
        MISSING_FIELDS,
        DATABASE_ERROR
    }

    // =========================================================
    //  REGISTER — validate and create a new customer account
    //
    //  Usage in CustomerSignUpGUI:
    //    RegisterResult result = CustomerService.register(
    //        name, email, phone, password, confirmPassword, photoPath);
    //    if (result == RegisterResult.SUCCESS) { ... }
    // =========================================================
    public static RegisterResult register(String fullName, String email,
                                          String phone, String password,
                                          String confirmPassword, String photoPath) {
        // 1. Required fields
        if (isBlank(fullName) || isBlank(email) || isBlank(password) || isBlank(confirmPassword))
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

        // 5. Email duplicate check
        if (customerDAO.emailExists(email))
            return RegisterResult.EMAIL_TAKEN;

        // 6. Create account
        Customer customer = new Customer(fullName, email, password,
                                         phone.isBlank() ? null : phone,
                                         photoPath);
        Customer created = customerDAO.createAccount(customer);

        return created != null ? RegisterResult.SUCCESS : RegisterResult.DATABASE_ERROR;
    }

    // =========================================================
    //  Friendly message for each result — use in status label
    // =========================================================
    public static String getMessage(RegisterResult result) {
        return switch (result) {
            case SUCCESS               -> "Account created successfully!";
            case EMAIL_TAKEN           -> "This email is already registered.";
            case INVALID_EMAIL         -> "Please enter a valid email address.";
            case PASSWORD_TOO_SHORT    -> "Password must be at least 8 characters.";
            case PASSWORDS_DO_NOT_MATCH-> "Passwords do not match.";
            case MISSING_FIELDS        -> "Please fill in all required fields.";
            case DATABASE_ERROR        -> "Something went wrong. Please try again.";
        };
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
