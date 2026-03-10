package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckUtils.SessionManager;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class AccountPanel extends JPanel {

    public AccountPanel(AdminNavCallback nav) {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
    }

    private void build() {
        add(AdminUIHelper.buildPageHeader("Account",
            "Manage your admin profile and security settings."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(buildGroup("Profile Details", buildProfileRows()));
        body.add(Box.createVerticalStrut(24));
        body.add(buildGroup("Security", buildSecurityRows()));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildProfileRows() {
        String name  = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getFullName() : "Administrator";
        String email = SessionManager.isLoggedIn() ? SessionManager.getCurrentUser().getEmail()    : "admin@carrental.com";

        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.add(buildSettingRow("Full Name",     name,            AdminUIHelper.buildReadOnlyField(name)));
        rows.add(AdminUIHelper.buildDivider());
        rows.add(buildSettingRow("Email",         email,           AdminUIHelper.buildReadOnlyField(email)));
        rows.add(AdminUIHelper.buildDivider());
        rows.add(buildSettingRow("Role",          "Administrator", AdminUIHelper.buildReadOnlyField("Administrator")));
        return rows;
    }

    private JPanel buildSecurityRows() {
        JPanel rows = new JPanel();
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setOpaque(false);
        rows.setBorder(new EmptyBorder(0, 20, 16, 20));

        JLabel hint = new JLabel("Enter your current password before setting a new one.");
        hint.setFont(UIAssets.FONT_SMALL);
        hint.setForeground(UIAssets.getTextSecondary());
        hint.setBorder(new EmptyBorder(16, 0, 12, 0));
        rows.add(hint);

        JPasswordField current = AdminUIHelper.buildPasswordField("Current password");
        JPasswordField newPass = AdminUIHelper.buildPasswordField("New password");
        JPasswordField confirm = AdminUIHelper.buildPasswordField("Confirm new password");

        rows.add(AdminUIHelper.buildFieldLabel("Current Password")); rows.add(Box.createVerticalStrut(4)); rows.add(current);
        rows.add(Box.createVerticalStrut(12));
        rows.add(AdminUIHelper.buildFieldLabel("New Password"));     rows.add(Box.createVerticalStrut(4)); rows.add(newPass);
        rows.add(Box.createVerticalStrut(12));
        rows.add(AdminUIHelper.buildFieldLabel("Confirm Password")); rows.add(Box.createVerticalStrut(4)); rows.add(confirm);
        rows.add(Box.createVerticalStrut(16));

        JButton saveBtn = AdminUIHelper.buildSolidButton("Update Password", UIAssets.CLR_BLUE);
        saveBtn.addActionListener(e -> {
            String np = new String(newPass.getPassword());
            String cp = new String(confirm.getPassword());
            if (np.isBlank()) { JOptionPane.showMessageDialog(this, "New password cannot be empty."); return; }
            if (!np.equals(cp)) { JOptionPane.showMessageDialog(this, "Passwords do not match."); return; }
            JOptionPane.showMessageDialog(this, "Password updated successfully.");
        });
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        rows.add(btnRow);
        return rows;
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
}
