package com.barberiacortes.servicios.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** 
 * Centraliza la apertura de conexiones JDBC con la base de datos barberia-practica-jdbc.
 * Las credenciales se leen de variables de entorno (archivo env), nunca del código.
 */

public final class ConexionBD {

    private static final String URL_POR_DEFECTO = "jdbc:mysql://localhost:3306/barberia_practica_jdbc?connectionTimeZone=UTC";

    private ConexionBD() {
        // Clase de utilidad: no se instancia.
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = System.getenv("DB_URL");
        String usuario = System.getenv("DB_USER");
        String contrasena = System.getenv("DB_PASSWORD");

        if (url == null || url.isBlank()) {
            url = URL_POR_DEFECTO;
        }
        if (usuario == null || usuario.isBlank()) {
            throw new SQLException("Falta DB_USER Configura el archivo .env.");
        }
        if (contrasena == null) {
            throw new SQLException("Falta DB_PASSWORD Configura el archivo .env.");
        }

        // DriverManager seleciona el controlador de MySQL que Maven agregó al proyecto.
        return DriverManager.getConnection(url, usuario, contrasena);
    }
}