package com.miportafolio.controller;

import com.miportafolio.config.DatabaseConfig;
import com.miportafolio.config.DatabaseInitializer;
import com.miportafolio.config.SupabaseConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * Pagina de diagnostico: abre /estado para ver si la base de datos
 * y Supabase Storage estan bien configurados. Util cuando algo falla.
 */
@WebServlet("/estado")
public class EstadoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>");
        out.println("<title>Estado del sistema</title>");
        out.println("<link rel='stylesheet' href='" + req.getContextPath() + "/css/styles.css'></head><body>");
        out.println("<div class='contenedor' style='max-width:720px'>");
        out.println("<h1 style='margin-top:40px'>Estado del sistema</h1>");
        boolean local = DatabaseConfig.isModoLocal();

        if (local) {
            out.println("<div class='aviso'>Estás en <strong>modo local</strong>: la aplicación creó "
                    + "su propia base de datos en tu equipo, así que ya puedes registrarte y subir "
                    + "tareas sin configurar nada. Cuando quieras publicarla en internet, llena los "
                    + "datos de Supabase en <code>application.properties</code> y la app cambiará "
                    + "sola al modo nube.</div>");
        }

        out.println("<table class='tabla-estado'>");

        fila(out, "Modo", true, local ? "Local (sin configuración)" : "Supabase (nube)");

        // --- Base de datos ---
        String detalleBd;
        boolean bdOk = false;
        try (Connection con = DatabaseConfig.getConnection()) {
            bdOk = con != null && !con.isClosed();
            detalleBd = bdOk ? DatabaseConfig.getDescripcion() : "La conexión se cerró";
        } catch (Exception e) {
            detalleBd = e.getMessage();
        }
        fila(out, "Base de datos", bdOk, detalleBd);

        // --- Tablas ---
        fila(out, "Tablas creadas", DatabaseInitializer.isBaseDatosLista(),
                DatabaseInitializer.isBaseDatosLista()
                        ? "usuarios, archivos, visitas"
                        : String.valueOf(DatabaseInitializer.getUltimoError()));

        // --- Almacenamiento de archivos ---
        if (local) {
            fila(out, "Archivos subidos", true,
                    "Se guardan en " + DatabaseConfig.getCarpetaLocal().resolve("archivos"));
        } else {
            boolean urlOk = esValido(SupabaseConfig.getUrl()) && !SupabaseConfig.getUrl().contains("xxxx");
            fila(out, "URL de Supabase", urlOk, urlOk ? SupabaseConfig.getUrl() : "Sin configurar");

            boolean keyOk = esValido(SupabaseConfig.getServiceKey())
                    && !SupabaseConfig.getServiceKey().startsWith("CAMBIA");
            fila(out, "Clave de Supabase", keyOk, keyOk ? "Configurada" : "Sin configurar");

            fila(out, "Bucket", esValido(SupabaseConfig.getBucket()),
                    String.valueOf(SupabaseConfig.getBucket()));
        }

        out.println("</table>");
        out.println("<p style='margin-top:28px'><a class='btn' href='" + req.getContextPath()
                + "/index.jsp'>Volver al portafolio</a></p>");
        out.println("</div></body></html>");
    }

    private boolean esValido(String valor) {
        return valor != null && !valor.isBlank();
    }

    private void fila(PrintWriter out, String nombre, boolean ok, String detalle) {
        out.println("<tr><td>" + nombre + "</td>"
                + "<td>" + (ok ? "<span class='pin pin-ok'>OK</span>"
                              : "<span class='pin pin-fallo'>Falla</span>") + "</td>"
                + "<td class='detalle'>" + escapar(detalle) + "</td></tr>");
    }

    private String escapar(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
