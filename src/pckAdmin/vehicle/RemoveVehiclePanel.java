package pckAdmin.vehicle;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckModels.Car;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * RemoveVehiclePanel.java
 * ─────────────────────────────────────────────────────────────
 * Full-page confirmation panel for removing a vehicle.
 * Fills the entire content area wall-to-wall — no narrow card.
 *
 * Layout (full-width, no scroll needed):
 *   ← Back to Vehicles
 *   ─────────────────────────────────────────────────────────
 *   Remove Vehicle  /  subtitle
 *   ─────────────────────────────────────────────────────────
 *   [ Vehicle summary card        ] │ [ ⚠ Warning notice      ]
 *   ─────────────────────────────────────────────────────────
 *   Reason for Removal *            │ Notes (optional)
 *   [ dropdown                    ] │ [ textarea               ]
 *   ─────────────────────────────────────────────────────────
 *                              [Cancel]  [Remove Vehicle]
 */
public class RemoveVehiclePanel extends JPanel {

    private static final String[] REASONS = {
        "Select a reason...", "Sold", "Damaged / Totaled",
        "Decommissioned", "Other"
    };

    private final CarDAO   carDAO = new CarDAO();
    private final Runnable onBack;
    private final Runnable onSuccess;
    private Car            currentCar;

    // Dynamic components
    private JLabel            carNameLbl;
    private JLabel            carMetaLbl;
    private JLabel            carPlateLbl;
    private JLabel            carStatusBadge;
    private JPanel            carImagePanel;
    private JComboBox<String> reasonBox;
    private JTextArea         notesArea;
    private JLabel            reasonErr;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public RemoveVehiclePanel(Runnable onBack, Runnable onSuccess) {
        this.onBack    = onBack;
        this.onSuccess = onSuccess;
        setLayout(new BorderLayout());
        setBackground(UIAssets.getSurface());
        build();
    }

    // =========================================================
    //  LOAD
    // =========================================================
    public void load(Car car) {
        this.currentCar = car;

        carNameLbl.setText(car.getBrand() + " " + car.getModel());
        carMetaLbl.setText(car.getYear() + "  ·  " + car.getCategory()
            + "  ·  " + car.getTransmission());
        carPlateLbl.setText("Plate: " + car.getPlateNumber());
        updateStatusBadge(car.getStatus());

        carImagePanel.revalidate();
        carImagePanel.repaint();

        reasonBox.setSelectedIndex(0);
        notesArea.setText("");
        reasonErr.setText(" ");
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildBackBar(), BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);
    }

    // ── Back bar ──────────────────────────────────────────────
    private JPanel buildBackBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(24, 48, 0, 48));

        JButton backBtn = new JButton("← Back to Vehicles");
        backBtn.setFont(UIAssets.FONT_H3);
        backBtn.setForeground(UIAssets.CLR_BLUE);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                backBtn.setForeground(UIAssets.CLR_BLUE_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                backBtn.setForeground(UIAssets.CLR_BLUE);
            }
        });
        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });
        bar.add(backBtn);
        return bar;
    }

    // ── Body: full-width, two-column layout ───────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(18, 48, 36, 48));

        // Header
        JLabel title = new JLabel("Remove Vehicle");
        title.setFont(UIAssets.FONT_TITLE);
        title.setForeground(UIAssets.CLR_RED);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("This action will permanently remove the vehicle from the fleet.");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(title);
        body.add(Box.createVerticalStrut(4));
        body.add(sub);
        body.add(Box.createVerticalStrut(18));
        body.add(divider());
        body.add(Box.createVerticalStrut(22));

        // Row 1: Vehicle summary (left) | Warning notice (right)
        body.add(splitRow(buildVehicleSummary(), buildWarningNotice(), 160));
        body.add(Box.createVerticalStrut(22));
        body.add(divider());
        body.add(Box.createVerticalStrut(22));

        // Row 2: Reason dropdown (left) | Notes textarea (right)
        body.add(splitRow(buildReasonCol(), buildNotesCol(), 110));
        body.add(Box.createVerticalStrut(28));

        body.add(divider());
        body.add(Box.createVerticalStrut(20));
        body.add(buildFooterRow());

        return body;
    }

    // ── Vehicle summary ───────────────────────────────────────
    private JPanel buildVehicleSummary() {
        JPanel summary = new JPanel(new BorderLayout(16, 0));
        summary.setBackground(UIAssets.getBg());
        summary.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(18, 18, 18, 18)));

        // Image thumbnail
        carImagePanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentCar == null) return;
                String path = AppConfig.vehiclePhoto(
                    currentCar.getCarId(), currentCar.getCategory());
                try {
                    java.io.File f = new java.io.File(path);
                    if (!f.exists()) return;
                    BufferedImage src = javax.imageio.ImageIO.read(f);
                    if (src == null) return;
                    Image scaled = src.getScaledInstance(
                        getWidth(), getHeight(), Image.SCALE_SMOOTH);
                    g.drawImage(scaled, 0, 0, null);
                } catch (Exception ignored) {}
            }
            @Override public Dimension getPreferredSize() { return new Dimension(90, 76); }
            @Override public Dimension getMinimumSize()   { return new Dimension(90, 76); }
        };
        carImagePanel.setBackground(UIAssets.getBorder());
        carImagePanel.setPreferredSize(new Dimension(90, 76));

        carNameLbl = new JLabel("—");
        carNameLbl.setFont(UIAssets.FONT_H2);
        carNameLbl.setForeground(UIAssets.getTextPrimary());

        carMetaLbl = new JLabel("—");
        carMetaLbl.setFont(UIAssets.FONT_SMALL);
        carMetaLbl.setForeground(UIAssets.getTextSecondary());

        carPlateLbl = new JLabel("—");
        carPlateLbl.setFont(UIAssets.FONT_SMALL);
        carPlateLbl.setForeground(UIAssets.getTextSecondary());

        carStatusBadge = new JLabel("—");
        carStatusBadge.setFont(UIAssets.FONT_SMALL);

        JPanel textCol = new JPanel();
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        textCol.setOpaque(false);
        textCol.add(carNameLbl);
        textCol.add(Box.createVerticalStrut(4));
        textCol.add(carMetaLbl);
        textCol.add(Box.createVerticalStrut(2));
        textCol.add(carPlateLbl);
        textCol.add(Box.createVerticalStrut(8));
        textCol.add(carStatusBadge);

        summary.add(carImagePanel, BorderLayout.WEST);
        summary.add(textCol,       BorderLayout.CENTER);
        return summary;
    }

    // ── Warning notice ────────────────────────────────────────
    private JPanel buildWarningNotice() {
        JPanel notice = new JPanel(new BorderLayout(14, 0));
        notice.setBackground(UIAssets.CLR_RED_LIGHT);
        notice.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_RED, 1, true),
            new EmptyBorder(18, 18, 18, 18)));

        JLabel icon = new JLabel("⚠");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        icon.setForeground(UIAssets.CLR_RED);
        icon.setVerticalAlignment(SwingConstants.TOP);

        JLabel msg = new JLabel(
            "<html>This will permanently delete the vehicle record."
            + " This action <b>cannot be undone</b>."
            + " Vehicles with active rentals cannot be removed.</html>");
        msg.setFont(UIAssets.FONT_SMALL);
        msg.setForeground(UIAssets.CLR_RED);

        notice.add(icon, BorderLayout.WEST);
        notice.add(msg,  BorderLayout.CENTER);
        return notice;
    }

    // ── Reason column ─────────────────────────────────────────
    private JPanel buildReasonCol() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lbl = AdminUIHelper.buildFieldLabel("Reason for Removal *");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        reasonBox = AdminUIHelper.buildComboBox(REASONS);
        reasonBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        reasonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        reasonErr = new JLabel(" ");
        reasonErr.setFont(UIAssets.FONT_SMALL);
        reasonErr.setForeground(UIAssets.CLR_RED);
        reasonErr.setAlignmentX(Component.LEFT_ALIGNMENT);

        col.add(lbl);
        col.add(Box.createVerticalStrut(6));
        col.add(reasonBox);
        col.add(reasonErr);
        return col;
    }

    // ── Notes column ──────────────────────────────────────────
    private JPanel buildNotesCol() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lbl = AdminUIHelper.buildFieldLabel("Notes  (optional)");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        notesArea = new JTextArea(3, 0);
        notesArea.setFont(UIAssets.FONT_INPUT);
        notesArea.setForeground(UIAssets.getTextPrimary());
        notesArea.setBackground(UIAssets.getSurface());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(10, 14, 10, 14)));

        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(null);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        col.add(lbl);
        col.add(Box.createVerticalStrut(6));
        col.add(notesScroll);
        return col;
    }

    // ── Footer ────────────────────────────────────────────────
    private JPanel buildFooterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton cancelBtn = AdminUIHelper.buildOutlineButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(110, 44));
        cancelBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        JButton removeBtn = AdminUIHelper.buildSolidButton("Remove Vehicle", UIAssets.CLR_RED);
        removeBtn.setPreferredSize(new Dimension(155, 44));
        removeBtn.addActionListener(e -> handleRemove());

        row.add(cancelBtn);
        row.add(removeBtn);
        return row;
    }

    // =========================================================
    //  LAYOUT HELPERS
    // =========================================================

    /**
     * Two equal columns side by side, with a fixed row height.
     * Both columns expand to fill the full available width.
     */
    private JPanel splitRow(JPanel left, JPanel right, int height) {
        JPanel row = new JPanel(new GridLayout(1, 2, 24, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        row.setPreferredSize(new Dimension(Integer.MAX_VALUE, height));
        row.add(left);
        row.add(right);
        return row;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    // =========================================================
    //  REMOVE
    // =========================================================
    private void handleRemove() {
        if (currentCar == null) return;

        if (reasonBox.getSelectedIndex() == 0) {
            reasonErr.setText("Please select a reason for removal.");
            return;
        }
        reasonErr.setText(" ");

        String reason = (String) reasonBox.getSelectedItem();
        String notes  = notesArea.getText().trim();

        String confirmMsg = "<html>You are about to permanently remove:<br>"
            + "<b>" + currentCar.getBrand() + " " + currentCar.getModel()
            + " (" + currentCar.getPlateNumber() + ")</b><br><br>"
            + "Reason: " + reason
            + (notes.isBlank() ? "" : "<br>Notes: " + notes)
            + "<br><br>This cannot be undone. Continue?</html>";

        int confirm = JOptionPane.showConfirmDialog(this, confirmMsg,
            "Confirm Removal", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = carDAO.deleteCar(currentCar.getCarId());
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                "<html>Could not remove this vehicle.<br>"
                + "It may have linked rental records — complete or cancel"
                + " those rentals first.</html>",
                "Removal Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
            currentCar.getBrand() + " " + currentCar.getModel()
            + " has been removed from the fleet.",
            "Vehicle Removed", JOptionPane.INFORMATION_MESSAGE);

        if (onSuccess != null) onSuccess.run();
    }

    // =========================================================
    //  HELPERS
    // =========================================================
    private void updateStatusBadge(String status) {
        Color fg;
        switch (status.toUpperCase()) {
            case "AVAILABLE"   -> fg = UIAssets.CLR_GREEN;
            case "RENTED"      -> fg = UIAssets.CLR_YELLOW;
            case "MAINTENANCE" -> fg = UIAssets.CLR_RED;
            default            -> fg = UIAssets.getTextSecondary();
        }
        carStatusBadge.setText(status);
        carStatusBadge.setForeground(fg);
    }
}
