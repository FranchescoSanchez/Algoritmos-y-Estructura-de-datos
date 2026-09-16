package com.miportafolio.dao;

import com.miportafolio.config.DatabaseConfig;
import com.miportafolio.model.Archivo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArchivoDAO {

    private static final String COLUMNAS =
            "id, usuario_id, titulo, nombre_original, nombre_almacenado, curso, unidad, semana, " +
            "url, tipo_mime, tamano, fecha_subida";

    public Archivo guardar(Archivo archivo) throws SQLException {
        String sql = "INSERT INTO archivos " +
                "(usuario_id, titulo, nombre_original, nombre_almacenado, curso, unidad, semana, " +
                " url, tipo_mime, tamano, fecha_subida) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // getGeneratedKeys funciona igual en Postgres (Supabase) y en H2 local.
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"})) {

            LocalDateTime ahora = LocalDateTime.now();
            ps.setInt(1, archivo.getUsuarioId());
            ps.setString(2, archivo.getTitulo());
            ps.setString(3, archivo.getNombreOriginal());
            ps.setString(4, archivo.getNombreAlmacenado());
            ps.setString(5, archivo.getCurso());
            ps.setInt(6, archivo.getUnidad());
            ps.setInt(7, archivo.getSemana());
            ps.setString(8, archivo.getUrl());
            ps.setString(9, archivo.getTipoMime());
            ps.setLong(10, archivo.getTamano());
            ps.setTimestamp(11, Timestamp.valueOf(ahora));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    archivo.setId(rs.getInt(1));
                }
            }
            archivo.setFechaSubida(ahora);
        }
        return archivo;
    }

    /** Todos los archivos de un curso, ordenados por unidad y semana. */
    public List<Archivo> listarPorCurso(String curso) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM archivos WHERE curso = ? " +
                "ORDER BY unidad, semana, fecha_subida DESC";
        List<Archivo> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, curso);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Agrupa los archivos de un curso con la clave "unidad-semana" (por ejemplo "2-3"),
     * que es como los consume el frontend para pintar cada semana.
     */
    public Map<String, List<Archivo>> agruparPorUnidadSemana(String curso) throws SQLException {
        Map<String, List<Archivo>> mapa = new LinkedHashMap<>();
        for (Archivo archivo : listarPorCurso(curso)) {
            String clave = archivo.getUnidad() + "-" + archivo.getSemana();
            mapa.computeIfAbsent(clave, k -> new ArrayList<>()).add(archivo);
        }
        return mapa;
    }

    public List<Archivo> listarPorSemana(String curso, int unidad, int semana) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM archivos " +
                "WHERE curso = ? AND unidad = ? AND semana = ? ORDER BY fecha_subida DESC";
        List<Archivo> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, curso);
            ps.setInt(2, unidad);
            ps.setInt(3, semana);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Archivo> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM archivos WHERE usuario_id = ? " +
                "ORDER BY curso, unidad, semana, fecha_subida DESC";
        List<Archivo> lista = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Cuenta cuantas semanas distintas de un curso ya tienen al menos un archivo. */
    public int contarSemanasEntregadas(String curso) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT (unidad, semana)) AS total FROM archivos WHERE curso = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, curso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }

    public Optional<Archivo> buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM archivos WHERE id = ?";
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

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM archivos WHERE id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Archivo mapear(ResultSet rs) throws SQLException {
        Archivo archivo = new Archivo();
        archivo.setId(rs.getInt("id"));
        archivo.setUsuarioId(rs.getInt("usuario_id"));
        archivo.setTitulo(rs.getString("titulo"));
        archivo.setNombreOriginal(rs.getString("nombre_original"));
        archivo.setNombreAlmacenado(rs.getString("nombre_almacenado"));
        archivo.setCurso(rs.getString("curso"));
        archivo.setUnidad(rs.getInt("unidad"));
        archivo.setSemana(rs.getInt("semana"));
        archivo.setUrl(rs.getString("url"));
        archivo.setTipoMime(rs.getString("tipo_mime"));
        archivo.setTamano(rs.getLong("tamano"));
        Timestamp ts = rs.getTimestamp("fecha_subida");
        if (ts != null) {
            archivo.setFechaSubida(ts.toLocalDateTime());
        }
        return archivo;
    }
}
