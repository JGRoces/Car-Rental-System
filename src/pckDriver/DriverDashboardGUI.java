package pckDriver;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckDatabase.CustomerDAO;
import pckDatabase.RentalDAO;
import pckModels.Car;
import pckModels.Customer;
import pckModels.Driver;
import pckModels.Rental;
import pckServices.AuthService;
import pckUtils.AppConfig;
import pckUtils.CustomTitleBar;
import pckUtils.SessionManager;
import pckUtils.UIAssets;
import pckMain.LoginGUI;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

public class DriverDashboardGUI extends JFrame {

    private static final String[] NAV_LABELS = { "Overview", "My Assignments", "Profile" };
    private static final String[] NAV_ICONS  = {
        AppConfig.ICON_NAV_OVERVIEW,
        AppConfig.ICON_NAV_MANAGEMENT,
        AppConfig.ICON_NAV_ACCOUNT
    };
    private static final String[] PANEL_KEYS = { "OVERVIEW", "ASSIGNMENTS", "PROFILE" };
    private static final int NAV_ICON_SIZE   = 16;

    private JPanel     sidebar, contentArea;
    private CardLayout cardLayout;
    private JButton[]  navButtons;
    private JButton    signOutBtn;
    private int        activeIdx = 0;
    private Driver     currentDriver;

    public DriverDashboardGUI() {
        loadDriver();
        initWindow();
        initComponents();
        UIAssets.addListener(this::applyTheme);
    }

    private void loadDriver() {
        if (SessionManager.isLoggedIn()) {
            currentDriver = new pckDatabase.DriverDAO()
                .getDriverByUserId(SessionManager.getCurrentUser().getUserId());
        }
    }

    private void initWindow() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 880);
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        add(new CustomTitleBar(this, "Car Rental System — Driver Portal", true), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(UIAssets.getBg());
        body.add(buildTopBar(),      BorderLayout.NORTH);
        body.add(buildSidebar(),     BorderLayout.WEST);
        body.add(buildContentArea(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Top Bar ───────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIAssets.getSurface());
        bar.setPreferredSize(new Dimension(0, 64));
        bar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
            new EmptyBorder(0, 28, 0, 32)
        ));

        String name = currentDriver != null ? currentDriver.getFullName() : "Driver";
        String initial = name.substring(0, 1).toUpperCase();

        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.CLR_GREEN);
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (38 - fm.stringWidth(initial)) / 2,
                    (38 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(38, 38));
        avatar.setOpaque(false);

        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 1));
        nameStack.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(UIAssets.FONT_H3);
        nameLbl.setForeground(UIAssets.getTextPrimary());
        JLabel roleLbl = new JLabel(currentDriver != null ? "Driver — " + currentDriver.getStatus() : "Driver");
        roleLbl.setFont(UIAssets.FONT_SMALL);
        roleLbl.setForeground(currentDriver != null && currentDriver.isVerified()
            ? UIAssets.CLR_GREEN : UIAssets.CLR_YELLOW);
        nameStack.add(nameLbl); nameStack.add(roleLbl);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);
        left.add(avatar); left.add(nameStack);

        JLabel tabLbl = new JLabel(NAV_LABELS[0]);
        tabLbl.setFont(UIAssets.FONT_H1);
        tabLbl.setForeground(UIAssets.getTextPrimary());

        JPanel lw = new JPanel(new GridBagLayout()); lw.setOpaque(false); lw.add(left);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(tabLbl);
        bar.add(lw, BorderLayout.WEST);
        bar.add(rw, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIAssets.getSurface());
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 0, 1, UIAssets.getBorder()),
            new EmptyBorder(16, 0, 16, 0)
        ));

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildNavButton(NAV_LABELS[i], NAV_ICONS[i], i == 0);
            navButtons[i].addActionListener(e -> selectNav(idx));
            sidebar.add(navButtons[i]);
            sidebar.add(Box.createVerticalStrut(2));
        }

        sidebar.add(Box.createVerticalGlue());

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        signOutBtn = buildNavButton("Sign Out", AppConfig.ICON_NAV_SIGNOUT, false);
        signOutBtn.setForeground(UIAssets.CLR_RED);
        signOutBtn.addActionListener(e -> handleSignOut());
        sidebar.add(signOutBtn);
        return sidebar;
    }

    private JButton buildNavButton(String label, String iconPath, boolean active) {
        ImageIcon iconInactive = AdminUIHelper.loadIcon(iconPath, UIAssets.getTextSecondary(), NAV_ICON_SIZE);
        ImageIcon iconActive   = AdminUIHelper.loadIcon(iconPath, Color.WHITE, NAV_ICON_SIZE);
        ImageIcon iconHover    = AdminUIHelper.loadIcon(iconPath, UIAssets.CLR_GREEN, NAV_ICON_SIZE);

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
        btn.putClientProperty("icon.inactive", iconInactive);
        btn.putClientProperty("icon.active",   iconActive);
        btn.putClientProperty("icon.hover",    iconHover);
        btn.setIcon(active ? iconActive : iconInactive);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setIconTextGap(10);
        btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        btn.setBackground(active ? UIAssets.CLR_GREEN : UIAssets.getSurface());
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(0, 20, 0, 12));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(220, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_GREEN)) {
                    btn.setBackground(UIAssets.CLR_GREEN_LIGHT);
                    btn.setForeground(UIAssets.CLR_GREEN);
                    Object ico = btn.getClientProperty("icon.hover");
                    if (ico instanceof ImageIcon i) btn.setIcon(i);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_GREEN)) {
                    btn.setBackground(UIAssets.getSurface());
                    btn.setForeground(UIAssets.getTextSecondary());
                    Object ico = btn.getClientProperty("icon.inactive");
                    if (ico instanceof ImageIcon i) btn.setIcon(i);
                }
            }
        });
        return btn;
    }

    private void selectNav(int index) {
        activeIdx = index;
        for (int i = 0; i < navButtons.length; i++) {
            boolean active = (i == index);
            navButtons[i].setBackground(active ? UIAssets.CLR_GREEN : UIAssets.getSurface());
            navButtons[i].setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
            navButtons[i].setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
            String key = active ? "icon.active" : "icon.inactive";
            Object ico = navButtons[i].getClientProperty(key);
            if (ico instanceof ImageIcon img) navButtons[i].setIcon(img);
        }
        cardLayout.show(contentArea, PANEL_KEYS[index]);
    }

    // ── Content Area ──────────────────────────────────────────
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIAssets.getBg());
        contentArea.add(buildOverviewPanel(),     PANEL_KEYS[0]);
        contentArea.add(buildAssignmentsPanel(),  PANEL_KEYS[1]);
        contentArea.add(buildProfilePanel(),      PANEL_KEYS[2]);
        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    // ── Overview ──────────────────────────────────────────────
    private JPanel buildOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));

        String name = currentDriver != null ? currentDriver.getFullName() : "Driver";
        panel.add(AdminUIHelper.buildPageHeader(
            "Welcome, " + name + "!",
            "Here's a summary of your driver account."
        ), BorderLayout.NORTH);

        // Status banner
        String status = currentDriver != null ? currentDriver.getStatus() : "UNKNOWN";
        Color statusColor = switch (status) {
            case "VERIFIED" -> UIAssets.CLR_GREEN;
            case "PENDING"  -> UIAssets.CLR_YELLOW;
            default         -> UIAssets.CLR_RED;
        };
        Color statusLight = switch (status) {
            case "VERIFIED" -> UIAssets.CLR_GREEN_LIGHT;
            case "PENDING"  -> UIAssets.CLR_YELLOW_LIGHT;
            default         -> UIAssets.CLR_RED_LIGHT;
        };
        String statusMsg = switch (status) {
            case "VERIFIED" -> "Your account is verified. You can receive rental assignments.";
            case "PENDING"  -> "Your account is pending admin verification. Please wait.";
            default         -> "Your account has been rejected. Please contact support.";
        };

        JPanel statusCard = new JPanel(new GridLayout(2, 1, 0, 4));
        statusCard.setBackground(statusLight);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(statusColor, 1, true),
            new EmptyBorder(14, 20, 14, 20)
        ));
        JLabel statusLbl = new JLabel("Account Status: " + status);
        statusLbl.setFont(UIAssets.FONT_H2);
        statusLbl.setForeground(statusColor);
        JLabel statusSubLbl = new JLabel(statusMsg);
        statusSubLbl.setFont(UIAssets.FONT_BODY);
        statusSubLbl.setForeground(statusColor);
        statusCard.add(statusLbl);
        statusCard.add(statusSubLbl);

        // Stat counts
        int totalAssignments = 0, activeAssignments = 0, completedAssignments = 0;
        if (currentDriver != null) {
            List<Rental> myRentals = new RentalDAO().getRentalsByDriverId(currentDriver.getDriverId());
            totalAssignments     = myRentals.size();
            activeAssignments    = (int) myRentals.stream().filter(r -> r.getStatus().equals("ACTIVE")).count();
            completedAssignments = (int) myRentals.stream().filter(r -> r.getStatus().equals("COMPLETED")).count();
        }

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(AdminUIHelper.buildStatCard("My Assignments", String.valueOf(totalAssignments),     "Total assigned to me", UIAssets.CLR_BLUE,  UIAssets.CLR_BLUE_LIGHT));
        stats.add(AdminUIHelper.buildStatCard("Active",         String.valueOf(activeAssignments),    "Currently active",     UIAssets.CLR_GREEN, UIAssets.CLR_GREEN_LIGHT));
        stats.add(AdminUIHelper.buildStatCard("Completed",      String.valueOf(completedAssignments), "Finished trips",       UIAssets.CLR_BLUE,  UIAssets.CLR_BLUE_LIGHT));

        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(statusCard, BorderLayout.NORTH);
        body.add(stats,      BorderLayout.CENTER);

        // Wrap body in NORTH so stats don't stretch vertically
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(body, BorderLayout.NORTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // ── Assignments ───────────────────────────────────────────
    private JPanel buildAssignmentsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));
        panel.add(AdminUIHelper.buildPageHeader("My Assignments",
            "Rentals assigned to you."), BorderLayout.NORTH);

        String[] cols = { "Rental #", "Customer", "Vehicle", "Start Date", "End Date", "Total (₱)", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (currentDriver != null) {
            List<Rental> rentals = new RentalDAO().getRentalsByDriverId(currentDriver.getDriverId());
            CustomerDAO customerDAO = new CustomerDAO();
            CarDAO      carDAO      = new CarDAO();
            for (Rental r : rentals) {
                Customer cust = customerDAO.getCustomerById(r.getCustomerId());
                String custName = cust != null ? cust.getFullName() : "Customer #" + r.getCustomerId();
                Car car = carDAO.getCarById(r.getCarId());
                String carName = car != null
                    ? car.getBrand() + " " + car.getModel() + " (" + car.getPlateNumber() + ")"
                    : "Car #" + r.getCarId();
                model.addRow(new Object[]{
                    r.getRentalId(), custName, carName,
                    r.getStartDate(), r.getEndDate(),
                    String.format("%,.2f", r.getTotalAmount()), r.getStatus()
                });
            }
        }

        JTable table = AdminUIHelper.buildStyledTable(cols, null);
        table.setModel(model);
        table.getColumnModel().getColumn(6).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
        panel.add(AdminUIHelper.buildTableScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // ── Profile ───────────────────────────────────────────────
    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(32, 36, 32, 36));
        panel.add(AdminUIHelper.buildPageHeader("Profile",
            "Your driver account details."), BorderLayout.NORTH);

        JPanel card = AdminUIHelper.buildCard(24);
        card.setLayout(new GridLayout(0, 2, 16, 12));

        String[][] fields = {
            { "Full Name",       currentDriver != null ? currentDriver.getFullName()      : "\u2014" },
            { "Email",           currentDriver != null ? currentDriver.getEmail()          : "\u2014" },
            { "Phone",           currentDriver != null ? currentDriver.getPhone()          : "\u2014" },
            { "License Number",  currentDriver != null ? currentDriver.getLicenseNumber()  : "\u2014" },
            { "License Expiry",  currentDriver != null ? currentDriver.getLicenseExpiry()  : "\u2014" },
            { "Vehicle Type",    currentDriver != null ? currentDriver.getVehicleType()    : "\u2014" },
            { "Status",          currentDriver != null ? currentDriver.getStatus()         : "\u2014" },
            { "Verified At",     currentDriver != null && currentDriver.getVerifiedAt() != null
                                    ? currentDriver.getVerifiedAt() : "Not yet verified" },
        };
        for (String[] f : fields) {
            JLabel key = new JLabel(f[0]);
            key.setFont(UIAssets.FONT_H3);
            key.setForeground(UIAssets.getTextSecondary());
            JLabel val = new JLabel(f[1]);
            val.setFont(UIAssets.FONT_BODY);
            val.setForeground(UIAssets.getTextPrimary());
            card.add(key); card.add(val);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.NORTH);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void applyTheme() { repaint(); revalidate(); }

    private void handleSignOut() {
        int c = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginGUI().setVisible(true);
            dispose();
        }
    }
}
