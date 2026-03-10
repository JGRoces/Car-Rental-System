package pckAdmin;

import pckAdmin.panels.*;
import pckMain.LoginGUI;
import pckServices.AuthService;
import pckUtils.AppConfig;
import pckAdmin.shared.*;
import pckUtils.CustomTitleBar;
import pckUtils.SessionManager;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * AdminDashboardGUI.java
 * ─────────────────────────────────────────────────────────────
 * SHELL ONLY — responsible for exactly four things:
 *   div1 → CustomTitleBar  (light mode, spans full width)
 *   div2 → TopBar          (white, 64px, admin name + active tab)
 *   div3 → Sidebar         (light bg #efefef, 220/60px collapsible)
 *   div4 → Content area    (CardLayout, each card = a panel class)
 *
 * This file never builds panel content. Each tab/panel lives in
 * its own class under pckAdmin/panels/ and pckAdmin/tabs/.
 * To add a new tab: add its label/key to the arrays, instantiate
 * the panel class in buildContentArea(), done.
 *
 * Design: fully light-mode — matches LoginGUI / SignUp screens.
 *   Sidebar bg    → UIAssets.getBg()       #efefef
 *   TopBar bg     → UIAssets.getSurface()  #ffffff
 *   Content bg    → UIAssets.getBg()       #efefef
 *   Active nav    → CLR_BLUE fill, white text
 *   Inactive nav  → getTextSecondary() text, transparent fill
 *   Hover nav     → CLR_BLUE_LIGHT bg
 */
public class AdminDashboardGUI extends JFrame {

    // ── Sidebar dimensions ────────────────────────────────────
    private static final int SIDEBAR_W_EXPANDED  = 220;
    private static final int SIDEBAR_W_COLLAPSED = 60;
    private boolean          sidebarExpanded      = true;

    // ── Top nav — Overview / Management / Payments / Reports ──
    private static final String[] TOP_PLAIN  = { "Overview", "Management", "Payments", "Reports" };
    private static final String[] TOP_LABELS = { "Overview", "Management", "Payments", "Reports" };
    private static final String[] TOP_ICONS  = {
        AppConfig.ICON_NAV_OVERVIEW,
        AppConfig.ICON_NAV_MANAGEMENT,
        AppConfig.ICON_NAV_PAYMENTS,
        AppConfig.ICON_NAV_REPORTS
    };
    private static final String[] TOP_KEYS   = { "OVERVIEW", "MANAGEMENT", "PAYMENTS", "REPORTS" };

    // ── Bottom nav — Account / Settings ──────────────────────
    private static final String[] BOT_PLAIN  = { "Account", "Settings" };
    private static final String[] BOT_LABELS = { "Account", "Settings" };
    private static final String[] BOT_ICONS  = {
        AppConfig.ICON_NAV_ACCOUNT,
        AppConfig.ICON_NAV_SETTINGS
    };
    private static final String[] BOT_KEYS   = { "ACCOUNT", "SETTINGS" };

    // Icon size for nav buttons — 16px sits cleanly in a 44px row
    private static final int NAV_ICON_SIZE = 16;

    // ── Component references ──────────────────────────────────
    private JPanel     sidebar;
    private JPanel     contentArea;
    private JPanel     topBar;
    private CardLayout cardLayout;

    private JButton[]  topNavBtns;   // Overview, Management, Payments, Reports
    private JButton[]  botNavBtns;   // Account, Settings
    private JButton    toggleBtn;    // sidebar collapse arrow
    private JButton    signOutBtn;

    private int activeTopIdx = 0;   // which top nav is highlighted
    private int activeBotIdx = -1;  // which bot nav is highlighted (-1 = none)

    private JLabel nameLabel;       // admin display name in top bar
    private JLabel activeTabLabel;  // current tab name, right side of top bar

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
        setMinimumSize(new Dimension(1100, 640));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        // div1 — light mode title bar (white, matches LoginGUI)
        add(new CustomTitleBar(this, "Car Rental System — Admin Dashboard", true),
            BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UIAssets.getBg());

        topBar = buildTopBar();              // div2
        body.add(topBar,             BorderLayout.NORTH);
        body.add(buildSidebar(),     BorderLayout.WEST);  // div3
        body.add(buildContentArea(), BorderLayout.CENTER); // div4

        add(body, BorderLayout.CENTER);
    }

    // =========================================================
    //  TOP BAR  (div2) — white 64px strip
    //  LEFT:  yellow avatar circle + admin name + role badge
    //  RIGHT: name of the currently active panel
    // =========================================================
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIAssets.getSurface());
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
            new EmptyBorder(0, 28, 0, 32)
        ));

        String displayName = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName() : "Administrator";
        String initial = displayName.substring(0, 1).toUpperCase();

        // LEFT — yellow circle avatar + name + "Admin" label
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        // Yellow avatar circle (admin color accent = yellow)
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.CLR_YELLOW);
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                    (38 - fm.stringWidth(initial)) / 2,
                    (38 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 1));
        nameStack.setOpaque(false);
        nameLabel = new JLabel(displayName);
        nameLabel.setFont(UIAssets.FONT_H3);
        nameLabel.setForeground(UIAssets.getTextPrimary());
        JLabel roleLabel = new JLabel("Administrator");
        roleLabel.setFont(UIAssets.FONT_SMALL);
        roleLabel.setForeground(UIAssets.getTextSecondary());
        nameStack.add(nameLabel);
        nameStack.add(roleLabel);

        left.add(avatar);
        left.add(nameStack);

        // RIGHT — active tab name
        activeTabLabel = new JLabel(TOP_PLAIN[0]);
        activeTabLabel.setFont(UIAssets.FONT_H1);
        activeTabLabel.setForeground(UIAssets.getTextPrimary());

        // Wrap in GridBagLayout panels to vertically center both sides
        JPanel lw = new JPanel(new GridBagLayout()); lw.setOpaque(false); lw.add(left);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(activeTabLabel);
        bar.add(lw, BorderLayout.WEST);
        bar.add(rw, BorderLayout.EAST);
        return bar;
    }

    // =========================================================
    //  SIDEBAR  (div3) — light #efefef background
    //  TOP:    collapse toggle + nav items
    //  BOTTOM: Account, Settings, divider, Sign Out
    // =========================================================
    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(SIDEBAR_W_EXPANDED, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        // White — same surface color as TopBar for visual continuity
        sidebar.setBackground(UIAssets.getSurface());
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 0, 1, UIAssets.getBorder()),
            new EmptyBorder(12, 0, 16, 0)
        ));

        // Collapse toggle — bold "<" left-aligned, same indent as nav items
        toggleBtn = buildToggleButton();
        sidebar.add(toggleBtn);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(10));

        // Top nav items — Overview, Management, Payments, Reports
        topNavBtns = new JButton[TOP_LABELS.length];
        for (int i = 0; i < TOP_LABELS.length; i++) {
            final int idx = i;
            topNavBtns[i] = buildNavButton(TOP_LABELS[i], TOP_ICONS[i], i == 0);
            topNavBtns[i].addActionListener(e -> selectTopNav(idx));
            sidebar.add(topNavBtns[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        // Push bottom items down
        sidebar.add(Box.createVerticalGlue());

        // Bottom nav items — Account, Settings
        botNavBtns = new JButton[BOT_LABELS.length];
        for (int i = 0; i < BOT_LABELS.length; i++) {
            final int idx = i;
            botNavBtns[i] = buildNavButton(BOT_LABELS[i], BOT_ICONS[i], false);
            botNavBtns[i].addActionListener(e -> selectBotNav(idx));
            sidebar.add(botNavBtns[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        // Divider + Sign Out pinned at bottom
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(buildSidebarDivider());
        sidebar.add(Box.createVerticalStrut(8));

        // Sign Out — red foreground, same nav button shape
        // setForeground() directly on the button — no child JLabel lookup needed
        signOutBtn = buildNavButton("Sign Out", AppConfig.ICON_NAV_SIGNOUT, false);
        // Override all Sign Out colors to red variants — never blue
        signOutBtn.setForeground(UIAssets.CLR_RED);
        signOutBtn.putClientProperty("restoreFg", UIAssets.CLR_RED);
        signOutBtn.putClientProperty("hoverBg",   UIAssets.CLR_RED_LIGHT);
        signOutBtn.putClientProperty("hoverFg",   UIAssets.CLR_RED);
        // Red inactive icon + red hover icon — both tinted from the same PNG
        ImageIcon redIcon      = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIGNOUT, UIAssets.CLR_RED,       NAV_ICON_SIZE);
        ImageIcon redHoverIcon = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIGNOUT, UIAssets.CLR_RED.darker(), NAV_ICON_SIZE);
        if (redIcon      != null) { signOutBtn.setIcon(redIcon);
                                    signOutBtn.putClientProperty("icon.inactive", redIcon); }
        if (redHoverIcon != null) { signOutBtn.putClientProperty("icon.hover",    redHoverIcon); }
        signOutBtn.addActionListener(e -> handleSignOut());
        sidebar.add(signOutBtn);

        return sidebar;
    }

    private JButton buildToggleButton() {
        // PNG icon version — sidebar-close.png shown when expanded (click to collapse),
        // sidebar-open.png shown when collapsed (click to expand).
        // Icons tinted to getTextSecondary(), blue on hover.
        // Stored as client properties for instant swap in toggleSidebar().
        ImageIcon closeIcon      = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIDEBAR_CLOSE, UIAssets.getTextSecondary(), NAV_ICON_SIZE);
        ImageIcon openIcon       = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIDEBAR_OPEN,  UIAssets.getTextSecondary(), NAV_ICON_SIZE);
        ImageIcon closeIconHover = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIDEBAR_CLOSE, UIAssets.CLR_BLUE,           NAV_ICON_SIZE);
        ImageIcon openIconHover  = AdminUIHelper.loadIcon(
            AppConfig.ICON_NAV_SIDEBAR_OPEN,  UIAssets.CLR_BLUE,           NAV_ICON_SIZE);

        JButton btn = new JButton(closeIcon); // starts expanded → show close icon
        btn.putClientProperty("icon.close",       closeIcon);
        btn.putClientProperty("icon.open",        openIcon);
        btn.putClientProperty("icon.close.hover", closeIconHover);
        btn.putClientProperty("icon.open.hover",  openIconHover);

        btn.setHorizontalAlignment(SwingConstants.RIGHT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setBorder(new EmptyBorder(0, 12, 0, 14));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                String key = sidebarExpanded ? "icon.close.hover" : "icon.open.hover";
                Object ico = btn.getClientProperty(key);
                if (ico instanceof ImageIcon i) btn.setIcon(i);
            }
            @Override public void mouseExited(MouseEvent e) {
                String key = sidebarExpanded ? "icon.close" : "icon.open";
                Object ico = btn.getClientProperty(key);
                if (ico instanceof ImageIcon i) btn.setIcon(i);
            }
        });
        btn.addActionListener(e -> toggleSidebar());
        return btn;
    }

    /**
     * Nav button — used for every sidebar item (top nav, bottom nav, Sign Out).
     *
     * Icon + text layout — icon on the left, text on the right, 10px gap.
     * Three icon variants are pre-loaded at build time and stored as client
     * properties so state changes are instant swaps with no disk reads:
     *   "icon.inactive" → gray  (getTextSecondary)   normal resting state
     *   "icon.active"   → white                       blue pill active state
     *   "icon.hover"    → CLR_BLUE                    hover highlight state
     *   "label"         → label string, used by toggleSidebar to restore text
     *
     * Collapsed sidebar: text = "", icon centered — acts as the visual hint.
     * Expanded sidebar:  icon left + text, LEFT aligned, 20px left padding.
     *
     * @param label    display text (e.g. "Overview")
     * @param iconPath path to the black PNG (e.g. AppConfig.ICON_NAV_OVERVIEW)
     * @param active   true = blue pill + white icon/text on build
     */
    private JButton buildNavButton(String label, String iconPath, boolean active) {
        // Pre-load all three tinted icon variants from the single black PNG.
        // loadIcon returns null gracefully if the file is missing — button
        // falls back to text-only without crashing.
        ImageIcon iconInactive = AdminUIHelper.loadIcon(iconPath,
                                     UIAssets.getTextSecondary(), NAV_ICON_SIZE);
        ImageIcon iconActive   = AdminUIHelper.loadIcon(iconPath,
                                     Color.WHITE,                 NAV_ICON_SIZE);
        ImageIcon iconHover    = AdminUIHelper.loadIcon(iconPath,
                                     UIAssets.CLR_BLUE,           NAV_ICON_SIZE);

        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                if (!getBackground().equals(UIAssets.getSurface())) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 8, 8);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };

        // Store variants + label as client properties for later retrieval
        btn.putClientProperty("icon.inactive", iconInactive);
        btn.putClientProperty("icon.active",   iconActive);
        btn.putClientProperty("icon.hover",    iconHover);
        btn.putClientProperty("label",         label);

        // Initial visual state
        btn.setIcon(active ? iconActive : iconInactive);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
        btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        btn.setBackground(active ? UIAssets.CLR_BLUE : UIAssets.getSurface());
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        // Equal left/right padding — symmetric sidebar items
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(SIDEBAR_W_EXPANDED, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Store per-button hover/restore colors so Sign Out uses red hover
        // while all other items use blue hover — no hardcoded color in listener.
        // These are set to blue defaults here; Sign Out overrides them after build.
        btn.putClientProperty("hoverBg",   UIAssets.CLR_BLUE_LIGHT);
        btn.putClientProperty("hoverFg",   UIAssets.CLR_BLUE);
        btn.putClientProperty("restoreFg", btn.getForeground());

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) {
                    Object hBg = btn.getClientProperty("hoverBg");
                    Object hFg = btn.getClientProperty("hoverFg");
                    btn.setBackground(hBg instanceof Color c ? c : UIAssets.CLR_BLUE_LIGHT);
                    btn.setForeground(hFg instanceof Color c ? c : UIAssets.CLR_BLUE);
                    Object ico = btn.getClientProperty("icon.hover");
                    if (ico instanceof ImageIcon i) btn.setIcon(i);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) {
                    btn.setBackground(UIAssets.getSurface());
                    Object rFg = btn.getClientProperty("restoreFg");
                    btn.setForeground(rFg instanceof Color c ? c : UIAssets.getTextSecondary());
                    Object ico = btn.getClientProperty("icon.inactive");
                    if (ico instanceof ImageIcon i) btn.setIcon(i);
                }
            }
        });
        return btn;
    }

    private JPanel buildSidebarDivider() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrapper.setBorder(new EmptyBorder(0, 12, 0, 12));
        JSeparator sep = new JSeparator();
        sep.setForeground(UIAssets.getBorder());
        wrapper.add(sep);
        return wrapper;
    }

    private void toggleSidebar() {
        sidebarExpanded = !sidebarExpanded;
        int w = sidebarExpanded ? SIDEBAR_W_EXPANDED : SIDEBAR_W_COLLAPSED;
        sidebar.setPreferredSize(new Dimension(w, 0));

        // Swap toggle icon: expanded → show close icon, collapsed → show open icon
        String toggleKey = sidebarExpanded ? "icon.close" : "icon.open";
        Object toggleIco = toggleBtn.getClientProperty(toggleKey);
        if (toggleIco instanceof ImageIcon i) toggleBtn.setIcon(i);

        // Expanded: icon LEFT + text RIGHT, left-aligned.
        // Collapsed: text hidden, icon centered — icon becomes the visual hint.
        int iconAlign = sidebarExpanded ? SwingConstants.LEFT   : SwingConstants.CENTER;
        int textPos   = sidebarExpanded ? SwingConstants.RIGHT  : SwingConstants.CENTER;

        JButton[][] allNavBtns = { topNavBtns, botNavBtns, { signOutBtn } };
        String[][]  allLabels  = { TOP_LABELS, BOT_LABELS, { "Sign Out" } };

        for (int g = 0; g < allNavBtns.length; g++) {
            for (int i = 0; i < allNavBtns[g].length; i++) {
                JButton btn = allNavBtns[g][i];
                if (btn == null) continue;
                btn.setText(sidebarExpanded ? allLabels[g][i] : "");
                btn.setHorizontalAlignment(iconAlign);
                btn.setHorizontalTextPosition(textPos);
                btn.setPreferredSize(new Dimension(w, 44));
            }
        }

        sidebar.revalidate();
        sidebar.repaint();
        revalidate();
        repaint();
    }

    // =========================================================
    //  NAV SELECTION LOGIC
    // =========================================================

    /** Activates a top nav item and deactivates all bottom nav items */
    private void selectTopNav(int index) {
        activeTopIdx = index;
        activeBotIdx = -1;
        for (int i = 0; i < topNavBtns.length; i++) setNavActive(topNavBtns[i], i == index);
        for (JButton btn : botNavBtns)               setNavActive(btn, false);
        if (activeTabLabel != null) activeTabLabel.setText(TOP_PLAIN[index]);
        cardLayout.show(contentArea, TOP_KEYS[index]);
    }

    /** Activates a bottom nav item and deactivates all top nav items */
    private void selectBotNav(int index) {
        activeBotIdx = index;
        activeTopIdx = -1;
        for (JButton btn : topNavBtns)               setNavActive(btn, false);
        for (int i = 0; i < botNavBtns.length; i++) setNavActive(botNavBtns[i], i == index);
        if (activeTabLabel != null) activeTabLabel.setText(BOT_PLAIN[index]);
        cardLayout.show(contentArea, BOT_KEYS[index]);
    }

    /**
     * Sets a nav button's visual active/inactive state.
     * Operates directly on the button — no child component lookup.
     *
     * Active:   CLR_BLUE pill bg, white bold text
     * Inactive: getSurface() (white) bg, getTextSecondary() plain text
     *
     * Sign Out is excluded from this method — its red color is set
     * once in buildSidebar() and preserved in applyTheme() separately.
     */
    private void setNavActive(JButton btn, boolean active) {
        btn.setBackground(active ? UIAssets.CLR_BLUE : UIAssets.getSurface());
        btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        // Swap pre-loaded icon variant — no disk read, instant
        String key = active ? "icon.active" : "icon.inactive";
        Object ico = btn.getClientProperty(key);
        if (ico instanceof ImageIcon i) btn.setIcon(i);
    }

    // =========================================================
    //  CONTENT AREA  (div4) — CardLayout
    //  Each card is a self-contained panel class.
    //  To add a new panel: add key to arrays above + add() here.
    // =========================================================
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIAssets.getBg());

        // Pass a navigation callback so panels can trigger tab switches
        // (e.g. Overview "View Reports" button → Reports panel)
        AdminNavCallback nav = new AdminNavCallback() {
            @Override public void goTo(String key) {
                for (int i = 0; i < TOP_KEYS.length; i++) {
                    if (TOP_KEYS[i].equals(key)) { selectTopNav(i); return; }
                }
                for (int i = 0; i < BOT_KEYS.length; i++) {
                    if (BOT_KEYS[i].equals(key)) { selectBotNav(i); return; }
                }
            }
        };

        contentArea.add(new OverviewPanel(nav),    TOP_KEYS[0]);
        contentArea.add(new ManagementPanel(nav),  TOP_KEYS[1]);
        contentArea.add(new PaymentsPanel(nav),    TOP_KEYS[2]);
        contentArea.add(new ReportsPanel(nav),     TOP_KEYS[3]);
        contentArea.add(new AccountPanel(nav),     BOT_KEYS[0]);
        contentArea.add(new SettingsPanel(nav),    BOT_KEYS[1]);

        cardLayout.show(contentArea, TOP_KEYS[0]);
        return contentArea;
    }

    /**
     * AdminNavCallback — passed to every panel so they can trigger
     * navigation without needing a reference to AdminDashboardGUI.
     * Implement as a lambda or anonymous class.
     *
     *   nav.goTo("REPORTS");   // switches to Reports panel
     *   nav.goTo("ACCOUNT");   // switches to Account panel
     */
    public interface AdminNavCallback {
        void goTo(String panelKey);
    }

    // =========================================================
    //  THEME APPLICATION
    //  Sidebar and TopBar are always light — only their
    //  text/border colors respond to theme changes.
    // =========================================================
    private void applyTheme() {
        if (topBar != null) {
            topBar.setBackground(UIAssets.getSurface());
            topBar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
                new EmptyBorder(0, 28, 0, 32)
            ));
        }
        if (nameLabel      != null) nameLabel.setForeground(UIAssets.getTextPrimary());
        if (activeTabLabel != null) activeTabLabel.setForeground(UIAssets.getTextPrimary());
        if (sidebar != null) {
            // White — stays in sync with TopBar surface color on theme change
            sidebar.setBackground(UIAssets.getSurface());
            sidebar.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, UIAssets.getBorder()),
                new EmptyBorder(12, 0, 16, 0)
            ));
        }
        if (contentArea != null) contentArea.setBackground(UIAssets.getBg());

        // Re-apply active/inactive state to all nav buttons
        if (topNavBtns != null)
            for (int i = 0; i < topNavBtns.length; i++)
                setNavActive(topNavBtns[i], i == activeTopIdx);
        if (botNavBtns != null)
            for (int i = 0; i < botNavBtns.length; i++)
                setNavActive(botNavBtns[i], i == activeBotIdx);
        // Sign Out: white bg, red text + red icon — never goes through setNavActive
        if (signOutBtn != null) {
            signOutBtn.setBackground(UIAssets.getSurface());
            signOutBtn.setForeground(UIAssets.CLR_RED);
            signOutBtn.putClientProperty("restoreFg", UIAssets.CLR_RED);
            signOutBtn.putClientProperty("hoverBg",   UIAssets.CLR_RED_LIGHT);
            signOutBtn.putClientProperty("hoverFg",   UIAssets.CLR_RED);
        }

        repaint();
        revalidate();
    }

    // =========================================================
    //  SIGN OUT
    // =========================================================
    private void handleSignOut() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to sign out?",
            "Sign Out",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (choice == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginGUI().setVisible(true);
            this.dispose();
        }
    }
}
