package com.library.dao;

import com.library.db.DatabaseConfig;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookDAO {

    private static final String BASE_SELECT =
        "SELECT b.id, b.title, b.author_id, b.genres, b.published_year, b.available, a.name AS author_name " +
        "FROM Books b LEFT JOIN Authors a ON b.author_id = a.id ";

    public List<Book> getAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY b.id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(map(rs));
        }
        return books;
    }

    public List<Book> searchByTitle(String titleFragment) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.title ILIKE ? ORDER BY b.id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + titleFragment + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(map(rs));
            }
        }
        return books;
    }

    public List<Book> getByAuthor(int authorId) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.author_id = ? ORDER BY b.id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(map(rs));
            }
        }
        return books;
    }

    public List<Book> getAvailable() throws SQLException {
        String sql = BASE_SELECT + "WHERE b.available = TRUE ORDER BY b.id";
        return runQuery(sql);
    }

    public List<Book> getPublishedAfter(int year) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.published_year > ? ORDER BY b.id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(map(rs));
            }
        }
        return books;
    }

    public List<Book> getAvailableAndPublishedAfter(int year) throws SQLException {
        String sql = BASE_SELECT + "WHERE b.available = TRUE AND b.published_year > ? ORDER BY b.id";
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) books.add(map(rs));
            }
        }
        return books;
    }

    private List<Book> runQuery(String sql) throws SQLException {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) books.add(map(rs));
        }
        return books;
    }

    public void insert(Book b) throws SQLException {
        String sql = "INSERT INTO Books (id, title, author_id, genres, published_year, available) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getId());
            ps.setString(2, b.getTitle());
            if (b.getAuthorId() == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, b.getAuthorId());
            Array genresArray = conn.createArrayOf("text", b.getGenres() == null ? new String[0] : b.getGenres().toArray());
            ps.setArray(4, genresArray);
            if (b.getPublishedYear() == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, b.getPublishedYear());
            ps.setBoolean(6, b.isAvailable());
            ps.executeUpdate();
        }
    }

    public void update(Book b) throws SQLException {
        String sql = "UPDATE Books SET title = ?, author_id = ?, genres = ?, published_year = ?, available = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getTitle());
            if (b.getAuthorId() == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, b.getAuthorId());
            Array genresArray = conn.createArrayOf("text", b.getGenres() == null ? new String[0] : b.getGenres().toArray());
            ps.setArray(3, genresArray);
            if (b.getPublishedYear() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, b.getPublishedYear());
            ps.setBoolean(5, b.isAvailable());
            ps.setInt(6, b.getId());
            ps.executeUpdate();
        }
    }

    public void setAvailability(int bookId, boolean available) throws SQLException {
        String sql = "UPDATE Books SET available = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        }
    }

    public void setAllAvailable() throws SQLException {
        String sql = "UPDATE Books SET available = TRUE";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        }
    }

    public void addGenre(int bookId, String genre) throws SQLException {
        String sql = "UPDATE Books SET genres = array_append(genres, ?) WHERE id = ? AND NOT (genres @> ARRAY[?]::text[])";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, genre);
            ps.setInt(2, bookId);
            ps.setString(3, genre);
            ps.executeUpdate();
        }
    }

    public void deleteByTitle(String title) throws SQLException {
        String sql = "DELETE FROM Books WHERE title = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM Books WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM Books";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt("next_id");
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setTitle(rs.getString("title"));
        int authorId = rs.getInt("author_id");
        b.setAuthorId(rs.wasNull() ? null : authorId);
        b.setAuthorName(rs.getString("author_name"));
        Array genresArr = rs.getArray("genres");
        if (genresArr != null) {
            String[] genres = (String[]) genresArr.getArray();
            b.setGenres(new ArrayList<>(Arrays.asList(genres)));
        } else {
            b.setGenres(new ArrayList<>());
        }
        int year = rs.getInt("published_year");
        b.setPublishedYear(rs.wasNull() ? null : year);
        b.setAvailable(rs.getBoolean("available"));
        return b;
    }
}
