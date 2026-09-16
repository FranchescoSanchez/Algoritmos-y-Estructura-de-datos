package com.miportafolio.controller;

import com.miportafolio.dao.ArchivoDAO;
import com.miportafolio.model.Archivo;
import com.miportafolio.model.Curso;
import com.miportafolio.model.Usuario;
import com.miportafolio.service.StorageService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/subir")
@MultipartConfig(maxFileSize = 25 * 1024 * 1024, maxRequestSize = 30 * 1024 * 1024) // 25 MB por archivo
public class SubirArchivoServlet extends HttpServlet {

    private final StorageService storageService = new StorageService();
    private final ArchivoDAO archivoDAO = new ArchivoDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession sesion = req.getSession(false);
        Usuario usuario = sesion != null ? (Usuario) sesion.getAttribute("usuario") : null;

        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String curso = req.getParameter("curso");
        String titulo = req.getParameter("titulo");

        int unidad;
        int semana;
        try {
            unidad = Integer.parseInt(req.getParameter("unidad"));
            semana = Integer.parseInt(req.getParameter("semana"));
        } catch (NumberFormatException e) {
            redirigirConMensaje(req, resp, "error", "Selecciona la unidad y la semana.");
            return;
        }

        if (!Curso.existe(curso)) {
            redirigirConMensaje(req, resp, "error", "Selecciona un curso válido.");
            return;
        }
        if (unidad < 1 || unidad > Curso.UNIDADES || semana < 1 || semana > Curso.SEMANAS_POR_UNIDAD) {
            redirigirConMensaje(req, resp, "error", "La unidad y la semana deben estar entre 1 y 4.");
            return;
        }

        Part parte = req.getPart("archivo");
        if (parte == null || parte.getSize() == 0) {
            redirigirConMensaje(req, resp, "error", "Elige un archivo para subir.");
            return;
        }

        String nombreOriginal = obtenerNombreArchivo(parte);
        String tipoMime = parte.getContentType();

        try (InputStream is = parte.getInputStream()) {
            byte[] datos = is.readAllBytes();

            // Carpeta en Storage: curso/unidad-N/semana-N/archivo.ext
            String carpeta = curso + "/unidad-" + unidad + "/semana-" + semana;
            String nombreAlmacenado = storageService.subirArchivo(datos, nombreOriginal, tipoMime, carpeta);

            Archivo archivo = new Archivo();
            archivo.setUsuarioId(usuario.getId());
            archivo.setTitulo(titulo != null && !titulo.isBlank() ? titulo.trim() : nombreOriginal);
            archivo.setNombreOriginal(nombreOriginal);
            archivo.setNombreAlmacenado(nombreAlmacenado);
            archivo.setCurso(curso);
            archivo.setUnidad(unidad);
            archivo.setSemana(semana);
            archivo.setUrl(storageService.obtenerUrlPublica(nombreAlmacenado));
            archivo.setTipoMime(tipoMime);
            archivo.setTamano(datos.length);

            archivoDAO.guardar(archivo);

            redirigirConMensaje(req, resp, "exito",
                    "Se subió \"" + nombreOriginal + "\" a la Unidad " + unidad + ", Semana " + semana + ".");

        } catch (Exception e) {
            redirigirConMensaje(req, resp, "error", "No se pudo subir el archivo: " + e.getMessage());
        }
    }

    private void redirigirConMensaje(HttpServletRequest req, HttpServletResponse resp,
                                     String tipo, String mensaje) throws IOException {
        // Patron POST-Redirect-GET: evita que se reenvie el formulario al recargar
        resp.sendRedirect(req.getContextPath() + "/dashboard.jsp?" + tipo + "="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }

    private String obtenerNombreArchivo(Part parte) {
        String header = parte.getHeader("content-disposition");
        if (header == null) return "archivo";
        for (String token : header.split(";")) {
            if (token.trim().startsWith("filename")) {
                String nombre = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                // Algunos navegadores envian la ruta completa
                int barra = Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\'));
                return barra >= 0 ? nombre.substring(barra + 1) : nombre;
            }
        }
        return "archivo";
    }
}
