package pckModels;

/**
 * Driver.java
 * Extends User with driver-specific profile and verification data.
 * Maps to the drivers table joined with users.
 *
 * users   → userId, fullName, email, password, role
 * drivers → driverId, phone, licenseNumber, licenseExpiry,
 *            vehicleType, status, photoPath, verifiedAt
 *
 * Status flow:
 *   PENDING  → driver just registered, waiting for admin review
 *   VERIFIED → admin approved, driver can receive booking requests
 *   REJECTED → admin rejected, driver cannot operate
 */
public class Driver extends User {

    // ── Driver status constants ──────────────────────────────
    public static final String STATUS_PENDING  = "PENDING";
    public static final String STATUS_VERIFIED = "VERIFIED";
    public static final String STATUS_REJECTED = "REJECTED";

    private int    driverId;
    private String phone;
    private String licenseNumber;
    private String licenseExpiry;   // stored as String "MM/DD/YYYY" for display
    private String vehicleType;
    private String status;          // PENDING | VERIFIED | REJECTED
    private String photoPath;       // nullable
    private String verifiedAt;      // nullable — timestamp set when verified

    // -------------------------
    // Constructor — new account (no IDs yet)
    // -------------------------
    public Driver(String fullName, String email, String password,
                  String phone, String licenseNumber, String licenseExpiry,
                  String vehicleType, String photoPath) {
        super(fullName, email, password, "DRIVER");
        this.phone          = phone;
        this.licenseNumber  = licenseNumber;
        this.licenseExpiry  = licenseExpiry;
        this.vehicleType    = vehicleType;
        this.status         = STATUS_PENDING;   // always starts as PENDING
        this.photoPath      = photoPath;
        this.verifiedAt     = null;
    }

    // -------------------------
    // Constructor — loaded from database
    // -------------------------
    public Driver(int userId, int driverId,
                  String fullName, String email, String password,
                  String phone, String licenseNumber, String licenseExpiry,
                  String vehicleType, String status,
                  String photoPath, String verifiedAt) {
        super(userId, fullName, email, password, "DRIVER");
        this.driverId       = driverId;
        this.phone          = phone;
        this.licenseNumber  = licenseNumber;
        this.licenseExpiry  = licenseExpiry;
        this.vehicleType    = vehicleType;
        this.status         = status;
        this.photoPath      = photoPath;
        this.verifiedAt     = verifiedAt;
    }

    // -------------------------
    // Getters
    // -------------------------
    public int    getDriverId()       { return driverId;      }
    public String getPhone()          { return phone;         }
    public String getLicenseNumber()  { return licenseNumber; }
    public String getLicenseExpiry()  { return licenseExpiry; }
    public String getVehicleType()    { return vehicleType;   }
    public String getStatus()         { return status;        }
    public String getPhotoPath()      { return photoPath;     }
    public String getVerifiedAt()     { return verifiedAt;    }

    // -------------------------
    // Setters
    // -------------------------
    public void setDriverId(int id)           { this.driverId      = id;     }
    public void setPhone(String phone)        { this.phone         = phone;  }
    public void setStatus(String status)      { this.status        = status; }
    public void setVerifiedAt(String ts)      { this.verifiedAt    = ts;     }
    public void setPhotoPath(String path)     { this.photoPath     = path;   }

    // -------------------------
    // Convenience status checks
    // -------------------------
    public boolean isPending()  { return STATUS_PENDING.equalsIgnoreCase(status);  }
    public boolean isVerified() { return STATUS_VERIFIED.equalsIgnoreCase(status); }
    public boolean isRejected() { return STATUS_REJECTED.equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "Driver{" +
               "driverId="      + driverId      +
               ", userId="      + getUserId()   +
               ", name='"       + getFullName() + '\'' +
               ", license='"    + licenseNumber + '\'' +
               ", status='"     + status        + '\'' +
               '}';
    }
}
