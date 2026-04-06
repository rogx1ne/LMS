package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class ProcurementModulePanel extends JPanel {
    private final SellerPanel sellerPanel = new SellerPanel();
    private final OrderEntryPanel orderEntryPanel = new OrderEntryPanel();
    private final OrderViewPanel orderViewPanel = new OrderViewPanel();
    private final BillEntryPanel billEntryPanel = new BillEntryPanel();
    private final BillReportPanel billReportPanel = new BillReportPanel();
    private final BillAccessionPanel billAccessionPanel = new BillAccessionPanel();

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
        JButton navBillEntry = ModuleTheme.createNavButton("BILL ENTRY");
        JButton navBillReport = ModuleTheme.createNavButton("BILL REPORT");
        JButton navBillAccession = ModuleTheme.createNavButton("BILL ACCESSION");

        header.add(navSeller);
        header.add(navAddOrder);
        header.add(navViewOrder);
        header.add(navBillEntry);
        header.add(navBillReport);
        header.add(navBillAccession);

        contentPanel.setBackground(ModuleTheme.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(sellerPanel, "SELLER");
        contentPanel.add(orderEntryPanel, "ADD_ORDER");
        contentPanel.add(orderViewPanel, "VIEW_ORDER");
        contentPanel.add(billEntryPanel, "BILL_ENTRY");
        contentPanel.add(billReportPanel, "BILL_REPORT");
        contentPanel.add(billAccessionPanel, "BILL_ACCESSION");

        navSeller.addActionListener(e -> cardLayout.show(contentPanel, "SELLER"));
        navAddOrder.addActionListener(e -> cardLayout.show(contentPanel, "ADD_ORDER"));
        navViewOrder.addActionListener(e -> cardLayout.show(contentPanel, "VIEW_ORDER"));
        navBillEntry.addActionListener(e -> cardLayout.show(contentPanel, "BILL_ENTRY"));
        navBillReport.addActionListener(e -> cardLayout.show(contentPanel, "BILL_REPORT"));
        navBillAccession.addActionListener(e -> cardLayout.show(contentPanel, "BILL_ACCESSION"));

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "SELLER");
    }

    public SellerPanel getSellerPanel() { return sellerPanel; }
    public OrderEntryPanel getOrderEntryPanel() { return orderEntryPanel; }
    public OrderViewPanel getOrderViewPanel() { return orderViewPanel; }
    public BillEntryPanel getBillEntryPanel() { return billEntryPanel; }
    public BillReportPanel getBillReportPanel() { return billReportPanel; }
    public BillAccessionPanel getBillAccessionPanel() { return billAccessionPanel; }

    public void showSection(String key) {
        if (key == null) return;
        switch (key.trim()) {
            case "SELLER":
                cardLayout.show(contentPanel, "SELLER");
                break;
            case "ADD_ORDER":
                cardLayout.show(contentPanel, "ADD_ORDER");
                break;
            case "VIEW_ORDER":
                cardLayout.show(contentPanel, "VIEW_ORDER");
                break;
            case "BILL_ENTRY":
                cardLayout.show(contentPanel, "BILL_ENTRY");
                break;
            case "BILL_REPORT":
                cardLayout.show(contentPanel, "BILL_REPORT");
                break;
            case "BILL":
            case "BILL_ACCESSION":
                cardLayout.show(contentPanel, "BILL_ACCESSION");
                break;
            default:
                break;
        }
    }
}
