package pckTests;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * CustomTitleBar.java
 * Custom title bar with theme support via ThemeManager.
 * Automatically repaints when the theme is toggled.
 *
 * Usage (same 2 lines on any JFrame):
 *   setUndecorated(true);
 *   add(new CustomTitleBar(this, "Window Title"), BorderLayout.NORTH);
 *
 * Theme changes are handled automatically — no extra wiring needed per window.
 */
public class CustomTitleBar extends JPanel {

    private static final int  BAR_HEIGHT = 40;
    private static final Font FONT_TITLE = new Font("Segoe UI", Font.PLAIN, 12);

    // Animation config
    private static final int ANIM_DURATION_MS = 150;
    private final int animDelayMs;
    private final int animSteps;

    // State
    private final JFrame    parentFrame;
    private final String    title;
    private       Point     dragStart;
    private       boolean   isMaximized = false;
    private       boolean   isAnimating = false;
    private       Rectangle normalBounds;

    // Themed sub-components that need repainting
    private JPanel titleBarPanel;
    private JLabel titleLabel;
    private JButton minimizeBtn, maximizeBtn, closeBtn;

    // -------------------------
    // Constructor
    // -------------------------
    public CustomTitleBar(JFrame parent, String windowTitle) {
        this.parentFrame = parent;
        this.title       = windowTitle;

        // Detect monitor refresh rate
        int refreshRate = 60;
        try {
            GraphicsDevice screen   = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                                          .getDefaultScreenDevice();
            int detected = screen.getDisplayMode().getRefreshRate();
            if (detected > 0 && detected != DisplayMode.REFRESH_RATE_UNKNOWN)
                refreshRate = detected;
        } catch (Exception ignored) {}

        this.animDelayMs = Math.max(1, 1000 / refreshRate);
        this.animSteps   = Math.max(4, ANIM_DURATION_MS / animDelayMs);

        System.out.println("[CustomTitleBar] " + refreshRate + "hz detected | "
            + animDelayMs + "ms/frame | " + animSteps + " steps");

        setPreferredSize(new Dimension(0, BAR_HEIGHT));
        setLayout(new BorderLayout());

        buildBar();
        enableDragging();
        applyTheme();

        // Register with ThemeManager — auto-repaint on theme toggle
        ThemeManager.addListener(this::applyTheme);
    }

    // -------------------------
    // Build Bar
    // -------------------------
    private void buildBar() {

        // LEFT — accent dots + title label
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setOpaque(false);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dots.setOpaque(false);

        for (Color c : new Color[]{
                ThemeManager.CLR_BLUE, ThemeManager.CLR_GREEN,
                ThemeManager.CLR_YELLOW, ThemeManager.CLR_RED }) {
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

        titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);

        leftSide.add(dots);
        leftSide.add(titleLabel);

        // RIGHT — control buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        minimizeBtn = buildControlButton("MIN", ThemeManager.CLR_YELLOW, e -> animateMinimize());
        maximizeBtn = buildControlButton("MAX", ThemeManager.CLR_GREEN,  e -> animateToggleMaximize());
        closeBtn    = buildControlButton("X",   ThemeManager.CLR_RED,    e -> System.exit(0));

        controls.add(minimizeBtn);
        controls.add(maximizeBtn);
        controls.add(closeBtn);

        JPanel lw = new JPanel(new GridBagLayout()); lw.setOpaque(false); lw.add(leftSide);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(controls);

        add(lw, BorderLayout.WEST);
        add(rw, BorderLayout.EAST);
    }

    // -------------------------
    // Apply Theme
    // Called on init and every time ThemeManager fires
    // -------------------------
    private void applyTheme() {
        Color bar = ThemeManager.getTitleBar();

        setBackground(bar);
        if (titleLabel != null)
            titleLabel.setForeground(new Color(200, 200, 200));

        // Refresh button backgrounds to match new bar color
        for (JButton btn : new JButton[]{ minimizeBtn, maximizeBtn, closeBtn }) {
            if (btn != null) btn.setBackground(bar);
        }

        repaint();
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
        btn.setBackground(ThemeManager.getTitleBar());
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
                btn.setBackground(ThemeManager.getTitleBar());
                btn.setForeground(new Color(140, 140, 140));
            }
        });

        btn.addActionListener(action);
        return btn;
    }

    // ====================================================
    //  ANIMATIONS
    // ====================================================

    private void animateMinimize() {
        if (isAnimating) return;
        isAnimating = true;

        Rectangle start  = parentFrame.getBounds();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                              .getMaximumWindowBounds();
        int targetY = screen.y + screen.height;
        int startX = start.x, startY = start.y, startW = start.width, startH = start.height;
        int[] step = {0};

        Timer timer = new Timer(animDelayMs, null);
        timer.addActionListener(e -> {
            step[0]++;
            float p = easeIn((float) step[0] / animSteps);
            parentFrame.setBounds(
                (int)(startX + (startW - (int)(startW - startW * 0.3f * p)) / 2),
                (int)(startY + (targetY - startY) * p),
                (int)(startW - startW * 0.3f * p),
                Math.max((int)(startH * (1f - p)), 1)
            );
            if (step[0] >= animSteps) {
                timer.stop();
                isAnimating = false;
                parentFrame.setBounds(start);
                parentFrame.setState(Frame.ICONIFIED);
                parentFrame.addWindowStateListener(new WindowStateListener() {
                    @Override public void windowStateChanged(WindowEvent evt) {
                        if ((evt.getNewState() & Frame.ICONIFIED) == 0) {
                            parentFrame.removeWindowStateListener(this);
                            animateRestore(start);
                        }
                    }
                });
            }
        });
        timer.start();
    }

    private void animateRestore(Rectangle target) {
        if (isAnimating) return;
        isAnimating = true;

        int startW = (int)(target.width * 0.5f), startH = (int)(target.height * 0.3f);
        int startX = target.x + (target.width - startW) / 2;
        int startY = target.y + (target.height - startH) / 2;

        parentFrame.setBounds(startX, startY, startW, startH);
        parentFrame.setVisible(true);

        int[] step = {0};
        Timer timer = new Timer(animDelayMs, null);
        timer.addActionListener(e -> {
            step[0]++;
            float p = easeOut((float) step[0] / animSteps);
            int cw = (int)(startW + (target.width  - startW) * p);
            int ch = (int)(startH + (target.height - startH) * p);
            parentFrame.setBounds(
                target.x + (target.width  - cw) / 2,
                target.y + (target.height - ch) / 2,
                cw, ch
            );
            if (step[0] >= animSteps) {
                timer.stop();
                parentFrame.setBounds(target);
                isAnimating = false;
            }
        });
        timer.start();
    }

    private void animateToggleMaximize() {
        if (isAnimating) return;
        isAnimating = true;

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                              .getMaximumWindowBounds();
        Rectangle from, to;
        if (isMaximized) {
            from = screen; to = normalBounds;
        } else {
            normalBounds = parentFrame.getBounds();
            from = normalBounds; to = screen;
            try { parentFrame.setShape(null); } catch (UnsupportedOperationException ignored) {}
        }

        final Rectangle animFrom = from, animTo = to;
        int[] step = {0};

        Timer timer = new Timer(animDelayMs, null);
        timer.addActionListener(e -> {
            step[0]++;
            float p = isMaximized
                ? easeIn( (float) step[0] / animSteps)
                : easeOut((float) step[0] / animSteps);

            parentFrame.setBounds(
                (int)(animFrom.x      + (animTo.x      - animFrom.x)      * p),
                (int)(animFrom.y      + (animTo.y      - animFrom.y)      * p),
                (int)(animFrom.width  + (animTo.width  - animFrom.width)  * p),
                (int)(animFrom.height + (animTo.height - animFrom.height) * p)
            );

            if (step[0] >= animSteps) {
                timer.stop();
                parentFrame.setBounds(animTo);
                isMaximized = !isMaximized;
                isAnimating = false;
                if (!isMaximized) {
                    try {
                        parentFrame.setShape(new RoundRectangle2D.Double(
                            0, 0, animTo.width, animTo.height, 12, 12));
                    } catch (UnsupportedOperationException ignored) {}
                }
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        });
        timer.start();
    }

    private float easeOut(float t) { return 1f - (1f - t) * (1f - t); }
    private float easeIn(float t)  { return t * t; }

    // -------------------------
    // Dragging
    // -------------------------
    private void enableDragging() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (isMaximized || isAnimating) return;
                dragStart = e.getPoint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) animateToggleMaximize();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (isMaximized || isAnimating) return;
                Point loc = parentFrame.getLocation();
                parentFrame.setLocation(
                    loc.x + e.getX() - dragStart.x,
                    loc.y + e.getY() - dragStart.y
                );
            }
        });
    }
}
