package com.library.ui;

import com.library.model.OrderDetail;
import com.library.model.OrderHeader;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProcOrderEditDialog extends JDialog {
    private final JComboBox<String> cmbSellerId = new JComboBox<>();
    private final JTextField txtOrderDate = new JTextField();

    private final JTextField txtTitle = new JTextField();
    private final JTextField txtAuthor = new JTextField();
    private final JTextField txtPublication = new JTextField();
    private final JTextField txtQty = new JTextField();

    private final JButton btnAddItem = new JButton("Add Item");
    private final JButton btnRemoveItem = new JButton("Remove Selected");
    private final JButton btnSave = new JButton("Save Update");
    private final JButton btnCancel = new JButton("Cancel");

    private final DefaultTableModel model;
    private final JTable table;
    private boolean saved = false;

    public ProcOrderEditDialog(Window owner, OrderHeader header, List<String> sellerIds, List<OrderDetail> details) {
        super(owner, "Update Order - " + header.getOrderId(), ModalityType.APPLICATION_MODAL);
        setSize(800, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridBagLayout());
        top.setBorder(ModuleTheme.sectionBorder("Update Order"));

        ModuleTheme.styleCombo(cmbSellerId);
        ModuleTheme.styleInput(txtOrderDate);
        ModuleTheme.addDatePicker(txtOrderDate);
        txtOrderDate.setToolTipText("Click to select Order Date (yyyy-MM-dd)");

        for (String id : sellerIds) cmbSellerId.addItem(id);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 7, 5, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; top.add(new JLabel("Seller S-ID"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; top.add(cmbSellerId, gbc);
        gbc.gridx = 2; gbc.weightx = 0; top.add(new JLabel("Order Date"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; top.add(txtOrderDate, gbc);

        JPanel itemInput = new JPanel(new GridBagLayout());
        itemInput.setBorder(ModuleTheme.sectionBorder("Add Item"));
        itemInput.setBackground(ModuleTheme.WHITE);

        ModuleTheme.styleInput(txtTitle);
        ModuleTheme.styleInput(txtAuthor);
        ModuleTheme.styleInput(txtPublication);
        ModuleTheme.styleInput(txtQty);
        ModuleTheme.stylePrimaryButton(btnAddItem);
        ModuleTheme.styleSubtleButton(btnRemoveItem);

        GridBagConstraints ig = new GridBagConstraints();
        ig.insets = new Insets(5, 7, 5, 7);
        ig.fill = GridBagConstraints.HORIZONTAL;

        ig.gridx = 0; ig.gridy = 0; itemInput.add(new JLabel("Title"), ig);
        ig.gridx = 1; ig.weightx = 1; itemInput.add(txtTitle, ig);
        ig.gridx = 2; ig.weightx = 0; itemInput.add(new JLabel("Author"), ig);
        ig.gridx = 3; ig.weightx = 1; itemInput.add(txtAuthor, ig);

        ig.gridx = 0; ig.gridy = 1; ig.weightx = 0; itemInput.add(new JLabel("Publication"), ig);
        ig.gridx = 1; ig.weightx = 1; itemInput.add(txtPublication, ig);
        ig.gridx = 2; ig.weightx = 0; itemInput.add(new JLabel("Qty"), ig);
        ig.gridx = 3; ig.weightx = 1; itemInput.add(txtQty, ig);

        JPanel itemActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        itemActions.setBackground(ModuleTheme.WHITE);
        itemActions.add(btnRemoveItem);
        itemActions.add(btnAddItem);

        ig.gridx = 0; ig.gridy = 2; ig.gridwidth = 4; ig.weightx = 1;
        itemInput.add(itemActions, ig);

        model = new DefaultTableModel(new String[]{"Book Title", "Author", "Publication", "Quantity"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        ModuleTheme.styleTable(table);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ModuleTheme.styleSubtleButton(btnCancel);
        ModuleTheme.styleAccentButton(btnSave);
        footer.add(btnCancel);
        footer.add(btnSave);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.add(itemInput, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        cmbSellerId.setSelectedItem(header.getSellerId());
        txtOrderDate.setText(String.valueOf(header.getOrderDate()));
        for (OrderDetail d : details) {
            model.addRow(new Object[]{d.getBookTitle(), d.getAuthor(), d.getPublication(), d.getQuantity()});
        }

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> { saved = true; dispose(); });
    }

    public JComboBox<String> getCmbSellerId() { return cmbSellerId; }
    public JTextField getTxtOrderDate() { return txtOrderDate; }
    public JTextField getTxtTitle() { return txtTitle; }
    public JTextField getTxtAuthor() { return txtAuthor; }
    public JTextField getTxtPublication() { return txtPublication; }
    public JTextField getTxtQty() { return txtQty; }
    public JButton getBtnAddItem() { return btnAddItem; }
    public JButton getBtnRemoveItem() { return btnRemoveItem; }
    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }
    public boolean isSaved() { return saved; }
}
