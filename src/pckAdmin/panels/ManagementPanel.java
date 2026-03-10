package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckAdmin.tabs.CustomersTab;
import pckAdmin.tabs.DriversTab;
import pckAdmin.tabs.RentalsTab;
import pckAdmin.tabs.VehiclesTab;
import pckAdmin.tabs.VehiclesTab.VehicleNavCallback;
import pckAdmin.vehicle.AddVehiclePanel;
import pckAdmin.vehicle.EditVehiclePanel;
import pckAdmin.vehicle.RemoveVehiclePanel;
import pckModels.Car;
import pckUtils.AppConfig;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ManagementPanel.java
 * ─────────────────────────────────────────────────────────────
 * Host panel for the Management section of AdminDashboardGUI.
 *
 * Layout:
 *   ┌─ header row ─────────────────────── [🔄 Refresh] ──────┐
 *   │  Management   "Manage vehicles, drivers…"               │
 *   ├─ sub-tab pills ─────────────────────────────────────────┤
 *   │  [Vehicles]  [Drivers]  [Customers]  [Rentals]          │
 *   ├─────────────────────────────────────────────────────────┤
 *   │  ContentArea (CardLayout)                               │
 *   │    VEHICLES       → VehiclesTab                         │
 *   │    DRIVERS        → DriversTab                          │
 *   │    CUSTOMERS      → CustomersTab                        │
 *   │    RENTALS        → RentalsTab                          │
 *   │    ADD_VEHICLE    → AddVehiclePanel                     │
 *   │    EDIT_VEHICLE   → EditVehiclePanel                    │
 *   │    REMOVE_VEHICLE → RemoveVehiclePanel                  │
 *   └─────────────────────────────────────────────────────────┘
 *
 * Global Refresh button (top-right of header):
 *   Uses AppConfig.ICON_REFRESH via buildIconOnlyButton — no Unicode
 *   glyph, so no rectangle-box rendering issue on any OS.
 *   Hidden automatically on Add / Edit / Remove vehicle pages.
 *
 * Each tab no longer has its own per-tab refresh button; all
 * refreshing flows through refreshActiveTab() here.
 */
public class ManagementPanel extends JPanel {

    // ── Card keys ─────────────────────────────────────────────
    private static final String TAB_VEHICLES      = "VEHICLES";
    private static final String TAB_DRIVERS       = "DRIVERS";
    private static final String TAB_CUSTOMERS     = "CUSTOMERS";
    private static final String TAB_RENTALS       = "RENTALS";
    private static final String PAGE_ADD_VEHICLE  = "ADD_VEHICLE";
    private static final String PAGE_EDIT_VEHICLE = "EDIT_VEHICLE";
    private static final String PAGE_REM_VEHICLE  = "REMOVE_VEHICLE";

    private static final String[] TAB_KEYS   =
        { TAB_VEHICLES, TAB_DRIVERS, TAB_CUSTOMERS, TAB_RENTALS };
    private static final String[] TAB_LABELS =
        { "Vehicles", "Drivers", "Customers", "Rentals" };

    // ── Component refs ────────────────────────────────────────
    private final AdminNavCallback nav;
    private JButton[]  tabBtns;
    private JPanel     contentArea;
    private CardLayout cardLayout;
    private JButton    refreshBtn;
    private int        activeTab   = 0;
    private String     currentCard = TAB_VEHICLES;

    // Tab instances
    private VehiclesTab  vehiclesTab;
    private DriversTab   driversTab;
    private CustomersTab customersTab;
    private RentalsTab   rentalsTab;

    // Vehicle CRUD page instances
    private AddVehiclePanel    addVehiclePanel;
    private EditVehiclePanel   editVehiclePanel;
    private RemoveVehiclePanel removeVehiclePanel;

    // =========================================================
    //  CONSTRUCTOR
    // =========================================================
    public ManagementPanel(AdminNavCallback nav) {
        this.nav = nav;
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
    }

    // =========================================================
    //  BUILD
    // =========================================================
    private void build() {
        add(buildTop(),     BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Top — header row + pills + divider ────────────────────
    private JPanel buildTop() {
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        top.add(buildHeaderRow());
        top.add(Box.createVerticalStrut(20));
        top.add(buildPillRow());
        top.add(Box.createVerticalStrut(4));

        JSeparator div = AdminUIHelper.buildDivider();
        div.setAlignmentX(Component.LEFT_ALIGNMENT);
        div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        top.add(div);

        return top;
    }

    /**
     * Header row — page title left, Refresh icon button right.
     * Uses buildIconOnlyButton with AppConfig.ICON_REFRESH so the
     * icon renders from the actual PNG — no Unicode glyph involved.
     */
    private JPanel buildHeaderRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JPanel header = AdminUIHelper.buildPageHeader(
            "Management",
            "Manage vehicles, drivers, customers, and rental records"
        );
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Global refresh — proper PNG icon, no Unicode box ──
        refreshBtn = AdminUIHelper.buildIconOnlyButton(
                AppConfig.ICON_REFRESH, UIAssets.CLR_BLUE, "Refresh current tab");
        refreshBtn.addActionListener(e -> refreshActiveTab());

        row.add(header,     BorderLayout.WEST);
        row.add(refreshBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildPillRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        tabBtns = new JButton[TAB_KEYS.length];
        for (int i = 0; i < TAB_KEYS.length; i++) {
            final int idx = i;
            tabBtns[i] = buildPillButton(TAB_LABELS[i], i == 0);
            tabBtns[i].addActionListener(e -> selectTab(idx));
            row.add(tabBtns[i]);
        }
        return row;
    }

    // ── Content area ──────────────────────────────────────────
    private JPanel buildContent() {
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIAssets.getBg());

        VehicleNavCallback vehicleNav = new VehicleNavCallback() {
            @Override public void goToAdd()         { showAddVehicle();      }
            @Override public void goToEdit(Car c)   { showEditVehicle(c);   }
            @Override public void goToRemove(Car c) { showRemoveVehicle(c); }
        };

        vehiclesTab  = new VehiclesTab(vehicleNav);
        driversTab   = new DriversTab();
        customersTab = new CustomersTab();
        rentalsTab   = new RentalsTab();

        Runnable backToVehicles = () -> {
            vehiclesTab.refresh();
            showTab(TAB_VEHICLES);
        };
        addVehiclePanel    = new AddVehiclePanel(
            backToVehicles,
            () -> { vehiclesTab.refresh(); showTab(TAB_VEHICLES); });
        editVehiclePanel   = new EditVehiclePanel(
            backToVehicles,
            () -> { vehiclesTab.refresh(); showTab(TAB_VEHICLES); });
        removeVehiclePanel = new RemoveVehiclePanel(
            backToVehicles,
            () -> { vehiclesTab.refresh(); showTab(TAB_VEHICLES); });

        contentArea.add(vehiclesTab,        TAB_VEHICLES);
        contentArea.add(driversTab,         TAB_DRIVERS);
        contentArea.add(customersTab,       TAB_CUSTOMERS);
        contentArea.add(rentalsTab,         TAB_RENTALS);
        contentArea.add(addVehiclePanel,    PAGE_ADD_VEHICLE);
        contentArea.add(editVehiclePanel,   PAGE_EDIT_VEHICLE);
        contentArea.add(removeVehiclePanel, PAGE_REM_VEHICLE);

        cardLayout.show(contentArea, TAB_VEHICLES);
        return contentArea;
    }

    // =========================================================
    //  TAB SELECTION
    // =========================================================
    private void selectTab(int index) {
        activeTab = index;
        for (int i = 0; i < tabBtns.length; i++) setPillActive(tabBtns[i], i == index);
        showTab(TAB_KEYS[index]);
    }

    private void showTab(String key) {
        currentCard = key;
        cardLayout.show(contentArea, key);
        boolean isTabPage = key.equals(TAB_VEHICLES)  || key.equals(TAB_DRIVERS)
                         || key.equals(TAB_CUSTOMERS) || key.equals(TAB_RENTALS);
        refreshBtn.setVisible(isTabPage);
    }

    private void setPillActive(JButton btn, boolean active) {
        btn.setBackground(active ? UIAssets.CLR_BLUE : UIAssets.getBg());
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
    }

    // =========================================================
    //  VEHICLE CRUD NAVIGATION
    // =========================================================
    private void showAddVehicle() {
        currentCard = PAGE_ADD_VEHICLE;
        cardLayout.show(contentArea, PAGE_ADD_VEHICLE);
        refreshBtn.setVisible(false);
    }

    private void showEditVehicle(Car car) {
        editVehiclePanel.load(car);
        currentCard = PAGE_EDIT_VEHICLE;
        cardLayout.show(contentArea, PAGE_EDIT_VEHICLE);
        refreshBtn.setVisible(false);
    }

    private void showRemoveVehicle(Car car) {
        removeVehiclePanel.load(car);
        currentCard = PAGE_REM_VEHICLE;
        cardLayout.show(contentArea, PAGE_REM_VEHICLE);
        refreshBtn.setVisible(false);
    }

    // =========================================================
    //  GLOBAL REFRESH
    //  Delegates to whichever tab is currently active.
    //  Individual tabs no longer have their own refresh buttons.
    // =========================================================
    private void refreshActiveTab() {
        switch (currentCard) {
            case TAB_VEHICLES  -> vehiclesTab.refresh();
            case TAB_DRIVERS   -> driversTab.refresh();
            case TAB_CUSTOMERS -> customersTab.refresh();
            case TAB_RENTALS   -> rentalsTab.refresh();
        }
    }

    // =========================================================
    //  PILL BUTTON
    // =========================================================
    private JButton buildPillButton(String label, boolean active) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(active ? UIAssets.FONT_NAV_BOLD : UIAssets.FONT_NAV);
        btn.setForeground(active ? Color.WHITE : UIAssets.getTextSecondary());
        btn.setBackground(active ? UIAssets.CLR_BLUE : UIAssets.getBg());
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 18, 7, 18));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) {
                    btn.setBackground(UIAssets.CLR_BLUE_LIGHT);
                    btn.setForeground(UIAssets.CLR_BLUE);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!btn.getBackground().equals(UIAssets.CLR_BLUE)) {
                    btn.setBackground(UIAssets.getBg());
                    btn.setForeground(UIAssets.getTextSecondary());
                }
            }
        });
        return btn;
    }
}
