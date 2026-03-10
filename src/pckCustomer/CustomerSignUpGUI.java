package pckCustomer;

import pckMain.SignUpChoiceGUI;
import pckUtils.CustomTitleBar;
import pckServices.CustomerService;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

/**
 * CustomerSignUpGUI.java
 * Sign-up form for new Customer accounts.
 *
 * Fields:
 *   Profile photo (optional, placeholder shown)
 *   Full Name | Email | Phone
 *   Password  | Confirm Password
 */
public class CustomerSignUpGUI extends JFrame {

    private static final String PLACEHOLDER_PHOTO =
        "assets/defaults/default-avatar.png";

    private final JFrame parent;

    // Form fields
    private JTextField     nameField, emailField, phoneField;
    private JPasswordField passwordField, confirmField;
    private JLabel         statusLabel, photoLabel;
    private String         selectedPhotoPath = null;

    public CustomerSignUpGUI(JFrame parent) {
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
        add(new CustomTitleBar(this, "Car Rental System — Customer Sign Up", true), BorderLayout.NORTH);

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
            "<html>Create your customer account<br>and start renting today.</html>");
        tagline.setFont(UIAssets.FONT_BODY);
        tagline.setForeground(UIAssets.getTextSecondary());
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(40));

        // Blue accent card on left
        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(UIAssets.CLR_BLUE_LIGHT);
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_BLUE, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        infoCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        infoCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] perks = { "Browse available vehicles", "Track your rentals", "Manage payments" };
        for (String perk : perks) {
            JLabel pl = new JLabel("✓  " + perk);
            pl.setFont(UIAssets.FONT_BODY);
            pl.setForeground(UIAssets.CLR_BLUE);
            pl.setAlignmentX(Component.LEFT_ALIGNMENT);
            infoCard.add(pl);
            infoCard.add(Box.createVerticalStrut(4));
        }
        inner.add(infoCard);

        panel.add(inner);
        return panel;
    }

    // =========================================================
    //  RIGHT — Sign Up form
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
        JLabel title = new JLabel("Customer Sign Up");
        title.setFont(UIAssets.FONT_DISPLAY);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Fill in your details to create an account.");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sub);
        form.add(Box.createVerticalStrut(20));

        // Profile photo
        form.add(buildPhotoSection());
        form.add(Box.createVerticalStrut(14));

        // Full Name
        form.add(buildFieldLabel("Full Name"));
        form.add(Box.createVerticalStrut(5));
        nameField = buildTextField("e.g. Juan dela Cruz");
        form.add(nameField);
        form.add(Box.createVerticalStrut(14));

        // Email + Phone (side by side via wrapper)
        JPanel twoCol = buildTwoColumnRow(
            "Email Address", "emailField",
            "Phone Number",  "phoneField"
        );
        form.add(twoCol);
        form.add(Box.createVerticalStrut(14));

        // Password + Confirm Password
        form.add(buildFieldLabel("Password"));
        form.add(Box.createVerticalStrut(5));
        passwordField = buildPasswordField("Create a password");
        form.add(passwordField);
        form.add(Box.createVerticalStrut(14));

        form.add(buildFieldLabel("Confirm Password"));
        form.add(Box.createVerticalStrut(5));
        confirmField = buildPasswordField("Re-enter your password");
        form.add(confirmField);
        form.add(Box.createVerticalStrut(8));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIAssets.FONT_SMALL);
        statusLabel.setForeground(UIAssets.CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));

        // Submit
        JButton submitBtn = buildSolidButton("Create Customer Account",
            UIAssets.CLR_BLUE, UIAssets.CLR_BLUE_HOVER);
        submitBtn.addActionListener(e -> handleSignUp());
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(submitBtn);
        form.add(Box.createVerticalStrut(14));

        // Footer
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);
        form.add(Box.createVerticalStrut(12));

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
    //  Photo section — circular preview + upload button
    // =========================================================
    private JPanel buildPhotoSection() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        row.setBackground(UIAssets.getSurface());
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        // Circular photo preview
        photoLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Clip to circle
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, 60, 60));
                if (getIcon() != null) {
                    getIcon().paintIcon(this, g2, 0, 0);
                } else {
                    g2.setColor(UIAssets.getBorder());
                    g2.fillOval(0, 0, 60, 60);
                }
                g2.dispose();
                // Circle border
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

        // Text + button stacked
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
        chooseBtn.setForeground(UIAssets.CLR_BLUE);
        chooseBtn.setBackground(UIAssets.CLR_BLUE_LIGHT);
        chooseBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_BLUE_LIGHT, 0), new EmptyBorder(4, 10, 4, 10)));
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
            ImageIcon raw    = new ImageIcon(path);
            Image    scaled  = raw.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.err.println("[CustomerSignUpGUI] Photo preview failed: " + e.getMessage());
            photoLabel.setIcon(null);
        }
        photoLabel.repaint();
    }

    private void pickPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Image files (JPG, PNG)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            selectedPhotoPath = file.getAbsolutePath();
            loadPhoto(selectedPhotoPath);
        }
    }

    /** Two-column row builder — email left, phone right */
    private JPanel buildTwoColumnRow(String l1, String ref1, String l2, String ref2) {
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
    //  Component builders
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
                    new LineBorder(UIAssets.CLR_BLUE, 1, true), new EmptyBorder(6, 12, 6, 12)));
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
                    new LineBorder(UIAssets.CLR_BLUE, 1, true), new EmptyBorder(6, 12, 6, 12)));
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
        btn.setForeground(UIAssets.CLR_BLUE);
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
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());
        String confirm  = String.valueOf(confirmField.getPassword());

        // Delegate all validation + DB insert to CustomerService
        CustomerService.RegisterResult result = CustomerService.register(
            name, email, phone, password, confirm, selectedPhotoPath);

        if (result == CustomerService.RegisterResult.SUCCESS) {
            setStatus(CustomerService.getMessage(result), UIAssets.CLR_GREEN);
            Timer t = new Timer(1400, ev -> { parent.setVisible(true); this.dispose(); });
            t.setRepeats(false);
            t.start();
        } else {
            setStatus(CustomerService.getMessage(result), UIAssets.CLR_RED);
        }
    }

    private boolean isPlaceholder(String value, String placeholder) {
        return value.isEmpty() || value.equals(placeholder);
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }
}
