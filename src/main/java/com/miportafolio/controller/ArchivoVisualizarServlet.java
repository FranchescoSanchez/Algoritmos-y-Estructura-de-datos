package com.miportafolio.controller;

import com.miportafolio.dao.ArchivoDAO;
import com.miportafolio.model.Archivo;
import com.miportafolio.service.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

/**
 * Muestra el archivo dentro del navegador (inline), util para PDFs e imagenes.
 */
@WebServlet("/visualizar")
public class ArchivoVisualizarServlet extends HttpServlet {

    private final ArchivoDAO archivoDAO = new ArchivoDAO();
    private final StorageService storageService = new StorageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(req.getParameter("id"));
            Optional<Archivo> archivoOpt = archivoDAO.buscarPorId(id);

            if (archivoOpt.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Archivo archivo = archivoOpt.get();
            byte[] datos = storageService.descargarArchivo(archivo.getNombreAlmacenado());

            resp.setContentType(archivo.getTipoMime() != null ? archivo.getTipoMime() : "application/octet-stream");
            resp.setHeader("Content-Disposition", "inline; filename=\"" + archivo.getNombreOriginal() + "\"");
            resp.setContentLength(datos.length);
            resp.getOutputStream().write(datos);

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
