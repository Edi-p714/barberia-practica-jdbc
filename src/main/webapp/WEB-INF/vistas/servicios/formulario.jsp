<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.barberiacortes.servicios.util.TextoHtml" %>
<%
    // El servlet entrega los valores del formulario y, si los hay, los errores de validación.
    @SuppressWarnings("unchecked")
    Map<String, String> formulario = (Map<String, String>) request.getAttribute("formulario");
    @SuppressWarnings("unchecked")
    List<String> errores = (List<String>) request.getAttribute("errores");

    String idServicio = formulario.get("idServicio");
    boolean esEdicion = idServicio != null && !idServicio.isEmpty();
    boolean activo = formulario.get("activo") != null;
    String rutaServicios = request.getContextPath() + "/servicios";
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= esEdicion ? "Editar servicio" : "Registrar servicio" %> · Barbería Cortes</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/estilos.css">
</head>
<body>
    <%@ include file="/WEB-INF/vistas/fragmentos/encabezado.jspf" %>

    <main class="contenido">
        <h1><%= esEdicion ? "Editar servicio " + TextoHtml.escapar(idServicio) : "Registrar servicio" %></h1>
    
        <% if (errores != null && !errores.isEmpty()) { %>
            <div class="aviso aviso-error" role="alert">
                <p>Revisa estos datos:</p>
                <ul>
                    <% for (String error : errores) { %>
                        <li><%= TextoHtml.escapar(error) %></li>
                    <% } %>
                </ul>
            </div>
        <% } %>
    
        <%-- Formulario con POST: crea o modifica datos. --%>
        <form class="formulario" method="post" action="<%= rutaServicios %>">
            <input type="hidden" name="accion" value="guardar">
            <input type="hidden" name="idServicio" value="<%= TextoHtml.escapar(idServicio) %>">
    
            <label for="nombreServicio">Nombre del servicio</label>
            <input id="nombreServicio" name="nombreServicio" type="text" maxlength="80" required
                   value="<%= TextoHtml.escapar(formulario.get("nombreServicio")) %>">
    
            <label for="descripcion">Descripción <span class="opcional">(opcional)</span></label>
            <textarea id="descripcion" name="descripcion" maxlength="255" rows="3"><%= TextoHtml.escapar(formulario.get("descripcion")) %></textarea>
    
            <label for="precio">Precio en pesos, sin separadores de miles</label>
            <input id="precio" name="precio" type="number" min="0" step="0.01" required
                   value="<%= TextoHtml.escapar(formulario.get("precio")) %>">
    
            <label for="duracionMinutos">Duración en minutos</label>
            <input id="duracionMinutos" name="duracionMinutos" type="number" min="1" step="1" required
                   value="<%= TextoHtml.escapar(formulario.get("duracionMinutos")) %>">
    
            <label class="casilla">
                <input name="activo" type="checkbox" <%= activo ? "checked" : "" %>>
                Servicio activo (se ofrece a los clientes)
            </label>
    
            <div class="botones">
                <button class="boton" type="submit"><%= esEdicion ? "Guardar cambios" : "Registrar" %></button>
                <a class="boton boton-secundario" href="<%= rutaServicios %>">Cancelar</a>
            </div>
        </form>
    </main>
</body>
</html>