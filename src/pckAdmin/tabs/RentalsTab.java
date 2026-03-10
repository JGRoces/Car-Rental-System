package pckAdmin.tabs;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckDatabase.CustomerDAO;
import pckDatabase.RentalDAO;
import pckModels.Car;
import pckModels.Customer;
import pckModels.Rental;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
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

    private final RentalDAO   rentalDAO   = new RentalDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final CarDAO      carDAO      = new CarDAO();

    private List<Rental>         rentals;
    private Map<Integer, String> customerNames = new HashMap<>();
    private Map<Integer, String> carNames      = new HashMap<>();

    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JComboBox<String> statusFilter;
    private JLabel            countLabel;

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
        rentals = rentalDAO.getAllRentals();
        customerDAO.getAllCustomers().forEach(c ->
            customerNames.put(c.getCustomerId(), c.getFullName()));
        carDAO.getAllCars().forEach(c ->
            carNames.put(c.getCarId(), c.getBrand() + " " + c.getModel()
                + " (" + c.getPlateNumber() + ")"));
    }

    /** Called by ManagementPanel's global Refresh button */
    public void refresh() {
        customerNames.clear();
        carNames.clear();
        loadData();
        applyFilters();
        updateCount();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);
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

    private void updateCount() {
        if (countLabel != null)
            countLabel.setText(rentals.size() + " rental"
                + (rentals.size() == 1 ? "" : "s") + " total");
    }

    // ── Table ─────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = {
            "#", "Customer", "Vehicle", "Start Date", "End Date",
            "Total (\u20b1)", "Status"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = AdminUIHelper.buildStyledTable(cols, null);
        table.setModel(tableModel);

        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);

        table.getColumnModel().getColumn(cols.length - 1)
             .setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        populateTable(rentals);
        return AdminUIHelper.buildTableScrollPane(table);
    }

    private void populateTable(List<Rental> data) {
        tableModel.setRowCount(0);
        for (Rental r : data) {
            String customerName = customerNames.getOrDefault(
                r.getCustomerId(), "Customer #" + r.getCustomerId());
            String carName = carNames.getOrDefault(
                r.getCarId(), "Car #" + r.getCarId());
            tableModel.addRow(new Object[]{
                r.getRentalId(),
                customerName,
                carName,
                r.getStartDate().toString(),
                r.getEndDate().toString(),
                r.getTotalAmount(),
                r.getStatus()
            });
        }
    }

    // ── Filters ───────────────────────────────────────────────
    private void applyFilters() {
        String statusSel = (String) statusFilter.getSelectedItem();
        String query     = searchField.getText().trim().toLowerCase();

        List<Rental> filtered = rentals.stream()
            .filter(r -> {
                boolean statusOk = "All".equals(statusSel)
                    || r.getStatus().equalsIgnoreCase(statusSel);
                if (!statusOk) return false;
                if (query.isEmpty()) return true;
                String customer = customerNames.getOrDefault(
                    r.getCustomerId(), "").toLowerCase();
                String car = carNames.getOrDefault(
                    r.getCarId(), "").toLowerCase();
                return customer.contains(query) || car.contains(query)
                    || String.valueOf(r.getRentalId()).contains(query);
            })
            .toList();

        populateTable(filtered);
    }

    // ── Export stub ───────────────────────────────────────────
    private void handleExport() {
        JOptionPane.showMessageDialog(this,
            "Export to CSV — coming soon.",
            "Export", JOptionPane.INFORMATION_MESSAGE);
    }
}
