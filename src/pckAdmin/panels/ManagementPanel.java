package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckModels.*;
import pckServices.*;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ManagementPanel extends JPanel {

    private static final String[] TAB_LABELS = { "Vehicles", "Drivers", "Customers", "Rentals" };
    private static final String[] TAB_KEYS   = { "VEHICLES", "DRIVERS", "CUSTOMERS", "RENTALS" };

    private JButton[]  tabButtons;
    private JPanel     tabContent;
    private CardLayout tabLayout;

    public ManagementPanel(AdminNavCallback nav) {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 0, 36));
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 0, 0));
        header.add(AdminUIHelper.buildPageHeader(
            "All Inventory & Population",
            "Manage vehicles, drivers, customers, and rentals."
        ), BorderLayout.NORTH);
        header.add(buildTabBar(), BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Content
        tabLayout  = new CardLayout();
        tabContent = new JPanel(tabLayout);
        tabContent.setOpaque(false);
        tabContent.add(buildVehiclesTab(), TAB_KEYS[0]);
        tabContent.add(buildDriversTab(),  TAB_KEYS[1]);
        tabContent.add(buildCustomersTab(),TAB_KEYS[2]);
        tabContent.add(buildRentalsTab(),  TAB_KEYS[3]);
        tabLayout.show(tabContent, TAB_KEYS[0]);
        add(tabContent, BorderLayout.CENTER);
    }

    private JPanel buildTabBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);
        tabButtons = new JButton[TAB_LABELS.length];
        for (int i = 0; i < TAB_LABELS.length; i++) {
            final int idx = i;
            tabButtons[i] = buildTabButton(TAB_LABELS[i], i == 0);
            tabButtons[i].addActionListener(e -> switchTab(idx));
            tabs.add(tabButtons[i]);
        }

        JSeparator line = new JSeparator();
        line.setForeground(UIAssets.getBorder());

        bar.add(tabs, BorderLayout.CENTER);
        bar.add(line, BorderLayout.SOUTH);
        return bar;
    }

    private JButton buildTabButton(String label, boolean active) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                if (getBackground().equals(UIAssets.CLR_BLUE)) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UIAssets.CLR_BLUE);
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setFont(active ? UIAssets.FONT_H3 : UIAssets.FONT_BODY);
        btn.setForeground(active ? UIAssets.CLR_BLUE : UIAssets.getTextSecondary());
        btn.setBackground(active ? UIAssets.CLR_BLUE : new Color(0, 0, 0, 0));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 0, 12, 28));
        btn.setOpaque(false);
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) btn.setForeground(UIAssets.getTextPrimary());
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) btn.setForeground(UIAssets.getTextSecondary());
            }
        });
        return btn;
    }

    private void switchTab(int index) {
        for (int i = 0; i < tabButtons.length; i++) {
            boolean active = (i == index);
            tabButtons[i].setBackground(active ? UIAssets.CLR_BLUE : new Color(0, 0, 0, 0));
            tabButtons[i].setForeground(active ? UIAssets.CLR_BLUE : UIAssets.getTextSecondary());
            tabButtons[i].setFont(active ? UIAssets.FONT_H3 : UIAssets.FONT_BODY);
            tabButtons[i].repaint();
        }
        tabLayout.show(tabContent, TAB_KEYS[index]);
    }

    // ── VEHICLES ──────────────────────────────────────────────
    private JPanel buildVehiclesTab() {
        String[] cols = { "ID", "Brand", "Model", "Year", "Plate No.", "Category", "Daily Rate", "Status" };
        List<Car> cars = CarService.getAllCars();
        Object[][] rows = new Object[cars.size()][];
        for (int i = 0; i < cars.size(); i++) {
            Car c = cars.get(i);
            rows[i] = new Object[]{ c.getCarId(), c.getBrand(), c.getModel(), c.getYear(),
                c.getPlateNumber(), c.getCategory(),
                "\u20b1" + String.format("%,.2f", c.getDailyRate()), c.getStatus() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(7).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        JButton addBtn = AdminUIHelper.buildSolidButton("+ Add Vehicle", UIAssets.CLR_BLUE);
        addBtn.addActionListener(e -> showAddVehicleDialog(table, rows));

        return buildTabPanel(table, rows.length, addBtn);
    }

    private void showAddVehicleDialog(JTable table, Object[][] existingRows) {
        JTextField brand = new JTextField(), model = new JTextField(),
            year = new JTextField(), plate = new JTextField(),
            rate = new JTextField(), color = new JTextField(), seats = new JTextField("5");
        JComboBox<String> cat  = new JComboBox<>(new String[]{"Sedan","SUV","Van","Truck","Pickup","Coupe","Minivan","MPV"});
        JComboBox<String> trans= new JComboBox<>(new String[]{"Automatic","Manual","CVT"});
        JComboBox<String> fuel = new JComboBox<>(new String[]{"Gasoline","Diesel","Hybrid","Electric"});
        JComboBox<String> stat = new JComboBox<>(new String[]{"AVAILABLE","MAINTENANCE"});

        JPanel form = buildFormGrid(
            new String[]{"Brand","Model","Year","Plate Number","Category","Transmission","Fuel Type","Seats","Daily Rate","Color","Status"},
            new JComponent[]{brand, model, year, plate, cat, trans, fuel, seats, rate, color, stat}
        );
        if (JOptionPane.showConfirmDialog(this, form, "Add Vehicle",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                Car car = new Car(brand.getText().trim(), model.getText().trim(),
                    Integer.parseInt(year.getText().trim()), plate.getText().trim(),
                    (String)cat.getSelectedItem(), (String)trans.getSelectedItem(),
                    (String)fuel.getSelectedItem(), Integer.parseInt(seats.getText().trim()),
                    new java.math.BigDecimal(rate.getText().trim()));
                car.setStatus((String)stat.getSelectedItem());
                car.setColor(color.getText().trim());
                int id = CarService.addCar(car);
                if (id > 0) {
                    JOptionPane.showMessageDialog(this, "Vehicle added successfully.");
                    refreshTab(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed. Plate may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid year, seats, or rate.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── DRIVERS ───────────────────────────────────────────────
    private JPanel buildDriversTab() {
        String[] cols = { "ID", "Name", "Email", "Phone", "License No.", "Expiry", "Vehicle Type", "Status" };
        List<Driver> drivers = new pckDatabase.DriverDAO().getDriversByStatus(Driver.STATUS_PENDING);
        Object[][] rows = new Object[drivers.size()][];
        for (int i = 0; i < drivers.size(); i++) {
            Driver d = drivers.get(i);
            rows[i] = new Object[]{ d.getDriverId(), d.getFullName(), d.getEmail(),
                d.getPhone(), d.getLicenseNumber(), d.getLicenseExpiry(),
                d.getVehicleType(), d.getStatus() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(7).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        JButton addBtn = AdminUIHelper.buildSolidButton("+ Add Driver", UIAssets.CLR_GREEN);
        addBtn.addActionListener(e -> showAddDriverDialog());

        JPanel panel = buildTabPanel(table, rows.length, addBtn);

        // Verify / Reject buttons below table
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        JButton verifyBtn = AdminUIHelper.buildSolidButton("Verify", UIAssets.CLR_GREEN);
        JButton rejectBtn = AdminUIHelper.buildSolidButton("Reject", UIAssets.CLR_RED);
        verifyBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a driver first."); return; }
            int driverId = (int) model.getValueAt(row, 0);
            if (DriverService.verify(driverId)) {
                model.setValueAt("VERIFIED", row, 7);
                JOptionPane.showMessageDialog(this, "Driver verified.");
            }
        });
        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a driver first."); return; }
            int driverId = (int) model.getValueAt(row, 0);
            if (DriverService.reject(driverId)) {
                model.setValueAt("REJECTED", row, 7);
                JOptionPane.showMessageDialog(this, "Driver rejected.");
            }
        });
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(8, 0, 0, 0));
        actionRow.add(verifyBtn); actionRow.add(rejectBtn);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private void showAddDriverDialog() {
        JTextField name = new JTextField(), email = new JTextField(),
            phone = new JTextField(), license = new JTextField(),
            expiry = new JTextField("YYYY-MM-DD"), vtype = new JTextField();
        JPasswordField pass = new JPasswordField();
        JPanel form = buildFormGrid(
            new String[]{"Full Name","Email","Password","Phone","License Number","License Expiry","Vehicle Type"},
            new JComponent[]{name, email, pass, phone, license, expiry, vtype}
        );
        if (JOptionPane.showConfirmDialog(this, form, "Add Driver",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            String password = new String(pass.getPassword());
            DriverService.RegisterResult result = DriverService.register(
                name.getText().trim(), email.getText().trim(), phone.getText().trim(),
                password, password, license.getText().trim(),
                expiry.getText().trim(), vtype.getText().trim(), null);
            if (result == DriverService.RegisterResult.SUCCESS) {
                JOptionPane.showMessageDialog(this, "Driver added. Status: PENDING.");
                refreshTab(1);
            } else {
                JOptionPane.showMessageDialog(this, DriverService.getMessage(result), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── CUSTOMERS ─────────────────────────────────────────────
    private JPanel buildCustomersTab() {
        String[] cols = { "ID", "Name", "Email", "Phone" };
        List<Customer> customers = new pckDatabase.CustomerDAO().getAllCustomerProfiles();
        Object[][] rows = new Object[customers.size()][];
        for (int i = 0; i < customers.size(); i++) {
            Customer c = customers.get(i);
            rows[i] = new Object[]{ c.getCustomerId(), c.getFullName(), c.getEmail(), c.getPhone() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        return buildTabPanel(table, rows.length, null);
    }

    // ── RENTALS ───────────────────────────────────────────────
    private JPanel buildRentalsTab() {
        String[] cols = { "ID", "Customer ID", "Car ID", "Start", "End", "Amount", "Status" };
        List<pckModels.Rental> rentals = RentalService.getAllRentals();
        Object[][] rows = new Object[rentals.size()][];
        for (int i = 0; i < rentals.size(); i++) {
            pckModels.Rental r = rentals.get(i);
            rows[i] = new Object[]{ r.getRentalId(), r.getCustomerId(), r.getCarId(),
                r.getStartDate(), r.getEndDate(),
                "\u20b1" + String.format("%,.2f", r.getTotalAmount()), r.getStatus() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(6).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        JButton newBtn = AdminUIHelper.buildSolidButton("+ New Rental", UIAssets.CLR_YELLOW);
        newBtn.addActionListener(e -> showNewRentalDialog());

        JPanel panel = buildTabPanel(table, rows.length, newBtn);

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        JButton completeBtn = AdminUIHelper.buildSolidButton("Mark Complete", UIAssets.CLR_GREEN);
        JButton cancelBtn   = AdminUIHelper.buildSolidButton("Cancel Rental", UIAssets.CLR_RED);
        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a rental first."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (RentalService.completeRental(id)) { model.setValueAt("COMPLETED", row, 6); }
        });
        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a rental first."); return; }
            int id = (int) model.getValueAt(row, 0);
            if (RentalService.cancelRental(id)) { model.setValueAt("CANCELLED", row, 6); }
        });
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(8, 0, 0, 0));
        actionRow.add(completeBtn); actionRow.add(cancelBtn);
        panel.add(actionRow, BorderLayout.SOUTH);
        return panel;
    }

    private void showNewRentalDialog() {
        List<Customer> customers = new pckDatabase.CustomerDAO().getAllCustomerProfiles();
        List<Car> cars = CarService.getAvailableCars();
        if (customers.isEmpty()) { JOptionPane.showMessageDialog(this, "No customers found."); return; }
        if (cars.isEmpty())      { JOptionPane.showMessageDialog(this, "No available vehicles."); return; }

        JComboBox<String> custBox = new JComboBox<>();
        for (Customer c : customers) custBox.addItem(c.getCustomerId() + " - " + c.getFullName());
        JComboBox<String> carBox = new JComboBox<>();
        for (Car c : cars) carBox.addItem(c.getCarId() + " - " + c.getBrand() + " " + c.getModel() + " (" + c.getPlateNumber() + ")");
        JTextField startField = new JTextField("YYYY-MM-DD");
        JTextField endField   = new JTextField("YYYY-MM-DD");

        JPanel form = buildFormGrid(
            new String[]{"Customer","Vehicle","Start Date","End Date"},
            new JComponent[]{custBox, carBox, startField, endField}
        );
        if (JOptionPane.showConfirmDialog(this, form, "New Rental",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            try {
                int custId = Integer.parseInt(custBox.getSelectedItem().toString().split(" - ")[0]);
                int carId  = Integer.parseInt(carBox.getSelectedItem().toString().split(" - ")[0]);
                Car selCar = cars.stream().filter(c -> c.getCarId() == carId).findFirst().orElse(null);
                java.time.LocalDate start = java.time.LocalDate.parse(startField.getText().trim());
                java.time.LocalDate end   = java.time.LocalDate.parse(endField.getText().trim());
                if (selCar != null && RentalService.bookRental(custId, carId, start, end, selCar.getDailyRate())) {
                    JOptionPane.showMessageDialog(this, "Rental created successfully.");
                    refreshTab(3);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to create rental.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── HELPERS ───────────────────────────────────────────────
    private JPanel buildTabPanel(JTable table, int count, JButton addBtn) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(16, 0, 16, 0));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setBorder(new EmptyBorder(0, 0, 10, 0));
        JLabel countLbl = new JLabel(count + " record(s)");
        countLbl.setFont(UIAssets.FONT_SMALL);
        countLbl.setForeground(UIAssets.getTextSecondary());
        toolbar.add(countLbl, BorderLayout.WEST);
        if (addBtn != null) toolbar.add(addBtn, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(AdminUIHelper.buildTableScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFormGrid(String[] labels, JComponent[] fields) {
        JPanel form = new JPanel(new GridLayout(labels.length, 2, 8, 8));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        for (int i = 0; i < labels.length; i++) {
            form.add(new JLabel(labels[i]));
            form.add(fields[i]);
        }
        return form;
    }

    private void refreshTab(int index) {
        tabContent.remove(tabContent.getComponent(index));
        JPanel fresh = switch (index) {
            case 0 -> buildVehiclesTab();
            case 1 -> buildDriversTab();
            case 2 -> buildCustomersTab();
            case 3 -> buildRentalsTab();
            default -> new JPanel();
        };
        tabContent.add(fresh, TAB_KEYS[index], index);
        switchTab(index);
    }
}
