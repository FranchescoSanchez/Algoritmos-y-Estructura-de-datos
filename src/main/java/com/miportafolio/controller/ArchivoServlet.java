package com.miportafolio.controller;

import com.google.gson.Gson;
import com.miportafolio.config.JsonConfig;
import com.miportafolio.dao.ArchivoDAO;
import com.miportafolio.model.Archivo;
import com.miportafolio.model.Curso;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Devuelve en JSON el contenido de un curso ya organizado en
 * unidades -> semanas -> archivos, listo para pintar en el portafolio publico.
 *
 * GET /archivos?curso=taller-aplicaciones-1
 */
@WebServlet("/archivos")
public class ArchivoServlet extends HttpServlet {

    private final ArchivoDAO archivoDAO = new ArchivoDAO();
    private final Gson gson = JsonConfig.get();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String curso = req.getParameter("curso");
        resp.setContentType("application/json;charset=UTF-8");

        if (!Curso.existe(curso)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Curso no válido\"}");
            return;
        }

        try {
            Map<String, List<Archivo>> porSemana = archivoDAO.agruparPorUnidadSemana(curso);

            // Construimos siempre las 4 unidades x 4 semanas, aunque esten vacias,
            // para que el portafolio muestre la estructura completa del curso.
            List<Map<String, Object>> unidades = new ArrayList<>();

            for (int u = 1; u <= Curso.UNIDADES; u++) {
                List<Map<String, Object>> semanas = new ArrayList<>();
                int entregadasEnUnidad = 0;

                for (int s = 1; s <= Curso.SEMANAS_POR_UNIDAD; s++) {
                    List<Archivo> archivos = porSemana.getOrDefault(u + "-" + s, List.of());
                    if (!archivos.isEmpty()) {
                        entregadasEnUnidad++;
                    }

                    Map<String, Object> semana = new LinkedHashMap<>();
                    semana.put("numero", s);
                    semana.put("archivos", archivos);
                    semanas.add(semana);
                }

                Map<String, Object> unidad = new LinkedHashMap<>();
                unidad.put("numero", u);
                unidad.put("semanasEntregadas", entregadasEnUnidad);
                unidad.put("semanas", semanas);
                unidades.add(unidad);
            }

            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("curso", curso);
            respuesta.put("nombre", Curso.nombreDe(curso));
            respuesta.put("totalSemanas", Curso.totalSemanas());
            respuesta.put("semanasEntregadas", archivoDAO.contarSemanasEntregadas(curso));
            respuesta.put("unidades", unidades);

            resp.getWriter().write(gson.toJson(respuesta));

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("error", e.getMessage())));
        }
    }
}
