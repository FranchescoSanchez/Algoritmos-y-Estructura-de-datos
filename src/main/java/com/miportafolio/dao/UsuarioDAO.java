package com.miportafolio.dao;

import com.miportafolio.config.DatabaseConfig;
import com.miportafolio.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class UsuarioDAO {

    public Usuario crear(String nombre, String email, String passwordHash) throws SQLException {
        // Se usa getGeneratedKeys en vez de "RETURNING" para que funcione
        // igual en Postgres (Supabase) y en la base local H2.
        String sql = "INSERT INTO usuarios (nombre, email, password_hash, fecha_registro) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {

            LocalDateTime ahora = LocalDateTime.now();
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setTimestamp(4, Timestamp.valueOf(ahora));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt(1));
                    usuario.setNombre(nombre);
                    usuario.setEmail(email);
                    usuario.setPasswordHash(passwordHash);
                    usuario.setFechaRegistro(ahora);
                    return usuario;
                }
            }
        }
        return null;
    }

    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            usuario.setFechaRegistro(ts.toLocalDateTime());
        }
        return usuario;
    }
}
