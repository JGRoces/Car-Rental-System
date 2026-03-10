package pckUtils;

import java.io.File;

/**
 * AppConfig.java
 * ─────────────────────────────────────────────────────────────
 * Single source of truth for all file system paths in the app.
 *
 * Folder layout on disk:
 *
 *   Documents/GitHub/
 *   ├── Car-Rental-System/          ← project root (this app runs from here)
 *   │   ├── assets/
 *   │   │   ├── images/
 *   │   │   ├── icons/
 *   │   │   │   ├── nav/
 *   │   │   │   └── action/
 *   │   │   └── defaults/
 *   │   │       ├── default-avatar.png
 *   │   │       └── default-vehicle.png
 *   │   └── src/
 *   │
 *   └── car-rental-uploads/         ← uploads root (beside project, NOT in Git)
 *       ├── users/
 *       │   ├── customers/
 *       │   └── drivers/
 *       └── vehicles/
 *           ├── sedan/
 *           ├── suv/
 *           ├── coupe/
 *           ├── hatchback/
 *           ├── van/
 *           ├── truck/
 *           └── motorcycle/
 *
 * Usage:
 *   AppConfig.init();                          // call once in Main.java before any GUI
 *   String path = AppConfig.customerPhoto(7);  // returns real path or default
 *   String path = AppConfig.vehiclePhoto(3, "sedan");
 *
 * The init() method auto-creates every uploads folder if it doesn't exist yet.
 * Groupmates never need to create folders manually — just clone and run.
 */
public class AppConfig {

    // =========================================================
    //  ASSET PATHS  —  inside the project, tracked by Git
    //  Relative to the project root (where the app is launched)
    // =========================================================

    // Branding / login screen images
    public static final String IMG_SHOWCASE       = "assets/images/bydshowcase.jpg";
    public static final String IMG_LOGO           = "assets/images/logo.png";

    // Default fallback media — used when a user or vehicle has no photo
    public static final String DEFAULT_AVATAR     = "assets/defaults/default-avatar.png";
    public static final String DEFAULT_VEHICLE    = "assets/defaults/default-vehicle.png";

    // Nav icons — sidebar (loaded in AdminDashboardGUI)
    public static final String ICON_NAV_OVERVIEW   = "assets/icons/nav/overview.png";
    public static final String ICON_NAV_MANAGEMENT = "assets/icons/nav/management.png";
    public static final String ICON_NAV_PAYMENTS   = "assets/icons/nav/payments.png";
    public static final String ICON_NAV_REPORTS    = "assets/icons/nav/reports.png";
    public static final String ICON_NAV_ACCOUNT    = "assets/icons/nav/account.png";
    public static final String ICON_NAV_SETTINGS   = "assets/icons/nav/settings.png";
    public static final String ICON_NAV_SIGNOUT     = "assets/icons/nav/signout.png";
    public static final String ICON_NAV_SIDEBAR_CLOSE = "assets/icons/nav/sidebar-close.png";
    public static final String ICON_NAV_SIDEBAR_OPEN  = "assets/icons/nav/sidebar-open.png";

    // Action icons — buttons inside panels and dialogs
    public static final String ICON_ADD      = "assets/icons/action/add.png";
    public static final String ICON_EDIT     = "assets/icons/action/edit.png";
    public static final String ICON_DELETE   = "assets/icons/action/delete.png";
    public static final String ICON_VERIFY   = "assets/icons/action/verify.png";
    public static final String ICON_REJECT   = "assets/icons/action/reject.png";
    public static final String ICON_SEARCH   = "assets/icons/action/search.png";
    public static final String ICON_FILTER   = "assets/icons/action/filter.png";
    public static final String ICON_REFRESH  = "assets/icons/action/refresh.png";
    public static final String ICON_EXPORT   = "assets/icons/action/export.png";
    public static final String ICON_UPLOAD   = "assets/icons/action/upload.png";
    public static final String ICON_CALENDAR = "assets/icons/action/calendar.png";
    public static final String ICON_CAR      = "assets/icons/action/car.png";

    // =========================================================
    //  UPLOAD PATHS  —  outside the project, NOT in Git
    //  "../" steps up from Car-Rental-System/ to GitHub/,
    //  then into car-rental-uploads/ beside the project.
    // =========================================================
    public static final String UPLOAD_ROOT      = "../car-rental-uploads/";
    public static final String UPLOAD_CUSTOMERS = UPLOAD_ROOT + "users/customers/";
    public static final String UPLOAD_DRIVERS   = UPLOAD_ROOT + "users/drivers/";
    public static final String UPLOAD_VEHICLES  = UPLOAD_ROOT + "vehicles/";

    // All vehicle type subfolders — must match vehicle_type values in the DB exactly
    private static final String[] VEHICLE_TYPES = {
        "sedan", "suv", "coupe", "hatchback", "van", "truck", "motorcycle"
    };

    // =========================================================
    //  INIT  —  call once in Main.java before any GUI opens
    //  Creates the full uploads folder tree if it doesn't exist.
    //  Safe to call on every launch — createDirs() is a no-op
    //  if the folder already exists.
    // =========================================================
    public static void init() {
        // User photo folders
        createDir(UPLOAD_CUSTOMERS);
        createDir(UPLOAD_DRIVERS);

        // Vehicle photo folders — one subfolder per vehicle type
        for (String type : VEHICLE_TYPES) {
            createDir(UPLOAD_VEHICLES + type + "/");
        }

        System.out.println("[AppConfig] Upload folders ready at: "
            + new File(UPLOAD_ROOT).getAbsolutePath());
    }

    private static void createDir(String relativePath) {
        File dir = new File(relativePath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("[AppConfig] Created: " + dir.getAbsolutePath());
            }
        }
    }

    // =========================================================
    //  PHOTO RESOLVERS
    //  Each method returns the real upload path if the file
    //  exists, or the appropriate default if it doesn't.
    //  GUI code never needs to do null checks — just call these.
    // =========================================================

    /**
     * Returns the profile photo path for a customer.
     * Falls back to default-avatar.png if they haven't uploaded one.
     *
     *   String path = AppConfig.customerPhoto(customer.getId());
     *   AdminUIHelper.buildCircularPhoto(path, size);
     */
    public static String customerPhoto(int customerId) {
        String path = UPLOAD_CUSTOMERS + "customer_" + customerId + ".png";
        return fileExists(path) ? path : DEFAULT_AVATAR;
    }

    /**
     * Returns the profile photo path for a driver.
     * Falls back to default-avatar.png if they haven't uploaded one.
     */
    public static String driverPhoto(int driverId) {
        String path = UPLOAD_DRIVERS + "driver_" + driverId + ".png";
        return fileExists(path) ? path : DEFAULT_AVATAR;
    }

    /**
     * Returns the photo path for a vehicle.
     * vehicleType must match a VEHICLE_TYPES entry exactly (e.g. "sedan").
     * Falls back to default-vehicle.png if no photo has been uploaded.
     *
     *   String path = AppConfig.vehiclePhoto(car.getId(), car.getVehicleType());
     */
    public static String vehiclePhoto(int vehicleId, String vehicleType) {
        String folder = UPLOAD_VEHICLES + vehicleType.toLowerCase().trim() + "/";
        // Check for any supported extension
        for (String ext : new String[]{ "webp", "png", "jpg", "jpeg" }) {
            String path = folder + "vehicle_" + vehicleId + "." + ext;
            if (fileExists(path)) return path;
        }
        return DEFAULT_VEHICLE;
    }

    /**
     * Saves an uploaded photo to the correct location.
     * Called by CustomerSignUpGUI, DriverSignUpGUI, and AddVehicleDialog
     * when the user picks a file from the chooser.
     *
     * Returns the saved path on success, null on failure.
     *
     * Usage:
     *   String saved = AppConfig.saveCustomerPhoto(sourceFile, customer.getId());
     *   if (saved != null) customer.setPhotoPath(saved);
     */
    public static String saveCustomerPhoto(File sourceFile, int customerId) {
        return copyFile(sourceFile, UPLOAD_CUSTOMERS + "customer_" + customerId + ".png");
    }

    public static String saveDriverPhoto(File sourceFile, int driverId) {
        return copyFile(sourceFile, UPLOAD_DRIVERS + "driver_" + driverId + ".png");
    }

    public static String saveVehiclePhoto(File sourceFile, int vehicleId, String vehicleType) {
        String ext  = getExtension(sourceFile.getName());
        String dest = UPLOAD_VEHICLES + vehicleType.toLowerCase().trim()
                    + "/vehicle_" + vehicleId + "." + ext;
        // Remove any old file with a different extension
        for (String old : new String[]{ "webp", "png", "jpg", "jpeg" }) {
            if (!old.equals(ext)) {
                File f = new File(UPLOAD_VEHICLES + vehicleType.toLowerCase().trim()
                    + "/vehicle_" + vehicleId + "." + old);
                if (f.exists() && !f.delete()) {
                    System.err.println("[AppConfig] WARNING: Could not delete old vehicle image: " + f.getAbsolutePath());
                }
            }
        }
        return copyFile(sourceFile, dest);
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1).toLowerCase() : "png";
    }

    // =========================================================
    //  INTERNAL HELPERS
    // =========================================================
    private static boolean fileExists(String relativePath) {
        return new File(relativePath).exists();
    }

    /**
     * Copies a file from source to destination path.
     * Creates parent directories if needed.
     * Returns the destination path on success, null on failure.
     */
    private static String copyFile(File source, String destPath) {
        try {
            File dest = new File(destPath);
            dest.getParentFile().mkdirs();
            java.nio.file.Files.copy(
                source.toPath(),
                dest.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            return destPath;
        } catch (java.io.IOException e) {
            System.err.println("[AppConfig] Failed to copy file from '"
                + source.getAbsolutePath() + "' to '" + destPath + "': " + e.getMessage());
            return null;
        }
    }

    // Prevent instantiation — static utility class only
    private AppConfig() {}
}
