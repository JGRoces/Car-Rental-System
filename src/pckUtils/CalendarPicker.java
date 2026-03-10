package pckUtils;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * CalendarPicker — date-picker button that opens a floating calendar panel.
 */
public class CalendarPicker extends JButton {

    private static final Color CLR_WHITE     = Color.WHITE;
    private static final Color CLR_BLACK     = new Color(18, 18, 18);
    private static final Color CLR_GRAY      = new Color(120, 120, 120);
    private static final Color CLR_BORDER    = new Color(200, 200, 200);
    private static final Color CLR_BLUE      = new Color(37, 99, 235);
    private static final Color CLR_BLUE_SOFT = new Color(219, 234, 254);
    private static final Color CLR_FIELD_BG  = new Color(250, 250, 250);
    private static final Color CLR_HOVER     = new Color(235, 235, 235);

    private static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_DOW  = new Font("Segoe UI", Font.BOLD,  11);
    private static final Font FONT_DAY  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_NAV  = new Font("Segoe UI", Font.BOLD,  16);

    private LocalDate selected;
    private Runnable  onChange;

    public CalendarPicker() { this(LocalDate.now(), null); }

    public CalendarPicker(LocalDate initial, Runnable onChange) {
        this.selected = initial;
        this.onChange = onChange;
        setFont(FONT_BODY);
        setForeground(CLR_BLACK);
        setBackground(CLR_FIELD_BG);
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CLR_BORDER, 1, true),
            new EmptyBorder(6, 10, 6, 10)));
        setContentAreaFilled(false);
        setFocusPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        setPreferredSize(new Dimension(160, 36));
        updateText();
        addActionListener(e -> showCalendar());
    }

    private void updateText() { setText(selected.toString()); }

    public LocalDate getLocalDate()     { return selected; }
    public String    getFormattedDate() { return selected.toString(); }
    public void      setOnChange(Runnable r) { this.onChange = r; }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        g2.dispose();
        super.paintComponent(g);
    }

    private void showCalendar() {
        // Find the top-level frame/dialog to use as owner
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            // Fallback: find any visible frame
            for (Frame f : Frame.getFrames()) {
                if (f.isVisible()) { owner = f; break; }
            }
        }

        final JDialog cal = (owner instanceof Frame)
            ? new JDialog((Frame) owner, false)
            : (owner instanceof Dialog)
                ? new JDialog((Dialog) owner, false)
                : new JDialog((Frame) null, false);

        cal.setUndecorated(true);
        cal.getRootPane().setBorder(BorderFactory.createLineBorder(CLR_BORDER, 1));

        final LocalDate[] viewing = { selected.withDayOfMonth(1) };

        // Month label
        JLabel monthLbl = new JLabel("", SwingConstants.CENTER);
        monthLbl.setFont(FONT_BOLD);
        monthLbl.setForeground(CLR_BLACK);

        // Nav buttons
        JButton prev = navBtn("<");
        JButton next = navBtn(">");

        JPanel nav = new JPanel(new BorderLayout(4, 0));
        nav.setBackground(CLR_WHITE);
        nav.setBorder(new EmptyBorder(0, 0, 8, 0));
        nav.add(prev,     BorderLayout.WEST);
        nav.add(monthLbl, BorderLayout.CENTER);
        nav.add(next,     BorderLayout.EAST);

        // Day-of-week header
        JPanel dowRow = new JPanel(new GridLayout(1, 7, 4, 0));
        dowRow.setBackground(CLR_WHITE);
        dowRow.setBorder(new EmptyBorder(0, 0, 4, 0));
        for (String d : new String[]{"Su","Mo","Tu","We","Th","Fr","Sa"}) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(FONT_DOW);
            l.setForeground(CLR_GRAY);
            l.setPreferredSize(new Dimension(34, 20));
            dowRow.add(l);
        }

        // Grid container
        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.setBackground(CLR_WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CLR_WHITE);
        top.add(nav,    BorderLayout.NORTH);
        top.add(dowRow, BorderLayout.SOUTH);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(CLR_WHITE);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        root.add(top,      BorderLayout.NORTH);
        root.add(gridWrap, BorderLayout.CENTER);

        Runnable rebuild = () -> {
            monthLbl.setText(viewing[0].format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            gridWrap.removeAll();
            gridWrap.add(buildDayGrid(viewing[0], cal), BorderLayout.CENTER);
            gridWrap.revalidate();
            gridWrap.repaint();
            cal.pack();
        };

        prev.addActionListener(e -> { viewing[0] = viewing[0].minusMonths(1); rebuild.run(); });
        next.addActionListener(e -> { viewing[0] = viewing[0].plusMonths(1);  rebuild.run(); });

        cal.setContentPane(root);
        rebuild.run();

        // Position below the button
        try {
            Point loc = getLocationOnScreen();
            cal.setLocation(loc.x, loc.y + getHeight() + 2);
        } catch (Exception ex) {
            cal.setLocationRelativeTo(this);
        }

        // Close when focus lost
        cal.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) {
                cal.dispose();
            }
        });

        cal.setVisible(true);
    }

    private JPanel buildDayGrid(LocalDate firstOfMonth, JDialog cal) {
        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setBackground(CLR_WHITE);
        grid.setBorder(new EmptyBorder(2, 0, 0, 0));

        int startDow    = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = firstOfMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < startDow; i++) {
            JLabel empty = new JLabel();
            empty.setPreferredSize(new Dimension(34, 34));
            grid.add(empty);
        }

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date       = firstOfMonth.withDayOfMonth(d);
            boolean   isSelected = date.equals(selected);
            boolean   isToday    = date.equals(today);

            JLabel cell = new JLabel(String.valueOf(d), SwingConstants.CENTER) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (isSelected) {
                        g2.setColor(CLR_BLUE);
                        g2.fillOval(1, 1, getWidth()-2, getHeight()-2);
                    } else if (isToday) {
                        g2.setColor(CLR_BLUE_SOFT);
                        g2.fillOval(1, 1, getWidth()-2, getHeight()-2);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            cell.setFont(isSelected ? FONT_BOLD : FONT_DAY);
            cell.setForeground(isSelected ? Color.WHITE : isToday ? CLR_BLUE : CLR_BLACK);
            cell.setOpaque(false);
            cell.setPreferredSize(new Dimension(34, 34));
            cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

            cell.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    if (!isSelected) { cell.setOpaque(true); cell.setBackground(CLR_HOVER); cell.repaint(); }
                }
                @Override public void mouseExited(MouseEvent e) {
                    cell.setOpaque(false); cell.repaint();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    selected = date;
                    updateText();
                    cal.dispose();
                    if (onChange != null) onChange.run();
                }
            });

            grid.add(cell);
        }
        return grid;
    }

    private JButton navBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_NAV);
        b.setForeground(CLR_GRAY);
        b.setBackground(CLR_WHITE);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(32, 28));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}
