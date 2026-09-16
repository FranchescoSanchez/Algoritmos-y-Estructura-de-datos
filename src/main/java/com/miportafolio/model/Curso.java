package com.miportafolio.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalogo de cursos del portafolio.
 * Cada curso tiene 4 unidades y cada unidad 4 semanas (16 semanas en total).
 */
public class Curso {

    public static final int UNIDADES = 4;
    public static final int SEMANAS_POR_UNIDAD = 4;

    private final String slug;
    private final String nombre;

    private static final Map<String, Curso> CATALOGO = new LinkedHashMap<>();

    static {
        registrar(new Curso("taller-aplicaciones-1", "Taller de Aplicaciones 1"));
        registrar(new Curso("algoritmos-estructura-datos", "Algoritmo y Estructura de Datos"));
    }

    private static void registrar(Curso curso) {
        CATALOGO.put(curso.getSlug(), curso);
    }

    public Curso(String slug, String nombre) {
        this.slug = slug;
        this.nombre = nombre;
    }

    public static Map<String, Curso> getCatalogo() {
        return CATALOGO;
    }

    public static boolean existe(String slug) {
        return CATALOGO.containsKey(slug);
    }

    public static String nombreDe(String slug) {
        Curso curso = CATALOGO.get(slug);
        return curso != null ? curso.getNombre() : slug;
    }

    /** Numero total de semanas del curso (4 unidades x 4 semanas). */
    public static int totalSemanas() {
        return UNIDADES * SEMANAS_POR_UNIDAD;
    }

    public String getSlug() {
        return slug;
    }

    public String getNombre() {
        return nombre;
    }
}
