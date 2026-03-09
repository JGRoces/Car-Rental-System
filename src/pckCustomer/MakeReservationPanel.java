package pckCustomer;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import pckDatabase.CarDAO;
import pckModels.Car;
import pckUtils.SessionManager;

/**
 * MakeReservationPanel.java
 * Plugs into CustomerDashboardGUI as PANEL_KEYS[1].
 * All inputs, cards, buttons and banners use rounded corners.
 *
 * FIXES applied:
 *  1. Section header divider lines now always span full card width.
 *     Root cause: JSeparator inside BoxLayout doesn't stretch reliably.
 *     Fix: buildSectionCardParts() uses BorderLayout for the card, with
 *     the title+divider in NORTH (guaranteed full width) and a BoxLayout
 *     content panel in CENTER for form widgets.
 *  2. Blue info banner in Driver's License section fills full width.
 *  3. Reservation Summary: emoji key labels replaced with plain text to
 *     avoid rendering as boxes; summary row height increased to 26px.
 */
public class MakeReservationPanel extends JPanel {
    // NOTE:
    // This panel is already styled using the same design system as BrowseCarsGUI:
    // - Same color palette (CLR_BG/CLR_WHITE/CLR_BLACK/CLR_GRAY/CLR_BORDER/CLR_BLUE/CLR_GREEN/CLR_RED/CLR_YELLOW)
    // - Same typography (Segoe UI) and page header layout
    // - Same card language (rounded cards, soft banners, subtle borders)
    // If you want it to look EVEN closer to BrowseCarsGUI, next changes would be:
    // 1) Add a BrowseCars-like header right action button (e.g., "Refresh Cars") in the page header row
    // 2) Add a BrowseCars-like search bar (optional) for filtering the carCombo list
    // 3) Use the same rounded search-field paint style for text inputs / combo boxes

    // ─────────────────────────────────────────────
    //  Colors
    // ─────────────────────────────────────────────
    private static final Color CLR_BG         = new Color(245, 245, 245);
    private static final Color CLR_WHITE      = Color.WHITE;
    private static final Color CLR_BLACK      = new Color(18, 18, 18);
    private static final Color CLR_GRAY       = new Color(120, 120, 120);
    private static final Color CLR_BORDER     = new Color(220, 220, 220);
    private static final Color CLR_BLUE       = new Color(37, 99, 235);
    private static final Color CLR_GREEN      = new Color(22, 163, 74);
    private static final Color CLR_RED        = new Color(220, 38, 38);
    private static final Color CLR_YELLOW     = new Color(234, 179, 8);
    private static final Color CLR_FIELD_BG   = new Color(250, 250, 250);
    private static final Color CLR_SECTION_BG = new Color(248, 249, 250);
    private static final Color CLR_DETAIL_BG  = new Color(241, 245, 249);
    private static final Color CLR_BLUE_SOFT  = new Color(219, 234, 254);
    private static final Color CLR_BLUE_TEXT  = new Color(30, 58, 138);
    private static final Color CLR_YLW_SOFT   = new Color(255, 249, 195);
    private static final Color CLR_YLW_TEXT   = new Color(120, 90, 0);

    // ─────────────────────────────────────────────
    //  Fonts
    // ─────────────────────────────────────────────
    private static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SECTION  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_INPUT    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD     = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_TOTAL    = new Font("Segoe UI", Font.BOLD,  22);

    // ─────────────────────────────────────────────
    //  Corner radii
    // ─────────────────────────────────────────────
    private static final int R_CARD    = 12;
    private static final int R_INPUT   = 8;
    private static final int R_BUTTON  = 8;
    private static final int R_BANNER  = 8;
    private static final int R_CHIP    = 8;
    private static final int R_STEP    = 11;

    // ─────────────────────────────────────────────
    //  Step keys
    // ─────────────────────────────────────────────
    private static final String STEP_FORM    = "FORM";
    private static final String STEP_CONFIRM = "CONFIRM";

    // ─────────────────────────────────────────────
    //  Locations
    // ─────────────────────────────────────────────
    private static final String[] LOCATIONS = {
        "Main Office \u2014 Makati",
        "Branch \u2014 BGC",
        "Branch \u2014 Quezon City",
        "Branch \u2014 Pasig",
        "Branch \u2014 Mandaluyong"
    };

    // ─────────────────────────────────────────────
    //  Form fields
    // ─────────────────────────────────────────────
    private JComboBox<String> carCombo;
    private JComboBox<String> rentalTypeCombo;
    private JComboBox<String> pickupLocCombo;
    private JComboBox<String> returnLocCombo;
    private JSpinner          pickupDateSpinner;
    private JSpinner          returnDateSpinner;
    private JComboBox<String> pickupTimeCombo;
    private JComboBox<String> returnTimeCombo;
    private JTextField        licenseNumField;
    private JLabel            licenseFileLabel;
    private File              licenseFile = null;

    // Car detail strip labels
    private JLabel carDetailCategory;
    private JLabel carDetailTransmission;
    private JLabel carDetailSeats;
    private JLabel carDetailRate;

    // ─────────────────────────────────────────────
    //  Summary labels (right card)
    // ─────────────────────────────────────────────
    private JLabel sumCarLbl;
    private JLabel sumTypeLbl;
    private JLabel sumPickupLbl;
    private JLabel sumReturnLbl;
    private JLabel sumPickupLocLbl;
    private JLabel sumReturnLocLbl;
    private JLabel sumLicenseLbl;
    private JLabel sumDurationLbl;
    private JLabel sumRateLbl;
    private JLabel sumTotalLbl;

    // ─────────────────────────────────────────────
    //  Confirm step labels
    // ─────────────────────────────────────────────
    private JLabel confCarLbl;
    private JLabel confTypeLbl;
    private JLabel confPickupLbl;
    private JLabel confReturnLbl;
    private JLabel confPickupLocLbl;
    private JLabel confReturnLocLbl;
    private JLabel confLicenseLbl;
    private JLabel confDurationLbl;
    private JLabel confRateLbl;
    private JLabel confTotalLbl;

    // ─────────────────────────────────────────────
    //  Data
    // ─────────────────────────────────────────────
    private List<Car> availableCars;
    private CarDAO    carDAO;

    // ─────────────────────────────────────────────
    //  Layout
    // ─────────────────────────────────────────────
    private CardLayout stepLayout;
    private JPanel     stepContainer;

    // ─────────────────────────────────────────────
    //  Constructor
    // ─────────────────────────────────────────────
    public MakeReservationPanel() {
        setLayout(new BorderLayout());
        setBackground(CLR_BG);
        carDAO        = new CarDAO();
        availableCars = carDAO.getAvailableCars();
        buildUI();
    }

    // ─────────────────────────────────────────────
    //  Root UI
    // ─────────────────────────────────────────────
    private void buildUI() {
        stepLayout    = new CardLayout();
        stepContainer = new JPanel(stepLayout);
        stepContainer.setBackground(CLR_BG);

        stepContainer.add(buildFormStep(),    STEP_FORM);
        stepContainer.add(buildConfirmStep(), STEP_CONFIRM);

        stepLayout.show(stepContainer, STEP_FORM);

        JScrollPane scroll = new JScrollPane(stepContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        scroll.getVerticalScrollBar().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    // ====================================================
    //  STEP 1 — FORM
    // ====================================================
    private JPanel buildFormStep() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        root.add(buildPageHeader("Make a Reservation",
            "Fill in the details below to book a car."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.add(buildStepIndicator(1));
        body.add(Box.createVerticalStrut(20));

        JPanel cols = new JPanel(new BorderLayout(20, 0));
        cols.setBackground(CLR_BG);
        cols.setAlignmentX(Component.LEFT_ALIGNMENT);
        cols.add(buildFormLeft(),  BorderLayout.CENTER);
        cols.add(buildFormRight(), BorderLayout.EAST);

        body.add(cols);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildFormLeft() {
        JPanel left = new JPanel();
        left.setLayout(new GridLayout(0, 1, 0, 16));
        left.setBackground(CLR_BG);
        left.add(buildCarSection());
        left.add(buildScheduleSection());
        left.add(buildLocationSection());
        left.add(buildLicenseSection());
        left.add(buildFormButtons());
        return left;
    }

    // ── Car selection ──
    private JPanel buildCarSection() {
        JPanel[] parts   = buildSectionCardParts("Car Selection");
        JPanel   outer   = parts[0];
        JPanel   content = parts[1];

        JPanel topRow = new JPanel(new GridLayout(1, 2, 12, 0));
        topRow.setBackground(CLR_WHITE);

        carCombo = new JComboBox<>();
        carCombo.addItem("— Select a Car —");
        for (Car car : availableCars) carCombo.addItem(car.getDisplayName());
        styleCombo(carCombo);

        rentalTypeCombo = new JComboBox<>(new String[]{"Per Day", "Per Hour"});
        styleCombo(rentalTypeCombo);

        topRow.add(buildField("Select Car",  carCombo));
        topRow.add(buildField("Rental Type", rentalTypeCombo));
        content.add(topRow);
        content.add(Box.createVerticalStrut(12));

        JPanel detailStrip = new JPanel(new GridLayout(1, 4, 10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CLR_DETAIL_BG);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), R_CHIP * 2, R_CHIP * 2));
                g2.dispose();
            }
        };
        detailStrip.setOpaque(false);
        detailStrip.setBorder(new EmptyBorder(10, 14, 10, 14));

        carDetailCategory     = buildDetailChip(detailStrip, "Category",     "—");
        carDetailTransmission = buildDetailChip(detailStrip, "Transmission", "—");
        carDetailSeats        = buildDetailChip(detailStrip, "Seats",        "—");
        carDetailRate         = buildDetailChip(detailStrip, "Daily Rate",   "—");

        // Wrap in BorderLayout row so it always fills the full card width
        JPanel stripRow = new JPanel(new BorderLayout());
        stripRow.setOpaque(false);
        stripRow.add(detailStrip, BorderLayout.CENTER);
        content.add(stripRow);

        carCombo.addActionListener(e -> { updateCarDetails(); refreshSummary(); });
        rentalTypeCombo.addActionListener(e -> refreshSummary());

        return outer;
    }

    private JPanel buildScheduleSection() {
        JPanel[] parts   = buildSectionCardParts("Schedule");
        JPanel   outer   = parts[0];
        JPanel   content = parts[1];

        JPanel grid = new JPanel(new GridLayout(2, 2, 12, 12));
        grid.setBackground(CLR_WHITE);

        pickupDateSpinner = new JSpinner(new SpinnerDateModel());
        returnDateSpinner = new JSpinner(new SpinnerDateModel());
        pickupDateSpinner.setEditor(new JSpinner.DateEditor(pickupDateSpinner, "yyyy-MM-dd"));
        returnDateSpinner.setEditor(new JSpinner.DateEditor(returnDateSpinner, "yyyy-MM-dd"));
        styleSpinner(pickupDateSpinner);
        styleSpinner(returnDateSpinner);
        pickupDateSpinner.addChangeListener(e -> refreshSummary());
        returnDateSpinner.addChangeListener(e -> refreshSummary());

        String[] times  = buildTimeOptions();
        pickupTimeCombo = new JComboBox<>(times);
        returnTimeCombo = new JComboBox<>(times);
        returnTimeCombo.setSelectedItem("05:00 PM");
        styleCombo(pickupTimeCombo);
        styleCombo(returnTimeCombo);

        grid.add(buildField("Pick-up Date", pickupDateSpinner));
        grid.add(buildField("Return Date",  returnDateSpinner));
        grid.add(buildField("Pick-up Time", pickupTimeCombo));
        grid.add(buildField("Return Time",  returnTimeCombo));
        content.add(grid);
        return outer;
    }

    private JPanel buildLocationSection() {
        JPanel[] parts   = buildSectionCardParts("Locations");
        JPanel   outer   = parts[0];
        JPanel   content = parts[1];

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setBackground(CLR_WHITE);

        pickupLocCombo = new JComboBox<>(LOCATIONS);
        returnLocCombo = new JComboBox<>(LOCATIONS);
        styleCombo(pickupLocCombo);
        styleCombo(returnLocCombo);
        pickupLocCombo.addActionListener(e -> refreshSummary());
        returnLocCombo.addActionListener(e -> refreshSummary());

        grid.add(buildField("Pick-up Location", pickupLocCombo));
        grid.add(buildField("Return Location",  returnLocCombo));
        content.add(grid);
        return outer;
    }

    private JPanel buildLicenseSection() {
        // Built manually (not via buildSectionCardParts) so the blue banner
        // can be placed in a dedicated BorderLayout slot — guaranteed full width.
        JLabel titleLbl = new JLabel("Driver’s License Validation");
        titleLbl.setFont(FONT_SECTION);
        titleLbl.setForeground(CLR_BLACK);
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(CLR_BORDER); g.fillRect(0, 0, getWidth(), getHeight());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 1); }
            @Override public Dimension getMinimumSize()   { return new Dimension(0, 1); }
        };
        divLine.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        header.add(titleLbl, BorderLayout.CENTER);
        header.add(divLine,  BorderLayout.SOUTH);

        // Banner in BorderLayout CENTER — always stretches to full card width
        JPanel banner = new RoundedPanel(R_BANNER, CLR_BLUE_SOFT);
        banner.setLayout(new BorderLayout());
        banner.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel bannerLbl = new JLabel(
            "Your license will be verified before approval. Ensure details match exactly.");
        bannerLbl.setFont(FONT_SMALL);
        bannerLbl.setForeground(CLR_BLUE_TEXT);
        banner.add(bannerLbl, BorderLayout.CENTER);

        JPanel bannerWrap = new JPanel(new BorderLayout());
        bannerWrap.setOpaque(false);
        bannerWrap.setBorder(new EmptyBorder(0, 0, 12, 0));
        bannerWrap.add(banner, BorderLayout.CENTER);

        licenseNumField = new RoundedTextField(R_INPUT);
        licenseNumField.setFont(FONT_INPUT);
        licenseNumField.setToolTipText("e.g. N01-23-456789");

        JPanel uploadWrap = new JPanel();
        uploadWrap.setLayout(new BoxLayout(uploadWrap, BoxLayout.Y_AXIS));
        uploadWrap.setBackground(CLR_WHITE);

        JButton uploadBtn = buildOutlineButton("Upload License Photo (Front)");
        uploadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter(
                "Image Files (JPG, PNG)", "jpg", "jpeg", "png"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                licenseFile = chooser.getSelectedFile();
                licenseFileLabel.setText("\u2713 " + licenseFile.getName());
                licenseFileLabel.setForeground(CLR_GREEN);
                refreshSummary();
            }
        });

        licenseFileLabel = new JLabel("No file selected");
        licenseFileLabel.setFont(FONT_SMALL);
        licenseFileLabel.setForeground(CLR_GRAY);
        licenseFileLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        uploadWrap.add(uploadBtn);
        uploadWrap.add(Box.createVerticalStrut(4));
        uploadWrap.add(licenseFileLabel);

        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setBackground(CLR_WHITE);
        grid.add(buildField("License Number", licenseNumField));
        grid.add(buildField("License Photo",  uploadWrap));

        JPanel inner = new JPanel(new BorderLayout(0, 0));
        inner.setOpaque(false);
        inner.add(bannerWrap, BorderLayout.NORTH);
        inner.add(grid,       BorderLayout.CENTER);

        RoundedPanel outer = new RoundedPanel(R_CARD, CLR_WHITE);
        outer.setLayout(new BorderLayout());
        outer.setBorder(new EmptyBorder(16, 16, 16, 16));
        outer.add(header, BorderLayout.NORTH);
        outer.add(inner,  BorderLayout.CENTER);
        return outer;
    }

    // ── Form buttons ──
    private JPanel buildFormButtons() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setBackground(CLR_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton clearBtn = buildOutlineButton("Clear Form");
        clearBtn.addActionListener(e -> clearForm());

        JButton nextBtn = buildSolidButton("Review Reservation  \u2192", CLR_BLUE);
        nextBtn.addActionListener(e -> {
            if (validateForm()) {
                populateConfirmStep();
                stepLayout.show(stepContainer, STEP_CONFIRM);
            }
        });

        row.add(clearBtn);
        row.add(nextBtn);
        return row;
    }

    // ── RIGHT: live summary card ──
    private JPanel buildFormRight() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(CLR_BG);
        right.setPreferredSize(new Dimension(260, 0));

        // Rounded summary card
        JPanel card = new RoundedPanel(R_CARD, CLR_WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Reservation Summary");
        title.setFont(FONT_SECTION);
        title.setForeground(CLR_BLACK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(title);
        // FIX: divider inside summary card needs MAX width too
        card.add(buildDivider());
        card.add(Box.createVerticalStrut(10));

        // FIX: use plain text keys (no emoji) so labels render correctly on all systems
        sumCarLbl       = buildSummaryRow(card, "Car",        "—");
        sumTypeLbl      = buildSummaryRow(card, "Type",       "—");
        sumPickupLbl    = buildSummaryRow(card, "Pick-up",    "—");
        sumReturnLbl    = buildSummaryRow(card, "Return",     "—");
        sumPickupLocLbl = buildSummaryRow(card, "From",       "—");
        sumReturnLocLbl = buildSummaryRow(card, "To",         "—");
        sumLicenseLbl   = buildSummaryRow(card, "License",    "—");

        card.add(Box.createVerticalStrut(8));
        card.add(buildDivider());
        card.add(Box.createVerticalStrut(8));

        sumDurationLbl = buildSummaryRow(card, "Duration", "—");
        sumRateLbl     = buildSummaryRow(card, "Rate",     "—");
        card.add(Box.createVerticalStrut(10));

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JLabel totalKey = new JLabel("TOTAL");
        totalKey.setFont(new Font("Segoe UI", Font.BOLD, 11));
        totalKey.setForeground(CLR_GRAY);

        sumTotalLbl = new JLabel("—");
        sumTotalLbl.setFont(FONT_TOTAL);
        sumTotalLbl.setForeground(CLR_BLUE);
        sumTotalLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        totalRow.add(totalKey,    BorderLayout.WEST);
        totalRow.add(sumTotalLbl, BorderLayout.EAST);
        card.add(totalRow);

        right.add(card);
        return right;
    }

    // ====================================================
    //  STEP 2 — CONFIRMATION
    // ====================================================
    private JPanel buildConfirmStep() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CLR_BG);
        root.setBorder(new EmptyBorder(28, 32, 28, 32));
        root.add(buildPageHeader("Confirm Reservation",
            "Review your booking details before submitting."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(CLR_BG);
        body.add(buildStepIndicator(2));
        body.add(Box.createVerticalStrut(20));

        // Rounded confirmation card
        JPanel card = new RoundedPanel(R_CARD, CLR_WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(820, Integer.MAX_VALUE));

        JLabel cardTitle = new JLabel("Booking Details");
        cardTitle.setFont(FONT_SECTION);
        cardTitle.setForeground(CLR_BLACK);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(12));
        card.add(buildDivider());
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(0, 2, 24, 12));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        confCarLbl       = addConfirmRow(grid, "Car Selected");
        confTypeLbl      = addConfirmRow(grid, "Rental Type");
        confPickupLbl    = addConfirmRow(grid, "Pick-up Date & Time");
        confReturnLbl    = addConfirmRow(grid, "Return Date & Time");
        confPickupLocLbl = addConfirmRow(grid, "Pick-up Location");
        confReturnLocLbl = addConfirmRow(grid, "Return Location");
        confLicenseLbl   = addConfirmRow(grid, "License Number");
        confDurationLbl  = addConfirmRow(grid, "Duration");
        confRateLbl      = addConfirmRow(grid, "Rate");
        confTotalLbl     = addConfirmRow(grid, "Total Amount");

        card.add(grid);
        card.add(Box.createVerticalStrut(16));
        card.add(buildDivider());
        card.add(Box.createVerticalStrut(12));

        // Rounded terms banner — wrapped in BorderLayout row for guaranteed full width
        JPanel termsBanner = new RoundedPanel(R_BANNER, CLR_YLW_SOFT);
        termsBanner.setLayout(new BorderLayout());
        termsBanner.setBorder(new EmptyBorder(8, 12, 8, 12));
        JLabel termsLbl = new JLabel(
            "By confirming, you agree to our rental terms and conditions.");
        termsLbl.setFont(FONT_SMALL);
        termsLbl.setForeground(CLR_YLW_TEXT);
        termsBanner.add(termsLbl, BorderLayout.CENTER);
        JPanel termsRow = new JPanel(new BorderLayout());
        termsRow.setOpaque(false);
        termsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        termsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        termsRow.add(termsBanner, BorderLayout.CENTER);
        card.add(termsRow);

        body.add(card);
        body.add(Box.createVerticalStrut(20));
        body.add(buildConfirmButtons());
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private JPanel buildConfirmButtons() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row.setBackground(CLR_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JButton backBtn = buildOutlineButton("\u2190  Edit Reservation");
        backBtn.addActionListener(e -> stepLayout.show(stepContainer, STEP_FORM));

        JButton submitBtn = buildSolidButton("\u2713  Confirm & Submit", CLR_GREEN);
        submitBtn.addActionListener(e -> submitReservation());

        row.add(backBtn);
        row.add(submitBtn);
        return row;
    }

    // ====================================================
    //  PUBLIC — called from nav dropdown
    // ====================================================
    public void setRentalType(String type) {
        rentalTypeCombo.setSelectedItem(type);
        refreshSummary();
    }

    /**
     * Pre-selects a car in the reservation form.
     * Called from BrowseCarsGUI when user clicks "Rent Now".
     */
    public void setSelectedCar(Car car) {
        if (car == null) return;
        
        // Find the car's display name in the combo box
        String displayName = car.getDisplayName();
        for (int i = 0; i < carCombo.getItemCount(); i++) {
            String item = carCombo.getItemAt(i);
            if (item != null && item.equals(displayName)) {
                carCombo.setSelectedIndex(i);
                break;
            }
        }
        updateCarDetails();
        refreshSummary();
    }

    // ====================================================
    //  CAR DETAIL STRIP
    // ====================================================
    private void updateCarDetails() {
        int idx = carCombo.getSelectedIndex();
        if (idx <= 0 || idx - 1 >= availableCars.size()) {
            carDetailCategory.setText("—");
            carDetailTransmission.setText("—");
            carDetailSeats.setText("—");
            carDetailRate.setText("—");
            return;
        }
        Car car = availableCars.get(idx - 1);
        carDetailCategory.setText(car.getCategory());
        carDetailTransmission.setText(car.getTransmission());
        carDetailSeats.setText(car.getSeatCapacity() + " seats");
        carDetailRate.setText("\u20B1" + String.format("%,.2f", car.getDailyRate()) + " / day");
    }

    // ====================================================
    //  POPULATE CONFIRMATION
    // ====================================================
    private void populateConfirmStep() {
        String carText  = carCombo.getSelectedIndex() > 0
            ? (String) carCombo.getSelectedItem() : "—";
        confCarLbl.setText(carText);
        confTypeLbl.setText((String) rentalTypeCombo.getSelectedItem());
        confPickupLbl.setText(formatDate(pickupDateSpinner) + "  " + pickupTimeCombo.getSelectedItem());
        confReturnLbl.setText(formatDate(returnDateSpinner) + "  " + returnTimeCombo.getSelectedItem());
        confPickupLocLbl.setText((String) pickupLocCombo.getSelectedItem());
        confReturnLocLbl.setText((String) returnLocCombo.getSelectedItem());
        confLicenseLbl.setText(licenseNumField.getText().trim().isEmpty()
            ? "—" : licenseNumField.getText().trim());
        confDurationLbl.setText(computeDurationLabel());
        confRateLbl.setText(getSelectedCarRate());
        confTotalLbl.setText(computeTotal());
        confTotalLbl.setForeground(CLR_GREEN);
        confTotalLbl.setFont(FONT_BOLD);
    }

    // ====================================================
    //  SUBMIT
    // ====================================================
    private void submitReservation() {
        // TODO: wire to RentalDAO.insertRental()
        String name = SessionManager.isLoggedIn()
            ? SessionManager.getCurrentUser().getFullName() : "Customer";
        JOptionPane.showMessageDialog(this,
            "<html><b>Reservation submitted!</b><br><br>"
            + "Hi <b>" + name + "</b>, your booking is <b>Pending Approval</b>.<br>"
            + "An admin will review your request shortly.<br><br>"
            + "<b>Total: " + computeTotal() + "</b></html>",
            "Reservation Submitted", JOptionPane.INFORMATION_MESSAGE);
        clearForm();
        stepLayout.show(stepContainer, STEP_FORM);
    }

    // ====================================================
    //  VALIDATION
    // ====================================================
    private boolean validateForm() {
        if (carCombo.getSelectedIndex() == 0) {
            showError("Please select a car."); return false;
        }
        if (licenseNumField.getText().trim().isEmpty()) {
            showError("Please enter your driver\u2019s license number."); return false;
        }
        Date pickup = (Date) pickupDateSpinner.getValue();
        Date ret    = (Date) returnDateSpinner.getValue();
        if (!ret.after(pickup)) {
            showError("Return date must be after the pick-up date."); return false;
        }
        return true;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Missing Information",
            JOptionPane.WARNING_MESSAGE);
    }

    // ====================================================
    //  LIVE SUMMARY REFRESH
    // ====================================================
    private void refreshSummary() {
        if (sumCarLbl == null) return;
        int idx = carCombo.getSelectedIndex();
        sumCarLbl.setText(idx > 0 ? (String) carCombo.getSelectedItem() : "—");
        sumTypeLbl.setText((String) rentalTypeCombo.getSelectedItem());
        sumPickupLbl.setText(formatDate(pickupDateSpinner));
        sumReturnLbl.setText(formatDate(returnDateSpinner));
        sumPickupLocLbl.setText(shortLoc((String) pickupLocCombo.getSelectedItem()));
        sumReturnLocLbl.setText(shortLoc((String) returnLocCombo.getSelectedItem()));
        sumLicenseLbl.setText(licenseFile != null ? "\u2713 Uploaded" : "—");
        sumDurationLbl.setText(computeDurationLabel());
        sumRateLbl.setText(getSelectedCarRate());
        sumTotalLbl.setText(computeTotal());
    }

    // ====================================================
    //  CLEAR FORM
    // ====================================================
    private void clearForm() {
        carCombo.setSelectedIndex(0);
        rentalTypeCombo.setSelectedIndex(0);
        pickupLocCombo.setSelectedIndex(0);
        returnLocCombo.setSelectedIndex(0);
        pickupTimeCombo.setSelectedIndex(0);
        returnTimeCombo.setSelectedItem("05:00 PM");
        licenseNumField.setText("");
        licenseFile = null;
        licenseFileLabel.setText("No file selected");
        licenseFileLabel.setForeground(CLR_GRAY);
        carDetailCategory.setText("—");
        carDetailTransmission.setText("—");
        carDetailSeats.setText("—");
        carDetailRate.setText("—");
        refreshSummary();
    }

    // ====================================================
    //  COMPUTATION HELPERS
    // ====================================================
    private String formatDate(JSpinner spinner) {
        return new SimpleDateFormat("yyyy-MM-dd").format((Date) spinner.getValue());
    }

    private long computeDays() {
        Date pickup = (Date) pickupDateSpinner.getValue();
        Date ret    = (Date) returnDateSpinner.getValue();
        return (ret.getTime() - pickup.getTime()) / (1000L * 60 * 60 * 24);
    }

    private String computeDurationLabel() {
        long days = computeDays();
        if (days <= 0) return "—";
        String type = (String) rentalTypeCombo.getSelectedItem();
        if ("Per Hour".equals(type)) return (days * 24) + " hours";
        return days + (days == 1 ? " day" : " days");
    }

    private String getSelectedCarRate() {
        int idx = carCombo.getSelectedIndex();
        if (idx <= 0 || idx - 1 >= availableCars.size()) return "—";
        return "\u20B1" + String.format("%,.2f",
            availableCars.get(idx - 1).getDailyRate()) + " / day";
    }

    private String computeTotal() {
        int idx = carCombo.getSelectedIndex();
        if (idx <= 0 || idx - 1 >= availableCars.size()) return "—";
        long days = computeDays();
        if (days <= 0) return "—";
        BigDecimal rate  = availableCars.get(idx - 1).getDailyRate();
        BigDecimal total;
        if ("Per Hour".equals(rentalTypeCombo.getSelectedItem())) {
            BigDecimal hourly = rate.divide(BigDecimal.valueOf(24), 2, RoundingMode.HALF_UP);
            total = hourly.multiply(BigDecimal.valueOf(days * 24));
        } else {
            total = rate.multiply(BigDecimal.valueOf(days));
        }
        return "\u20B1" + String.format("%,.2f", total);
    }

    private String shortLoc(String loc) {
        if (loc == null) return "—";
        int dash = loc.indexOf('\u2014');
        return dash >= 0 ? loc.substring(dash + 1).trim() : loc;
    }

    private String[] buildTimeOptions() {
        String[] times = new String[48];
        int i = 0;
        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m += 30) {
                int h12 = h % 12 == 0 ? 12 : h % 12;
                times[i++] = String.format("%02d:%02d %s", h12, m, h < 12 ? "AM" : "PM");
            }
        return times;
    }

    // ====================================================
    //  REUSABLE UI BUILDERS
    // ====================================================

    private JPanel buildPageHeader(String title, String sub) {
        JPanel hdr = new JPanel(new GridLayout(2, 1, 0, 2));
        hdr.setBackground(CLR_BG);
        hdr.setBorder(new EmptyBorder(0, 0, 20, 0));
        JLabel t = new JLabel(title); t.setFont(FONT_TITLE);    t.setForeground(CLR_BLACK);
        JLabel s = new JLabel(sub);   s.setFont(FONT_SUBTITLE); s.setForeground(CLR_GRAY);
        hdr.add(t); hdr.add(s);
        return hdr;
    }

    private JPanel buildStepIndicator(int current) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(CLR_BG);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        String[] steps = {"Reservation Details", "Confirm & Submit"};
        for (int i = 0; i < steps.length; i++) {
            boolean active = (i + 1 == current);
            boolean done   = (i + 1 < current);

            JPanel chip = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            chip.setBackground(CLR_BG);

            Color badgeBg = done ? CLR_GREEN : active ? CLR_BLUE : CLR_BORDER;
            Color badgeFg = (done || active) ? Color.WHITE : CLR_GRAY;
            String badgeTxt = done ? "\u2713" : String.valueOf(i + 1);

            JLabel num = new JLabel(badgeTxt) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(badgeBg);
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        R_STEP, R_STEP));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            num.setFont(new Font("Segoe UI", Font.BOLD, 11));
            num.setPreferredSize(new Dimension(22, 22));
            num.setHorizontalAlignment(SwingConstants.CENTER);
            num.setForeground(badgeFg);
            num.setOpaque(false);

            JLabel lbl = new JLabel(steps[i]);
            lbl.setFont(active ? FONT_BOLD : FONT_SMALL);
            lbl.setForeground(active ? CLR_BLACK : CLR_GRAY);

            chip.add(num); chip.add(lbl);
            bar.add(chip);

            if (i < steps.length - 1) {
                JLabel arrow = new JLabel("  \u203A  ");
                arrow.setFont(FONT_SMALL);
                arrow.setForeground(CLR_GRAY);
                bar.add(arrow);
            }
        }
        return bar;
    }

    /**
     * Builds a rounded section card using BorderLayout so the header divider
     * is always full width. Returns [0] = outer card, [1] = content panel.
     * Callers add widgets into parts[1] (content), and return parts[0] (card).
     */
    private JPanel[] buildSectionCardParts(String title) {
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(FONT_SECTION);
        titleLbl.setForeground(CLR_BLACK);
        titleLbl.setBorder(new EmptyBorder(0, 0, 8, 0));

        // 1px divider — BorderLayout.SOUTH guarantees full card width
        JPanel divLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(CLR_BORDER);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(0, 1); }
            @Override public Dimension getMinimumSize()   { return new Dimension(0, 1); }
        };
        divLine.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        header.add(titleLbl, BorderLayout.CENTER);
        header.add(divLine,  BorderLayout.SOUTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        RoundedPanel card = new RoundedPanel(R_CARD, CLR_WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));
        card.add(header,  BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return new JPanel[]{ card, content };
    }

    private JPanel buildField(String labelText, JComponent field) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBackground(CLR_WHITE);

        JLabel lbl = new JLabel(labelText.toUpperCase());
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(CLR_GRAY);
        lbl.setBorder(new EmptyBorder(0, 0, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        wrap.add(lbl);
        wrap.add(field);
        return wrap;
    }

    private JLabel buildDetailChip(JPanel parent, String key, String value) {
        JPanel chip = new JPanel();
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setOpaque(false);

        JLabel keyLbl = new JLabel(key.toUpperCase());
        keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        keyLbl.setForeground(CLR_GRAY);
        keyLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(FONT_BOLD);
        valLbl.setForeground(CLR_BLACK);
        valLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        chip.add(keyLbl);
        chip.add(Box.createVerticalStrut(2));
        chip.add(valLbl);
        parent.add(chip);
        return valLbl;
    }

    /**
     * FIX 3: Summary rows now have a generous max-height (26px) so they are
     * not squeezed to zero by BoxLayout when the card is narrow.
     */
    private JLabel buildSummaryRow(JPanel parent, String key, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        // FIX: increased height from 22 → 26 so rows are never clipped
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        row.setBorder(new EmptyBorder(2, 0, 2, 0));

        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(FONT_SMALL);
        keyLbl.setForeground(CLR_GRAY);

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        valLbl.setForeground(CLR_BLACK);
        valLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(keyLbl, BorderLayout.WEST);
        row.add(valLbl, BorderLayout.EAST);
        parent.add(row);
        parent.add(Box.createVerticalStrut(2));
        return valLbl;
    }

    private JLabel addConfirmRow(JPanel grid, String key) {
        JLabel keyLbl = new JLabel(key);
        keyLbl.setFont(FONT_LABEL);
        keyLbl.setForeground(CLR_GRAY);

        JLabel valLbl = new JLabel("—");
        valLbl.setFont(FONT_INPUT);
        valLbl.setForeground(CLR_BLACK);

        grid.add(keyLbl);
        grid.add(valLbl);
        return valLbl;
    }

    /**
     * FIX: painted JPanel instead of JSeparator — always fills full width.
     */
    private JPanel buildDivider() {
        JPanel divLine = new JPanel() {
            @Override public Dimension getPreferredSize() { return new Dimension(0, 1); }
            @Override public Dimension getMinimumSize()   { return new Dimension(0, 1); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 1); }
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(CLR_BORDER);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        divLine.setOpaque(false);
        divLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        return divLine;
    }

    /** Solid filled rounded button */
    private JButton buildSolidButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    R_BUTTON * 2, R_BUTTON * 2));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 38));
        Color darker = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(darker); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg);     }
        });
        return btn;
    }

    /** Outline rounded button */
    private JButton buildOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    R_BUTTON * 2, R_BUTTON * 2));
                g2.setColor(CLR_BORDER);
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2,
                    R_BUTTON * 2, R_BUTTON * 2));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(CLR_BLACK);
        btn.setBackground(CLR_WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 38));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(CLR_SECTION_BG); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(CLR_WHITE);      btn.repaint(); }
        });
        return btn;
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setFont(FONT_INPUT);
        combo.setBackground(CLR_FIELD_BG);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT_INPUT);
        spinner.setBackground(CLR_FIELD_BG);
        spinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor ed) {
            ed.getTextField().setBackground(CLR_FIELD_BG);
            ed.getTextField().setFont(FONT_INPUT);
        }
    }

    // ====================================================
    //  INNER — RoundedPanel
    // ====================================================
    private static class RoundedPanel extends JPanel {
        private final int   radius;
        private final Color bg;

        RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            this.bg     = bg;
            setOpaque(false);
            setBackground(bg);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                radius * 2, radius * 2));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ====================================================
    //  INNER — RoundedTextField
    // ====================================================
    private static class RoundedTextField extends JTextField {
        private final int radius;

        RoundedTextField(int radius) {
            this.radius = radius;
            setOpaque(false);
            setBorder(new EmptyBorder(6, 10, 6, 10));
            setBackground(CLR_FIELD_BG);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                radius * 2, radius * 2));
            g2.dispose();
            super.paintComponent(g);
        }

        @Override protected void paintBorder(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CLR_BORDER);
            g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2,
                radius * 2, radius * 2));
            g2.dispose();
        }
    }
}
