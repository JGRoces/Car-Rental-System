package pckDriver;

import pckMain.SignUpChoiceGUI;
import pckUtils.CustomTitleBar;
import pckServices.DriverService;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

/**
 * DriverSignUpGUI.java
 * Sign-up form for new Driver accounts.
 *
 * Fields:
 *   Profile photo (optional, placeholder shown)
 *   Full Name | Email | Phone
 *   Password  | Confirm Password
 *   Driver's License Number | License Expiry
 *   Vehicle Type Preference
 */
public class DriverSignUpGUI extends JFrame {

    private static final String PLACEHOLDER_PHOTO =
        "assets/test-images/profile photos/mockpfp.png";

    private static final String[] VEHICLE_TYPES = {
        "Select vehicle type…", "Sedan", "SUV", "Van / Minibus",
        "Pickup Truck", "Motorcycle", "Any"
    };

    private final JFrame parent;

    // Form fields
    private JTextField     nameField, emailField, phoneField;
    private JTextField     licenseField, licenseExpiryField;
    private JPasswordField passwordField, confirmField;
    private JComboBox<String> vehicleTypeBox;
    private JLabel         statusLabel, photoLabel;
    private String         selectedPhotoPath = null;

    public DriverSignUpGUI(JFrame parent) {
        this.parent = parent;
        initWindow();
        initComponents();
    }

    private void initWindow() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1400, 880);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        try { setShape(new RoundRectangle2D.Double(0, 0, 1400, 880, 12, 12)); }
        catch (UnsupportedOperationException ignored) {}
    }

    private void initComponents() {
        add(new CustomTitleBar(this, "Car Rental System — Driver Sign Up", true), BorderLayout.NORTH);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIAssets.getBg());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.6; gbc.gridx = 0;
        root.add(buildLeftPanel(), gbc);

        gbc.weightx = 0.4; gbc.gridx = 1;
        root.add(buildRightPanel(), gbc);

        add(root, BorderLayout.CENTER);
    }

    // =========================================================
    //  LEFT — Branding
    // =========================================================
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getBg());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIAssets.getBg());
        inner.setBorder(new EmptyBorder(0, 52, 0, 52));

        JPanel accentBar = new JPanel(new GridLayout(1, 4, 3, 0));
        accentBar.setBackground(UIAssets.getBg());
        accentBar.setMaximumSize(new Dimension(80, 5));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Color c : new Color[]{ UIAssets.CLR_BLUE, UIAssets.CLR_GREEN,
                                    UIAssets.CLR_YELLOW, UIAssets.CLR_RED }) {
            JPanel seg = new JPanel(); seg.setBackground(c); accentBar.add(seg);
        }
        inner.add(accentBar);
        inner.add(Box.createVerticalStrut(24));

        JLabel brand = new JLabel("CarRentals");
        brand.setFont(UIAssets.FONT_TITLE);
        brand.setForeground(UIAssets.getTextPrimary());
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(brand);
        inner.add(Box.createVerticalStrut(10));

        JLabel tagline = new JLabel(
            "<html>Join our driver network and<br>earn on your schedule.</html>");
        tagline.setFont(UIAssets.FONT_BODY);
        tagline.setForeground(UIAssets.getTextSecondary());
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(40));

        // Green accent info card
        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(UIAssets.CLR_GREEN_LIGHT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_GREEN, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] perks = {
            "Set your own availability",
            "Manage assigned vehicles",
            "View rental history"
        };
        for (String perk : perks) {
            JLabel pl = new JLabel("✓  " + perk);
            pl.setFont(UIAssets.FONT_BODY);
            pl.setForeground(UIAssets.CLR_GREEN);
            pl.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoCard.add(pl);
            infoCard.add(Box.createVerticalStrut(4));
        }
        inner.add(infoCard);

        inner.add(Box.createVerticalStrut(24));

        // Requirements note
        JLabel reqNote = new JLabel(
            "<html>You will need: a valid driver's license<br>"
            + "and an active government-issued ID.</font></html>");
        reqNote.setFont(UIAssets.FONT_SMALL);
        reqNote.setForeground(UIAssets.getTextSecondary());
        reqNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(reqNote);

        panel.add(inner);
        return panel;
    }

    // =========================================================
    //  RIGHT — Sign Up form (scrollable)
    // =========================================================
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getSurface());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UIAssets.getSurface());
        form.setBorder(new EmptyBorder(12, 44, 12, 44));

        // Form fills the right panel directly — no scroll needed at 1400x880

        // Back
        JButton back = buildTextButton("< Back");
        back.addActionListener(e -> { parent.setVisible(true); this.dispose(); });
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(back);
        form.add(Box.createVerticalStrut(10));

        // Header
        JLabel title = new JLabel("Driver Sign Up");
        title.setFont(UIAssets.FONT_DISPLAY);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Register as a driver in our network.");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(Box.createVerticalStrut(10));

        // ── Section: Personal Info ────────────────────────────
        form.add(buildSectionDivider("Personal Information"));
        form.add(Box.createVerticalStrut(8));

        // Profile photo
        form.add(buildPhotoSection());
        form.add(Box.createVerticalStrut(10));

        // Full Name
        form.add(buildFieldLabel("Full Name"));
        form.add(Box.createVerticalStrut(5));
        nameField = buildTextField("e.g. Juan dela Cruz");
        form.add(nameField);
        form.add(Box.createVerticalStrut(10));

        // Email + Phone
        form.add(buildTwoColumnRow());
        form.add(Box.createVerticalStrut(10));

        // Password + Confirm
        form.add(buildFieldLabel("Password"));
        form.add(Box.createVerticalStrut(5));
        passwordField = buildPasswordField("Create a password (min. 8 characters)");
        form.add(passwordField);
        form.add(Box.createVerticalStrut(12));

        form.add(buildFieldLabel("Confirm Password"));
        form.add(Box.createVerticalStrut(5));
        confirmField = buildPasswordField("Re-enter your password");
        form.add(confirmField);
        form.add(Box.createVerticalStrut(10));

        // ── Section: Driver Details ───────────────────────────
        form.add(buildSectionDivider("Driver Information"));
        form.add(Box.createVerticalStrut(8));

        // License No + Expiry (two columns)
        JPanel licRow = new JPanel(new GridLayout(1, 2, 12, 0));
        licRow.setBackground(UIAssets.getSurface());
        licRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        licRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel licLeft = new JPanel();
        licLeft.setLayout(new BoxLayout(licLeft, BoxLayout.Y_AXIS));
        licLeft.setBackground(UIAssets.getSurface());
        licLeft.add(buildFieldLabel("Driver's License No."));
        licLeft.add(Box.createVerticalStrut(5));
        licenseField = buildTextField("e.g. N01-23-456789");
        licLeft.add(licenseField);

        JPanel licRight = new JPanel();
        licRight.setLayout(new BoxLayout(licRight, BoxLayout.Y_AXIS));
        licRight.setBackground(UIAssets.getSurface());
        licRight.add(buildFieldLabel("License Expiry Date"));
        licRight.add(Box.createVerticalStrut(5));
        licenseExpiryField = buildTextField("MM/DD/YYYY");
        licRight.add(licenseExpiryField);

        licRow.add(licLeft);
        licRow.add(licRight);
        form.add(licRow);
        form.add(Box.createVerticalStrut(12));

        // Vehicle Type preference
        form.add(buildFieldLabel("Vehicle Type Preference"));
        form.add(Box.createVerticalStrut(5));
        vehicleTypeBox = new JComboBox<>(VEHICLE_TYPES);
        vehicleTypeBox.setFont(UIAssets.FONT_BODY);
        vehicleTypeBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        vehicleTypeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        vehicleTypeBox.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        form.add(vehicleTypeBox);
        form.add(Box.createVerticalStrut(8));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIAssets.FONT_SMALL);
        statusLabel.setForeground(UIAssets.CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));

        // Submit — green for driver
        JButton submitBtn = buildSolidButton("Create Driver Account",
            UIAssets.CLR_GREEN, UIAssets.CLR_GREEN.darker());
        submitBtn.addActionListener(e -> handleSignUp());
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(submitBtn);
        form.add(Box.createVerticalStrut(12));

        // Footer
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);
        form.add(Box.createVerticalStrut(10));
        JLabel footer = new JLabel("Car Rental System  •  v1.0");
        footer.setFont(UIAssets.FONT_SMALL);
        footer.setForeground(UIAssets.getTextPlaceholder());
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(footer);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(form, gbc);
        return panel;
    }

    // =========================================================
    //  Section divider label
    // =========================================================
    private JPanel buildSectionDivider(String text) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(UIAssets.getSurface());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setForeground(UIAssets.getTextSecondary());
        lbl.setPreferredSize(new Dimension(160, 20));

        JSeparator sep = new JSeparator();
        sep.setForeground(UIAssets.getBorder());

        row.add(lbl, BorderLayout.WEST);
        row.add(sep, BorderLayout.CENTER);
        return row;
    }

    // =========================================================
    //  Photo section
    // =========================================================
    private JPanel buildPhotoSection() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setBackground(UIAssets.getSurface());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        photoLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 60, 60));
                if (getIcon() != null) getIcon().paintIcon(this, g2, 0, 0);
                else { g2.setColor(UIAssets.getBorder()); g2.fillOval(0, 0, 60, 60); }
                g2.dispose();
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(UIAssets.getBorder());
                g3.setStroke(new BasicStroke(1.5f));
                g3.drawOval(1, 1, 57, 57);
                g3.dispose();
            }
        };
        photoLabel.setPreferredSize(new Dimension(60, 60));
        photoLabel.setOpaque(false);
        loadPhoto(PLACEHOLDER_PHOTO);

        JPanel textSide = new JPanel();
        textSide.setLayout(new BoxLayout(textSide, BoxLayout.Y_AXIS));
        textSide.setBackground(UIAssets.getSurface());

        JLabel photTitle = new JLabel("Profile Photo");
        photTitle.setFont(UIAssets.FONT_H3);
        photTitle.setForeground(UIAssets.getTextPrimary());
        photTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel photSub = new JLabel("Optional — JPG or PNG");
        photSub.setFont(UIAssets.FONT_SMALL);
        photSub.setForeground(UIAssets.getTextSecondary());
        photSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton chooseBtn = new JButton("Upload Photo");
        chooseBtn.setFont(UIAssets.FONT_SMALL);
        chooseBtn.setForeground(UIAssets.CLR_GREEN);
        chooseBtn.setBackground(UIAssets.CLR_GREEN_LIGHT);
        chooseBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_GREEN_LIGHT, 0), new EmptyBorder(4, 10, 4, 10)));
        chooseBtn.setFocusPainted(false);
        chooseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chooseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        chooseBtn.addActionListener(e -> pickPhoto());

        textSide.add(photTitle);
        textSide.add(Box.createVerticalStrut(2));
        textSide.add(photSub);
        textSide.add(Box.createVerticalStrut(6));
        textSide.add(chooseBtn);

        row.add(photoLabel);
        row.add(textSide);
        return row;
    }

    private void loadPhoto(String path) {
        try {
            ImageIcon raw   = new ImageIcon(path);
            Image    scaled = raw.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception ignored) { photoLabel.setIcon(null); }
        photoLabel.repaint();
    }

    private void pickPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files (JPG, PNG)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedPhotoPath = chooser.getSelectedFile().getAbsolutePath();
            loadPhoto(selectedPhotoPath);
        }
    }

    private JPanel buildTwoColumnRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setBackground(UIAssets.getSurface());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UIAssets.getSurface());
        left.add(buildFieldLabel("Email Address"));
        left.add(Box.createVerticalStrut(5));
        emailField = buildTextField("you@email.com");
        left.add(emailField);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(UIAssets.getSurface());
        right.add(buildFieldLabel("Phone Number"));
        right.add(Box.createVerticalStrut(5));
        phoneField = buildTextField("+63 9XX XXX XXXX");
        right.add(phoneField);

        row.add(left);
        row.add(right);
        return row;
    }

    // =========================================================
    //  Component builders (same pattern as CustomerSignUpGUI)
    // =========================================================
    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setForeground(UIAssets.getTextPrimary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setForeground(UIAssets.getTextSecondary());
        field.setBackground(UIAssets.getSurface());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(6, 12, 6, 12)));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText(""); field.setForeground(UIAssets.getTextPrimary());
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIAssets.CLR_GREEN, 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(UIAssets.getTextSecondary()); field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
        });
        return field;
    }

    private JPasswordField buildPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setForeground(UIAssets.getTextSecondary());
        field.setBackground(UIAssets.getSurface());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(6, 12, 6, 12)));
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText(""); field.setForeground(UIAssets.getTextPrimary()); field.setEchoChar('•');
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIAssets.CLR_GREEN, 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0); field.setForeground(UIAssets.getTextSecondary());
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(6, 12, 6, 12)));
            }
        });
        return field;
    }

    private JButton buildSolidButton(String label, Color bg, Color hover) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIAssets.FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);    }
        });
        return btn;
    }

    private JButton buildTextButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(UIAssets.FONT_SMALL);
        btn.setForeground(UIAssets.CLR_GREEN);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        return btn;
    }

    // =========================================================
    //  Validation + Submit
    // =========================================================
    private void handleSignUp() {
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        String phone   = phoneField.getText().trim();
        String license = licenseField.getText().trim();
        String expiry  = licenseExpiryField.getText().trim();
        String pw      = String.valueOf(passwordField.getPassword());
        String confirm = String.valueOf(confirmField.getPassword());

        // Vehicle type check stays here — it's a UI concern, not service concern
        if (vehicleTypeBox.getSelectedIndex() == 0) {
            setStatus("Please select a vehicle type preference.", UIAssets.CLR_RED); return;
        }

        String vehicleType = (String) vehicleTypeBox.getSelectedItem();

        // Delegate all validation + DB insert to DriverService
        DriverService.RegisterResult result = DriverService.register(
            name, email, phone, pw, confirm,
            license, expiry, vehicleType, selectedPhotoPath);

        if (result == DriverService.RegisterResult.SUCCESS) {
            setStatus(DriverService.getMessage(result), UIAssets.CLR_GREEN);
            Timer t = new Timer(1800, ev -> { parent.setVisible(true); this.dispose(); });
            t.setRepeats(false);
            t.start();
        } else {
            setStatus(DriverService.getMessage(result), UIAssets.CLR_RED);
        }
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
