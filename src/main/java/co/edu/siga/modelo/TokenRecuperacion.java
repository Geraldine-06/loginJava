package co.edu.siga.modelo;
 
import java.time.LocalDateTime;
 
/**
 * TokenRecuperacion  -  Paquete MODELO (entidad)
 * Representa una fila de la tabla "tokenRecuperacion".
 */
public class TokenRecuperacion {
 
    private Integer idToken;
    private Integer idUsuario;
    private String tokenHash;
    private LocalDateTime fechaExpiracion;
    private boolean usado;
 
    /** Sirve solo si no se ha usado y no ha vencido. */
    public boolean estaVigente(LocalDateTime fechaActual) {
        return !usado && fechaExpiracion != null && fechaExpiracion.isAfter(fechaActual);
    }
 
    public Integer getIdToken() { return idToken; }
    public void setIdToken(Integer idToken) { this.idToken = idToken; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}
