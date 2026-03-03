package pckCustomer;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import pckMain.LoginGUI;
import pckServices.AuthService;
import pckUtils.SessionManager;


/**
 * CustomerDashboardGUI.java
 * Main dashboard screen for the Customer actor.
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
public class CustomerDashboardGUI extends JFrame {

    // -------------------------
    // Color Palette
    // -------------------------
    private static final Color CLR_BG       = new Color(245, 245, 245);
    private static final Color CLR_WHITE    = Color.WHITE;
    private static final Color CLR_BLACK    = new Color(18, 18, 18);
    private static final Color CLR_GRAY     = new Color(120, 120, 120);
    private static final Color CLR_BORDER   = new Color(220, 220, 220);
    private static final Color CLR_TOPBAR       = Color.WHITE;
    private static final Color CLR_MENUBAR      = new Color(18, 18, 18);
    private static final Color CLR_MENU_HOVER   = new Color(32, 32, 32);
    private static final Color CLR_MENU_ACTIVE  = new Color(37, 99, 235);
    private static final Color CLR_BLUE         = new Color(37, 99, 235);
    private static final Color CLR_GREEN        = new Color(22, 163, 74);
    private static final Color CLR_YELLOW       = new Color(234, 179, 8);
    private static final Color CLR_RED          = new Color(220, 38, 38);
    private static final Color CLR_BLUE_LIGHT   = new Color(219, 234, 254);
    private static final Color CLR_GREEN_LIGHT  = new Color(220, 252, 231);
    private static final Color CLR_YELLOW_LIGHT = new Color(254, 249, 195);
    private static final Color CLR_RED_LIGHT    = new Color(254, 226, 226);

    // -------------------------
    // Fonts
    // -------------------------
    private static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NAV         = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_NAV_BOLD    = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_CARD_VALUE  = new Font("Segoe UI", Font.BOLD,  32);
    private static final Font FONT_CARD_LABEL  = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_CARD_SUB    = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_SECTION     = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TOPBAR_NAME = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TOPBAR_ROLE = new Font("Segoe UI", Font.PLAIN, 11);


    //--------------------------
    // Nav Items
    //--------------------------
    private static final String[] NAV_LABELS = {
        "Dashboard",
        "Browse Cars",
        "Make a Reservation",
        "My Rentals",
        "Payment",
        "Logout"
    };
    private static final String[] NAV_ICONS = {
        "\u229E",
        "\uD83D\uDE97",
        "\uD83D\uDCC5", 
        "\uD83D\uDCCB", 
        "\uD83D\uDCB3"
    };
    private static final String[] PANEL_KEYS = {
        "DASHBOARD",
        "BROWSE_CARS",
        "MAKE_RESERVATION",
        "MY_RENTALS",
        "PAYMENT"
    };
    
    //--------------------------
    //Components
    //--------------------------
    private JPanel     contentArea;
    private CardLayout cardLayout;
    private JButton[]  navButtons;

    // -------------------------
    // Constructor
    // -------------------------
    public CustomerDashboardGUI() {
        initWindow();
        initComponents();
    }

    // -------------------------
    // Window Setup
    // -------------------------
    private void initWindow() {
        setTitle("Car Rental System — Customer Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));   // Minimum resize boundary
        setResizable(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CLR_BG);
        setLayout(new BorderLayout());
    }

    // -------------------------
    // Build Layout
    // div2 (title bar) + div3 (menu bar) stacked in NORTH
    // div4 (content area) fills CENTER
    // -------------------------
    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(buildTitleBar()); // div2
        topStack.add(buildMenuBar()); // div3

        add(topStack,           BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER); // div4
    }

    // ====================================================
    //  DIV2 — TITLE BAR
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

        // LEFT — branding
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

        // RIGHT — user info + avatar
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
    //  DIV3 — MENU BAR  (dark, 48px)
    // ====================================================
    private JPanel buildMenuBar() {
        JPanel menuBar = new JPanel(new BorderLayout());
        menuBar.setBackground(CLR_MENUBAR);
        menuBar.setPreferredSize(new Dimension(0, 48));
        menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        // LEFT — nav items
        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navItems.setBackground(CLR_MENUBAR);
        navItems.setBorder(new EmptyBorder(0, 8, 0, 0));

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildMenuNavButton(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navButtons[i].addActionListener(e -> switchPanel(idx));
            navItems.add(navButtons[i]);
        }

        // RIGHT — logout button
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSide.setBackground(CLR_MENUBAR);

        JButton logoutBtn = buildMenuNavButton("\uD83D\uDEAA", "Logout", false);
        logoutBtn.addActionListener(e -> handleLogout());
        rightSide.add(logoutBtn);

        menuBar.add(navItems,  BorderLayout.WEST);
        menuBar.add(rightSide, BorderLayout.EAST);
        return menuBar;
    }

    private JButton buildMenuNavButton(String icon, String label, boolean isActive) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Active state: blue underline at bottom
                if (getBackground().equals(CLR_MENU_ACTIVE)) {
                    g2.setColor(new Color(96, 165, 250));
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new FlowLayout(FlowLayout.CENTER, 6, 0));
        btn.setBackground(isActive ? CLR_MENU_ACTIVE : CLR_MENUBAR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(computeNavWidth(label), 48));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        iconLbl.setForeground(Color.WHITE);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(isActive ? FONT_NAV_BOLD : FONT_NAV);
        textLbl.setForeground(isActive ? Color.WHITE : new Color(180, 180, 180));

        btn.add(iconLbl);
        btn.add(textLbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(CLR_MENU_ACTIVE))
                    btn.setBackground(CLR_MENU_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(CLR_MENU_ACTIVE))
                    btn.setBackground(CLR_MENUBAR);
            }
        });
        return btn;
    }

    /** Estimate button width from label length */
    private int computeNavWidth(String label) {
        return Math.max(110, label.length() * 9 + 48);
    }

    // ====================================================
    //  DIV4 — CONTENT AREA  (CardLayout, fills all remaining height)
    // ====================================================
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(CLR_BG);

        contentArea.add(buildDashboardPanel(),                                    PANEL_KEYS[0]);
        contentArea.add(buildPlaceholderPanel("Browse Cars",          "\uD83D\uDE97"), PANEL_KEYS[1]);
        contentArea.add(buildPlaceholderPanel("Make a Reservation",   "\uD83D\uDCC5"), PANEL_KEYS[2]);
        contentArea.add(buildPlaceholderPanel("My Rentals",           "\uD83D\uDCCB"), PANEL_KEYS[3]);
        contentArea.add(buildPlaceholderPanel("Payment",              "\uD83D\uDCB3"), PANEL_KEYS[4]);

        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    // ====================================================
    //  DASHBOARD PANEL  (default view after login)
    // ====================================================
    private JPanel buildDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Page header
        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName()
            : "Customer";

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setBackground(CLR_BG);
        titleStack.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel pageTitle = new JLabel("Welcome, " + displayName + "!");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(CLR_BLACK);

        JLabel pageSub = new JLabel("Here's a summary of your account and activity.");
        pageSub.setFont(FONT_SUBTITLE);
        pageSub.setForeground(CLR_GRAY);

        titleStack.add(pageTitle);
        titleStack.add(pageSub);

        // Scrollable body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);

        body.add(buildSectionLabel("My Overview"));
        body.add(Box.createVerticalStrut(12));
        body.add(buildStatsRow());
        body.add(Box.createVerticalStrut(28));

        body.add(buildSectionLabel("My Recent Rentals"));
        body.add(Box.createVerticalStrut(12));
        body.add(buildRecentRentalsTable());
        body.add(Box.createVerticalStrut(28));

        body.add(buildSectionLabel("Quick Actions"));
        body.add(Box.createVerticalStrut(12));
        body.add(buildQuickActions());
        body.add(Box.createVerticalStrut(24));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(CLR_BG);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(titleStack, BorderLayout.NORTH);
        panel.add(scroll,     BorderLayout.CENTER);
        return panel;
    }

    // ====================================================
    //  STATS ROW — 4 Cards
    // ====================================================
    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(CLR_BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildStatCard("Active Rentals",   "—", "Currently renting",  CLR_BLUE,   CLR_BLUE_LIGHT));
        row.add(buildStatCard("Total Rentals",    "—", "All time",            CLR_GREEN,  CLR_GREEN_LIGHT));
        row.add(buildStatCard("Pending Payments", "—", "Awaiting settlement", CLR_YELLOW, CLR_YELLOW_LIGHT));
        row.add(buildStatCard("Total Spent",      "—", "Lifetime payments",   CLR_RED,    CLR_RED_LIGHT));

        return row;
    }

    private JPanel buildStatCard(String label, String value, String sub,
                                  Color accent, Color bgLight) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CLR_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CLR_WHITE);

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_CARD_LABEL);
        lbl.setForeground(CLR_GRAY);

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgLight);
                g2.fillOval(0, 0, 26, 26);
                g2.setColor(accent);
                g2.fillOval(8, 8, 10, 10);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(26, 26));
        badge.setOpaque(false);

        top.add(lbl,   BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_CARD_VALUE);
        valLbl.setForeground(CLR_BLACK);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(FONT_CARD_SUB);
        subLbl.setForeground(CLR_GRAY);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 2));
        bottom.setBackground(CLR_WHITE);
        bottom.add(valLbl);
        bottom.add(subLbl);

        card.add(top,    BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // ====================================================
    //  RECENT RENTALS TABLE
    // ====================================================
    private JPanel buildRecentRentalsTable() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CLR_WHITE);
        wrapper.setBorder(new LineBorder(CLR_BORDER, 1, true));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = { "#", "Car", "Start Date", "End Date", "Total Amount", "Status" };
        JTable table  = new JTable(new Object[][]{}, cols);
        table.setFont(FONT_SMALL);
        table.setRowHeight(36);
        table.setBackground(CLR_WHITE);
        table.setForeground(CLR_BLACK);
        table.setGridColor(CLR_BORDER);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);
        table.getTableHeader().setFont(FONT_NAV_BOLD);
        table.getTableHeader().setBackground(CLR_BG);
        table.getTableHeader().setForeground(CLR_GRAY);
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, CLR_BORDER));

        JLabel empty = new JLabel("No rental records yet.", SwingConstants.CENTER);
        empty.setFont(FONT_SUBTITLE);
        empty.setForeground(CLR_GRAY);
        empty.setBorder(new EmptyBorder(40, 0, 40, 0));

        wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        wrapper.add(empty,                  BorderLayout.CENTER);

        // TODO: Replace empty label with JScrollPane(table) once RentalDAO is connected

        return wrapper;
    }

    // ====================================================
    //  QUICK ACTIONS
    // ====================================================
    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setBackground(CLR_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildActionButton("Browse Cars",     CLR_BLUE,   () -> switchPanel(1)));
        row.add(buildActionButton("New Reservation", CLR_GREEN,  () -> switchPanel(2)));
        row.add(buildActionButton("My Rentals",      CLR_YELLOW, () -> switchPanel(3)));
        row.add(buildActionButton("Make Payment",    CLR_RED,    () -> switchPanel(4)));

        return row;
    }

    private JButton buildActionButton(String label, Color color, Runnable action) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_NAV_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color darker = color.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(darker); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color);  }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    // ====================================================
    //  PLACEHOLDER PANEL  (for unbuilt sections)
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
        inner.add(Box.createVerticalStrut(12));
        inner.add(titleLbl);
        inner.add(Box.createVerticalStrut(6));
        inner.add(subLbl);

        panel.add(inner);
        return panel;
    }

    // ====================================================
    //  HELPERS
    // ====================================================
    private JLabel buildSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(CLR_BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void switchPanel(int index) {
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = (i == index);
            navButtons[i].setBackground(active ? CLR_MENU_ACTIVE : CLR_MENUBAR);
            Component[] comps = navButtons[i].getComponents();
            // comps[0] = icon label, comps[1] = text label
            if (comps.length > 1 && comps[1] instanceof JLabel lbl) {
                lbl.setFont(active ? FONT_NAV_BOLD : FONT_NAV);
                lbl.setForeground(active ? Color.WHITE : new Color(180, 180, 180));
            }
        }
        cardLayout.show(contentArea, PANEL_KEYS[index]);
    }

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
}
