package com.library.dao;

import com.library.db.DatabaseConfig;
import com.library.model.BorrowingRecord;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowingDAO {

    private static final String BASE_SELECT =
        "SELECT r.id, r.book_id, b.title AS book_title, r.patron_id, p.name AS patron_name, " +
        "r.borrow_date, r.due_date, r.return_date, r.returned " +
        "FROM BorrowingRecords r " +
        "JOIN Books b ON r.book_id = b.id " +
        "JOIN Patrons p ON r.patron_id = p.id ";

    /** All currently-open (not yet returned) loans. */
    public List<BorrowingRecord> getActive() throws SQLException {
        String sql = BASE_SELECT + "WHERE r.returned = FALSE ORDER BY r.due_date";
        return runQuery(sql);
    }

    /** Full history, most recent first. */
    public List<BorrowingRecord> getAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY r.id DESC";
        return runQuery(sql);
    }

    public List<BorrowingRecord> getOverdue() throws SQLException {
        String sql = BASE_SELECT + "WHERE r.returned = FALSE AND r.due_date < CURRENT_DATE ORDER BY r.due_date";
        return runQuery(sql);
    }

    private List<BorrowingRecord> runQuery(String sql) throws SQLException {
        List<BorrowingRecord> records = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) records.add(map(rs));
        }
        return records;
    }

    /**
     * Borrows a book: inserts a BorrowingRecords row, marks the book
     * unavailable, and appends the book id to the patron's borrowed_books
     * array. Runs as a single transaction so the book/patron/record state
     * never drifts out of sync.
     */
    public void borrowBook(int bookId, int patronId, int loanDays) throws SQLException {
        String checkAvailableSql = "SELECT available FROM Books WHERE id = ? FOR UPDATE";
        String insertRecordSql = "INSERT INTO BorrowingRecords (book_id, patron_id, borrow_date, due_date, returned) " +
                                  "VALUES (?, ?, CURRENT_DATE, CURRENT_DATE + (? || ' days')::interval, FALSE)";
        String markUnavailableSql = "UPDATE Books SET available = FALSE WHERE id = ?";
        String appendToPatronSql = "UPDATE Patrons SET borrowed_books = array_append(borrowed_books, ?) WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean available;
                try (PreparedStatement ps = conn.prepareStatement(checkAvailableSql)) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Book id " + bookId + " does not exist.");
                        available = rs.getBoolean("available");
                    }
                }
                if (!available) {
                    throw new SQLException("Book id " + bookId + " is already checked out.");
                }

                try (PreparedStatement ps = conn.prepareStatement(insertRecordSql)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, patronId);
                    ps.setInt(3, loanDays);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(markUnavailableSql)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(appendToPatronSql)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, patronId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Returns a book: closes the open BorrowingRecords row, marks the book
     * available again, and removes the book id from the patron's
     * borrowed_books array.
     */
    public void returnBook(int recordId) throws SQLException {
        String getRecordSql = "SELECT book_id, patron_id FROM BorrowingRecords WHERE id = ? AND returned = FALSE";
        String closeRecordSql = "UPDATE BorrowingRecords SET returned = TRUE, return_date = CURRENT_DATE WHERE id = ?";
        String markAvailableSql = "UPDATE Books SET available = TRUE WHERE id = ?";
        String removeFromPatronSql = "UPDATE Patrons SET borrowed_books = array_remove(borrowed_books, ?) WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId, patronId;
                try (PreparedStatement ps = conn.prepareStatement(getRecordSql)) {
                    ps.setInt(1, recordId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("No open loan with record id " + recordId);
                        bookId = rs.getInt("book_id");
                        patronId = rs.getInt("patron_id");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(closeRecordSql)) {
                    ps.setInt(1, recordId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(markAvailableSql)) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(removeFromPatronSql)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, patronId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private BorrowingRecord map(ResultSet rs) throws SQLException {
        BorrowingRecord r = new BorrowingRecord();
        r.setId(rs.getInt("id"));
        r.setBookId(rs.getInt("book_id"));
        r.setBookTitle(rs.getString("book_title"));
        r.setPatronId(rs.getInt("patron_id"));
        r.setPatronName(rs.getString("patron_name"));
        Date bd = rs.getDate("borrow_date");
        r.setBorrowDate(bd == null ? null : bd.toLocalDate());
        Date dd = rs.getDate("due_date");
        r.setDueDate(dd == null ? null : dd.toLocalDate());
        Date rd = rs.getDate("return_date");
        r.setReturnDate(rd == null ? null : rd.toLocalDate());
        r.setReturned(rs.getBoolean("returned"));
        return r;
    }
}
