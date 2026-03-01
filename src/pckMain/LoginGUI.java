package pckMain;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginGUI.java
 * Shared login screen for both Admin and Customer.
 * After successful login, AuthService will route the user
 * to the correct dashboard based on their role.
 *
 * Design: Minimalist B&W base with blue/green/yellow/red accents.
 */
public class LoginGUI extends JFrame {

    // -------------------------
    // Color Palette
    // -------------------------
    private static final Color CLR_BG         = new Color(245, 245, 245); // Off-white background
    private static final Color CLR_PANEL      = Color.WHITE;              // Card white
    private static final Color CLR_BLACK      = new Color(18, 18, 18);    // Near-black text
    private static final Color CLR_GRAY       = new Color(120, 120, 120); // Subtext gray
    private static final Color CLR_BORDER     = new Color(220, 220, 220); // Subtle border
    private static final Color CLR_BLUE       = new Color(37, 99, 235);   // Primary action blue
    private static final Color CLR_BLUE_HOVER = new Color(29, 78, 216);   // Hover state
    private static final Color CLR_GREEN      = new Color(22, 163, 74);   // Success indicator
    private static final Color CLR_YELLOW     = new Color(234, 179, 8);   // Warning indicator
    private static final Color CLR_RED        = new Color(220, 38, 38);   // Error indicator

    // -------------------------
    // Fonts
    // -------------------------
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,   26);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN,  13);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,   12);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN,  14);
    private static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,   14);
    private static final Font FONT_FOOTER   = new Font("Segoe UI", Font.PLAIN,  11);

    // -------------------------
    // Components
    // -------------------------
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private JCheckBox      showPasswordBox;

    // -------------------------
    // Constructor
    // -------------------------
    public LoginGUI() {
        initWindow();
        initComponents();
    }

    // -------------------------
    // Window Setup
    // -------------------------
    private void initWindow() {
        setTitle("Car Rental System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 540);
        setResizable(false);
        setLocationRelativeTo(null);                  // Center on screen
        getContentPane().setBackground(CLR_BG);
        setLayout(new GridBagLayout());               // Centers the card on the window
    }

    // -------------------------
    // Build UI
    // -------------------------
    private void initComponents() {

        // --- Outer card panel ---
        JPanel card = new JPanel();
        card.setBackground(CLR_PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(40, 40, 36, 40)
        ));
        card.setPreferredSize(new Dimension(360, 460));

        // --- Top accent bar (4 color segments) ---
        JPanel accentBar = buildAccentBar();
        card.add(accentBar);
        card.add(Box.createVerticalStrut(28));

        // --- Title ---
        JLabel titleLabel = new JLabel("Welcome");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(CLR_BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(4));

        // --- Subtitle ---
        JLabel subtitleLabel = new JLabel("Sign in to the Car Rental System");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(CLR_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(28));

        // --- Email field ---
        card.add(buildLabel("Email Address"));
        card.add(Box.createVerticalStrut(6));
        emailField = buildTextField("Enter your email");
        card.add(emailField);
        card.add(Box.createVerticalStrut(16));

        // --- Password field ---
        card.add(buildLabel("Password"));
        card.add(Box.createVerticalStrut(6));
        passwordField = buildPasswordField("Enter your password");
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));

        // --- Show password checkbox ---
        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(FONT_FOOTER);
        showPasswordBox.setForeground(CLR_GRAY);
        showPasswordBox.setBackground(CLR_PANEL);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordBox.addActionListener(e -> togglePasswordVisibility());
        card.add(showPasswordBox);
        card.add(Box.createVerticalStrut(22));

        // --- Status label (shows error/success messages) ---
        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_FOOTER);
        statusLabel.setForeground(CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(6));

        // --- Login button ---
        loginButton = buildLoginButton();
        card.add(loginButton);
        card.add(Box.createVerticalStrut(20));

        // --- Footer ---
        JLabel footerLabel = new JLabel("Car Rental System  •  v1.0");
        footerLabel.setFont(FONT_FOOTER);
        footerLabel.setForeground(CLR_BORDER);
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(footerLabel);

        // --- Add card to frame ---
        add(card);
    }

    // -------------------------
    // Component Builders
    // -------------------------

    private JPanel buildAccentBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 2, 0));
        bar.setBackground(CLR_PANEL);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 5));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        Color[] colors = { CLR_BLUE, CLR_GREEN, CLR_YELLOW, CLR_RED };
        for (Color c : colors) {
            JPanel seg = new JPanel();
            seg.setBackground(c);
            bar.add(seg);
        }
        return bar;
    }

    private JLabel buildLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(CLR_BLACK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(FONT_INPUT);
        field.setForeground(CLR_BLACK);
        field.setBackground(CLR_PANEL);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));

        // Placeholder behavior
        field.setForeground(CLR_GRAY);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CLR_BLACK);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(CLR_BLUE, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(CLR_GRAY);
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(CLR_BORDER, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });
        return field;
    }

    private JPasswordField buildPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(FONT_INPUT);
        field.setForeground(CLR_GRAY);
        field.setBackground(CLR_PANEL);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        field.setEchoChar((char) 0);           // Show placeholder text
        field.setText(placeholder);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(CLR_BLACK);
                    field.setEchoChar('•');
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(CLR_BLUE, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setForeground(CLR_GRAY);
                    field.setText(placeholder);
                }
                field.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(CLR_BORDER, 1, true),
                    new EmptyBorder(6, 12, 6, 12)
                ));
            }
        });

        // Allow pressing Enter to trigger login
        field.addActionListener(e -> handleLogin());
        return field;
    }

    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(CLR_BLUE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(CLR_BLUE_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(CLR_BLUE);       }
        });

        btn.addActionListener(e -> handleLogin());
        return btn;
    }

    // -------------------------
    // Actions
    // -------------------------

    private void togglePasswordVisibility() {
        String currentText = String.valueOf(passwordField.getPassword());
        boolean isPlaceholder = currentText.equals("Enter your password");

        if (!isPlaceholder) {
            if (showPasswordBox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        }
    }

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        // Basic empty field check
        if (email.isEmpty() || email.equals("Enter your email") ||
            password.isEmpty() || password.equals("Enter your password")) {
            setStatus("Please fill in all fields.", CLR_RED);
            return;
        }

        // Basic email format check
        if (!email.contains("@") || !email.contains(".")) {
            setStatus("Please enter a valid email address.", CLR_RED);
            return;
        }

        // -- Placeholder for AuthService --
        // TODO: Replace this block with actual AuthService call
        // Example:
        //   User user = AuthService.login(email, password);
        //   if (user != null) {
        //       SessionManager.setCurrentUser(user);
        //       if (user.isAdmin()) new AdminDashboardGUI().setVisible(true);
        //       else new CustomerDashboardGUI().setVisible(true);
        //       this.dispose();
        //   } else {
        //       setStatus("Invalid email or password.", CLR_RED);
        //   }

        // Temporary feedback for testing UI only
        setStatus("Login functionality coming soon.", CLR_YELLOW);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }
}
