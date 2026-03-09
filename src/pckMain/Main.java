package pckMain;

import javax.swing.SwingUtilities;

/**
 * Main.java
 * Entry point of the Car Rental System application.
 * Launches the LoginGUI on the Event Dispatch Thread (EDT),
 * which is the correct and safe way to start a Swing application.
 */
public class Main {

    public static void main(String[] args) {

        // Always launch Swing GUIs on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginGUI loginWindow = new LoginGUI();
            loginWindow.setVisible(true);
        });
    }
}
