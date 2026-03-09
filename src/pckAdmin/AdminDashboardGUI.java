package pckAdmin;

import pckServices.AuthService;
import pckUtils.CustomTitleBar;
import pckUtils.SessionManager;
import pckUtils.UIAssets;
import pckMain.LoginGUI;
import pckDatabase.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * AdminDashboardGUI.java
 *
 * Layout (CSS grid equivalent):
 *   div1 — CustomTitleBar   (span 10, row 1)
 *   div2 — TopBar 64px      (span 10, row 2)
 *   div3 — Sidebar          (span 2, rows 3-10)
 *   div4 — Content area     (span 8, rows 3-10)
 */
public class AdminDashboardGUI extends JFrame {

    // ── Sidebar ──────────────────────────────────────────────
    private static final int SIDEBAR_EXPANDED  = 220;
    private static final int SIDEBAR_COLLAPSED = 60;
    private boolean          sidebarExpanded   = true;

    // ── Nav labels / keys ────────────────────────────────────
    private static final String[] TOP_NAV_LABELS = { "Overview", "Management", "Payments", "Reports" };
    private static final String[] TOP_NAV_KEYS   = { "OVERVIEW", "MANAGEMENT", "PAYMENTS", "REPORTS" };
    private static final String[] BOT_NAV_LABELS = { "Account", "Settings" };
    private static final String[] BOT_NAV_KEYS   = { "ACCOUNT", "SETTINGS" };

    // ── Management sub-tabs ───────────────────────────────────
    private static final String[] MGMT_TAB_LABELS = { "Vehicles", "Drivers", "Customers", "Rentals" };
    private static final String[] MGMT_TAB_KEYS   = { "VEHICLES", "DRIVERS", "CUSTOMERS", "RENTALS" };

    // ── Components ────────────────────────────────────────────
    private JPanel     sidebar, contentArea, topBar;
    private CardLayout cardLayout;
    private JButton[]  topNavButtons;
    private JButton[]  botNavButtons;
    private JButton    toggleBtn, logoutBtn;
    private int        activeTopIndex = 0;
    private int        activeBotIndex = -1;

    private JLabel  nameLabel, roleLabel, activeTabLabel;
    private JPanel  overviewPanel, settingsPanel;
    private JButton themeToggleBtn;

    // Management
    private JButton[]   mgmtTabButtons;
    private JPanel      mgmtContentArea;
    private CardLayout  mgmtCardLayout;

    // ── Constructor ───────────────────────────────────────────
    public AdminDashboardGUI() {
        initWindow();
        initComponents();
        applyTheme();
        UIAssets.addListener(this::applyTheme);
    }

    // =========================================================
    //  WINDOW SETUP
    // =========================================================
    private void initWindow() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        add(new CustomTitleBar(this, "Car Rental System — Admin Dashboard"), BorderLayout.NORTH);
        JPanel inner = new JPanel(new BorderLayout());
        topBar = buildTopBar();
        inner.add(topBar,             BorderLayout.NORTH);
        inner.add(buildSidebar(),     BorderLayout.WEST);
        inner.add(buildContentArea(), BorderLayout.CENTER);
        add(inner, BorderLayout.CENTER);
    }

    // =========================================================
    //  TOP BAR  (div2)
    // =========================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(new EmptyBorder(0, 24, 0, 28));

        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName() : "Administrator";
        String initial = displayName.substring(0, 1).toUpperCase();

        // LEFT — yellow avatar + name + role
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftSide.setOpaque(false);

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.CLR_YELLOW);
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                    (40 - fm.stringWidth(initial)) / 2,
                    (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setOpaque(false);

        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 2));
        nameStack.setOpaque(false);
        nameLabel = new JLabel(displayName);
        nameLabel.setFont(UIAssets.FONT_NAV_BOLD);
        nameLabel.setForeground(Color.WHITE);
        roleLabel = new JLabel("Admin");
        roleLabel.setFont(UIAssets.FONT_SMALL);
        roleLabel.setForeground(new Color(160, 160, 160));
        nameStack.add(nameLabel);
        nameStack.add(roleLabel);

        leftSide.add(avatar);
        leftSide.add(nameStack);

        // RIGHT — active tab label
        activeTabLabel = new JLabel(TOP_NAV_LABELS[0]);
        activeTabLabel.setFont(UIAssets.FONT_H1);
        activeTabLabel.setForeground(Color.WHITE);

        JPanel lw = new JPanel(new GridBagLayout()); lw.setOpaque(false); lw.add(leftSide);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(activeTabLabel);
        bar.add(lw, BorderLayout.WEST);
        bar.add(rw, BorderLayout.EAST);
        return bar;
    }

    // =========================================================
    //  SIDEBAR  (div3)
    // =========================================================
    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(12, 0, 12, 0));

        toggleBtn = buildToggleButton();
        sidebar.add(toggleBtn);
        sidebar.add(Box.createVerticalStrut(16));

        topNavButtons = new JButton[TOP_NAV_LABELS.length];
        for (int i = 0; i < TOP_NAV_LABELS.length; i++) {
            final int idx = i;
            topNavButtons[i] = buildNavButton(TOP_NAV_LABELS[i], i == 0);
            topNavButtons[i].addActionListener(e -> selectTopNav(idx));
            sidebar.add(topNavButtons[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        botNavButtons = new JButton[BOT_NAV_LABELS.length];
        for (int i = 0; i < BOT_NAV_LABELS.length; i++) {
            final int idx = i;
            botNavButtons[i] = buildNavButton(BOT_NAV_LABELS[i], false);
            botNavButtons[i].addActionListener(e -> selectBotNav(idx));
            sidebar.add(botNavButtons[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }
        sidebar.add(Box.createVerticalStrut(8));

        logoutBtn = buildNavButton("Logout", false);
        logoutBtn.addActionListener(e -> handleLogout());
        sidebar.add(logoutBtn);
        return sidebar;
    }

    private JButton buildToggleButton() {
        JButton btn = new JButton("◀");
        btn.setFont(UIAssets.FONT_SMALL);
        btn.setForeground(new Color(100, 100, 100));
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
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(new Color(100, 100, 100)); }
        });
        btn.addActionListener(e -> toggleSidebar());
        return btn;
    }

    private JButton buildNavButton(String label, boolean isActive) {
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
        btn.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        btn.setBackground(isActive ? UIAssets.CLR_SIDEBAR_ACTIVE : UIAssets.getSidebar());
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(SIDEBAR_EXPANDED, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel textLbl = new JLabel(label);
        textLbl.setFont(isActive ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        textLbl.setForeground(isActive ? Color.WHITE : new Color(180, 180, 180));
        btn.add(textLbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_SIDEBAR_ACTIVE))
                    btn.setBackground(UIAssets.getSidebarHover());
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_SIDEBAR_ACTIVE))
                    btn.setBackground(UIAssets.getSidebar());
            }
        });
        return btn;
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        int w = sidebarExpanded ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        sidebar.setPreferredSize(new Dimension(w, 0));
        toggleBtn.setText(sidebarExpanded ? "◀" : "▶");
        for (JButton[] group : new JButton[][]{ topNavButtons, botNavButtons, { logoutBtn } })
            for (JButton btn : group) {
                if (btn == null) continue;
                Component[] c = btn.getComponents();
                if (c.length > 0) c[0].setVisible(sidebarExpanded);
                btn.setPreferredSize(new Dimension(w, 44));
            }
        sidebar.revalidate(); sidebar.repaint();
        revalidate(); repaint();
    }

    // =========================================================
    //  NAV SELECTION
    // =========================================================
    private void selectTopNav(int index) {
        activeTopIndex = index; activeBotIndex = -1;
        for (int i = 0; i < topNavButtons.length; i++) setNavActive(topNavButtons[i], i == index);
        for (JButton btn : botNavButtons)               setNavActive(btn, false);
        if (activeTabLabel != null) activeTabLabel.setText(TOP_NAV_LABELS[index]);
        cardLayout.show(contentArea, TOP_NAV_KEYS[index]);
    }

    private void selectBotNav(int index) {
        activeBotIndex = index; activeTopIndex = -1;
        for (JButton btn : topNavButtons)               setNavActive(btn, false);
        for (int i = 0; i < botNavButtons.length; i++) setNavActive(botNavButtons[i], i == index);
        if (activeTabLabel != null) activeTabLabel.setText(BOT_NAV_LABELS[index]);
        cardLayout.show(contentArea, BOT_NAV_KEYS[index]);
    }

    private void setNavActive(JButton btn, boolean active) {
        btn.setBackground(active ? UIAssets.CLR_SIDEBAR_ACTIVE : UIAssets.getSidebar());
        Component[] comps = btn.getComponents();
        if (comps.length > 0 && comps[0] instanceof JLabel lbl) {
            lbl.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
            lbl.setForeground(active ? Color.WHITE : new Color(180, 180, 180));
        }
    }

    // =========================================================
    //  CONTENT AREA — CardLayout  (div4)
    // =========================================================
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        overviewPanel = buildOverviewPanel();
        contentArea.add(overviewPanel,          TOP_NAV_KEYS[0]);
        contentArea.add(buildManagementPanel(), TOP_NAV_KEYS[1]);
        contentArea.add(buildPaymentsPanel(),   TOP_NAV_KEYS[2]);
        contentArea.add(buildReportsPanel(),    TOP_NAV_KEYS[3]);
        contentArea.add(buildAccountPanel(),    BOT_NAV_KEYS[0]);
        settingsPanel = buildSettingsPanel();
        contentArea.add(settingsPanel,          BOT_NAV_KEYS[1]);
        cardLayout.show(contentArea, TOP_NAV_KEYS[0]);
        return contentArea;
    }

    // =========================================================
    //  OVERVIEW PANEL
    // =========================================================
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // ── Header row: title left, quick actions right
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 24, 0));

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setOpaque(false);
        JLabel pageTitle = new JLabel("Statistical Summary of Inventory");
        pageTitle.setFont(UIAssets.FONT_TITLE);
        JLabel pageSub   = new JLabel("Welcome back! Here's a live snapshot of the system.");
        pageSub.setFont(UIAssets.FONT_SUBTITLE);
        titleStack.add(pageTitle);
        titleStack.add(pageSub);

        JPanel quickActions = buildQuickActions();
        header.add(titleStack,   BorderLayout.WEST);
        header.add(quickActions, BorderLayout.EAST);

        // ── 4 Stat cards
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cardsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsRow.add(buildStatCard("Total Vehicles",      "—", "Registered in fleet",  UIAssets.CLR_BLUE,   UIAssets.CLR_BLUE_LIGHT));
        cardsRow.add(buildStatCard("Total Revenue",       "—", "All-time earnings",     UIAssets.CLR_GREEN,  UIAssets.CLR_GREEN_LIGHT));
        cardsRow.add(buildStatCard("Active Rentals",      "—", "Currently rented out",  UIAssets.CLR_YELLOW, UIAssets.CLR_YELLOW_LIGHT));
        cardsRow.add(buildStatCard("Users",               "—", "Customers & Drivers",   UIAssets.CLR_RED,    UIAssets.CLR_RED_LIGHT));

        // ── Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(cardsRow);
        body.add(Box.createVerticalStrut(28));

        // ── Bottom row: Recent Rentals + Recent Driver Requests side by side
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomRow.setOpaque(false);
        bottomRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomRow.add(buildRecentSection("Recent Rentals",
            new String[]{ "Customer", "Car", "Start Date", "End Date", "Status" },
            new Object[][]{},
            "No rental records yet."));
        bottomRow.add(buildRecentSection("Recent Driver Requests",
            new String[]{ "Name", "License No.", "Vehicle Type", "Status" },
            new Object[][]{},
            "No pending driver requests."));
        body.add(bottomRow);

        panel.add(header, BorderLayout.NORTH);
        panel.add(body,   BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatCard(String label, String value, String sub, Color accent, Color bgLight) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIAssets.FONT_H3);

        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgLight); g2.fillOval(0, 0, 26, 26);
                g2.setColor(accent);  g2.fillOval(8, 8, 10, 10);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(26, 26));
        badge.setOpaque(false);
        top.add(lbl, BorderLayout.WEST);
        top.add(badge, BorderLayout.EAST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(UIAssets.FONT_STAT_VALUE);
        valLbl.setForeground(accent);
        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(UIAssets.FONT_SMALL);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 2));
        bottom.setOpaque(false);
        bottom.add(valLbl);
        bottom.add(subLbl);

        card.add(top,    BorderLayout.NORTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        row.add(buildActionButton("+ Add Vehicle",  UIAssets.CLR_BLUE,   () -> { selectTopNav(1); showMgmtTab(0); }));
        row.add(buildActionButton("+ Add Customer", UIAssets.CLR_GREEN,  () -> { selectTopNav(1); showMgmtTab(2); }));
        row.add(buildActionButton("New Rental",     UIAssets.CLR_YELLOW, () -> { selectTopNav(1); showMgmtTab(3); }));
        row.add(buildActionButton("View Reports",   UIAssets.CLR_RED,    () -> selectTopNav(3)));
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
        btn.setFont(UIAssets.FONT_NAV_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(color);
        btn.setPreferredSize(new Dimension(148, 40));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(color.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(color); }
        });
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private JPanel buildRecentSection(String title, String[] cols, Object[][] rows, String emptyMsg) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        JLabel hdr = new JLabel(title);
        hdr.setFont(UIAssets.FONT_H3);
        hdr.setBorder(new EmptyBorder(14, 16, 14, 16));

        JSeparator sep = new JSeparator();
        sep.setForeground(UIAssets.getBorder());

        JTable table = buildStyledTable(cols, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(0, 200));

        JLabel empty = new JLabel(emptyMsg, SwingConstants.CENTER);
        empty.setFont(UIAssets.FONT_SUBTITLE);
        empty.setForeground(UIAssets.getTextSecondary());
        empty.setBorder(new EmptyBorder(40, 0, 40, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(hdr, BorderLayout.CENTER);
        headerPanel.add(sep, BorderLayout.SOUTH);

        wrapper.add(headerPanel, BorderLayout.NORTH);
        wrapper.add(rows.length > 0 ? scroll : empty, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================
    //  MANAGEMENT PANEL
    // =========================================================
    private JPanel buildManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 0, 28));

        // Header
        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setOpaque(false);
        titleStack.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel pageTitle = new JLabel("All Inventory & Population");
        pageTitle.setFont(UIAssets.FONT_TITLE);
        JLabel pageSub = new JLabel("Manage vehicles, drivers, customers, and rentals.");
        pageSub.setFont(UIAssets.FONT_SUBTITLE);
        titleStack.add(pageTitle);
        titleStack.add(pageSub);

        // Sub-tab bar: Vehicles | Drivers | Customers | Rentals
        JPanel tabBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabBar.setOpaque(false);
        mgmtTabButtons = new JButton[MGMT_TAB_LABELS.length];
        for (int i = 0; i < MGMT_TAB_LABELS.length; i++) {
            final int idx = i;
            mgmtTabButtons[i] = buildSubTabButton(MGMT_TAB_LABELS[i], i == 0);
            mgmtTabButtons[i].addActionListener(e -> showMgmtTab(idx));
            tabBar.add(mgmtTabButtons[i]);
        }

        // Tab divider line
        JSeparator tabLine = new JSeparator();
        tabLine.setForeground(UIAssets.getBorder());

        JPanel tabHeader = new JPanel(new BorderLayout());
        tabHeader.setOpaque(false);
        tabHeader.add(tabBar,    BorderLayout.CENTER);
        tabHeader.add(tabLine,   BorderLayout.SOUTH);

        // Content panels per tab
        mgmtCardLayout  = new CardLayout();
        mgmtContentArea = new JPanel(mgmtCardLayout);
        mgmtContentArea.setOpaque(false);

        mgmtContentArea.add(buildMgmtTablePanel("Vehicles",
            new String[]{ "ID", "Brand", "Model", "Year", "Plate No.", "Category", "Daily Rate", "Status" },
            new Object[][]{},
            UIAssets.CLR_BLUE,
            "+ Add Vehicle"), MGMT_TAB_KEYS[0]);

        mgmtContentArea.add(buildDriversPanel(), MGMT_TAB_KEYS[1]);

        mgmtContentArea.add(buildMgmtTablePanel("Customers",
            new String[]{ "ID", "Full Name", "Email", "Phone", "Joined" },
            new Object[][]{},
            UIAssets.CLR_BLUE,
            null), MGMT_TAB_KEYS[2]);

        mgmtContentArea.add(buildMgmtTablePanel("Rentals",
            new String[]{ "ID", "Customer", "Vehicle", "Driver", "Start", "End", "Amount", "Status" },
            new Object[][]{},
            UIAssets.CLR_YELLOW,
            "+ New Rental"), MGMT_TAB_KEYS[3]);

        mgmtCardLayout.show(mgmtContentArea, MGMT_TAB_KEYS[0]);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(titleStack, BorderLayout.NORTH);
        header.add(tabHeader,  BorderLayout.SOUTH);

        panel.add(header,          BorderLayout.NORTH);
        panel.add(mgmtContentArea, BorderLayout.CENTER);
        return panel;
    }

    /** Generic management table panel with optional add button */
    private JPanel buildMgmtTablePanel(String entity, String[] cols, Object[][] rows,
                                        Color accent, String addLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel count = new JLabel(rows.length + " record(s)");
        count.setFont(UIAssets.FONT_SMALL);
        count.setForeground(UIAssets.getTextSecondary());

        if (addLabel != null) {
            JButton addBtn = buildActionButton(addLabel, accent, () ->
                JOptionPane.showMessageDialog(this, addLabel + " — coming soon."));
            toolbar.add(count,  BorderLayout.WEST);
            toolbar.add(addBtn, BorderLayout.EAST);
        } else {
            toolbar.add(count, BorderLayout.WEST);
        }

        JTable table = buildStyledTable(cols, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    /** Drivers panel — shows PENDING first with Verify / Reject buttons */
    private JPanel buildDriversPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, 0, 12, 0));

        String[] statuses = { "All", "Pending", "Verified", "Rejected" };
        JComboBox<String> statusFilter = new JComboBox<>(statuses);
        statusFilter.setFont(UIAssets.FONT_BODY);
        statusFilter.setPreferredSize(new Dimension(140, 36));

        JLabel filterLbl = new JLabel("Filter by status:");
        filterLbl.setFont(UIAssets.FONT_SMALL);
        filterLbl.setForeground(UIAssets.getTextSecondary());

        JButton addDriverBtn = buildActionButton("+ Add Driver", UIAssets.CLR_GREEN, () ->
            JOptionPane.showMessageDialog(this, "Add Driver — coming soon."));

        filterRow.add(filterLbl);
        filterRow.add(statusFilter);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(filterRow,   BorderLayout.WEST);
        toolbar.add(addDriverBtn, BorderLayout.EAST);
        toolbar.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Driver table with action column
        String[] cols = { "ID", "Full Name", "Email", "Phone", "License No.", "Expiry", "Vehicle Type", "Status", "Actions" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 8; }
        };
        JTable table = buildStyledTable(cols, null);
        table.setModel(model);
        table.getColumn("Actions").setCellRenderer(new DriverActionRenderer());
        table.getColumn("Actions").setCellEditor(new DriverActionEditor(table, model));
        table.getColumn("Actions").setMinWidth(160);
        table.getColumn("Actions").setMaxWidth(180);
        table.getColumn("Status").setMinWidth(90);
        table.getColumn("Status").setMaxWidth(110);
        table.getColumn("Status").setCellRenderer(new StatusBadgeRenderer());

        // WIRING POINT (Step 6):
        //   Replace the empty model below with real data once
        //   DriverService / DriverDAO are confirmed working:
        //
        //   List<Driver> pending = DriverDAO.getPendingDrivers();
        //   for (Driver d : pending) {
        //       model.addRow(new Object[]{
        //           d.getDriverId(), d.getFullName(), d.getEmail(),
        //           d.getPhone(), d.getLicenseNumber(), d.getLicenseExpiry(),
        //           d.getVehicleType(), d.getStatus(), "actions"
        //       });
        //   }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    private JButton buildSubTabButton(String label, boolean isActive) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if (getBackground().equals(UIAssets.CLR_BLUE)) {
                    g2.setColor(UIAssets.CLR_BLUE);
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(isActive ? UIAssets.FONT_H3 : UIAssets.FONT_BODY);
        btn.setForeground(isActive ? UIAssets.CLR_BLUE : UIAssets.getTextSecondary());
        btn.setBackground(isActive ? UIAssets.CLR_BLUE : new Color(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 0, 12, 28));
        btn.setOpaque(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE))
                    btn.setForeground(UIAssets.getTextPrimary());
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE))
                    btn.setForeground(UIAssets.getTextSecondary());
            }
        });
        return btn;
    }

    private void showMgmtTab(int index) {
        for (int i = 0; i < mgmtTabButtons.length; i++) {
            boolean active = (i == index);
            mgmtTabButtons[i].setBackground(active ? UIAssets.CLR_BLUE : new Color(0, 0, 0, 0));
            mgmtTabButtons[i].setForeground(active ? UIAssets.CLR_BLUE : UIAssets.getTextSecondary());
            mgmtTabButtons[i].setFont(active ? UIAssets.FONT_H3 : UIAssets.FONT_BODY);
            mgmtTabButtons[i].repaint();
        }
        mgmtCardLayout.show(mgmtContentArea, MGMT_TAB_KEYS[index]);
    }

    // =========================================================
    //  PAYMENTS PANEL
    // =========================================================
    private JPanel buildPaymentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel titleStack = buildPageHeader("Payments",
            "Review transactions, approve pending payments, and manage late fees.");

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Transaction Ledger ───────────────────────────────
        body.add(buildSectionLabel("Transaction Ledger"));
        body.add(Box.createVerticalStrut(10));
        String[] txCols = { "Payment ID", "Rental ID", "Customer", "Amount", "Date", "Method", "Status" };
        body.add(buildTableSection(txCols, new Object[][]{}));
        body.add(Box.createVerticalStrut(24));

        // ── Pending Verifications ────────────────────────────
        body.add(buildSectionLabel("Pending Verifications"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildPendingVerificationsPanel());
        body.add(Box.createVerticalStrut(24));

        // ── Late Fees ────────────────────────────────────────
        body.add(buildSectionLabel("Late Fees"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildLateFeesPanel());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(titleStack, BorderLayout.NORTH);
        panel.add(scroll,     BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildPendingVerificationsPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = { "Rental ID", "Customer", "Amount", "Method", "Submitted", "Approve" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        JTable table = buildStyledTable(cols, null);
        table.setModel(model);
        table.getColumn("Approve").setCellRenderer(new ApproveButtonRenderer());
        table.getColumn("Approve").setCellEditor(new ApproveButtonEditor(table, model));
        table.getColumn("Approve").setMinWidth(130);
        table.getColumn("Approve").setMaxWidth(150);

        JLabel empty = new JLabel("No pending payment verifications.", SwingConstants.CENTER);
        empty.setFont(UIAssets.FONT_SUBTITLE);
        empty.setForeground(UIAssets.getTextSecondary());
        empty.setBorder(new EmptyBorder(40, 0, 40, 0));

        JScrollPane scroll = new JScrollPane(model.getRowCount() > 0 ? table : empty);
        scroll.setBorder(null);

        wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        wrapper.add(empty,                  BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildLateFeesPanel() {
        JPanel card = new JPanel(new GridLayout(1, 3, 16, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        card.add(buildInfoField("Standard Late Fee Rate", "₱500 / day", false));
        card.add(buildInfoField("Overdue Rentals",        "—", false));
        card.add(buildInfoField("Total Late Fees Owed",   "—", false));
        return card;
    }

    // =========================================================
    //  REPORTS PANEL
    // =========================================================
    private JPanel buildReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel titleStack = buildPageHeader("Reports",
            "Generate reports filtered by date range.");

        // ── Date filter row ──────────────────────────────────
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setOpaque(false);
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterRow.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel fromLbl = new JLabel("From:");
        fromLbl.setFont(UIAssets.FONT_H3);
        JTextField fromField = buildInlineField("YYYY-MM-DD", 130);

        JLabel toLbl = new JLabel("To:");
        toLbl.setFont(UIAssets.FONT_H3);
        JTextField toField = buildInlineField("YYYY-MM-DD", 130);

        JButton genBtn = buildActionButton("Generate", UIAssets.CLR_BLUE, () ->
            JOptionPane.showMessageDialog(this, "Report generation — coming soon."));

        filterRow.add(fromLbl); filterRow.add(fromField);
        filterRow.add(toLbl);   filterRow.add(toField);
        filterRow.add(genBtn);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(filterRow);

        // ── Revenue Report ───────────────────────────────────
        body.add(buildSectionLabel("Revenue Report"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildTableSection(
            new String[]{ "Period", "Total Rentals", "Gross Revenue", "Late Fees", "Net Revenue" },
            new Object[][]{}));
        body.add(Box.createVerticalStrut(24));

        // ── Popularity Report ────────────────────────────────
        body.add(buildSectionLabel("Vehicle Popularity"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildTableSection(
            new String[]{ "Rank", "Vehicle", "Plate No.", "Category", "Times Rented", "Total Revenue" },
            new Object[][]{}));
        body.add(Box.createVerticalStrut(24));

        // ── Delinquency Report ───────────────────────────────
        body.add(buildSectionLabel("Delinquency Report"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildTableSection(
            new String[]{ "Customer", "Email", "Late Returns", "Total Late Fees", "Last Offence" },
            new Object[][]{}));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(titleStack, BorderLayout.NORTH);
        panel.add(scroll,     BorderLayout.CENTER);
        return panel;
    }

    // =========================================================
    //  ACCOUNT PANEL
    // =========================================================
    private JPanel buildAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel titleStack = buildPageHeader("Account", "Manage your admin profile and security settings.");

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Profile Details ──────────────────────────────────
        body.add(buildSettingsGroup("Profile Details", buildProfileRows()));
        body.add(Box.createVerticalStrut(24));

        // ── Security ─────────────────────────────────────────
        body.add(buildSettingsGroup("Security", buildSecurityRows()));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(titleStack, BorderLayout.NORTH);
        panel.add(scroll,     BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProfileRows() {
        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName() : "Administrator";
        String email = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getEmail() : "admin@carrental.com";

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Full Name",      displayName, buildEditableField(displayName)));
        rows.add(buildSettingRowDivider());
        rows.add(buildSettingRow("Contact Email",  email,       buildEditableField(email)));
        rows.add(buildSettingRowDivider());
        rows.add(buildSettingRow("Role",           "Administrator", new JLabel("Administrator") {{
            setFont(UIAssets.FONT_BODY);
            setForeground(UIAssets.getTextSecondary());
        }}));
        return rows;
    }

    private JPanel buildSecurityRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(0, 20, 16, 20));

        JLabel hint = new JLabel("Enter your current password before setting a new one.");
        hint.setFont(UIAssets.FONT_SMALL);
        hint.setForeground(UIAssets.getTextSecondary());
        hint.setBorder(new EmptyBorder(16, 0, 12, 0));
        rows.add(hint);

        rows.add(buildFieldLabelRow("Current Password"));
        rows.add(Box.createVerticalStrut(4));
        rows.add(buildPasswordRow());
        rows.add(Box.createVerticalStrut(10));
        rows.add(buildFieldLabelRow("New Password"));
        rows.add(Box.createVerticalStrut(4));
        rows.add(buildPasswordRow());
        rows.add(Box.createVerticalStrut(10));
        rows.add(buildFieldLabelRow("Confirm New Password"));
        rows.add(Box.createVerticalStrut(4));
        rows.add(buildPasswordRow());
        rows.add(Box.createVerticalStrut(16));

        JButton saveBtn = buildActionButton("Update Password", UIAssets.CLR_BLUE, () ->
            JOptionPane.showMessageDialog(this, "Password update — coming soon."));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        rows.add(btnRow);
        return rows;
    }

    private JLabel buildFieldLabelRow(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPasswordField buildPasswordRow() {
        JPasswordField field = new JPasswordField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(6, 12, 6, 12)));
        return field;
    }

    private JTextField buildEditableField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(UIAssets.FONT_BODY);
        field.setPreferredSize(new Dimension(240, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(4, 10, 4, 10)));
        return field;
    }

    // =========================================================
    //  SETTINGS PANEL
    // =========================================================
    private JPanel buildSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel titleStack = buildPageHeader("Settings", "Manage application preferences and configuration.");

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(buildSettingsGroup("Appearance",     buildAppearanceRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildSettingsGroup("Business Rules", buildBusinessRulesRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildSettingsGroup("Database",       buildDatabaseRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildSettingsGroup("System",         buildSystemRows()));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(titleStack, BorderLayout.NORTH);
        panel.add(scroll,     BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAppearanceRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow(
            UIAssets.isDark() ? "Dark Mode" : "Light Mode",
            "Switch the application color theme",
            buildThemeTogglePill()));
        return rows;
    }

    private JPanel buildBusinessRulesRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Standard Late Fee Rate",
            "Fee applied per day when a vehicle is returned late",
            buildInlineField("₱500.00", 110)));
        rows.add(buildSettingRowDivider());
        rows.add(buildSettingRow("Default Tax Rate",
            "Percentage applied to all rental transactions",
            buildInlineField("12%", 110)));
        return rows;
    }

    // ─────────────────────────────────────────────────────────
    // isDatabaseConnected()
    //   Extracted helper so the result is a final local variable
    //   in buildDatabaseRows(). Java requires variables captured
    //   inside anonymous inner classes (the JLabel paintComponent
    //   override) to be effectively final — a boolean that is
    //   first declared false and then reassigned inside a try/catch
    //   does not qualify. Extracting the check here fixes that.
    // ─────────────────────────────────────────────────────────
    private boolean isDatabaseConnected() {
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            return conn != null && !conn.isClosed();
        } catch (Exception ignored) {
            return false;
        }
    }

    private JPanel buildDatabaseRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        // final is required here because `connected` is captured
        // inside the anonymous paintComponent override below.
        final boolean connected = isDatabaseConnected();

        Color statusColor = connected ? UIAssets.CLR_GREEN : UIAssets.CLR_RED;
        String statusText = connected ? "Connected" : "Disconnected";

        JLabel statusBadge = new JLabel("  " + statusText + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(connected ? UIAssets.CLR_GREEN_LIGHT : UIAssets.CLR_RED_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBadge.setFont(UIAssets.FONT_SMALL);
        statusBadge.setForeground(statusColor);
        statusBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        statusBadge.setOpaque(false);

        rows.add(buildSettingRow("MySQL Connection",
            "car_rental_db @ localhost", statusBadge));
        return rows;
    }

    private JPanel buildSystemRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Language", "English (Default)", buildComingSoonBadge()));
        rows.add(buildSettingRowDivider());
        rows.add(buildSettingRow("Application Version", "v1.0.0 — Car Rental System",
            new JLabel("") {{ setPreferredSize(new Dimension(1, 1)); }}));
        return rows;
    }

    private JButton buildThemeTogglePill() {
        themeToggleBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dark = UIAssets.isDark();
                int w = getWidth(), h = getHeight();
                g2.setColor(dark ? UIAssets.CLR_BLUE : new Color(180, 180, 180));
                g2.fillRoundRect(0, 0, w, h, h, h);
                int knobSize = h - 6;
                int knobX    = dark ? w - knobSize - 3 : 3;
                g2.setColor(Color.WHITE);
                g2.fillOval(knobX, 3, knobSize, knobSize);
                g2.dispose();
            }
        };
        themeToggleBtn.setPreferredSize(new Dimension(52, 28));
        themeToggleBtn.setBorderPainted(false);
        themeToggleBtn.setContentAreaFilled(false);
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleBtn.setToolTipText("Toggle dark / light mode");
        themeToggleBtn.addActionListener(e -> UIAssets.toggleTheme());
        return themeToggleBtn;
    }

    private JTextField buildInlineField(String value, int width) {
        JTextField field = new JTextField(value);
        field.setFont(UIAssets.FONT_BODY);
        field.setPreferredSize(new Dimension(width, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(4, 10, 4, 10)));
        return field;
    }

    // =========================================================
    //  SHARED PANEL BUILDERS
    // =========================================================
    private JPanel buildPageHeader(String title, String subtitle) {
        JPanel stack = new JPanel(new GridLayout(2, 1, 0, 2));
        stack.setOpaque(false);
        stack.setBorder(new EmptyBorder(0, 0, 24, 0));
        JLabel pageTitle = new JLabel(title);
        pageTitle.setFont(UIAssets.FONT_TITLE);
        JLabel pageSub = new JLabel(subtitle);
        pageSub.setFont(UIAssets.FONT_SUBTITLE);
        stack.add(pageTitle);
        stack.add(pageSub);
        return stack;
    }

    private JPanel buildTableSection(String[] cols, Object[][] rows) {
        JTable table = buildStyledTable(cols, rows);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        if (rows == null || rows.length == 0) {
            JLabel empty = new JLabel("No records found.", SwingConstants.CENTER);
            empty.setFont(UIAssets.FONT_SUBTITLE);
            empty.setForeground(UIAssets.getTextSecondary());
            empty.setBorder(new EmptyBorder(40, 0, 40, 0));
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
            wrapper.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
            wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
            wrapper.add(empty, BorderLayout.CENTER);
            return wrapper;
        }
        return wrapInPanel(scroll);
    }

    private JPanel wrapInPanel(JComponent comp) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSettingsGroup(String groupTitle, JPanel rows) {
        JPanel group = new JPanel(new BorderLayout());
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        group.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel groupLbl = new JLabel(groupTitle);
        groupLbl.setFont(UIAssets.FONT_H2);
        groupLbl.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(4, 0, 4, 0)
        ));
        card.add(rows);

        group.add(groupLbl, BorderLayout.NORTH);
        group.add(card,     BorderLayout.CENTER);
        return group;
    }

    private JPanel buildSettingRow(String label, String subtitle, JComponent control) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel textSide = new JPanel(new GridLayout(2, 1, 0, 3));
        textSide.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIAssets.FONT_H3);
        JLabel sub = new JLabel(subtitle);
        sub.setFont(UIAssets.FONT_SMALL);
        sub.setForeground(UIAssets.getTextSecondary());
        textSide.add(lbl);
        textSide.add(sub);

        JPanel controlWrap = new JPanel(new GridBagLayout());
        controlWrap.setOpaque(false);
        controlWrap.add(control);

        row.add(textSide,    BorderLayout.WEST);
        row.add(controlWrap, BorderLayout.EAST);
        return row;
    }

    private JSeparator buildSettingRowDivider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setForeground(UIAssets.getBorder());
        return sep;
    }

    private JLabel buildComingSoonBadge() {
        JLabel badge = new JLabel("Coming Soon") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.CLR_YELLOW_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UIAssets.FONT_SMALL);
        badge.setForeground(UIAssets.CLR_YELLOW);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setOpaque(false);
        return badge;
    }

    private JPanel buildInfoField(String label, String value, boolean editable) {
        JPanel col = new JPanel(new GridLayout(2, 1, 0, 6));
        col.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIAssets.FONT_H3);
        JLabel val = new JLabel(value);
        val.setFont(UIAssets.FONT_BODY);
        val.setForeground(UIAssets.getTextSecondary());
        col.add(lbl);
        col.add(val);
        return col;
    }

    private JLabel buildSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIAssets.FONT_H2);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // =========================================================
    //  TABLE BUILDER
    // =========================================================
    private JTable buildStyledTable(String[] cols, Object[][] rows) {
        DefaultTableModel model = rows != null
            ? new DefaultTableModel(rows, cols) { @Override public boolean isCellEditable(int r, int c) { return false; } }
            : new DefaultTableModel(cols, 0)    { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable table = new JTable(model);
        table.setFont(UIAssets.FONT_BODY);
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UIAssets.getBorder());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(UIAssets.CLR_BLUE_LIGHT);
        table.setSelectionForeground(UIAssets.getTextPrimary());
        table.getTableHeader().setFont(UIAssets.FONT_H3);
        table.getTableHeader().setBackground(UIAssets.getSurface());
        table.getTableHeader().setForeground(UIAssets.getTextSecondary());
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()));
        table.getTableHeader().setReorderingAllowed(false);
        table.setBackground(UIAssets.getSurface());
        table.setForeground(UIAssets.getTextPrimary());
        return table;
    }

    // =========================================================
    //  CUSTOM CELL RENDERERS & EDITORS
    // =========================================================

    /** Renders a colored status badge — PENDING/yellow, VERIFIED/green, REJECTED/red */
    private class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            JLabel lbl = new JLabel(val == null ? "" : val.toString(), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(2, 4, getWidth()-4, getHeight()-8, getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lbl.setFont(UIAssets.FONT_SMALL);
            lbl.setOpaque(false);
            String s = val == null ? "" : val.toString();
            switch (s.toUpperCase()) {
                case "PENDING"  -> { lbl.setBackground(UIAssets.CLR_YELLOW_LIGHT); lbl.setForeground(UIAssets.CLR_YELLOW); }
                case "VERIFIED" -> { lbl.setBackground(UIAssets.CLR_GREEN_LIGHT);  lbl.setForeground(UIAssets.CLR_GREEN);  }
                case "REJECTED" -> { lbl.setBackground(UIAssets.CLR_RED_LIGHT);    lbl.setForeground(UIAssets.CLR_RED);    }
                default         -> { lbl.setBackground(UIAssets.getBorder());      lbl.setForeground(UIAssets.getTextSecondary()); }
            }
            return lbl;
        }
    }

    /** Renders Verify + Reject buttons in the drivers table action column */
    private class DriverActionRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            return buildDriverActionPanel();
        }
    }

    // ─────────────────────────────────────────────────────────
    // DriverActionEditor
    //   Cell editor that injects live Verify / Reject buttons
    //   into the Actions column of the drivers table.
    //
    //   Note: `table` is intentionally NOT stored as a field —
    //   it was flagged as unused because the editor only needs
    //   `model` to read the driver ID via getValueAt(row, 0).
    //   When Step 6 (DriverService wiring) is implemented, pass
    //   the driverId directly to DriverService.verify(driverId)
    //   and DriverService.reject(driverId).
    // ─────────────────────────────────────────────────────────
    private class DriverActionEditor extends DefaultCellEditor {
        private final DefaultTableModel model;

        DriverActionEditor(JTable table, DefaultTableModel model) {
            super(new JCheckBox());
            this.model = model;
            // `table` param kept in constructor signature so call-site
            // (buildDriversPanel) doesn't need to change when we wire
            // real DAO calls in Step 6.
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object val, boolean sel, int r, int c) {
            JPanel p = buildDriverActionPanel();
            ((JButton) p.getComponent(0)).addActionListener(e -> {
                fireEditingStopped();
                int driverId = (int) model.getValueAt(r, 0);
                JOptionPane.showMessageDialog(AdminDashboardGUI.this,
                    "Verifying driver ID " + driverId + " — DriverService.verify() coming soon.");
            });
            ((JButton) p.getComponent(1)).addActionListener(e -> {
                fireEditingStopped();
                int driverId = (int) model.getValueAt(r, 0);
                JOptionPane.showMessageDialog(AdminDashboardGUI.this,
                    "Rejecting driver ID " + driverId + " — DriverService.reject() coming soon.");
            });
            return p;
        }
    }

    private JPanel buildDriverActionPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        p.setOpaque(false);
        JButton verify = new JButton("Verify");
        verify.setFont(UIAssets.FONT_SMALL);
        verify.setForeground(Color.WHITE);
        verify.setBackground(UIAssets.CLR_GREEN);
        verify.setBorderPainted(false);
        verify.setFocusPainted(false);
        verify.setCursor(new Cursor(Cursor.HAND_CURSOR));
        verify.setBorder(new EmptyBorder(4, 10, 4, 10));
        JButton reject = new JButton("Reject");
        reject.setFont(UIAssets.FONT_SMALL);
        reject.setForeground(Color.WHITE);
        reject.setBackground(UIAssets.CLR_RED);
        reject.setBorderPainted(false);
        reject.setFocusPainted(false);
        reject.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reject.setBorder(new EmptyBorder(4, 10, 4, 10));
        p.add(verify);
        p.add(reject);
        return p;
    }

    /** Renders an Approve button for the payments verification table */
    private class ApproveButtonRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            JButton btn = new JButton("Approve Payment");
            btn.setFont(UIAssets.FONT_SMALL);
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIAssets.CLR_GREEN);
            btn.setBorderPainted(false);
            return btn;
        }
    }

    // ─────────────────────────────────────────────────────────
    // ApproveButtonEditor
    //   Cell editor for the Approve column in the Pending
    //   Verifications table. The `model` param is accepted in
    //   the constructor so the call-site stays consistent, but
    //   it is not stored as a field because the current stub
    //   action (JOptionPane) doesn't need to read row data.
    //   When PaymentService.approve(rentalId) is wired in,
    //   store model and use model.getValueAt(r, 0) for the ID.
    // ─────────────────────────────────────────────────────────
    private class ApproveButtonEditor extends DefaultCellEditor {

        ApproveButtonEditor(JTable table, DefaultTableModel model) {
            super(new JCheckBox());
            // model not stored yet — see note above
        }
        @Override public Component getTableCellEditorComponent(
                JTable t, Object val, boolean sel, int r, int c) {
            JButton btn = new JButton("Approve Payment");
            btn.setFont(UIAssets.FONT_SMALL);
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIAssets.CLR_GREEN);
            btn.setBorderPainted(false);
            btn.addActionListener(e -> {
                fireEditingStopped();
                JOptionPane.showMessageDialog(AdminDashboardGUI.this,
                    "Payment approved — PaymentService.approve() coming soon.");
            });
            return btn;
        }
    }

    // =========================================================
    //  THEME APPLICATION
    // =========================================================
    private void applyTheme() {
        if (topBar != null) {
            topBar.setBackground(UIAssets.getTitleBar());
            topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
                new EmptyBorder(0, 24, 0, 28)
            ));
        }
        if (nameLabel      != null) nameLabel.setForeground(Color.WHITE);
        if (roleLabel      != null) roleLabel.setForeground(new Color(160, 160, 160));
        if (activeTabLabel != null) activeTabLabel.setForeground(Color.WHITE);

        if (sidebar     != null) sidebar.setBackground(UIAssets.getSidebar());
        if (toggleBtn   != null) toggleBtn.setBackground(UIAssets.getSidebar());
        if (contentArea != null) contentArea.setBackground(UIAssets.getBg());
        if (overviewPanel  != null) overviewPanel.setBackground(UIAssets.getBg());
        if (settingsPanel  != null) settingsPanel.setBackground(UIAssets.getBg());
        if (themeToggleBtn != null) themeToggleBtn.repaint();

        if (topNavButtons != null)
            for (int i = 0; i < topNavButtons.length; i++)
                setNavActive(topNavButtons[i], i == activeTopIndex);
        if (botNavButtons != null)
            for (int i = 0; i < botNavButtons.length; i++)
                setNavActive(botNavButtons[i], i == activeBotIndex);
        if (logoutBtn != null) setNavActive(logoutBtn, false);

        repaint();
        revalidate();
    }

    // =========================================================
    //  LOGOUT
    // =========================================================
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out?",
            "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginGUI().setVisible(true);
            this.dispose();
        }
    }
}
