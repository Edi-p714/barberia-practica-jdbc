package com.barberiacortes.servicios.dao;

import com.barberiacortes.servicios.config.ConexionBD;
import com.barberiacortes.servicios.modelo.Servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO (Data Access Object) de la tabla servicios.
 * Concentra todas las sentencias SQL del módulo; ninguna otra clase escribe SQL.
 * Todas las sentencias usan PreparedStatement y cada método abre y cierra
 * sus recursos con try-with-resources.
 */
public final class ServicioDAO {

    private static final String COLUMNAS =
            "id_servicio, nombre_servicio, descripcion, precio, duracion_minutos, estado";

    /** Inserta un servicio y devuelve el ID generado por la base de datos. */
    public int insertar(Servicio servicio) throws SQLException {
        String sql = "INSERT INTO servicios "
                + "(nombre_servicio, descripcion, precio, duracion_minutos, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            asignarParametros(ps, servicio);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas != 1) {
                throw new SQLException("El servicio no pudo registrarse.");
            }

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
                throw new SQLException("No se recibió el ID generado para el servicio.");
            }
        }
    }

    /** Devuelve todos los servicios ordenados por ID. */
    public List<Servicio> listar() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM servicios ORDER BY id_servicio";
        List<Servicio> servicios = new ArrayList<>();

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                servicios.add(convertirEnServicio(rs));
            }
        }

        return servicios;
    }

    /** Busca un servicio por su ID; devuelve Optional vacío si no existe. */
    public Optional<Servicio> buscarPorId(int idServicio) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM servicios WHERE id_servicio = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idServicio);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(convertirEnServicio(rs));
                }
            }
        }

        return Optional.empty();
    }

    /** Actualiza todos los campos de un servicio. Devuelve true si modificó una fila. */
    public boolean actualizar(Servicio servicio) throws SQLException {
        String sql = "UPDATE servicios SET nombre_servicio = ?, descripcion = ?, precio = ?, "
                + "duracion_minutos = ?, estado = ? WHERE id_servicio = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            asignarParametros(ps, servicio);
            ps.setInt(6, servicio.getIdServicio());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un servicio por su ID. Devuelve true si eliminó una fila.
     * Si el servicio ya está referenciado por citas, la base de datos rechaza
     * el borrado y se lanza SQLIntegrityConstraintViolationException.
     */
    public boolean eliminar(int idServicio) throws SQLException {
        String sql = "DELETE FROM servicios WHERE id_servicio = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idServicio);
            return ps.executeUpdate() > 0;
        }
    }

    /** Asigna los cinco primeros parámetros, comunes a INSERT y UPDATE. */
    private void asignarParametros(PreparedStatement ps, Servicio servicio) throws SQLException {
        ps.setString(1, servicio.getNombreServicio());

        if (servicio.getDescripcion() == null || servicio.getDescripcion().isBlank()) {
            ps.setNull(2, Types.VARCHAR);
        } else {
            ps.setString(2, servicio.getDescripcion());
        }

        ps.setBigDecimal(3, servicio.getPrecio());
        ps.setInt(4, servicio.getDuracionMinutos());
        ps.setBoolean(5, servicio.isActivo());
    }

    private Servicio convertirEnServicio(ResultSet rs) throws SQLException {
        return new Servicio(
                rs.getInt("id_servicio"),
                rs.getString("nombre_servicio"),
                rs.getString("descripcion"),
                rs.getBigDecimal("precio"),
                rs.getInt("duracion_minutos"),
                rs.getBoolean("estado")
        );
    }
}
