package com.miportafolio.controller;

import com.miportafolio.dao.ArchivoDAO;
import com.miportafolio.model.Archivo;
import com.miportafolio.model.Usuario;
import com.miportafolio.service.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/eliminar")
public class EliminarArchivoServlet extends HttpServlet {

    private final ArchivoDAO archivoDAO = new ArchivoDAO();
    private final StorageService storageService = new StorageService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion != null ? (Usuario) sesion.getAttribute("usuario") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Optional<Archivo> archivoOpt = archivoDAO.buscarPorId(id);

            // Solo el dueño del archivo puede eliminarlo
            if (archivoOpt.isPresent() && archivoOpt.get().getUsuarioId() == usuario.getId()) {
                Archivo archivo = archivoOpt.get();
                storageService.eliminarArchivo(archivo.getNombreAlmacenado());
                archivoDAO.eliminar(id);
            }

            resp.sendRedirect(req.getContextPath() + "/dashboard.jsp");
        } catch (Exception e) {
            req.setAttribute("error", "No se pudo eliminar el archivo.");
            req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
        }
    }
}
