package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckDatabase.DatabaseConnection;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class SettingsPanel extends JPanel {

    private JButton themeToggleBtn;

    public SettingsPanel(AdminNavCallback nav) {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
        UIAssets.addListener(this::onThemeChanged);
    }

    private void build() {
        add(AdminUIHelper.buildPageHeader("Settings",
            "Manage application preferences and configuration."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(buildGroup("Appearance",     buildAppearanceRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildGroup("Business Rules", buildBusinessRulesRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildGroup("Database",       buildDatabaseRows()));
        body.add(Box.createVerticalStrut(20));
        body.add(buildGroup("System",         buildSystemRows()));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildAppearanceRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        themeToggleBtn = buildThemePill();
        rows.add(buildSettingRow(
            UIAssets.isDark() ? "Dark Mode" : "Light Mode",
            "Switch the application color theme",
            themeToggleBtn));
        return rows;
    }

    private JPanel buildBusinessRulesRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Standard Late Fee Rate",
            "Fee applied per day when a vehicle is returned late",
            AdminUIHelper.buildComboBox(new String[]{"\u20b1500 / day", "\u20b11,000 / day"})));
        rows.add(AdminUIHelper.buildDivider());
        rows.add(buildSettingRow("Default Tax Rate",
            "Percentage applied to all rental transactions",
            AdminUIHelper.buildComboBox(new String[]{"12%", "0%"})));
        return rows;
    }

    private JPanel buildDatabaseRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);

        final boolean connected = isDatabaseConnected();
        Color statusColor = connected ? UIAssets.CLR_GREEN : UIAssets.CLR_RED;
        String statusText = connected ? "Connected" : "Disconnected";

        JLabel badge = new JLabel("  " + statusText + "  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(connected ? UIAssets.CLR_GREEN_LIGHT : UIAssets.CLR_RED_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UIAssets.FONT_SMALL);
        badge.setForeground(statusColor);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        badge.setOpaque(false);

        rows.add(buildSettingRow("MySQL Connection", "car_rental_db @ localhost", badge));
        return rows;
    }

    private JPanel buildSystemRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Language", "English (Default)", buildComingSoonBadge()));
        rows.add(AdminUIHelper.buildDivider());
        rows.add(buildSettingRow("Application Version", "v1.0.0 — Car Rental System",
            new JLabel("") {{ setPreferredSize(new Dimension(1, 1)); }}));
        return rows;
    }

    private JButton buildThemePill() {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean dark = UIAssets.isDark();
                int w = getWidth(), h = getHeight();
                g2.setColor(dark ? UIAssets.CLR_BLUE : new Color(180, 180, 180));
                g2.fillRoundRect(0, 0, w, h, h, h);
                int knobSize = h - 6;
                int knobX    = dark ? w - knobSize - 3 : 3;
                g2.setColor(Color.WHITE);
                g2.fillOval(knobX, 3, knobSize, knobSize);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(52, 28));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> UIAssets.toggleTheme());
        return btn;
    }

    private JLabel buildComingSoonBadge() {
        JLabel badge = new JLabel("Coming Soon") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.CLR_YELLOW_LIGHT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(UIAssets.FONT_SMALL);
        badge.setForeground(UIAssets.CLR_YELLOW);
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));
        badge.setOpaque(false);
        return badge;
    }

    private JPanel buildGroup(String title, JPanel content) {
        JPanel group = new JPanel(new BorderLayout());
        group.setOpaque(false);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = AdminUIHelper.buildSectionLabel(title);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIAssets.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true), new EmptyBorder(4, 0, 4, 0)));
        card.add(content);
        group.add(lbl,  BorderLayout.NORTH);
        group.add(card, BorderLayout.CENTER);
        return group;
    }

    private JPanel buildSettingRow(String label, String subtitle, JComponent control) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        row.setBorder(new EmptyBorder(14, 20, 14, 20));
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UIAssets.FONT_H3);
        JLabel sub = new JLabel(subtitle); sub.setFont(UIAssets.FONT_SMALL); sub.setForeground(UIAssets.getTextSecondary());
        text.add(lbl); text.add(sub);
        JPanel wrap = new JPanel(new GridBagLayout()); wrap.setOpaque(false); wrap.add(control);
        row.add(text, BorderLayout.WEST);
        row.add(wrap, BorderLayout.EAST);
        return row;
    }

    private boolean isDatabaseConnected() {
        try {
            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            return conn != null && !conn.isClosed();
        } catch (java.sql.SQLException e) {
            System.err.println("[SettingsPanel] DB connection check failed: " + e.getMessage());
            return false;
        }
    }

    private void onThemeChanged() {
        if (themeToggleBtn != null) themeToggleBtn.repaint();
    }
}
