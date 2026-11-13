package co.edu.siga.controlador;
 
import co.edu.siga.configuracion.Configuracion;
import co.edu.siga.modelo.TokenRecuperacion;
import co.edu.siga.modelo.TokenRecuperacionModelo;
import co.edu.siga.modelo.Usuario;
import co.edu.siga.modelo.UsuarioModelo;
import co.edu.siga.servicio.CorreoServicio;
import co.edu.siga.servicio.SeguridadServicio;
import co.edu.siga.servicio.ValidadorDatos;
import com.google.gson.JsonObject;
import io.jsonwebtoken.Claims;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
 
/**
 * AutenticacionControlador  -  Paquete CONTROLADOR (Servlet)
 * Recibe las peticiones de la VISTA, valida, usa el MODELO y responde JSON.
 *   POST /api/autenticacion/registro     POST /api/autenticacion/login
 *   POST /api/autenticacion/recuperar    POST /api/autenticacion/restablecer
 *   GET  /api/autenticacion/perfil  (protegida por FiltroJwt)
 */
@WebServlet(name = "AutenticacionControlador", urlPatterns = {"/api/autenticacion/*"})
public class AutenticacionControlador extends HttpServlet {
 
    private static final Logger registro = Logger.getLogger(AutenticacionControlador.class.getName());
    private static final String MENSAJE_LOGIN_FALLIDO = "Credenciales incorrectas o el tipo de usuario no corresponde.";
    private static final String MENSAJE_RECUPERACION =
            "Si el correo está registrado, recibirás un enlace para restablecer la contraseña.";
    private static final String MENSAJE_CLAVE_DEBIL =
            "La contraseña necesita mínimo 8 caracteres, una mayúscula, una minúscula y un número.";
 
    @Override
    protected void doPost(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        String accion = peticion.getPathInfo() == null ? "" : peticion.getPathInfo();
        try {
            switch (accion) {
                case "/registro" -> registrarUsuario(peticion, respuesta);
                case "/login" -> iniciarSesion(peticion, respuesta);
                case "/recuperar" -> solicitarRecuperacion(peticion, respuesta);
                case "/restablecer" -> restablecerClave(peticion, respuesta);
                default -> RespuestaJson.responder(respuesta, false, "Recurso no encontrado.", null, 404);
            }
        } catch (SQLException | RuntimeException errorInterno) {
            manejarError(respuesta, errorInterno);
        }
    }
 
    @Override
    protected void doGet(HttpServletRequest peticion, HttpServletResponse respuesta) throws IOException {
        try {
            if ("/perfil".equals(peticion.getPathInfo())) {
                obtenerPerfil(peticion, respuesta);
            } else {
                RespuestaJson.responder(respuesta, false, "Recurso no encontrado.", null, 404);
            }
        } catch (SQLException | RuntimeException errorInterno) {
            manejarError(respuesta, errorInterno);
        }
    }
 
    private void registrarUsuario(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException, SQLException {
        JsonObject cuerpo = RespuestaJson.leerCuerpo(peticion);
        Map<String, String> datos = new HashMap<>();
        for (String nombreCampo : List.of("tipoUsuario", "nombreCompleto", "documento", "correo", "clave", "confirmarClave")) {
            datos.put(nombreCampo, RespuestaJson.leerTexto(cuerpo, nombreCampo));
        }
        datos.put("correo", datos.get("correo").toLowerCase());
 
        List<String> errores = ValidadorDatos.validarRegistro(datos);
        if (!errores.isEmpty()) {
            RespuestaJson.responder(respuesta, false, errores.get(0), Map.of("errores", errores), 400);
            return;
        }
 
        UsuarioModelo usuarioModelo = new UsuarioModelo();
        if (usuarioModelo.existeCorreo(datos.get("correo"))) {
            RespuestaJson.responder(respuesta, false, "Ya existe una cuenta con ese correo.", null, 409);
            return;
        }
        if (usuarioModelo.existeDocumento(datos.get("documento"))) {
            RespuestaJson.responder(respuesta, false, "Ya existe una cuenta con ese documento.", null, 409);
            return;
        }
 
        Usuario nuevoUsuario = new Usuario(datos.get("nombreCompleto"), datos.get("documento"), datos.get("correo"),
                SeguridadServicio.generarHashClave(datos.get("clave")),   // ¡se guarda el HASH!
                datos.get("tipoUsuario"));
        usuarioModelo.insertar(nuevoUsuario);
        RespuestaJson.responder(respuesta, true,
                "¡Bienvenido(a), " + nuevoUsuario.getNombreCompleto() + "! Tu cuenta fue creada.", null, 201);
    }
 
    private void iniciarSesion(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException, SQLException {
        JsonObject cuerpo = RespuestaJson.leerCuerpo(peticion);
        String valorUsuario = RespuestaJson.leerTexto(cuerpo, "usuario").toLowerCase();
        String clave = RespuestaJson.leerTexto(cuerpo, "clave");
        String tipoUsuario = RespuestaJson.leerTexto(cuerpo, "tipoUsuario");
        boolean recordarDispositivo = cuerpo.has("recordarDispositivo")
                && cuerpo.get("recordarDispositivo").isJsonPrimitive()
                && cuerpo.get("recordarDispositivo").getAsBoolean();
 
        if (valorUsuario.isEmpty() || clave.isEmpty() || !ValidadorDatos.esTipoUsuarioValido(tipoUsuario)) {
            RespuestaJson.responder(respuesta, false, "Complete el tipo de usuario, el usuario y la contraseña.", null, 400);
            return;
        }
 
        UsuarioModelo usuarioModelo = new UsuarioModelo();
        Usuario usuario = usuarioModelo.buscarPorCorreoODocumento(valorUsuario);
 
        // Mismo mensaje para todos los fallos: no se revela si el usuario existe.
        if (usuario == null || !usuario.isEstado() || !usuario.getTipoUsuario().equals(tipoUsuario)
                || !SeguridadServicio.verificarClave(clave, usuario.getClaveHash())) {
            RespuestaJson.responder(respuesta, false, MENSAJE_LOGIN_FALLIDO, null, 401);
            return;
        }
 
        usuarioModelo.registrarIngreso(usuario.getIdUsuario());
        Map<String, Object> datosRespuesta = new HashMap<>();
        datosRespuesta.put("token", SeguridadServicio.generarJwt(usuario, recordarDispositivo));
        datosRespuesta.put("usuario", usuario.aMapaPublico());
        RespuestaJson.responder(respuesta, true, "Bienvenido(a), " + usuario.getNombreCompleto() + ".", datosRespuesta, 200);
    }
 
    private void solicitarRecuperacion(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException, SQLException {
        String correo = RespuestaJson.leerTexto(RespuestaJson.leerCuerpo(peticion), "correo").toLowerCase();
        if (!ValidadorDatos.esCorreoValido(correo)) {
            RespuestaJson.responder(respuesta, false, "Escriba un correo electrónico válido.", null, 400);
            return;
        }
 
        Usuario usuario = new UsuarioModelo().buscarPorCorreo(correo);
        if (usuario != null && usuario.isEstado()) {
            TokenRecuperacionModelo tokenModelo = new TokenRecuperacionModelo();
            tokenModelo.invalidarTokensDeUsuario(usuario.getIdUsuario());
            String tokenPlano = SeguridadServicio.generarTokenRecuperacion();        // va en el correo
            String tokenHash = SeguridadServicio.calcularHashToken(tokenPlano);      // va en la BD
            tokenModelo.insertar(usuario.getIdUsuario(), tokenHash, Configuracion.tokenMinutosExpiracion());
            if (!CorreoServicio.enviarCorreoRecuperacion(usuario.getCorreo(), usuario.getNombreCompleto(), tokenPlano)) {
                registro.severe("Falló el envío de recuperación para el usuario " + usuario.getIdUsuario());
            }
        }
        RespuestaJson.responder(respuesta, true, MENSAJE_RECUPERACION, null, 200);
    }
 
    private void restablecerClave(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException, SQLException {
        JsonObject cuerpo = RespuestaJson.leerCuerpo(peticion);
        String tokenPlano = RespuestaJson.leerTexto(cuerpo, "token");
        String nuevaClave = RespuestaJson.leerTexto(cuerpo, "nuevaClave");
 
        if (tokenPlano.isEmpty()) {
            RespuestaJson.responder(respuesta, false, "El enlace no es válido.", null, 400);
            return;
        }
        if (!ValidadorDatos.esClaveSegura(nuevaClave)) {
            RespuestaJson.responder(respuesta, false, MENSAJE_CLAVE_DEBIL, null, 400);
            return;
        }
        if (!nuevaClave.equals(RespuestaJson.leerTexto(cuerpo, "confirmarClave"))) {
            RespuestaJson.responder(respuesta, false, "Las contraseñas no coinciden.", null, 400);
            return;
        }
 
        TokenRecuperacionModelo tokenModelo = new TokenRecuperacionModelo();
        TokenRecuperacion tokenGuardado = tokenModelo.buscarPorHash(SeguridadServicio.calcularHashToken(tokenPlano));
        if (tokenGuardado == null || !tokenGuardado.estaVigente(LocalDateTime.now())) {
            RespuestaJson.responder(respuesta, false, "El enlace no es válido o ya expiró. Solicite uno nuevo.", null, 400);
            return;
        }
 
        // Primero se "gasta" el token: si dos peticiones llegan a la vez, solo una pasa (un solo uso real).
        if (!tokenModelo.marcarComoUsado(tokenGuardado.getIdToken())) {
            RespuestaJson.responder(respuesta, false, "El enlace no es válido o ya expiró. Solicite uno nuevo.", null, 400);
            return;
        }
        new UsuarioModelo().actualizarClave(tokenGuardado.getIdUsuario(), SeguridadServicio.generarHashClave(nuevaClave));
        RespuestaJson.responder(respuesta, true, "Contraseña actualizada correctamente.", null, 200);
    }
 
    private void obtenerPerfil(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws IOException, SQLException {
        Claims cargaUtil = (Claims) peticion.getAttribute("cargaUtilJwt");   // lo dejó el FiltroJwt
        Usuario usuario = new UsuarioModelo().buscarPorId(Integer.parseInt(cargaUtil.getSubject()));
        if (usuario == null || !usuario.isEstado()) {
            RespuestaJson.responder(respuesta, false, "Usuario no encontrado.", null, 404);
            return;
        }
        Map<String, Object> datosPerfil = usuario.aMapaPublico();
        datosPerfil.put("backend", "Java · Maven + Tomcat");
        RespuestaJson.responder(respuesta, true, "Perfil consultado.", datosPerfil, 200);
    }
 
    private void manejarError(HttpServletResponse respuesta, Exception errorInterno) throws IOException {
        registro.log(Level.SEVERE, "Error no controlado", errorInterno);
        RespuestaJson.responder(respuesta, false, "Ocurrió un error interno. Intente más tarde.", null, 500);
    }
}
