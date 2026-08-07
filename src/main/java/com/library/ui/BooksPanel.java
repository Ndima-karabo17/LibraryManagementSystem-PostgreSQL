package com.library.ui;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.model.Author;
import com.library.model.Book;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BooksPanel extends JPanel {

    private final BookDAO bookDAO = new BookDAO();
    private final AuthorDAO authorDAO = new AuthorDAO();

    private final BookTableModel tableModel = new BookTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(15);
    private final JCheckBox availableOnlyBox = new JCheckBox("Available only");
    private final JTextField yearAfterField = new JTextField(5);

    public BooksPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);

        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        refresh();
    }

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.add(new JLabel("Title contains:"));
        bar.add(searchField);
        bar.add(new JLabel("Published after:"));
        yearAfterField.setToolTipText("e.g. 1950");
        bar.add(yearAfterField);
        bar.add(availableOnlyBox);

        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> applyFilters());
        bar.add(searchBtn);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            yearAfterField.setText("");
            availableOnlyBox.setSelected(false);
            refresh();
        });
        bar.add(clearBtn);
        return bar;
    }

    private JComponent buildButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addBtn = new JButton("Add Book");
        addBtn.addActionListener(e -> onAdd());
        bar.add(addBtn);

        JButton editBtn = new JButton("Edit Selected");
        editBtn.addActionListener(e -> onEdit());
        bar.add(editBtn);

        JButton toggleBtn = new JButton("Toggle Available/Borrowed");
        toggleBtn.addActionListener(e -> onToggleAvailability());
        bar.add(toggleBtn);

        JButton addGenreBtn = new JButton("Add Genre");
        addGenreBtn.addActionListener(e -> onAddGenre());
        bar.add(addGenreBtn);

        JButton deleteBtn = new JButton("Delete Selected");
        deleteBtn.addActionListener(e -> onDelete());
        bar.add(deleteBtn);

        JButton setAllAvailBtn = new JButton("Set ALL Available");
        setAllAvailBtn.addActionListener(e -> onSetAllAvailable());
        bar.add(setAllAvailBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        bar.add(refreshBtn);

        return bar;
    }

    private void refresh() {
        try {
            tableModel.setBooks(bookDAO.getAll());
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void applyFilters() {
        try {
            String title = searchField.getText().trim();
            String yearText = yearAfterField.getText().trim();
            boolean availableOnly = availableOnlyBox.isSelected();

            List<Book> result;
            if (!title.isEmpty()) {
                result = bookDAO.searchByTitle(title);
            } else if (!yearText.isEmpty() && availableOnly) {
                result = bookDAO.getAvailableAndPublishedAfter(Integer.parseInt(yearText));
            } else if (!yearText.isEmpty()) {
                result = bookDAO.getPublishedAfter(Integer.parseInt(yearText));
            } else if (availableOnly) {
                result = bookDAO.getAvailable();
            } else {
                result = bookDAO.getAll();
            }
            tableModel.setBooks(result);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Year must be a number.", "Invalid input", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            showError(e);
        }
    }

    private Book selected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a book first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return tableModel.getBookAt(table.convertRowIndexToModel(row));
    }

    private void onAdd() {
        try {
            List<Author> authors = authorDAO.getAll();
            BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this),
                    null, authors, bookDAO.nextId());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                bookDAO.insert(dialog.buildBook());
                refresh();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onEdit() {
        Book book = selected();
        if (book == null) return;
        try {
            List<Author> authors = authorDAO.getAll();
            BookDialog dialog = new BookDialog((Frame) SwingUtilities.getWindowAncestor(this), book, authors, book.getId());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                bookDAO.update(dialog.buildBook());
                refresh();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onToggleAvailability() {
        Book book = selected();
        if (book == null) return;
        try {
            bookDAO.setAvailability(book.getId(), !book.isAvailable());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onAddGenre() {
        Book book = selected();
        if (book == null) return;
        String genre = JOptionPane.showInputDialog(this, "New genre for \"" + book.getTitle() + "\":");
        if (genre == null || genre.trim().isEmpty()) return;
        try {
            bookDAO.addGenre(book.getId(), genre.trim());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onDelete() {
        Book book = selected();
        if (book == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + book.getTitle() + "\"? This also removes its borrowing history.",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            bookDAO.deleteById(book.getId());
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void onSetAllAvailable() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark every book as available? This does not close open loans.",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            bookDAO.setAllAvailable();
            refresh();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(this, e.getMessage(), "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- Table model ----

    private static class BookTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Title", "Author", "Genres", "Year", "Available"};
        private List<Book> books = new ArrayList<>();

        void setBooks(List<Book> books) {
            this.books = books;
            fireTableDataChanged();
        }

        Book getBookAt(int row) { return books.get(row); }

        @Override public int getRowCount() { return books.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Book b = books.get(row);
            switch (col) {
                case 0: return b.getId();
                case 1: return b.getTitle();
                case 2: return b.getAuthorName();
                case 3: return b.getGenres() == null ? "" : String.join(", ", b.getGenres());
                case 4: return b.getPublishedYear();
                case 5: return b.isAvailable() ? "Available" : "Borrowed";
                default: return "";
            }
        }
    }

    // ---- Add/Edit dialog ----

    private static class BookDialog extends JDialog {
        private final JTextField idField = new JTextField(6);
        private final JTextField titleField = new JTextField(20);
        private final JComboBox<Author> authorCombo;
        private final JTextField genresField = new JTextField(20);
        private final JTextField yearField = new JTextField(6);
        private final JCheckBox availableBox = new JCheckBox("Available", true);
        private boolean saved = false;

        BookDialog(Frame owner, Book existing, List<Author> authors, int suggestedId) {
            super(owner, existing == null ? "Add Book" : "Edit Book", true);
            authorCombo = new JComboBox<>(authors.toArray(new Author[0]));

            JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
            form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            idField.setText(String.valueOf(suggestedId));
            idField.setEditable(existing == null);
            form.add(new JLabel("ID:"));
            form.add(idField);

            form.add(new JLabel("Title:"));
            form.add(titleField);

            form.add(new JLabel("Author:"));
            form.add(authorCombo);

            form.add(new JLabel("Genres (comma-separated):"));
            form.add(genresField);

            form.add(new JLabel("Published year:"));
            form.add(yearField);

            form.add(new JLabel("Status:"));
            form.add(availableBox);

            if (existing != null) {
                titleField.setText(existing.getTitle());
                genresField.setText(existing.getGenres() == null ? "" : String.join(", ", existing.getGenres()));
                if (existing.getPublishedYear() != null) yearField.setText(String.valueOf(existing.getPublishedYear()));
                availableBox.setSelected(existing.isAvailable());
                if (existing.getAuthorId() != null) {
                    for (int i = 0; i < authorCombo.getItemCount(); i++) {
                        if (authorCombo.getItemAt(i).getId() == existing.getAuthorId()) {
                            authorCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }

            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                if (titleField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Title is required.");
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

        Book buildBook() {
            Book b = new Book();
            b.setId(Integer.parseInt(idField.getText().trim()));
            b.setTitle(titleField.getText().trim());
            Author author = (Author) authorCombo.getSelectedItem();
            b.setAuthorId(author == null ? null : author.getId());
            String genresText = genresField.getText().trim();
            if (genresText.isEmpty()) {
                b.setGenres(new ArrayList<>());
            } else {
                b.setGenres(Arrays.asList(genresText.split("\\s*,\\s*")));
            }
            String yearText = yearField.getText().trim();
            b.setPublishedYear(yearText.isEmpty() ? null : Integer.parseInt(yearText));
            b.setAvailable(availableBox.isSelected());
            return b;
        }
    }
}
