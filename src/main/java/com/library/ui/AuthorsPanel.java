package com.library.ui;

import com.library.dao.AuthorDAO;
import com.library.model.Author;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthorsPanel extends JPanel {

    private final AuthorDAO authorDAO = new AuthorDAO();
    private final AuthorTableModel tableModel = new AuthorTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(15);

    public AuthorsPanel() {
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
        JButton addBtn = new JButton("Add Author");
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
            tableModel.setAuthors(authorDAO.getAll());
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void search() {
        String text = searchField.getText().trim();
        try {
            tableModel.setAuthors(text.isEmpty() ? authorDAO.getAll() : authorDAO.searchByName(text));
        } catch (SQLException e) {
            showError(e);
        }
    }

    private Author selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an author first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tableModel.getAuthorAt(table.convertRowIndexToModel(row));
    }

    private void onAdd() {
        try {
            AuthorDialog dialog = new AuthorDialog((Frame) SwingUtilities.getWindowAncestor(this), null, authorDAO.nextId());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                authorDAO.insert(dialog.buildAuthor());
                refresh();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onEdit() {
        Author a = selected();
        if (a == null) return;
        AuthorDialog dialog = new AuthorDialog((Frame) SwingUtilities.getWindowAncestor(this), a, a.getId());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            try {
                authorDAO.update(dialog.buildAuthor());
                refresh();
            } catch (SQLException e) {
                showError(e);
            }
        }
    }

    private void onDelete() {
        Author a = selected();
        if (a == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + a.getName() + "\"? Books by this author will have their author unset.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            authorDAO.deleteById(a.getId());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    private static class AuthorTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Nationality", "Birth Year", "Death Year"};
        private List<Author> authors = new ArrayList<>();

        void setAuthors(List<Author> authors) {
            this.authors = authors;
            fireTableDataChanged();
        }

        Author getAuthorAt(int row) { return authors.get(row); }

        @Override public int getRowCount() { return authors.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Author a = authors.get(row);
            switch (col) {
                case 0: return a.getId();
                case 1: return a.getName();
                case 2: return a.getNationality();
                case 3: return a.getBirthYear();
                case 4: return a.getDeathYear();
                default: return "";
            }
        }
    }

    private static class AuthorDialog extends JDialog {
        private final JTextField idField = new JTextField(6);
        private final JTextField nameField = new JTextField(20);
        private final JTextField nationalityField = new JTextField(20);
        private final JTextField birthField = new JTextField(6);
        private final JTextField deathField = new JTextField(6);
        private boolean saved = false;

        AuthorDialog(Frame owner, Author existing, int suggestedId) {
            super(owner, existing == null ? "Add Author" : "Edit Author", true);

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            idField.setText(String.valueOf(suggestedId));
            idField.setEditable(existing == null);
            form.add(new JLabel("ID:"));
            form.add(idField);
            form.add(new JLabel("Name:"));
            form.add(nameField);
            form.add(new JLabel("Nationality:"));
            form.add(nationalityField);
            form.add(new JLabel("Birth year:"));
            form.add(birthField);
            form.add(new JLabel("Death year (blank if living):"));
            form.add(deathField);

            if (existing != null) {
                nameField.setText(existing.getName());
                nationalityField.setText(existing.getNationality());
                if (existing.getBirthYear() != null) birthField.setText(String.valueOf(existing.getBirthYear()));
                if (existing.getDeathYear() != null) deathField.setText(String.valueOf(existing.getDeathYear()));
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

        Author buildAuthor() {
            Author a = new Author();
            a.setId(Integer.parseInt(idField.getText().trim()));
            a.setName(nameField.getText().trim());
            a.setNationality(nationalityField.getText().trim());
            String birth = birthField.getText().trim();
            String death = deathField.getText().trim();
            a.setBirthYear(birth.isEmpty() ? null : Integer.parseInt(birth));
            a.setDeathYear(death.isEmpty() ? null : Integer.parseInt(death));
            return a;
        }
    }
}
