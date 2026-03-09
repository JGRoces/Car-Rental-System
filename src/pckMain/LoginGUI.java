package pckMain;

import javax.swing.*;
import javax.swing.border.*;

import pckAdmin.AdminDashboardGUI;
import pckCustomer.CustomerDashboardGUI;
import pckModels.User;
import pckServices.AuthService;
import pckUtils.CustomTitleBar;
import pckUtils.SessionManager;
import pckUtils.UIAssets;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginGUI.java
 * Entry screen — Sign In or Create Account.
 *
 * Right panel uses CardLayout:
 *   LANDING → two call-to-action buttons
 *   SIGNIN  → email + password form
 *
 * "Create Account" opens SignUpChoiceGUI (Customer or Driver).
 */
public class LoginGUI extends JFrame {


    // Right panel CardLayout
    private static final String CARD_LANDING = "LANDING";
    private static final String CARD_SIGNIN  = "SIGNIN";

    // Components
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private JCheckBox      showPasswordBox;
    private JPanel         rightPanel;
    private CardLayout     rightCard;

    public LoginGUI() {
        initWindow();
        initComponents();
        UIAssets.addListener(this::applyTheme);
    }

    private void initWindow() {
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 880);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        try { setShape(new RoundRectangle2D.Double(0, 0, 1400, 880, 12, 12)); }
        catch (UnsupportedOperationException ignored) {}
    }

    private void initComponents() {
        add(new CustomTitleBar(this, "Car Rental System", true), BorderLayout.NORTH);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(UIAssets.getBg());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.6; gbc.gridx = 0;
        root.add(buildLeftPanel(), gbc);

        gbc.weightx = 0.4; gbc.gridx = 1;
        root.add(buildRightPanel(), gbc);

        add(root, BorderLayout.CENTER);
    }

    private void applyTheme() { repaint(); }

    // =========================================================
    //  LEFT — Branding panel (always dark)
    // =========================================================
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getBg());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIAssets.getBg());
        inner.setBorder(new EmptyBorder(0, 52, 0, 52));

        // 4-color accent bar
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
            "<html>Your trusted platform for<br>seamless car rental management.</html>");
        tagline.setFont(UIAssets.FONT_BODY);
        tagline.setForeground(UIAssets.getTextSecondary());
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(40));

        // Car image
        ImageIcon raw = new ImageIcon("assets/images/bydshowcase.jpg");
        Image scaled  = raw.getImage().getScaledInstance(520, 380, Image.SCALE_SMOOTH);
        JLabel img    = new JLabel(new ImageIcon(scaled));
        img.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(img);

        panel.add(inner);
        return panel;
    }

    // =========================================================
    //  RIGHT — CardLayout: Landing → SignIn
    // =========================================================
    private JPanel buildRightPanel() {
        rightCard  = new CardLayout();
        rightPanel = new JPanel(rightCard);
        rightPanel.setBackground(UIAssets.getSurface());

        rightPanel.add(buildLandingCard(), CARD_LANDING);
        rightPanel.add(buildSignInCard(),  CARD_SIGNIN);

        rightCard.show(rightPanel, CARD_LANDING);
        return rightPanel;
    }

    // ── LANDING card ──────────────────────────────────────────
    private JPanel buildLandingCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getSurface());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIAssets.getSurface());
        inner.setBorder(new EmptyBorder(0, 52, 0, 52));

        // Logo mark — 4 accent dots stacked
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        logoRow.setBackground(UIAssets.getSurface());
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Color c : new Color[]{ UIAssets.CLR_BLUE, UIAssets.CLR_GREEN,
                                    UIAssets.CLR_YELLOW, UIAssets.CLR_RED }) {
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c); g2.fillOval(0, 0, 12, 12); g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(12, 12));
            dot.setOpaque(false);
            logoRow.add(dot);
        }
        inner.add(logoRow);
        inner.add(Box.createVerticalStrut(28));

        // Heading
        JLabel heading = new JLabel("Get Started");
        heading.setFont(UIAssets.FONT_DISPLAY);
        heading.setForeground(UIAssets.getTextPrimary());
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(heading);
        inner.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("Sign in to your account or create a new one.");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(sub);
        inner.add(Box.createVerticalStrut(40));

        // Sign In button (solid blue)
        JButton signInBtn = buildSolidButton("Sign In", UIAssets.CLR_BLUE, UIAssets.CLR_BLUE_HOVER);
        signInBtn.addActionListener(e -> rightCard.show(rightPanel, CARD_SIGNIN));
        signInBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(signInBtn);
        inner.add(Box.createVerticalStrut(12));

        // Sign Up button (outlined)
        JButton createBtn = buildOutlineButton("Sign Up");
        createBtn.addActionListener(e -> openSignUpChoice());
        createBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(createBtn);
        inner.add(Box.createVerticalStrut(48));

        // Divider
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(sep);
        inner.add(Box.createVerticalStrut(20));

        JLabel footer = new JLabel("Car Rental System  •  v1.0");
        footer.setFont(UIAssets.FONT_SMALL);
        footer.setForeground(UIAssets.getTextPlaceholder());
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(footer);

        panel.add(inner);
        return panel;
    }

    // ── SIGN IN card ──────────────────────────────────────────
    private JPanel buildSignInCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getSurface());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(UIAssets.getSurface());
        form.setBorder(new EmptyBorder(0, 52, 0, 52));
        form.setPreferredSize(new Dimension(380, 600));

        // Back link
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(UIAssets.FONT_SMALL);
        backBtn.setForeground(UIAssets.CLR_BLUE);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        backBtn.addActionListener(e -> {
            statusLabel.setText(" ");
            rightCard.show(rightPanel, CARD_LANDING);
        });
        form.add(backBtn);
        form.add(Box.createVerticalStrut(16));

        // Title
        JLabel title = new JLabel("Welcome Back");
        title.setFont(UIAssets.FONT_DISPLAY);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setFont(UIAssets.FONT_SUBTITLE);
        subtitle.setForeground(UIAssets.getTextSecondary());
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);
        form.add(Box.createVerticalStrut(32));

        // Email
        form.add(buildFieldLabel("Email Address"));
        form.add(Box.createVerticalStrut(6));
        emailField = buildTextField("Enter your email");
        form.add(emailField);
        form.add(Box.createVerticalStrut(18));

        // Password
        form.add(buildFieldLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        passwordField = buildPasswordField("Enter your password");
        form.add(passwordField);
        form.add(Box.createVerticalStrut(8));

        // Show password
        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(UIAssets.FONT_SMALL);
        showPasswordBox.setForeground(UIAssets.getTextSecondary());
        showPasswordBox.setBackground(UIAssets.getSurface());
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordBox.addActionListener(e -> togglePasswordVisibility());
        form.add(showPasswordBox);
        form.add(Box.createVerticalStrut(20));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIAssets.FONT_SMALL);
        statusLabel.setForeground(UIAssets.CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(6));

        // Sign In button
        loginButton = buildSolidButton("Sign In", UIAssets.CLR_BLUE, UIAssets.CLR_BLUE_HOVER);
        loginButton.addActionListener(e -> handleLogin());
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(loginButton);
        form.add(Box.createVerticalStrut(20));

        // No account? Create one
        JPanel noAccountRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        noAccountRow.setBackground(UIAssets.getSurface());
        noAccountRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel noAccLbl = new JLabel("Don't have an account? ");
        noAccLbl.setFont(UIAssets.FONT_SMALL);
        noAccLbl.setForeground(UIAssets.getTextSecondary());
        JButton noAccBtn = new JButton("Create one");
        noAccBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        noAccBtn.setForeground(UIAssets.CLR_BLUE);
        noAccBtn.setBorderPainted(false);
        noAccBtn.setContentAreaFilled(false);
        noAccBtn.setFocusPainted(false);
        noAccBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noAccBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        noAccBtn.addActionListener(e -> openSignUpChoice());
        noAccountRow.add(noAccLbl);
        noAccountRow.add(noAccBtn);
        form.add(noAccountRow);

        form.add(Box.createVerticalStrut(28));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);
        form.add(Box.createVerticalStrut(16));
        JLabel footer = new JLabel("Car Rental System  •  v1.0");
        footer.setFont(UIAssets.FONT_SMALL);
        footer.setForeground(UIAssets.getTextPlaceholder());
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(footer);

        panel.add(form);
        return panel;
    }

    // =========================================================
    //  Component Builders
    // =========================================================
    private JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setForeground(UIAssets.getTextPrimary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Solid filled button — used for primary actions */
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

    /** Outlined button — used for secondary actions */
    private JButton buildOutlineButton(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(UIAssets.CLR_BLUE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 7, 7));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIAssets.FONT_BUTTON);
        btn.setForeground(UIAssets.CLR_BLUE);
        btn.setBackground(UIAssets.getSurface());
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Color hoverBg = UIAssets.CLR_BLUE_LIGHT;
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg);  }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(UIAssets.getSurface()); }
        });
        return btn;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setForeground(UIAssets.getTextSecondary());
        field.setBackground(UIAssets.getSurface());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
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
        field.addActionListener(e -> handleLogin());
        return field;
    }

    // =========================================================
    //  Actions
    // =========================================================
    private void openSignUpChoice() {
        new SignUpChoiceGUI(this).setVisible(true);
        this.setVisible(false);
    }

    private void togglePasswordVisibility() {
        String current = String.valueOf(passwordField.getPassword());
        if (!current.equals("Enter your password"))
            passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '•');
    }

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        if (email.isEmpty() || email.equals("Enter your email") ||
            password.isEmpty() || password.equals("Enter your password")) {
            setStatus("Please fill in all fields.", UIAssets.CLR_RED); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            setStatus("Please enter a valid email address.", UIAssets.CLR_RED); return;
        }

        loginButton.setEnabled(false);
        setStatus("Signing in...", UIAssets.getTextSecondary());

        User user = AuthService.login(email, password);
        if (user != null) {
            if (user.isAdmin()) new AdminDashboardGUI().setVisible(true);
            else                new CustomerDashboardGUI().setVisible(true);
            this.dispose();
        } else {
            setStatus("Invalid email or password.", UIAssets.CLR_RED);
            loginButton.setEnabled(true);
        }
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
