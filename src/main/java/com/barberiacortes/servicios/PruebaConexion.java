package com.barberiacortes.servicios;

import com.barberiacortes.servicios.config.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Primer punto de control: verificar que Java llega a la base de datos.
 */
public final class PruebaConexion {

    private PruebaConexion() {
    }

    public static void main(String[] args) {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            System.out.println("Conexión JDBC establecida correctamente.");
            System.out.println("Base de datos: " + conexion.getCatalog());
        } catch (SQLException error) {
            System.err.println("No fue posible conectar con MySQL.");
            System.err.println("Detalle: " + error.getMessage());
            System.err.println("Revisa el servidor, la base de datos y el archivo .env.");
        }
    }
}