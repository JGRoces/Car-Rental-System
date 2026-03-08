package pckModels;

/**
 * Customer.java
 * Extends User with customer-specific profile data.
 * Maps to the customers table joined with users.
 *
 * users       → userId, fullName, email, password, role
 * customers   → customerId, phone, photoPath
 */
public class Customer extends User {

    private int    customerId;
    private String phone;
    private String photoPath;   // nullable — profile photo file path

    // -------------------------
    // Constructor — new account (no IDs yet, DB generates them)
    // -------------------------
    public Customer(String fullName, String email, String password,
                    String phone, String photoPath) {
        super(fullName, email, password, "CUSTOMER");
        this.phone     = phone;
        this.photoPath = photoPath;
    }

    // -------------------------
    // Constructor — loaded from database (all IDs present)
    // -------------------------
    public Customer(int userId, int customerId,
                    String fullName, String email, String password,
                    String phone, String photoPath) {
        super(userId, fullName, email, password, "CUSTOMER");
        this.customerId = customerId;
        this.phone      = phone;
        this.photoPath  = photoPath;
    }

    // -------------------------
    // Getters
    // -------------------------
    public int    getCustomerId() { return customerId; }
    public String getPhone()      { return phone;      }
    public String getPhotoPath()  { return photoPath;  }

    // -------------------------
    // Setters
    // -------------------------
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setPhone(String phone)        { this.phone      = phone;      }
    public void setPhotoPath(String path)     { this.photoPath  = path;       }

    @Override
    public String toString() {
        return "Customer{" +
               "customerId=" + customerId +
               ", userId="   + getUserId() +
               ", name='"    + getFullName() + '\'' +
               ", email='"   + getEmail()    + '\'' +
               ", phone='"   + phone         + '\'' +
               '}';
    }
}
