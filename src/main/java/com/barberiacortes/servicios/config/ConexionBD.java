package com.barberiacortes.servicios.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Centraliza la apertura de conexiones JDBC con la base de datos barberia_practica_jdbc.
 * Las credenciales se leen de variables de entorno (archivo .env), nunca del código.
 */
public final class ConexionBD {

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/barberia_practica_jdbc?connectionTimeZone=UTC";

    /** Clase del controlador MySQL Connector/J. DB_DRIVER permite cambiarla si se usa otro motor. */
    private static final String CONTROLADOR_POR_DEFECTO = "com.mysql.cj.jdbc.Driver";

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
            throw new SQLException("Falta DB_USER. Configura el archivo .env.");
        }
        if (contrasena == null) {
            throw new SQLException("Falta DB_PASSWORD. Configura el archivo .env.");
        }

        cargarControlador();
        return DriverManager.getConnection(url, usuario, contrasena);
    }

    /**
     * En una aplicación de consola, DriverManager encuentra solo el controlador.
     * Dentro de Tomcat no: el controlador viaja en el WAR (WEB-INF/lib) y hay que
     * cargarlo de forma explícita para que quede registrado.
     */
    private static void cargarControlador() throws SQLException {
        String controlador = System.getenv("DB_DRIVER");
        if (controlador == null || controlador.isBlank()) {
            controlador = CONTROLADOR_POR_DEFECTO;
        }

        try {
            Class.forName(controlador);
        } catch (ClassNotFoundException error) {
            throw new SQLException("No se encontró el controlador JDBC " + controlador
                    + ". Revisa la dependencia en pom.xml.", error);
        }
    }
}