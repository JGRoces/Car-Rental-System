package pckModels;

/**
 * Customer.java
 * Extends User to represent a customer account.
 * Maps to the `customers` table (one-to-one with `users`).
 */
public class Customer extends User {

    private int    customerId;
    private String phoneNumber;
    private String address;
    private String licenseNumber;

    // New customer — before DB insert
    public Customer(String fullName, String email, String password,
                    String phoneNumber, String address, String licenseNumber) {
        super(fullName, email, password, "CUSTOMER");
        this.phoneNumber   = phoneNumber;
        this.address       = address;
        this.licenseNumber = licenseNumber;
    }

    // Loaded from DB — has both userId and customerId
    public Customer(int userId, String fullName, String email, String password,
                    int customerId, String phoneNumber, String address, String licenseNumber) {
        super(userId, fullName, email, password, "CUSTOMER");
        this.customerId    = customerId;
        this.phoneNumber   = phoneNumber;
        this.address       = address;
        this.licenseNumber = licenseNumber;
    }

    public int    getCustomerId()    { return customerId;    }
    public String getPhoneNumber()   { return phoneNumber;   }
    public String getAddress()       { return address;       }
    public String getLicenseNumber() { return licenseNumber; }

    public void setCustomerId(int customerId)      { this.customerId    = customerId;  }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber   = phoneNumber; }
    public void setAddress(String address)         { this.address       = address;     }
    public void setLicenseNumber(String license)   { this.licenseNumber = license;     }

    @Override
    public String toString() {
        return "Customer{customerId=" + customerId + ", userId=" + getUserId() +
               ", fullName='" + getFullName() + "', email='" + getEmail() +
               "', phone='" + phoneNumber + "', license='" + licenseNumber + "'}";
    }
}