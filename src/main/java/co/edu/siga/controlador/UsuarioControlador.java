package co.edu.siga.controlador;

import co.edu.siga.modelo.Usuario;
import co.edu.siga.modelo.UsuarioModelo;
import co.edu.siga.servicio.SeguridadServicio;
import co.edu.siga.servicio.ValidadorDatos;
import co.edu.siga.servicio.excepciones.ErrorConflicto;
import co.edu.siga.servicio.excepciones.ErrorNoEncontrado;
import co.edu.siga.servicio.excepciones.ErrorValidacion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * UsuarioControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2 (Gestión de Usuarios)
 * Rutas (prefijo /api/usuarios)              Permiso
 *     GET    ""                 listar       cualquier perfil con sesión
 *     GET    "/sin-funcionario" para el formulario de funcionarios
 *     GET    "/{id}"            consultar uno
 *     POST   ""                 crear        solo Coordinador
 *     PUT    "/{id}"            actualizar   solo Coordinador
 *     DELETE "/{id}"            eliminar     solo Coordinador
 * Los errores se LANZAN (throw) y ControladorBase los convierte en JSON.
 * Por eso aquí no hay try/catch repetidos.
 */
@WebServlet(name = "UsuarioControlador", urlPatterns = {"/api/usuarios/*"})
public class UsuarioControlador extends ControladorBase {

    @Override
    protected boolean usaLlaveNumerica() {
        return true;
    }

    /** Agrega la ruta especial /sin-funcionario y deja el resto al enrutamiento CRUD de ControladorBase. */
    @Override
    protected void procesarPeticion(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws SQLException, IOException {
        if ("GET".equals(peticion.getMethod()) && "/sin-funcionario".equals(peticion.getPathInfo())) {
            listarSinFuncionario(respuesta);
            return;
        }
        super.procesarPeticion(peticion, respuesta);
    }

    private Usuario buscarUsuarioOFallar(UsuarioModelo usuarioModelo, int idUsuario) throws SQLException {
        Usuario usuario = usuarioModelo.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new ErrorNoEncontrado("No existe el usuario con id " + idUsuario + ".");
        }
        return usuario;
    }

    @Override
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        List<Usuario> usuarios = new UsuarioModelo().listar();
        RespuestaJson.responder(respuesta, true, usuarios.size() + " usuarios encontrados.",
                usuarios.stream().map(Usuario::aMapaGestion).toList(), 200);
    }

    private void listarSinFuncionario(HttpServletResponse respuesta) throws SQLException, IOException {
        List<Usuario> usuarios = new UsuarioModelo().listarSinFuncionario();
        RespuestaJson.responder(respuesta, true, "Usuarios sin ficha de funcionario.",
                usuarios.stream().map(Usuario::aMapaGestion).toList(), 200);
    }

    @Override
    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        Usuario usuario = buscarUsuarioOFallar(new UsuarioModelo(), Integer.parseInt(llave));
        RespuestaJson.responder(respuesta, true, "Usuario consultado.", usuario.aMapaGestion(), 200);
    }

    @Override
    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        List<String> errores = ValidadorDatos.validarUsuarioGestion(datos, true);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }

        UsuarioModelo usuarioModelo = new UsuarioModelo();
        if (usuarioModelo.existeCorreo((String) datos.get("correo"))) {
            throw new ErrorConflicto("Ya existe un usuario con ese correo.");
        }
        if (usuarioModelo.existeDocumento((String) datos.get("documento"))) {
            throw new ErrorConflicto("Ya existe un usuario con ese documento.");
        }

        Usuario nuevoUsuario = new Usuario((String) datos.get("nombreCompleto"), (String) datos.get("documento"),
                (String) datos.get("correo"), SeguridadServicio.generarHashClave((String) datos.get("clave")),
                (String) datos.get("tipoUsuario"));
        usuarioModelo.insertar(nuevoUsuario);
        RespuestaJson.responder(respuesta, true, "Usuario " + nuevoUsuario.getNombreCompleto() + " creado.", null, 201);
    }

    @Override
    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int idUsuario = Integer.parseInt(llave);
        UsuarioModelo usuarioModelo = new UsuarioModelo();
        Usuario usuarioGuardado = buscarUsuarioOFallar(usuarioModelo, idUsuario);

        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        List<String> errores = ValidadorDatos.validarUsuarioGestion(datos, false);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }

        // Regla de negocio: nadie puede quitarse a sí mismo el acceso (quedaría sin Coordinador).
        boolean esSuPropiaCuenta = idUsuario == obtenerIdUsuarioSesion(peticion);
        boolean quedaActivo = (Boolean) datos.get("estado");
        if (esSuPropiaCuenta && (!quedaActivo || !Objects.equals(datos.get("tipoUsuario"), usuarioGuardado.getTipoUsuario()))) {
            throw new ErrorConflicto("No puede inactivar su propia cuenta ni cambiar su propio tipo de usuario.");
        }
        if (usuarioModelo.existeCorreoDeOtro((String) datos.get("correo"), idUsuario)) {
            throw new ErrorConflicto("Otro usuario ya tiene ese correo.");
        }
        if (usuarioModelo.existeDocumentoDeOtro((String) datos.get("documento"), idUsuario)) {
            throw new ErrorConflicto("Otro usuario ya tiene ese documento.");
        }

        usuarioGuardado.setNombreCompleto((String) datos.get("nombreCompleto"));
        usuarioGuardado.setDocumento((String) datos.get("documento"));
        usuarioGuardado.setCorreo((String) datos.get("correo"));
        usuarioGuardado.setTipoUsuario((String) datos.get("tipoUsuario"));
        usuarioGuardado.setEstado(quedaActivo);
        usuarioModelo.actualizarDatos(usuarioGuardado);
        String nuevaClave = (String) datos.get("clave");
        if (!nuevaClave.isEmpty()) {                                // la clave solo cambia si la escribieron
            usuarioModelo.actualizarClave(idUsuario, SeguridadServicio.generarHashClave(nuevaClave));
        }
        RespuestaJson.responder(respuesta, true, "Usuario actualizado.", null, 200);
    }

    @Override
    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int idUsuario = Integer.parseInt(llave);
        UsuarioModelo usuarioModelo = new UsuarioModelo();
        Usuario usuario = buscarUsuarioOFallar(usuarioModelo, idUsuario);
        if (idUsuario == obtenerIdUsuarioSesion(peticion)) {
            throw new ErrorConflicto("No puede eliminar su propia cuenta mientras la está usando.");
        }
        usuarioModelo.eliminar(idUsuario);                          // puede lanzar ErrorRegistroEnUso (409)
        RespuestaJson.responder(respuesta, true, "Usuario " + usuario.getNombreCompleto() + " eliminado.", null, 200);
    }
}
