package com.library.dao;

import com.library.db.DatabaseConfig;
import com.library.model.Patron;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatronDAO {

    public List<Patron> getAll() throws SQLException {
        String sql = "SELECT id, name, email FROM Patrons ORDER BY id";
        List<Patron> patrons = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) patrons.add(map(rs));
        }
        return patrons;
    }

    public List<Patron> searchByName(String nameFragment) throws SQLException {
        String sql = "SELECT id, name, email FROM Patrons WHERE name ILIKE ? ORDER BY id";
        List<Patron> patrons = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + nameFragment + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) patrons.add(map(rs));
            }
        }
        return patrons;
    }

    public void insert(Patron p) throws SQLException {
        String sql = "INSERT INTO Patrons (name, email, borrowed_books) VALUES (?, ?, ARRAY[]::INT[])";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getEmail());
            ps.executeUpdate();
        }
    }

    public void update(Patron p) throws SQLException {
        String sql = "UPDATE Patrons SET name = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getEmail());
            ps.setInt(3, p.getId());
            ps.executeUpdate();
        }
    }

    public void deleteById(int id) throws SQLException {
        String sql = "DELETE FROM Patrons WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Patron map(ResultSet rs) throws SQLException {
        return new Patron(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
    }
}
