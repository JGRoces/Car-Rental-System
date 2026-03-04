package pckModels;

/**
 * Admin.java
 * Extends User to represent an admin account.
 * Minimal — role is handled by the parent User class.
 */
public class Admin extends User {

    // New admin — before DB insert
    public Admin(String fullName, String email, String password) {
        super(fullName, email, password, "ADMIN");
    }

    // Loaded from DB — has userId
    public Admin(int userId, String fullName, String email, String password) {
        super(userId, fullName, email, password, "ADMIN");
    }

    @Override
    public String toString() {
        return "Admin{userId=" + getUserId() + ", fullName='" + getFullName() +
               "', email='" + getEmail() + "'}";
    }
}