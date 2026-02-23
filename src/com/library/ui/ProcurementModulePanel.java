package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class ProcurementModulePanel extends JPanel {
    private final SellerPanel sellerPanel = new SellerPanel();
    private final OrderEntryPanel orderEntryPanel = new OrderEntryPanel();
    private final OrderViewPanel orderViewPanel = new OrderViewPanel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public ProcurementModulePanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton navSeller = ModuleTheme.createNavButton("SELLER MASTER");
        JButton navAddOrder = ModuleTheme.createNavButton("ADD ORDER");
        JButton navViewOrder = ModuleTheme.createNavButton("VIEW ORDER");

        header.add(navSeller);
        header.add(navAddOrder);
        header.add(navViewOrder);

        contentPanel.setBackground(ModuleTheme.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(sellerPanel, "SELLER");
        contentPanel.add(orderEntryPanel, "ADD_ORDER");
        contentPanel.add(orderViewPanel, "VIEW_ORDER");

        navSeller.addActionListener(e -> cardLayout.show(contentPanel, "SELLER"));
        navAddOrder.addActionListener(e -> cardLayout.show(contentPanel, "ADD_ORDER"));
        navViewOrder.addActionListener(e -> cardLayout.show(contentPanel, "VIEW_ORDER"));

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "SELLER");
    }

    public SellerPanel getSellerPanel() { return sellerPanel; }
    public OrderEntryPanel getOrderEntryPanel() { return orderEntryPanel; }
    public OrderViewPanel getOrderViewPanel() { return orderViewPanel; }
}
