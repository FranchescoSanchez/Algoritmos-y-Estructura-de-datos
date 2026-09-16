package com.miportafolio.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Decide a que base de datos conectarse.
 *
 * 1) Si hay credenciales reales de Supabase (por variables de entorno o por
 *    application.properties), usa Postgres en la nube.
 * 2) Si NO las hay, usa automaticamente una base de datos local H2 guardada
 *    en la carpeta del usuario. Asi la aplicacion funciona de inmediato,
 *    sin configurar nada.
 *
 * Variables de entorno (tienen prioridad): DB_HOST, DB_PORT, DB_NAME,
 * DB_USER, DB_PASSWORD.
 */
public class DatabaseConfig {

    private static final Properties props = new Properties();

    /** Valores de ejemplo que NO cuentan como configuracion real. */
    private static final String[] PLACEHOLDERS = {
            "CAMBIA_ESTA_CLAVE", "xxxxxxxxxxxxxxxx", "xxxxxxxxxxxxxxxxxxxx",
            "TU_CLAVE", "tu-proyecto"
    };

    private static boolean modoLocal = false;

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer application.properties", e);
        }

        modoLocal = !hayConfiguracionSupabase();

        try {
            if (modoLocal) {
                Class.forName("org.h2.Driver");
            } else {
                Class.forName("org.postgresql.Driver");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Falta el driver JDBC. Ejecuta: mvn clean package", e);
        }
    }

    private static String valor(String clavePropiedad, String variableEntorno) {
        String env = System.getenv(variableEntorno);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return props.getProperty(clavePropiedad);
    }

    /** true solo si el valor existe y no es un texto de ejemplo. */
    private static boolean configurado(String v) {
        if (v == null || v.isBlank()) {
            return false;
        }
        for (String placeholder : PLACEHOLDERS) {
            if (v.contains(placeholder)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hayConfiguracionSupabase() {
        return configurado(valor("db.host", "DB_HOST"))
                && configurado(valor("db.user", "DB_USER"))
                && configurado(valor("db.password", "DB_PASSWORD"));
    }

    /** true cuando se esta usando la base de datos local H2. */
    public static boolean isModoLocal() {
        return modoLocal;
    }

    /** Carpeta donde se guardan la base local y los archivos subidos. */
    public static Path getCarpetaLocal() {
        Path carpeta = Paths.get(System.getProperty("user.home"), "miportafolio-datos");
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de datos local: " + carpeta, e);
        }
        return carpeta;
    }

    public static String getDescripcion() {
        if (modoLocal) {
            return "Base de datos local (H2) en " + getCarpetaLocal().resolve("portafolio");
        }
        return "Supabase Postgres en " + valor("db.host", "DB_HOST");
    }

    public static Connection getConnection() throws SQLException {
        if (modoLocal) {
            // MODE=PostgreSQL hace que H2 entienda el mismo SQL que Supabase,
            // para que el resto del codigo no cambie en nada.
            String rutaBase = getCarpetaLocal().resolve("portafolio").toString();
            String url = "jdbc:h2:file:" + rutaBase + ";MODE=PostgreSQL;AUTO_SERVER=TRUE";
            return DriverManager.getConnection(url, "sa", "");
        }

        String host = valor("db.host", "DB_HOST");
        String port = valor("db.port", "DB_PORT");
        String name = valor("db.name", "DB_NAME");
        String user = valor("db.user", "DB_USER");
        String password = valor("db.password", "DB_PASSWORD");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name + "?sslmode=require";

        return DriverManager.getConnection(url, user, password);
    }
}
