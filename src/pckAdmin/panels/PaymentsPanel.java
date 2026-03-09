package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * PaymentsPanel.java
 * Stub — Stage 3 will flesh this out with full content.
 * AdminDashboardGUI compiles and runs with this in place.
 */
public class PaymentsPanel extends JPanel {

    public PaymentsPanel(AdminNavCallback nav) {
        setLayout(new GridBagLayout());
        setBackground(UIAssets.getBg());

        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);

        JLabel title = new JLabel("PaymentsPanel", SwingConstants.CENTER);
        title.setFont(UIAssets.FONT_TITLE);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Coming in the next stage.", SwingConstants.CENTER);
        sub.setFont(UIAssets.FONT_SUBTITLE);
        sub.setForeground(UIAssets.getTextSecondary());
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        inner.add(title);
        inner.add(Box.createVerticalStrut(8));
        inner.add(sub);
        add(inner);
    }
}
