package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class CirculationModulePanel extends JPanel {
    private final IssuePanel issuePanel = new IssuePanel();
    private final ReturnPanel returnPanel = new ReturnPanel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public CirculationModulePanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton navIssue = ModuleTheme.createNavButton("ISSUE BOOK");
        JButton navReturn = ModuleTheme.createNavButton("RETURN BOOK");

        header.add(navIssue);
        header.add(navReturn);

        contentPanel.setBackground(ModuleTheme.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(issuePanel, "ISSUE");
        contentPanel.add(returnPanel, "RETURN");

        navIssue.addActionListener(e -> cardLayout.show(contentPanel, "ISSUE"));
        navReturn.addActionListener(e -> cardLayout.show(contentPanel, "RETURN"));

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "ISSUE");
    }

    public IssuePanel getIssuePanel() { return issuePanel; }
    public ReturnPanel getReturnPanel() { return returnPanel; }
}
