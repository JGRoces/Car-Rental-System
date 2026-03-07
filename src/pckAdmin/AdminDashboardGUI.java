package pckAdmin;

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
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import pckMain.LoginGUI;
import pckServices.AuthService;
import pckUtils.SessionManager;

/**
 * AdminDashboardGUI.java
 * Main dashboard for the Admin actor.
 *
 * Layout:
 * ┌─────────────────────────────────────────────────────┐
 * │                    TOP BAR                          │
 * ├───────────────┬─────────────────────────────────────┤
 * │               │                                     │
 * │   SIDEBAR     │         CONTENT AREA                │
 * │  (collapse)   │     (CardLayout panels)             │
 * │               │                                     │
 * └───────────────┴─────────────────────────────────────┘
 *
 * Sidebar nav items:
 *   - Overview (default)
 *   - Manage Cars
 *   - Manage Customers
 *   - Manage Rentals
 *   - Reports
 *   - Logout
 */
public class AdminDashboardGUI extends JFrame {

    // -------------------------
    // Color Palette
    // -------------------------
    private static final Color CLR_BG             = new Color(245, 245, 245);
    private static final Color CLR_WHITE          = Color.WHITE;
    private static final Color CLR_BLACK          = new Color(18, 18, 18);
    private static final Color CLR_GRAY           = new Color(120, 120, 120);
    private static final Color CLR_BORDER         = new Color(220, 220, 220);
    private static final Color CLR_SIDEBAR        = new Color(18, 18, 18);
    private static final Color CLR_SIDEBAR_HOVER  = new Color(32, 32, 32);
    private static final Color CLR_SIDEBAR_ACTIVE = new Color(37, 99, 235);
    private static final Color CLR_TOPBAR         = Color.WHITE;
    private static final Color CLR_BLUE           = new Color(37, 99, 235);
    private static final Color CLR_GREEN          = new Color(22, 163, 74);
    private static final Color CLR_YELLOW         = new Color(234, 179, 8);
    private static final Color CLR_RED            = new Color(220, 38, 38);
    private static final Color CLR_BLUE_LIGHT     = new Color(219, 234, 254);
    private static final Color CLR_GREEN_LIGHT    = new Color(220, 252, 231);
    private static final Color CLR_YELLOW_LIGHT   = new Color(254, 249, 195);
    private static final Color CLR_RED_LIGHT      = new Color(254, 226, 226);

    // -------------------------
    // Fonts
    // -------------------------
    private static final Font FONT_TITLE        = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SUBTITLE     = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NAV          = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_NAV_BOLD     = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_CARD_VALUE   = new Font("Segoe UI", Font.BOLD,  32);
    private static final Font FONT_CARD_LABEL   = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_CARD_SUB     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_SECTION      = new Font("Segoe UI", Font.BOLD,  15);
    private static final Font FONT_SMALL        = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TOPBAR_NAME  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_TOPBAR_ROLE  = new Font("Segoe UI", Font.PLAIN, 11);

    // -------------------------
    // Sidebar Config
    // -------------------------
    private static final int SIDEBAR_EXPANDED  = 220;
    private static final int SIDEBAR_COLLAPSED = 60;
    private boolean          sidebarExpanded   = true;

    // -------------------------
    // Nav Items
    // -------------------------
    private static final String[] NAV_LABELS = {
        "Overview", "Manage Cars", "Manage Customers", "Manage Rentals", "Reports"
    };
    private static final String[] NAV_ICONS = {
        "⊞", "🚗", "👤", "📋", "📊"
    };
    private static final String[] PANEL_KEYS = {
        "OVERVIEW", "CARS", "CUSTOMERS", "RENTALS", "REPORTS"
    };

    // -------------------------
    // Components
    // -------------------------
    private JPanel     sidebar;
    private JPanel     contentArea;
    private CardLayout cardLayout;
    private JButton[]  navButtons;
    private JButton    toggleBtn;

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
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CLR_BG);
    }

    // -------------------------
    // Build Layout
    // -------------------------
    private void initComponents() {
        add(buildTopBar(),      BorderLayout.NORTH);
        add(buildSidebar(),     BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ====================================================
    //  TOP BAR
    // ====================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(CLR_TOPBAR);
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, CLR_BORDER),
            new EmptyBorder(0, 20, 0, 24)
        ));

        // LEFT — Branding
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftSide.setBackground(CLR_TOPBAR);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
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

        JLabel appName = new JLabel("CarRentals  —  Admin Panel");
        appName.setFont(FONT_NAV_BOLD);
        appName.setForeground(CLR_BLACK);
        appName.setBorder(new EmptyBorder(0, 10, 0, 0));

        leftSide.add(dots);
        leftSide.add(appName);

        // RIGHT — User info + avatar
        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName()
            : "Administrator";

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setBackground(CLR_TOPBAR);

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 0));
        userInfo.setBackground(CLR_TOPBAR);

        JLabel nameLabel = new JLabel(displayName, SwingConstants.RIGHT);
        nameLabel.setFont(FONT_TOPBAR_NAME);
        nameLabel.setForeground(CLR_BLACK);

        JLabel roleLabel = new JLabel("Admin", SwingConstants.RIGHT);
        roleLabel.setFont(FONT_TOPBAR_ROLE);
        roleLabel.setForeground(CLR_GRAY);

        userInfo.add(nameLabel);
        userInfo.add(roleLabel);

        String initial = displayName.substring(0, 1).toUpperCase();
        JPanel avatar  = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_BLUE);
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
    //  SIDEBAR
    // ====================================================
    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setBackground(CLR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setMinimumSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(12, 0, 12, 0));

        // Toggle button
        toggleBtn = buildToggleButton();
        sidebar.add(toggleBtn);
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(8));

        // Nav buttons
        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildNavButton(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navButtons[i].addActionListener(e -> switchPanel(idx));
            sidebar.add(navButtons[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        // Logout at bottom
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(8));

        JButton logoutBtn = buildNavButton("🚪", "Logout", false);
        logoutBtn.addActionListener(e -> handleLogout());
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton("◀");
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(CLR_GRAY);
        btn.setBackground(CLR_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setBorder(new EmptyBorder(0, 0, 0, 14));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(CLR_GRAY);   }
        });
        btn.addActionListener(e -> toggleSidebar());
        return btn;
    }

    private JButton buildNavButton(String icon, String label, boolean isActive) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setLayout(new BorderLayout());
        btn.setBackground(isActive ? CLR_SIDEBAR_ACTIVE : CLR_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon — fixed 60px wrapper so it stays centered when collapsed
        JPanel iconWrapper = new JPanel(new GridBagLayout());
        iconWrapper.setOpaque(false);
        iconWrapper.setPreferredSize(new Dimension(SIDEBAR_COLLAPSED, 44));

        JLabel iconLbl = new JLabel(icon, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        iconLbl.setForeground(Color.WHITE);
        iconWrapper.add(iconLbl);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(isActive ? FONT_NAV_BOLD : FONT_NAV);
        textLbl.setForeground(isActive ? Color.WHITE : new Color(180, 180, 180));
        textLbl.setBorder(new EmptyBorder(0, 0, 0, 12));

        btn.add(iconWrapper, BorderLayout.WEST);
        btn.add(textLbl,     BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(CLR_SIDEBAR_ACTIVE))
                    btn.setBackground(CLR_SIDEBAR_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(CLR_SIDEBAR_ACTIVE))
                    btn.setBackground(CLR_SIDEBAR);
            }
        });
        return btn;
    }

    private JSeparator buildSidebarDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(40, 40, 40));
        sep.setBackground(CLR_SIDEBAR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        int w = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        sidebar.setPreferredSize(new Dimension(w, 0));
        toggleBtn.setText(sidebarExpanded ? "◀" : "▶");

        for (JButton btn : navButtons) {
    Component center = ((BorderLayout) btn.getLayout()).getLayoutComponent(BorderLayout.CENTER);
    if (center != null) center.setVisible(sidebarExpanded);
    btn.setPreferredSize(new Dimension(w, 44));
    btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
}

    // Also toggle logout button text
    Component logoutComp = sidebar.getComponent(sidebar.getComponentCount() - 1);
    if (logoutComp instanceof JButton logoutBtn) {
        Component center = ((BorderLayout) logoutBtn.getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (center != null) center.setVisible(sidebarExpanded);
        logoutBtn.setPreferredSize(new Dimension(w, 44));
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
    }
        sidebar.revalidate();
        sidebar.repaint();
        revalidate();
        repaint();
    }

    // ====================================================
    //  CONTENT AREA — CardLayout
    // ====================================================
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(CLR_BG);

        contentArea.add(buildOverviewPanel(),                                    PANEL_KEYS[0]);
        contentArea.add(buildPlaceholderPanel("Manage Cars",      "\uD83D\uDE97"), PANEL_KEYS[1]);
        contentArea.add(buildPlaceholderPanel("Manage Customers", "\uD83D\uDC64"), PANEL_KEYS[2]);
        contentArea.add(buildPlaceholderPanel("Manage Rentals",   "\uD83D\uDCCB"), PANEL_KEYS[3]);
        contentArea.add(buildPlaceholderPanel("Reports",          "\uD83D\uDCCA"), PANEL_KEYS[4]);

        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    // ====================================================
    //  OVERVIEW PANEL
    // ====================================================
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Page header
        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setBackground(CLR_BG);
        titleStack.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel pageTitle = new JLabel("Overview");
        pageTitle.setFont(FONT_TITLE);
        pageTitle.setForeground(CLR_BLACK);

        JLabel pageSub = new JLabel("Welcome back! Here's a summary of the system.");
        pageSub.setFont(FONT_SUBTITLE);
        pageSub.setForeground(CLR_GRAY);

        titleStack.add(pageTitle);
        titleStack.add(pageSub);

        // Scrollable body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);

        body.add(buildSectionLabel("At a Glance"));
        body.add(Box.createVerticalStrut(12));
        body.add(buildStatsRow());
        body.add(Box.createVerticalStrut(28));

        body.add(buildSectionLabel("Recent Rentals"));
        body.add(Box.createVerticalStrut(12));
        body.add(buildRecentRentalsPlaceholder());
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

        row.add(buildStatCard("Total Cars",     "—", "In inventory",        CLR_BLUE,   CLR_BLUE_LIGHT));
        row.add(buildStatCard("Active Rentals", "—", "Currently ongoing",   CLR_GREEN,  CLR_GREEN_LIGHT));
        row.add(buildStatCard("Customers",      "—", "Registered accounts", CLR_YELLOW, CLR_YELLOW_LIGHT));
        row.add(buildStatCard("Total Revenue",  "—", "All time earnings",   CLR_RED,    CLR_RED_LIGHT));

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

        // Top: label + colored badge
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

        // Bottom: value + sub
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
    //  RECENT RENTALS PLACEHOLDER
    // ====================================================
    private JPanel buildRecentRentalsPlaceholder() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CLR_WHITE);
        wrapper.setBorder(new LineBorder(CLR_BORDER, 1, true));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] cols = { "#", "Customer", "Car", "Start Date", "End Date", "Status" };
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

        // TODO: Replace empty label with JScrollPane(table) once RentalDAO is ready

        return wrapper;
    }

    // ====================================================
    //  QUICK ACTIONS
    // ====================================================
    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setBackground(CLR_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildActionButton("+ Add Car",      CLR_BLUE,   () -> switchPanel(1)));
        row.add(buildActionButton("+ Add Customer", CLR_GREEN,  () -> switchPanel(2)));
        row.add(buildActionButton("New Rental",     CLR_YELLOW, () -> switchPanel(3)));
        row.add(buildActionButton("View Reports",   CLR_RED,    () -> switchPanel(4)));

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
        btn.setPreferredSize(new Dimension(148, 40));
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
    //  PLACEHOLDER PANEL
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
            navButtons[i].setBackground(active ? CLR_SIDEBAR_ACTIVE : CLR_SIDEBAR);
            Component center = ((BorderLayout) navButtons[i].getLayout())
                .getLayoutComponent(BorderLayout.CENTER);
            if (center instanceof JLabel lbl) {
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