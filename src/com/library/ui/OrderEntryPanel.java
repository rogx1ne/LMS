package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class OrderEntryPanel extends JPanel {
    private final JTextField txtOrderId = new JTextField();
    private final JTextField txtOrderDate = new JTextField();
    private final JComboBox<String> cmbSellerId = new JComboBox<>();

    private final JTextField txtBookTitle = new JTextField();
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtPublication = new JTextField();
    private final JTextField txtQty = new JTextField();

    private final JButton btnAddItem = new JButton("Add Item");
    private final JButton btnRemoveItem = new JButton("Remove Selected");
    private final JButton btnPlaceOrder = new JButton("Place Order");

    private final DefaultTableModel itemsModel;
    private final JTable itemsTable;

    private final JTextArea txtReceiptPreview = new JTextArea(10, 30);
    private final JButton btnDownloadReceipt = new JButton("Download Receipt PDF");
    private final JButton btnSendEmail = new JButton("Send PDF by Email");

    public OrderEntryPanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new GridBagLayout());
        top.setBackground(ModuleTheme.WHITE);
        top.setBorder(ModuleTheme.sectionBorder("Add Order"));

        styleInputs();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(top, gbc, row++, "Order ID", txtOrderId, "Order Date", txtOrderDate);
        addRow(top, gbc, row++, "Seller S-ID", cmbSellerId, "Book Title", txtBookTitle);
        addRow(top, gbc, row++, "Author", txtAuthor, "Publication", txtPublication);
        addRow(top, gbc, row++, "Quantity", txtQty, "", new JLabel(""));

        JPanel itemActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        itemActions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.styleSubtleButton(btnRemoveItem);
        ModuleTheme.stylePrimaryButton(btnAddItem);
        itemActions.add(btnRemoveItem);
        itemActions.add(btnAddItem);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        top.add(itemActions, gbc);

        String[] cols = {"Book Title", "Author", "Publication", "Quantity"};
        itemsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsModel);
        ModuleTheme.styleTable(itemsTable);

        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsScroll.getViewport().setBackground(ModuleTheme.TABLE_BG);
        itemsScroll.setBorder(ModuleTheme.sectionBorder("Order Items"));

        JPanel receiptPanel = new JPanel(new BorderLayout(0, 6));
        receiptPanel.setBackground(ModuleTheme.WHITE);
        receiptPanel.setBorder(ModuleTheme.sectionBorder("Receipt Preview"));
        txtReceiptPreview.setEditable(false);
        txtReceiptPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReceiptPreview.setBackground(new Color(250, 250, 250));
        txtReceiptPreview.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        receiptPanel.add(new JScrollPane(txtReceiptPreview), BorderLayout.CENTER);

        JPanel receiptActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        receiptActions.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnDownloadReceipt);
        ModuleTheme.styleAccentButton(btnSendEmail);
        receiptActions.add(btnDownloadReceipt);
        receiptActions.add(btnSendEmail);
        receiptPanel.add(receiptActions, BorderLayout.SOUTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(ModuleTheme.WHITE);
        ModuleTheme.stylePrimaryButton(btnPlaceOrder);
        footer.add(btnPlaceOrder);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, itemsScroll, receiptPanel);
        split.setResizeWeight(0.5);
        split.setBorder(null);

        add(top, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void styleInputs() {
        ModuleTheme.styleInput(txtOrderId);
        ModuleTheme.styleInput(txtOrderDate);
        ModuleTheme.addDatePicker(txtOrderDate);
        ModuleTheme.styleCombo(cmbSellerId);
        ModuleTheme.styleInput(txtBookTitle);
        ModuleTheme.styleInput(txtAuthor);
        ModuleTheme.styleInput(txtPublication);
        ModuleTheme.styleInput(txtQty);

        txtOrderId.setEditable(false);
        txtOrderId.setBackground(new Color(245, 245, 245));
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String l1, JComponent c1, String l2, JComponent c2) {
        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0;
        panel.add(label(l1), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(c1, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(label(l2), gbc);

        gbc.gridx = 3;
        gbc.weightx = 1;
        panel.add(c2, gbc);
    }

    private JLabel label(String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(ModuleTheme.BLUE_DARK);
        return l;
    }

    public JTextField getTxtOrderId() { return txtOrderId; }
    public JTextField getTxtOrderDate() { return txtOrderDate; }
    public JComboBox<String> getCmbSellerId() { return cmbSellerId; }

    public JTextField getTxtBookTitle() { return txtBookTitle; }
    public JTextField getTxtAuthor() { return txtAuthor; }
    public JTextField getTxtPublication() { return txtPublication; }
    public JTextField getTxtQty() { return txtQty; }

    public JButton getBtnAddItem() { return btnAddItem; }
    public JButton getBtnRemoveItem() { return btnRemoveItem; }
    public JButton getBtnPlaceOrder() { return btnPlaceOrder; }

    public DefaultTableModel getItemsModel() { return itemsModel; }
    public JTable getItemsTable() { return itemsTable; }

    public JTextArea getTxtReceiptPreview() { return txtReceiptPreview; }
    public JButton getBtnDownloadReceipt() { return btnDownloadReceipt; }
    public JButton getBtnSendEmail() { return btnSendEmail; }

    public void clearItemInput() {
        txtBookTitle.setText("");
        txtAuthor.setText("");
        txtPublication.setText("");
        txtQty.setText("");
    }
}
