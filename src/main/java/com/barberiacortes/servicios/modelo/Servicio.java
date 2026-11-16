package com.barberiacortes.servicios.modelo;

import java.math.BigDecimal;

/**
 * Modelo de un registro de la tabla servicios.
 * Cada atributo corresponde a una columna: idServicio -> id_servicio,
 * nombreServicio -> nombre_servicio, duracionMinutos -> duracion_minutos, etc.
 */
public final class Servicio {

    private final int idServicio;
    private final String nombreServicio;
    private final String descripcion;
    private final BigDecimal precio;
    private final int duracionMinutos;
    private final boolean activo;

    public Servicio(int idServicio, String nombreServicio, String descripcion,
                    BigDecimal precio, int duracionMinutos, boolean activo) {
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
        this.activo = activo;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public boolean isActivo() {
        return activo;
    }

    @Override
    public String toString() {
        String textoDescripcion = (descripcion == null || descripcion.isBlank())
                ? "(sin descripción)"
                : descripcion;

        return "ID: " + idServicio
                + " | Servicio: " + nombreServicio
                + " | Precio: $" + precio.toPlainString()
                + " | Duración: " + duracionMinutos + " min"
                + " | Estado: " + (activo ? "Activo" : "Inactivo")
                + " | Descripción: " + textoDescripcion;
    }
}
