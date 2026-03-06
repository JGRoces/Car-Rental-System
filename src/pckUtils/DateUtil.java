package pckUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * DateUtil.java
 * Utility class for date formatting and calculations.
 * Never instantiate — use static methods only.
 */
public class DateUtil {

    // Standard display format used across all GUI screens
    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DISPLAY);

    // Format used when storing/reading from the database
    private static final DateTimeFormatter DB_FORMAT =
        DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_DB);

    // Private constructor — prevent instantiation
    private DateUtil() {}

    /**
     * Formats a LocalDate for display in the GUI (e.g. "Jan 15, 2025").
     * @param date the LocalDate to format
     * @return formatted string, or empty string if null
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMAT);
    }

    /**
     * Formats a LocalDate for MySQL storage (e.g. "2025-01-15").
     * @param date the LocalDate to format
     * @return formatted string, or empty string if null
     */
    public static String formatForDB(LocalDate date) {
        if (date == null) return "";
        return date.format(DB_FORMAT);
    }

    /**
     * Parses a DB-formatted date string (yyyy-MM-dd) into a LocalDate.
     * @param dateStr the date string from the database
     * @return LocalDate, or null if parsing fails
     */
    public static LocalDate parseFromDB(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return LocalDate.parse(dateStr.trim(), DB_FORMAT);
        } catch (DateTimeParseException e) {
            System.err.println("[DateUtil] Failed to parse date: " + dateStr);
            return null;
        }
    }

    /**
     * Calculates the number of rental days between two dates.
     * A same-day rental counts as 1 day minimum.
     * @param startDate the rental start date
     * @param endDate   the rental end date
     * @return number of days (minimum 1), or 0 if dates are invalid
     */
    public static long calculateDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        if (!endDate.isAfter(startDate)) return 0;
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Returns today's date as a display-formatted string.
     * @return today's date string for display
     */
    public static String todayDisplay() {
        return formatForDisplay(LocalDate.now());
    }

    /**
     * Returns today's date as a DB-formatted string.
     * @return today's date string for DB
     */
    public static String todayDB() {
        return formatForDB(LocalDate.now());
    }
}