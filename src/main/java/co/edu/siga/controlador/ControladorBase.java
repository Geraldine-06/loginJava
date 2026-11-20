package co.edu.siga.controlador;

import co.edu.siga.servicio.SeguridadServicio;
import co.edu.siga.servicio.excepciones.ErrorAplicacion;
import co.edu.siga.servicio.excepciones.ErrorNoEncontrado;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * ControladorBase  -  Paquete CONTROLADOR  ·  Taller 2
 * Clase padre de todos los servlets. Hace dos trabajos para que los hijos
 * no repitan código:
 *
 * 1) MANEJO CENTRAL DE EXCEPCIONES (un solo try/catch para toda la API):
 *      ErrorAplicacion (y sus hijas)  -> su código HTTP (400, 401, 403, 404, 409, 503)
 *      cualquier otra excepción       -> 500 + registro en el log de Tomcat
 *    Los controladores solo hacen "throw new ErrorNoEncontrado(...)".
 *
 * 2) ENRUTAMIENTO CRUD leyendo getPathInfo():
 *      GET    /api/areas      -> listar          POST   /api/areas      -> crear
 *      GET    /api/areas/7    -> consultar("7")  PUT    /api/areas/7    -> actualizar("7")
 *                                                DELETE /api/areas/7    -> eliminar("7")
 *    POST, PUT y DELETE exigen el perfil Coordinador (equivale a @requiereGestion
 *    de Flask). El FiltroJwt ya se ejecutó antes, así que el usuario está autenticado.
 */
public abstract class ControladorBase extends HttpServlet {

    private static final Logger registro = Logger.getLogger("siga");
    private static final Pattern PATRON_LLAVE_NUMERICA = Pattern.compile("\\d{1,9}");
    protected static final String MENSAJE_NO_ENCONTRADO = "Recurso no encontrado.";

    /** Tomcat llama a service() en CADA petición: aquí está el try/catch central. */
    @Override
    protected void service(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        try {
            procesarPeticion(peticion, respuesta);
        } catch (ErrorAplicacion errorAplicacion) {                  // nuestras excepciones
            Object datos = errorAplicacion.getErrores().isEmpty()
                    ? null : Map.of("errores", errorAplicacion.getErrores());
            RespuestaJson.responder(respuesta, false, errorAplicacion.getMensaje(), datos,
                    errorAplicacion.getCodigoEstado());
        } catch (Exception errorInterno) {                           // lo inesperado: 500
            registro.log(Level.SEVERE, "Error no controlado", errorInterno);
            RespuestaJson.responder(respuesta, false, "Ocurrió un error interno. Intente más tarde.", null, 500);
        }
    }

    /** Enrutamiento CRUD por defecto. AutenticacionControlador y DashboardControlador lo reemplazan. */
    protected void procesarPeticion(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws SQLException, IOException {
        String llave = obtenerLlave(peticion);                      // null = la colección completa
        String metodoHttp = peticion.getMethod();

        if (llave == null && "GET".equals(metodoHttp)) {
            listar(peticion, respuesta);
        } else if (llave == null && "POST".equals(metodoHttp)) {
            exigirPermisoGestion(peticion);
            crear(peticion, respuesta);
        } else if (llave != null && "GET".equals(metodoHttp)) {
            consultar(peticion, respuesta, llave);
        } else if (llave != null && "PUT".equals(metodoHttp)) {
            exigirPermisoGestion(peticion);
            actualizar(peticion, respuesta, llave);
        } else if (llave != null && "DELETE".equals(metodoHttp)) {
            exigirPermisoGestion(peticion);
            eliminar(peticion, respuesta, llave);
        } else {
            responderMetodoNoPermitido(respuesta);
        }
    }

    /**
     * Lee la llave de la URL:  /api/areas -> null   /api/areas/7 -> "7".
     * Si el módulo usa llave numérica y llega "abc" (o una ruta más larga) responde 404, como Flask.
     */
    protected String obtenerLlave(HttpServletRequest peticion) {
        String rutaExtra = peticion.getPathInfo();
        if (rutaExtra == null || rutaExtra.equals("/")) {
            return null;
        }
        String llave = rutaExtra.substring(1);
        if (llave.contains("/") || (usaLlaveNumerica() && !PATRON_LLAVE_NUMERICA.matcher(llave).matches())) {
            throw new ErrorNoEncontrado(MENSAJE_NO_ENCONTRADO);
        }
        return llave;
    }

    /** Áreas y materias (código numérico) y usuarios (id) devuelven true. */
    protected boolean usaLlaveNumerica() {
        return false;
    }

    // --- Acciones CRUD: cada controlador sobrescribe las que usa ---
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws SQLException, IOException {
        responderMetodoNoPermitido(respuesta);
    }

    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        responderMetodoNoPermitido(respuesta);
    }

    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws SQLException, IOException {
        responderMetodoNoPermitido(respuesta);
    }

    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        responderMetodoNoPermitido(respuesta);
    }

    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        responderMetodoNoPermitido(respuesta);
    }

    // --- Utilidades para los controladores hijos ---
    protected void responderMetodoNoPermitido(HttpServletResponse respuesta) throws IOException {
        RespuestaJson.responder(respuesta, false, "Método no permitido para esta ruta.", null, 405);
    }

    /** Carga útil del JWT que dejó el FiltroJwt (sub, nombre, tipoUsuario...). */
    protected Claims obtenerCargaUtil(HttpServletRequest peticion) {
        return (Claims) peticion.getAttribute("cargaUtilJwt");
    }

    /** idUsuario de quien inició sesión (el "sub" del JWT). */
    protected int obtenerIdUsuarioSesion(HttpServletRequest peticion) {
        return Integer.parseInt(obtenerCargaUtil(peticion).getSubject());
    }

    /** Autorización por perfil: solo el Coordinador crea, edita y elimina (si no -> 403). */
    protected void exigirPermisoGestion(HttpServletRequest peticion) {
        SeguridadServicio.exigirPermisoGestion(obtenerCargaUtil(peticion));
    }
}
