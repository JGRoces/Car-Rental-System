package pckTests;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginGUI_Test.java
 * Mock LoginGUI using FlatLaf for native Windows animations + custom title bar.
 *
 * FlatLaf replaces the default Java Look and Feel. Calling
 * FlatDarkLaf.setup() before creating any JFrame is all that's needed —
 * it handles the DWM wiring automatically.
 */
public class LoginGUI_Test extends JFrame {

    // -------------------------
    // Color Palette
    // -------------------------
    private static final Color CLR_BG         = new Color(245, 245, 245);
    private static final Color CLR_WHITE      = Color.WHITE;
    private static final Color CLR_IMAGE_BG   = new Color(20, 20, 20);
    private static final Color CLR_IMAGE_TEXT = new Color(60, 60, 60);
    private static final Color CLR_BLACK      = new Color(18, 18, 18);
    private static final Color CLR_GRAY       = new Color(120, 120, 120);
    private static final Color CLR_BORDER     = new Color(220, 220, 220);
    private static final Color CLR_BLUE       = new Color(37, 99, 235);
    private static final Color CLR_BLUE_HOVER = new Color(29, 78, 216);
    private static final Color CLR_GREEN      = new Color(22, 163, 74);
    private static final Color CLR_YELLOW     = new Color(234, 179, 8);
    private static final Color CLR_RED        = new Color(220, 38, 38);

    // -------------------------
    // Fonts
    // -------------------------
    private static final Font FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  28);
    private static final Font FONT_SUBTITLE  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_LABEL     = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_INPUT     = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON    = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_FOOTER    = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_IMG_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    private static final Font FONT_IMG_SUB   = new Font("Segoe UI", Font.PLAIN, 13);

    // -------------------------
    // Components
    // -------------------------
    private JTextField     emailField;
    private JPasswordField passwordField;
    private JButton        loginButton;
    private JLabel         statusLabel;
    private JCheckBox      showPasswordBox;

    // -------------------------
    // Entry point
    // -------------------------
    public static void main(String[] args) {
        FlatDarkLaf.setup(); // FIX 1: Must be called before any Swing component is created
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
    }

    // -------------------------
    // Window Setup
    // FlatLaf requires these RootPane properties to take over the title bar
    // -------------------------
    private void initWindow() {
        // Tell FlatLaf to use custom window decorations
        // This removes the default Windows title bar and hands control to FlatLaf
        getRootPane().putClientProperty("JRootPane.titleBarBackground",  new Color(18, 18, 18));
        getRootPane().putClientProperty("JRootPane.titleBarForeground",  Color.WHITE);
        getRootPane().putClientProperty("JRootPane.titleBarShowTitle",   false);  // Hide default title text
        getRootPane().putClientProperty("JRootPane.titleBarHeight",      0);      // Zero height — we draw our own

        setTitle("Car Rental System — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CLR_BG);
    }

    // -------------------------
    // Build Layout
    // -------------------------
    private void initComponents() {

        // Custom title bar sits at the very top
        add(new CustomTitleBar(this, "Car Rental System — Login"), BorderLayout.NORTH);

        // Root split panel — LEFT 60% / RIGHT 40%
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(CLR_IMAGE_BG);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill    = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.weightx = 0.6;
        gbc.gridx   = 0;
        root.add(buildLeftPanel(), gbc);

        gbc.weightx = 0.4;
        gbc.gridx   = 1;
        root.add(buildRightPanel(), gbc);

        add(root, BorderLayout.CENTER);
    }

    // -------------------------
    // LEFT — Image Panel
    // -------------------------
    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_IMAGE_BG);

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(CLR_IMAGE_BG);
        inner.setBorder(new EmptyBorder(0, 52, 0, 52));

        JPanel accentBar = new JPanel(new GridLayout(1, 4, 3, 0));
        accentBar.setBackground(CLR_IMAGE_BG);
        accentBar.setMaximumSize(new Dimension(80, 5));
        accentBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (Color c : new Color[]{ CLR_BLUE, CLR_GREEN, CLR_YELLOW, CLR_RED }) {
            JPanel seg = new JPanel();
            seg.setBackground(c);
            accentBar.add(seg);
        }
        inner.add(accentBar);
        inner.add(Box.createVerticalStrut(24));

        JLabel brandLabel = new JLabel("CarRentals");
        brandLabel.setFont(FONT_IMG_TITLE);
        brandLabel.setForeground(Color.WHITE);
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(brandLabel);
        inner.add(Box.createVerticalStrut(10));

        JLabel tagline = new JLabel(
            "<html>Your trusted platform for<br>seamless car rental management.</html>");
        tagline.setFont(FONT_IMG_SUB);
        tagline.setForeground(CLR_IMAGE_TEXT);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(40));

        JPanel imgPlaceholder = new JPanel(new GridBagLayout());
        imgPlaceholder.setBackground(new Color(32, 32, 32));
        imgPlaceholder.setBorder(new LineBorder(new Color(45, 45, 45), 1, true));
        imgPlaceholder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        imgPlaceholder.setPreferredSize(new Dimension(560, 400));
        imgPlaceholder.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel phText = new JPanel();
        phText.setLayout(new BoxLayout(phText, BoxLayout.Y_AXIS));
        phText.setBackground(new Color(32, 32, 32));

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
        inner.add(imgPlaceholder);

        panel.add(inner);
        return panel;
    }

    // -------------------------
    // RIGHT — Login Form
    // -------------------------
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CLR_WHITE);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(CLR_WHITE);
        form.setBorder(new EmptyBorder(0, 52, 0, 52));
        form.setPreferredSize(new Dimension(380, 600));

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(CLR_BLACK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(titleLabel);
        form.add(Box.createVerticalStrut(6));

        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(CLR_GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitleLabel);
        form.add(Box.createVerticalStrut(36));

        form.add(buildLabel("Email Address"));
        form.add(Box.createVerticalStrut(6));
        emailField = buildTextField("Enter your email");
        form.add(emailField);
        form.add(Box.createVerticalStrut(20));

        form.add(buildLabel("Password"));
        form.add(Box.createVerticalStrut(6));
        passwordField = buildPasswordField("Enter your password");
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(FONT_FOOTER);
        showPasswordBox.setForeground(CLR_GRAY);
        showPasswordBox.setBackground(CLR_WHITE);
        showPasswordBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordBox.addActionListener(e -> togglePasswordVisibility());
        form.add(showPasswordBox);
        form.add(Box.createVerticalStrut(24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(FONT_FOOTER);
        statusLabel.setForeground(CLR_RED);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(statusLabel);
        form.add(Box.createVerticalStrut(8));

        loginButton = buildLoginButton();
        form.add(loginButton);
        form.add(Box.createVerticalStrut(28));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(CLR_BORDER);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(sep);
        form.add(Box.createVerticalStrut(20));

        JLabel footerLabel = new JLabel("Car Rental System  •  v1.0  •  TEST BUILD");
        footerLabel.setFont(FONT_FOOTER);
        footerLabel.setForeground(CLR_BORDER);
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(footerLabel);

        panel.add(form);
        return panel;
    }

    // -------------------------
    // Component Builders
    // -------------------------
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
        field.setForeground(CLR_GRAY);
        field.setBackground(CLR_WHITE);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
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
        field.setBackground(CLR_WHITE);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        field.setEchoChar((char) 0);
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
        btn.setBackground(CLR_BLUE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
        String current = String.valueOf(passwordField.getPassword());
        if (!current.equals("Enter your password")) {
            passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : '•');
        }
    }

    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = String.valueOf(passwordField.getPassword());

        if (email.isEmpty() || email.equals("Enter your email") ||
            password.isEmpty() || password.equals("Enter your password")) {
            setStatus("Please fill in all fields.", CLR_RED);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            setStatus("Please enter a valid email address.", CLR_RED);
            return;
        }
        setStatus("✓ UI test — login flow works.", CLR_GREEN);
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    // =========================================================
    // FIX 2: CustomTitleBar — was missing, caused compile error
    // =========================================================
    private static class CustomTitleBar extends JPanel {

        private Point dragStart;

        public CustomTitleBar(JFrame owner, String title) {
            setBackground(new Color(18, 18, 18));
            setPreferredSize(new Dimension(0, 36));
            setLayout(new BorderLayout());

            // Title label
            JLabel titleLabel = new JLabel("  " + title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLabel.setForeground(Color.WHITE);
            add(titleLabel, BorderLayout.CENTER);

            // Window controls (minimize, close)
            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            controls.setBackground(new Color(18, 18, 18));
            controls.add(makeTitleBarButton("—", new Color(18, 18, 18), new Color(50, 50, 50),
                    e -> owner.setState(Frame.ICONIFIED)));
            controls.add(makeTitleBarButton("✕", new Color(18, 18, 18), new Color(196, 43, 28),
                    e -> System.exit(0)));
            add(controls, BorderLayout.EAST);

            // Drag-to-move
            addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override public void mouseDragged(MouseEvent e) {
                    Point loc = owner.getLocation();
                    owner.setLocation(
                        loc.x + e.getX() - dragStart.x,
                        loc.y + e.getY() - dragStart.y
                    );
                }
            });
        }

        private JButton makeTitleBarButton(String text, Color bg, Color hover, ActionListener action) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setForeground(Color.WHITE);
            btn.setBackground(bg);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setOpaque(true);
            btn.setPreferredSize(new Dimension(46, 36));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
                @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);    }
            });
            btn.addActionListener(action);
            return btn;
        }
    }
}