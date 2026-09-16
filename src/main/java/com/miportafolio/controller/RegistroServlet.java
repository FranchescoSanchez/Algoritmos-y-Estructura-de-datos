package com.miportafolio.controller;

import com.miportafolio.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/registro")
public class RegistroServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/registro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nombre = req.getParameter("nombre");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            authService.registrar(nombre, email, password);
            req.setAttribute("exito", "Tu cuenta quedó creada. Ya puedes iniciar sesión.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Errores esperables: datos inválidos o base de datos sin configurar
            volverConError(req, resp, e.getMessage(), nombre, email);

        } catch (SQLException e) {
            // Mostramos el detalle real para poder corregir la configuración
            volverConError(req, resp,
                    "No se pudo guardar en la base de datos: " + e.getMessage(), nombre, email);
        }
    }

    private void volverConError(HttpServletRequest req, HttpServletResponse resp,
                                String mensaje, String nombre, String email)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.setAttribute("nombre", nombre);   // conserva lo que ya escribió
        req.setAttribute("email", email);
        req.getRequestDispatcher("/registro.jsp").forward(req, resp);
    }
}
