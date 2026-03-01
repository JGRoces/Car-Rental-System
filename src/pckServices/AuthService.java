package pckServices;

import pckDatabase.UserDAO;
import pckModels.User;
import pckUtils.SessionManager;

/**
 * AuthService.java
 * Handles all login and logout logic.
 * Acts as the bridge between LoginGUI and UserDAO.
 *
 * Flow:
 *   LoginGUI → AuthService.login() → UserDAO → Database
 *                                   ↓
 *                             SessionManager (stores user)
 *                                   ↓
 *               AdminDashboardGUI or CustomerDashboardGUI
 */
public class AuthService {

    // -------------------------
    // DAO Reference
    // -------------------------
    private static final UserDAO userDAO = new UserDAO();

    // -------------------------
    // Private Constructor
    // Utility class — should never be instantiated
    // -------------------------
    private AuthService() {}

    // -------------------------
    // Login
    // Returns the matched User object, or null if credentials are wrong.
    // On success, automatically sets the session via SessionManager.
    //
    // Usage in LoginGUI:
    //   User user = AuthService.login(email, password);
    // -------------------------
    public static User login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            System.err.println("[AuthService] Login attempted with empty credentials.");
            return null;
        }

        User user = userDAO.getUserByEmailAndPassword(email, password);

        if (user != null) {
            SessionManager.setCurrentUser(user);
            System.out.println("[AuthService] Login successful → routing to " + user.getRole() + " dashboard.");
        } else {
            System.err.println("[AuthService] Login failed → no matching credentials found.");
        }

        return user;
    }

    // -------------------------
    // Logout
    // Clears the session. Call this from any dashboard's logout button.
    //
    // Usage:
    //   AuthService.logout();
    //   new LoginGUI().setVisible(true);
    //   this.dispose();
    // -------------------------
    public static void logout() {
        SessionManager.clearSession();
        System.out.println("[AuthService] User logged out — session cleared.");
    }
}
