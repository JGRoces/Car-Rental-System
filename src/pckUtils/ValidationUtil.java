package pckUtils;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * ValidationUtil.java
 * Utility class for validating user input across the application.
 * Never instantiate — use static methods only.
 */
public class ValidationUtil {

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Private constructor — prevent instantiation
    private ValidationUtil() {}

    /**
     * Checks if a string is null or blank.
     * @param value the string to check
     * @return true if empty or null
     */
    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks if multiple fields are all non-empty.
     * @param fields varargs of string inputs
     * @return true if ALL fields are filled
     */
    public static boolean areFieldsFilled(String... fields) {
        for (String field : fields) {
            if (isEmpty(field)) return false;
        }
        return true;
    }

    /**
     * Validates email format.
     * @param email the email string to validate
     * @return true if format is valid
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates a phone number (accepts digits, spaces, dashes, plus sign).
     * Minimum 7 digits, maximum 15 digits.
     * @param phone the phone number string
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        String digits = phone.replaceAll("[\\s\\-+]", "");
        return digits.matches("\\d{7,15}");
    }

    /**
     * Validates that a date range is logical:
     * - startDate must not be null
     * - endDate must not be null
     * - endDate must be after startDate
     * @param startDate the start of the rental
     * @param endDate   the end of the rental
     * @return true if the date range is valid
     */
    public static boolean isValidDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return false;
        return endDate.isAfter(startDate);
    }

    /**
     * Validates that a start date is not in the past.
     * @param startDate the proposed start date
     * @return true if startDate is today or in the future
     */
    public static boolean isStartDateValid(LocalDate startDate) {
        if (startDate == null) return false;
        return !startDate.isBefore(LocalDate.now());
    }

    /**
     * Validates a license number — must be alphanumeric, 5–20 characters.
     * @param license the license number string
     * @return true if valid
     */
    public static boolean isValidLicense(String license) {
        if (isEmpty(license)) return false;
        return license.trim().matches("[A-Za-z0-9\\-]{5,20}");
    }

    /**
     * Validates a plate number — alphanumeric, allows dashes, 4–10 characters.
     * @param plate the plate number string
     * @return true if valid
     */
    public static boolean isValidPlateNumber(String plate) {
        if (isEmpty(plate)) return false;
        return plate.trim().matches("[A-Za-z0-9\\-]{4,10}");
    }

    /**
     * Checks if a numeric string represents a positive number.
     * Useful for validating daily rates, amounts, etc.
     * @param value the string to check
     * @return true if it parses to a positive double
     */
    public static boolean isPositiveNumber(String value) {
        if (isEmpty(value)) return false;
        try {
            return Double.parseDouble(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}