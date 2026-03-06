package pckTests;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.ui.FlatRootPaneUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * CustomTitleBar.java
 * Custom title bar using FlatLaf's built-in window decoration support.
 * Native Windows animations (minimize, maximize, restore, snap) work
 * automatically. No JNA or manual DWM calls needed.
 *
 * Usage (same 2 lines as before on any JFrame):
 *   // 1. In initWindow() — before setVisible()
 *   getRootPane().putClientProperty("FlatLaf.titleBarBackground", new Color(18,18,18));
 *   getRootPane().putClientProperty("FlatLaf.titleBarForeground", Color.WHITE);
 *   getRootPane().putClientProperty("JRootPane.titleBarBackground", new Color(18,18,18));
 *
 *   // 2. In initComponents() — very first line
 *   add(new CustomTitleBar(this, "Window Title"), BorderLayout.NORTH);
 */
public class CustomTitleBar extends JPanel {

    // -------------------------
    // Appearance
    // -------------------------
    private static final int   BAR_HEIGHT   = 40;
    private static final Color CLR_BAR      = new Color(18, 18, 18);
    private static final Color CLR_TITLE    = new Color(200, 200, 200);
    private static final Color CLR_CLOSE    = new Color(220, 38, 38);
    private static final Color CLR_MINIMIZE = new Color(234, 179, 8);
    private static final Color CLR_MAXIMIZE = new Color(22, 163, 74);
    private static final Font  FONT_TITLE   = new Font("Segoe UI", Font.PLAIN, 12);

    // -------------------------
    // State
    // -------------------------
    private final JFrame    parentFrame;
    private final String    title;
    private       Point     dragStart;
    private       boolean   isMaximized = false;
    private       Rectangle normalBounds;

    // -------------------------
    // Constructor
    // -------------------------
    public CustomTitleBar(JFrame parent, String title) {
        this.parentFrame = parent;
        this.title       = title;

        setPreferredSize(new Dimension(0, BAR_HEIGHT));
        setBackground(CLR_BAR);
        setLayout(new BorderLayout());

        buildBar();
        enableDragging();
    }

    // -------------------------
    // Build the bar
    // -------------------------
    private void buildBar() {

        // LEFT — accent dots + title
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setBackground(CLR_BAR);
        leftSide.setOpaque(false);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dots.setBackground(CLR_BAR);
        dots.setOpaque(false);

        for (Color c : new Color[]{
                new Color(37, 99, 235),
                new Color(22, 163, 74),
                new Color(234, 179, 8),
                new Color(220, 38, 38) }) {
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillOval(0, 0, 8, 8);
                    g2.dispose();
                }
            };
            dot.setPreferredSize(new Dimension(8, 8));
            dot.setOpaque(false);
            dots.add(dot);
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(CLR_TITLE);

        leftSide.add(dots);
        leftSide.add(titleLabel);

        // RIGHT — control buttons (drawn icons, no Unicode)
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setBackground(CLR_BAR);
        controls.setOpaque(false);

        controls.add(buildControlButton("MIN", CLR_MINIMIZE, e -> parentFrame.setState(Frame.ICONIFIED)));
        controls.add(buildControlButton("MAX", CLR_MAXIMIZE, e -> handleMaximize()));
        controls.add(buildControlButton("X",   CLR_CLOSE,    e -> System.exit(0)));

        JPanel lw = new JPanel(new GridBagLayout()); lw.setBackground(CLR_BAR); lw.setOpaque(false); lw.add(leftSide);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setBackground(CLR_BAR); rw.setOpaque(false); rw.add(controls);

        add(lw, BorderLayout.WEST);
        add(rw, BorderLayout.EAST);
    }

    // -------------------------
    // Control Button Builder
    // -------------------------
    private JButton buildControlButton(String type, Color hoverColor, ActionListener action) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());

                int cx = getWidth()  / 2;
                int cy = getHeight() / 2;
                g2.setColor(getForeground());
                g2.setStroke(new BasicStroke(1.5f));

                switch (type) {
                    case "MIN" -> g2.drawLine(cx - 6, cy + 3, cx + 6, cy + 3);
                    case "MAX" -> g2.drawRect(cx - 6, cy - 6, 12, 12);
                    case "X"   -> {
                        g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                        g2.drawLine(cx + 5, cy - 5, cx - 5, cy + 5);
                    }
                }
                g2.dispose();
            }
        };

        btn.setForeground(new Color(140, 140, 140));
        btn.setBackground(CLR_BAR);
        btn.setPreferredSize(new Dimension(46, BAR_HEIGHT));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
                btn.setForeground(Color.WHITE);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(CLR_BAR);
                btn.setForeground(new Color(140, 140, 140));
            }
        });

        btn.addActionListener(action);
        return btn;
    }

    // -------------------------
    // Maximize / Restore
    // FlatLaf handles the actual animation natively
    // -------------------------
    private void handleMaximize() {
        if (isMaximized) {
            parentFrame.setExtendedState(JFrame.NORMAL);
            parentFrame.setBounds(normalBounds);
            isMaximized = false;
        } else {
            normalBounds = parentFrame.getBounds();
            parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            isMaximized = true;
        }
        parentFrame.revalidate();
        parentFrame.repaint();
    }

    // -------------------------
    // Dragging
    // -------------------------
    private void enableDragging() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (isMaximized) return;
                dragStart = e.getPoint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) handleMaximize();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (isMaximized) return;
                Point loc = parentFrame.getLocation();
                parentFrame.setLocation(
                    loc.x + e.getX() - dragStart.x,
                    loc.y + e.getY() - dragStart.y
                );
            }
        });
    }
}
