package co.edu.siga.servicio;
 
import at.favre.lib.crypto.bcrypt.BCrypt;
import co.edu.siga.configuracion.Configuracion;
import co.edu.siga.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
 
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
 
/**
 * SeguridadServicio  -  Paquete SERVICIO
 * Todo lo criptográfico en un solo lugar:
 *  - BCrypt para contraseñas (hash lento + sal aleatoria).
 *  - JWT para la sesión (firma HMAC-SHA256 = HS256).
 *  - Token de recuperación aleatorio + su hash SHA-256.
 */
public final class SeguridadServicio {
 
    private static final int COSTO_BCRYPT = 10;
    private static final SecureRandom generadorAleatorio = new SecureRandom();
 
    private SeguridadServicio() { }
 
    // ---------------- Contraseñas (BCrypt) ----------------
    public static String generarHashClave(String clavePlana) {
        return BCrypt.withDefaults().hashToString(COSTO_BCRYPT, clavePlana.toCharArray());
    }
 
    public static boolean verificarClave(String clavePlana, String claveHash) {
        try {
            return BCrypt.verifyer().verify(clavePlana.toCharArray(), claveHash).verified;
        } catch (IllegalArgumentException errorBcrypt) {
            // Clave de más de 71 bytes o hash con formato inválido: se trata como clave incorrecta (401, no 500)
            return false;
        }
    }
 
    // ---------------- JWT (sesión) ----------------
    private static SecretKey obtenerClaveFirma() {
        byte[] bytesSecreto = Configuracion.jwtSecreto().getBytes(StandardCharsets.UTF_8);
        if (bytesSecreto.length < 32) {
            throw new IllegalStateException("JWT_SECRETO debe tener mínimo 32 caracteres (revise configuracion.properties).");
        }
        return Keys.hmacShaKeyFor(bytesSecreto);
    }
 
    public static String generarJwt(Usuario usuario, boolean recordarDispositivo) {
        Instant fechaActual = Instant.now();
        Duration duracion = recordarDispositivo
                ? Duration.ofDays(Configuracion.jwtDiasRecordar())
                : Duration.ofHours(Configuracion.jwtHorasExpiracion());
        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))   // "sub": a quién pertenece
                .issuer("siga")                                    // "iss": quién lo emitió
                .claim("nombre", usuario.getNombreCompleto())
                .claim("tipoUsuario", usuario.getTipoUsuario())
                .issuedAt(Date.from(fechaActual))                  // "iat"
                .expiration(Date.from(fechaActual.plus(duracion))) // "exp"
                .signWith(obtenerClaveFirma())                     // HS256
                .compact();
    }
 
    /** Devuelve la carga útil (Claims) si la firma y la fecha son válidas; si no, null. */
    public static Claims verificarJwt(String tokenSesion) {
        try {
            return Jwts.parser()
                    .verifyWith(obtenerClaveFirma())
                    .requireIssuer("siga")
                    .build()
                    .parseSignedClaims(tokenSesion)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException errorToken) {
            return null;
        }
    }
 
    // ---------------- Token de recuperación ----------------
    public static String generarTokenRecuperacion() {
        byte[] bytesAleatorios = new byte[32];
        generadorAleatorio.nextBytes(bytesAleatorios);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytesAleatorios);
    }
 
    public static String calcularHashToken(String tokenPlano) {
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(resumen.digest(tokenPlano.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException errorAlgoritmo) {
            throw new IllegalStateException("SHA-256 no disponible", errorAlgoritmo);
        }
    }
}
