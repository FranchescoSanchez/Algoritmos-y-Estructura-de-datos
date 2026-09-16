package com.miportafolio.service;

import com.miportafolio.config.DatabaseConfig;
import com.miportafolio.config.SupabaseConfig;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Sube, descarga y elimina los archivos del portafolio.
 *
 * Tiene dos modos, y elige solo cual usar:
 *  - Supabase Storage (cuando hay credenciales configuradas).
 *  - Carpeta local en el equipo (cuando no las hay), para que la app
 *    funcione sin configurar nada.
 */
public class StorageService {

    private final HttpClient cliente = HttpClient.newHttpClient();

    private boolean esLocal() {
        return DatabaseConfig.isModoLocal();
    }

    /** Carpeta local donde se guardan los archivos subidos. */
    private Path carpetaArchivos() throws IOException {
        Path carpeta = DatabaseConfig.getCarpetaLocal().resolve("archivos");
        Files.createDirectories(carpeta);
        return carpeta;
    }

    /**
     * Evita que un nombre con "../" pueda escribir fuera de la carpeta.
     */
    private Path rutaSegura(String nombreAlmacenado) throws IOException {
        Path base = carpetaArchivos().toAbsolutePath().normalize();
        Path destino = base.resolve(nombreAlmacenado).normalize();
        if (!destino.startsWith(base)) {
            throw new IOException("Ruta de archivo no válida: " + nombreAlmacenado);
        }
        return destino;
    }

    public String subirArchivo(byte[] datos, String nombreOriginal, String tipoMime, String categoria)
            throws IOException, InterruptedException {

        String extension = "";
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto >= 0) {
            extension = nombreOriginal.substring(punto);
        }

        String carpeta = (categoria == null || categoria.isBlank()) ? "otros" : categoria;
        String nombreAlmacenado = carpeta + "/" + UUID.randomUUID() + extension;

        if (esLocal()) {
            Path destino = rutaSegura(nombreAlmacenado);
            Files.createDirectories(destino.getParent());
            Files.write(destino, datos);
            return nombreAlmacenado;
        }

        String urlSubida = SupabaseConfig.getUrl() + "/storage/v1/object/"
                + SupabaseConfig.getBucket() + "/" + nombreAlmacenado;

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlSubida))
                .header("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                .header("apikey", SupabaseConfig.getServiceKey())
                .header("Content-Type", tipoMime != null ? tipoMime : "application/octet-stream")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(datos))
                .build();

        HttpResponse<String> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofString());

        if (respuesta.statusCode() != 200 && respuesta.statusCode() != 201) {
            throw new IOException("Error al subir archivo a Supabase (" + respuesta.statusCode() + "): "
                    + respuesta.body());
        }

        return nombreAlmacenado;
    }

    public String obtenerUrlPublica(String nombreAlmacenado) {
        if (esLocal()) {
            // En modo local no hay URL publica: la descarga y la vista previa
            // pasan siempre por los servlets /descargar?id=N y /ver?id=N,
            // que son los que usa la interfaz. Se guarda solo una referencia.
            return "local:" + nombreAlmacenado;
        }
        return SupabaseConfig.getUrl() + "/storage/v1/object/public/"
                + SupabaseConfig.getBucket() + "/" + nombreAlmacenado;
    }

    public byte[] descargarArchivo(String nombreAlmacenado) throws IOException, InterruptedException {
        if (esLocal()) {
            Path origen = rutaSegura(nombreAlmacenado);
            if (!Files.exists(origen)) {
                throw new IOException("El archivo ya no existe en la carpeta local.");
            }
            return Files.readAllBytes(origen);
        }

        String urlDescarga = SupabaseConfig.getUrl() + "/storage/v1/object/"
                + SupabaseConfig.getBucket() + "/" + nombreAlmacenado;

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlDescarga))
                .header("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                .header("apikey", SupabaseConfig.getServiceKey())
                .GET()
                .build();

        HttpResponse<byte[]> respuesta = cliente.send(peticion, HttpResponse.BodyHandlers.ofByteArray());

        if (respuesta.statusCode() != 200) {
            throw new IOException("Error al descargar archivo (" + respuesta.statusCode() + ")");
        }
        return respuesta.body();
    }

    public void eliminarArchivo(String nombreAlmacenado) throws IOException, InterruptedException {
        if (esLocal()) {
            Files.deleteIfExists(rutaSegura(nombreAlmacenado));
            return;
        }

        String urlEliminar = SupabaseConfig.getUrl() + "/storage/v1/object/"
                + SupabaseConfig.getBucket() + "/" + nombreAlmacenado;

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(urlEliminar))
                .header("Authorization", "Bearer " + SupabaseConfig.getServiceKey())
                .header("apikey", SupabaseConfig.getServiceKey())
                .DELETE()
                .build();

        cliente.send(peticion, HttpResponse.BodyHandlers.ofString());
    }
}
