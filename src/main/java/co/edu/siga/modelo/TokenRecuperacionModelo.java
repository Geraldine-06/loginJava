package co.edu.siga.modelo;
 
import co.edu.siga.configuracion.ConexionBd;
 
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
 
/**
 * TokenRecuperacionModelo  -  Paquete MODELO (acceso a la tabla "tokenRecuperacion")
 */
public class TokenRecuperacionModelo extends ConexionBd {
 
    public boolean insertar(int idUsuario, String tokenHash, int minutosVigencia) throws SQLException {
        LocalDateTime fechaActual = LocalDateTime.now();
        return ejecutarComando(
                "INSERT INTO tokenRecuperacion (idUsuario, tokenHash, fechaExpiracion, usado, fechaCreacion) "
                        + "VALUES (?, ?, ?, 0, ?)",
                idUsuario, tokenHash, fechaActual.plusMinutes(minutosVigencia), fechaActual) == 1;
    }
 
    public TokenRecuperacion buscarPorHash(String tokenHash) throws SQLException {
        List<Map<String, Object>> filas = ejecutarConsulta(
                "SELECT idToken, idUsuario, tokenHash, fechaExpiracion, usado FROM tokenRecuperacion WHERE tokenHash = ?",
                tokenHash);
        if (filas.isEmpty()) {
            return null;
        }
        Map<String, Object> fila = filas.get(0);
        TokenRecuperacion tokenRecuperacion = new TokenRecuperacion();
        tokenRecuperacion.setIdToken(((Number) fila.get("idToken")).intValue());
        tokenRecuperacion.setIdUsuario(((Number) fila.get("idUsuario")).intValue());
        tokenRecuperacion.setTokenHash((String) fila.get("tokenHash"));
        tokenRecuperacion.setFechaExpiracion((LocalDateTime) fila.get("fechaExpiracion"));
        tokenRecuperacion.setUsado(convertirABooleano(fila.get("usado")));
        return tokenRecuperacion;
    }
 
    /** Marca el token como usado SOLO si aún no lo estaba. Devuelve false si otra petición ya lo usó. */
    public boolean marcarComoUsado(int idToken) throws SQLException {
        return ejecutarComando("UPDATE tokenRecuperacion SET usado = 1 WHERE idToken = ? AND usado = 0", idToken) == 1;
    }
 
    public void invalidarTokensDeUsuario(int idUsuario) throws SQLException {
        ejecutarComando("UPDATE tokenRecuperacion SET usado = 1 WHERE idUsuario = ? AND usado = 0", idUsuario);
    }
}
