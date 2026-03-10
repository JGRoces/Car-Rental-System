package pckAdmin.tabs;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CustomerDAO;
import pckDatabase.RentalDAO;
import pckModels.Customer;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * CustomersTab.java
 * ─────────────────────────────────────────────────────────────
 * Customers sub-tab inside ManagementPanel.
 *
 * Refresh is handled globally by ManagementPanel — no per-tab button here.
 */
public class CustomersTab extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RentalDAO   rentalDAO   = new RentalDAO();

    private List<Customer>    customers;
    private DefaultTableModel tableModel;
    private JTable            table;
    private JTextField        searchField;
    private JLabel            countLabel;

    private static final int COL_ACTIVE = 0;
    private static final int COL_PHOTO  = 1;

    public CustomersTab() {
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
        customers = customerDAO.getAllCustomers();
    }

    /** Called by ManagementPanel's global Refresh button */
    public void refresh() {
        loadData();
        populateTable(customers);
        updateCount();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTable(),   BorderLayout.CENTER);
    }

    // ── Toolbar: count label + search only ────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        countLabel = new JLabel();
        countLabel.setFont(UIAssets.FONT_H3);
        countLabel.setForeground(UIAssets.getTextSecondary());
        updateCount();

        searchField = AdminUIHelper.buildTextField("Search customers...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applySearch(); }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(searchField);

        bar.add(countLabel, BorderLayout.WEST);
        bar.add(right,      BorderLayout.EAST);
        return bar;
    }

    private void updateCount() {
        if (countLabel != null)
            countLabel.setText(customers.size() + " customer"
                + (customers.size() == 1 ? "" : "s") + " total");
    }

    // ── Table ─────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = { "", "Photo", "Name", "Email", "Phone", "Active Rental" };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == COL_ACTIVE) return Boolean.class;
                if (c == COL_PHOTO)  return ImageIcon.class;
                return Object.class;
            }
        };

        table = AdminUIHelper.buildStyledTable(cols, null);
        table.setModel(tableModel);
        table.setRowHeight(52);

        table.getColumnModel().getColumn(COL_ACTIVE)
            .setCellRenderer(new ActiveRentalRenderer());
        table.getColumnModel().getColumn(COL_ACTIVE).setMaxWidth(28);
        table.getColumnModel().getColumn(COL_ACTIVE).setMinWidth(28);

        table.getColumnModel().getColumn(COL_PHOTO)
            .setCellRenderer(new CircularPhotoRenderer());
        table.getColumnModel().getColumn(COL_PHOTO).setMaxWidth(60);

        populateTable(customers);
        return AdminUIHelper.buildTableScrollPane(table);
    }

    private void populateTable(List<Customer> data) {
        tableModel.setRowCount(0);
        for (Customer c : data) {
            boolean hasActive = rentalDAO.hasActiveRental(c.getCustomerId());
            String  photo     = AppConfig.customerPhoto(c.getCustomerId());
            tableModel.addRow(new Object[]{
                hasActive,
                loadCircularIcon(photo, 36),
                c.getFullName(),
                c.getEmail(),
                c.getPhone() != null ? c.getPhone() : "—",
                hasActive ? "Active" : "None"
            });
        }
    }

    // ── Search ────────────────────────────────────────────────
    private void applySearch() {
        String q = searchField.getText().trim().toLowerCase();
        List<Customer> filtered = customers.stream()
            .filter(c -> q.isEmpty()
                || c.getFullName().toLowerCase().contains(q)
                || c.getEmail().toLowerCase().contains(q)
                || (c.getPhone() != null && c.getPhone().toLowerCase().contains(q)))
            .toList();
        tableModel.setRowCount(0);
        for (Customer c : filtered) {
            boolean hasActive = rentalDAO.hasActiveRental(c.getCustomerId());
            String  photo     = AppConfig.customerPhoto(c.getCustomerId());
            tableModel.addRow(new Object[]{
                hasActive,
                loadCircularIcon(photo, 36),
                c.getFullName(),
                c.getEmail(),
                c.getPhone() != null ? c.getPhone() : "—",
                hasActive ? "Active" : "None"
            });
        }
    }

    // =========================================================
    //  CIRCULAR PHOTO HELPER
    // =========================================================
    private ImageIcon loadCircularIcon(String path, int size) {
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) f = new java.io.File(AppConfig.DEFAULT_AVATAR);
            java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(f);
            if (src == null) return null;

            java.awt.image.BufferedImage out =
                new java.awt.image.BufferedImage(size, size,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
            g2.drawImage(src.getScaledInstance(size, size, Image.SCALE_SMOOTH), 0, 0, null);
            g2.setClip(null);
            g2.setColor(UIAssets.getBorder());
            g2.setStroke(new java.awt.BasicStroke(1.2f));
            g2.drawOval(0, 0, size - 1, size - 1);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception e) {
            return null;
        }
    }

    // =========================================================
    //  RENDERERS
    // =========================================================
    private static class ActiveRentalRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            boolean active = Boolean.TRUE.equals(val);
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    int cx = getWidth() / 2, cy = getHeight() / 2, r = 5;
                    if (active) {
                        g2.setColor(UIAssets.CLR_GREEN);
                        g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                    } else {
                        g2.setColor(UIAssets.getBorder());
                        g2.setStroke(new java.awt.BasicStroke(1.5f));
                        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    }
                    g2.dispose();
                }
            };
            dot.setOpaque(true);
            dot.setBackground(sel ? UIAssets.CLR_BLUE_LIGHT : UIAssets.getSurface());
            return dot;
        }
    }

    private static class CircularPhotoRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            JLabel lbl = new JLabel();
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setBackground(sel ? UIAssets.CLR_BLUE_LIGHT : UIAssets.getSurface());
            if (val instanceof ImageIcon ico) lbl.setIcon(ico);
            return lbl;
        }
    }
}
