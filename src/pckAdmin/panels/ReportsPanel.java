package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckModels.Payment;
import pckModels.Rental;
import pckServices.RentalService;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    public ReportsPanel(AdminNavCallback nav) {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
    }

    private void build() {
        add(AdminUIHelper.buildPageHeader("Reports",
            "System-wide summary of rentals and payments."), BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        // ── Revenue Summary ───────────────────────────────────
        body.add(AdminUIHelper.buildSectionLabel("Revenue Summary"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildRevenueSummary());
        body.add(Box.createVerticalStrut(24));

        // ── All Rentals ───────────────────────────────────────
        body.add(AdminUIHelper.buildSectionLabel("All Rentals"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildRentalsTable());
        body.add(Box.createVerticalStrut(24));

        // ── All Payments ──────────────────────────────────────
        body.add(AdminUIHelper.buildSectionLabel("All Payments"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildPaymentsTable());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildRevenueSummary() {
        int total   = RentalService.getAllRentals().size();
        int active  = RentalService.getActiveCount();
        int pending = RentalService.getPendingCount();
        String rev  = "\u20b1" + String.format("%,.2f", RentalService.getTotalRevenue());

        Object[][] rows = {{ total, active, pending, rev }};
        JTable table = AdminUIHelper.buildStyledTable(
            new String[]{ "Total Rentals", "Active", "Pending", "Total Revenue (Completed)" }, rows);
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(scroll);
        return wrapper;
    }

    private JPanel buildRentalsTable() {
        String[] cols = { "ID", "Customer ID", "Car ID", "Start", "End", "Amount", "Status" };
        List<Rental> rentals = RentalService.getAllRentals();
        Object[][] rows = new Object[rentals.size()][];
        for (int i = 0; i < rentals.size(); i++) {
            Rental r = rentals.get(i);
            rows[i] = new Object[]{ r.getRentalId(), r.getCustomerId(), r.getCarId(),
                r.getStartDate(), r.getEndDate(),
                "\u20b1" + String.format("%,.2f", r.getTotalAmount()), r.getStatus() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(6).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(scroll);
        return wrapper;
    }

    private JPanel buildPaymentsTable() {
        String[] cols = { "Payment ID", "Rental ID", "Amount", "Date", "Method", "Status" };
        List<Payment> payments = RentalService.getAllPayments();
        Object[][] rows = new Object[payments.size()][];
        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            rows[i] = new Object[]{ p.getPaymentId(), p.getRentalId(),
                "\u20b1" + String.format("%,.2f", p.getAmountPaid()),
                p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : "\u2014",
                p.getPaymentMethod(), p.getStatus() };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(5).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false); wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(scroll);
        return wrapper;
    }
}
