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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import pckMain.LoginGUI;
import pckServices.AuthService;
import pckServices.RentalService;
import pckUtils.SessionManager;
import pckDatabase.CustomerDAO;
import pckModels.Customer;
import pckModels.Rental;
import pckModels.Payment;

import java.util.List;

public class CustomerDashboardGUI extends JFrame {

    private static final String[] NAV_LABELS = {
        "Browse Cars", "Make a Reservation", "My Rentals", "Payment"
    };
    private static final String[] NAV_ICONS = {
        "\uD83D\uDE97", "\uD83D\uDCC5", "\uD83D\uDCCB", "\uD83D\uDCB3"
    };
    private static final String[] PANEL_KEYS = {
        "BROWSE_CARS", "MAKE_RESERVATION", "MY_RENTALS", "PAYMENT"
    };

    private JPanel               contentArea;
    private CardLayout           cardLayout;
    private JButton[]            navButtons;
    private BrowseCarsGUI        browseCarsPanel;
    private MakeReservationPanel makeReservationPanel;
    private JTable               myRentalsTable;
    private DefaultTableModel    myRentalsModel;
    private List<Rental>         myRentalsData = new java.util.ArrayList<>();

    public CustomerDashboardGUI() {
        initWindow();
        initComponents();
    }

    private void initWindow() {
        setTitle("Car Rental System \u2014 Customer Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(1600, 900);
        setMinimumSize(new Dimension(1024, 600));
        setResizable(true);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIAssets.getBg());
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.add(new CustomTitleBar(this, "CarRentals  \u2014  Customer Portal"));
        topStack.add(buildMenuBar());
        add(topStack,           BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER);
    }

    // ── Menu Bar ──────────────────────────────────────────────
    private JPanel buildMenuBar() {
        JPanel menuBar = new JPanel(new BorderLayout());
        menuBar.setBackground(UIAssets.CLR_CHROME);
        menuBar.setPreferredSize(new Dimension(0, 48));
        menuBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel navItems = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        navItems.setBackground(UIAssets.CLR_CHROME);

        navButtons = new JButton[NAV_LABELS.length];
        for (int i = 0; i < NAV_LABELS.length; i++) {
            final int idx = i;
            navButtons[i] = buildNavBtn(NAV_ICONS[i], NAV_LABELS[i], i == 0);
            navButtons[i].addActionListener(e -> switchPanel(idx));
            navItems.add(navButtons[i]);
        }

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
                Object w = getClientProperty("underlineWidth");
                int lineW = w instanceof Integer ? (Integer) w : 0;
                if (lineW > 0) {
                    g2.setColor(new Color(96, 165, 250));
                    g2.fillRect((getWidth() - lineW) / 2, getHeight() - 3, lineW, 3);
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
        btn.setPreferredSize(new Dimension(Math.max(110, label.length() * 9 + 48), 48));
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
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(UIAssets.CLR_CHROME);
                animateUnderline(btn, false);
            }
        });
        return btn;
    }

    // ── Content Area ──────────────────────────────────────────
    private JPanel buildContentArea() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIAssets.getBg());

        browseCarsPanel      = new BrowseCarsGUI();
        makeReservationPanel = new MakeReservationPanel();

        contentArea.add((JPanel) browseCarsPanel, PANEL_KEYS[0]);
        contentArea.add(makeReservationPanel,      PANEL_KEYS[1]);
        contentArea.add(buildMyRentalsPanel(null), PANEL_KEYS[2]);
        contentArea.add(buildPaymentPanel(),       PANEL_KEYS[3]);

        browseCarsPanel.setOnRentNow(() -> navigateToReservationWithCar(browseCarsPanel.getSelectedCar()));

        cardLayout.show(contentArea, PANEL_KEYS[0]);
        return contentArea;
    }

    private void navigateToReservationWithCar(pckModels.Car car) {
        if (car != null) makeReservationPanel.setSelectedCar(car);
        switchPanel(1);
    }

    // ── Switch Panel ──────────────────────────────────────────
    private void switchPanel(int index) {
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

    // ── My Rentals Panel ──────────────────────────────────────
    private JPanel buildMyRentalsPanel(String statusFilter) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel title = new JLabel("My Rentals");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIAssets.getTextPrimary());
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        JLabel sub = new JLabel(statusFilter != null ? "Showing: " + statusFilter : "View your rental history and current bookings.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(UIAssets.getTextSecondary());
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        header.add(title); header.add(sub);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        String[] filters = { "All", "PENDING", "ACTIVE", "COMPLETED", "CANCELLED" };
        String[] labels  = { "All", "Pending", "Active", "Completed", "Cancelled" };
        for (int i = 0; i < filters.length; i++) {
            final String f = filters[i];
            boolean active = (statusFilter == null && f.equals("All")) || f.equals(statusFilter);
            JButton btn = new JButton(labels[i]);
            btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
            btn.setForeground(active ? Color.WHITE : UIAssets.getTextPrimary());
            btn.setBackground(active ? UIAssets.CLR_BLUE : new Color(220, 220, 220));
            btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> switchMyRentals(f.equals("All") ? null : f));
            filterRow.add(btn);
        }

        // Action buttons — only show for Pending and Active
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(0, 0, 12, 0));
        boolean showActions = statusFilter == null || statusFilter.equals("PENDING") || statusFilter.equals("ACTIVE");
        if (showActions) {
            JButton modifyBtn = new JButton("Modify Booking");
            modifyBtn.setFont(UIAssets.FONT_NAV);
            modifyBtn.setForeground(Color.WHITE);
            modifyBtn.setBackground(UIAssets.CLR_YELLOW);
            modifyBtn.setBorderPainted(false); modifyBtn.setFocusPainted(false);
            modifyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            modifyBtn.addActionListener(e -> showModifyBookingDialog());

            JButton cancelBtn = new JButton("Cancel Booking");
            cancelBtn.setFont(UIAssets.FONT_NAV);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setBackground(UIAssets.CLR_RED);
            cancelBtn.setBorderPainted(false); cancelBtn.setFocusPainted(false);
            cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            cancelBtn.addActionListener(e -> showCancelBookingDialog());

            actionRow.add(modifyBtn);
            actionRow.add(cancelBtn);
        }

        String[] cols = { "Rental #", "Car", "Start Date", "End Date", "Driver", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        Customer customer = getLoggedInCustomer();
        myRentalsData.clear();
        if (customer != null) {
            pckDatabase.CarDAO carDAO = new pckDatabase.CarDAO();
            pckDatabase.DriverDAO driverDAO = new pckDatabase.DriverDAO();
            for (Rental r : RentalService.getCustomerRentals(customer.getCustomerId())) {
                if (statusFilter == null || r.getStatus().equalsIgnoreCase(statusFilter)) {
                    pckModels.Car car = carDAO.getCarById(r.getCarId());
                    String carName = car != null ? car.getBrand() + " " + car.getModel() + " (" + car.getPlateNumber() + ")" : "Car #" + r.getCarId();
                    String driverName = r.getDriverId() > 0
                        ? java.util.Optional.ofNullable(driverDAO.getDriverById(r.getDriverId()))
                              .map(pckModels.Driver::getFullName).orElse("\u2014")
                        : "\u2014";
                    myRentalsData.add(r);
                    model.addRow(new Object[]{ r.getRentalId(), carName,
                        r.getStartDate(), r.getEndDate(), driverName, r.getStatus() });
                }
            }
        }

        myRentalsModel = model;
        myRentalsTable = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(myRentalsTable);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header,    BorderLayout.NORTH);
        top.add(filterRow, BorderLayout.CENTER);
        top.add(actionRow, BorderLayout.SOUTH);

        panel.add(top,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void switchMyRentals(String statusFilter) {
        contentArea.remove(2);
        contentArea.add(buildMyRentalsPanel(statusFilter), PANEL_KEYS[2], 2);
        cardLayout.show(contentArea, PANEL_KEYS[2]);
    }

    // ── Payment Panel ─────────────────────────────────────────
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

        String[] cols = { "Amount", "Date", "Method", "Status" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        Customer customer = getLoggedInCustomer();
        if (customer != null) {
            for (Rental r : RentalService.getCustomerRentals(customer.getCustomerId())) {
                for (Payment p : RentalService.getAllPayments()) {
                    if (p.getRentalId() == r.getRentalId()) {
                        model.addRow(new Object[]{
                            "\u20b1" + String.format("%,.2f", p.getAmountPaid()),
                            p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : "\u2014",
                            p.getPaymentMethod(), p.getStatus() });
                    }
                }
            }
        }

        JTable table = buildStyledTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ── Shared table builder ──────────────────────────────────
    private JTable buildStyledTable(DefaultTableModel model) {
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
        return table;
    }

    private Customer getLoggedInCustomer() {
        if (!SessionManager.isLoggedIn()) return null;
        return new CustomerDAO().getCustomerByUserId(SessionManager.getCurrentUser().getUserId());
    }

    // ── Modify Booking ────────────────────────────────────────
    private void showModifyBookingDialog() {
        if (myRentalsTable == null || myRentalsTable.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.");
            return;
        }
        int row = myRentalsTable.getSelectedRow();
        if (row >= myRentalsData.size()) return;
        Rental rental = myRentalsData.get(row);

        // Validate eligibility
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().plusHours(24);
        boolean eligible = rental.getStatus().equals("PENDING") ||
            (rental.getStatus().equals("ACTIVE") &&
             cutoff.isBefore(rental.getStartDate().atStartOfDay()));
        if (!eligible) {
            JOptionPane.showMessageDialog(this,
                "This booking cannot be modified.\n"
                + "Active bookings can only be modified more than 24 hours before the start date.",
                "Cannot Modify", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer customer = getLoggedInCustomer();
        if (customer == null) { JOptionPane.showMessageDialog(this, "Please log in first."); return; }

        pckUtils.CalendarPicker newStart = new pckUtils.CalendarPicker();
        pckUtils.CalendarPicker newEnd   = new pckUtils.CalendarPicker();
        newStart.setPreferredSize(new java.awt.Dimension(160, 36));
        newEnd.setPreferredSize(new java.awt.Dimension(160, 36));
        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.setPreferredSize(new java.awt.Dimension(420, 90));
        form.add(new JLabel("New Start Date:")); form.add(newStart);
        form.add(new JLabel("New End Date:"));   form.add(newEnd);

        if (JOptionPane.showConfirmDialog(this, form,
                "Modify Rental #" + rental.getRentalId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            java.time.LocalDate start = newStart.getLocalDate();
            java.time.LocalDate end   = newEnd.getLocalDate();
            if (!end.isAfter(start)) { JOptionPane.showMessageDialog(this, "End date must be after start date."); return; }
            boolean wasActive = rental.getStatus().equals("ACTIVE");
            if (wasActive) {
                pckModels.Car car = new pckDatabase.CarDAO().getCarById(rental.getCarId());
                long days = java.time.temporal.ChronoUnit.DAYS.between(start, end);
                if (days <= 0) days = 1;
                java.math.BigDecimal newTotal = car != null
                    ? car.getDailyRate().multiply(java.math.BigDecimal.valueOf(days))
                    : rental.getTotalAmount();
                if (new pckDatabase.RentalDAO().updateDatesAndStatus(
                        rental.getRentalId(), start, end, newTotal, "PENDING")) {
                    JOptionPane.showMessageDialog(this, "Booking modified and returned to Pending for re-approval.");
                    switchMyRentals("ACTIVE");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to modify booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (RentalService.cancelRental(rental.getRentalId())) {
                    pckModels.Car car = new pckDatabase.CarDAO().getCarById(rental.getCarId());
                    if (car != null && RentalService.bookRental(customer.getCustomerId(), car.getCarId(), start, end, car.getDailyRate())) {
                        JOptionPane.showMessageDialog(this, "Booking modified successfully!");
                        switchMyRentals("PENDING");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to rebook. Car may no longer be available.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to modify booking.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }


    // ── Cancel Booking ────────────────────────────────────────
    private void showCancelBookingDialog() {
        if (myRentalsTable == null || myRentalsTable.getSelectedRow() < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table first.");
            return;
        }
        int row = myRentalsTable.getSelectedRow();
        if (row >= myRentalsData.size()) return;
        Rental rental = myRentalsData.get(row);

        // Validate eligibility
        java.time.LocalDateTime cutoff = java.time.LocalDateTime.now().plusHours(24);
        boolean eligible = rental.getStatus().equals("PENDING") ||
            (rental.getStatus().equals("ACTIVE") &&
             cutoff.isBefore(rental.getStartDate().atStartOfDay()));
        if (!eligible) {
            JOptionPane.showMessageDialog(this,
                "This booking cannot be cancelled.\n"
                + "Active bookings can only be cancelled more than 24 hours before the start date.",
                "Cannot Cancel", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (JOptionPane.showConfirmDialog(this,
                "Cancel Rental #" + rental.getRentalId() + "? This cannot be undone.",
                "Confirm Cancel", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (RentalService.cancelRental(rental.getRentalId())) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully.");
                switchMyRentals(rental.getStatus().equals("PENDING") ? "PENDING" : "ACTIVE");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // ── Logout ────────────────────────────────────────────────
    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            AuthService.logout();
            new LoginGUI().setVisible(true);
            dispose();
        }
    }

    // ── Animated underline ────────────────────────────────────
    private void animateUnderline(JButton btn, boolean expand) {
        int maxWidth = btn.getWidth();
        int[] step = {expand ? 0 : maxWidth};
        javax.swing.Timer t = new javax.swing.Timer(10, null);
        t.addActionListener(e -> {
            step[0] = expand ? Math.min(step[0] + 12, maxWidth) : Math.max(step[0] - 12, 0);
            btn.putClientProperty("underlineWidth", step[0]);
            btn.repaint();
            if ((expand && step[0] >= maxWidth) || (!expand && step[0] <= 0)) t.stop();
        });
        t.start();
    }
}
