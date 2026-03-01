package pckModels;

/**
 * User.java
 * Base class for all system users (Admin and Customer).
 * Both actors share these common fields and methods.
 */
public class User {

    // -------------------------
    // Fields
    // -------------------------
    private int    userId;
    private String fullName;
    private String email;
    private String password;
    private String role;     // "ADMIN" or "CUSTOMER"

    // -------------------------
    // Constructors
    // -------------------------

    // Used when creating a new user (no ID yet, DB will generate it)
    public User(String fullName, String email, String password, String role) {
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // Used when loading an existing user from the database
    public User(int userId, String fullName, String email, String password, String role) {
        this.userId   = userId;
        this.fullName = fullName;
        this.email    = email;
        this.password = password;
        this.role     = role;
    }

    // -------------------------
    // Getters
    // -------------------------
    public int    getUserId()   { return userId;   }
    public String getFullName() { return fullName; }
    public String getEmail()    { return email;    }
    public String getPassword() { return password; }
    public String getRole()     { return role;     }

    // -------------------------
    // Setters
    // -------------------------
    public void setUserId(int userId)       { this.userId   = userId;   }
    public void setFullName(String fullName){ this.fullName = fullName; }
    public void setEmail(String email)      { this.email    = email;    }
    public void setPassword(String password){ this.password = password; }
    public void setRole(String role)        { this.role     = role;     }

    // -------------------------
    // Utility
    // -------------------------

    /**
     * Quick role checks — use these instead of comparing strings manually.
     * Example: if (user.isAdmin()) { ... }
     */
    public boolean isAdmin()    { return "ADMIN".equalsIgnoreCase(role);    }
    public boolean isCustomer() { return "CUSTOMER".equalsIgnoreCase(role); }

    @Override
    public String toString() {
        return "User{" +
               "userId="   + userId   +
               ", name='"  + fullName + '\'' +
               ", email='" + email    + '\'' +
               ", role='"  + role     + '\'' +
               '}';
    }
}
