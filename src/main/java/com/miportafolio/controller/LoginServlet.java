package com.miportafolio.controller;

import com.miportafolio.model.Usuario;
import com.miportafolio.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            Usuario usuario = authService.login(email, password);

            if (usuario != null) {
                HttpSession sesion = req.getSession(true);
                sesion.setAttribute("usuario", usuario);
                resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
                return;
            }
            volverConError(req, resp, "El correo o la contraseña no coinciden.", email);

        } catch (IllegalStateException e) {
            volverConError(req, resp, e.getMessage(), email);
        } catch (SQLException e) {
            volverConError(req, resp, "No se pudo consultar la base de datos: " + e.getMessage(), email);
        }
    }

    private void volverConError(HttpServletRequest req, HttpServletResponse resp,
                                String mensaje, String email)
            throws ServletException, IOException {
        req.setAttribute("error", mensaje);
        req.setAttribute("email", email);
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
