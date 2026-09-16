package com.miportafolio.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gson compartido y ya configurado.
 *
 * Importante: Gson NO sabe serializar java.time.LocalDateTime por su cuenta y
 * en Java 17+ falla con InaccessibleObjectException al intentar acceder por
 * reflexion a las clases internas de java.time. Por eso registramos un
 * adaptador propio que la convierte a texto ISO ("2026-03-14T09:30:00").
 */
public class JsonConfig {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (fecha, tipo, contexto) ->
                            fecha == null ? null : new JsonPrimitive(fecha.format(ISO)))
            .create();

    public static Gson get() {
        return GSON;
    }
}
