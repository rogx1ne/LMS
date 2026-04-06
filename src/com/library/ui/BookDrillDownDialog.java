package com.library.ui;

import com.library.model.BookCopyStatusRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BookDrillDownDialog extends JDialog {
    public BookDrillDownDialog(Window owner, String title, String author, int edition, List<BookCopyStatusRow> rows) {
        super(owner, "Copy Details", ModalityType.APPLICATION_MODAL);
        setSize(500, 380);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Title: " + title + " | Author: " + author + " | Edition: " + edition);
        header.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Accession No", "Status", "Withdrawn Date"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (BookCopyStatusRow row : rows) {
            model.addRow(new Object[]{row.getAccessionNo(), row.getStatus(), row.getWithdrawnDate()});
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        foot.add(close);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(foot, BorderLayout.SOUTH);
    }
}
