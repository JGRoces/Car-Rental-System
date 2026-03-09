package pckCustomer;

import pckUtils.UIAssets;
import pckUtils.CustomTitleBar;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
 * │        CUSTOM TITLE BAR (dark, 40px)        │  ← CustomTitleBar
 * ├─────────────────────────────────────────────┤
 * │         MENU BAR (dark) — centered          │  ← buildMenuBar()
 * ├─────────────────────────────────────────────┤
 * │              CONTENT AREA                   │
 * └─────────────────────────────────────────────┘
 */
public class CustomerDashboardGUI extends JFrame {

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
    private JPanel               contentArea;
    private CardLayout           cardLayout;
    private JButton[]            navButtons;
    private BrowseCarsGUI        browseCarsPanel;
    private MakeReservationPanel makeReservationPanel;

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
        setUndecorated(true);                            // required for CustomTitleBar
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIAssets.getBg());
        setLayout(new BorderLayout());
    }

    // ─────────────────────────────────────────────
    //  Build layout
    // ─────────────────────────────────────────────
    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));

        // CustomTitleBar replaces the old white title bar
        topStack.add(new CustomTitleBar(this, "CarRentals  \u2014  Customer Portal"));
        topStack.add(buildMenuBar());

        add(topStack,           BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ====================================================
    //  MENU BAR  (dark chrome, 48px, nav centered)
    // ====================================================
    private JPanel buildMenuBar() {
        JPanel menuBar = new JPanel(new BorderLayout());
        menuBar.setBackground(UIAssets.CLR_CHROME);
        menuBar.setPreferredSize(new Dimension(0, 48));
        menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // CENTER — nav buttons
        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        navItems.setBackground(UIAssets.CLR_CHROME);

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildNavBtn(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navButtons[i].addActionListener(e -> switchPanel(idx));
            navItems.add(navButtons[i]);
        }

        // RIGHT — logout
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSide.setBackground(UIAssets.CLR_CHROME);
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
                // Animated underline
                Object w = getClientProperty("underlineWidth");
                int lineW = w instanceof Integer ? (Integer) w : 0;
                if (lineW > 0) {
                    g2.setColor(new Color(96, 165, 250));
                    int x = (getWidth() - lineW) / 2;
                    g2.fillRect(x, getHeight() - 3, lineW, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BoxLayout(btn, BoxLayout.Y_AXIS));
        btn.setBackground(UIAssets.CLR_CHROME);
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
        textLbl.setFont(isActive ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        textLbl.setForeground(isActive ? Color.WHITE : new Color(180, 180, 180));
        textLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        btn.add(iconLbl);
        btn.add(textLbl);
        btn.putClientProperty("underlineWidth", 0);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(UIAssets.CLR_CHROME_HOVER);
                animateUnderline(btn, true);
                if (activeDropdown != null && activeDropdown.isShowing() && activeDropdownBtn != btn) {
                    activeDropdown.dispose();
                    activeDropdown    = null;
                    activeDropdownBtn = null;
                }
                if (activeDropdown == null || !activeDropdown.isShowing()) {
                    showDropdown(btn);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(UIAssets.CLR_CHROME);
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
        contentArea.setBackground(UIAssets.getBg());

        browseCarsPanel      = new BrowseCarsGUI();
        makeReservationPanel = new MakeReservationPanel();

        contentArea.add((JPanel) browseCarsPanel,  PANEL_KEYS[0]);
        contentArea.add(makeReservationPanel,       PANEL_KEYS[1]);
        contentArea.add(buildPlaceholderPanel("My Rentals", "\uD83D\uDCCB"), PANEL_KEYS[2]);
        contentArea.add(buildPlaceholderPanel("Payment",    "\uD83D\uDCB3"), PANEL_KEYS[3]);

        browseCarsPanel.setOnRentNow(() -> navigateToReservationWithCar(browseCarsPanel.getSelectedCar()));

        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    private void navigateToReservationWithCar(pckModels.Car car) {
        if (car != null) makeReservationPanel.setSelectedCar(car);
        switchPanel(1);
    }

    // ====================================================
    //  PLACEHOLDER PANEL
    // ====================================================
    private JPanel buildPlaceholderPanel(String title, String icon) {
        JPanel panel = new JPanel(new java.awt.GridBagLayout());
        panel.setBackground(UIAssets.getBg());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIAssets.getBg());

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UIAssets.FONT_TITLE);
        titleLbl.setForeground(UIAssets.getTextPrimary());
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("This panel is under construction.", SwingConstants.CENTER);
        subLbl.setFont(UIAssets.FONT_SUBTITLE);
        subLbl.setForeground(UIAssets.getTextSecondary());
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
            navButtons[i].setBackground(UIAssets.CLR_CHROME);
            Component[] comps = navButtons[i].getComponents();
            if (comps.length > 1 && comps[1] instanceof JLabel lbl) {
                lbl.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
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
    //  ANIMATED UNDERLINE
    // ====================================================
    private void animateUnderline(JButton btn, boolean expand) {
        int maxWidth = btn.getWidth();
        int[] step = {expand ? 0 : maxWidth};
        javax.swing.Timer t = new javax.swing.Timer(10, null);
        t.addActionListener(e -> {
            if (expand) step[0] = Math.min(step[0] + 12, maxWidth);
            else        step[0] = Math.max(step[0] - 12, 0);
            btn.putClientProperty("underlineWidth", step[0]);
            btn.repaint();
            if ((expand && step[0] >= maxWidth) || (!expand && step[0] <= 0)) t.stop();
        });
        t.start();
    }

    // ====================================================
    //  DROPDOWN MENU
    // ====================================================
    private JWindow activeDropdown    = null;
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
        activeDropdown    = dropdown;
        activeDropdownBtn = btn;

        JPanel panel = new JPanel(new GridLayout(1, items.length, 24, 0)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIAssets.getSurface());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setOpaque(true);
        panel.setBackground(UIAssets.getSurface());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIAssets.getBorder(), 1),
            new EmptyBorder(14, 18, 14, 18)
        ));

        for (String[] col : items) {
            JPanel colPanel = new JPanel();
            colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));
            colPanel.setOpaque(true);
            colPanel.setBackground(UIAssets.getSurface());

            JLabel header = new JLabel(col[0].toUpperCase());
            header.setFont(new Font("Segoe UI", Font.BOLD, 10));
            header.setForeground(UIAssets.getTextSecondary());
            header.setBorder(new EmptyBorder(0, 4, 6, 4));
            header.setAlignmentX(Component.LEFT_ALIGNMENT);
            colPanel.add(header);

            for (int i = 1; i < col.length; i++) {
                final String colCategory = col[0];
                final String value       = col[i];

                JPanel itemRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
                    @Override protected void paintComponent(Graphics g) {
                        if (Boolean.TRUE.equals(getClientProperty("hovered"))) {
                            g.setColor(UIAssets.CLR_BLUE_LIGHT);
                            g.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                        }
                    }
                };
                itemRow.setOpaque(false);
                itemRow.setAlignmentX(Component.LEFT_ALIGNMENT);
                itemRow.setBorder(new EmptyBorder(3, 4, 3, 8));
                itemRow.setCursor(new Cursor(Cursor.HAND_CURSOR));

                boolean isActiveFilter = value.equals(browseCarsPanel.getActiveFilterForCategory(colCategory));
                JLabel itemLbl = new JLabel((isActiveFilter ? "\u2713  " : "     ") + value);
                itemLbl.setFont(new Font("Segoe UI", isActiveFilter ? Font.BOLD : Font.PLAIN, 12));
                itemLbl.setForeground(isActiveFilter ? UIAssets.CLR_BLUE : UIAssets.getTextPrimary());
                itemRow.add(itemLbl);

                itemRow.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        itemRow.putClientProperty("hovered", true);  itemRow.repaint();
                    }
                    public void mouseExited(MouseEvent e) {
                        itemRow.putClientProperty("hovered", false); itemRow.repaint();
                    }
                    public void mousePressed(MouseEvent e) {
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

        java.awt.Point loc = btn.getLocationOnScreen();
        dropdown.setLocation(loc.x, loc.y + btn.getHeight());
        dropdown.setVisible(true);

        final boolean[] mouseEnteredDropdown = {false};
        javax.swing.Timer hoverCheck = new javax.swing.Timer(40, null);
        hoverCheck.addActionListener(ev -> {
            if (!dropdown.isShowing()) { hoverCheck.stop(); return; }
            java.awt.Point     mouse  = java.awt.MouseInfo.getPointerInfo().getLocation();
            java.awt.Rectangle bounds = dropdown.getBounds();
            if (bounds.contains(mouse)) {
                mouseEnteredDropdown[0] = true;
            } else if (mouseEnteredDropdown[0]) {
                hoverCheck.stop();
                dropdown.dispose();
                activeDropdown = null;
            }
        });
        hoverCheck.start();
    }

    private String[][] getDropdownItems(String label) {
        return switch (label) {
            case "Browse Cars" -> new String[][] {
                {"By Type",      "Sedan", "SUV", "MPV", "Van", "Pickup"},
                {"By Brand",     "Toyota", "Honda", "Mitsubishi", "BYD"},
                {"By Price",     "Under \u20B11,000", "\u20B11,000 \u2013 \u20B12,000", "Above \u20B12,000"},
                {"Transmission", "Automatic", "Manual", "CVT"},
                {"Fuel Type",    "Gasoline", "Diesel", "Hybrid", "Electric"},
            };
            case "Make a Reservation" -> new String[][] {
                {"Rental",  "New Reservation", "Modify Booking", "Cancel Booking"},
                {"Options", "Per Hour", "Per Day", "Long Term"},
            };
            case "My Rentals" -> new String[][] {
                {"Status",  "Active Rentals", "Pending Approval", "Completed", "Cancelled"},
                {"Actions", "View Details", "Extend Rental", "Request Return"},
            };
            case "Payment" -> new String[][] {
                {"Transactions", "Payment History", "Pending Payments", "Refund Status"},
                {"Methods",      "Manage Payment Methods", "Add Credit Card", "Add PayPal"},
            };
            default -> null;
        };
    }

    // ====================================================
    //  BROWSE CARS FILTER DISPATCHER
    // ====================================================
    private void applyBrowseFilter(String category, String value) {
        switchPanel(0);
        switch (category) {
            case "By Type"      -> browseCarsPanel.filterByType(value);
            case "By Brand"     -> browseCarsPanel.filterByBrand(value);
            case "By Price"     -> browseCarsPanel.filterByPriceRange(value);
            case "Transmission" -> browseCarsPanel.filterByTransmission(value);
            case "Fuel Type"    -> browseCarsPanel.filterByFuelType(value);
        }
    }
}