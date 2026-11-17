# Gestión de servicios — Barbería Cortes (Java, JDBC, Servlets y JSP)

| | |
|---|---|
| **Evidencia** | GA7-220501096-AA2-EV02 — Módulos de software codificados y probados |
| **Actividad** | GA7-220501096-AA2 — Aplicar estándares de codificación, de acuerdo con el diseño |
| **Proyecto formativo** | Sistema de Gestión para Barbería — Barbería Cortes |
| **Aprendiz** | Edison Puerta Arroyave |
| **Ficha** | 3389005 |
| **Instructor** | Diego Manuel Dussan Muñoz |
| **Programa** | Tecnología en Análisis y Desarrollo de Software — SENA |
| **Fecha** | Noviembre de 2026 |
| **Repositorio** | https://github.com/Edi-p714/barberia-practica-jdbc |

Módulo del **Sistema de Gestión para Barbería** que administra el catálogo de servicios de la Barbería Cortes: registrar, consultar, actualizar y eliminar servicios en MySQL. Está hecho en Java 17 con Maven y tiene dos interfaces sobre el mismo acceso a datos:

- **Web (EV02):** formularios HTML que envían a un servlet, con métodos GET y POST, y vistas en JSP. Se ejecuta en Apache Tomcat 10.1.
- **Consola (EV01):** menú de texto que se ejecuta desde VS Code.

> **Versiones entregadas.** Este repositorio guarda las dos evidencias en un mismo historial, marcadas con etiquetas de Git:
> - **EV01** (consola con JDBC), tal como se entregó: https://github.com/Edi-p714/barberia-practica-jdbc/tree/ev01
> - **EV02** (web con Servlets y JSP): https://github.com/Edi-p714/barberia-practica-jdbc/tree/ev02

> **Alcance tecnológico.** El backend oficial del proyecto es PHP 8 con MySQL. Este módulo se construye en Java porque así lo exige la guía de la actividad, como práctica con otra tecnología. Trabaja sobre el modelo de datos real del proyecto y no cambia el stack definido. Por eso la base de datos (`barberia_practica_jdbc`), el usuario de MySQL (`practica_jdbc`) y el repositorio llevan el sufijo *práctica JDBC*: se distinguen a simple vista de los del sistema real.

## 1. Relación con los artefactos del proyecto

- **Requisito de alcance:** gestión del catálogo de servicios, precios y duración estimada, función exclusiva del administrador.
- **Historia de usuario:** gestión del catálogo de servicios (HU01–HU12 del proyecto).
- **Prototipo:** pantalla **P-12 Gestión de servicios**. La versión web usa los colores del sistema de diseño del proyecto (tema oscuro con acento dorado).
- **Arquitectura MVC:** familia de controladores **Gestión de catálogo**. En la versión web la separación es completa:

| Capa MVC | Clase o archivo | Responsabilidad |
|---|---|---|
| Modelo | `Servicio`, `ServicioDAO`, `ConexionBD` | Datos del servicio y acceso a MySQL con JDBC |
| Vista | `lista.jsp`, `formulario.jsp`, `encabezado.jspf` | Mostrar datos y formularios; no consultan la base de datos |
| Controlador | `ServicioServlet` | Recibir cada petición GET o POST, validar, llamar al DAO y elegir la vista |

- **Diagrama de clases:** clase de dominio `Servicio`.
- **Modelo de datos:** tabla `servicios`, normalizada en 3FN. Se respetan los mismos atributos y tipos:

| Columna | Tipo | Atributo en Java |
|---|---|---|
| `id_servicio` | `INT AUTO_INCREMENT` PK | `idServicio` (`int`) |
| `nombre_servicio` | `VARCHAR(80)` | `nombreServicio` (`String`) |
| `descripcion` | `VARCHAR(255)` | `descripcion` (`String`) |
| `precio` | `DECIMAL(10,2)` | `precio` (`BigDecimal`) |
| `duracion_minutos` | `INT` | `duracionMinutos` (`int`) |
| `estado` | `TINYINT(1)` | `activo` (`boolean`) |

**Reglas del modelo que el código respeta:**
- `precio` ≥ 0 y `duracion_minutos` > 0. Se validan en el formulario, en el servlet y con restricciones `CHECK` en la tabla.
- `estado` es booleano: un servicio inactivo sigue registrado pero deja de ofrecerse.
- **No se borra un servicio que tenga historial.** En el modelo completo, `citas` referencia a `servicios` con borrado restringido. Si se intenta eliminar un servicio con citas, la base de datos lo rechaza y la aplicación recomienda desactivarlo.

## 2. Estructura

```text
barberia-practica-jdbc/
├── .vscode/launch.json            ejecución de la versión de consola (carga .env)
├── .env.example                   plantilla de credenciales
├── .gitignore                     excluye target/ y .env
├── pom.xml                        Maven: WAR, Java 17, Connector/J y API de Servlets 6.0
├── docs/capturas/                 capturas de las pruebas W01 a W12
├── sql/01_crear_servicios.sql     base barberia_practica_jdbc y tabla servicios
└── src/main/
    ├── java/com/barberiacortes/servicios/
    │   ├── config/ConexionBD.java      apertura de conexiones JDBC
    │   ├── modelo/Servicio.java        representación de un registro
    │   ├── dao/ServicioDAO.java        sentencias SQL del CRUD
    │   ├── web/ServicioServlet.java    controlador web (GET y POST)
    │   ├── util/TextoHtml.java         escape de texto antes de mostrarlo en HTML
    │   ├── Principal.java              menú de consola (EV01)
    │   └── PruebaConexion.java         prueba de la conexión
    └── webapp/
        ├── index.jsp                   redirige al listado
        ├── css/estilos.css             colores del sistema de diseño
        └── WEB-INF/vistas/
            ├── fragmentos/encabezado.jspf
            └── servicios/
                ├── lista.jsp           listado, consulta por ID y eliminación
                └── formulario.jsp      registro y edición
```

Las vistas están dentro de `WEB-INF`, que Tomcat no entrega directamente al navegador: solo se llega a ellas a través del servlet. Así ninguna página se muestra sin pasar por el controlador.

## 3. Cómo funciona la versión web

Todas las peticiones llegan a una sola ruta, `/servicios`, y el parámetro `accion` indica qué hacer.

| Método | Petición | Qué hace | Vista |
|---|---|---|---|
| GET | `/servicios` | Lista todos los servicios | `lista.jsp` |
| GET | `/servicios?accion=buscar&id=1` | Consulta un servicio por ID | `lista.jsp` |
| GET | `/servicios?accion=nuevo` | Muestra el formulario vacío | `formulario.jsp` |
| GET | `/servicios?accion=editar&id=1` | Muestra el formulario con los datos | `formulario.jsp` |
| POST | `/servicios` con `accion=guardar` | Inserta (sin ID) o actualiza (con ID) | Redirige al listado |
| POST | `/servicios` con `accion=eliminar` | Elimina el servicio | Redirige al listado |

**Por qué GET o POST en cada caso.** GET solo **lee**: sus datos van en la URL, se pueden guardar como marcador y repetir sin riesgo. POST **cambia** datos: los envía en el cuerpo de la petición. Por eso el botón *Eliminar* es un pequeño formulario con POST y no un enlace: un enlace usa GET, y un navegador o buscador podría seguirlo y borrar datos sin que nadie lo pida.

**Post/Redirect/Get.** Después de un POST exitoso, el servlet no muestra una página: redirige al listado con un código de mensaje (`?mensaje=creado`). Así, si se recarga la página, el navegador repite el GET del listado y no el POST, y el servicio no se registra dos veces.

**Elementos de JSP usados:**

| Elemento | Sintaxis | Dónde se usa |
|---|---|---|
| Directiva `page` | `<%@ page contentType=... import=... %>` | Codificación UTF-8 e importación de clases en cada vista |
| Directiva `include` | `<%@ include file="..." %>` | Incorporar el encabezado común `encabezado.jspf` |
| Scriptlet | `<% ... %>` | Recorrer la lista de servicios y mostrar avisos según el caso |
| Expresión | `<%= ... %>` | Escribir valores en la página: ID, nombre, precio, rutas |
| Comentario JSP | `<%-- ... --%>` | Notas que no llegan al navegador |
| Objetos implícitos | `request`, `response` | Leer los atributos que entrega el servlet y redirigir |

**Seguridad básica:**
- Todo texto que viene de la base de datos o del usuario pasa por `TextoHtml.escapar()` antes de mostrarse, para que un nombre como `<script>` no se ejecute en el navegador (XSS).
- Los mensajes de la URL llegan como un código (`creado`, `enUso`) y el servlet los traduce a un texto propio: nunca se muestra en la página lo que venga escrito en la URL.
- Todas las sentencias SQL siguen usando `PreparedStatement`.

## 4. Estándares de codificación aplicados

Se siguen las convenciones de nombres de Java (Oracle Code Conventions) y, en la base de datos, la convención del modelo del proyecto.

| Elemento | Convención | Ejemplos en el código |
|---|---|---|
| Paquetes | minúsculas, dominio invertido, sin guiones | `com.barberiacortes.servicios.web`, `com.barberiacortes.servicios.dao` |
| Clases | `PascalCase`, sustantivo | `Servicio`, `ServicioDAO`, `ServicioServlet`, `TextoHtml` |
| Métodos | `camelCase`, empiezan con verbo | `buscarPorId`, `mostrarFormulario`, `redirigirAlListado`, `escapar` |
| Variables y atributos | `camelCase`, nombres descriptivos | `nombreServicio`, `duracionMinutos`, `codigoMensaje`, `esEdicion` |
| Constantes | `MAYÚSCULAS_CON_GUION_BAJO`, `static final` | `VISTA_LISTA`, `MENSAJES_EXITO`, `LONGITUD_MAXIMA_NOMBRE` |
| Booleanos | prefijo de estado; getter con `is` | `activo`, `isActivo()`, `esBusqueda`, `esEdicion` |
| Vistas y recursos web | minúsculas, en español | `lista.jsp`, `formulario.jsp`, `estilos.css` |
| Tablas y columnas SQL | `snake_case`, igual que el modelo de datos | `servicios`, `nombre_servicio`, `duracion_minutos` |

Nombres de clases, métodos y variables sin tildes ni ñ; los textos que ve el usuario sí las llevan.

Otras prácticas aplicadas:
- **Una responsabilidad por clase.** Solo `ServicioDAO` escribe SQL; solo `ServicioServlet` decide qué hacer con cada petición; las JSP solo muestran.
- **El mismo DAO sirve a la consola y a la web.** La capa de datos no cambió de EV01 a EV02: solo se agregó una interfaz nueva.
- **`try-with-resources`** en cada operación con la base de datos.
- **`BigDecimal` para el precio**, porque `double` produce errores de redondeo con dinero.
- **Credenciales fuera del código**, en `.env`, excluido del repositorio.
- Comentarios Javadoc en las clases y en los métodos públicos.

## 5. Requisitos

- JDK 17.
- Apache Tomcat **10.1** (Servlet 6.0 y JSP 3.1). Tomcat 9 no sirve: usa los paquetes `javax.servlet`, y este proyecto usa `jakarta.servlet`.
- Maven, Git y Visual Studio Code con **Extension Pack for Java**.
- MySQL 8.x y MySQL Workbench. En el equipo del aprendiz se usó el MySQL 8.0.40 incluido en MAMP, en el puerto 8889; por eso su `.env` usa `localhost:8889`.

## 6. Instalación y ejecución

**Comunes a las dos versiones:**

1. **Base de datos.** En MySQL Workbench, ejecuta `sql/01_crear_servicios.sql`.
2. **Credenciales.** Copia `.env.example` como `.env` y escribe tu usuario y contraseña de MySQL.

**Versión web:**

3. **Credenciales para Tomcat.** Tomcat no lee el `.env` del proyecto por sí solo. Crea el archivo `bin/setenv.sh` dentro de la carpeta de Tomcat con este contenido (ajusta la ruta del `.env`):
   ```bash
   set -a
   . "$HOME/Proyectos/barberia-practica-jdbc/.env"
   set +a
   ```
4. **Empaquetar:** `mvn clean package` genera `target/barberia-practica-jdbc.war`.
5. **Desplegar:** copia el WAR a la carpeta `webapps/` de Tomcat e inícialo con `bin/startup.sh`.
6. **Abrir:** http://localhost:8080/barberia-practica-jdbc/

**Versión de consola:** en VS Code, *Run and Debug* → **Gestión de servicios (terminal)** → `F5`.

## 7. Pruebas de la versión web

Ejecutadas en el navegador, con la tabla `servicios` vacía al empezar. La captura de cada caso está en [`docs/capturas/`](docs/capturas/), con el nombre del caso (`W01.png` a `W12.png`).

| Caso | Acción | Método | Resultado esperado |
|---|---|---|---|
| [W01 Listado vacío](docs/capturas/W01.png) | Abrir la aplicación | GET | «Aún no hay servicios registrados.» |
| [W02 Registro](docs/capturas/W02.png) | *Registrar servicio* → «Corte clásico», descripción, 25000, 30 min | GET + POST | Aviso verde «Servicio registrado correctamente.» y el servicio en la tabla |
| [W03 Validación](docs/capturas/W03.png) | Registrar con precio `-5` y duración `0` | POST | El navegador lo impide. Si se fuerza el envío, el servlet devuelve el formulario con los errores y conserva lo escrito |
| [W04 Registro sin descripción](docs/capturas/W04.png) | «Corte infantil», descripción vacía, 20000, 25 min | POST | En la tabla aparece «—» en la descripción |
| [W05 Consulta por ID](docs/capturas/W05.png) | *Consultar por ID* → 1 | GET | Solo aparece «Corte clásico»; la URL muestra `accion=buscar&id=1` |
| [W06 ID inexistente](docs/capturas/W06.png) | *Consultar por ID* → 99 | GET | Aviso rojo «No existe un servicio con el ID 99.» |
| [W07 Edición](docs/capturas/W07.png) | *Editar* en el servicio 1 → precio 28000 → *Guardar cambios* | GET + POST | Aviso «Servicio actualizado correctamente.» y precio $28000.00 |
| [W08 Desactivación](docs/capturas/W08.png) | *Editar* el servicio 2 → desmarcar *Servicio activo* → guardar | POST | El servicio 2 aparece como «Inactivo» |
| [W09 Eliminación](docs/capturas/W09.png) | *Eliminar* en el servicio 2 → confirmar | POST | Aviso «Servicio eliminado correctamente.» |
| [W10 Recarga tras guardar](docs/capturas/W10.png) | Pulsar `Cmd + R` después de W09 | GET | No pide reenviar el formulario ni repite la operación |
| [W11 Texto con HTML](docs/capturas/W11.png) | Registrar un servicio llamado `<b>prueba</b>` | POST | Se muestra el texto literal `<b>prueba</b>`, sin negrita; luego se elimina |
| [W12 Edición inexistente](docs/capturas/W12.png) | Abrir `/servicios?accion=editar&id=99` | GET | Vuelve al listado con el aviso «El servicio solicitado no existe.» |

Verificación en MySQL Workbench:

```sql
USE barberia_practica_jdbc;
SELECT * FROM servicios ORDER BY id_servicio;
```

Las pruebas de la versión de consola (P01 a P09) están en el README de la etiqueta `ev01`.

## 8. Versionamiento

El proyecto se versiona con Git y se publica en GitHub. Cada entrega queda marcada con una etiqueta (`ev01`, `ev02`), de modo que la versión entregada se puede consultar aunque el proyecto siga avanzando. El historial muestra la evolución de consola a web, un commit por etapa. El archivo `.env` nunca se sube.

## 9. Errores frecuentes

- **404 al abrir la aplicación:** el WAR no está en `webapps/` o Tomcat no terminó de desplegarlo. Revisa `logs/catalina.out`.
- **Aviso «No fue posible acceder a la base de datos»:** MySQL está apagado, o Tomcat no cargó las credenciales. Revisa `bin/setenv.sh` y el detalle en `logs/localhost.*.log`.
- **«No suitable driver» en el log:** dentro de Tomcat el controlador JDBC no se registra solo; `ConexionBD` lo carga con `Class.forName`. Si aparece, el WAR no incluye Connector/J: vuelve a ejecutar `mvn clean package`.
- **Tildes dañadas en la página:** cada JSP y cada fragmento `.jspf` debe declarar `pageEncoding="UTF-8"`.
- **`Address already in use` al iniciar Tomcat:** ya hay otro Tomcat en el puerto 8080. Detén el anterior con `bin/shutdown.sh`.

## Fuentes técnicas

- Apache Software Foundation. *Apache Tomcat 10.1 Documentation*. https://tomcat.apache.org/tomcat-10.1-doc/
- Eclipse Foundation. *Jakarta Servlet 6.0 Specification*. https://jakarta.ee/specifications/servlet/6.0/
- Eclipse Foundation. *Jakarta Server Pages 3.1 Specification*. https://jakarta.ee/specifications/pages/3.1/
- Oracle. *JDBC Basics* (The Java Tutorials). https://docs.oracle.com/javase/tutorial/jdbc/basics/
- Oracle. *Code Conventions for the Java Programming Language: Naming Conventions*. https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html