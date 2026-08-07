package com.library.ui;

import com.library.dao.BookDAO;
import com.library.dao.BorrowingDAO;
import com.library.dao.PatronDAO;
import com.library.model.Book;
import com.library.model.BorrowingRecord;
import com.library.model.Patron;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingPanel extends JPanel {

    private final BorrowingDAO borrowingDAO = new BorrowingDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final PatronDAO patronDAO = new PatronDAO();

    private final LoanTableModel tableModel = new LoanTableModel();
    private final JTable table = new JTable(tableModel);
    private final JRadioButton activeOnlyBtn = new JRadioButton("Active loans", true);
    private final JRadioButton overdueOnlyBtn = new JRadioButton("Overdue only");
    private final JRadioButton allHistoryBtn = new JRadioButton("Full history");

    public BorrowingPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildViewSelector(), BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setDefaultRenderer(Object.class, new OverdueRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JComponent buildViewSelector() {
        ButtonGroup group = new ButtonGroup();
        group.add(activeOnlyBtn);
        group.add(overdueOnlyBtn);
        group.add(allHistoryBtn);

        activeOnlyBtn.addActionListener(e -> refresh());
        overdueOnlyBtn.addActionListener(e -> refresh());
        allHistoryBtn.addActionListener(e -> refresh());

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(activeOnlyBtn);
        bar.add(overdueOnlyBtn);
        bar.add(allHistoryBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        bar.add(refreshBtn);
        return bar;
    }

    private JComponent buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton borrowBtn = new JButton("Borrow a Book...");
        borrowBtn.addActionListener(e -> onBorrow());
        bar.add(borrowBtn);

        JButton returnBtn = new JButton("Return Selected");
        returnBtn.addActionListener(e -> onReturn());
        bar.add(returnBtn);

        return bar;
    }

    private void refresh() {
        try {
            List<BorrowingRecord> records;
            if (overdueOnlyBtn.isSelected()) {
                records = borrowingDAO.getOverdue();
            } else if (allHistoryBtn.isSelected()) {
                records = borrowingDAO.getAll();
            } else {
                records = borrowingDAO.getActive();
            }
            tableModel.setRecords(records);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onBorrow() {
        try {
            List<Book> availableBooks = bookDAO.getAvailable();
            List<Patron> patrons = patronDAO.getAll();
            if (availableBooks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No books are currently available.", "Nothing to borrow", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            BorrowDialog dialog = new BorrowDialog((Frame) SwingUtilities.getWindowAncestor(this), availableBooks, patrons);
            dialog.setVisible(true);
            if (dialog.isConfirmed()) {
                borrowingDAO.borrowBook(dialog.getSelectedBookId(), dialog.getSelectedPatronId(), dialog.getLoanDays());
                refresh();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onReturn() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a loan to return.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BorrowingRecord record = tableModel.getRecordAt(table.convertRowIndexToModel(row));
        if (record.isReturned()) {
            JOptionPane.showMessageDialog(this, "That loan is already closed.", "Already returned", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            borrowingDAO.returnBook(record.getId());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Table model ----

    private static class LoanTableModel extends AbstractTableModel {
        private final String[] columns = {"Loan ID", "Book", "Patron", "Borrowed", "Due", "Returned", "Status"};
        private List<BorrowingRecord> records = new ArrayList<>();

        void setRecords(List<BorrowingRecord> records) {
            this.records = records;
            fireTableDataChanged();
        }

        BorrowingRecord getRecordAt(int row) { return records.get(row); }

        @Override public int getRowCount() { return records.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            BorrowingRecord r = records.get(row);
            switch (col) {
                case 0: return r.getId();
                case 1: return r.getBookTitle();
                case 2: return r.getPatronName();
                case 3: return r.getBorrowDate();
                case 4: return r.getDueDate();
                case 5: return r.getReturnDate() == null ? "-" : r.getReturnDate();
                case 6:
                    if (r.isReturned()) return "Returned";
                    return r.getDueDate() != null && r.getDueDate().isBefore(LocalDate.now()) ? "OVERDUE" : "Out";
                default: return "";
            }
        }
    }

    /** Highlights overdue, still-open loans in a light red. */
    private class OverdueRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                         boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            int modelRow = tbl.convertRowIndexToModel(row);
            BorrowingRecord r = tableModel.getRecordAt(modelRow);
            boolean overdue = !r.isReturned() && r.getDueDate() != null && r.getDueDate().isBefore(LocalDate.now());
            if (!isSelected) {
                c.setBackground(overdue ? new Color(255, 220, 220) : Color.WHITE);
            }
            return c;
        }
    }

    // ---- Borrow dialog ----

    private static class BorrowDialog extends JDialog {
        private final JComboBox<Book> bookCombo;
        private final JComboBox<Patron> patronCombo;
        private final JTextField loanDaysField = new JTextField("14", 4);
        private boolean confirmed = false;

        BorrowDialog(Frame owner, List<Book> books, List<Patron> patrons) {
            super(owner, "Borrow a Book", true);
            bookCombo = new JComboBox<>(books.toArray(new Book[0]));
            patronCombo = new JComboBox<>(patrons.toArray(new Patron[0]));

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            form.add(new JLabel("Book:"));
            form.add(bookCombo);
            form.add(new JLabel("Patron:"));
            form.add(patronCombo);
            form.add(new JLabel("Loan length (days):"));
            form.add(loanDaysField);

            JButton confirmBtn = new JButton("Borrow");
            confirmBtn.addActionListener(e -> {
                try {
                    Integer.parseInt(loanDaysField.getText().trim());
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Loan length must be a number.");
                    return;
                }
                confirmed = true;
                setVisible(false);
            });
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> setVisible(false));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(confirmBtn);
            buttons.add(cancelBtn);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(owner);
        }

        boolean isConfirmed() { return confirmed; }
        int getSelectedBookId() { return ((Book) bookCombo.getSelectedItem()).getId(); }
        int getSelectedPatronId() { return ((Patron) patronCombo.getSelectedItem()).getId(); }
        int getLoanDays() { return Integer.parseInt(loanDaysField.getText().trim()); }
    }
}
