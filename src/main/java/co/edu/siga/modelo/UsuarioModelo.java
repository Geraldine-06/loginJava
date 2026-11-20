package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;
import co.edu.siga.servicio.excepciones.ErrorRegistroEnUso;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * UsuarioModelo  -  Paquete MODELO (acceso a datos de la tabla "usuario")
 * "extends ConexionBd" igual que las clases del Modelo de SIGA heredan de conexion.
 * Todo el SQL de usuarios vive aquí; el controlador no escribe SQL.
 */
public class UsuarioModelo extends ConexionBd {

    private static final String COLUMNAS_USUARIO =
            "idUsuario, nombreCompleto, documento, correo, claveHash, tipoUsuario, estado, fechaCreacion, ultimoIngreso";

    public Usuario buscarPorCorreoODocumento(String valorBusqueda) throws SQLException {
        return convertirPrimeraFila(ejecutarConsulta(
                "SELECT " + COLUMNAS_USUARIO + " FROM usuario WHERE correo = ? OR documento = ?",
                valorBusqueda, valorBusqueda));
    }

    public Usuario buscarPorCorreo(String correo) throws SQLException {
        return convertirPrimeraFila(ejecutarConsulta(
                "SELECT " + COLUMNAS_USUARIO + " FROM usuario WHERE correo = ?", correo));
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        return convertirPrimeraFila(ejecutarConsulta(
                "SELECT " + COLUMNAS_USUARIO + " FROM usuario WHERE idUsuario = ?", idUsuario));
    }

    public boolean existeCorreo(String correo) throws SQLException {
        return !ejecutarConsulta("SELECT idUsuario FROM usuario WHERE correo = ?", correo).isEmpty();
    }

    public boolean existeDocumento(String documento) throws SQLException {
        return !ejecutarConsulta("SELECT idUsuario FROM usuario WHERE documento = ?", documento).isEmpty();
    }

    public boolean insertar(Usuario usuario) throws SQLException {
        return ejecutarComando(
                "INSERT INTO usuario (nombreCompleto, documento, correo, claveHash, tipoUsuario, estado, fechaCreacion) "
                        + "VALUES (?, ?, ?, ?, ?, 1, ?)",
                usuario.getNombreCompleto(), usuario.getDocumento(), usuario.getCorreo(),
                usuario.getClaveHash(), usuario.getTipoUsuario(), LocalDateTime.now()) == 1;
    }

    public boolean actualizarClave(int idUsuario, String nuevaClaveHash) throws SQLException {
        return ejecutarComando("UPDATE usuario SET claveHash = ? WHERE idUsuario = ?", nuevaClaveHash, idUsuario) == 1;
    }

    public void registrarIngreso(int idUsuario) throws SQLException {
        ejecutarComando("UPDATE usuario SET ultimoIngreso = ? WHERE idUsuario = ?", LocalDateTime.now(), idUsuario);
    }

    // =================================================================
    //  TALLER 2 - Gestión de Usuarios (CRUD del dashboard)
    // =================================================================
    public List<Usuario> listar() throws SQLException {
        return ejecutarConsulta("SELECT " + COLUMNAS_USUARIO + " FROM usuario ORDER BY nombreCompleto")
                .stream().map(this::convertirFila).toList();
    }

    /** Usuarios que todavía no tienen ficha de funcionario (para el formulario de Funcionarios). */
    public List<Usuario> listarSinFuncionario() throws SQLException {
        return ejecutarConsulta("SELECT " + COLUMNAS_USUARIO + " FROM usuario u "
                + "WHERE NOT EXISTS (SELECT 1 FROM funcionario f WHERE f.docFuncionario = u.documento) "
                + "ORDER BY nombreCompleto")
                .stream().map(this::convertirFila).toList();
    }

    /** ¿Otro usuario (distinto al que se edita) ya usa ese correo? */
    public boolean existeCorreoDeOtro(String correo, int idUsuario) throws SQLException {
        return !ejecutarConsulta("SELECT idUsuario FROM usuario WHERE correo = ? AND idUsuario <> ?",
                correo, idUsuario).isEmpty();
    }

    public boolean existeDocumentoDeOtro(String documento, int idUsuario) throws SQLException {
        return !ejecutarConsulta("SELECT idUsuario FROM usuario WHERE documento = ? AND idUsuario <> ?",
                documento, idUsuario).isEmpty();
    }

    /** Actualiza todo menos la clave (la clave tiene su propio método: actualizarClave). */
    public int actualizarDatos(Usuario usuario) throws SQLException {
        return ejecutarComando(
                "UPDATE usuario SET nombreCompleto = ?, documento = ?, correo = ?, tipoUsuario = ?, estado = ? "
                        + "WHERE idUsuario = ?",
                usuario.getNombreCompleto(), usuario.getDocumento(), usuario.getCorreo(), usuario.getTipoUsuario(),
                usuario.isEstado() ? 1 : 0, usuario.getIdUsuario());
    }

    public boolean eliminar(int idUsuario) throws SQLException {
        try {
            return ejecutarComando("DELETE FROM usuario WHERE idUsuario = ?", idUsuario) == 1;
        } catch (ErrorRegistroEnUso errorEnUso) {
            // La FK funcionario -> usuario lo impide: damos un mensaje que diga QUÉ hacer.
            throw new ErrorRegistroEnUso("No se puede eliminar: el usuario tiene una ficha de funcionario. "
                    + "Elimine primero el funcionario o marque el usuario como Inactivo.", errorEnUso);
        }
    }

    /** Convierte la primera fila del resultado en un objeto Usuario (o null si no hay filas). */
    private Usuario convertirPrimeraFila(List<Map<String, Object>> filas) {
        return filas.isEmpty() ? null : convertirFila(filas.get(0));
    }

    /** Convierte una fila (mapa columna -> valor) en un objeto Usuario. */
    private Usuario convertirFila(Map<String, Object> fila) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(((Number) fila.get("idUsuario")).intValue());
        usuario.setNombreCompleto((String) fila.get("nombreCompleto"));
        usuario.setDocumento((String) fila.get("documento"));
        usuario.setCorreo((String) fila.get("correo"));
        usuario.setClaveHash((String) fila.get("claveHash"));
        usuario.setTipoUsuario((String) fila.get("tipoUsuario"));
        usuario.setEstado(convertirABooleano(fila.get("estado")));
        usuario.setFechaCreacion((LocalDateTime) fila.get("fechaCreacion"));
        usuario.setUltimoIngreso((LocalDateTime) fila.get("ultimoIngreso"));
        return usuario;
    }
}
