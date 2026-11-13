package co.edu.siga.modelo;
 
import co.edu.siga.configuracion.ConexionBd;
 
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
 
    /** Convierte la primera fila del resultado en un objeto Usuario (o null si no hay filas). */
    private Usuario convertirPrimeraFila(List<Map<String, Object>> filas) {
        if (filas.isEmpty()) {
            return null;
        }
        Map<String, Object> fila = filas.get(0);
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
