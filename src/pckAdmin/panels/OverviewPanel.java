package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckModels.Driver;
import pckModels.Rental;
import pckServices.CarService;
import pckServices.CustomerService;
import pckServices.DriverService;
import pckServices.RentalService;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class OverviewPanel extends JPanel {

    private final AdminNavCallback nav;

    public OverviewPanel(AdminNavCallback nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 24, 0));
        row.add(AdminUIHelper.buildPageHeader(
            "Statistical Summary of Inventory",
            "Welcome back! Here's a live snapshot of the system."
        ), BorderLayout.WEST);
        row.add(buildQuickActions(), BorderLayout.EAST);
        return row;
    }

    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);
        JButton addVehicle  = AdminUIHelper.buildSolidButton("+ Add Vehicle",  UIAssets.CLR_BLUE);
        JButton addCustomer = AdminUIHelper.buildSolidButton("+ Add Customer", UIAssets.CLR_GREEN);
        JButton newRental   = AdminUIHelper.buildSolidButton("New Rental",     UIAssets.CLR_YELLOW);
        JButton viewReports = AdminUIHelper.buildOutlineButton("View Reports");
        addVehicle.addActionListener(e  -> nav.goTo("MANAGEMENT"));
        addCustomer.addActionListener(e -> nav.goTo("MANAGEMENT"));
        newRental.addActionListener(e   -> nav.goTo("MANAGEMENT"));
        viewReports.addActionListener(e -> nav.goTo("REPORTS"));
        row.add(addVehicle); row.add(addCustomer); row.add(newRental); row.add(viewReports);
        return row;
    }

    // ── Body ──────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(buildStatCards());
        body.add(Box.createVerticalStrut(28));
        body.add(buildRecentRow());
        return body;
    }

    // ── Stat cards with live data ─────────────────────────────
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        String totalVehicles = String.valueOf(CarService.getAllCars().size());
        String totalRevenue  = "\u20b1" + String.format("%,.0f", RentalService.getTotalRevenue());
        String activeRentals = String.valueOf(RentalService.getActiveCount());
        String totalUsers    = String.valueOf(new pckDatabase.CustomerDAO().getAllCustomerProfiles().size());

        row.add(AdminUIHelper.buildStatCard("Total Vehicles", totalVehicles, "Registered in fleet",  UIAssets.CLR_BLUE,   UIAssets.CLR_BLUE_LIGHT));
        row.add(AdminUIHelper.buildStatCard("Total Revenue",  totalRevenue,  "All-time earnings",     UIAssets.CLR_GREEN,  UIAssets.CLR_GREEN_LIGHT));
        row.add(AdminUIHelper.buildStatCard("Active Rentals", activeRentals, "Currently rented out",  UIAssets.CLR_YELLOW, UIAssets.CLR_YELLOW_LIGHT));
        row.add(AdminUIHelper.buildStatCard("Users",          totalUsers,    "Customers & Drivers",   UIAssets.CLR_RED,    UIAssets.CLR_RED_LIGHT));
        return row;
    }

    // ── Recent sections with live data ────────────────────────
    private JPanel buildRecentRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(buildRecentRentals());
        row.add(buildRecentDriverRequests());
        return row;
    }

    private JPanel buildRecentRentals() {
        String[] cols = { "Rental ID", "Customer ID", "Car ID", "Start Date", "End Date", "Status" };
        List<Rental> recent = RentalService.getRecentRentals(5);
        Object[][] rows = new Object[recent.size()][];
        for (int i = 0; i < recent.size(); i++) {
            Rental r = recent.get(i);
            rows[i] = new Object[]{ r.getRentalId(), r.getCustomerId(), r.getCarId(),
                r.getStartDate(), r.getEndDate(), r.getStatus() };
        }
        return buildRecentSection("Recent Rentals", cols, rows);
    }

    private JPanel buildRecentDriverRequests() {
        String[] cols = { "Name", "License No.", "Vehicle Type", "Status" };
        List<Driver> pending = new pckDatabase.DriverDAO().getDriversByStatus(Driver.STATUS_PENDING);
        Object[][] rows = new Object[pending.size()][];
        for (int i = 0; i < pending.size(); i++) {
            Driver d = pending.get(i);
            rows[i] = new Object[]{ d.getFullName(), d.getLicenseNumber(),
                d.getVehicleType(), d.getStatus() };
        }
        return buildRecentSection("Recent Driver Requests", cols, rows);
    }

    private JPanel buildRecentSection(String title, String[] cols, Object[][] rows) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIAssets.getSurface());
        card.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
            new EmptyBorder(14, 18, 14, 18)
        ));
        JLabel titleLbl = AdminUIHelper.buildSectionLabel(title);
        titleLbl.setFont(UIAssets.FONT_H3);
        JButton viewAll = AdminUIHelper.buildGhostButton("View all \u2192", UIAssets.CLR_BLUE);
        viewAll.addActionListener(e -> nav.goTo("MANAGEMENT"));
        headerRow.add(titleLbl, BorderLayout.WEST);
        headerRow.add(viewAll,  BorderLayout.EAST);
        card.add(headerRow, BorderLayout.NORTH);

        if (rows.length > 0) {
            JTable table = AdminUIHelper.buildStyledTable(cols, rows);
            table.getColumnModel().getColumn(cols.length - 1)
                 .setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
            JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
            scroll.setBorder(null);
            scroll.setPreferredSize(new Dimension(0, 200));
            card.add(scroll, BorderLayout.CENTER);
        } else {
            card.add(AdminUIHelper.buildEmptyState("No records yet."), BorderLayout.CENTER);
        }
        return card;
    }
}
