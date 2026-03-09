package pckCustomer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import pckMain.LoginGUI;
import pckServices.AuthService;
import pckUtils.SessionManager;

/**
 * CustomerDashboardGUI.java
 * Main screen for the Customer actor.
 * No dashboard — opens directly on Browse Cars.
 *
 * Layout:
 * ┌─────────────────────────────────────────────┐
 * │              TITLE BAR (white)              │
 * ├─────────────────────────────────────────────┤
 * │         MENU BAR (dark) — centered          │
 * ├─────────────────────────────────────────────┤
 * │              CONTENT AREA                   │
 * └─────────────────────────────────────────────┘
 */
public class CustomerDashboardGUI extends JFrame {

    // ─────────────────────────────────────────────
    //  Colors
    // ─────────────────────────────────────────────
    private static final Color CLR_BG          = new Color(245, 245, 245);
    private static final Color CLR_WHITE       = Color.WHITE;
    private static final Color CLR_BLACK       = new Color(18, 18, 18);
    private static final Color CLR_GRAY        = new Color(120, 120, 120);
    private static final Color CLR_BORDER      = new Color(220, 220, 220);
    private static final Color CLR_TOPBAR      = Color.WHITE;
    private static final Color CLR_MENUBAR     = new Color(18, 18, 18);
    private static final Color CLR_MENU_HOVER  = new Color(32, 32, 32);
    private static final Color CLR_GREEN       = new Color(22, 163, 74);
    private static final Color CLR_BLUE        = new Color(37, 99, 235);
    private static final Color CLR_YELLOW      = new Color(234, 179, 8);
    private static final Color CLR_RED         = new Color(220, 38, 38);

    // ─────────────────────────────────────────────
    //  Fonts
    // ─────────────────────────────────────────────
    private static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NAV         = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_NAV_BOLD    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TOPBAR_NAME = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TOPBAR_ROLE = new Font("Segoe UI", Font.PLAIN, 11);

    // ─────────────────────────────────────────────
    //  Nav items  (no Dashboard)
    // ─────────────────────────────────────────────
    private static final String[] NAV_LABELS = {
        "Browse Cars",
        "Make a Reservation",
        "My Rentals",
        "Payment"
    };
    private static final String[] NAV_ICONS = {
        "\uD83D\uDE97",
        "\uD83D\uDCC5",
        "\uD83D\uDCCB",
        "\uD83D\uDCB3"
    };
    private static final String[] PANEL_KEYS = {
        "BROWSE_CARS",
        "MAKE_RESERVATION",
        "MY_RENTALS",
        "PAYMENT"
    };

    // ─────────────────────────────────────────────
    //  Components
    // ─────────────────────────────────────────────
    private JPanel     contentArea;
    private CardLayout cardLayout;
    private JButton[]  navButtons;
    private BrowseCarsGUI browseCarsPanel; 

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────
    public CustomerDashboardGUI() {
        initWindow();
        initComponents();
    }

    // ─────────────────────────────────────────────
    //  Window setup
    // ─────────────────────────────────────────────
    private void initWindow() {
        setTitle("Car Rental System — Customer Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CLR_BG);
        setLayout(new BorderLayout());
    }

    // ─────────────────────────────────────────────
    //  Build layout
    // ─────────────────────────────────────────────
    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(buildTitleBar());
        topStack.add(buildMenuBar());

        add(topStack,           BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ====================================================
    //  TITLE BAR  (white, 64px)
    // ====================================================
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CLR_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(0, 24, 0, 24)
        ));

        // LEFT — colored dots + app name
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSide.setBackground(CLR_TOPBAR);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dots.setBackground(CLR_TOPBAR);
        for (Color c : new Color[]{ CLR_BLUE, CLR_GREEN, CLR_YELLOW, CLR_RED }) {
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillOval(0, 0, 10, 10);
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(10, 10));
            dot.setOpaque(false);
            dots.add(dot);
        }

        JLabel appName = new JLabel("CarRentals  \u2014  Customer Portal");
        appName.setFont(FONT_NAV_BOLD);
        appName.setForeground(CLR_BLACK);
        appName.setBorder(new EmptyBorder(0, 10, 0, 0));

        leftSide.add(dots);
        leftSide.add(appName);

        // RIGHT — user name + avatar
        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName()
            : "Customer";

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setBackground(CLR_TOPBAR);

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 0));
        userInfo.setBackground(CLR_TOPBAR);

        JLabel nameLabel = new JLabel(displayName, SwingConstants.RIGHT);
        nameLabel.setFont(FONT_TOPBAR_NAME);
        nameLabel.setForeground(CLR_BLACK);

        JLabel roleLabel = new JLabel("Customer", SwingConstants.RIGHT);
        roleLabel.setFont(FONT_TOPBAR_ROLE);
        roleLabel.setForeground(CLR_GRAY);

        userInfo.add(nameLabel);
        userInfo.add(roleLabel);

        String initial = displayName.substring(0, 1).toUpperCase();
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_GREEN);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(initial)) / 2;
                int y = (36 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(false);

        rightSide.add(userInfo);
        rightSide.add(avatar);

        JPanel lw = new JPanel(new GridBagLayout()); lw.setBackground(CLR_TOPBAR); lw.add(leftSide);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setBackground(CLR_TOPBAR); rw.add(rightSide);

        bar.add(lw, BorderLayout.WEST);
        bar.add(rw, BorderLayout.EAST);
        return bar;
    }

    // ====================================================
    //  MENU BAR  (dark, 48px, nav centered)
    // ====================================================
    private JPanel buildMenuBar() {
        JPanel menuBar = new JPanel(new BorderLayout());
        menuBar.setBackground(CLR_MENUBAR);
        menuBar.setPreferredSize(new Dimension(0, 48));
        menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // CENTER — nav buttons
        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        navItems.setBackground(CLR_MENUBAR);

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildNavBtn(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navButtons[i].addActionListener(e -> switchPanel(idx));
            navItems.add(navButtons[i]);
        }

        // RIGHT — logout
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSide.setBackground(CLR_MENUBAR);
        JButton logoutBtn = buildNavBtn("\uD83D\uDEAA", "Logout", false);
        logoutBtn.addActionListener(e -> handleLogout());
        rightSide.add(logoutBtn);

        menuBar.add(navItems,  BorderLayout.CENTER);
        menuBar.add(rightSide, BorderLayout.EAST);
        return menuBar;
    }

    private JButton buildNavBtn(String icon, String label, boolean isActive) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Animated underline stored per button
                Object w = getClientProperty("underlineWidth");
                int lineW = w instanceof Integer ? (Integer) w : 0;
                if (lineW > 0) {
                    g2.setColor(new Color(96, 165, 250));
                    int x = (getWidth() - lineW) / 2; // starts from center
                    g2.fillRect(x, getHeight() - 3, lineW, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setBackground(CLR_MENUBAR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(computeNavWidth(label), 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
            iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
            iconLbl.setForeground(Color.WHITE);
            iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            iconLbl.setBorder(new EmptyBorder(6, 0, 0, 0));

        JLabel textLbl = new JLabel(label, SwingConstants.CENTER);
            textLbl.setFont(isActive ? FONT_NAV_BOLD : FONT_NAV);
            textLbl.setForeground(isActive ? Color.WHITE : new Color(180, 180, 180));
            textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            btn.add(iconLbl);
            btn.add(textLbl);

        btn.putClientProperty("underlineWidth", 0);

            btn.addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) {
            btn.setBackground(CLR_MENU_HOVER);
            animateUnderline(btn, true);
            // If hovering a different button, close the old dropdown first
            if (activeDropdown != null && activeDropdown.isShowing() && activeDropdownBtn != btn) {
                activeDropdown.dispose();
                activeDropdown = null;
                activeDropdownBtn = null;
            }
            // Only open if not already showing for this button
            if (activeDropdown == null || !activeDropdown.isShowing()) {
                showDropdown(btn);
            }
        }
        @Override public void mouseExited(MouseEvent e) {
            btn.setBackground(CLR_MENUBAR);
            animateUnderline(btn, false);
        }
    });
        return btn;
    }

    private int computeNavWidth(String label) {
        return Math.max(110, label.length() * 9 + 48);
    }

    // ====================================================
    //  CONTENT AREA  (CardLayout — opens on Browse Cars)
    // ====================================================
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(CLR_BG);

        browseCarsPanel = new BrowseCarsGUI();
        contentArea.add((JPanel) browseCarsPanel, PANEL_KEYS[0]);
        contentArea.add(new MakeReservationPanel(), PANEL_KEYS[1]);
        contentArea.add(buildPlaceholderPanel("My Rentals",         "\uD83D\uDCCB"), PANEL_KEYS[2]);
        contentArea.add(buildPlaceholderPanel("Payment",            "\uD83D\uDCB3"), PANEL_KEYS[3]);

        // Start on Browse Cars
        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    // ====================================================
    //  PLACEHOLDER PANEL  (replace each with real panel later)
    // ====================================================
    private JPanel buildPlaceholderPanel(String title, String icon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(CLR_BG);

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(FONT_TITLE);
        titleLbl.setForeground(CLR_BLACK);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("This panel is under construction.", SwingConstants.CENTER);
        subLbl.setFont(FONT_SUBTITLE);
        subLbl.setForeground(CLR_GRAY);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(iconLbl);
        inner.add(javax.swing.Box.createVerticalStrut(12));
        inner.add(titleLbl);
        inner.add(javax.swing.Box.createVerticalStrut(6));
        inner.add(subLbl);

        panel.add(inner);
        return panel;
    }

    // ====================================================
    //  SWITCH PANEL
    // ====================================================
    private void switchPanel(int index) {
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = (i == index);
            navButtons[i].setBackground(CLR_MENUBAR);
            Component[] comps = navButtons[i].getComponents();
            // comps[0] = icon label, comps[1] = text label
            if (comps.length > 1 && comps[1] instanceof JLabel lbl) {
                lbl.setFont(active ? FONT_NAV_BOLD : FONT_NAV);
                lbl.setForeground(active ? Color.WHITE : new Color(180, 180, 180));
            }
        }
        cardLayout.show(contentArea, PANEL_KEYS[index]);
    }

    // ====================================================
    //  LOGOUT
    // ====================================================
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to log out?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginGUI().setVisible(true);
            this.dispose();
        }
    }
    // ====================================================
    //  ANIMATED UNDERLINE (for nav buttons)
    // ====================================================
        private void animateUnderline(JButton btn, boolean expand) {
        int maxWidth = btn.getWidth();
        int[] step = {expand ? 0 : maxWidth};
        javax.swing.Timer t = new javax.swing.Timer(10, null);
        t.addActionListener(e -> {
            if (expand) {
                step[0] = Math.min(step[0] + 12, maxWidth);
            } else {
                step[0] = Math.max(step[0] - 12, 0);
            }
            btn.putClientProperty("underlineWidth", step[0]);
            btn.repaint();
            if ((expand && step[0] >= maxWidth) || (!expand && step[0] <= 0)) {
                t.stop();
            }
        });
        t.start();
    }
    // ====================================================
    //  DROPDOWN MENU (for nav buttons that have one)       
    // ====================================================
        private JWindow activeDropdown = null;
        private JButton activeDropdownBtn = null;

    private void showDropdown(JButton btn) {
        if (activeDropdown != null) {
            activeDropdown.dispose();
            activeDropdown = null;
        }

        String label = ((JLabel) btn.getComponents()[1]).getText();
        String[][] items = getDropdownItems(label);
        if (items == null) return;

        JWindow dropdown = new JWindow(this);
        activeDropdown = dropdown;
        activeDropdownBtn = btn;

        // ── Background panel ──────────────────────────────────────
        // Must be opaque with custom paintComponent — JWindow ignores
        // setBackground on some L&Fs, so we paint manually.
        JPanel panel = new JPanel(new GridLayout(1, items.length, 24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(14, 18, 14, 18)
        ));

        for (String[] col : items) {
            JPanel colPanel = new JPanel();
            colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));
            colPanel.setOpaque(true);
            colPanel.setBackground(Color.WHITE);

            // Section header
            JLabel header = new JLabel(col[0].toUpperCase());
            header.setFont(new Font("Segoe UI", Font.BOLD, 10));
            header.setForeground(new Color(150, 150, 150));
            header.setBorder(new EmptyBorder(0, 4, 6, 4));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            colPanel.add(header);

            for (int i = 1; i < col.length; i++) {
                final String colCategory = col[0];
                final String value = col[i];

                // ── Item row ──────────────────────────────────────
                // JPanel fills full column width → reliable hit area.
                // MouseListener is on the panel, not the inner label.
                JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
                    @Override protected void paintComponent(Graphics g) {
                        if (Boolean.TRUE.equals(getClientProperty("hovered"))) {
                            g.setColor(new Color(240, 245, 255));
                            g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        }
                    }
                };
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemRow.setBorder(new EmptyBorder(3, 4, 3, 8));
                itemRow.setCursor(new Cursor(Cursor.HAND_CURSOR));

                boolean isActiveFilter = value.equals(browseCarsPanel.getActiveFilterForCategory(colCategory));
                JLabel itemLbl = new JLabel((isActiveFilter ? "✓  " : "     ") + value);
                itemLbl.setFont(new Font("Segoe UI", isActiveFilter ? Font.BOLD : Font.PLAIN, 12));
                itemLbl.setForeground(isActiveFilter ? new Color(37, 99, 235) : new Color(18, 18, 18));
                itemRow.add(itemLbl);

                itemRow.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        itemRow.putClientProperty("hovered", true);
                        itemRow.repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        itemRow.putClientProperty("hovered", false);
                        itemRow.repaint();
                    }
                    public void mousePressed(MouseEvent e) {
                        // Use mousePressed instead of mouseClicked — more
                        // reliable; mouseClicked requires exact press+release
                        // on same pixel which can fail on slow systems.
                        dropdown.dispose();
                        activeDropdown = null;
                        applyBrowseFilter(colCategory, value);
                    }
                });
                colPanel.add(itemRow);
            }
            panel.add(colPanel);
        }

        dropdown.setContentPane(panel);
        dropdown.pack();

        // ── Position: flush below the nav button ─────────────────
        java.awt.Point loc = btn.getLocationOnScreen();
        dropdown.setLocation(loc.x, loc.y + btn.getHeight());
        dropdown.setVisible(true);

        // ── Close on mouse-leave via polling timer ────────────────
        // Wait until the mouse is INSIDE the dropdown first (confirms it
        // has fully appeared), then close only when it leaves the bounds.
        // This avoids false-close during the window's paint/position delay.
        final boolean[] mouseEnteredDropdown = {false};
        javax.swing.Timer hoverCheck = new javax.swing.Timer(40, null);
        hoverCheck.addActionListener(ev -> {
            if (!dropdown.isShowing()) {
                hoverCheck.stop();
                return;
            }
            java.awt.Point mouse = java.awt.MouseInfo.getPointerInfo().getLocation();
            java.awt.Rectangle bounds = dropdown.getBounds();
            if (bounds.contains(mouse)) {
                // Mouse is inside — mark as entered, keep open
                mouseEnteredDropdown[0] = true;
            } else if (mouseEnteredDropdown[0]) {
                // Mouse has entered before and now left — close
                hoverCheck.stop();
                dropdown.dispose();
                activeDropdown = null;
            }
            // If mouse never entered yet, do nothing (still appearing)
        });
        hoverCheck.start();
    }

    private String[][] getDropdownItems(String label) {
        switch (label) {
            case "Browse Cars":
                return new String[][] {
                    {"By Type",    "Sedan", "SUV", "MPV", "Van", "Pickup"},
                    {"By Brand",   "Toyota", "Honda", "Mitsubishi", "BYD"},
                    {"By Price",       "Under \u20B11,000", "\u20B11,000 \u2013 \u20B12,000", "Above \u20B12,000"},
                    {"Transmission",   "Automatic", "Manual", "CVT"},
                    {"Fuel Type",      "Gasoline", "Diesel", "Hybrid", "Electric"},
                };
            case "Make a Reservation":
                return new String[][] {
                    {"Rental",     "New Reservation", "Modify Booking", "Cancel Booking"},
                    {"Options",    "Per Hour", "Per Day", "Long Term"},
                };
            case "My Rentals":
                return new String[][] {
                    {"Status",    "Active Rentals", "Pending Approval", "Completed", "Cancelled"},
                    {"Actions",   "View Details", "Extend Rental", "Request Return"},
                };
            case "Payment":
                return new String[][] {
                    {"Transactions", "Payment History", "Pending Payments", "Refund Status"},
                    {"Methods",      "Manage Payment Methods", "Add Credit Card", "Add PayPal"},
                };
            default:
                return null;
        }
    }

    // ====================================================
    //  BROWSE CARS FILTER DISPATCHER
    // ====================================================
    private void applyBrowseFilter(String category, String value) {
        switchPanel(0); // navigate to Browse Cars first
        switch (category) {
            case "By Type"      -> browseCarsPanel.filterByType(value);
            case "By Brand"     -> browseCarsPanel.filterByBrand(value);
            case "By Price"     -> browseCarsPanel.filterByPriceRange(value);
            case "Transmission" -> browseCarsPanel.filterByTransmission(value);
            case "Fuel Type"    -> browseCarsPanel.filterByFuelType(value);
        }
    }
}