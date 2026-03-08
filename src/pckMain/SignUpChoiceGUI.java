package pckMain;

import pckCustomer.CustomerSignUpGUI;
import pckDriver.DriverSignUpGUI;
import pckUtils.CustomTitleBar;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * SignUpChoiceGUI.java
 * Shown when the user clicks "Create Account" from LoginGUI.
 * Two choices: Customer or Driver → routes to the respective sign-up screen.
 */
public class SignUpChoiceGUI extends JFrame {


    private final JFrame parent;

    public SignUpChoiceGUI(JFrame parent) {
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
        add(new CustomTitleBar(this, "Car Rental System — Create Account", true), BorderLayout.NORTH);

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
    //  LEFT — same branding panel
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

        JLabel tagline = new JLabel("<html>Join us today — manage your<br>rentals with ease.</html>");
        tagline.setFont(UIAssets.FONT_BODY);
        tagline.setForeground(UIAssets.getTextSecondary());
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(tagline);

        panel.add(inner);
        return panel;
    }

    // =========================================================
    //  RIGHT — Choose role
    // =========================================================
    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIAssets.getSurface());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(UIAssets.getSurface());
        inner.setBorder(new EmptyBorder(0, 52, 0, 52));

        // Back
        JButton back = buildTextButton("< Back to Sign In");
        back.addActionListener(e -> {
            parent.setVisible(true);
            this.dispose();
        });
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(back);
        inner.add(Box.createVerticalStrut(28));

        JLabel title = new JLabel("Create Account");
        title.setFont(UIAssets.FONT_DISPLAY);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(title);
        inner.add(Box.createVerticalStrut(8));

        JLabel sub = new JLabel("Choose how you want to use CarRentals.");
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        inner.add(sub);
        inner.add(Box.createVerticalStrut(36));

        // Customer card
        inner.add(buildRoleCard(
            "Customer",
            "Rent vehicles for personal or business use.",
            UIAssets.CLR_BLUE,
            UIAssets.CLR_BLUE_LIGHT,
            e -> {
                new CustomerSignUpGUI(this).setVisible(true);
                this.setVisible(false);
            }
        ));
        inner.add(Box.createVerticalStrut(14));

        // Driver card
        inner.add(buildRoleCard(
            "Driver",
            "Register as a driver and manage your availability.",
            UIAssets.CLR_GREEN,
            UIAssets.CLR_GREEN_LIGHT,
            e -> {
                new DriverSignUpGUI(this).setVisible(true);
                this.setVisible(false);
            }
        ));

        panel.add(inner);
        return panel;
    }

    /** Clickable role selection card */
    private JPanel buildRoleCard(String role, String desc, Color accent, Color bgLight, ActionListener action) {
        JPanel card = new JPanel(new BorderLayout(14, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        card.setBackground(UIAssets.getSurface());
        card.setOpaque(false);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Left — accent circle
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgLight);
                g2.fillOval(0, 0, 44, 44);
                g2.setColor(accent);
                g2.fillOval(14, 14, 16, 16);
                g2.dispose();
            }
        };
        circle.setPreferredSize(new Dimension(44, 44));
        circle.setOpaque(false);

        // Center — text
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 4));
        text.setOpaque(false);
        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(UIAssets.FONT_H3);
        roleLbl.setForeground(UIAssets.getTextPrimary());
        JLabel descLbl = new JLabel(desc);
        descLbl.setFont(UIAssets.FONT_SMALL);
        descLbl.setForeground(UIAssets.getTextSecondary());
        text.add(roleLbl);
        text.add(descLbl);

        // Right — chevron
        JLabel chevron = new JLabel("›");
        chevron.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        chevron.setForeground(UIAssets.getTextSecondary());

        card.add(circle,  BorderLayout.WEST);
        card.add(text,    BorderLayout.CENTER);
        card.add(chevron, BorderLayout.EAST);

        // Hover + click
        Color hoverBorder = accent;
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(hoverBorder, 1, true), new EmptyBorder(20, 20, 20, 20)));
                card.setBackground(bgLight);
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(20, 20, 20, 20)));
                card.setBackground(UIAssets.getSurface());
            }
            @Override public void mouseClicked(MouseEvent e) { action.actionPerformed(null); }
        });
        return card;
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
}
