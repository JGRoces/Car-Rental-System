package pckUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * CustomTitleBar.java
 * Reusable custom title bar for all JFrames in the application.
 * Auto-registers with UIAssets and repaints on theme change.
 * Animation speed scales to the monitor's refresh rate.
 *
 * Usage (2 lines on any JFrame):
 *   setUndecorated(true);                                           // in initWindow()
 *   add(new CustomTitleBar(this, "Title"), BorderLayout.NORTH);    // first line of initComponents()
 */
public class CustomTitleBar extends JPanel {

    private static final int  BAR_HEIGHT        = 40;
    private static final int  ANIM_DURATION_MS  = 150;
    private static final Font FONT_TITLE        = new Font("Segoe UI", Font.PLAIN, 12);

    private final int animDelayMs;
    private final int animSteps;

    private final JFrame   parentFrame;
    private final String   title;
    private final boolean  lightMode;   // true = white bar (login/signup), false = dark chrome (dashboard)
    private       Point   dragStart;
    private       boolean isMaximized = false;
    private       boolean isAnimating = false;
    private       Rectangle normalBounds;

    // Themed sub-components
    private JButton minimizeBtn, maximizeBtn, closeBtn;
    private JLabel  titleLabel;

    /** Dark chrome title bar — for dashboard windows */
    public CustomTitleBar(JFrame parent, String windowTitle) {
        this(parent, windowTitle, false);
    }

    /** @param lightMode true = white/light bar for login & sign-up screens */
    public CustomTitleBar(JFrame parent, String windowTitle, boolean lightMode) {
        this.parentFrame = parent;
        this.title       = windowTitle;
        this.lightMode   = lightMode;

        // Detect refresh rate
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

        setPreferredSize(new Dimension(0, BAR_HEIGHT));
        setLayout(new BorderLayout());
        buildBar();
        enableDragging();
        applyTheme();
        UIAssets.addListener(this::applyTheme);
    }

    private void buildBar() {
        // LEFT — accent dots + title
        JPanel leftSide = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftSide.setOpaque(false);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        dots.setOpaque(false);
        for (Color c : new Color[]{
                UIAssets.CLR_BLUE, UIAssets.CLR_GREEN,
                UIAssets.CLR_YELLOW, UIAssets.CLR_RED }) {
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        titleLabel.setForeground(lightMode ? UIAssets.getTextSecondary() : new Color(200, 200, 200));

        leftSide.add(dots);
        leftSide.add(titleLabel);

        // RIGHT — control buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        controls.setOpaque(false);

        minimizeBtn = buildControlButton("MIN", UIAssets.CLR_YELLOW, e -> animateMinimize());
        maximizeBtn = buildControlButton("MAX", UIAssets.CLR_GREEN,  e -> animateToggleMaximize());
        closeBtn    = buildControlButton("X",   UIAssets.CLR_RED,    e -> System.exit(0));

        controls.add(minimizeBtn);
        controls.add(maximizeBtn);
        controls.add(closeBtn);

        JPanel lw = new JPanel(new GridBagLayout()); lw.setOpaque(false); lw.add(leftSide);
        JPanel rw = new JPanel(new GridBagLayout()); rw.setOpaque(false); rw.add(controls);
        add(lw, BorderLayout.WEST);
        add(rw, BorderLayout.EAST);
    }

    private void applyTheme() {
        Color bar = lightMode ? UIAssets.getSurface() : UIAssets.getTitleBar();
        setBackground(bar);
        if (minimizeBtn != null) minimizeBtn.setBackground(bar);
        if (maximizeBtn != null) maximizeBtn.setBackground(bar);
        if (closeBtn    != null) closeBtn.setBackground(bar);
        if (titleLabel  != null) titleLabel.setForeground(
            lightMode ? UIAssets.getTextSecondary() : new Color(200, 200, 200));
        repaint();
    }

    private JButton buildControlButton(String type, Color hoverColor, ActionListener action) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.setColor(getForeground());
                g2.setStroke(new BasicStroke(1.5f));
                switch (type) {
                    case "MIN" -> g2.drawLine(cx - 6, cy + 3, cx + 6, cy + 3);
                    case "MAX" -> g2.drawRect(cx - 6, cy - 6, 12, 12);
                    case "X"  -> { g2.drawLine(cx-5,cy-5,cx+5,cy+5); g2.drawLine(cx+5,cy-5,cx-5,cy+5); }
                }
                g2.dispose();
            }
        };
        btn.setForeground(lightMode ? new Color(120, 120, 120) : new Color(140, 140, 140));
        btn.setBackground(lightMode ? UIAssets.getSurface() : UIAssets.getTitleBar());
        btn.setPreferredSize(new Dimension(46, BAR_HEIGHT));
        btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); btn.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(lightMode ? UIAssets.getSurface() : UIAssets.getTitleBar()); btn.setForeground(lightMode ? new Color(120,120,120) : new Color(140,140,140)); }
        });
        btn.addActionListener(action);
        return btn;
    }

    // ---- Animations ----
    private void animateMinimize() {
        if (isAnimating) return;
        isAnimating = true;
        Rectangle start  = parentFrame.getBounds();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int tY = screen.y + screen.height;
        int sX = start.x, sY = start.y, sW = start.width, sH = start.height;
        int[] step = {0};
        Timer t = new Timer(animDelayMs, null);
        t.addActionListener(e -> {
            step[0]++;
            float p = easeIn((float) step[0] / animSteps);
            int cW = (int)(sW - sW * 0.3f * p);
            parentFrame.setBounds((int)(sX+(sW-cW)/2), (int)(sY+(tY-sY)*p), cW, Math.max((int)(sH*(1f-p)),1));
            if (step[0] >= animSteps) {
                t.stop(); isAnimating = false;
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
        t.start();
    }

    private void animateRestore(Rectangle target) {
        if (isAnimating) return;
        isAnimating = true;
        int sW = (int)(target.width * 0.5f), sH = (int)(target.height * 0.3f);
        int sX = target.x + (target.width - sW) / 2, sY = target.y + (target.height - sH) / 2;
        parentFrame.setBounds(sX, sY, sW, sH);
        parentFrame.setVisible(true);
        int[] step = {0};
        Timer t = new Timer(animDelayMs, null);
        t.addActionListener(e -> {
            step[0]++;
            float p = easeOut((float) step[0] / animSteps);
            int cW = (int)(sW + (target.width - sW) * p), cH = (int)(sH + (target.height - sH) * p);
            parentFrame.setBounds(target.x + (target.width - cW)/2, target.y + (target.height - cH)/2, cW, cH);
            if (step[0] >= animSteps) { t.stop(); parentFrame.setBounds(target); isAnimating = false; }
        });
        t.start();
    }

    private void animateToggleMaximize() {
        if (isAnimating) return;
        isAnimating = true;
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Rectangle from, to;
        if (isMaximized) { from = screen; to = normalBounds; }
        else { normalBounds = parentFrame.getBounds(); from = normalBounds; to = screen;
               try { parentFrame.setShape(null); } catch (UnsupportedOperationException ignored) {} }
        final Rectangle aF = from, aT = to;
        int[] step = {0};
        Timer t = new Timer(animDelayMs, null);
        t.addActionListener(e -> {
            step[0]++;
            float p = isMaximized ? easeIn((float)step[0]/animSteps) : easeOut((float)step[0]/animSteps);
            parentFrame.setBounds(
                (int)(aF.x+(aT.x-aF.x)*p), (int)(aF.y+(aT.y-aF.y)*p),
                (int)(aF.width+(aT.width-aF.width)*p), (int)(aF.height+(aT.height-aF.height)*p));
            if (step[0] >= animSteps) {
                t.stop(); parentFrame.setBounds(aT); isMaximized = !isMaximized; isAnimating = false;
                if (!isMaximized) try { parentFrame.setShape(new RoundRectangle2D.Double(0,0,aT.width,aT.height,12,12)); }
                                  catch (UnsupportedOperationException ignored) {}
                parentFrame.revalidate(); parentFrame.repaint();
            }
        });
        t.start();
    }

    private float easeOut(float t) { return 1f - (1f - t) * (1f - t); }
    private float easeIn(float t)  { return t * t; }

    private void enableDragging() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { if (!isMaximized && !isAnimating) dragStart = e.getPoint(); }
            @Override public void mouseClicked(MouseEvent e)  { if (e.getClickCount() == 2) animateToggleMaximize(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (isMaximized || isAnimating) return;
                Point loc = parentFrame.getLocation();
                parentFrame.setLocation(loc.x + e.getX() - dragStart.x, loc.y + e.getY() - dragStart.y);
            }
        });
    }
}
