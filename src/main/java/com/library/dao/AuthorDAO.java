package com.library.dao;

import com.library.db.DatabaseConfig;
import com.library.model.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {

    public List<Author> getAll() throws SQLException {
        String sql = "SELECT * FROM Authors ORDER BY id";
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                authors.add(map(rs));
            }
        }
        return authors;
    }

    public List<Author> searchByName(String nameFragment) throws SQLException {
        String sql = "SELECT * FROM Authors WHERE name ILIKE ? ORDER BY id";
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameFragment + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) authors.add(map(rs));
            }
        }
        return authors;
    }

    public void insert(Author a) throws SQLException {
        String sql = "INSERT INTO Authors (id, name, nationality, birth_year, death_year) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getId());
            ps.setString(2, a.getName());
            ps.setString(3, a.getNationality());
            setNullableInt(ps, 4, a.getBirthYear());
            setNullableInt(ps, 5, a.getDeathYear());
            ps.executeUpdate();
        }
    }

    public void update(Author a) throws SQLException {
        String sql = "UPDATE Authors SET name = ?, nationality = ?, birth_year = ?, death_year = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getName());
            ps.setString(2, a.getNationality());
            setNullableInt(ps, 3, a.getBirthYear());
            setNullableInt(ps, 4, a.getDeathYear());
            ps.setInt(5, a.getId());
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM Authors WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int nextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS next_id FROM Authors";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt("next_id");
        }
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private Author map(ResultSet rs) throws SQLException {
        Author a = new Author();
        a.setId(rs.getInt("id"));
        a.setName(rs.getString("name"));
        a.setNationality(rs.getString("nationality"));
        int birth = rs.getInt("birth_year");
        a.setBirthYear(rs.wasNull() ? null : birth);
        int death = rs.getInt("death_year");
        a.setDeathYear(rs.wasNull() ? null : death);
        return a;
    }
}
