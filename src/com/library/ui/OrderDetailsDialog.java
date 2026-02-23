package com.library.ui;

import com.library.model.OrderDetail;
import com.library.model.OrderHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderDetailsDialog extends JDialog {
    private final JButton btnDownloadPdf = new JButton("Download PDF");

    public OrderDetailsDialog(Window owner, OrderHeader header, String sellerInfo, List<OrderDetail> details) {
        super(owner, "Order Details - " + header.getOrderId(), ModalityType.APPLICATION_MODAL);
        setSize(760, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2, 2, 8, 4));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        top.add(new JLabel("Order ID: " + header.getOrderId()));
        top.add(new JLabel("Order Date: " + header.getOrderDate()));
        top.add(new JLabel("Seller ID: " + header.getSellerId()));
        top.add(new JLabel("Seller: " + sellerInfo));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Book Title", "Author", "Publication", "Quantity"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (OrderDetail d : details) {
            model.addRow(new Object[]{d.getBookTitle(), d.getAuthor(), d.getPublication(), d.getQuantity()});
        }

        JTable table = new JTable(model);
        ModuleTheme.styleTable(table);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ModuleTheme.stylePrimaryButton(btnDownloadPdf);
        JButton btnClose = new JButton("Close");
        ModuleTheme.styleSubtleButton(btnClose);
        btnClose.addActionListener(e -> dispose());
        footer.add(btnClose);
        footer.add(btnDownloadPdf);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    public JButton getBtnDownloadPdf() { return btnDownloadPdf; }
}
