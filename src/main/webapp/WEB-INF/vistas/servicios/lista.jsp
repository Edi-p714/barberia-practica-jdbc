<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.barberiacortes.servicios.modelo.Servicio" %>
<%@ page import="com.barberiacortes.servicios.util.TextoHtml" %>
<%
    // Datos que entrega el servlet. La vista solo los muestra; no consulta la base de datos.
    @SuppressWarnings("unchecked")
    List<Servicio> servicios = (List<Servicio>) request.getAttribute("servicios");
    String mensaje = (String) request.getAttribute("mensaje");
    String error = (String) request.getAttribute("error");
    boolean esBusqueda = request.getAttribute("esBusqueda") != null;
    String rutaServicios = request.getContextPath() + "/servicios";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Servicios · Barbería Cortes</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/estilos.css">
</head>
<body>
<%@ include file="/WEB-INF/vistas/fragmentos/encabezado.jspf" %>

<main class="contenido">
    <div class="titulo-seccion">
        <h1>Servicios</h1>
        <a class="boton" href="<%= rutaServicios %>?accion=nuevo">Registrar servicio</a>
    </div>

    <% if (mensaje != null) { %>
        <p class="aviso aviso-exito" role="status"><%= TextoHtml.escapar(mensaje) %></p>
    <% } %>
    <% if (error != null) { %>
        <p class="aviso aviso-error" role="alert"><%= TextoHtml.escapar(error) %></p>
    <% } %>

    <%-- Formulario con GET: solo consulta, no cambia datos. --%>
    <form class="buscador" method="get" action="<%= rutaServicios %>">
        <input type="hidden" name="accion" value="buscar">
        <label for="id">Consultar por ID</label>
        <input id="id" name="id" type="number" min="1" required>
        <button class="boton boton-secundario" type="submit">Consultar</button>
        <% if (esBusqueda) { %>
            <a href="<%= rutaServicios %>">Ver todos</a>
        <% } %>
    </form>

    <% if (servicios == null || servicios.isEmpty()) { %>
        <% if (!esBusqueda && error == null) { %>
            <p>Aún no hay servicios registrados.</p>
        <% } %>
    <% } else { %>
        <table class="tabla">
            <thead>
            <tr>
                <th scope="col">ID</th>
                <th scope="col">Servicio</th>
                <th scope="col">Descripción</th>
                <th scope="col">Precio</th>
                <th scope="col">Duración</th>
                <th scope="col">Estado</th>
                <th scope="col">Acciones</th>
            </tr>
            </thead>
            <tbody>
            <% for (Servicio servicio : servicios) { %>
                <tr>
                    <td><%= servicio.getIdServicio() %></td>
                    <td><%= TextoHtml.escapar(servicio.getNombreServicio()) %></td>
                    <td><%= servicio.getDescripcion() == null ? "—" : TextoHtml.escapar(servicio.getDescripcion()) %></td>
                    <td>$<%= servicio.getPrecio().toPlainString() %></td>
                    <td><%= servicio.getDuracionMinutos() %> min</td>
                    <td>
                        <span class="estado <%= servicio.isActivo() ? "estado-activo" : "estado-inactivo" %>">
                            <%= servicio.isActivo() ? "Activo" : "Inactivo" %>
                        </span>
                    </td>
                    <td class="acciones">
                        <a href="<%= rutaServicios %>?accion=editar&id=<%= servicio.getIdServicio() %>">Editar</a>
                        <%-- Eliminar cambia datos: por eso es un formulario con POST y no un enlace. --%>
                        <form method="post" action="<%= rutaServicios %>"
                              onsubmit="return confirm('¿Eliminar el servicio <%= servicio.getIdServicio() %>?');">
                            <input type="hidden" name="accion" value="eliminar">
                            <input type="hidden" name="id" value="<%= servicio.getIdServicio() %>">
                            <button class="enlace-peligro" type="submit">Eliminar</button>
                        </form>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
        <p class="total">Total: <%= servicios.size() %> servicio(s).</p>
    <% } %>
</main>
</body>
</html>