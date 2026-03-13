package pckAdmin.panels;

import pckAdmin.AdminDashboardGUI.AdminNavCallback;
import pckAdmin.shared.AdminUIHelper;
import pckDatabase.CarDAO;
import pckDatabase.CustomerDAO;
import pckDatabase.RentalDAO;
import pckModels.Car;
import pckModels.Customer;
import pckModels.Payment;
import pckModels.Rental;
import pckServices.RentalService;
import pckUtils.UIAssets;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentsPanel extends JPanel {

    public PaymentsPanel(AdminNavCallback nav) {
        setLayout(new BorderLayout());
        setBackground(UIAssets.getBg());
        setBorder(new EmptyBorder(32, 36, 32, 36));
        build();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                removeAll();
                build();
                revalidate();
                repaint();
            }
        });
    }

    private void build() {
        add(AdminUIHelper.buildPageHeader("Payments",
            "Review transactions and approve pending payments."), BorderLayout.NORTH);

        // Pre-load lookup maps once for all tables
        Map<Integer, String> customerNames = new HashMap<>();
        Map<Integer, String> carNames      = new HashMap<>();
        Map<Integer, Rental> rentalMap     = new HashMap<>();

        new CustomerDAO().getAllCustomers().forEach(c ->
            customerNames.put(c.getCustomerId(), c.getFullName()));
        new CarDAO().getAllCars().forEach(c ->
            carNames.put(c.getCarId(), c.getBrand() + " " + c.getModel() + " (" + c.getPlateNumber() + ")"));
        new RentalDAO().getAllRentals().forEach(r ->
            rentalMap.put(r.getRentalId(), r));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        body.add(AdminUIHelper.buildSectionLabel("Transaction Ledger"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildLedger(customerNames, carNames, rentalMap));
        body.add(Box.createVerticalStrut(24));

        body.add(AdminUIHelper.buildSectionLabel("Pending Verifications"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildPendingVerifications(customerNames, carNames, rentalMap));
        body.add(Box.createVerticalStrut(24));

        body.add(AdminUIHelper.buildSectionLabel("Late Fees"));
        body.add(Box.createVerticalStrut(10));
        body.add(buildLateFeesCard());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildLedger(Map<Integer, String> customerNames,
                                Map<Integer, String> carNames,
                                Map<Integer, Rental> rentalMap) {
        String[] cols = { "Payment #", "Customer", "Vehicle", "Amount", "Date", "Method", "Status" };
        List<Payment> payments = RentalService.getAllPayments();
        Object[][] rows = new Object[payments.size()][];
        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            Rental  r = rentalMap.get(p.getRentalId());
            String customer = r != null ? customerNames.getOrDefault(r.getCustomerId(), "Customer #" + r.getCustomerId()) : "—";
            String car      = r != null ? carNames.getOrDefault(r.getCarId(), "Car #" + r.getCarId()) : "—";
            rows[i] = new Object[]{
                p.getPaymentId(), customer, car,
                "\u20b1" + String.format("%,.2f", p.getAmountPaid()),
                p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : "\u2014",
                p.getPaymentMethod(), p.getStatus()
            };
        }
        JTable table = AdminUIHelper.buildStyledTable(cols, rows);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(6).setCellRenderer(new AdminUIHelper.StatusBadgeRenderer());
        JScrollPane scroll = AdminUIHelper.buildTableScrollPane(table);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(scroll);
        return wrapper;
    }

    private JPanel buildPendingVerifications(Map<Integer, String> customerNames,
                                              Map<Integer, String> carNames,
                                              Map<Integer, Rental> rentalMap) {
        String[] cols = { "Payment #", "Customer", "Vehicle", "Amount", "Method", "Submitted", "Action" };
        List<Payment> pending = RentalService.getPendingPayments();

        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        for (Payment p : pending) {
            Rental r = rentalMap.get(p.getRentalId());
            String customer = r != null ? customerNames.getOrDefault(r.getCustomerId(), "Customer #" + r.getCustomerId()) : "—";
            String car      = r != null ? carNames.getOrDefault(r.getCarId(), "Car #" + r.getCarId()) : "—";
            model.addRow(new Object[]{
                p.getPaymentId(), customer, car,
                "\u20b1" + String.format("%,.2f", p.getAmountPaid()),
                p.getPaymentMethod(),
                p.getPaymentDate() != null ? p.getPaymentDate().toLocalDate() : "\u2014",
                "approve"
            });
        }

        JTable table = AdminUIHelper.buildStyledTable(cols, null);
        table.setModel(model);
        table.getColumn("Action").setCellRenderer(new ApproveRenderer());
        table.getColumn("Action").setCellEditor(new ApproveEditor(model));
        table.getColumn("Action").setMinWidth(110);
        table.getColumn("Action").setMaxWidth(130);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UIAssets.getSurface());
        wrapper.setBorder(new LineBorder(UIAssets.getBorder(), 1, true));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(table.getTableHeader(), BorderLayout.NORTH);
        if (pending.isEmpty()) {
            wrapper.add(AdminUIHelper.buildEmptyState("No pending payment verifications."), BorderLayout.CENTER);
        } else {
            wrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        return wrapper;
    }

    private JPanel buildLateFeesCard() {
        JPanel card = new JPanel(new GridLayout(1, 3, 16, 0));
        card.setBackground(UIAssets.getSurface());
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(UIAssets.getBorder(), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(buildInfoField("Standard Late Fee Rate", "\u20b1500 / day"));
        card.add(buildInfoField("Overdue Rentals", "\u2014"));
        card.add(buildInfoField("Total Late Fees Owed", "\u2014"));
        return card;
    }

    private JPanel buildInfoField(String label, String value) {
        JPanel col = new JPanel(new GridLayout(2, 1, 0, 6));
        col.setOpaque(false);
        JLabel lbl = new JLabel(label); lbl.setFont(UIAssets.FONT_H3);
        JLabel val = new JLabel(value); val.setFont(UIAssets.FONT_BODY);
        val.setForeground(UIAssets.getTextSecondary());
        col.add(lbl); col.add(val);
        return col;
    }

    private static class ApproveRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) {
            JButton btn = new JButton("Approve");
            btn.setFont(UIAssets.FONT_SMALL);
            btn.setForeground(Color.WHITE);
            btn.setBackground(UIAssets.CLR_GREEN);
            btn.setBorderPainted(false);
            return btn;
        }
    }

    private class ApproveEditor extends DefaultCellEditor {
        private final DefaultTableModel model;

        ApproveEditor(DefaultTableModel model) {
            super(new JCheckBox());
            this.model = model;
        }

        @Override public Component getTableCellEditorComponent(
                JTable t, Object v, boolean s, int r, int c) {
            int row = r;
            JButton btn = AdminUIHelper.buildSolidButton("Approve", UIAssets.CLR_GREEN);
            btn.addActionListener(e -> {
                Object val = model.getValueAt(row, 0);
                if (!(val instanceof Integer)) { fireEditingStopped(); return; }
                int paymentId = (int) val;
                fireEditingStopped();
                if (RentalService.approvePayment(paymentId)) {
                    model.removeRow(row);
                    JOptionPane.showMessageDialog(PaymentsPanel.this, "Payment approved.");
                } else {
                    JOptionPane.showMessageDialog(PaymentsPanel.this,
                        "Failed to approve payment.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            return btn;
        }
    }
}
