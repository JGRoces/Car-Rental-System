package pckAdmin.shared;

import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

/**
 * AdminUIHelper.java
 * ─────────────────────────────────────────────────────────────
 * Single source of truth for ALL reusable UI component builders
 * inside the admin dashboard. Every panel and tab imports this
 * class instead of reimplementing the same patterns.
 *
 * Design tokens match LoginGUI / SignUp screens exactly:
 *   • Page bg         → UIAssets.getBg()           #efefef light
 *   • Card / surface  → UIAssets.getSurface()       #ffffff
 *   • Card border     → LineBorder(getBorder(), 1)
 *   • Input rest      → getBorder() + EmptyBorder(10, 14, 10, 14)
 *   • Input focus     → CLR_BLUE  + EmptyBorder(10, 14, 10, 14)
 *   • Solid button    → RoundRectangle2D radius 8, height 46
 *   • Outline button  → radius 8, 1.5px CLR_BLUE stroke
 *   • Table row       → 40px, FONT_BODY, getSurface() bg
 *   • Section padding → EmptyBorder(32, 36, 32, 36)
 *   • Group gap       → Box.createVerticalStrut(24)
 *
 * All methods are static — import AdminUIHelper and call directly,
 * no instance needed:
 *   JButton btn = AdminUIHelper.buildSolidButton("Save", UIAssets.CLR_BLUE);
 */
public class AdminUIHelper {

    // Prevent instantiation — utility class only
    private AdminUIHelper() {}

    // =========================================================
    //  ICON LOADING + TINTING
    // =========================================================

    /**
     * Loads a black PNG icon and tints every non-transparent pixel
     * to the target color, preserving the alpha channel exactly.
     *
     * Works because the source icons are black (R=0, G=0, B=0) —
     * tinting replaces the RGB values while keeping the shape defined
     * by the alpha mask intact. Result is a crisp icon in any color.
     *
     * Returns null (not an exception) if the file is missing — callers
     * should handle null gracefully (e.g. render text-only).
     *
     * Usage:
     *   ImageIcon gray  = AdminUIHelper.loadIcon(AppConfig.ICON_NAV_OVERVIEW,
     *                         UIAssets.getTextSecondary(), 16);
     *   ImageIcon white = AdminUIHelper.loadIcon(AppConfig.ICON_NAV_OVERVIEW,
     *                         Color.WHITE, 16);
     *
     * @param path      relative path to the black PNG (e.g. AppConfig.ICON_NAV_OVERVIEW)
     * @param tintColor target color to paint the icon pixels
     * @param size      pixel size to scale to (16 fits a 44px tall nav row cleanly)
     */
    public static ImageIcon loadIcon(String path, Color tintColor, int size) {
        try {
            BufferedImage original = javax.imageio.ImageIO.read(new java.io.File(path));
            if (original == null) return null;

            // Step 1 — scale to target size with smooth interpolation
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                                RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(original, 0, 0, size, size, null);
            g2.dispose();

            // Step 2 — replace every non-transparent pixel's RGB with tintColor
            int tr = tintColor.getRed();
            int tg = tintColor.getGreen();
            int tb = tintColor.getBlue();
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int argb  = scaled.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    if (alpha > 0) {
                        scaled.setRGB(x, y, (alpha << 24) | (tr << 16) | (tg << 8) | tb);
                    }
                }
            }
            return new ImageIcon(scaled);

        } catch (Exception e) {
            System.err.println("[AdminUIHelper] Could not load icon: "
                + path + " — " + e.getMessage());
            return null;
        }
    }

    // =========================================================
    //  BUTTONS
    // =========================================================

    /**
     * Solid filled button — primary actions (Save, Add, Submit).
     * Rounded corners radius 8, white text, hover darkens bg.
     *
     *   AdminUIHelper.buildSolidButton("Add Vehicle", UIAssets.CLR_BLUE)
     */
    public static JButton buildSolidButton(String label, Color bg) {
        Color hover = bg.darker();
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
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 16, 0, 16));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);    }
        });
        return btn;
    }

    /**
     * Solid button with a leading icon — same visual as buildSolidButton
     * but includes a tinted PNG icon to the left of the label.
     * Icon is automatically tinted WHITE to match button text.
     *
     *   AdminUIHelper.buildSolidButtonWithIcon("Add", AppConfig.ICON_ADD, UIAssets.CLR_BLUE)
     */
    public static JButton buildSolidButtonWithIcon(String label, String iconPath, Color bg) {
        JButton btn = buildSolidButton(label, bg);
        ImageIcon icon = loadIcon(iconPath, Color.WHITE, 15);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(7);
            // Recalculate preferred width now that icon is set
            btn.setPreferredSize(new Dimension(btn.getPreferredSize().width + icon.getIconWidth() + 7, 40));
        }
        return btn;
    }

    /**
     * Outlined button — secondary actions (Cancel, Filter).
     * CLR_BLUE border 1.5px stroke, blue text, hover fills CLR_BLUE_LIGHT.
     */
    public static JButton buildOutlineButton(String label) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(UIAssets.CLR_BLUE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 7, 7));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIAssets.FONT_BUTTON);
        btn.setForeground(UIAssets.CLR_BLUE);
        btn.setBackground(UIAssets.getSurface());
        btn.setPreferredSize(new Dimension(btn.getPreferredSize().width, 40));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 16, 0, 16));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(UIAssets.CLR_BLUE_LIGHT); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(UIAssets.getSurface());   }
        });
        return btn;
    }

    /**
     * Outlined button with a leading icon — same as buildOutlineButton
     * but includes a blue-tinted PNG icon to the left of the label.
     *
     *   AdminUIHelper.buildOutlineButtonWithIcon("Export", AppConfig.ICON_EXPORT)
     */
    public static JButton buildOutlineButtonWithIcon(String label, String iconPath) {
        JButton btn = buildOutlineButton(label);
        ImageIcon icon = loadIcon(iconPath, UIAssets.CLR_BLUE, 15);
        if (icon != null) {
            btn.setIcon(icon);
            btn.setIconTextGap(7);
        }
        return btn;
    }

    /**
     * Small ghost/text button — inline tertiary action (View, Edit, Details).
     * No border, no fill, just colored text with underline-on-hover feel.
     */
    public static JButton buildGhostButton(String label, Color color) {
        JButton btn = new JButton(label);
        btn.setFont(UIAssets.FONT_SMALL);
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 0, 0, 0));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(color.darker()); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(color); }
        });
        return btn;
    }

    /**
     * Small icon-only button — used for Refresh, Filter, Export, etc.
     * Renders a 36×36 transparent button showing only the icon.
     * On hover: paints a subtle rounded bg tint so the button feels clickable.
     *
     * Pass a tooltip so the action is discoverable without a label.
     *
     * Returns a text-fallback button ("…") when the icon file is missing
     * so the UI never breaks silently.
     *
     * Usage:
     *   JButton refreshBtn = AdminUIHelper.buildIconOnlyButton(
     *       AppConfig.ICON_REFRESH, UIAssets.CLR_BLUE, "Refresh");
     *   refreshBtn.addActionListener(e -> refresh());
     */
    public static JButton buildIconOnlyButton(String iconPath, Color iconTint, String tooltip) {
        ImageIcon icon = loadIcon(iconPath, iconTint, 16);
        boolean[] hovered = { false };

        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered[0]) {
                    g2.setColor(UIAssets.getBorder());
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        if (icon != null) {
            btn.setIcon(icon);
        } else {
            // Graceful fallback when icon file is absent — plain text, no box artifact
            btn.setText("↻");
            btn.setFont(UIAssets.FONT_BUTTON);
            btn.setForeground(iconTint);
        }

        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true;  btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered[0] = false; btn.repaint(); }
        });
        return btn;
    }

    // =========================================================
    //  FORM FIELDS
    // =========================================================

    /**
     * Field label above each input — bold 13px, textPrimary color.
     * Matches the label style used in CustomerSignUpGUI exactly.
     */
    public static JLabel buildFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setForeground(UIAssets.getTextPrimary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Text field with placeholder, border-swap focus listener.
     * Rest:  LineBorder(getBorder())  + 10px vertical padding
     * Focus: LineBorder(CLR_BLUE)     + 10px vertical padding
     */
    public static JTextField buildTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setForeground(UIAssets.getTextPlaceholder());
        field.setBackground(UIAssets.getSurface());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyRestBorder(field);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIAssets.getTextPrimary());
                }
                applyFocusBorder(field);
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(UIAssets.getTextPlaceholder());
                }
                applyRestBorder(field);
            }
        });
        return field;
    }

    /**
     * Password field with placeholder and echo-char swap on focus.
     * Rest state shows placeholder as plain text; gains '•' on focus.
     */
    public static JPasswordField buildPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField();
        field.setFont(UIAssets.FONT_INPUT);
        field.setForeground(UIAssets.getTextPlaceholder());
        field.setBackground(UIAssets.getSurface());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        applyRestBorder(field);
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIAssets.getTextPrimary());
                    field.setEchoChar('•');
                }
                applyFocusBorder(field);
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setForeground(UIAssets.getTextPlaceholder());
                    field.setText(placeholder);
                }
                applyRestBorder(field);
            }
        });
        return field;
    }

    /**
     * Non-editable display field — same visual as input but read-only.
     * Used in Account panel and Settings rows.
     */
    public static JTextField buildReadOnlyField(String value) {
        JTextField field = new JTextField(value);
        field.setFont(UIAssets.FONT_BODY);
        field.setForeground(UIAssets.getTextSecondary());
        field.setBackground(UIAssets.getBg());
        field.setEditable(false);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /** JComboBox styled to match the input field visual language */
    public static <T> JComboBox<T> buildComboBox(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setFont(UIAssets.FONT_INPUT);
        box.setBackground(UIAssets.getSurface());
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        return box;
    }

    // ── Border helpers (called by text fields + panels) ───────
    public static void applyRestBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
    }

    public static void applyFocusBorder(JComponent c) {
        c.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.CLR_BLUE, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));
    }

    // =========================================================
    //  CARDS
    // =========================================================

    /**
     * White surface card with rounded border.
     * Use as the container for any grouped content block.
     * Inner padding: 24px all sides unless overridden.
     */
    public static JPanel buildCard(int innerPadding) {
        JPanel card = new JPanel();
        card.setBackground(UIAssets.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(innerPadding, innerPadding, innerPadding, innerPadding)
        ));
        return card;
    }

    /**
     * Overview stat card — top label + accent dot, large value, subtitle.
     */
    public static JPanel buildStatCard(String label, String value,
                                       String subtitle, Color accent, Color accentLight) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(UIAssets.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(22, 22, 22, 22)
        ));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIAssets.FONT_H3);
        lbl.setForeground(UIAssets.getTextSecondary());
        topRow.add(lbl, BorderLayout.WEST);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accentLight);
                g2.fillOval(0, 0, 26, 26);
                g2.setColor(accent);
                g2.fillOval(8, 8, 10, 10);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(26, 26));
        dot.setOpaque(false);
        topRow.add(dot, BorderLayout.EAST);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(UIAssets.FONT_STAT_VALUE);
        valLbl.setForeground(accent);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UIAssets.FONT_SMALL);
        subLbl.setForeground(UIAssets.getTextPlaceholder());

        JPanel bottomStack = new JPanel(new GridLayout(2, 1, 0, 3));
        bottomStack.setOpaque(false);
        bottomStack.add(valLbl);
        bottomStack.add(subLbl);

        card.add(topRow,      BorderLayout.NORTH);
        card.add(bottomStack, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================
    //  PAGE STRUCTURE
    // =========================================================

    /**
     * Page header — title + subtitle stacked, 24px bottom margin.
     */
    public static JPanel buildPageHeader(String title, String subtitle) {
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setOpaque(false);
        stack.setBorder(new EmptyBorder(0, 0, 24, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIAssets.FONT_TITLE);
        titleLbl.setForeground(UIAssets.getTextPrimary());
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(UIAssets.FONT_SUBTITLE);
        subLbl.setForeground(UIAssets.getTextSecondary());
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        stack.add(titleLbl);
        stack.add(Box.createVerticalStrut(4));
        stack.add(subLbl);
        return stack;
    }

    /**
     * Section label — bold 15px, used above grouped content blocks.
     */
    public static JLabel buildSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIAssets.FONT_H2);
        lbl.setForeground(UIAssets.getTextPrimary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * Horizontal separator — 1px, getBorder() color.
     */
    public static JSeparator buildDivider() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    // =========================================================
    //  TABLES
    // =========================================================

    /**
     * Styled JTable — consistent across all tabs and panels.
     */
    public static JTable buildStyledTable(String[] cols, Object[][] rows) {
        DefaultTableModel model = (rows != null)
            ? new DefaultTableModel(rows, cols) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
              }
            : new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
              };

        JTable table = new JTable(model);
        table.setFont(UIAssets.FONT_BODY);
        table.setForeground(UIAssets.getTextPrimary());
        table.setBackground(UIAssets.getSurface());
        table.setRowHeight(40);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UIAssets.getBorder());
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(UIAssets.CLR_BLUE_LIGHT);
        table.setSelectionForeground(UIAssets.getTextPrimary());
        table.getTableHeader().setFont(UIAssets.FONT_H3);
        table.getTableHeader().setBackground(UIAssets.getSurface());
        table.getTableHeader().setForeground(UIAssets.getTextSecondary());
        table.getTableHeader().setBorder(
            new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()));
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    /**
     * Wraps a JTable in a scroll pane styled to match the admin panels.
     */
    public static JScrollPane buildTableScrollPane(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        scroll.setBackground(UIAssets.getSurface());
        scroll.getViewport().setBackground(UIAssets.getSurface());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        return scroll;
    }

    // =========================================================
    //  STATUS BADGES
    // =========================================================

    /**
     * Colored pill badge for status columns — renders inline in a table cell.
     */
    public static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object val, boolean sel, boolean focus, int r, int c) {
            String text = val == null ? "" : val.toString();
            Color fg, bg;
            switch (text.toUpperCase()) {
                case "PENDING"   -> { fg = UIAssets.CLR_YELLOW; bg = UIAssets.CLR_YELLOW_LIGHT; }
                case "VERIFIED",
                     "ACTIVE",
                     "AVAILABLE" -> { fg = UIAssets.CLR_GREEN;  bg = UIAssets.CLR_GREEN_LIGHT;  }
                case "REJECTED",
                     "CANCELLED" -> { fg = UIAssets.CLR_RED;    bg = UIAssets.CLR_RED_LIGHT;    }
                case "COMPLETED" -> { fg = UIAssets.CLR_BLUE;   bg = UIAssets.CLR_BLUE_LIGHT;   }
                case "RENTED",
                     "MAINTENANCE"->{ fg = UIAssets.CLR_YELLOW; bg = UIAssets.CLR_YELLOW_LIGHT; }
                default          -> { fg = UIAssets.getTextSecondary(); bg = UIAssets.getBorder(); }
            }
            final Color finalFg = fg, finalBg = bg;
            JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(finalBg);
                    g2.fillRoundRect(4, 6, getWidth()-8, getHeight()-12, getHeight(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lbl.setFont(UIAssets.FONT_SMALL);
            lbl.setForeground(finalFg);
            lbl.setOpaque(false);
            return lbl;
        }
    }

    // =========================================================
    //  EMPTY STATE
    // =========================================================

    /**
     * Centered empty-state message shown when a table has no rows.
     */
    public static JLabel buildEmptyState(String message) {
        JLabel lbl = new JLabel(message, SwingConstants.CENTER);
        lbl.setFont(UIAssets.FONT_SUBTITLE);
        lbl.setForeground(UIAssets.getTextPlaceholder());
        lbl.setBorder(new EmptyBorder(48, 0, 48, 0));
        return lbl;
    }

    // =========================================================
    //  PROFILE PHOTO
    // =========================================================

    /**
     * Circular profile photo from a file path.
     * Falls back to defaultPath if filePath is null or unreadable.
     */
    public static JPanel buildCircularPhoto(String filePath, String defaultPath, int size) {
        String path = (filePath != null && !filePath.isBlank()) ? filePath : defaultPath;
        ImageIcon raw = new ImageIcon(path);
        Image img = raw.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);

        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, size, size));
                g2.drawImage(img, 0, 0, size, size, null);
                g2.setClip(null);
                g2.setColor(UIAssets.getBorder());
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0, 0, size - 1, size - 1);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(size, size); }
            @Override public Dimension getMinimumSize()   { return new Dimension(size, size); }
            @Override public Dimension getMaximumSize()   { return new Dimension(size, size); }
        };
    }
}
