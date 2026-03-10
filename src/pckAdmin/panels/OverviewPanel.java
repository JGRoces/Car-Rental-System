package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * OverviewPanel.java
 * ─────────────────────────────────────────────────────────────
 * The first panel visible on login — system-wide snapshot.
 *
 * Layout (top to bottom):
 *   ┌─────────────────────────────────────────────────────┐
 *   │  Page header (left)    Quick Actions (right)        │
 *   ├──────────┬──────────┬──────────┬────────────────────┤
 *   │  Total   │  Total   │  Active  │  Users             │
 *   │ Vehicles │ Revenue  │ Rentals  │ (Customers+Drivers)│
 *   ├──────────┴──────────┴──────────┴────────────────────┤
 *   │  Recent Rentals (left)  │  Recent Driver Requests   │
 *   └─────────────────────────────────────────────────────┘
 *
 * Color convention for stat cards:
 *   Total Vehicles → BLUE
 *   Total Revenue  → GREEN
 *   Active Rentals → YELLOW
 *   Users          → RED
 *
 * Wiring points (labeled "WIRING POINT") are where real DAO
 * calls will replace the placeholder "—" values and empty tables.
 */
public class OverviewPanel extends JPanel {

    private final AdminNavCallback nav;

    // Stat value labels — kept as fields so DAO can update them later
    // WIRING POINT: call setStatValue(vehiclesLbl, String.valueOf(count))
    //               after fetching from CarDAO.getCount() etc.
    private JLabel vehiclesLbl;
    private JLabel revenueLbl;
    private JLabel rentalsLbl;
    private JLabel usersLbl;

    public OverviewPanel(AdminNavCallback nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Header: title left, quick actions right ───────────────
    private JPanel buildHeader() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 24, 0));

        // Left — page title + subtitle
        JPanel titleBlock = AdminUIHelper.buildPageHeader(
            "Statistical Summary of Inventory",
            "Welcome back! Here's a live snapshot of the system."
        );
        row.add(titleBlock, BorderLayout.WEST);

        // Right — quick action buttons
        row.add(buildQuickActions(), BorderLayout.EAST);
        return row;
    }

    /**
     * Quick action buttons — shortcuts to Management sub-tabs and Reports.
     * Each button navigates via the AdminNavCallback so this panel stays
     * decoupled from AdminDashboardGUI's internals.
     */
    private JPanel buildQuickActions() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setOpaque(false);

        // WIRING POINT: these currently only navigate; when dialogs
        // are ready, replace the nav.goTo() call with dialog.open().
        JButton addVehicle  = AdminUIHelper.buildSolidButton("+ Add Vehicle",  UIAssets.CLR_BLUE);
        JButton addCustomer = AdminUIHelper.buildSolidButton("+ Add Customer", UIAssets.CLR_GREEN);
        JButton newRental   = AdminUIHelper.buildSolidButton("New Rental",     UIAssets.CLR_YELLOW);
        JButton viewReports = AdminUIHelper.buildOutlineButton("View Reports");

        addVehicle.addActionListener(e  -> nav.goTo("MANAGEMENT"));
        addCustomer.addActionListener(e -> nav.goTo("MANAGEMENT"));
        newRental.addActionListener(e   -> nav.goTo("MANAGEMENT"));
        viewReports.addActionListener(e -> nav.goTo("REPORTS"));

        row.add(addVehicle);
        row.add(addCustomer);
        row.add(newRental);
        row.add(viewReports);
        return row;
    }

    // ── Body: stat cards + recent sections ───────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(buildStatCards());
        body.add(Box.createVerticalStrut(28));
        body.add(buildRecentRow());

        return body;
    }

    // ── 4 stat cards ─────────────────────────────────────────
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // WIRING POINT: replace "—" with live values from DAO
        //   e.g. CarDAO.getCount(), RentalDAO.getActiveCount(),
        //        CustomerDAO.getCount() + DriverDAO.getCount()
        JPanel vehiclesCard = AdminUIHelper.buildStatCard(
            "Total Vehicles", "—", "Registered in fleet",
            UIAssets.CLR_BLUE, UIAssets.CLR_BLUE_LIGHT);
        JPanel revenueCard  = AdminUIHelper.buildStatCard(
            "Total Revenue",  "—", "All-time earnings",
            UIAssets.CLR_GREEN, UIAssets.CLR_GREEN_LIGHT);
        JPanel rentalsCard  = AdminUIHelper.buildStatCard(
            "Active Rentals", "—", "Currently rented out",
            UIAssets.CLR_YELLOW, UIAssets.CLR_YELLOW_LIGHT);
        JPanel usersCard    = AdminUIHelper.buildStatCard(
            "Users",          "—", "Customers & Drivers",
            UIAssets.CLR_RED, UIAssets.CLR_RED_LIGHT);

        // Keep references to the value labels for future DAO wiring
        vehiclesLbl = getStatValueLabel(vehiclesCard);
        revenueLbl  = getStatValueLabel(revenueCard);
        rentalsLbl  = getStatValueLabel(rentalsCard);
        usersLbl    = getStatValueLabel(usersCard);

        row.add(vehiclesCard);
        row.add(revenueCard);
        row.add(rentalsCard);
        row.add(usersCard);
        return row;
    }

    /**
     * Helper to retrieve the large value JLabel from inside a stat card.
     * buildStatCard puts valLbl in a GridLayout panel in SOUTH position.
     * Structure: card → SOUTH: GridLayout(2,1) → first child = valLbl
     */
    private JLabel getStatValueLabel(JPanel card) {
        try {
            JPanel bottomStack = (JPanel) card.getComponent(1); // SOUTH
            return (JLabel) bottomStack.getComponent(0);        // first = value
        } catch (Exception ignored) {
            return new JLabel("—"); // safe fallback
        }
    }

    /**
     * Update a stat card value label at runtime.
     * Call this after loading data from DAO:
     *   setStatValue(vehiclesLbl, "24");
     *   setStatValue(revenueLbl, "₱145,000");
     */
    public void setStatValue(JLabel lbl, String value) {
        lbl.setText(value);
    }

    // ── Two side-by-side recent sections ─────────────────────
    private JPanel buildRecentRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(buildRecentRentals());
        row.add(buildRecentDriverRequests());
        return row;
    }

    // ── Recent Rentals section ────────────────────────────────
    private JPanel buildRecentRentals() {
        String[] cols = { "Customer", "Vehicle", "Start Date", "End Date", "Status" };

        // WIRING POINT: replace empty Object[][] with real rows from
        //   RentalDAO.getRecent(5) — last 5 rentals
        Object[][] rows = {};

        return buildRecentSection("Recent Rentals", cols, rows);
    }

    // ── Recent Driver Requests section ────────────────────────
    private JPanel buildRecentDriverRequests() {
        String[] cols = { "Name", "License No.", "Vehicle Type", "Submitted", "Status" };

        // WIRING POINT: replace with DriverDAO.getPendingDrivers()
        //   limited to the 5 most recently submitted
        Object[][] rows = {};

        return buildRecentSection("Recent Driver Requests", cols, rows);
    }

    /**
     * Generic "recent" card — white surface card, title bar at top,
     * table body or empty state in center.
     * Used for both Recent Rentals and Recent Driver Requests.
     */
    private JPanel buildRecentSection(String title, String[] cols, Object[][] rows) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIAssets.getSurface());
        card.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Card header row — title left, "View All" ghost link right
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
            new EmptyBorder(14, 18, 14, 18)
        ));

        JLabel titleLbl = AdminUIHelper.buildSectionLabel(title);
        titleLbl.setFont(UIAssets.FONT_H3);

        JButton viewAll = AdminUIHelper.buildGhostButton("View all →", UIAssets.CLR_BLUE);
        viewAll.addActionListener(e -> nav.goTo("MANAGEMENT"));

        headerRow.add(titleLbl, BorderLayout.WEST);
        headerRow.add(viewAll,  BorderLayout.EAST);
        card.add(headerRow, BorderLayout.NORTH);

        if (rows.length > 0) {
            // Real data — show table
            JTable table = AdminUIHelper.buildStyledTable(cols, rows);
            // Apply status badge renderer to last column (Status)
            table.getColumnModel()
                 .getColumn(cols.length - 1)
                 .setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
            JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
            scroll.setBorder(null); // card already has its own border
            scroll.setPreferredSize(new Dimension(0, 200));
            card.add(scroll, BorderLayout.CENTER);
        } else {
            // Empty state — gray centered message
            card.add(AdminUIHelper.buildEmptyState("No records yet."), BorderLayout.CENTER);
        }

        return card;
    }
}