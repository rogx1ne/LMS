package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class BookModulePanel extends JPanel {
    private final AddBookPanel addBookPanel = new AddBookPanel();
    private final BillAccessionPanel billAccessionPanel = new BillAccessionPanel();
    private final AccessionRegisterPanel accessionRegisterPanel = new AccessionRegisterPanel();
    private final StockPanel stockPanel = new StockPanel();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public BookModulePanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton navAdd = ModuleTheme.createNavButton("ADD BOOK");
        JButton navBill = ModuleTheme.createNavButton("BILL ACCESSION");
        JButton navRegister = ModuleTheme.createNavButton("ACCESSION REGISTER");
        JButton navStock = ModuleTheme.createNavButton("STOCK");

        header.add(navAdd);
        header.add(navBill);
        header.add(navRegister);
        header.add(navStock);

        contentPanel.setBackground(ModuleTheme.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(addBookPanel, "ADD");
        contentPanel.add(billAccessionPanel, "BILL");
        contentPanel.add(accessionRegisterPanel, "REGISTER");
        contentPanel.add(stockPanel, "STOCK");

        navAdd.addActionListener(e -> cardLayout.show(contentPanel, "ADD"));
        navBill.addActionListener(e -> cardLayout.show(contentPanel, "BILL"));
        navRegister.addActionListener(e -> cardLayout.show(contentPanel, "REGISTER"));
        navStock.addActionListener(e -> cardLayout.show(contentPanel, "STOCK"));

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "ADD");
    }

    public AddBookPanel getAddBookPanel() { return addBookPanel; }
    public BillAccessionPanel getBillAccessionPanel() { return billAccessionPanel; }
    public AccessionRegisterPanel getAccessionRegisterPanel() { return accessionRegisterPanel; }
    public StockPanel getStockPanel() { return stockPanel; }

    public void showSection(String key) {
        if (key == null) return;
        switch (key.trim()) {
            case "ADD":
                cardLayout.show(contentPanel, "ADD");
                break;
            case "BILL":
                cardLayout.show(contentPanel, "BILL");
                break;
            case "REGISTER":
                cardLayout.show(contentPanel, "REGISTER");
                break;
            case "STOCK":
                cardLayout.show(contentPanel, "STOCK");
                break;
            default:
                break;
        }
    }
}
