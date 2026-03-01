package pckAdmin;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * AdminDashboardGUI.java
 * Main dashboard screen for the Admin actor.
 * Size: 1600 x 900 — Resizable
 *
 * Layout (planned):
 * ┌─────────────────────────────────────────────┐
 * │              TOP NAVIGATION BAR             │
 * ├──────────────┬──────────────────────────────┤
 * │              │                              │
 * │   SIDE NAV   │       MAIN CONTENT AREA      │
 * │              │                              │
 * └──────────────┴──────────────────────────────┘
 *
 * TODO: Add navigation bar, sidebar, and content panels
 *       once database and service layers are ready.
 */
public class AdminDashboardGUI extends JFrame {

    // -------------------------
    // Color Palette
    // -------------------------
    private static final Color CLR_BG       = new Color(245, 245, 245);
    private static final Color CLR_WHITE    = Color.WHITE;
    private static final Color CLR_BLACK    = new Color(18, 18, 18);
    private static final Color CLR_GRAY     = new Color(120, 120, 120);
    private static final Color CLR_BORDER   = new Color(220, 220, 220);
    private static final Color CLR_BLUE     = new Color(37, 99, 235);
    private static final Color CLR_GREEN    = new Color(22, 163, 74);
    private static final Color CLR_YELLOW   = new Color(234, 179, 8);
    private static final Color CLR_RED      = new Color(220, 38, 38);
    private static final Color CLR_SIDEBAR  = new Color(18, 18, 18);
    private static final Color CLR_TOPBAR   = Color.WHITE;

    // -------------------------
    // Fonts
    // -------------------------
    private static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD,  13);

    // -------------------------
    // Constructor
    // -------------------------
    public AdminDashboardGUI() {
        initWindow();
        initComponents();
    }

    // -------------------------
    // Window Setup
    // -------------------------
    private void initWindow() {
        setTitle("Car Rental System — Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));   // Minimum resize boundary
        setResizable(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CLR_BG);
        setLayout(new BorderLayout());
    }

    // -------------------------
    // Build Layout Skeleton
    // -------------------------
    private void initComponents() {

        // --- TOP NAVIGATION BAR ---
        JPanel topBar = buildTopBar();
        add(topBar, BorderLayout.NORTH);

        // --- SIDE NAVIGATION ---
        JPanel sideNav = buildSideNav();
        add(sideNav, BorderLayout.WEST);

        // --- MAIN CONTENT AREA ---
        JPanel contentArea = buildContentArea();
        add(contentArea, BorderLayout.CENTER);
    }

    // -------------------------
    // Top Bar
    // -------------------------
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CLR_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER));

        // TODO: Add app logo/title on the left
        // TODO: Add user info (name, role) and logout button on the right

        return bar;
    }

    // -------------------------
    // Side Navigation
    // -------------------------
    private JPanel buildSideNav() {
        JPanel nav = new JPanel();
        nav.setBackground(CLR_SIDEBAR);
        nav.setPreferredSize(new Dimension(220, 0));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        // TODO: Add navigation menu items:
        //   - Dashboard (overview/stats)
        //   - Manage Cars
        //   - Manage Customers
        //   - Manage Rentals
        //   - Reports
        //   - Logout

        return nav;
    }

    // -------------------------
    // Main Content Area
    // -------------------------
    private JPanel buildContentArea() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CLR_BG);
        content.setBorder(new EmptyBorder(24, 24, 24, 24));

        // TODO: This area will dynamically swap panels based on
        //       which sidebar item the admin clicks.
        //       Default view will be an overview/stats panel.

        return content;
    }
}
