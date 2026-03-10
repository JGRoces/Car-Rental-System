package pckAdmin.tabs;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.DriverDAO;
import pckModels.Driver;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * DriversTab.java
 * ─────────────────────────────────────────────────────────────
 * Drivers sub-tab inside ManagementPanel.
 *
 * Layout:
 *   ┌─ toolbar ────────────────────────────────────────────────┐
 *   │  "X pending · X verified"           [Search drivers...]  │
 *   └──────────────────────────────────────────────────────────┘
 *   ┌─ "Pending Review" card ──────────────────────────────────┐
 *   │  ● dot · title                            [X pending]    │
 *   │  table: Photo | Name | Phone | License | Type |          │
 *   │          Expiry | Submitted | [Verify] [Reject]          │
 *   └──────────────────────────────────────────────────────────┘
 *   ┌─ "Verified Drivers" card ────────────────────────────────┐
 *   │  ● dot · title                           [X verified]    │
 *   │  table: Photo | Name | Phone | License | Type |          │
 *   │          Expiry | Verified Date                          │
 *   └──────────────────────────────────────────────────────────┘
 *
 * Global refresh via ManagementPanel — no per-section button.
 * Search filters both tables simultaneously.
 */
public class DriversTab extends JPanel {

    private final DriverDAO driverDAO = new DriverDAO();

    private DefaultTableModel pendingModel;
    private DefaultTableModel verifiedModel;
    private JTable            pendingTable;
    private JTable            verifiedTable;
    private JTextField        searchField;

    // Count labels — updated on every data load or verify/reject
    private JLabel toolbarCountLabel;
    private JLabel pendingCountBadge;
    private JLabel verifiedCountBadge;

    // Raw lists kept for search filtering without re-querying DB
    private List<Driver> allPending;
    private List<Driver> allVerified;

    private static final int COL_PHOTO   = 0;
    private static final int COL_ACTIONS = 7;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public DriversTab() {
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
        allPending  = driverDAO.getPendingDrivers();
        allVerified = driverDAO.getDriversByStatus(Driver.STATUS_VERIFIED);
    }

    /** Called by ManagementPanel's global Refresh button */
    public void refresh() {
        loadData();
        applySearch();
        updateCounts();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(buildSection("Pending Review",   buildPendingTable(),
                UIAssets.CLR_YELLOW, UIAssets.CLR_YELLOW_LIGHT, true));
        body.add(Box.createVerticalStrut(20));
        body.add(buildSection("Verified Drivers", buildVerifiedTable(),
                UIAssets.CLR_GREEN,  UIAssets.CLR_GREEN_LIGHT,  false));
        add(body, BorderLayout.CENTER);
    }

    // ── Toolbar: summary counts + search ──────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        toolbarCountLabel = new JLabel();
        toolbarCountLabel.setFont(UIAssets.FONT_H3);
        toolbarCountLabel.setForeground(UIAssets.getTextSecondary());

        searchField = AdminUIHelper.buildTextField("Search drivers...");
        searchField.setPreferredSize(new Dimension(280, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applySearch(); }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(buildSearchWithIcon(searchField));

        bar.add(toolbarCountLabel, BorderLayout.WEST);
        bar.add(right,             BorderLayout.EAST);
        return bar;
    }

    // ── Section card ──────────────────────────────────────────
    private JPanel buildSection(String title, JScrollPane tableScroll,
                                Color accent, Color accentLight, boolean isPending) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UIAssets.getSurface());
        card.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // ── Header ───────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()),
            new EmptyBorder(12, 18, 12, 18)
        ));

        // Left: accent dot + title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentLight);
                g2.fillOval(0, 0, 16, 16);
                g2.setColor(accent);
                g2.fillOval(5, 5, 6, 6);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(16, 16));
        dot.setOpaque(false);

        JLabel titleLbl = AdminUIHelper.buildSectionLabel(title);
        titleRow.add(dot);
        titleRow.add(titleLbl);

        // Right: count pill badge
        JLabel badge = buildCountPill(accent, accentLight);
        if (isPending) pendingCountBadge  = badge;
        else           verifiedCountBadge = badge;

        JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightWrap.setOpaque(false);
        rightWrap.add(badge);

        header.add(titleRow,  BorderLayout.WEST);
        header.add(rightWrap, BorderLayout.EAST);
        card.add(header,      BorderLayout.NORTH);
        card.add(tableScroll, BorderLayout.CENTER);

        // Set initial count now that badge reference is saved
        updateCounts();
        return card;
    }

    /** Rounded pill label, text set by updateCounts() */
    private JLabel buildCountPill(Color accent, Color accentLight) {
        final Color fg = accent, bg = accentLight;
        JLabel lbl = new JLabel("", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(UIAssets.FONT_SMALL);
        lbl.setForeground(fg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 12, 3, 12));
        return lbl;
    }

    private void updateCounts() {
        int p = allPending  != null ? allPending.size()  : 0;
        int v = allVerified != null ? allVerified.size() : 0;

        if (toolbarCountLabel  != null)
            toolbarCountLabel.setText(p + " pending  ·  " + v + " verified");
        if (pendingCountBadge  != null)
            pendingCountBadge.setText(p + (p == 1 ? " pending" : " pending"));
        if (verifiedCountBadge != null)
            verifiedCountBadge.setText(v + (v == 1 ? " verified" : " verified"));
    }

    // =========================================================
    //  TABLES
    // =========================================================
    private JScrollPane buildPendingTable() {
        String[] cols = { "Photo", "Name", "Phone", "License No.", "Vehicle Type",
                          "License Expiry", "Submitted", "Actions" };

        pendingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == COL_PHOTO ? ImageIcon.class : Object.class;
            }
        };

        pendingTable = AdminUIHelper.buildStyledTable(cols, null);
        pendingTable.setModel(pendingModel);
        pendingTable.setRowHeight(52);

        pendingTable.getColumnModel().getColumn(COL_PHOTO)
            .setCellRenderer(new CircularPhotoRenderer());
        pendingTable.getColumnModel().getColumn(COL_PHOTO).setMaxWidth(60);

        pendingTable.getColumnModel().getColumn(COL_ACTIONS)
            .setCellRenderer(new ActionButtonRenderer());
        pendingTable.getColumnModel().getColumn(COL_ACTIONS)
            .setCellEditor(new ActionButtonEditor(pendingTable));
        pendingTable.getColumnModel().getColumn(COL_ACTIONS).setMinWidth(178);

        populatePending(allPending);
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(pendingTable);
        scroll.setPreferredSize(new Dimension(0, 220));
        return scroll;
    }

    private JScrollPane buildVerifiedTable() {
        String[] cols = { "Photo", "Name", "Phone", "License No.", "Vehicle Type",
                          "License Expiry", "Verified Date" };

        verifiedModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == COL_PHOTO ? ImageIcon.class : Object.class;
            }
        };

        verifiedTable = AdminUIHelper.buildStyledTable(cols, null);
        verifiedTable.setModel(verifiedModel);
        verifiedTable.setRowHeight(52);

        verifiedTable.getColumnModel().getColumn(COL_PHOTO)
            .setCellRenderer(new CircularPhotoRenderer());
        verifiedTable.getColumnModel().getColumn(COL_PHOTO).setMaxWidth(60);

        populateVerified(allVerified);
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(verifiedTable);
        scroll.setPreferredSize(new Dimension(0, 220));
        return scroll;
    }

    // =========================================================
    //  DATA POPULATION
    // =========================================================
    private void populatePending(List<Driver> data) {
        pendingModel.setRowCount(0);
        if (data == null) return;
        for (Driver d : data) {
            pendingModel.addRow(new Object[]{
                loadCircularIcon(AppConfig.driverPhoto(d.getDriverId()), 36),
                d.getFullName(), d.getPhone(), d.getLicenseNumber(),
                d.getVehicleType(), d.getLicenseExpiry(), "Pending",
                d.getDriverId()
            });
        }
    }

    private void populateVerified(List<Driver> data) {
        verifiedModel.setRowCount(0);
        if (data == null) return;
        for (Driver d : data) {
            verifiedModel.addRow(new Object[]{
                loadCircularIcon(AppConfig.driverPhoto(d.getDriverId()), 36),
                d.getFullName(), d.getPhone(), d.getLicenseNumber(),
                d.getVehicleType(), d.getLicenseExpiry(),
                d.getVerifiedAt() != null ? d.getVerifiedAt() : "—"
            });
        }
    }

    // ── Search ────────────────────────────────────────────────
    private void applySearch() {
        if (searchField == null) return;
        String q = searchField.getText().trim().toLowerCase();
        List<Driver> fp = (allPending  == null) ? List.of() :
            allPending.stream().filter(d -> matches(d, q)).toList();
        List<Driver> fv = (allVerified == null) ? List.of() :
            allVerified.stream().filter(d -> matches(d, q)).toList();
        populatePending(fp);
        populateVerified(fv);
    }

    private boolean matches(Driver d, String q) {
        if (q.isEmpty()) return true;
        return d.getFullName().toLowerCase().contains(q)
            || (d.getPhone()         != null && d.getPhone().toLowerCase().contains(q))
            || (d.getLicenseNumber() != null && d.getLicenseNumber().toLowerCase().contains(q))
            || (d.getVehicleType()   != null && d.getVehicleType().toLowerCase().contains(q));
    }

    // =========================================================
    //  VERIFY / REJECT ACTIONS
    // =========================================================
    private void verifyDriver(int driverId, int pendingRow) {
        boolean ok = driverDAO.updateStatus(driverId, Driver.STATUS_VERIFIED);
        if (ok) {
            pendingModel.removeRow(pendingRow);
            allPending.removeIf(d -> d.getDriverId() == driverId);
            allVerified = driverDAO.getDriversByStatus(Driver.STATUS_VERIFIED);
            populateVerified(allVerified);
            updateCounts();
            JOptionPane.showMessageDialog(this,
                "Driver verified successfully.", "Verified",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to verify driver. Please try again.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectDriver(int driverId, int pendingRow) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reject this driver?",
            "Reject Driver", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = driverDAO.updateStatus(driverId, Driver.STATUS_REJECTED);
        if (ok) {
            pendingModel.removeRow(pendingRow);
            allPending.removeIf(d -> d.getDriverId() == driverId);
            updateCounts();
            JOptionPane.showMessageDialog(this,
                "Driver rejected.", "Rejected",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to reject driver. Please try again.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    //  SEARCH FIELD WITH ICON
    // =========================================================
    private JPanel buildSearchWithIcon(JTextField field) {
        ImageIcon searchIcon = AdminUIHelper.loadIcon(
                AppConfig.ICON_SEARCH, UIAssets.getTextSecondary(), 14);

        JPanel wrap = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(UIAssets.getBorder());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        int iconW = (searchIcon != null) ? 34 : 0;
        wrap.setPreferredSize(new Dimension(
                field.getPreferredSize().width + iconW,
                field.getPreferredSize().height));

        if (searchIcon != null) {
            JLabel iconLbl = new JLabel(searchIcon);
            iconLbl.setBorder(new EmptyBorder(0, 10, 0, 2));
            wrap.add(iconLbl, BorderLayout.WEST);
        }
        field.setBorder(new EmptyBorder(0, 4, 0, 10));
        field.setOpaque(false);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
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
        } catch (Exception e) { return null; }
    }

    // =========================================================
    //  RENDERERS
    // =========================================================
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

    // =========================================================
    //  ACTION BUTTON RENDERER + EDITOR
    // =========================================================
    private class ActionButtonRenderer implements TableCellRenderer {
        private final JPanel panel = buildBtnPanel();
        @Override public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            return panel;
        }
    }

    private JPanel buildBtnPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        p.setOpaque(true);
        p.setBackground(UIAssets.getSurface());

        JButton v = AdminUIHelper.buildSolidButtonWithIcon(
                "Verify", AppConfig.ICON_VERIFY, UIAssets.CLR_GREEN);
        JButton r = AdminUIHelper.buildOutlineButtonWithIcon(
                "Reject", AppConfig.ICON_REJECT);
        r.setForeground(UIAssets.CLR_RED);
        ImageIcon ri = AdminUIHelper.loadIcon(AppConfig.ICON_REJECT, UIAssets.CLR_RED, 15);
        if (ri != null) r.setIcon(ri);

        v.setPreferredSize(new Dimension(86, 30));
        r.setPreferredSize(new Dimension(86, 30));
        p.add(v); p.add(r);
        return p;
    }

    private class ActionButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel  panel;
        private final JButton verifyBtn;
        private final JButton rejectBtn;
        private       int     editingRow;

        ActionButtonEditor(JTable table) {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
            verifyBtn = AdminUIHelper.buildSolidButtonWithIcon(
                    "Verify", AppConfig.ICON_VERIFY, UIAssets.CLR_GREEN);
            rejectBtn = AdminUIHelper.buildOutlineButtonWithIcon(
                    "Reject", AppConfig.ICON_REJECT);
            rejectBtn.setForeground(UIAssets.CLR_RED);
            ImageIcon ri = AdminUIHelper.loadIcon(
                    AppConfig.ICON_REJECT, UIAssets.CLR_RED, 15);
            if (ri != null) rejectBtn.setIcon(ri);

            verifyBtn.setPreferredSize(new Dimension(86, 30));
            rejectBtn.setPreferredSize(new Dimension(86, 30));
            panel.setOpaque(true);
            panel.setBackground(UIAssets.getSurface());
            panel.add(verifyBtn);
            panel.add(rejectBtn);

            verifyBtn.addActionListener(e -> {
                int driverId = (int) pendingModel.getValueAt(editingRow, COL_ACTIONS);
                stopCellEditing();
                verifyDriver(driverId, editingRow);
            });
            rejectBtn.addActionListener(e -> {
                int driverId = (int) pendingModel.getValueAt(editingRow, COL_ACTIONS);
                stopCellEditing();
                rejectDriver(driverId, editingRow);
            });
        }

        @Override public Component getTableCellEditorComponent(
                JTable table, Object val, boolean sel, int row, int col) {
            editingRow = row;
            return panel;
        }
        @Override public Object getCellEditorValue() { return null; }
    }
}
