package pckUtils;

import pckModels.User;

/**
 * SessionManager.java
 * Tracks the currently logged-in user across the entire application.
 * Any class can call SessionManager.getCurrentUser() to know who is logged in
 * without needing to pass the User object around through every constructor.
 *
 * Usage:
 *   Set on login  → SessionManager.setCurrentUser(user);
 *   Get anywhere  → SessionManager.getCurrentUser();
 *   Clear on logout → SessionManager.clearSession();
 */
public class SessionManager {

    // -------------------------
    // Current Session
    // -------------------------
    private static User currentUser = null;

    // -------------------------
    // Private Constructor
    // Utility class — should never be instantiated
    // -------------------------
    private SessionManager() {}

    // -------------------------
    // Set the logged-in user
    // Called right after successful login in AuthService
    // -------------------------
    public static void setCurrentUser(User user) {
        currentUser = user;
        System.out.println("[Session] Logged in as: " + user.getFullName() + " (" + user.getRole() + ")");
    }

    // -------------------------
    // Get the logged-in user
    // Returns null if no one is logged in
    // -------------------------
    public static User getCurrentUser() {
        return currentUser;
    }

    // -------------------------
    // Check if someone is logged in
    // -------------------------
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // -------------------------
    // Clear the session
    // Called on logout — always call this before closing the dashboard
    // -------------------------
    public static void clearSession() {
        if (currentUser != null) {
            System.out.println("[Session] Logged out: " + currentUser.getFullName());
        }
        currentUser = null;
    }
}
