# Gestión de servicios — Barbería Cortes (Java + JDBC)

| | |
|---|---|
| **Evidencia** | GA7-220501096-AA2-EV01 — Codificación de módulos del software |
| **Actividad** | GA7-220501096-AA2 — Aplicar estándares de codificación, de acuerdo con el diseño |
| **Proyecto formativo** | Sistema de Gestión para Barbería — Barbería Cortes |
| **Aprendiz** | Edison Puerta Arroyave |
| **Ficha** | 3389005 |
| **Instructor** | Diego Manuel Dussan Muñoz |
| **Programa** | Tecnología en Análisis y Desarrollo de Software — SENA |
| **Fecha** | Noviembre de 2026 |
| **Repositorio** | https://github.com/Edi-p714/barberia-practica-jdbc |

Módulo del **Sistema de Gestión para Barbería** que administra el catálogo de servicios de la Barbería Cortes: registrar, consultar, actualizar y eliminar servicios en MySQL mediante JDBC. Es una aplicación de consola en Java 17 con Maven, organizada con el patrón DAO.

> **Alcance tecnológico.** El backend oficial del proyecto es PHP 8 con MySQL. Este módulo se construye en Java con JDBC porque así lo exige la guía de la actividad, como práctica de conexión a bases de datos con otra tecnología. Trabaja sobre el modelo de datos real del proyecto y no cambia el stack definido. Por eso la base de datos (`barberia_practica_jdbc`), el usuario de MySQL (`practica_jdbc`) y el repositorio llevan el sufijo *práctica JDBC*: se distinguen a simple vista de los del sistema real.

## 1. Relación con los artefactos del proyecto

- **Requisito de alcance:** gestión del catálogo de servicios, precios y duración estimada, función exclusiva del administrador.
- **Historia de usuario:** gestión del catálogo de servicios (HU01–HU12 del proyecto).
- **Prototipo:** pantalla **P-12 Gestión de servicios**, una de las 17 pantallas del prototipo navegable.
- **Arquitectura MVC:** familia de controladores **Gestión de catálogo**. En este módulo, `Servicio` es el modelo, `ServicioDAO` el acceso a datos y `Principal` reemplaza a la vista y al controlador.
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
- `precio` ≥ 0 y `duracion_minutos` > 0. Se validan en la entrada y además con restricciones `CHECK` en la tabla.
- `estado` es booleano: un servicio inactivo sigue registrado pero deja de ofrecerse.
- **No se borra un servicio que tenga historial.** En el modelo completo, `citas` referencia a `servicios` con borrado restringido. Si se intenta eliminar un servicio con citas, la base de datos lo rechaza y el programa recomienda desactivarlo en lugar de borrarlo.

## 2. Estructura

```text
barberia-practica-jdbc/
├── .vscode/launch.json          configuraciones de ejecución (cargan .env)
├── .env.example                 plantilla de credenciales
├── .gitignore                   excluye target/ y .env
├── pom.xml                      Maven: Java 17 + MySQL Connector/J
├── sql/01_crear_servicios.sql   base barberia_practica_jdbc y tabla servicios
└── src/main/java/com/barberiacortes/servicios/
    ├── Principal.java           menú de consola y lectura de datos
    ├── PruebaConexion.java      primer punto de control de la conexión
    ├── config/ConexionBD.java   apertura de conexiones JDBC
    ├── modelo/Servicio.java     representación de un registro
    └── dao/ServicioDAO.java     sentencias SQL del CRUD
```

## 3. Estándares de codificación aplicados

Se siguen las convenciones de nombres de Java (Oracle Code Conventions) y, en la base de datos, la convención del modelo del proyecto.

| Elemento | Convención | Ejemplos en el código |
|---|---|---|
| Paquetes | minúsculas, dominio invertido, sin guiones | `com.barberiacortes.servicios`, `com.barberiacortes.servicios.dao` |
| Clases | `PascalCase`, sustantivo | `Servicio`, `ServicioDAO`, `ConexionBD`, `PruebaConexion` |
| Métodos | `camelCase`, empiezan con verbo | `insertar`, `listar`, `buscarPorId`, `actualizar`, `eliminar`, `obtenerConexion`, `leerPrecio` |
| Variables y atributos | `camelCase`, nombres descriptivos | `nombreServicio`, `duracionMinutos`, `idGenerado`, `filasAfectadas` |
| Constantes | `MAYÚSCULAS_CON_GUION_BAJO`, `static final` | `URL_POR_DEFECTO`, `LONGITUD_MAXIMA_NOMBRE`, `SERVICIO_DAO` |
| Booleanos | prefijo de estado; getter con `is` | atributo `activo`, método `isActivo()` |
| Tablas y columnas SQL | `snake_case`, igual que el modelo de datos | `servicios`, `nombre_servicio`, `duracion_minutos` |

Otras prácticas aplicadas:
- **Una responsabilidad por clase.** Solo `ServicioDAO` escribe SQL y solo `Principal` lee del teclado.
- **`PreparedStatement` en todas las sentencias**, con marcadores `?`. Nunca se concatenan datos del usuario en el SQL, lo que evita la inyección SQL.
- **`try-with-resources`** en cada operación, para cerrar siempre `Connection`, `PreparedStatement` y `ResultSet`.
- **`BigDecimal` para el precio**, porque `double` produce errores de redondeo con dinero.
- **Credenciales fuera del código**, en `.env`, excluido del repositorio.
- **Atributos `private final` en el modelo**: un `Servicio` no cambia después de creado.
- Comentarios Javadoc en las clases y en los métodos públicos.

## 4. Requisitos

- JDK 17.
- Visual Studio Code con **Extension Pack for Java** (Microsoft) y Maven for Java.
- MySQL Server 8.x y MySQL Workbench. En el equipo del aprendiz se usó el MySQL 8.0.40 incluido en MAMP, en el puerto 8889; por eso su `.env` usa `localhost:8889`.
- Git.

## 5. Instalación y ejecución

1. **Base de datos.** En MySQL Workbench, abre y ejecuta `sql/01_crear_servicios.sql`. Se crea la base `barberia_practica_jdbc` con la tabla `servicios`. El script usa `IF NOT EXISTS`, así que no borra datos si se vuelve a ejecutar.
2. **Credenciales.** Copia `.env.example` como `.env` y escribe tu usuario y contraseña de MySQL.
3. **Abrir el proyecto.** En VS Code, *File → Open Folder* y selecciona la carpeta que contiene `pom.xml`. Espera a que Maven descargue Connector/J.
4. **Probar la conexión.** En *Run and Debug* (`Ctrl+Shift+D`) elige **Probar conexión JDBC** y pulsa `F5`. Debe aparecer `Conexión JDBC establecida correctamente.`
5. **Ejecutar el módulo.** Elige **Gestión de servicios (terminal)** y pulsa `F5`. El menú se abre en el terminal integrado, que es donde `Scanner` puede leer el teclado.

Java no lee `.env` por sí solo: lo cargan las configuraciones de `.vscode/launch.json`. Por eso hay que ejecutar desde *Run and Debug* y no con un botón genérico.

## 6. Pruebas de funcionamiento

| Caso | Acción en el menú | Resultado esperado |
|---|---|---|
| P01 Inserción | 1 — «Corte clásico», con descripción, precio 25000, 30 min | `Servicio registrado. ID generado: 1` |
| P02 Inserción sin descripción | 1 — «Corte infantil», Enter en descripción, 20000, 25 min | Se registra con ID 2; en el listado aparece `(sin descripción)` |
| P03 Validación | En P02, precio `-5` y duración `0` | El programa rechaza el valor y lo vuelve a pedir |
| P04 Consulta general | 2 | Lista los dos servicios y el total |
| P05 Consulta por ID | 3 — ID 1 | Muestra «Corte clásico» |
| P06 Actualización | 4 — ID 1, Enter en todo excepto precio 28000 | `Servicio actualizado correctamente.`; al consultar, precio 28000 |
| P07 Desactivación | 4 — ID 2, Enter en todo y `n` en «¿Servicio activo?» | El servicio queda `Inactivo` |
| P08 Eliminación | 5 — ID 2, confirmar con `s` | `Servicio eliminado correctamente.`; ya no aparece en el listado |
| P09 ID inexistente | 3 y 5 — ID 99 | `No existe un servicio con el ID 99.` |

Verificación en MySQL Workbench después de cada operación:

```sql
USE barberia_practica_jdbc;
SELECT * FROM servicios ORDER BY id_servicio;
```

## 7. Preguntas de la sesión

**¿Por qué necesita JDBC un controlador?**
JDBC es una API: define interfaces (`Connection`, `PreparedStatement`, `ResultSet`), pero no sabe comunicarse con ningún motor en particular. El controlador (*driver*) implementa esas interfaces para un motor específico y traduce las llamadas de Java al protocolo de red de ese servidor. Por eso el controlador debe corresponder al motor. Este proyecto usa MySQL Connector/J, declarado en `pom.xml`, y `DriverManager` lo selecciona a partir del prefijo `jdbc:mysql:` de la URL.

**¿Qué hacen `Connection` y `PreparedStatement`?**
`Connection` representa una sesión abierta con la base de datos. Se obtiene con `DriverManager.getConnection(url, usuario, contrasena)` y consume recursos del servidor, por eso se cierra al terminar. `PreparedStatement` es una sentencia SQL precompilada con marcadores `?`. Los valores se asignan después con `setString`, `setInt`, `setBigDecimal`, etc. y el controlador los envía separados del texto SQL, lo que evita la inyección SQL.

**¿Cuándo se usa `ResultSet`?**
Cuando la sentencia devuelve filas, es decir, en un `SELECT` ejecutado con `executeQuery()`. El `ResultSet` se recorre con `next()`, que avanza una fila y devuelve `false` al terminar, y los valores se leen por nombre de columna (`getString("nombre_servicio")`). Las sentencias que modifican datos (`INSERT`, `UPDATE`, `DELETE`) usan `executeUpdate()`, que devuelve el número de filas afectadas. En este módulo ese número indica si el ID existía.

**¿Qué operaciones forman parte del CRUD?**
*Create* (`INSERT`), *Read* (`SELECT`), *Update* (`UPDATE`) y *Delete* (`DELETE`). En el módulo corresponden a los métodos `insertar`, `listar` y `buscarPorId`, `actualizar` y `eliminar` de `ServicioDAO`.

## 8. Versionamiento

El proyecto se versiona con Git y se publica en GitHub; el enlace está en la portada de este documento. El historial muestra la construcción por etapas: configuración, script SQL, conexión, modelo, DAO, menú y documentación, un commit por etapa. El archivo `.env` nunca se sube: compruébalo con `git status` antes de cada `commit`.

## 9. Errores frecuentes

- **Access denied for user:** revisa `DB_USER`, `DB_PASSWORD` y los permisos del usuario en MySQL.
- **Communications link failure:** el servicio de MySQL no está iniciado o el puerto de `DB_URL` no es el correcto.
- **Unknown database 'barberia_practica_jdbc':** falta ejecutar `sql/01_crear_servicios.sql`.
- **No suitable driver:** Maven no ha descargado Connector/J. Espera la importación o ejecuta `mvn clean compile`.
- **Falta DB_USER o DB_PASSWORD:** no existe `.env` o no se ejecutó desde *Run and Debug*.
- **Las tildes salen como símbolos raros en Windows:** ejecuta `chcp 65001` en el terminal antes de iniciar el programa.

## Fuentes técnicas

- Oracle. *JDBC Basics* (The Java Tutorials). https://docs.oracle.com/javase/tutorial/jdbc/basics/
- Oracle. *Code Conventions for the Java Programming Language: Naming Conventions*. https://www.oracle.com/java/technologies/javase/codeconventions-namingconventions.html
- MySQL. *Connector/J Developer Guide: Installing using Maven*. https://dev.mysql.com/doc/connector-j/en/connector-j-installing-maven.html
- Microsoft. *Running and debugging Java* (Visual Studio Code). https://code.visualstudio.com/docs/java/java-debugging
