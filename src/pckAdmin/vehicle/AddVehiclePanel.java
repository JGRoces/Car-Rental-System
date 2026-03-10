package pckAdmin.vehicle;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckModels.Car;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;

/**
 * AddVehiclePanel.java
 * ─────────────────────────────────────────────────────────────
 * Full-page panel for adding a new vehicle to the fleet.
 * Fills the entire content area wall-to-wall — no narrow card.
 *
 * Layout (full-width, no scroll needed):
 *   ← Back to Vehicles
 *   ─────────────────────────────────────────────────────────
 *   Add New Vehicle  /  subtitle
 *   ─────────────────────────────────────────────────────────
 *   Brand *          │ Model *          │ Year *
 *   Color            │ Plate Number *   │ Seat Capacity *
 *   Category *       │ Transmission *   │ Fuel Type *
 *   Daily Rate (₱) * │ Vehicle Image ────────── [Browse]
 *   ─────────────────────────────────────────────────────────
 *                                   [Cancel]  [Add Vehicle]
 */
public class AddVehiclePanel extends JPanel {

    private static final String[] CATEGORIES    =
        { "Sedan", "SUV", "Van", "Truck", "Pickup", "Coupe", "Minivan", "MPV" };
    private static final String[] TRANSMISSIONS =
        { "Automatic", "Manual", "CVT" };
    private static final String[] FUEL_TYPES    =
        { "Gasoline", "Diesel", "Hybrid", "Electric" };

    private JTextField        brandField, modelField, yearField, colorField,
                              plateField, seatsField, rateField;
    private JComboBox<String> categoryBox, transmissionBox, fuelTypeBox;
    private JLabel            imageFileLabel;
    private File              selectedImageFile = null;

    private JLabel brandErr, modelErr, yearErr, plateErr, seatsErr, rateErr;

    private final CarDAO   carDAO = new CarDAO();
    private final Runnable onBack;
    private final Runnable onSuccess;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public AddVehiclePanel(Runnable onBack, Runnable onSuccess) {
        this.onBack    = onBack;
        this.onSuccess = onSuccess;
        setLayout(new BorderLayout());
        setBackground(UIAssets.getSurface());
        build();
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

    // ── Body: title + all fields, full-width ──────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(18, 48, 36, 48));

        // Title
        JLabel title = new JLabel("Add New Vehicle");
        title.setFont(UIAssets.FONT_TITLE);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Fill in the details below to add a vehicle to the fleet");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(title);
        body.add(Box.createVerticalStrut(4));
        body.add(sub);
        body.add(Box.createVerticalStrut(18));
        body.add(divider());
        body.add(Box.createVerticalStrut(22));

        // ── Build fields ──────────────────────────────────────
        brandField      = AdminUIHelper.buildTextField("e.g. Toyota");
        modelField      = AdminUIHelper.buildTextField("e.g. Vios");
        yearField       = AdminUIHelper.buildTextField("e.g. 2022");
        colorField      = AdminUIHelper.buildTextField("e.g. Pearl White (optional)");
        plateField      = AdminUIHelper.buildTextField("e.g. ABC 1234");
        seatsField      = AdminUIHelper.buildTextField("e.g. 5");
        categoryBox     = AdminUIHelper.buildComboBox(CATEGORIES);
        transmissionBox = AdminUIHelper.buildComboBox(TRANSMISSIONS);
        fuelTypeBox     = AdminUIHelper.buildComboBox(FUEL_TYPES);
        rateField       = AdminUIHelper.buildTextField("e.g. 1500.00");

        brandErr = errLbl(); modelErr = errLbl(); yearErr  = errLbl();
        plateErr = errLbl(); seatsErr = errLbl(); rateErr  = errLbl();

        // Row 1: Brand | Model | Year
        body.add(row3(
            col("Brand *",              brandField,      brandErr),
            col("Model *",              modelField,      modelErr),
            col("Year *",               yearField,       yearErr)));
        body.add(Box.createVerticalStrut(16));

        // Row 2: Color | Plate Number | Seat Capacity
        body.add(row3(
            col("Color",                colorField,      null),
            col("Plate Number *",       plateField,      plateErr),
            col("Seat Capacity *",      seatsField,      seatsErr)));
        body.add(Box.createVerticalStrut(16));

        // Row 3: Category | Transmission | Fuel Type
        body.add(row3(
            col("Category *",           categoryBox,     null),
            col("Transmission *",       transmissionBox, null),
            col("Fuel Type *",          fuelTypeBox,     null)));
        body.add(Box.createVerticalStrut(16));

        // Row 4: Daily Rate | Vehicle Image (2 cols)
        body.add(row2(
            col("Daily Rate (\u20b1) *", rateField,      rateErr),
            buildImageCol()));
        body.add(Box.createVerticalStrut(28));

        body.add(divider());
        body.add(Box.createVerticalStrut(20));
        body.add(buildFooterRow());

        return body;
    }

    // ── Image column ──────────────────────────────────────────
    private JPanel buildImageCol() {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lbl = AdminUIHelper.buildFieldLabel("Vehicle Image  (optional)");
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        imageFileLabel = new JLabel("No file selected");
        imageFileLabel.setFont(UIAssets.FONT_BODY);
        imageFileLabel.setForeground(UIAssets.getTextSecondary());

        JButton browseBtn = AdminUIHelper.buildOutlineButton("Browse...");
        browseBtn.setPreferredSize(new Dimension(110, 46));
        browseBtn.addActionListener(e -> browseImage());

        JPanel picker = new JPanel(new BorderLayout(10, 0));
        picker.setOpaque(false);
        picker.setAlignmentX(Component.LEFT_ALIGNMENT);
        picker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        picker.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(0, 12, 0, 8)));
        picker.add(imageFileLabel, BorderLayout.CENTER);
        picker.add(browseBtn,      BorderLayout.EAST);

        col.add(lbl);
        col.add(Box.createVerticalStrut(6));
        col.add(picker);
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

        JButton saveBtn = AdminUIHelper.buildSolidButton("Add Vehicle", UIAssets.CLR_BLUE);
        saveBtn.setPreferredSize(new Dimension(140, 44));
        saveBtn.addActionListener(e -> handleSave());

        row.add(cancelBtn);
        row.add(saveBtn);
        return row;
    }

    // =========================================================
    //  LAYOUT HELPERS
    // =========================================================
    private JPanel row3(JPanel c1, JPanel c2, JPanel c3) {
        JPanel row = new JPanel(new GridLayout(1, 3, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        row.add(c1); row.add(c2); row.add(c3);
        return row;
    }

    private JPanel row2(JPanel c1, JPanel c2) {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        row.add(c1); row.add(c2);
        return row;
    }

    private JPanel col(String label, JComponent input, JLabel err) {
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);

        JLabel lbl = AdminUIHelper.buildFieldLabel(label);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        col.add(lbl);
        col.add(Box.createVerticalStrut(6));
        col.add(input);
        if (err != null) { err.setAlignmentX(Component.LEFT_ALIGNMENT); col.add(err); }
        return col;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel errLbl() {
        JLabel l = new JLabel(" ");
        l.setFont(UIAssets.FONT_SMALL);
        l.setForeground(UIAssets.CLR_RED);
        return l;
    }

    // =========================================================
    //  IMAGE BROWSE
    // =========================================================
    private void browseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Vehicle Image");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "webp"));
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            imageFileLabel.setText(selectedImageFile.getName());
            imageFileLabel.setForeground(UIAssets.getTextPrimary());
        }
    }

    // =========================================================
    //  SAVE
    // =========================================================
    private void handleSave() {
        clearErrors();
        if (!validateForm()) return;

        String     brand        = brandField.getText().trim();
        String     model        = modelField.getText().trim();
        int        year         = Integer.parseInt(yearField.getText().trim());
        String     color        = getOptional(colorField, "e.g. Pearl White (optional)");
        String     plate        = plateField.getText().trim().toUpperCase();
        int        seats        = Integer.parseInt(seatsField.getText().trim());
        String     category     = (String) categoryBox.getSelectedItem();
        String     transmission = (String) transmissionBox.getSelectedItem();
        String     fuelType     = (String) fuelTypeBox.getSelectedItem();
        BigDecimal dailyRate    = new BigDecimal(rateField.getText().trim());

        Car car = new Car(brand, model, year, plate, category,
                          transmission, fuelType, seats, dailyRate);
        if (!color.isBlank()) car.setColor(color);

        int carId = carDAO.insertCar(car);
        if (carId == -1) {
            JOptionPane.showMessageDialog(this,
                "Failed to save vehicle. Check your database connection and try again.",
                "Save Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedImageFile != null) {
            try { AppConfig.saveVehiclePhoto(selectedImageFile, carId, category); }
            catch (Exception ex) {
                System.err.println("[AddVehiclePanel] Image copy failed: " + ex.getMessage());
            }
        }

        JOptionPane.showMessageDialog(this,
            "\"" + brand + " " + model + "\" has been added to the fleet.",
            "Vehicle Added", JOptionPane.INFORMATION_MESSAGE);

        if (onSuccess != null) onSuccess.run();
    }

    // =========================================================
    //  VALIDATION
    // =========================================================
    private boolean validateForm() {
        boolean ok = true;
        int maxYear = java.time.Year.now().getValue() + 1;

        if (isBlankOrPlaceholder(brandField, "e.g. Toyota"))
            { brandErr.setText("Brand is required."); ok = false; }
        if (isBlankOrPlaceholder(modelField, "e.g. Vios"))
            { modelErr.setText("Model is required."); ok = false; }

        try {
            int y = Integer.parseInt(yearField.getText().trim());
            if (y < 1980 || y > maxYear)
                { yearErr.setText("Year must be 1980–" + maxYear + "."); ok = false; }
        } catch (NumberFormatException e)
            { yearErr.setText("Year must be a number."); ok = false; }

        if (isBlankOrPlaceholder(plateField, "e.g. ABC 1234"))
            { plateErr.setText("Plate number is required."); ok = false; }
        else if (carDAO.plateExists(plateField.getText().trim().toUpperCase()))
            { plateErr.setText("Plate number already registered."); ok = false; }

        try {
            int s = Integer.parseInt(seatsField.getText().trim());
            if (s < 1 || s > 60)
                { seatsErr.setText("Must be between 1 and 60."); ok = false; }
        } catch (NumberFormatException e)
            { seatsErr.setText("Must be a whole number."); ok = false; }

        try {
            BigDecimal r = new BigDecimal(rateField.getText().trim());
            if (r.compareTo(BigDecimal.ZERO) <= 0)
                { rateErr.setText("Rate must be greater than 0."); ok = false; }
        } catch (NumberFormatException e)
            { rateErr.setText("Enter a valid amount (e.g. 1500.00)."); ok = false; }

        return ok;
    }

    private void clearErrors() {
        for (JLabel e : new JLabel[]{ brandErr, modelErr, yearErr, plateErr, seatsErr, rateErr })
            e.setText(" ");
    }

    private boolean isBlankOrPlaceholder(JTextField f, String placeholder) {
        String t = f.getText().trim();
        return t.isBlank() || t.equals(placeholder);
    }

    private String getOptional(JTextField f, String placeholder) {
        String t = f.getText().trim();
        return t.equals(placeholder) ? "" : t;
    }
}
