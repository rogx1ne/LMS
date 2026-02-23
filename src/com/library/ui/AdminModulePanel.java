package com.library.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

public class AdminModulePanel extends JPanel {
    private final UserManagementPanel userManagementPanel = new UserManagementPanel();
    private final DataImportExportPanel dataImportExportPanel = new DataImportExportPanel();
    private final AuditLogPanel auditLogPanel = new AuditLogPanel();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    public AdminModulePanel() {
        setLayout(new BorderLayout());
        setBackground(ModuleTheme.WHITE);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 40, 15));
        header.setBackground(ModuleTheme.WHITE);
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton navUsers = ModuleTheme.createNavButton("USER MANAGEMENT");
        JButton navData = ModuleTheme.createNavButton("IMPORT / EXPORT");
        JButton navAudit = ModuleTheme.createNavButton("AUDIT LOGS");

        header.add(navUsers);
        header.add(navData);
        header.add(navAudit);

        contentPanel.setBackground(ModuleTheme.WHITE);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPanel.add(userManagementPanel, "USERS");
        contentPanel.add(dataImportExportPanel, "DATA");
        contentPanel.add(auditLogPanel, "AUDIT");

        navUsers.addActionListener(e -> cardLayout.show(contentPanel, "USERS"));
        navData.addActionListener(e -> cardLayout.show(contentPanel, "DATA"));
        navAudit.addActionListener(e -> cardLayout.show(contentPanel, "AUDIT"));

        add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        cardLayout.show(contentPanel, "USERS");
    }

    public UserManagementPanel getUserManagementPanel() { return userManagementPanel; }
    public DataImportExportPanel getDataImportExportPanel() { return dataImportExportPanel; }
    public AuditLogPanel getAuditLogPanel() { return auditLogPanel; }
}
