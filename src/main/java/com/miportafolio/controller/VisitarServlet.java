package com.miportafolio.controller;

import com.miportafolio.config.DatabaseConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Registra una visita a una pagina del portafolio (contador simple de visitas).
 * Se llama de forma asincrona desde el frontend (JS) al cargar index.jsp.
 */
@WebServlet("/visitar")
public class VisitarServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String ip = req.getRemoteAddr();
        String pagina = req.getParameter("pagina");

        try (Connection con = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO visitas (ip, pagina, fecha) VALUES (?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, ip);
                ps.setString(2, pagina != null ? pagina : "index");
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            // No interrumpe la navegación del usuario si falla el registro de la visita
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"registrado\":true}");
    }
}
