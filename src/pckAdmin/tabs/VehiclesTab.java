package pckAdmin.tabs;

import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckModels.Car;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * VehiclesTab.java
 * ─────────────────────────────────────────────────────────────
 * Vehicles sub-tab inside ManagementPanel.
 *
 * Toolbar:
 *   [+ Add]  [✎ Edit*]  [✕ Remove*]     [Search...]    [Cards][List]
 *   * Edit and Remove start disabled — enabled when a vehicle is selected.
 *
 * Refresh is handled globally by ManagementPanel — no per-tab button.
 */
public class VehiclesTab extends JPanel {

    public interface VehicleNavCallback {
        void goToAdd();
        void goToEdit(Car car);
        void goToRemove(Car car);
    }

    private static final String CARD_VIEW = "CARDS";
    private static final String LIST_VIEW = "LIST";

    private final CarDAO             carDAO;
    private final VehicleNavCallback navCallback;
    private       List<Car>          cars;

    private Car    selectedCar       = null;
    private JPanel selectedCardPanel = null;

    private JButton editBtn;
    private JButton removeBtn;

    private JPanel     viewContainer;
    private CardLayout viewCard;
    private JPanel     cardGrid;
    private JTable     listTable;
    private JTextField searchField;
    private JButton    cardViewBtn;
    private JButton    listViewBtn;
    private String     currentView = CARD_VIEW;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public VehiclesTab(VehicleNavCallback navCallback) {
        this.navCallback = navCallback;
        this.carDAO      = new CarDAO();
        setLayout(new BorderLayout(0, 0));
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(24, 28, 24, 28));
        loadData();
        build();
    }

    // =========================================================
    //  DATA
    // =========================================================
    private void loadData() {
        cars = carDAO.getAllCars();
    }

    /** Called by ManagementPanel's global Refresh button */
    public void refresh() {
        clearSelection();
        loadData();
        rebuildViews();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildViews(),   BorderLayout.CENTER);
    }

    private void rebuildViews() {
        if (viewContainer != null) remove(viewContainer);
        add(buildViews(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // ── Toolbar ───────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, 0, 18, 0));

        // ── Left: Search (with icon) + Cards/List toggle ──────
        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftGroup.setOpaque(false);

        searchField = AdminUIHelper.buildTextField("Search vehicles...");
        searchField.setPreferredSize(new Dimension(260, 40));
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applySearch(); }
        });
        leftGroup.add(buildSearchWithIcon(searchField));

        cardViewBtn = buildViewToggleBtn("Cards", true);
        listViewBtn = buildViewToggleBtn("List",  false);
        cardViewBtn.addActionListener(e -> switchView(CARD_VIEW));
        listViewBtn.addActionListener(e -> switchView(LIST_VIEW));
        leftGroup.add(cardViewBtn);
        leftGroup.add(listViewBtn);

        // ── Right: Add / Edit / Remove ────────────────────────
        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        rightBtns.setOpaque(false);

        JButton addBtn = AdminUIHelper.buildSolidButtonWithIcon(
                "Add", AppConfig.ICON_ADD, UIAssets.CLR_BLUE);
        addBtn.addActionListener(e -> { if (navCallback != null) navCallback.goToAdd(); });

        editBtn = AdminUIHelper.buildSolidButtonWithIcon(
                "Edit", AppConfig.ICON_EDIT, UIAssets.CLR_GREEN);
        editBtn.setEnabled(false);
        editBtn.addActionListener(e -> {
            if (selectedCar != null && navCallback != null) navCallback.goToEdit(selectedCar);
        });

        removeBtn = AdminUIHelper.buildSolidButtonWithIcon(
                "Remove", AppConfig.ICON_DELETE, UIAssets.CLR_RED);
        removeBtn.setEnabled(false);
        removeBtn.addActionListener(e -> {
            if (selectedCar != null && navCallback != null) navCallback.goToRemove(selectedCar);
        });

        rightBtns.add(addBtn);
        rightBtns.add(editBtn);
        rightBtns.add(removeBtn);

        bar.add(leftGroup, BorderLayout.WEST);
        bar.add(rightBtns, BorderLayout.EAST);
        return bar;
    }

    /**
     * Wraps a JTextField in a panel that draws a leading search icon
     * inside the left inset. The wrapper paints its own rounded border
     * so the field's own border is stripped — looks like one seamless input.
     */
    private JPanel buildSearchWithIcon(JTextField field) {
        ImageIcon searchIcon = AdminUIHelper.loadIcon(
                AppConfig.ICON_SEARCH, UIAssets.getTextSecondary(), 14);

        JPanel wrap = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIAssets.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(UIAssets.getBorder());
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
            }
        };
        wrap.setOpaque(false);
        int iconW = (searchIcon != null) ? 34 : 0;
        wrap.setPreferredSize(new Dimension(
                field.getPreferredSize().width + iconW,
                field.getPreferredSize().height));

        if (searchIcon != null) {
            JLabel iconLbl = new JLabel(searchIcon);
            iconLbl.setBorder(new EmptyBorder(0, 10, 0, 2));
            wrap.add(iconLbl, BorderLayout.WEST);
        }

        // Strip the field's own border so only the wrapper border shows
        field.setBorder(new EmptyBorder(0, 4, 0, 10));
        field.setOpaque(false);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JButton buildViewToggleBtn(String label, boolean active) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                if (getBackground().equals(UIAssets.getSurface())) {
                    g2.setColor(UIAssets.getBorder());
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(UIAssets.FONT_H3);
        btn.setPreferredSize(new Dimension(68, 36));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(active ? UIAssets.CLR_BLUE : UIAssets.getSurface());
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        return btn;
    }

    // ── Content area ──────────────────────────────────────────
    private JPanel buildViews() {
        viewCard      = new CardLayout();
        viewContainer = new JPanel(viewCard);
        viewContainer.setBackground(UIAssets.getBg());
        viewContainer.add(buildCardGrid(), CARD_VIEW);
        viewContainer.add(buildListView(), LIST_VIEW);
        viewCard.show(viewContainer, currentView);
        return viewContainer;
    }

    // =========================================================
    //  CARD VIEW
    // =========================================================
    private JScrollPane buildCardGrid() {
        cardGrid = new JPanel(new GridLayout(0, 3, 16, 16));
        cardGrid.setBackground(UIAssets.getBg());
        cardGrid.setBorder(new EmptyBorder(4, 0, 4, 0));
        populateCards(cars);

        JScrollPane scroll = new JScrollPane(cardGrid);
        scroll.setBorder(null);
        scroll.setBackground(UIAssets.getBg());
        scroll.getViewport().setBackground(UIAssets.getBg());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void populateCards(List<Car> data) {
        cardGrid.removeAll();
        if (data.isEmpty()) {
            cardGrid.add(AdminUIHelper.buildEmptyState("No vehicles found."));
            cardGrid.add(new JLabel());
            cardGrid.add(new JLabel());
        } else {
            for (Car car : data) cardGrid.add(buildVehicleCard(car));
        }
        cardGrid.revalidate();
        cardGrid.repaint();
    }

    private JPanel buildVehicleCard(Car car) {
        JPanel card = new JPanel(new BorderLayout(0, 0));
        card.setBackground(UIAssets.getSurface());
        boolean isSelected = selectedCar != null && selectedCar.getCarId() == car.getCarId();
        card.setBorder(isSelected
            ? new LineBorder(UIAssets.CLR_BLUE, 2, true)
            : new LineBorder(UIAssets.getBorder(), 1, true));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isSelected) selectedCardPanel = card;

        String imgPath = car.getImagePath() != null && !car.getImagePath().isBlank()
            ? "assets/images/" + car.getImagePath()
            : AppConfig.DEFAULT_VEHICLE;
        card.add(buildCardImage(imgPath, 220, 150), BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(UIAssets.getSurface());
        info.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel nameLbl = new JLabel(car.getBrand() + " " + car.getModel());
        nameLbl.setFont(UIAssets.FONT_H2);
        nameLbl.setForeground(UIAssets.getTextPrimary());
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel metaLbl = new JLabel(car.getYear() + "  ·  " + car.getCategory());
        metaLbl.setFont(UIAssets.FONT_SMALL);
        metaLbl.setForeground(UIAssets.getTextSecondary());
        metaLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel plateLbl = new JLabel("Plate: " + car.getPlateNumber());
        plateLbl.setFont(UIAssets.FONT_SMALL);
        plateLbl.setForeground(UIAssets.getTextSecondary());
        plateLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String colorStr = car.getColor() != null ? car.getColor() + "  ·  " : "";
        JLabel specsLbl = new JLabel(
            colorStr + car.getTransmission() + "  ·  " + car.getFuelType()
            + "  ·  " + car.getSeatCapacity() + " seats");
        specsLbl.setFont(UIAssets.FONT_SMALL);
        specsLbl.setForeground(UIAssets.getTextPlaceholder());
        specsLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        info.add(nameLbl);
        info.add(Box.createVerticalStrut(4));
        info.add(metaLbl);
        info.add(Box.createVerticalStrut(2));
        info.add(plateLbl);
        info.add(Box.createVerticalStrut(6));
        info.add(specsLbl);
        info.add(Box.createVerticalStrut(12));

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(UIAssets.getBorder());
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(sep);
        info.add(Box.createVerticalStrut(10));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottom.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel rateLbl = new JLabel("\u20b1" + car.getDailyRate() + " / day");
        rateLbl.setFont(UIAssets.FONT_H2);
        rateLbl.setForeground(UIAssets.CLR_BLUE);
        bottom.add(rateLbl, BorderLayout.WEST);
        bottom.add(buildStatusBadge(car.getStatus()), BorderLayout.EAST);
        info.add(bottom);

        card.add(info, BorderLayout.CENTER);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { handleCardClick(car, card); }
            @Override public void mouseEntered(MouseEvent e) {
                if (selectedCardPanel != card)
                    card.setBorder(new LineBorder(UIAssets.CLR_BLUE, 1, true));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (selectedCardPanel != card)
                    card.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
            }
        });

        return card;
    }

    private void handleCardClick(Car car, JPanel card) {
        if (selectedCar != null && selectedCar.getCarId() == car.getCarId()) {
            clearSelection();
        } else {
            if (selectedCardPanel != null)
                selectedCardPanel.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
            selectedCar       = car;
            selectedCardPanel = card;
            card.setBorder(new LineBorder(UIAssets.CLR_BLUE, 2, true));
            setActionButtonsEnabled(true);
        }
    }

    private void clearSelection() {
        if (selectedCardPanel != null)
            selectedCardPanel.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        selectedCar       = null;
        selectedCardPanel = null;
        setActionButtonsEnabled(false);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        if (editBtn   != null) editBtn.setEnabled(enabled);
        if (removeBtn != null) removeBtn.setEnabled(enabled);
    }

    // =========================================================
    //  LIST VIEW
    // =========================================================
    private JScrollPane buildListView() {
        String[] cols = {
            "#", "Brand", "Model", "Year", "Plate",
            "Category", "Transmission", "Fuel", "Seats", "Daily Rate (\u20b1)", "Status"
        };
        listTable = AdminUIHelper.buildStyledTable(cols, null);
        populateTable(cars);

        listTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        listTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        listTable.getColumnModel().getColumn(8).setPreferredWidth(50);
        listTable.getColumnModel()
                 .getColumn(cols.length - 1)
                 .setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());

        listTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = listTable.getSelectedRow();
            if (row >= 0) {
                int carId = (int) listTable.getValueAt(row, 0);
                selectedCar = cars.stream()
                    .filter(c -> c.getCarId() == carId)
                    .findFirst().orElse(null);
                setActionButtonsEnabled(selectedCar != null);
            } else {
                selectedCar = null;
                setActionButtonsEnabled(false);
            }
        });

        return AdminUIHelper.buildTableScrollPane(listTable);
    }

    private void populateTable(List<Car> data) {
        javax.swing.table.DefaultTableModel model =
            (javax.swing.table.DefaultTableModel) listTable.getModel();
        model.setRowCount(0);
        for (Car c : data) {
            model.addRow(new Object[]{
                c.getCarId(), c.getBrand(), c.getModel(), c.getYear(),
                c.getPlateNumber(), c.getCategory(), c.getTransmission(),
                c.getFuelType(), c.getSeatCapacity(), c.getDailyRate(), c.getStatus()
            });
        }
    }

    // ── Search + view toggle ──────────────────────────────────
    private void applySearch() {
        String q = searchField.getText().trim().toLowerCase();
        List<Car> filtered = cars.stream()
            .filter(c -> q.isEmpty()
                || c.getBrand().toLowerCase().contains(q)
                || c.getModel().toLowerCase().contains(q)
                || c.getPlateNumber().toLowerCase().contains(q)
                || c.getCategory().toLowerCase().contains(q))
            .toList();
        populateCards(filtered);
        populateTable(filtered);
    }

    private void switchView(String view) {
        currentView = view;
        viewCard.show(viewContainer, view);
        boolean isCards = view.equals(CARD_VIEW);
        cardViewBtn.setBackground(isCards ? UIAssets.CLR_BLUE : UIAssets.getSurface());
        cardViewBtn.setForeground(isCards ? Color.WHITE : UIAssets.getTextSecondary());
        listViewBtn.setBackground(isCards ? UIAssets.getSurface() : UIAssets.CLR_BLUE);
        listViewBtn.setForeground(isCards ? UIAssets.getTextSecondary() : Color.WHITE);
    }

    // ── Image helpers ─────────────────────────────────────────
    private JPanel buildCardImage(String path, int w, int h) {
        return new JPanel() {
            final Image img = loadScaled(path, w, h);
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) {
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(UIAssets.getBorder());
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(UIAssets.getTextPlaceholder());
                    g.setFont(UIAssets.FONT_SMALL);
                    FontMetrics fm = g.getFontMetrics();
                    String txt = "No Image";
                    g.drawString(txt,
                        (getWidth()  - fm.stringWidth(txt)) / 2,
                        (getHeight() + fm.getAscent())       / 2);
                }
            }
            @Override public Dimension getPreferredSize() { return new Dimension(w, h); }
            @Override public Dimension getMinimumSize()   { return new Dimension(w, h); }
        };
    }

    private Image loadScaled(String path, int w, int h) {
        try {
            java.io.File f = new java.io.File(path);
            if (!f.exists()) return null;
            BufferedImage src = javax.imageio.ImageIO.read(f);
            if (src != null) return src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            // Fallback to Toolkit for formats ImageIO cannot handle (e.g. WebP)
            Image img = Toolkit.getDefaultToolkit().createImage(f.getAbsolutePath());
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(img, 0);
            try {
                mt.waitForAll();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("[VehiclesTab] Image load interrupted for: " + path);
                return null;
            }
            return (img.getWidth(null) > 0) ? img.getScaledInstance(w, h, Image.SCALE_SMOOTH) : null;
        } catch (java.io.IOException e) {
            System.err.println("[VehiclesTab] Failed to read image '" + path + "': " + e.getMessage());
            return null;
        }
    }

    private JLabel buildStatusBadge(String status) {
        Color fg, bg;
        switch (status.toUpperCase()) {
            case "AVAILABLE"   -> { fg = UIAssets.CLR_GREEN;  bg = UIAssets.CLR_GREEN_LIGHT;  }
            case "RENTED"      -> { fg = UIAssets.CLR_YELLOW; bg = UIAssets.CLR_YELLOW_LIGHT; }
            case "MAINTENANCE" -> { fg = UIAssets.CLR_RED;    bg = UIAssets.CLR_RED_LIGHT;    }
            default            -> { fg = UIAssets.getTextSecondary(); bg = UIAssets.getBorder(); }
        }
        final Color fFg = fg, fBg = bg;
        JLabel lbl = new JLabel(status, SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(UIAssets.FONT_SMALL);
        lbl.setForeground(fFg);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 10, 3, 10));
        return lbl;
    }
}
