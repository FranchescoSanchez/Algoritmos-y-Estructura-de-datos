package com.miportafolio.model;

import java.time.LocalDateTime;

/**
 * Una tarea/archivo subido a una semana concreta de una unidad de un curso.
 */
public class Archivo {

    private int id;
    private int usuarioId;
    private String titulo;            // titulo de la tarea (opcional)
    private String nombreOriginal;
    private String nombreAlmacenado;
    private String curso;             // slug: taller-aplicaciones-1 | algoritmos-estructura-datos
    private int unidad;               // 1..4
    private int semana;               // 1..4 (dentro de la unidad)
    private String url;
    private String tipoMime;
    private long tamano;
    private LocalDateTime fechaSubida;

    public Archivo() {
    }

    /** Numero de semana correlativo dentro del curso (1..16). */
    public int getSemanaGlobal() {
        return (unidad - 1) * Curso.SEMANAS_POR_UNIDAD + semana;
    }

    /** Tamano legible, por ejemplo "1.4 MB". */
    public String getTamanoLegible() {
        if (tamano < 1024) return tamano + " B";
        if (tamano < 1024 * 1024) return Math.round(tamano / 1024.0) + " KB";
        return String.format("%.1f MB", tamano / (1024.0 * 1024.0));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getNombreAlmacenado() {
        return nombreAlmacenado;
    }

    public void setNombreAlmacenado(String nombreAlmacenado) {
        this.nombreAlmacenado = nombreAlmacenado;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public int getUnidad() {
        return unidad;
    }

    public void setUnidad(int unidad) {
        this.unidad = unidad;
    }

    public int getSemana() {
        return semana;
    }

    public void setSemana(int semana) {
        this.semana = semana;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTipoMime() {
        return tipoMime;
    }

    public void setTipoMime(String tipoMime) {
        this.tipoMime = tipoMime;
    }

    public long getTamano() {
        return tamano;
    }

    public void setTamano(long tamano) {
        this.tamano = tamano;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}
