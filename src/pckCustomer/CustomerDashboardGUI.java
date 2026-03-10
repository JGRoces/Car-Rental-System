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
import pckServices.RentalService;
import pckUtils.SessionManager;
import pckDatabase.CustomerDAO;
import pckModels.Customer;
import pckModels.Rental;
import pckModels.Payment;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;
import java.util.List;

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
        contentArea.add(buildMyRentalsPanel(),      PANEL_KEYS[2]);
        contentArea.add(buildPaymentPanel(),        PANEL_KEYS[3]);

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
        // Rebuild My Rentals and Payment panels fresh each time to get latest data
        if (index == 2) {
            contentArea.remove(2);
            contentArea.add(buildMyRentalsPanel(null), PANEL_KEYS[2], 2);
        } else if (index == 3) {
            contentArea.remove(3);
            contentArea.add(buildPaymentPanel(), PANEL_KEYS[3], 3);
        }
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
    //  MY RENTALS PANEL
    // ====================================================
    private JPanel buildMyRentalsPanel() {
        return buildMyRentalsPanel(null);
    }

    private JPanel buildMyRentalsPanel(String statusFilter) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));

        String subtitle = statusFilter != null
            ? "Showing: " + statusFilter
            : "View your rental history and current bookings.";
        JLabel title = new JLabel("My Rentals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIAssets.getTextPrimary());
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel sub = new JLabel(subtitle);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(UIAssets.getTextSecondary());
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(title); header.add(sub);

        // Filter buttons row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        String[] filters = { "All", "PENDING", "ACTIVE", "COMPLETED", "CANCELLED" };
        String[] labels  = { "All", "Pending", "Active", "Completed", "Cancelled" };
        for (int i = 0; i < filters.length; i++) {
            final String f = filters[i];
            boolean active = (statusFilter == null && f.equals("All")) ||
                             (f.equals(statusFilter));
            JButton btn = new JButton(labels[i]);
            btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
            btn.setForeground(active ? Color.WHITE : UIAssets.getTextPrimary());
            btn.setBackground(active ? UIAssets.CLR_BLUE : new Color(220, 220, 220));
            btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> switchMyRentals(f.equals("All") ? null : f));
            filterRow.add(btn);
        }

        String[] cols = { "Rental ID", "Car ID", "Start Date", "End Date", "Total", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Customer customer = getLoggedInCustomer();
        if (customer != null) {
            List<Rental> rentals = RentalService.getCustomerRentals(customer.getCustomerId());
            for (Rental r : rentals) {
                if (statusFilter == null || r.getStatus().equalsIgnoreCase(statusFilter)) {
                    model.addRow(new Object[]{ r.getRentalId(), r.getCarId(),
                        r.getStartDate(), r.getEndDate(),
                        "\u20b1" + String.format("%,.2f", r.getTotalAmount()), r.getStatus() });
                }
            }
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setBackground(Color.WHITE);
        table.setForeground(UIAssets.getTextPrimary());
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(UIAssets.CLR_BLUE_LIGHT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(UIAssets.getBg());
        table.getTableHeader().setForeground(UIAssets.getTextSecondary());
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header,    BorderLayout.NORTH);
        top.add(filterRow, BorderLayout.SOUTH);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void switchMyRentals(String statusFilter) {
        contentArea.remove(2);
        contentArea.add(buildMyRentalsPanel(statusFilter), PANEL_KEYS[2], 2);
        cardLayout.show(contentArea, PANEL_KEYS[2]);
    }

    // ====================================================
    //  PAYMENT PANEL
    // ====================================================
    private JPanel buildPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("Payment");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIAssets.getTextPrimary());
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel sub = new JLabel("View your payment history and pending transactions.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(UIAssets.getTextSecondary());
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));
        header.add(title); header.add(sub);

        String[] cols = { "Payment ID", "Rental ID", "Amount", "Date", "Method", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        Customer customer = getLoggedInCustomer();
        if (customer != null) {
            for (Rental r : RentalService.getCustomerRentals(customer.getCustomerId())) {
                for (Payment p : RentalService.getAllPayments()) {
                    if (p.getRentalId() == r.getRentalId()) {
                        model.addRow(new Object[]{ p.getPaymentId(), p.getRentalId(),
                            "\u20b1" + String.format("%,.2f", p.getAmountPaid()),
                            p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : "\u2014",
                            p.getPaymentMethod(), p.getStatus() });
                    }
                }
            }
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setBackground(Color.WHITE);
        table.setForeground(UIAssets.getTextPrimary());
        table.setGridColor(new Color(220, 220, 220));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(UIAssets.CLR_BLUE_LIGHT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(UIAssets.getBg());
        table.getTableHeader().setForeground(UIAssets.getTextSecondary());
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll,  BorderLayout.CENTER);
        return panel;
    }

    private Customer getLoggedInCustomer() {
        if (!SessionManager.isLoggedIn()) return null;
        CustomerDAO dao = new CustomerDAO();
        return dao.getCustomerByUserId(SessionManager.getCurrentUser().getUserId());
    }

    // ====================================================
    //  MODIFY BOOKING DIALOG
    // ====================================================
    private void showModifyBookingDialog() {
        Customer customer = getLoggedInCustomer();
        if (customer == null) { JOptionPane.showMessageDialog(this, "Please log in first."); return; }

        List<Rental> rentals = RentalService.getCustomerRentals(customer.getCustomerId())
            .stream().filter(r -> r.getStatus().equals("PENDING")).toList();

        if (rentals.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no pending bookings to modify.");
            return;
        }

        JComboBox<String> rentalBox = new JComboBox<>();
        for (Rental r : rentals)
            rentalBox.addItem("Rental #" + r.getRentalId() + " — Car ID " + r.getCarId()
                + " | " + r.getStartDate() + " to " + r.getEndDate());

        JTextField newStart = new JTextField("YYYY-MM-DD");
        JTextField newEnd   = new JTextField("YYYY-MM-DD");

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("Select Booking:")); form.add(rentalBox);
        form.add(new JLabel("New Start Date:")); form.add(newStart);
        form.add(new JLabel("New End Date:"));   form.add(newEnd);

        if (JOptionPane.showConfirmDialog(this, form, "Modify Booking",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                int idx = rentalBox.getSelectedIndex();
                Rental selected = rentals.get(idx);
                java.time.LocalDate start = java.time.LocalDate.parse(newStart.getText().trim());
                java.time.LocalDate end   = java.time.LocalDate.parse(newEnd.getText().trim());
                if (!end.isAfter(start)) {
                    JOptionPane.showMessageDialog(this, "End date must be after start date."); return;
                }
                // Cancel old rental and create a new one with updated dates
                if (RentalService.cancelRental(selected.getRentalId())) {
                    pckDatabase.CarDAO carDAO = new pckDatabase.CarDAO();
                    pckModels.Car car = carDAO.getCarById(selected.getCarId());
                    if (car != null && RentalService.bookRental(
                            customer.getCustomerId(), car.getCarId(),
                            start, end, car.getDailyRate())) {
                        JOptionPane.showMessageDialog(this, "Booking modified successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to rebook. Car may no longer be available.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to modify booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ====================================================
    //  CANCEL BOOKING DIALOG
    // ====================================================
    private void showCancelBookingDialog() {
        Customer customer = getLoggedInCustomer();
        if (customer == null) { JOptionPane.showMessageDialog(this, "Please log in first."); return; }

        List<Rental> rentals = RentalService.getCustomerRentals(customer.getCustomerId())
            .stream().filter(r -> r.getStatus().equals("PENDING") || r.getStatus().equals("ACTIVE")).toList();

        if (rentals.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You have no active or pending bookings to cancel.");
            return;
        }

        JComboBox<String> rentalBox = new JComboBox<>();
        for (Rental r : rentals)
            rentalBox.addItem("Rental #" + r.getRentalId() + " — Car ID " + r.getCarId()
                + " | " + r.getStartDate() + " to " + r.getEndDate() + " [" + r.getStatus() + "]");

        JPanel form = new JPanel(new GridLayout(1, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("Select Booking:")); form.add(rentalBox);

        if (JOptionPane.showConfirmDialog(this, form, "Cancel Booking",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            int idx = rentalBox.getSelectedIndex();
            Rental selected = rentals.get(idx);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel Rental #" + selected.getRentalId() + "? This cannot be undone.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (RentalService.cancelRental(selected.getRentalId())) {
                    JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
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
                        javax.swing.SwingUtilities.invokeLater(() -> applyBrowseFilter(colCategory, value));
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
        switch (category) {
            // Browse Cars filters
            case "By Type"      -> { switchPanel(0); browseCarsPanel.filterByType(value);         }
            case "By Brand"     -> { switchPanel(0); browseCarsPanel.filterByBrand(value);        }
            case "By Price"     -> { switchPanel(0); browseCarsPanel.filterByPriceRange(value);   }
            case "Transmission" -> { switchPanel(0); browseCarsPanel.filterByTransmission(value); }
            case "Fuel Type"    -> { switchPanel(0); browseCarsPanel.filterByFuelType(value);     }
            // Make a Reservation actions
            case "Rental" -> {
                if (value.equals("New Reservation")) {
                    switchPanel(1);
                } else if (value.equals("Modify Booking")) {
                    showModifyBookingDialog();
                } else if (value.equals("Cancel Booking")) {
                    showCancelBookingDialog();
                }
            }
            case "Options" -> {
                switchPanel(1);
                makeReservationPanel.setRentalType(
                    value.equals("Per Hour") ? "Per Hour" : "Per Day");
            }
            // My Rentals actions
            case "Status" -> {
                String filter = switch (value) {
                    case "Active Rentals"   -> "ACTIVE";
                    case "Pending Approval" -> "PENDING";
                    case "Completed"        -> "COMPLETED";
                    case "Cancelled"        -> "CANCELLED";
                    default                 -> null;
                };
                switchMyRentals(filter);
            }
            case "Actions" -> switchPanel(2);
            // Payment actions
            case "Transactions", "Methods" -> switchPanel(3);
        }
    }
}