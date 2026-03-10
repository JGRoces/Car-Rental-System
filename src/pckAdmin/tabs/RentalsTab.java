package pckAdmin.tabs;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CustomerDAO;
import pckDatabase.DriverDAO;
import pckDatabase.RentalDAO;
import pckModels.Driver;
import pckModels.Rental;
import pckServices.RentalService;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RentalsTab.java
 * ─────────────────────────────────────────────────────────────
 * Rentals sub-tab inside ManagementPanel.
 *
 * Refresh is handled globally by ManagementPanel — no per-tab button here.
 * Toolbar contains: count label | filter icon + status combo | search | export button.
 */
public class RentalsTab extends JPanel {

    private List<Rental>         rentals;
    private Map<Integer, String> customerNames = new HashMap<>();
    private Map<Integer, String> driverNames   = new HashMap<>();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JComboBox<String> statusFilter;
    private JLabel            countLabel;
    private JButton           manageBtn;

    private static final String[] STATUS_OPTIONS = {
        "All", "PENDING", "ACTIVE", "COMPLETED", "CANCELLED"
    };

    public RentalsTab() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(24, 28, 24, 28));
        loadData();
        build();
    }

    // =========================================================
    //  DATA
    // =========================================================
    private void loadData() {
        rentals = new RentalDAO().getAllRentals();
        customerNames.clear();
        driverNames.clear();
        new CustomerDAO().getAllCustomers().forEach(c ->
            customerNames.put(c.getCustomerId(), c.getFullName()));
        new DriverDAO().getDriversByStatus(Driver.STATUS_VERIFIED).forEach(d ->
            driverNames.put(d.getDriverId(), d.getFullName()));
        new DriverDAO().getDriversByStatus(Driver.STATUS_PENDING).forEach(d ->
            driverNames.put(d.getDriverId(), d.getFullName()));
    }

    /** Called by ManagementPanel's global Refresh button */
    public void refresh() {
        customerNames.clear();
        driverNames.clear();
        loadData();
        applyFilters();
        updateCount();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(buildToolbar());
        north.add(buildActionBar());
        add(north,          BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
    }

    // ── Toolbar: count | filter combo | search | export ───────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        // Left — count label
        countLabel = new JLabel();
        countLabel.setFont(UIAssets.FONT_H3);
        countLabel.setForeground(UIAssets.getTextSecondary());
        updateCount();

        // Right — filter icon + status combo + search + export
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        // Small filter icon beside the combo for visual context
        ImageIcon filterIcon = AdminUIHelper.loadIcon(
                AppConfig.ICON_FILTER, UIAssets.getTextSecondary(), 14);
        if (filterIcon != null) {
            JLabel filterLbl = new JLabel(filterIcon);
            filterLbl.setToolTipText("Filter by status");
            right.add(filterLbl);
        }

        statusFilter = AdminUIHelper.buildComboBox(STATUS_OPTIONS);
        statusFilter.setPreferredSize(new Dimension(140, 40));
        statusFilter.addActionListener(e -> applyFilters());
        right.add(statusFilter);

        searchField = AdminUIHelper.buildTextField("Search rentals...");
        searchField.setPreferredSize(new Dimension(240, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilters(); }
        });
        right.add(searchField);

        JButton exportBtn = AdminUIHelper.buildOutlineButtonWithIcon(
                "Export", AppConfig.ICON_EXPORT);
        exportBtn.setPreferredSize(new Dimension(100, 40));
        exportBtn.setToolTipText("Export rental records to CSV");
        exportBtn.addActionListener(e -> handleExport());
        right.add(exportBtn);

        bar.add(countLabel, BorderLayout.WEST);
        bar.add(right,      BorderLayout.EAST);
        return bar;
    }

    // ── Action bar: Manage Booking button ─────────────────────
    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 12, 0));

        manageBtn = AdminUIHelper.buildSolidButton("Manage Booking", UIAssets.CLR_BLUE);
        manageBtn.setPreferredSize(new Dimension(160, 38));
        manageBtn.setEnabled(false);
        manageBtn.setToolTipText("Select a PENDING or eligible ACTIVE rental to manage");
        manageBtn.addActionListener(e -> showManageDialog());
        bar.add(manageBtn);
        return bar;
    }

    private void updateCount() {
        if (countLabel != null)
            countLabel.setText(rentals.size() + " rental"
                + (rentals.size() == 1 ? "" : "s") + " total");
    }

    // ── Table ─────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {
            "Rental #", "Customer", "Start Date", "End Date",
            "Driver Assigned", "Total (\u20b1)", "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = AdminUIHelper.buildStyledTable(cols, null);
        table.setModel(tableModel);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        table.getColumnModel().getColumn(cols.length - 1)
             .setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        populateTable(rentals);

        // Enable Manage button for PENDING always; ACTIVE only if start date > 24h from now
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0) { manageBtn.setEnabled(false); return; }
            String status    = String.valueOf(tableModel.getValueAt(row, 6));
            String startStr  = String.valueOf(tableModel.getValueAt(row, 2));
            if ("PENDING".equalsIgnoreCase(status)) {
                manageBtn.setEnabled(true);
            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                // Allow manage if start date is more than 24h away OR can always complete
                manageBtn.setEnabled(true);
            } else {
                manageBtn.setEnabled(false);
            }
        });

        return AdminUIHelper.buildTableScrollPane(table);
    }

    private void populateTable(List<Rental> data) {
        tableModel.setRowCount(0);
        for (Rental r : data) {
            String customerName = customerNames.getOrDefault(
                r.getCustomerId(), "Customer #" + r.getCustomerId());
            String driverName = r.getDriverId() > 0
                ? driverNames.getOrDefault(r.getDriverId(), "Driver #" + r.getDriverId())
                : "Self Drive";
            tableModel.addRow(new Object[]{
                r.getRentalId(),
                customerName,
                r.getStartDate().toString(),
                r.getEndDate().toString(),
                driverName,
                r.getTotalAmount(),
                r.getStatus()
            });
        }
    }

    // ── Manage Booking dialog ─────────────────────────────────
    private void showManageDialog() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int    rentalId = (int) tableModel.getValueAt(row, 0);
        String status   = String.valueOf(tableModel.getValueAt(row, 6));
        String startStr = String.valueOf(tableModel.getValueAt(row, 2));
        String customer = String.valueOf(tableModel.getValueAt(row, 1));

        LocalDate startDate = LocalDate.parse(startStr);
        boolean startOver24h = LocalDateTime.now().plusHours(24)
            .isBefore(startDate.atStartOfDay());

        // Build action options based on status
        String[] actions;
        if ("PENDING".equalsIgnoreCase(status)) {
            actions = new String[]{ "Approve (set ACTIVE)", "Cancel (set CANCELLED)" };
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            if (startOver24h)
                actions = new String[]{ "Complete (set COMPLETED)", "Cancel (set CANCELLED)" };
            else
                actions = new String[]{ "Complete (set COMPLETED)" };
        } else {
            JOptionPane.showMessageDialog(this,
                "This booking cannot be modified.",
                "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build driver list (only relevant for PENDING approve)
        List<Driver> drivers = new DriverDAO().getDriversByStatus(Driver.STATUS_VERIFIED);
        String[] driverOptions = new String[drivers.size() + 1];
        driverOptions[0] = "\u2014 No Driver \u2014";
        for (int i = 0; i < drivers.size(); i++)
            driverOptions[i + 1] = drivers.get(i).getFullName()
                + " (" + drivers.get(i).getLicenseNumber() + ")";

        JComboBox<String> actionCombo = new JComboBox<>(actions);
        actionCombo.setFont(UIAssets.FONT_INPUT);

        JComboBox<String> driverCombo = new JComboBox<>(driverOptions);
        driverCombo.setFont(UIAssets.FONT_INPUT);
        // Driver assignment only makes sense when approving a PENDING rental
        boolean showDriver = "PENDING".equalsIgnoreCase(status);
        driverCombo.setVisible(showDriver);

        JPanel form = new JPanel(new GridLayout(showDriver ? 2 : 1, 2, 10, 10));
        form.setBorder(new EmptyBorder(8, 8, 8, 8));
        form.add(new JLabel("Action:")); form.add(actionCombo);
        if (showDriver) {
            JLabel driverLbl = new JLabel("Assign Driver:");
            form.add(driverLbl);
            form.add(driverCombo);
            // Disable driver combo when cancelling
            actionCombo.addActionListener(e -> {
                boolean isApprove = actionCombo.getSelectedIndex() == 0;
                driverCombo.setEnabled(isApprove);
                driverLbl.setEnabled(isApprove);
            });
        }

        int result = JOptionPane.showConfirmDialog(this, form,
            "Manage Booking #" + rentalId + "  \u2014  " + customer,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String selected = (String) actionCombo.getSelectedItem();
        String newStatus;
        if (selected != null && selected.contains("ACTIVE"))       newStatus = "ACTIVE";
        else if (selected != null && selected.contains("COMPLETED")) newStatus = "COMPLETED";
        else                                                          newStatus = "CANCELLED";

        // Validate cancel rule for ACTIVE rentals
        if ("ACTIVE".equalsIgnoreCase(status) && "CANCELLED".equals(newStatus) && !startOver24h) {
            JOptionPane.showMessageDialog(this,
                "Cannot cancel: start date is within 24 hours.",
                "Not Allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Assign driver if approving a PENDING rental
        if ("PENDING".equalsIgnoreCase(status) && "ACTIVE".equals(newStatus)
                && driverCombo.getSelectedIndex() > 0) {
            Driver d = drivers.get(driverCombo.getSelectedIndex() - 1);
            new RentalDAO().assignDriver(rentalId, d.getDriverId());
        }

        if (RentalService.updateRentalStatus(rentalId, newStatus)) {
            JOptionPane.showMessageDialog(this,
                "Booking #" + rentalId + " set to " + newStatus + ".",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            applyFilters();
            updateCount();
            manageBtn.setEnabled(false);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to update booking status.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Filters ───────────────────────────────────────────────
    private static final String SEARCH_PLACEHOLDER = "Search rentals...";

    private void applyFilters() {
        String statusSel = (String) statusFilter.getSelectedItem();
        String raw       = searchField.getText().trim();
        String query     = raw.equals(SEARCH_PLACEHOLDER) ? "" : raw.toLowerCase();

        List<Rental> filtered = rentals.stream()
            .filter(r -> {
                boolean statusOk = "All".equals(statusSel)
                    || r.getStatus().equalsIgnoreCase(statusSel);
                if (!statusOk) return false;
                if (query.isEmpty()) return true;
                String customer = customerNames.getOrDefault(
                    r.getCustomerId(), "").toLowerCase();
                String driver = r.getDriverId() > 0
                    ? driverNames.getOrDefault(r.getDriverId(), "").toLowerCase()
                    : "self drive";
                return customer.contains(query) || driver.contains(query)
                    || String.valueOf(r.getRentalId()).contains(query);
            })
            .toList();

        populateTable(filtered);
    }

    // ── Export stub ───────────────────────────────────────────
    private void handleExport() {
        javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
        chooser.setDialogTitle("Export Rentals to CSV");
        chooser.setSelectedFile(new java.io.File("rentals_export.csv"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        if (chooser.showSaveDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".csv"))
            file = new java.io.File(file.getAbsolutePath() + ".csv");

        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(file))) {
            // Header
            pw.println("Rental #,Customer,Start Date,End Date,Driver Assigned,Total (PHP),Status");
            // Rows — export whatever is currently visible in the table
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.println(
                    tableModel.getValueAt(i, 0) + "," +
                    escapeCSV(String.valueOf(tableModel.getValueAt(i, 1))) + "," +
                    tableModel.getValueAt(i, 2) + "," +
                    tableModel.getValueAt(i, 3) + "," +
                    escapeCSV(String.valueOf(tableModel.getValueAt(i, 4))) + "," +
                    tableModel.getValueAt(i, 5) + "," +
                    tableModel.getValueAt(i, 6)
                );
            }
            JOptionPane.showMessageDialog(this,
                "Exported " + tableModel.getRowCount() + " records to:\n" + file.getAbsolutePath(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (java.io.IOException e) {
            JOptionPane.showMessageDialog(this,
                "Failed to export: " + e.getMessage(),
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }
}
