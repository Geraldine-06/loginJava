package co.edu.siga.servicio.excepciones;

import java.util.List;

/**
 * ErrorAplicacion  -  Paquete SERVICIO (excepciones)  ·  Taller 2
 * Clase base de las excepciones PROPIAS de SIGA. Cada una sabe qué código
 * HTTP le corresponde, así el controlador solo hace "throw" y ControladorBase
 * (manejador central) la convierte en la respuesta JSON estándar:
 *     { "exito": false, "mensaje": "...", "datos": {"errores": [...]} }
 *
 * Hereda de RuntimeException (excepción NO verificada): no obliga a escribir
 * "throws" en cada método y viaja sola hasta el manejador central.
 *
 * Jerarquía:
 *     ErrorAplicacion (400)
 *      ├── ErrorValidacion       400  datos del formulario incorrectos
 *      ├── ErrorNoAutorizado     401  falta iniciar sesión / JWT vencido
 *      ├── ErrorPermiso          403  el perfil no puede hacer esa acción
 *      ├── ErrorNoEncontrado     404  el registro no existe
 *      ├── ErrorConflicto        409  duplicado o registro en uso
 *      │    ├── ErrorRegistroDuplicado   (llave primaria o UNIQUE repetido)
 *      │    ├── ErrorRegistroEnUso       (otra tabla depende del registro)
 *      │    └── ErrorReferenciaInvalida  (apunta a un registro que no existe)
 *      └── ErrorBaseDatos        503  no hay conexión con MySQL / SQL Server
 */
public class ErrorAplicacion extends RuntimeException {

    private final List<String> errores;   // lista de mensajes (validaciones de formulario)

    public ErrorAplicacion(String mensaje) {
        this(mensaje, List.of());
    }

    public ErrorAplicacion(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = errores == null ? List.of() : List.copyOf(errores);
    }

    /** "causa" guarda el error original (por ejemplo la SQLException) para el registro / log. */
    public ErrorAplicacion(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.errores = List.of();
    }

    /** Código HTTP de la respuesta. Las subclases lo cambian (sobrescritura). */
    public int getCodigoEstado() { return 400; }

    public String getMensaje() { return getMessage(); }

    public List<String> getErrores() { return errores; }
}
