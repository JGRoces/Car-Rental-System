package pckTests;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginGUI_Test.java
 * Mock LoginGUI with dark/light mode toggle via ThemeManager.
 *
 * The sun/moon toggle button in the top-right of the right panel
 * calls ThemeManager.toggleTheme() — all registered components
 * (including the CustomTitleBar) update automatically.
 */
public class LoginGUI_Test extends JFrame {

    // Fonts (same in both themes)
    private static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  28);
    private static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_INPUT     = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_FOOTER    = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_IMG_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_IMG_SUB   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_TOGGLE    = new Font("Segoe UI", Font.BOLD,  13);

    // -------------------------
    // All panels and components
    // that need repainting on theme change
    // -------------------------
    private JPanel     rootPanel, leftPanel, rightPanel;
    private JPanel     innerLeft, imgPlaceholder, accentBar;
    private JPanel     formPanel;
    private JLabel     brandLabel, taglineLabel;
    private JLabel     titleLabel, subtitleLabel, footerLabel;
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JLabel     emailLbl, passwordLbl;
    private JLabel     statusLabel;
    private JCheckBox  showPasswordBox;
    private JButton    loginButton, themeToggleBtn;
    private JSeparator separator;

    // -------------------------
    // Entry Point
    // -------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI_Test frame = new LoginGUI_Test();
            frame.setVisible(true);
        });
    }

    // -------------------------
    // Constructor
    // -------------------------
    public LoginGUI_Test() {
        initWindow();
        initComponents();
        applyTheme();  // Paint initial theme after all components exist

        // Register — repaints everything when theme toggles
        ThemeManager.addListener(this::applyTheme);
    }

    // -------------------------
    // Window Setup
    // -------------------------
    private void initWindow() {
        setUndecorated(true);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        try {
            setShape(new RoundRectangle2D.Double(0, 0, 1200, 800, 12, 12));
        } catch (UnsupportedOperationException ignored) {}
    }

    // -------------------------
    // Build Layout
    // -------------------------
    private void initComponents() {
        add(new CustomTitleBar(this, "Car Rental System — Login"), BorderLayout.NORTH);

        rootPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.6; gbc.gridx = 0;
        rootPanel.add(buildLeftPanel(), gbc);

        gbc.weightx = 0.4; gbc.gridx = 1;
        rootPanel.add(buildRightPanel(), gbc);

        add(rootPanel, BorderLayout.CENTER);
    }

    // -------------------------
    // LEFT Panel
    // -------------------------
    private JPanel buildLeftPanel() {
        leftPanel = new JPanel(new GridBagLayout());

        innerLeft = new JPanel();
        innerLeft.setLayout(new BoxLayout(innerLeft, BoxLayout.Y_AXIS));
        innerLeft.setOpaque(false);
        innerLeft.setBorder(new EmptyBorder(0, 52, 0, 52));

        // Accent bar
        accentBar = new JPanel(new GridLayout(1, 4, 3, 0));
        accentBar.setOpaque(false);
        accentBar.setMaximumSize(new Dimension(80, 5));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Color c : new Color[]{
                ThemeManager.CLR_BLUE, ThemeManager.CLR_GREEN,
                ThemeManager.CLR_YELLOW, ThemeManager.CLR_RED }) {
            JPanel seg = new JPanel();
            seg.setBackground(c);
            accentBar.add(seg);
        }
        innerLeft.add(accentBar);
        innerLeft.add(Box.createVerticalStrut(24));

        brandLabel = new JLabel("CarRentals");
        brandLabel.setFont(FONT_IMG_TITLE);
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerLeft.add(brandLabel);
        innerLeft.add(Box.createVerticalStrut(10));

        taglineLabel = new JLabel(
            "<html>Your trusted platform for<br>seamless car rental management.</html>");
        taglineLabel.setFont(FONT_IMG_SUB);
        taglineLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerLeft.add(taglineLabel);
        innerLeft.add(Box.createVerticalStrut(40));

        // Image placeholder
        imgPlaceholder = new JPanel(new GridBagLayout());
        imgPlaceholder.setBorder(BorderFactory.createLineBorder(new Color(45, 45, 45), 1));
        imgPlaceholder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        imgPlaceholder.setPreferredSize(new Dimension(560, 400));
        imgPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel phText = new JPanel();
        phText.setLayout(new BoxLayout(phText, BoxLayout.Y_AXIS));
        phText.setOpaque(false);

        JLabel iconLbl = new JLabel("[ IMAGE ]");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        iconLbl.setForeground(new Color(75, 75, 75));
        iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        phText.add(iconLbl);
        phText.add(Box.createVerticalStrut(8));

        JLabel hintLbl = new JLabel("Car image goes here");
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLbl.setForeground(new Color(60, 60, 60));
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        phText.add(hintLbl);

        imgPlaceholder.add(phText);
        innerLeft.add(imgPlaceholder);
        leftPanel.add(innerLeft);

        return leftPanel;
    }

    // -------------------------
    // RIGHT Panel
    // -------------------------
    private JPanel buildRightPanel() {
        rightPanel = new JPanel(new GridBagLayout());

        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(0, 52, 0, 52));
        formPanel.setPreferredSize(new Dimension(380, 600));

        // Theme toggle button — top right of form
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        themeToggleBtn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Pill background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                // Icon: sun (light mode) or moon (dark mode)
                g2.setColor(getForeground());
                int cx = getWidth() / 2, cy = getHeight() / 2, r = 6;
                if (ThemeManager.isDark()) {
                    // Moon shape — circle with a smaller circle cut out
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                    g2.setColor(getBackground());
                    g2.fillOval(cx - r + 4, cy - r - 2, r * 2 - 2, r * 2 - 2);
                } else {
                    // Sun — circle + rays
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(cx - r + 2, cy - r + 2, (r - 2) * 2, (r - 2) * 2);
                    for (int i = 0; i < 8; i++) {
                        double angle = Math.toRadians(i * 45);
                        int x1 = (int)(cx + Math.cos(angle) * (r + 1));
                        int y1 = (int)(cy + Math.sin(angle) * (r + 1));
                        int x2 = (int)(cx + Math.cos(angle) * (r + 4));
                        int y2 = (int)(cy + Math.sin(angle) * (r + 4));
                        g2.drawLine(x1, y1, x2, y2);
                    }
                }
                g2.dispose();
            }
        };
        themeToggleBtn.setPreferredSize(new Dimension(36, 36));
        themeToggleBtn.setBorderPainted(false);
        themeToggleBtn.setContentAreaFilled(false);
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleBtn.addActionListener(e -> ThemeManager.toggleTheme());

        topRow.add(themeToggleBtn, BorderLayout.EAST);
        formPanel.add(topRow);
        formPanel.add(Box.createVerticalStrut(8));

        titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(titleLabel);
        formPanel.add(Box.createVerticalStrut(6));

        subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(subtitleLabel);
        formPanel.add(Box.createVerticalStrut(36));

        emailLbl = buildLabel("Email Address");
        formPanel.add(emailLbl);
        formPanel.add(Box.createVerticalStrut(6));
        emailField = buildTextField("Enter your email");
        formPanel.add(emailField);
        formPanel.add(Box.createVerticalStrut(20));

        passwordLbl = buildLabel("Password");
        formPanel.add(passwordLbl);
        formPanel.add(Box.createVerticalStrut(6));
        passwordField = buildPasswordField("Enter your password");
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(10));

        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(FONT_FOOTER);
        showPasswordBox.setOpaque(false);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordBox.addActionListener(e -> {
            String pw = String.valueOf(passwordField.getPassword());
            if (!pw.equals("Enter your password"))
                passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '•');
        });
        formPanel.add(showPasswordBox);
        formPanel.add(Box.createVerticalStrut(24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_FOOTER);
        statusLabel.setForeground(ThemeManager.CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(statusLabel);
        formPanel.add(Box.createVerticalStrut(8));

        loginButton = buildLoginButton();
        formPanel.add(loginButton);
        formPanel.add(Box.createVerticalStrut(28));

        separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(separator);
        formPanel.add(Box.createVerticalStrut(20));

        footerLabel = new JLabel("Car Rental System  •  v1.0  •  TEST BUILD");
        footerLabel.setFont(FONT_FOOTER);
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(footerLabel);

        rightPanel.add(formPanel);
        return rightPanel;
    }

    // ====================================================
    //  THEME APPLICATION
    //  Called on init and every time ThemeManager fires.
    //  Update every component's colors here.
    // ====================================================
    private void applyTheme() {
        boolean dark = ThemeManager.isDark();

        // Root + left panel — always dark (image panel)
        if (rootPanel   != null) rootPanel.setBackground(new Color(20, 20, 20));
        if (leftPanel   != null) leftPanel.setBackground(new Color(20, 20, 20));
        if (imgPlaceholder != null) imgPlaceholder.setBackground(new Color(32, 32, 32));

        // Brand text — always white on dark left panel
        if (brandLabel  != null) brandLabel.setForeground(Color.WHITE);
        if (taglineLabel!= null) taglineLabel.setForeground(new Color(60, 60, 60));

        // Right panel — switches with theme
        Color panelBg = dark ? new Color(28, 28, 28) : Color.WHITE;
        if (rightPanel  != null) rightPanel.setBackground(panelBg);

        // Titles
        if (titleLabel    != null) titleLabel.setForeground(ThemeManager.getTextPrimary());
        if (subtitleLabel != null) subtitleLabel.setForeground(ThemeManager.getTextSecondary());
        if (emailLbl      != null) emailLbl.setForeground(ThemeManager.getTextPrimary());
        if (passwordLbl   != null) passwordLbl.setForeground(ThemeManager.getTextPrimary());
        if (footerLabel   != null) footerLabel.setForeground(ThemeManager.getBorder());

        // Checkbox
        if (showPasswordBox != null) {
            showPasswordBox.setForeground(ThemeManager.getTextSecondary());
        }

        // Separator
        if (separator != null) separator.setForeground(ThemeManager.getBorder());

        // Input fields
        Color inputBg     = ThemeManager.getInputBg();
        Color inputBorder = ThemeManager.getBorder();
        Color inputFg     = dark ? new Color(200, 200, 200) : new Color(18, 18, 18);
        for (JTextField field : new JTextField[]{ emailField, passwordField }) {
            if (field != null) {
                field.setBackground(inputBg);
                field.setForeground(inputFg);
                field.setCaretColor(inputFg);
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(inputBorder, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        }

        // Theme toggle button
        if (themeToggleBtn != null) {
            themeToggleBtn.setBackground(dark ? new Color(45, 45, 45) : new Color(230, 230, 230));
            themeToggleBtn.setForeground(dark ? new Color(200, 200, 200) : new Color(80, 80, 80));
            themeToggleBtn.repaint();
        }

        // Login button stays blue always
        if (loginButton != null) {
            loginButton.setBackground(ThemeManager.CLR_BLUE);
            loginButton.setForeground(Color.WHITE);
        }

        repaint();
        revalidate();
    }

    // -------------------------
    // Component Builders
    // -------------------------
    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_INPUT);
        field.setForeground(ThemeManager.getTextPlaceholder());
        field.setBackground(ThemeManager.getInputBg());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorder(), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeManager.getTextPrimary());
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ThemeManager.CLR_BLUE, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(ThemeManager.getTextPlaceholder());
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ThemeManager.getBorder(), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return field;
    }

    private JPasswordField buildPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_INPUT);
        field.setForeground(ThemeManager.getTextPlaceholder());
        field.setBackground(ThemeManager.getInputBg());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(ThemeManager.getBorder(), 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        field.setEchoChar((char) 0);
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(ThemeManager.getTextPrimary());
                    field.setEchoChar('•');
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ThemeManager.CLR_BLUE, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setForeground(ThemeManager.getTextPlaceholder());
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ThemeManager.getBorder(), 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        field.addActionListener(e -> handleLogin());
        return field;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ThemeManager.CLR_BLUE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(ThemeManager.CLR_BLUE_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(ThemeManager.CLR_BLUE);       }
        });
        btn.addActionListener(e -> handleLogin());
        return btn;
    }

    // -------------------------
    // Login Action
    // -------------------------
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        if (email.isEmpty() || email.equals("Enter your email") ||
            password.isEmpty() || password.equals("Enter your password")) {
            statusLabel.setText("Please fill in all fields.");
            statusLabel.setForeground(ThemeManager.CLR_RED);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Please enter a valid email address.");
            statusLabel.setForeground(ThemeManager.CLR_RED);
            return;
        }
        statusLabel.setText("✓ UI test — login flow works.");
        statusLabel.setForeground(ThemeManager.CLR_GREEN);
    }
}
