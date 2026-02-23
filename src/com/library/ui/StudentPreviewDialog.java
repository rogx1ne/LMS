package com.library.ui;

import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.service.StudentPdfService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class StudentPreviewDialog extends JDialog {

    public StudentPreviewDialog(
        Window owner,
        Student student,
        List<BorrowRecord> history,
        StudentPdfService pdfService,
        Runnable onEdit
    ) {
        super(owner, "Student Preview - " + student.getCardId(), ModalityType.APPLICATION_MODAL);

        setSize(900, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(12, 12, 12, 12));
        JLabel title = new JLabel(student.getName() + " (" + student.getCardId() + ")");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnReceipt = new JButton("Download Receipt (PDF)");
        JButton btnCard = new JButton("Download Library Card (PDF)");
        JButton btnEdit = new JButton("Edit");
        JButton btnClose = new JButton("Close");
        actions.add(btnReceipt);
        actions.add(btnCard);
        actions.add(btnEdit);
        actions.add(btnClose);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.setBorder(new EmptyBorder(0, 12, 12, 12));

        center.add(createProfilePanel(student));
        center.add(createHistoryPanel(history));

        add(center, BorderLayout.CENTER);

        btnClose.addActionListener(e -> dispose());
        btnEdit.addActionListener(e -> {
            dispose();
            if (onEdit != null) onEdit.run();
        });

        btnReceipt.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(student.getReceiptNo() + "_Receipt.pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    pdfService.generateReceiptPdf(student, fc.getSelectedFile().toPath());
                    JOptionPane.showMessageDialog(this, "Receipt PDF saved.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });

        btnCard.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(student.getCardId() + "_LibraryCard.pdf"));
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    pdfService.generateLibraryCardPdf(student, fc.getSelectedFile().toPath());
                    JOptionPane.showMessageDialog(this, "Library card PDF saved.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            }
        });
    }

    private JPanel createProfilePanel(Student s) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createTitledBorder("Profile"));

        p.add(row("Card ID", s.getCardId()));
        p.add(row("Receipt No", s.getReceiptNo()));
        p.add(row("Name", s.getName()));
        p.add(row("Course", s.getCourse()));
        p.add(row("Session", s.getSession()));
        p.add(row("Roll No", String.valueOf(s.getRoll())));
        p.add(row("Contact", String.valueOf(s.getPhone())));
        p.add(row("Book Limit", String.valueOf(s.getBookLimit())));
        p.add(row("Fee", String.valueOf(s.getFee())));
        p.add(row("Issued By", s.getIssuedBy()));
        p.add(row("Issue Date", String.valueOf(s.getIssueDate())));
        p.add(row("Status", s.getStatus()));

        JTextArea addr = new JTextArea(s.getAddress());
        addr.setLineWrap(true);
        addr.setWrapStyleWord(true);
        addr.setEditable(false);
        addr.setBackground(p.getBackground());
        addr.setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel addrWrap = new JPanel(new BorderLayout());
        addrWrap.setBorder(BorderFactory.createTitledBorder("Address"));
        addrWrap.add(new JScrollPane(addr), BorderLayout.CENTER);
        p.add(Box.createVerticalStrut(8));
        p.add(addrWrap);
        return p;
    }

    private JPanel createHistoryPanel(List<BorrowRecord> history) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Borrowing History"));

        String[] cols = {"Access No", "Title", "Author", "Issued", "Due", "Returned", "Fine"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        if (history != null) {
            for (BorrowRecord r : history) {
                model.addRow(new Object[]{
                    r.getAccessNo(),
                    r.getBookTitle(),
                    r.getAuthorName(),
                    r.getIssueDate(),
                    r.getDueDate(),
                    r.getReturnDate(),
                    r.getFineAmount()
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private JPanel row(String k, String v) {
        JPanel r = new JPanel(new BorderLayout());
        JLabel lk = new JLabel(k + ": ");
        lk.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel lv = new JLabel(v == null ? "" : v);
        lv.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        r.add(lk, BorderLayout.WEST);
        r.add(lv, BorderLayout.CENTER);
        r.setBorder(new EmptyBorder(4, 8, 4, 8));
        return r;
    }
}
