package com.miportafolio.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Datos de conexion a Supabase Storage (bucket de archivos).
 * Prioriza variables de entorno (SUPABASE_URL, SUPABASE_KEY, SUPABASE_BUCKET).
 */
public class SupabaseConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = SupabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar SupabaseConfig", e);
        }
    }

    private static String valor(String clavePropiedad, String variableEntorno) {
        String env = System.getenv(variableEntorno);
        if (env != null && !env.isBlank()) {
            return env;
        }
        return props.getProperty(clavePropiedad);
    }

    public static String getUrl() {
        return valor("supabase.url", "SUPABASE_URL");
    }

    public static String getServiceKey() {
        return valor("supabase.key", "SUPABASE_KEY");
    }

    public static String getBucket() {
        return valor("supabase.bucket", "SUPABASE_BUCKET");
    }
}
