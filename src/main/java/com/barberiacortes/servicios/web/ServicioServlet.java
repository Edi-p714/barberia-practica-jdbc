package com.barberiacortes.servicios.web;

import com.barberiacortes.servicios.dao.ServicioDAO;
import com.barberiacortes.servicios.modelo.Servicio;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador web del módulo de servicios (rol Administrador, pantalla P-12).
 *
 * GET  /servicios                      -> listado
 * GET  /servicios?accion=buscar&id=N   -> consulta por ID
 * GET  /servicios?accion=nuevo         -> formulario vacío
 * GET  /servicios?accion=editar&id=N   -> formulario con los datos del servicio
 * POST /servicios  accion=guardar      -> inserta o actualiza
 * POST /servicios  accion=eliminar     -> elimina
 *
 * Las consultas usan GET porque solo leen; las operaciones que cambian datos
 * usan POST. Después de un POST exitoso se redirige al listado
 * (patrón Post/Redirect/Get) para que recargar la página no repita la operación.
 */
@WebServlet("/servicios")
public class ServicioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String VISTA_LISTA = "/WEB-INF/vistas/servicios/lista.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/servicios/formulario.jsp";

    private static final int LONGITUD_MAXIMA_NOMBRE = 80;
    private static final int LONGITUD_MAXIMA_DESCRIPCION = 255;

    /** Mensajes de éxito que llegan al listado después de una redirección. */
    private static final Map<String, String> MENSAJES_EXITO = Map.of(
            "creado", "Servicio registrado correctamente.",
            "actualizado", "Servicio actualizado correctamente.",
            "eliminado", "Servicio eliminado correctamente."
    );

    /** Mensajes de error que llegan al listado después de una redirección. */
    private static final Map<String, String> MENSAJES_ERROR = Map.of(
            "noEncontrado", "El servicio solicitado no existe.",
            "enUso", "No se puede eliminar: el servicio tiene citas registradas. Desactívalo en su lugar."
    );

    private final transient ServicioDAO servicioDAO = new ServicioDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = valorOVacio(request.getParameter("accion"));

        try {
            switch (accion) {
                case "nuevo" -> mostrarFormularioNuevo(request, response);
                case "editar" -> mostrarFormularioEdicion(request, response);
                case "buscar" -> buscarPorId(request, response);
                default -> listar(request, response);
            }
        } catch (SQLException error) {
            mostrarErrorBaseDatos(request, response, error);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String accion = valorOVacio(request.getParameter("accion"));

        try {
            switch (accion) {
                case "guardar" -> guardar(request, response);
                case "eliminar" -> eliminar(request, response);
                default -> redirigirAlListado(request, response, null);
            }
        } catch (SQLException error) {
            mostrarErrorBaseDatos(request, response, error);
        }
    }

    // ----- Acciones GET -----

    private void listar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        // Solo se muestran textos propios: el parámetro trae un código, nunca el texto.
        String codigoMensaje = request.getParameter("mensaje");
        if (codigoMensaje != null) {
            if (MENSAJES_EXITO.containsKey(codigoMensaje)) {
                request.setAttribute("mensaje", MENSAJES_EXITO.get(codigoMensaje));
            } else if (MENSAJES_ERROR.containsKey(codigoMensaje)) {
                request.setAttribute("error", MENSAJES_ERROR.get(codigoMensaje));
            }
        }

        request.setAttribute("servicios", servicioDAO.listar());
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void buscarPorId(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Integer idServicio = convertirEnteroPositivo(request.getParameter("id"));
        List<Servicio> servicios = new ArrayList<>();

        if (idServicio == null) {
            request.setAttribute("error", "Escribe un ID válido: un número entero mayor que cero.");
        } else {
            Optional<Servicio> encontrado = servicioDAO.buscarPorId(idServicio);
            if (encontrado.isPresent()) {
                servicios.add(encontrado.get());
            } else {
                request.setAttribute("error", "No existe un servicio con el ID " + idServicio + ".");
            }
        }

        request.setAttribute("servicios", servicios);
        request.setAttribute("esBusqueda", Boolean.TRUE);
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private void mostrarFormularioNuevo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, String> formulario = new LinkedHashMap<>();
        formulario.put("activo", "on");
        mostrarFormulario(request, response, formulario, List.of());
    }

    private void mostrarFormularioEdicion(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Integer idServicio = convertirEnteroPositivo(request.getParameter("id"));
        Optional<Servicio> encontrado = (idServicio == null)
                ? Optional.empty()
                : servicioDAO.buscarPorId(idServicio);

        if (encontrado.isEmpty()) {
            redirigirAlListado(request, response, "noEncontrado");
            return;
        }

        Servicio servicio = encontrado.get();
        Map<String, String> formulario = new LinkedHashMap<>();
        formulario.put("idServicio", String.valueOf(servicio.getIdServicio()));
        formulario.put("nombreServicio", servicio.getNombreServicio());
        formulario.put("descripcion", servicio.getDescripcion());
        formulario.put("precio", servicio.getPrecio().toPlainString());
        formulario.put("duracionMinutos", String.valueOf(servicio.getDuracionMinutos()));
        formulario.put("activo", servicio.isActivo() ? "on" : null);

        mostrarFormulario(request, response, formulario, List.of());
    }

    // ----- Acciones POST -----

    private void guardar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        Map<String, String> formulario = new LinkedHashMap<>();
        formulario.put("idServicio", valorOVacio(request.getParameter("idServicio")).trim());
        formulario.put("nombreServicio", valorOVacio(request.getParameter("nombreServicio")).trim());
        formulario.put("descripcion", valorOVacio(request.getParameter("descripcion")).trim());
        formulario.put("precio", valorOVacio(request.getParameter("precio")).trim());
        formulario.put("duracionMinutos", valorOVacio(request.getParameter("duracionMinutos")).trim());
        formulario.put("activo", request.getParameter("activo"));

        List<String> errores = new ArrayList<>();

        String nombreServicio = formulario.get("nombreServicio");
        if (nombreServicio.isEmpty() || nombreServicio.length() > LONGITUD_MAXIMA_NOMBRE) {
            errores.add("El nombre es obligatorio y admite hasta " + LONGITUD_MAXIMA_NOMBRE + " caracteres.");
        }

        String descripcion = formulario.get("descripcion");
        if (descripcion.length() > LONGITUD_MAXIMA_DESCRIPCION) {
            errores.add("La descripción admite hasta " + LONGITUD_MAXIMA_DESCRIPCION + " caracteres.");
        }

        BigDecimal precio = convertirPrecio(formulario.get("precio"));
        if (precio == null) {
            errores.add("El precio debe ser un número no negativo, con máximo 2 decimales.");
        }

        Integer duracionMinutos = convertirEnteroPositivo(formulario.get("duracionMinutos"));
        if (duracionMinutos == null) {
            errores.add("La duración debe ser un número entero de minutos mayor que cero.");
        }

        boolean esEdicion = !formulario.get("idServicio").isEmpty();
        Integer idServicio = esEdicion ? convertirEnteroPositivo(formulario.get("idServicio")) : null;
        if (esEdicion && idServicio == null) {
            errores.add("El identificador del servicio no es válido.");
        }

        if (!errores.isEmpty()) {
            mostrarFormulario(request, response, formulario, errores);
            return;
        }

        boolean activo = formulario.get("activo") != null;
        Servicio servicio = new Servicio(esEdicion ? idServicio : 0, nombreServicio,
                descripcion, precio, duracionMinutos, activo);

        if (esEdicion) {
            boolean actualizado = servicioDAO.actualizar(servicio);
            redirigirAlListado(request, response, actualizado ? "actualizado" : "noEncontrado");
        } else {
            servicioDAO.insertar(servicio);
            redirigirAlListado(request, response, "creado");
        }
    }

    private void eliminar(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Integer idServicio = convertirEnteroPositivo(request.getParameter("id"));
        if (idServicio == null) {
            redirigirAlListado(request, response, "noEncontrado");
            return;
        }

        try {
            boolean eliminado = servicioDAO.eliminar(idServicio);
            redirigirAlListado(request, response, eliminado ? "eliminado" : "noEncontrado");
        } catch (SQLIntegrityConstraintViolationException error) {
            // Regla del proyecto: no se borra un registro que tiene historial asociado.
            redirigirAlListado(request, response, "enUso");
        }
    }

    // ----- Apoyo -----

    private void mostrarFormulario(HttpServletRequest request, HttpServletResponse response,
                                   Map<String, String> formulario, List<String> errores)
            throws ServletException, IOException {

        request.setAttribute("formulario", formulario);
        request.setAttribute("errores", errores);
        request.getRequestDispatcher(VISTA_FORMULARIO).forward(request, response);
    }

    private void redirigirAlListado(HttpServletRequest request, HttpServletResponse response,
                                    String codigoMensaje) throws IOException {

        String destino = request.getContextPath() + "/servicios";
        if (codigoMensaje != null) {
            destino += "?mensaje=" + codigoMensaje;
        }
        response.sendRedirect(destino);
    }

    private void mostrarErrorBaseDatos(HttpServletRequest request, HttpServletResponse response,
                                       SQLException error) throws ServletException, IOException {

        log("Error de base de datos en el módulo de servicios", error);
        request.setAttribute("error", "No fue posible acceder a la base de datos. "
                + "Revisa que MySQL esté encendido y la configuración de conexión.");
        request.setAttribute("servicios", List.of());
        request.getRequestDispatcher(VISTA_LISTA).forward(request, response);
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }

    private static Integer convertirEnteroPositivo(String texto) {
        try {
            int numero = Integer.parseInt(valorOVacio(texto).trim());
            return numero > 0 ? numero : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }

    /** Precio: no negativo, máximo 2 decimales y 8 enteros (DECIMAL(10,2)). */
    private static BigDecimal convertirPrecio(String texto) {
        try {
            BigDecimal precio = new BigDecimal(valorOVacio(texto).trim().replace(',', '.'));
            boolean valido = precio.signum() >= 0
                    && precio.scale() <= 2
                    && precio.precision() - precio.scale() <= 8;
            return valido ? precio : null;
        } catch (NumberFormatException error) {
            return null;
        }
    }
}