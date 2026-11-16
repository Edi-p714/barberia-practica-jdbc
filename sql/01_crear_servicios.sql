-- Sistema de Gestión para Barbería - Barbería Cortes
-- Módulo: gestión de servicios (pantalla p-12)
-- Ejecutar una sola vez en MySQL Worbench.
-- IF NOT EXISTS permite repetir el script sin  perder los registros existentes.
--
-- La tabla respeta los atributos y tipos  del módelo de datos del proyecto
-- (relación servicios, normalizada en 3FN). Este módulo solo crea la tabla 
-- servicios: las otras 6 tablas del modelo no intervienen en él.
--
-- barberia_practica_jdbc es una base de practica para las evidencias de JDBC.
-- No es la base de datos del sistema real.

CREATE DATABASE IF NOT EXISTS barberia_practica_jdbc
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE barberia_practica_jdbc;

CREATE TABLE IF NOT EXISTS servicios (
    id_servicio INT NOT NULL AUTO_INCREMENT,
    nombre_servicio VARCHAR(80) NOT NULL,
    descripcion_servicio VARCHAR(255) NULL,
    precio_servicio DECIMAL(10, 2) NOT NULL,
    duracion_minutos INT NOT NULL,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_servicio),
    CONSTRAINT chk_precio_servicio CHECK (precio_servicio >= 0),
    CONSTRAINT chk_duracion_minutos CHECK (duracion_minutos > 0),
    CONSTRAINT chk_estado CHECK (estado IN (0, 1))
);

-- Control de la estructura y de los registros.
DESCRIBE servicios;
SELECT id_servicio, nombre_servicio, description, precio, duracion_minutos, estado
FROM servicios;
ORDER BY id_servicio;