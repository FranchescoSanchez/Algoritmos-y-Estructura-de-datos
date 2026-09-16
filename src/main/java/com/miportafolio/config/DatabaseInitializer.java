package com.miportafolio.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Se ejecuta al arrancar la aplicacion y crea las tablas si no existen.
 * Asi no hace falta correr el SQL a mano en Supabase.
 *
 * Tambien guarda el estado de la conexion para poder mostrar un mensaje
 * util (en vez de un generico "error del servidor") cuando falla.
 */
@WebListener
public class DatabaseInitializer implements ServletContextListener {

    private static boolean baseDatosLista = false;
    private static String ultimoError = null;

    private static final String SQL_USUARIOS = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id              SERIAL PRIMARY KEY,
                nombre          VARCHAR(150) NOT NULL,
                email           VARCHAR(150) UNIQUE NOT NULL,
                password_hash   TEXT NOT NULL,
                fecha_registro  TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """;

    private static final String SQL_ARCHIVOS = """
            CREATE TABLE IF NOT EXISTS archivos (
                id                SERIAL PRIMARY KEY,
                usuario_id        INTEGER REFERENCES usuarios(id) ON DELETE CASCADE,
                titulo            VARCHAR(200),
                nombre_original   VARCHAR(255) NOT NULL,
                nombre_almacenado VARCHAR(255) NOT NULL,
                curso             VARCHAR(80)  NOT NULL,
                unidad            INTEGER      NOT NULL CHECK (unidad BETWEEN 1 AND 4),
                semana            INTEGER      NOT NULL CHECK (semana BETWEEN 1 AND 4),
                url               TEXT NOT NULL,
                tipo_mime         VARCHAR(120),
                tamano            BIGINT,
                fecha_subida      TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """;

    private static final String SQL_VISITAS = """
            CREATE TABLE IF NOT EXISTS visitas (
                id      SERIAL PRIMARY KEY,
                ip      VARCHAR(60),
                pagina  VARCHAR(100),
                fecha   TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """;

    private static final String SQL_INDICE =
            "CREATE INDEX IF NOT EXISTS idx_archivos_ubicacion ON archivos (curso, unidad, semana)";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement()) {

            st.execute(SQL_USUARIOS);
            st.execute(SQL_ARCHIVOS);
            st.execute(SQL_VISITAS);
            st.execute(SQL_INDICE);

            baseDatosLista = true;
            ultimoError = null;
            System.out.println("[MiPortafolio] Base de datos lista -> " + DatabaseConfig.getDescripcion());

            if (DatabaseConfig.isModoLocal()) {
                System.out.println("[MiPortafolio] Modo LOCAL: no hay credenciales de Supabase, "
                        + "así que se usa una base de datos en tu equipo. Ya puedes registrarte.");
                System.out.println("[MiPortafolio] Tus datos y archivos quedan en: "
                        + DatabaseConfig.getCarpetaLocal());
            }

        } catch (SQLException e) {
            baseDatosLista = false;
            ultimoError = e.getMessage();
            System.err.println("[MiPortafolio] No se pudo conectar a la base de datos: " + e.getMessage());
            if (DatabaseConfig.isModoLocal()) {
                System.err.println("[MiPortafolio] Estaba usando la base local en "
                        + DatabaseConfig.getCarpetaLocal()
                        + ". Revisa que esa carpeta tenga permisos de escritura.");
            } else {
                System.err.println("[MiPortafolio] Revisa DB_HOST, DB_USER y DB_PASSWORD "
                        + "(o src/main/resources/application.properties). Recuerda usar el "
                        + "\"Session pooler\" de Supabase, no la conexión directa.");
            }
        }
    }

    public static boolean isBaseDatosLista() {
        return baseDatosLista;
    }

    public static String getUltimoError() {
        return ultimoError;
    }
}
