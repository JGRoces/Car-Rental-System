package pckCustomer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import pckModels.Car;
import pckServices.CarService;

/**
 * BrowseCarsGUI.java
 * Customer sub-panel — Browse cars from the database.
 * Extends JPanel — embedded in CustomerDashboardGUI via CardLayout.
 *
 * Layout:
 * ┌──────────────────────────────────────────────────────┐
 * │  "Browse Cars" + results count          [Refresh]    │  ← HEADER
 * ├──────────────────────────────────────────────────────┤
 * │  🔍 Search bar                                       │  ← SEARCH
 * ├─────────────────────────────────────┬────────────────┤
 * │  [ Card ][ Card ][ Card ][ Card ]   │  Detail Panel  │
 * │  [ Card ][ Card ][ Card ][ Card ]   │  (slide-out)   │  ← BODY
 * └─────────────────────────────────────┴────────────────┘
 *
 * Image loading:
 *  - DB stores filename only (e.g. "honda-civic.png")
 *  - Resolved to assets/images/<filename> at runtime
 *  - Cover scaling: fills placeholder, preserves aspect ratio, crops excess
 *  - Falls back to category emoji if image is missing or null
 */
public class BrowseCarsGUI extends JPanel {

    // ─────────────────────────────────────────────
    //  Colors
    // ─────────────────────────────────────────────
    private static final Color CLR_BG     = new Color(245, 245, 245);
    private static final Color CLR_WHITE  = Color.WHITE;
    private static final Color CLR_BLACK  = new Color(18, 18, 18);
    private static final Color CLR_GRAY   = new Color(120, 120, 120);
    private static final Color CLR_BORDER = new Color(220, 220, 220);
    private static final Color CLR_BLUE   = new Color(37, 99, 235);
    private static final Color CLR_GREEN  = new Color(22, 163, 74);
    private static final Color CLR_RED    = new Color(220, 38, 38);

    // ─────────────────────────────────────────────
    //  Fonts
    // ─────────────────────────────────────────────
    private static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_LABEL       = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BOLD        = new Font("Segoe UI", Font.BOLD,  12);
    private static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_DETAIL_HEAD = new Font("Segoe UI", Font.BOLD,  16);

    // ─────────────────────────────────────────────
    //  Detail panel constants
    // ─────────────────────────────────────────────
    private static final int DETAIL_WIDTH = 540;
    private static final int SLIDE_STEP   = 45;
    private static final int SLIDE_DELAY  = 8;

    // ─────────────────────────────────────────────
    //  State
    // ─────────────────────────────────────────────
    private List<Car> allCars;
    private Car       selectedCar;
    private boolean   detailVisible = false;

    // ─────────────────────────────────────────────
    //  Components
    // ─────────────────────────────────────────────
    private JLabel     resultsCountLabel;
    private JPanel     cardGrid;
    private JTextField searchField;
    private JPanel     bodyPanel;
    private JPanel     detailPanel;
    private JPanel     selectedCardPanel;

    // ─────────────────────────────────────────────
    //  Callback — set by CustomerDashboardGUI
    // ─────────────────────────────────────────────
    private Runnable onRentNow;

    public void setOnRentNow(Runnable r) {
        this.onRentNow = r;
    }

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────
    public BrowseCarsGUI() {
        setLayout(new BorderLayout());
        setBackground(CLR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        loadCars();
        initComponents();
    }

    // ─────────────────────────────────────────────
    //  Load from DB
    // ─────────────────────────────────────────────
    private void loadCars() {
        try {
            allCars = CarService.getAllCars();
        } catch (Exception e) {
            System.err.println("[BrowseCarsGUI] DB load failed: " + e.getMessage());
            allCars = List.of();
        }
    }

    // ─────────────────────────────────────────────
    //  Build layout
    // ─────────────────────────────────────────────
    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setBackground(CLR_BG);
        topStack.add(buildHeader());
        topStack.add(Box.createVerticalStrut(12));
        topStack.add(buildSearchBar());
        topStack.add(Box.createVerticalStrut(12));

        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(CLR_BG);
        bodyPanel.add(buildCardsArea(), BorderLayout.CENTER);

        detailPanel = buildDetailShell();
        detailPanel.setPreferredSize(new Dimension(0, 0));
        detailPanel.setVisible(false);
        bodyPanel.add(detailPanel, BorderLayout.EAST);

        add(topStack,  BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    // ====================================================
    //  HEADER
    // ====================================================
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CLR_BG);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(CLR_BG);

        JLabel title = new JLabel("Browse Cars");
        title.setFont(FONT_TITLE);
        title.setForeground(CLR_BLACK);

        resultsCountLabel = new JLabel(allCars.size() + " cars found");
        resultsCountLabel.setFont(FONT_SUBTITLE);
        resultsCountLabel.setForeground(CLR_GRAY);

        left.add(title);
        left.add(resultsCountLabel);

        JButton refreshBtn = new JButton("Refresh") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        refreshBtn.setFont(FONT_BOLD);
        refreshBtn.setForeground(CLR_BLUE);
        refreshBtn.setBackground(new Color(219, 234, 254));
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(new Color(191, 219, 254)); }
            @Override public void mouseExited(MouseEvent e)  { refreshBtn.setBackground(new Color(219, 234, 254)); }
        });
        refreshBtn.addActionListener(e -> {
            refreshBtn.setText("Loading...");
            refreshBtn.setEnabled(false);
            refresh();
            refreshBtn.setText("Refresh");
            refreshBtn.setEnabled(true);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setBackground(CLR_BG);
        right.add(refreshBtn);

        header.add(left,  BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ====================================================
    //  SEARCH BAR
    // ====================================================
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_BG);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_WHITE);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(CLR_BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 10, 10));
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 14, 8, 14));

        JLabel icon = new JLabel("\uD83D\uDD0D");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        icon.setBorder(new EmptyBorder(0, 0, 0, 8));

        searchField = new JTextField();
        searchField.setFont(FONT_LABEL);
        searchField.setForeground(CLR_GRAY);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setText("Search by brand, model, or plate number...");

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().startsWith("Search")) {
                    searchField.setText("");
                    searchField.setForeground(CLR_BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Search by brand, model, or plate number...");
                    searchField.setForeground(CLR_GRAY);
                }
            }
        });
        searchField.addActionListener(e -> filterCards(searchField.getText().trim()));

        wrapper.add(icon,        BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);
        panel.add(wrapper,       BorderLayout.CENTER);
        return panel;
    }

    // ====================================================
    //  CAR CARDS GRID  (5 columns, scrollable)
    // ====================================================
    private JScrollPane buildCardsArea() {
        cardGrid = new JPanel(new GridLayout(0, 5, 16, 16));
        cardGrid.setBackground(CLR_BG);
        cardGrid.setBorder(new EmptyBorder(4, 0, 4, 0));
        populateGrid(allCars);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CLR_BG);
        wrapper.add(cardGrid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void populateGrid(List<Car> cars) {
        cardGrid.removeAll();
        if (cars.isEmpty()) {
            cardGrid.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No cars found.", SwingConstants.CENTER);
            empty.setFont(FONT_SUBTITLE);
            empty.setForeground(CLR_GRAY);
            cardGrid.add(empty, BorderLayout.CENTER);
        } else {
            cardGrid.setLayout(new GridLayout(0, 5, 12, 12));
            for (Car car : cars) {
                cardGrid.add(buildCarCard(car));
            }
        }
        resultsCountLabel.setText(cars.size() + " cars found");
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    // ====================================================
    //  SINGLE CAR CARD  (fixed 280px height, rounded)
    // ====================================================
    private JPanel buildCarCard(Car car) {

        final JPanel[] cardRef = new JPanel[1];

        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(3, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.setColor(CLR_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovered  = Boolean.TRUE.equals(getClientProperty("hovered"));
                boolean selected = (cardRef[0] == selectedCardPanel);
                g2.setColor(selected || hovered ? CLR_BLUE : new Color(190, 190, 190));
                g2.setStroke(new BasicStroke(selected || hovered ? 2f : 1f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2.dispose();
            }
        };
        cardRef[0] = card;

        card.setOpaque(false);
        card.setBorder(new EmptyBorder(4, 4, 6, 4));
        card.setPreferredSize(new Dimension(0, 280));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.putClientProperty("hovered", true);  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.putClientProperty("hovered", false); card.repaint(); }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (selectedCardPanel != null) selectedCardPanel.repaint();
                selectedCardPanel = card;
                card.repaint();
                openDetail(car);
            }
        });

        // ── Image area (140px) — cover paint with rounded top corners ──
        final BufferedImage cardImg = loadImageRaw(car.getImagePath());

        JPanel imgArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                // Clip to rounded top corners only (bottom is flush with info panel)
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));

                if (cardImg != null) {
                    // Cover: scale to fill, center, crop excess — no distortion
                    int pw = getWidth(),       ph = getHeight();
                    int iw = cardImg.getWidth(), ih = cardImg.getHeight();
                    double scale = Math.max((double) pw / iw, (double) ph / ih);
                    int drawW = (int) (iw * scale);
                    int drawH = (int) (ih * scale);
                    int x = (pw - drawW) / 2;
                    int y = (ph - drawH) / 2;
                    g2.drawImage(cardImg, x, y, drawW, drawH, null);
                } else {
                    // Fallback — plain background, emoji added as child component
                    g2.setColor(new Color(235, 238, 245));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        imgArea.setOpaque(false);
        imgArea.setPreferredSize(new Dimension(0, 140));

        if (cardImg == null) {
            imgArea.add(buildCarEmoji(car.getCategory()), BorderLayout.CENTER);
        }

        // ── Info area ──
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(CLR_WHITE);
        info.setOpaque(true);
        info.setBorder(new EmptyBorder(10, 12, 12, 12));

        JLabel nameLbl = new JLabel(car.getBrand() + " " + car.getModel());
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(CLR_BLACK);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));

        JLabel metaLbl = new JLabel(car.getYear() + "  \u00B7  " + car.getCategory());
        metaLbl.setFont(FONT_SMALL);
        metaLbl.setForeground(CLR_GRAY);
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(metaLbl);
        info.add(Box.createVerticalStrut(2));

        JLabel specLbl = new JLabel(car.getTransmission() + "  \u00B7  " + car.getSeatCapacity() + " seats");
        specLbl.setFont(FONT_SMALL);
        specLbl.setForeground(CLR_GRAY);
        specLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(specLbl);
        info.add(Box.createVerticalStrut(8));

        JPanel div = new JPanel();
        div.setBackground(CLR_BORDER);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(div);
        info.add(Box.createVerticalStrut(8));

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setBackground(CLR_WHITE);
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLbl = new JLabel("\u20B1" + String.format("%,.0f", car.getDailyRate().doubleValue()));
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        priceLbl.setForeground(CLR_BLUE);

        JLabel perDayLbl = new JLabel("/day");
        perDayLbl.setFont(FONT_SMALL);
        perDayLbl.setForeground(CLR_GRAY);

        priceRow.add(priceLbl,  BorderLayout.WEST);
        priceRow.add(perDayLbl, BorderLayout.EAST);
        info.add(priceRow);

        card.add(imgArea, BorderLayout.CENTER);
        card.add(info,    BorderLayout.SOUTH);
        return card;
    }

    // ====================================================
    //  SLIDE-OUT DETAIL PANEL
    // ====================================================
    private JPanel buildDetailShell() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CLR_WHITE);
        panel.setBorder(new MatteBorder(0, 1, 0, 0, CLR_BORDER));
        return panel;
    }

    private void openDetail(Car car) {
        selectedCar = car;
        detailPanel.removeAll();
        detailPanel.add(buildDetailContent(car), BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
        if (!detailVisible) {
            detailVisible = true;
            detailPanel.setVisible(true);
            animateDetail(true);
        }
    }

    private void closeDetail() {
        if (selectedCardPanel != null) {
            selectedCardPanel.repaint();
            selectedCardPanel = null;
        }
        animateDetail(false);
    }

    private void animateDetail(boolean open) {
        int[] current = { detailPanel.getPreferredSize().width };
        int target = open ? DETAIL_WIDTH : 0;

        Timer t = new Timer(SLIDE_DELAY, null);
        t.addActionListener(e -> {
            current[0] = open
                    ? Math.min(current[0] + SLIDE_STEP, target)
                    : Math.max(current[0] - SLIDE_STEP, target);
            detailPanel.setPreferredSize(new Dimension(current[0], 0));
            bodyPanel.revalidate();
            if (current[0] == target) {
                t.stop();
                if (!open) {
                    detailPanel.setVisible(false);
                    detailVisible = false;
                    selectedCar   = null;
                }
            }
        });
        t.start();
    }

    private JPanel buildDetailContent(Car car) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_WHITE);

        // ── Image area — cover image as background, topBar floats on top ──
        final BufferedImage detailImg = loadImageRaw(car.getImagePath());

        JPanel imgArea = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);

                if (detailImg != null) {
                    // Cover: scale to fill, center, crop excess — no distortion
                    int pw = getWidth(),        ph = getHeight();
                    int iw = detailImg.getWidth(), ih = detailImg.getHeight();
                    double scale = Math.max((double) pw / iw, (double) ph / ih);
                    int drawW = (int) (iw * scale);
                    int drawH = (int) (ih * scale);
                    int x = (pw - drawW) / 2;
                    int y = (ph - drawH) / 2;
                    g2.drawImage(detailImg, x, y, drawW, drawH, null);
                } else {
                    g2.setColor(new Color(235, 238, 245));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        imgArea.setOpaque(false);
        imgArea.setPreferredSize(new Dimension(DETAIL_WIDTH, 180));

        if (detailImg == null) {
            imgArea.add(buildCarEmoji(car.getCategory()), BorderLayout.CENTER);
        }

        // topBar overlays the image via BorderLayout.NORTH
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(8, 8, 0, 8));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(buildStatusBadge(car.getStatus()));
        topBar.add(badgeWrap, BorderLayout.WEST);

        JButton closeBtn = new JButton("\u2190  Back") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtn.setForeground(CLR_GRAY);
        closeBtn.setBackground(new Color(230, 230, 230));
        closeBtn.setPreferredSize(new Dimension(90, 28));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { closeBtn.setBackground(new Color(210, 210, 210)); }
            @Override public void mouseExited(MouseEvent e)  { closeBtn.setBackground(new Color(230, 230, 230)); }
        });
        closeBtn.addActionListener(e -> closeDetail());
        topBar.add(closeBtn, BorderLayout.EAST);
        imgArea.add(topBar, BorderLayout.NORTH);

        root.add(imgArea, BorderLayout.NORTH);

        // ── Scrollable body ──
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_WHITE);
        body.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel nameHead = new JLabel(car.getBrand() + " " + car.getModel());
        nameHead.setFont(FONT_DETAIL_HEAD);
        nameHead.setForeground(CLR_BLACK);
        nameHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(nameHead);
        body.add(Box.createVerticalStrut(2));

        JLabel yearCat = new JLabel(car.getYear() + "  \u00B7  " + car.getCategory());
        yearCat.setFont(FONT_SMALL);
        yearCat.setForeground(CLR_GRAY);
        yearCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(yearCat);
        body.add(Box.createVerticalStrut(16));

        body.add(buildSectionHeader("Specifications"));
        body.add(Box.createVerticalStrut(8));
        body.add(buildSpecRow("Plate Number",  car.getPlateNumber()));
        body.add(buildSpecRow("Transmission",  car.getTransmission()));
        body.add(buildSpecRow("Seat Capacity", car.getSeatCapacity() + " seats"));
        body.add(buildSpecRow("Category",      car.getCategory()));
        body.add(Box.createVerticalStrut(16));

        double base  = car.getDailyRate().doubleValue();
        double tax   = base * 0.12;
        double total = base + tax;

        body.add(buildSectionHeader("Daily Rate Breakdown"));
        body.add(Box.createVerticalStrut(8));
        body.add(buildSpecRow("Base Rate", "\u20B1" + String.format("%,.2f", base)));
        body.add(buildSpecRow("VAT (12%)", "\u20B1" + String.format("%,.2f", tax)));
        body.add(Box.createVerticalStrut(4));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(new Color(219, 234, 254));
        totalRow.setBorder(new EmptyBorder(8, 10, 8, 10));
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel totalKey = new JLabel("Total per Day");
        totalKey.setFont(FONT_BOLD);
        totalKey.setForeground(CLR_BLUE);
        JLabel totalVal = new JLabel("\u20B1" + String.format("%,.2f", total));
        totalVal.setFont(FONT_BOLD);
        totalVal.setForeground(CLR_BLUE);
        totalRow.add(totalKey, BorderLayout.WEST);
        totalRow.add(totalVal, BorderLayout.EAST);
        body.add(totalRow);
        body.add(Box.createVerticalStrut(16));

        body.add(buildSectionHeader("Availability"));
        body.add(Box.createVerticalStrut(8));
        if ("AVAILABLE".equals(car.getStatus())) {
            body.add(buildSpecRow("Status",        "Available now"));
            body.add(buildSpecRow("Next Available", "\u2014"));
        } else if ("RENTED".equals(car.getStatus())) {
            body.add(buildSpecRow("Status",        "Currently rented"));
            body.add(buildSpecRow("Available From", "TODO: check rentals"));
        } else {
            body.add(buildSpecRow("Status",        "Under maintenance"));
            body.add(buildSpecRow("Available From", "TBD"));
        }
        body.add(Box.createVerticalStrut(24));

        boolean available = "AVAILABLE".equals(car.getStatus());
        JButton rentBtn = new JButton(available ? "Rent Now" : "Not Available") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rentBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rentBtn.setForeground(Color.WHITE);
        rentBtn.setBackground(available ? CLR_GREEN : CLR_GRAY);
        rentBtn.setEnabled(available);
        rentBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        rentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        rentBtn.setBorderPainted(false);
        rentBtn.setContentAreaFilled(false);
        rentBtn.setFocusPainted(false);
        rentBtn.setCursor(available ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        if (available) {
            rentBtn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { rentBtn.setBackground(CLR_GREEN.darker()); }
                @Override public void mouseExited(MouseEvent e)  { rentBtn.setBackground(CLR_GREEN); }
            });
            rentBtn.addActionListener(e -> {
                if (onRentNow != null) onRentNow.run();
            });
        }
        body.add(rentBtn);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setViewportBorder(null);
        bodyScroll.getViewport().setBackground(CLR_WHITE);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(bodyScroll, BorderLayout.CENTER);
        return root;
    }

    // ─────────────────────────────────────────────
    //  Detail helpers
    // ─────────────────────────────────────────────
    private JLabel buildSectionHeader(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(CLR_GRAY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildSpecRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(CLR_WHITE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new MatteBorder(0, 0, 1, 0, new Color(240, 240, 240)));
        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(FONT_SMALL);
        keyLbl.setForeground(CLR_GRAY);
        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_BOLD);
        valLbl.setForeground(CLR_BLACK);
        row.add(keyLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    // ====================================================
    //  STATUS BADGE
    // ====================================================
    private JPanel buildStatusBadge(String status) {
        Color bg, fg;
        String text;
        switch (status) {
            case "AVAILABLE"   -> { bg = new Color(220, 252, 231); fg = CLR_GREEN; text = "\u25CF Available";  }
            case "RENTED"      -> { bg = new Color(254, 226, 226); fg = CLR_RED;   text = "\u25CF Rented Out"; }
            case "MAINTENANCE" -> { bg = new Color(229, 231, 235); fg = new Color(107, 114, 128); text = "\u25CF Unavailable"; }
            default            -> { bg = new Color(229, 231, 235); fg = CLR_GRAY;  text = "\u25CF Unknown";    }
        }
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        badge.setPreferredSize(new Dimension(94, 22));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(fg);
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        badge.add(lbl);
        return badge;
    }

    // ====================================================
    //  IMAGE LOADER
    // ====================================================

    /**
     * Loads a raw BufferedImage from assets/images/<filename>.
     * No pre-scaling — paintComponent handles cover scaling at runtime.
     * Returns null if filename is blank, file not found, or read fails.
     */
    private BufferedImage loadImageRaw(String filename) {
        if (filename == null || filename.isBlank()) return null;
        try {
            File file = new File("assets/images/" + filename);
            if (!file.exists()) {
                System.err.println("[BrowseCarsGUI] Image not found: " + file.getAbsolutePath());
                return null;
            }
            return ImageIO.read(file);
        } catch (Exception e) {
            System.err.println("[BrowseCarsGUI] Error loading image: " + filename);
            e.printStackTrace();
            return null;
        }
    }

    // ====================================================
    //  CAR EMOJI PLACEHOLDER
    // ====================================================
    private JLabel buildCarEmoji(String category) {
        String emoji = switch (category) {
            case "SUV"    -> "\uD83D\uDE99";
            case "Van"    -> "\uD83D\uDE90";
            case "Truck"  -> "\uD83D\uDE9B";
            case "Pickup" -> "\uD83D\uDEFB";
            default       -> "\uD83D\uDE97";
        };
        JLabel lbl = new JLabel(emoji, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        return lbl;
    }

    // ====================================================
    //  PUBLIC FILTER METHODS
    // ====================================================
    public void filterByType(String type) {
        List<Car> filtered = allCars.stream()
                .filter(c -> c.getCategory().equalsIgnoreCase(type))
                .toList();
        populateGrid(filtered);
        closeDetailIfOpen();
    }

    public void filterByBrand(String brand) {
        List<Car> filtered = allCars.stream()
                .filter(c -> c.getBrand().equalsIgnoreCase(brand))
                .toList();
        populateGrid(filtered);
        closeDetailIfOpen();
    }

    public void filterByPriceRange(String range) {
        List<Car> filtered = allCars.stream()
                .filter(c -> matchesPriceRange(c.getDailyRate(), range))
                .toList();
        populateGrid(filtered);
        closeDetailIfOpen();
    }

    public void showAll() {
        populateGrid(allCars);
        closeDetailIfOpen();
    }

    private void filterCards(String query) {
        if (query.isBlank()) {
            populateGrid(allCars);
            return;
        }
        String q = query.toLowerCase();
        List<Car> filtered = allCars.stream()
                .filter(c -> c.getBrand().toLowerCase().contains(q)
                        || c.getModel().toLowerCase().contains(q)
                        || c.getPlateNumber().toLowerCase().contains(q))
                .toList();
        populateGrid(filtered);
        closeDetailIfOpen();
    }

    private boolean matchesPriceRange(BigDecimal rate, String range) {
        double r = rate.doubleValue();
        return switch (range) {
            case "Under \u20B11,000"              -> r < 1000;
            case "\u20B11,000 \u2013 \u20B12,000" -> r >= 1000 && r <= 2000;
            case "Above \u20B12,000"              -> r > 2000;
            default -> true;
        };
    }

    private void closeDetailIfOpen() {
        if (detailVisible) closeDetail();
    }

    /** Reload from DB — call after car inventory changes */
    public void refresh() {
        loadCars();
        populateGrid(allCars);
        closeDetailIfOpen();
    }

    public void updateResultsCount(int count) {
        resultsCountLabel.setText(count + " cars found");
    }
}