package com.library.ui;

import com.library.dao.PatronDAO;
import com.library.model.Patron;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatronsPanel extends JPanel {

    private final PatronDAO patronDAO = new PatronDAO();
    private final PatronTableModel tableModel = new PatronTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(15);

    public PatronsPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Name contains:"));
        top.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> search());
        top.add(searchBtn);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        top.add(clearBtn);
        add(top, BorderLayout.NORTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Patron");
        addBtn.addActionListener(e -> onAdd());
        bottom.add(addBtn);
        JButton editBtn = new JButton("Edit Selected");
        editBtn.addActionListener(e -> onEdit());
        bottom.add(editBtn);
        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> onDelete());
        bottom.add(deleteBtn);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        try {
            tableModel.setPatrons(patronDAO.getAll());
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void search() {
        String text = searchField.getText().trim();
        try {
            tableModel.setPatrons(text.isEmpty() ? patronDAO.getAll() : patronDAO.searchByName(text));
        } catch (SQLException e) {
            showError(e);
        }
    }

    private Patron selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a patron first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tableModel.getPatronAt(table.convertRowIndexToModel(row));
    }

    private void onAdd() {
        PatronDialog dialog = new PatronDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                patronDAO.insert(dialog.buildPatron());
                refresh();
            } catch (SQLException e) {
                showError(e);
            }
        }
    }

    private void onEdit() {
        Patron p = selected();
        if (p == null) return;
        PatronDialog dialog = new PatronDialog((Frame) SwingUtilities.getWindowAncestor(this), p);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                patronDAO.update(dialog.buildPatron());
                refresh();
            } catch (SQLException e) {
                showError(e);
            }
        }
    }

    private void onDelete() {
        Patron p = selected();
        if (p == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + p.getName() + "\"? This also removes their borrowing history.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            patronDAO.deleteById(p.getId());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static class PatronTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Email"};
        private List<Patron> patrons = new ArrayList<>();

        void setPatrons(List<Patron> patrons) {
            this.patrons = patrons;
            fireTableDataChanged();
        }

        Patron getPatronAt(int row) { return patrons.get(row); }

        @Override public int getRowCount() { return patrons.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Patron p = patrons.get(row);
            switch (col) {
                case 0: return p.getId();
                case 1: return p.getName();
                case 2: return p.getEmail();
                default: return "";
            }
        }
    }

    private static class PatronDialog extends JDialog {
        private final JTextField nameField = new JTextField(20);
        private final JTextField emailField = new JTextField(20);
        private final Patron existing;
        private boolean saved = false;

        PatronDialog(Frame owner, Patron existing) {
            super(owner, existing == null ? "Add Patron" : "Edit Patron", true);
            this.existing = existing;

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            form.add(new JLabel("Name:"));
            form.add(nameField);
            form.add(new JLabel("Email:"));
            form.add(emailField);

            if (existing != null) {
                nameField.setText(existing.getName());
                emailField.setText(existing.getEmail());
            }

            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Name is required.");
                    return;
                }
                saved = true;
                setVisible(false);
            });
            JButton cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(e -> setVisible(false));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.add(saveBtn);
            buttons.add(cancelBtn);

            setLayout(new BorderLayout());
            add(form, BorderLayout.CENTER);
            add(buttons, BorderLayout.SOUTH);
            pack();
            setLocationRelativeTo(owner);
        }

        boolean isSaved() { return saved; }

        Patron buildPatron() {
            Patron p = new Patron();
            p.setId(existing == null ? 0 : existing.getId());
            p.setName(nameField.getText().trim());
            p.setEmail(emailField.getText().trim());
            return p;
        }
    }
}
