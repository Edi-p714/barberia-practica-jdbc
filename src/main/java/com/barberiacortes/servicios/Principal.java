package com.barberiacortes.servicios;

import com.barberiacortes.servicios.dao.ServicioDAO;
import com.barberiacortes.servicios.modelo.Servicio;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Menú de consola del módulo de gestión de servicios (rol Administrador, pantalla P-12).
 * Esta clase solo lee datos y muestra resultados; el SQL vive en ServicioDAO.
 */
public final class Principal {

    private static final int LONGITUD_MAXIMA_NOMBRE = 80;
    private static final int LONGITUD_MAXIMA_DESCRIPCION = 255;

    private static final Scanner ENTRADA = new Scanner(System.in);
    private static final ServicioDAO SERVICIO_DAO = new ServicioDAO();

    private Principal() {
    }

    public static void main(String[] args) {
        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Selecciona una opción: ");

            try {
                switch (opcion) {
                    case 1 -> registrarServicio();
                    case 2 -> listarServicios();
                    case 3 -> consultarServicio();
                    case 4 -> actualizarServicio();
                    case 5 -> eliminarServicio();
                    case 0 -> System.out.println("Aplicación finalizada.");
                    default -> System.out.println("Opción fuera del menú.");
                }
            } catch (SQLException error) {
                System.err.println("Error al acceder a la base de datos: " + error.getMessage());
            }

        } while (opcion != 0);
    }

    private static void mostrarMenu() {
        System.out.println("\n===== BARBERÍA CORTES · GESTIÓN DE SERVICIOS =====");
        System.out.println("1. Registrar servicio");
        System.out.println("2. Listar servicios");
        System.out.println("3. Consultar servicio por ID");
        System.out.println("4. Actualizar servicio");
        System.out.println("5. Eliminar servicio");
        System.out.println("0. Salir");
        System.out.println("==================================================");
    }

    private static void registrarServicio() throws SQLException {
        System.out.println("\n--- REGISTRO DE SERVICIO ---");
        String nombreServicio = leerTextoObligatorio("Nombre del servicio: ", LONGITUD_MAXIMA_NOMBRE);
        String descripcion = leerTextoOpcional("Descripción (Enter para omitir): ",
                null, LONGITUD_MAXIMA_DESCRIPCION);
        BigDecimal precio = leerPrecio("Precio sin separadores de miles: ", null);
        int duracionMinutos = leerDuracion("Duración en minutos: ", null);

        Servicio nuevoServicio = new Servicio(0, nombreServicio, descripcion,
                precio, duracionMinutos, true);
        int idGenerado = SERVICIO_DAO.insertar(nuevoServicio);
        System.out.println("Servicio registrado. ID generado: " + idGenerado);
    }

    private static void listarServicios() throws SQLException {
        System.out.println("\n--- LISTADO DE SERVICIOS ---");
        List<Servicio> servicios = SERVICIO_DAO.listar();

        if (servicios.isEmpty()) {
            System.out.println("Aún no hay servicios registrados.");
            return;
        }

        for (Servicio servicio : servicios) {
            System.out.println(servicio);
        }
        System.out.println("Total: " + servicios.size() + " servicio(s).");
    }

    private static void consultarServicio() throws SQLException {
        System.out.println("\n--- CONSULTA POR ID ---");
        int idServicio = leerEnteroPositivo("ID del servicio: ");
        Optional<Servicio> encontrado = SERVICIO_DAO.buscarPorId(idServicio);

        if (encontrado.isPresent()) {
            System.out.println(encontrado.get());
        } else {
            System.out.println("No existe un servicio con el ID " + idServicio + ".");
        }
    }

    private static void actualizarServicio() throws SQLException {
        System.out.println("\n--- ACTUALIZACIÓN DE SERVICIO ---");
        int idServicio = leerEnteroPositivo("ID a actualizar: ");
        Optional<Servicio> encontrado = SERVICIO_DAO.buscarPorId(idServicio);

        if (encontrado.isEmpty()) {
            System.out.println("No existe un servicio con el ID " + idServicio + ".");
            return;
        }

        Servicio actual = encontrado.get();
        System.out.println("Registro actual: " + actual);
        System.out.println("Pulsa Enter en cualquier campo para conservar el valor actual.");

        String nombreServicio = leerTextoOpcional("Nuevo nombre [" + actual.getNombreServicio() + "]: ",
                actual.getNombreServicio(), LONGITUD_MAXIMA_NOMBRE);
        String descripcion = leerTextoOpcional("Nueva descripción: ",
                actual.getDescripcion(), LONGITUD_MAXIMA_DESCRIPCION);
        BigDecimal precio = leerPrecio("Nuevo precio [" + actual.getPrecio().toPlainString() + "]: ",
                actual.getPrecio());
        int duracionMinutos = leerDuracion("Nueva duración [" + actual.getDuracionMinutos() + " min]: ",
                actual.getDuracionMinutos());
        boolean activo = leerConfirmacion("¿Servicio activo? (s/n) ["
                + (actual.isActivo() ? "s" : "n") + "]: ", actual.isActivo());

        Servicio servicioModificado = new Servicio(idServicio, nombreServicio, descripcion,
                precio, duracionMinutos, activo);
        boolean actualizado = SERVICIO_DAO.actualizar(servicioModificado);
        System.out.println(actualizado
                ? "Servicio actualizado correctamente."
                : "No se modificó ningún servicio.");
    }

    private static void eliminarServicio() throws SQLException {
        System.out.println("\n--- ELIMINACIÓN DE SERVICIO ---");
        int idServicio = leerEnteroPositivo("ID a eliminar: ");
        Optional<Servicio> encontrado = SERVICIO_DAO.buscarPorId(idServicio);

        if (encontrado.isEmpty()) {
            System.out.println("No existe un servicio con el ID " + idServicio + ".");
            return;
        }

        System.out.println("Registro encontrado: " + encontrado.get());
        if (!leerConfirmacion("¿Confirmas la eliminación? (s/n): ", false)) {
            System.out.println("Eliminación cancelada.");
            return;
        }

        try {
            boolean eliminado = SERVICIO_DAO.eliminar(idServicio);
            System.out.println(eliminado
                    ? "Servicio eliminado correctamente."
                    : "No se eliminó ningún servicio.");
        } catch (SQLIntegrityConstraintViolationException error) {
            // Regla del proyecto: no se borra un registro que tiene historial asociado.
            System.out.println("No se puede eliminar: el servicio tiene citas registradas.");
            System.out.println("Para retirarlo del catálogo, desactívalo con la opción 4.");
        }
    }

    // ----- Lectura y validación de datos -----

    private static String leerTextoObligatorio(String mensaje, int longitudMaxima) {
        while (true) {
            System.out.print(mensaje);
            String texto = ENTRADA.nextLine().trim();
            if (!texto.isEmpty() && texto.length() <= longitudMaxima) {
                return texto;
            }
            System.out.println("El texto debe tener entre 1 y " + longitudMaxima + " caracteres.");
        }
    }

    /** Lee un texto opcional. Si el usuario pulsa Enter, devuelve el valor actual. */
    private static String leerTextoOpcional(String mensaje, String valorActual, int longitudMaxima) {
        while (true) {
            System.out.print(mensaje);
            String texto = ENTRADA.nextLine().trim();
            if (texto.isEmpty()) {
                return valorActual;
            }
            if (texto.length() <= longitudMaxima) {
                return texto;
            }
            System.out.println("El texto no puede superar " + longitudMaxima + " caracteres.");
        }
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = ENTRADA.nextLine().trim();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException error) {
                System.out.println("Ingresa un número entero válido.");
            }
        }
    }

    private static int leerEnteroPositivo(String mensaje) {
        while (true) {
            int numero = leerEntero(mensaje);
            if (numero > 0) {
                return numero;
            }
            System.out.println("El número debe ser mayor que cero.");
        }
    }

    /** Duración en minutos: entero mayor que cero. Enter conserva el valor actual si existe. */
    private static int leerDuracion(String mensaje, Integer valorActual) {
        while (true) {
            System.out.print(mensaje);
            String texto = ENTRADA.nextLine().trim();
            if (texto.isEmpty() && valorActual != null) {
                return valorActual;
            }
            try {
                int duracionMinutos = Integer.parseInt(texto);
                if (duracionMinutos > 0) {
                    return duracionMinutos;
                }
            } catch (NumberFormatException error) {
                // El texto no es un número entero.
            }
            System.out.println("La duración debe ser un número entero mayor que cero.");
        }
    }

    /** Precio: no negativo, máximo 2 decimales y 8 enteros (DECIMAL(10,2)). */
    private static BigDecimal leerPrecio(String mensaje, BigDecimal valorActual) {
        while (true) {
            System.out.print(mensaje);
            String texto = ENTRADA.nextLine().trim().replace(',', '.');
            if (texto.isEmpty() && valorActual != null) {
                return valorActual;
            }
            try {
                BigDecimal precio = new BigDecimal(texto);
                if (precio.signum() >= 0
                        && precio.scale() <= 2
                        && precio.precision() - precio.scale() <= 8) {
                    return precio;
                }
            } catch (NumberFormatException error) {
                // El texto no es un número decimal válido.
            }
            System.out.println("Escribe un precio válido, no negativo y con máximo 2 decimales.");
            System.out.println("Ejemplo: 25000");
        }
    }

    /** Lee s/n. Si el usuario pulsa Enter, devuelve el valor por defecto. */
    private static boolean leerConfirmacion(String mensaje, boolean valorPorDefecto) {
        while (true) {
            System.out.print(mensaje);
            String respuesta = ENTRADA.nextLine().trim().toLowerCase();
            if (respuesta.isEmpty()) {
                return valorPorDefecto;
            }
            if (respuesta.equals("s")) {
                return true;
            }
            if (respuesta.equals("n")) {
                return false;
            }
            System.out.println("Responde s o n.");
        }
    }
}