package pckCustomer;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import pckModels.Car;
import pckServices.CarService;
import pckUtils.UIAssets;

public class BrowseCarsGUI extends JPanel {

    private static final int DETAIL_WIDTH = 540;
    private static final int SLIDE_STEP   = 45;
    private static final int SLIDE_DELAY  = 8;

    private List<Car> allCars;
    private Car       selectedCar;
    private boolean   detailVisible = false;
    private java.util.Map<String, String> activeFilters = new java.util.LinkedHashMap<>();
    private java.util.Map<String, javax.swing.JButton> filterPillButtons = new java.util.LinkedHashMap<>();

    private JLabel     resultsCountLabel;
    private JPanel     cardGrid;
    private JTextField searchField;
    private JPanel     bodyPanel;
    private JPanel     detailPanel;
    private JPanel     selectedCardPanel;
    private JPanel     chipRow;
    private Runnable   onRentNow;

    public void setOnRentNow(Runnable r) { this.onRentNow = r; }
    public Car getSelectedCar()          { return selectedCar; }

    public BrowseCarsGUI() {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        loadCars();
        initComponents();
    }

    private void loadCars() {
        try { allCars = CarService.getAllCars(); }
        catch (RuntimeException e) { System.err.println("[BrowseCarsGUI] DB load failed: " + e.getMessage()); allCars = List.of(); }
    }

    private void initComponents() {
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setBackground(UIAssets.getBg());
        topStack.add(buildHeader());
        topStack.add(Box.createVerticalStrut(12));
        topStack.add(buildSearchBar());
        topStack.add(Box.createVerticalStrut(12));

        bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBackground(UIAssets.getBg());
        bodyPanel.add(buildCardsArea(), BorderLayout.CENTER);

        detailPanel = buildDetailShell();
        detailPanel.setPreferredSize(new Dimension(0, 0));
        detailPanel.setVisible(false);
        bodyPanel.add(detailPanel, BorderLayout.EAST);

        add(topStack,  BorderLayout.NORTH);
        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIAssets.getBg());
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMinimumSize(new Dimension(0, 120));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(UIAssets.getBg());
        titleBlock.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("Browse Cars");
        title.setFont(UIAssets.FONT_TITLE);
        title.setForeground(UIAssets.getTextPrimary());
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        resultsCountLabel = new JLabel(allCars.size() + " cars found");
        resultsCountLabel.setFont(UIAssets.FONT_SUBTITLE);
        resultsCountLabel.setForeground(UIAssets.getTextSecondary());
        resultsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(resultsCountLabel);
        titleBlock.add(Box.createVerticalStrut(8));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filterRow.setBackground(UIAssets.getBg());
        filterRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        filterPillButtons.clear();
        
        JPanel typePanel = buildFilterCombo("Type",
            new String[]{ "All Types", "Sedan", "SUV", "MPV", "Van", "Pickup", "Coupe", "Minivan", "Truck" },
            v -> { if (v.equals("All Types")) { activeFilters.remove("Type"); applyFilters(); } else filterByType(v); });
        filterPillButtons.put("Type", findPillButton(typePanel));
        filterRow.add(typePanel);
        
        JPanel brandPanel = buildFilterCombo("Brand",
            buildBrandOptions(),
            v -> { if (v.equals("All Brands")) { activeFilters.remove("Brand"); applyFilters(); } else filterByBrand(v); });
        filterPillButtons.put("Brand", findPillButton(brandPanel));
        filterRow.add(brandPanel);
        
        JPanel transPanel = buildFilterCombo("Transmission",
            new String[]{ "All", "Automatic", "Manual", "CVT" },
            v -> { if (v.equals("All")) { activeFilters.remove("Transmission"); applyFilters(); } else filterByTransmission(v); });
        filterPillButtons.put("Transmission", findPillButton(transPanel));
        filterRow.add(transPanel);
        
        JPanel fuelPanel = buildFilterCombo("Fuel",
            new String[]{ "All Fuels", "Gasoline", "Diesel", "Hybrid", "Electric" },
            v -> { if (v.equals("All Fuels")) { activeFilters.remove("Fuel Type"); applyFilters(); } else filterByFuelType(v); });
        filterPillButtons.put("Fuel", findPillButton(fuelPanel));
        filterRow.add(fuelPanel);
        
        JPanel pricePanel = buildFilterCombo("Price",
            new String[]{ "All Prices", "Under \u20B11,000", "\u20B11,000 \u2013 \u20B12,000", "Above \u20B12,000" },
            v -> { if (v.equals("All Prices")) { activeFilters.remove("Price"); applyFilters(); } else filterByPriceRange(v); });
        filterPillButtons.put("Price", findPillButton(pricePanel));
        filterRow.add(pricePanel);
        
        JPanel colorPanel = buildFilterCombo("Color",
            buildColorOptions(),
            v -> { if (v.equals("All Colors")) { activeFilters.remove("Color"); applyFilters(); } else filterByColor(v); });
        filterPillButtons.put("Color", findPillButton(colorPanel));
        filterRow.add(colorPanel);

        JButton refreshBtn = new JButton("Refresh") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(UIAssets.CLR_BLUE_LIGHT.darker());
                g2.setStroke(new BasicStroke(1));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int textWidth = fm.stringWidth(text);
                int textHeight = fm.getAscent();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        };
        refreshBtn.setFont(UIAssets.FONT_H3);
        refreshBtn.setForeground(UIAssets.CLR_BLUE);
        refreshBtn.setBackground(UIAssets.CLR_BLUE_LIGHT);
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.setBorderPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { refreshBtn.setBackground(new Color(191, 219, 254)); }
            @Override public void mouseExited(MouseEvent e)  { refreshBtn.setBackground(UIAssets.CLR_BLUE_LIGHT); }
        });
        refreshBtn.addActionListener(e -> {
            refreshBtn.setText("Loading...");
            refreshBtn.setEnabled(false);
            refresh();
            refreshBtn.setText("Refresh");
            refreshBtn.setEnabled(true);
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(UIAssets.getBg());
        JPanel refreshWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        refreshWrapper.setBackground(UIAssets.getBg());
        refreshWrapper.add(refreshBtn);
        rightPanel.add(refreshWrapper, BorderLayout.NORTH);
        rightPanel.setBorder(new EmptyBorder(0, 0, 0, 10));

        chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chipRow.setBorder(new EmptyBorder(8, 0, 0, 0));
        chipRow.setBackground(UIAssets.getBg());
        chipRow.setVisible(false);
        chipRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftTop = new JPanel();
        leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
        leftTop.setBackground(UIAssets.getBg());
        leftTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        leftTop.add(titleBlock);
        leftTop.add(filterRow);
        leftTop.add(chipRow);

        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(UIAssets.getBg());
        left.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(leftTop, BorderLayout.NORTH);

        header.add(left, BorderLayout.WEST);
        
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private String getDefaultFilterText(String category) {
        switch (category) {
            case "Type": return "All Types";
            case "Brand": return "All Brands";
            case "Transmission": return "All";
            case "Fuel": return "All Fuels";
            case "Price": return "All Prices";
            case "Color": return "All Colors";
            default: return null;
        }
    }

    private String[] buildBrandOptions() {
        try {
            List<String> brands = pckServices.CarService.getDistinctBrands();
            if (brands == null || brands.isEmpty()) {
                return new String[]{ "All Brands", "Toyota", "Honda", "Mitsubishi", "BYD", "Ford", "Nissan", "Isuzu", "Mazda", "Kia" };
            }
            String[] options = new String[brands.size() + 1];
            options[0] = "All Brands";
            for (int i = 0; i < brands.size(); i++) {
                options[i + 1] = brands.get(i);
            }
            return options;
        } catch (Exception e) {
            System.err.println("[BrowseCarsGUI] Failed to load brands: " + e.getMessage());
            return new String[]{ "All Brands", "Toyota", "Honda", "Mitsubishi", "BYD", "Ford", "Nissan", "Isuzu", "Mazda", "Kia" };
        }
    }

    private String[] buildColorOptions() {
        try {
            List<String> colors = pckServices.CarService.getDistinctColors();
            if (colors == null || colors.isEmpty()) {
                return new String[]{ "All Colors" };
            }
            String[] options = new String[colors.size() + 1];
            options[0] = "All Colors";
            for (int i = 0; i < colors.size(); i++) {
                options[i + 1] = colors.get(i);
            }
            return options;
        } catch (Exception e) {
            System.err.println("[BrowseCarsGUI] Failed to load colors: " + e.getMessage());
            return new String[]{ "All Colors" };
        }
    }

    private javax.swing.JButton findPillButton(JPanel container) {
        if (container.getComponentCount() > 0 && container.getComponent(0) instanceof JButton) {
            return (JButton) container.getComponent(0);
        }
        return null;
    }

    private javax.swing.JPanel buildFilterCombo(String label, String[] options, java.util.function.Consumer<String> onSelect) {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.setOpaque(false);

        JButton pillBtn = new JButton(options[0] + "  ▼") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 17, 17);
                g2.setColor(UIAssets.getBorder());
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 17, 17);
                g2.dispose();
                g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(getFont());
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getAscent();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2 - 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        pillBtn.setFont(UIAssets.FONT_SMALL);
        pillBtn.setForeground(UIAssets.getTextSecondary());
        pillBtn.setBackground(UIAssets.getSurface());
        pillBtn.setBorderPainted(false);
        pillBtn.setContentAreaFilled(false);
        pillBtn.setFocusPainted(false);
        pillBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        pillBtn.setPreferredSize(new Dimension(135, 34));

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(UIAssets.getBorder(), 1));
        popup.setBackground(UIAssets.getSurface());

        for (String option : options) {
            JMenuItem item = new JMenuItem(option) {
                @Override protected void paintComponent(Graphics g) {
                    if (getModel().isArmed() || getModel().isSelected()) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(UIAssets.CLR_BLUE_LIGHT);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.dispose();
                    }
                    super.paintComponent(g);
                }
            };
            item.setFont(UIAssets.FONT_SMALL);
            item.setForeground(UIAssets.getTextPrimary());
            item.setBackground(UIAssets.getSurface());
            item.setOpaque(true);
            item.addActionListener(e -> {
                pillBtn.setText(option + "  ▼");
                onSelect.accept(option);
                popup.setVisible(false);
            });
            popup.add(item);
        }

        pillBtn.addActionListener(e -> {
            Dimension popupSize = new Dimension(pillBtn.getWidth(), options.length * 28);
            popup.setPopupSize(popupSize);
            popup.show(pillBtn, 0, pillBtn.getHeight());
        });

        container.add(pillBtn, BorderLayout.CENTER);
        return container;
    }

    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getBg());
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.getInputBg());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.setColor(UIAssets.getBorder());
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
        searchField.setFont(UIAssets.FONT_BODY);
        searchField.setForeground(UIAssets.getTextSecondary());
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setText("Search cars...");

        searchField.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (searchField.getText().startsWith("Search")) {
                    searchField.setText("");
                    searchField.setForeground(UIAssets.getTextPrimary());
                }
            }
            @Override public void focusLost(FocusEvent e) {
                if (searchField.getText().isBlank()) {
                    searchField.setText("Search cars...");
                    searchField.setForeground(UIAssets.getTextSecondary());
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { runSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { runSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { runSearch(); }
            private void runSearch() {
                String text = searchField.getText().trim();
                if (text.isEmpty() || text.equals("Search cars...")) {
                    populateGrid(allCars);
                    closeDetailIfOpen();
                    return;
                }
                filterCards(text);
            }
        });
        wrapper.add(icon, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane buildCardsArea() {
        cardGrid = new JPanel(new GridLayout(0, 5, 16, 16));
        cardGrid.setBackground(UIAssets.getBg());
        cardGrid.setOpaque(true);
        cardGrid.setBorder(new EmptyBorder(4, 0, 4, 0));
        populateGrid(allCars);

        // Scroll cardGrid directly — eliminates the empty wrapper region that caused
        // ghost images. BACKINGSTORE_SCROLL_MODE keeps an off-screen copy of the full
        // scroll content and repaints every newly-revealed pixel from it, so no stale
        // image pixels ever appear at the top or bottom when scrolling.
        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setViewportBorder(null);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(UIAssets.getBg());
        scroll.setOpaque(true);
        scroll.setBackground(UIAssets.getBg());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
        return scroll;
    }

    private void populateGrid(List<Car> cars) {
        cardGrid.removeAll();
        if (cars.isEmpty()) {
            cardGrid.setLayout(new BorderLayout());
            JLabel empty = new JLabel("No cars found.", SwingConstants.CENTER);
            empty.setFont(UIAssets.FONT_SUBTITLE);
            empty.setForeground(UIAssets.getTextSecondary());
            cardGrid.add(empty, BorderLayout.CENTER);
        } else {
            cardGrid.setLayout(new GridLayout(0, 5, 12, 12));
            for (Car car : cars) cardGrid.add(buildCarCard(car));
        }
        resultsCountLabel.setText(cars.size() + " cars found");
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JPanel buildCarCard(Car car) {
        final JPanel[] cardRef = new JPanel[1];

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.getBg());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(3, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                g2.setColor(UIAssets.getCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hovered  = Boolean.TRUE.equals(getClientProperty("hovered"));
                boolean selected = (cardRef[0] == selectedCardPanel);
                g2.setColor(selected || hovered ? UIAssets.CLR_BLUE : UIAssets.getBorder());
                g2.setStroke(new BasicStroke(selected || hovered ? 2f : 1f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);
                g2.dispose();
            }
        };
        cardRef[0] = card;

        card.setOpaque(true);
        card.setBackground(UIAssets.getCard());
        card.setBorder(new EmptyBorder(4, 4, 6, 4));
        card.setPreferredSize(new Dimension(0, 280));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { card.putClientProperty("hovered", true);  card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.putClientProperty("hovered", false); card.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                if (selectedCardPanel != null) selectedCardPanel.repaint();
                selectedCardPanel = card;
                card.repaint();
                openDetail(car);
            }
        });

        final BufferedImage cardImg  = loadImageRaw(car.getImagePath());
        final Color         carColor = parseColor(car.getColor());

        JPanel imgArea = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setColor(UIAssets.getBgSecondary());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (cardImg != null) {
                    int pw = getWidth(), ph = 140;
                    int iw = cardImg.getWidth(), ih = cardImg.getHeight();
                    double scale = Math.max((double) pw / iw, (double) ph / ih);
                    int drawW = (int)(iw * scale), drawH = (int)(ih * scale);
                    java.awt.geom.RoundRectangle2D clip = new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), 140, 12, 12);
                    g2.setClip(clip);
                    g2.drawImage(cardImg, (pw - drawW) / 2, (ph - drawH) / 2, drawW, drawH, null);
                }
                g2.dispose();
            }
        };
        imgArea.setOpaque(false);
        imgArea.setPreferredSize(new Dimension(0, 140));
        if (cardImg == null) imgArea.add(buildCarEmoji(car.getCategory()), BorderLayout.CENTER);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(UIAssets.getCard());
        info.setOpaque(true);
        info.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel nameLbl = new JLabel(car.getBrand() + " " + car.getModel());
        nameLbl.setFont(UIAssets.FONT_H3);
        nameLbl.setForeground(UIAssets.getTextPrimary());
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(2));

        JLabel metaLbl = new JLabel(car.getYear() + "  \u00B7  " + car.getCategory());
        metaLbl.setFont(UIAssets.FONT_SMALL);
        metaLbl.setForeground(UIAssets.getTextSecondary());
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(metaLbl);
        info.add(Box.createVerticalStrut(2));

        JLabel specLbl = new JLabel(car.getTransmission() + "  \u00B7  " + car.getFuelType() + "  \u00B7  " + car.getSeatCapacity() + " seats");
        specLbl.setFont(UIAssets.FONT_SMALL);
        specLbl.setForeground(UIAssets.getTextSecondary());
        specLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(specLbl);
        info.add(Box.createVerticalStrut(6));

        String colorName = (car.getColor() != null && !car.getColor().isBlank()) ? car.getColor() : "Unknown";
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        colorRow.setBackground(UIAssets.getCard());
        colorRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel swatch = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(carColor);
                g2.fillOval(0, 0, 10, 10);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, 9, 9);
                g2.dispose();
            }
        };
        swatch.setPreferredSize(new Dimension(10, 10));
        swatch.setOpaque(false);

        JLabel colorLbl = new JLabel(" " + colorName);
        colorLbl.setFont(UIAssets.FONT_SMALL);
        colorLbl.setForeground(UIAssets.getTextSecondary());
        colorRow.add(swatch);
        colorRow.add(colorLbl);
        info.add(colorRow);
        info.add(Box.createVerticalStrut(6));

        JPanel div = new JPanel();
        div.setBackground(UIAssets.getBorder());
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        div.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(div);
        info.add(Box.createVerticalStrut(6));

        JPanel priceRow = new JPanel(new BorderLayout());
        priceRow.setBackground(UIAssets.getCard());
        priceRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceLbl = new JLabel("\u20B1" + String.format("%,.0f", car.getDailyRate().doubleValue()));
        priceLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        priceLbl.setForeground(UIAssets.CLR_BLUE);

        JLabel perDayLbl = new JLabel("/day");
        perDayLbl.setFont(UIAssets.FONT_SMALL);
        perDayLbl.setForeground(UIAssets.getTextSecondary());

        priceRow.add(priceLbl,  BorderLayout.WEST);
        priceRow.add(perDayLbl, BorderLayout.EAST);
        info.add(priceRow);

        card.add(imgArea, BorderLayout.CENTER);
        card.add(info,    BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildDetailShell() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIAssets.getSurface());
        panel.setBorder(new MatteBorder(0, 1, 0, 0, UIAssets.getBorder()));
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
        if (selectedCardPanel != null) { selectedCardPanel.repaint(); selectedCardPanel = null; }
        animateDetail(false);
    }

    private void animateDetail(boolean open) {
        int[] current = { detailPanel.getPreferredSize().width };
        int target = open ? DETAIL_WIDTH : 0;
        Timer t = new Timer(SLIDE_DELAY, null);
        t.addActionListener(e -> {
            current[0] = open ? Math.min(current[0] + SLIDE_STEP, target) : Math.max(current[0] - SLIDE_STEP, target);
            detailPanel.setPreferredSize(new Dimension(current[0], 0));
            bodyPanel.revalidate();
            if (current[0] == target) {
                t.stop();
                if (!open) { detailPanel.setVisible(false); detailVisible = false; selectedCar = null; }
            }
        });
        t.start();
    }

    private JPanel buildDetailContent(Car car) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIAssets.getSurface());

        final BufferedImage detailImg = loadImageRaw(car.getImagePath());

        JPanel imgArea = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_ON);
                if (detailImg != null) {
                    int pw = getWidth(), ph = getHeight();
                    int iw = detailImg.getWidth(), ih = detailImg.getHeight();
                    double scale = Math.max((double) pw / iw, (double) ph / ih);
                    int drawW = (int)(iw * scale), drawH = (int)(ih * scale);
                    g2.drawImage(detailImg, (pw - drawW) / 2, (ph - drawH) / 2, drawW, drawH, null);
                } else {
                    g2.setColor(UIAssets.getBgSecondary());
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        imgArea.setOpaque(true);
        imgArea.setPreferredSize(new Dimension(DETAIL_WIDTH, 180));
        if (detailImg == null) imgArea.add(buildCarEmoji(car.getCategory()), BorderLayout.CENTER);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(8, 8, 0, 8));

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(buildStatusBadge(car.getStatus()));
        topBar.add(badgeWrap, BorderLayout.WEST);

        JButton closeBtn = new JButton("\u2190  Back") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        closeBtn.setForeground(UIAssets.getTextSecondary());
        closeBtn.setBackground(UIAssets.getBgSecondary());
        closeBtn.setPreferredSize(new Dimension(90, 28));
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { closeBtn.setBackground(UIAssets.getBorder()); }
            @Override public void mouseExited(MouseEvent e)  { closeBtn.setBackground(UIAssets.getBgSecondary()); }
        });
        closeBtn.addActionListener(e -> closeDetail());
        topBar.add(closeBtn, BorderLayout.EAST);
        imgArea.add(topBar, BorderLayout.NORTH);
        root.add(imgArea, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIAssets.getSurface());
        body.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel nameHead = new JLabel(car.getBrand() + " " + car.getModel());
        nameHead.setFont(UIAssets.FONT_H1);
        nameHead.setForeground(UIAssets.getTextPrimary());
        nameHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(nameHead);
        body.add(Box.createVerticalStrut(2));

        JLabel yearCat = new JLabel(car.getYear() + "  \u00B7  " + car.getCategory());
        yearCat.setFont(UIAssets.FONT_SMALL);
        yearCat.setForeground(UIAssets.getTextSecondary());
        yearCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(yearCat);
        body.add(Box.createVerticalStrut(16));

        body.add(buildSectionHeader("Specifications"));
        body.add(Box.createVerticalStrut(8));
        body.add(buildSpecRow("Plate Number",  car.getPlateNumber()));
        body.add(buildSpecRow("Transmission",  car.getTransmission()));
        body.add(buildSpecRow("Fuel Type",     car.getFuelType()));
        body.add(buildSpecRow("Seat Capacity", car.getSeatCapacity() + " seats"));
        body.add(buildSpecRow("Category",      car.getCategory()));
        body.add(buildColorSpecRow(car.getColor()));
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
        totalRow.setBackground(UIAssets.CLR_BLUE_LIGHT);
        totalRow.setBorder(new EmptyBorder(8, 10, 8, 10));
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel totalKey = new JLabel("Total per Day");
        totalKey.setFont(UIAssets.FONT_H3);
        totalKey.setForeground(UIAssets.CLR_BLUE);
        JLabel totalVal = new JLabel("\u20B1" + String.format("%,.2f", total));
        totalVal.setFont(UIAssets.FONT_H3);
        totalVal.setForeground(UIAssets.CLR_BLUE);
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
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rentBtn.setFont(UIAssets.FONT_BUTTON);
        rentBtn.setForeground(Color.WHITE);
        rentBtn.setBackground(available ? UIAssets.CLR_GREEN : UIAssets.getTextSecondary());
        rentBtn.setEnabled(available);
        rentBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        rentBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        rentBtn.setBorderPainted(false);
        rentBtn.setContentAreaFilled(false);
        rentBtn.setFocusPainted(false);
        rentBtn.setCursor(available ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
        if (available) {
            rentBtn.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { rentBtn.setBackground(UIAssets.CLR_GREEN.darker()); }
                @Override public void mouseExited(MouseEvent e)  { rentBtn.setBackground(UIAssets.CLR_GREEN); }
            });
            rentBtn.addActionListener(e -> { if (onRentNow != null) onRentNow.run(); });
        }
        body.add(rentBtn);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setViewportBorder(null);
        bodyScroll.getViewport().setBackground(UIAssets.getSurface());
        bodyScroll.getVerticalScrollBar().setUnitIncrement(12);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        root.add(bodyScroll, BorderLayout.CENTER);
        return root;
    }

    private JLabel buildSectionHeader(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(UIAssets.getTextSecondary());
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildSpecRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UIAssets.getSurface());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()));
        JLabel keyLbl = new JLabel(key);   keyLbl.setFont(UIAssets.FONT_SMALL); keyLbl.setForeground(UIAssets.getTextSecondary());
        JLabel valLbl = new JLabel(value); valLbl.setFont(UIAssets.FONT_H3);    valLbl.setForeground(UIAssets.getTextPrimary());
        row.add(keyLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel buildColorSpecRow(String colorName) {
        String label  = (colorName != null && !colorName.isBlank()) ? colorName : "Unknown";
        Color  swatch = parseColor(colorName);

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UIAssets.getSurface());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new MatteBorder(0, 0, 1, 0, UIAssets.getBorder()));

        JLabel keyLbl = new JLabel("Color");
        keyLbl.setFont(UIAssets.FONT_SMALL);
        keyLbl.setForeground(UIAssets.getTextSecondary());

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        valuePanel.setBackground(UIAssets.getSurface());

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(swatch);
                g2.fillOval(0, 0, 12, 12);
                g2.setColor(new Color(0, 0, 0, 40));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, 11, 11);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setOpaque(false);

        JLabel valLbl = new JLabel(label);
        valLbl.setFont(UIAssets.FONT_H3);
        valLbl.setForeground(UIAssets.getTextPrimary());

        valuePanel.add(dot);
        valuePanel.add(valLbl);
        row.add(keyLbl,     BorderLayout.WEST);
        row.add(valuePanel, BorderLayout.EAST);
        return row;
    }

    private JPanel buildStatusBadge(String status) {
        Color bg, fg;
        String text;
        switch (status) {
            case "AVAILABLE"   -> { bg = UIAssets.CLR_GREEN_LIGHT; fg = UIAssets.CLR_GREEN; text = "\u25CF Available";    }
            case "RENTED"      -> { bg = UIAssets.CLR_RED_LIGHT;   fg = UIAssets.CLR_RED;   text = "\u25CF Rented Out";   }
            case "MAINTENANCE" -> { bg = new Color(229,231,235); fg = new Color(107,114,128); text = "\u25CF Unavailable"; }
            default            -> { bg = new Color(229,231,235); fg = UIAssets.getTextSecondary(); text = "\u25CF Unknown"; }
        }
        JPanel badge = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
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

    private BufferedImage loadImageRaw(String filename) {
        if (filename == null || filename.isBlank()) return null;
        java.io.File f = new java.io.File("assets/images/" + filename);
        if (!f.exists()) {
            System.err.println("[BrowseCarsGUI] Image not found: " + f.getAbsolutePath());
            return null;
        }
        try {
            BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img != null) return img;
            // Fallback to Toolkit for formats ImageIO cannot handle (e.g. WebP)
            java.awt.Image toolkit = java.awt.Toolkit.getDefaultToolkit().createImage(f.getAbsolutePath());
            java.awt.MediaTracker mt = new java.awt.MediaTracker(this);
            mt.addImage(toolkit, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("[BrowseCarsGUI] Image load interrupted for: " + filename);
                return null;
            }
            if (toolkit.getWidth(null) <= 0) return null;
            BufferedImage buf = new BufferedImage(toolkit.getWidth(null), toolkit.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            buf.getGraphics().drawImage(toolkit, 0, 0, null);
            return buf;
        } catch (java.io.IOException e) {
            System.err.println("[BrowseCarsGUI] Failed to read image '" + filename + "': " + e.getMessage());
            return null;
        }
    }

    private Color parseColor(String colorName) {
        if (colorName == null || colorName.isBlank()) return new Color(180, 180, 180);
        return switch (colorName.trim().toLowerCase()) {
            case "white", "pearl white", "solid white"          -> new Color(245, 245, 245);
            case "black", "midnight black", "jet black"         -> new Color(28,  28,  28);
            case "silver", "silver metallic", "granite silver"  -> new Color(192, 192, 192);
            case "gray", "grey", "charcoal", "dark gray"        -> new Color(108, 108, 108);
            case "red", "crimson red", "passion red"            -> new Color(200, 30,  30);
            case "blue", "navy blue", "azure blue", "dark blue" -> new Color(30,  80,  180);
            case "light blue", "sky blue"                       -> new Color(100, 160, 220);
            case "green", "dark green", "forest green"          -> new Color(34,  120, 60);
            case "olive", "olive green"                         -> new Color(107, 120, 50);
            case "yellow", "bright yellow"                      -> new Color(230, 190, 20);
            case "gold", "golden"                               -> new Color(210, 170, 50);
            case "orange"                                       -> new Color(220, 100, 30);
            case "brown", "bronze"                              -> new Color(140, 80,  40);
            case "beige", "cream", "champagne"                  -> new Color(220, 205, 175);
            case "purple", "violet"                             -> new Color(110, 60,  160);
            case "maroon", "burgundy", "dark red"               -> new Color(120, 20,  40);
            case "pink"                                         -> new Color(220, 130, 150);
            default                                             -> new Color(150, 150, 150);
        };
    }

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

    public void filterByType(String type)              { activeFilters.put("Type",         type);     applyFilters(); }
    public void filterByBrand(String brand)            { activeFilters.put("Brand",        brand);    applyFilters(); }
    public void filterByPriceRange(String range)       { activeFilters.put("Price",        range);    applyFilters(); }
    public void filterByTransmission(String t)         { activeFilters.put("Transmission", t);        applyFilters(); }
    public void filterByFuelType(String fuelType)      { activeFilters.put("Fuel Type",    fuelType); applyFilters(); }
    public void filterByColor(String color)              { activeFilters.put("Color",        color);     applyFilters(); }
    public void showAll()                              { activeFilters.clear();                       applyFilters(); }
    public String getActiveFilterForCategory(String c) { return activeFilters.get(c);                               }
    public String getActiveFilterValue()               { return activeFilters.isEmpty() ? null : activeFilters.values().iterator().next(); }

    private void applyFilters() {
        List<Car> filtered = allCars.stream().filter(c -> {
            for (java.util.Map.Entry<String, String> f : activeFilters.entrySet()) {
                boolean match = switch (f.getKey()) {
                    case "Type"         -> c.getCategory().equalsIgnoreCase(f.getValue());
                    case "Brand"        -> c.getBrand().equalsIgnoreCase(f.getValue());
                    case "Price"        -> matchesPriceRange(c.getDailyRate(), f.getValue());
                    case "Transmission" -> c.getTransmission().equalsIgnoreCase(f.getValue());
                    case "Fuel Type"    -> c.getFuelType() != null && c.getFuelType().equalsIgnoreCase(f.getValue());
                    case "Color"        -> c.getColor() != null && c.getColor().equalsIgnoreCase(f.getValue());
                    default             -> true;
                };
                if (!match) return false;
            }
            return true;
        }).toList();
        rebuildChipRow();
        populateGrid(filtered);
        closeDetailIfOpen();
    }

    private void rebuildChipRow() {
        chipRow.removeAll();
        if (activeFilters.isEmpty()) {
            chipRow.setVisible(false);
            return;
        }
        chipRow.setVisible(true);
        
        for (java.util.Map.Entry<String, String> entry : activeFilters.entrySet()) {
            JPanel chip = buildChip(entry.getKey(), entry.getValue());
            chipRow.add(chip);
        }
        
        if (activeFilters.size() >= 1) {
            JButton clearAllBtn = new JButton("Clear All  ×") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            clearAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            clearAllBtn.setForeground(UIAssets.CLR_RED);
            clearAllBtn.setBackground(UIAssets.CLR_RED_LIGHT);
            clearAllBtn.setBorderPainted(false);
            clearAllBtn.setContentAreaFilled(false);
            clearAllBtn.setFocusPainted(false);
            clearAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            clearAllBtn.setPreferredSize(new Dimension(95, 26));
            clearAllBtn.addActionListener(e -> {
                for (java.util.Map.Entry<String, JButton> entry : filterPillButtons.entrySet()) {
                    JButton btn = entry.getValue();
                    String category = entry.getKey();
                    String defaultText = getDefaultFilterText(category);
                    if (btn != null && defaultText != null) {
                        btn.setText(defaultText + "  \u25BC");
                    }
                }
                activeFilters.clear();
                applyFilters();
            });
            chipRow.add(clearAllBtn);
        }
        
        chipRow.revalidate();
        chipRow.repaint();
    }

    private JPanel buildChip(String category, String value) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setOpaque(true);
        chip.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(UIAssets.CLR_BLUE, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        JLabel lbl = new JLabel(category + ": " + value);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(UIAssets.CLR_BLUE);
        JLabel x = new JLabel("\u00D7");
        x.setFont(new Font("Segoe UI", Font.BOLD, 11));
        x.setForeground(UIAssets.CLR_BLUE);
        x.setCursor(new Cursor(Cursor.HAND_CURSOR));
        x.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { activeFilters.remove(category); applyFilters(); }
        });
        chip.add(lbl);
        chip.add(x);
        return chip;
    }

    private void filterCards(String query) {
        if (query.isBlank()) { populateGrid(allCars); return; }
        String[] tokens = query.toLowerCase().trim().split("\\s+");
        List<Car> filtered = allCars.stream().filter(c -> {
            String haystack = String.join(" ",
                c.getBrand(), c.getModel(), String.valueOf(c.getYear()), c.getCategory(), c.getTransmission(),
                c.getFuelType() != null ? c.getFuelType() : "",
                c.getColor()    != null ? c.getColor()    : "",
                String.valueOf(c.getSeatCapacity()), c.getDailyRate().toPlainString()
            ).toLowerCase();
            for (String token : tokens) if (!haystack.contains(token)) return false;
            return true;
        }).toList();
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

    private void closeDetailIfOpen() { if (detailVisible) closeDetail(); }

    public void refresh() { loadCars(); populateGrid(allCars); closeDetailIfOpen(); }

    public void updateResultsCount(int count) { resultsCountLabel.setText(count + " cars found"); }
}