package pckUtils;

/**
 * Constants.java
 * App-wide constants shared across all layers.
 * Never instantiate — reference fields directly via Constants.FIELD_NAME.
 */
public class ConstantsUtil {

    // Private constructor — prevent instantiation
    private Constants() {}

    // -------------------------
    // Application Info
    // -------------------------
    public static final String APP_NAME        = "CarRentals";
    public static final String APP_VERSION     = "1.0.0";

    // -------------------------
    // Database
    // -------------------------
    public static final String DB_NAME         = "car_rental_db";
    public static final String DB_URL          = "jdbc:mysql://localhost:3306/" + DB_NAME;
    public static final String DB_USER         = "carrentaluser";   // replace before push
    public static final String DB_PASS         = "carrentalpass";   // replace before push

    // -------------------------
    // Date Formats
    // -------------------------
    public static final String DATE_FORMAT_DB      = "yyyy-MM-dd";         // MySQL format
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";       // GUI display

    // -------------------------
    // Rental Business Rules
    // -------------------------
    public static final int    MIN_RENTAL_DAYS     = 1;
    public static final int    MAX_RENTAL_DAYS     = 90;
    public static final double LATE_FEE_RATE       = 1.5;   // multiplier on daily rate
    public static final double TAX_RATE            = 0.12;  // 12% tax on total amount

    // -------------------------
    // Car Categories
    // -------------------------
    public static final String[] CAR_CATEGORIES = {
        "Sedan", "SUV", "Van", "Truck"
    };

    // -------------------------
    // Status Values
    // -------------------------
    public static final String STATUS_AVAILABLE   = "AVAILABLE";
    public static final String STATUS_RENTED      = "RENTED";
    public static final String STATUS_MAINTENANCE = "MAINTENANCE";

    public static final String RENTAL_PENDING    = "PENDING";
    public static final String RENTAL_ACTIVE     = "ACTIVE";
    public static final String RENTAL_COMPLETED  = "COMPLETED";
    public static final String RENTAL_CANCELLED  = "CANCELLED";

    public static final String PAYMENT_PAID      = "PAID";
    public static final String PAYMENT_PENDING   = "PENDING";
    public static final String PAYMENT_REFUNDED  = "REFUNDED";

    // -------------------------
    // Payment Methods
    // -------------------------
    public static final String[] PAYMENT_METHODS = {
        "CASH", "CARD", "ONLINE"
    };

    // -------------------------
    // User Roles
    // -------------------------
    public static final String ROLE_ADMIN    = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    // -------------------------
    // UI / Window Sizes
    // -------------------------
    public static final int LOGIN_WIDTH      = 1200;
    public static final int LOGIN_HEIGHT     = 800;
    public static final int DASHBOARD_WIDTH  = 1600;
    public static final int DASHBOARD_HEIGHT = 900;
    public static final int MIN_WIDTH        = 1024;
    public static final int MIN_HEIGHT       = 600;
}