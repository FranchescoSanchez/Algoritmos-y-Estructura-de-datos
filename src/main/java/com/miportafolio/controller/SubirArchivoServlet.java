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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@WebServlet("/subir")
@MultipartConfig(maxFileSize = 25 * 1024 * 1024, maxRequestSize = 150 * 1024 * 1024) // 25 MB por archivo, hasta ~6 archivos por vez
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

        Collection<Part> todasLasPartes;
        try {
            todasLasPartes = req.getParts();
        } catch (Exception e) {
            redirigirConMensaje(req, resp, "error", "No se pudo leer la solicitud: " + e.getMessage());
            return;
        }

        List<Part> partesArchivo = new ArrayList<>();
        for (Part p : todasLasPartes) {
            if ("archivo".equals(p.getName()) && p.getSize() > 0) {
                partesArchivo.add(p);
            }
        }

        if (partesArchivo.isEmpty()) {
            redirigirConMensaje(req, resp, "error", "Elige al menos un archivo para subir.");
            return;
        }

        int subidos = 0;
        List<String> fallidos = new ArrayList<>();
        String ultimoNombre = null;

        for (Part parte : partesArchivo) {
            String nombreOriginal = obtenerNombreArchivo(parte);
            String tipoMime = parte.getContentType();

            try (InputStream is = parte.getInputStream()) {
                byte[] datos = is.readAllBytes();

                // Carpeta en Storage: curso/unidad-N/semana-N/archivo.ext
                String carpeta = curso + "/unidad-" + unidad + "/semana-" + semana;
                String nombreAlmacenado = storageService.subirArchivo(datos, nombreOriginal, tipoMime, carpeta);

                String tituloFinal;
                if (titulo != null && !titulo.isBlank()) {
                    tituloFinal = partesArchivo.size() > 1
                            ? titulo.trim() + " – " + nombreOriginal
                            : titulo.trim();
                } else {
                    tituloFinal = nombreOriginal;
                }

                Archivo archivo = new Archivo();
                archivo.setUsuarioId(usuario.getId());
                archivo.setTitulo(tituloFinal);
                archivo.setNombreOriginal(nombreOriginal);
                archivo.setNombreAlmacenado(nombreAlmacenado);
                archivo.setCurso(curso);
                archivo.setUnidad(unidad);
                archivo.setSemana(semana);
                archivo.setUrl(storageService.obtenerUrlPublica(nombreAlmacenado));
                archivo.setTipoMime(tipoMime);
                archivo.setTamano(datos.length);

                archivoDAO.guardar(archivo);
                subidos++;
                ultimoNombre = nombreOriginal;

            } catch (Exception e) {
                fallidos.add(nombreOriginal);
            }
        }

        if (subidos == 0) {
            redirigirConMensaje(req, resp, "error", "No se pudo subir ningún archivo: " + fallidos);
            return;
        }

        String mensaje;
        if (subidos == 1 && fallidos.isEmpty()) {
            mensaje = "Se subió \"" + ultimoNombre + "\" a la Unidad " + unidad + ", Semana " + semana + ".";
        } else {
            mensaje = "Se subieron " + subidos + " archivo(s) a la Unidad " + unidad + ", Semana " + semana + ".";
            if (!fallidos.isEmpty()) {
                mensaje += " No se pudieron subir: " + String.join(", ", fallidos) + ".";
            }
        }
        redirigirConMensaje(req, resp, fallidos.isEmpty() ? "exito" : "error", mensaje);
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
