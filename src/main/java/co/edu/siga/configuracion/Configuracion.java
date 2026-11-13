package co.edu.siga.configuracion;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
 
/**
 * Configuracion  -  Paquete CONFIGURACION (transversal)
 * Lee "configuracion.properties" una sola vez (patrón Singleton).
 * Si existe una variable de entorno con el mismo nombre, esta tiene prioridad.
 */
public final class Configuracion {
 
    private static final Properties propiedades = new Properties();
 
    static {
        try (InputStream flujoArchivo = Configuracion.class.getClassLoader()
                .getResourceAsStream("configuracion.properties")) {
            if (flujoArchivo == null) {
                throw new IllegalStateException("No se encontró configuracion.properties en src/main/resources");
            }
            propiedades.load(new InputStreamReader(flujoArchivo, StandardCharsets.UTF_8));
        } catch (IOException errorLectura) {
            throw new IllegalStateException("No se pudo leer configuracion.properties", errorLectura);
        }
    }
 
    private Configuracion() { }   // clase de utilidad: no se instancia
 
    public static String obtenerTexto(String nombreClave, String valorPorDefecto) {
        String valorEntorno = System.getenv(nombreClave);
        if (valorEntorno != null && !valorEntorno.isBlank()) {
            return valorEntorno;
        }
        return propiedades.getProperty(nombreClave, valorPorDefecto).trim();
    }
 
    public static int obtenerEntero(String nombreClave, int valorPorDefecto) {
        return Integer.parseInt(obtenerTexto(nombreClave, String.valueOf(valorPorDefecto)));
    }
 
    // --- Accesos con nombre (más legibles para el resto del código) ---
    public static String urlBaseAplicacion() { return obtenerTexto("URL_BASE_APLICACION", "http://localhost:8080/loginJava"); }
    public static String motorBd() { return obtenerTexto("MOTOR_BD", "mysql").toLowerCase(); }
    public static String bdServidor() { return obtenerTexto("BD_SERVIDOR", "localhost"); }
    public static int bdPuerto() { return obtenerEntero("BD_PUERTO", 3306); }
    public static String bdNombre() { return obtenerTexto("BD_NOMBRE", "dbsigalogin"); }
    public static String bdUsuario() { return obtenerTexto("BD_USUARIO", "root"); }
    public static String bdClave() { return obtenerTexto("BD_CLAVE", ""); }
    public static String jwtSecreto() { return obtenerTexto("JWT_SECRETO", ""); }
    public static int jwtHorasExpiracion() { return obtenerEntero("JWT_HORAS_EXPIRACION", 2); }
    public static int jwtDiasRecordar() { return obtenerEntero("JWT_DIAS_RECORDAR", 30); }
    public static String correoModo() { return obtenerTexto("CORREO_MODO", "consola").toLowerCase(); }
    public static String smtpServidor() { return obtenerTexto("SMTP_SERVIDOR", "smtp.gmail.com"); }
    public static int smtpPuerto() { return obtenerEntero("SMTP_PUERTO", 587); }
    public static String smtpUsuario() { return obtenerTexto("SMTP_USUARIO", ""); }
    public static String smtpClave() { return obtenerTexto("SMTP_CLAVE", ""); }
    public static String correoRemitente() { return obtenerTexto("CORREO_REMITENTE", "SIGA <no-responder@siga.edu.co>"); }
    public static int tokenMinutosExpiracion() { return obtenerEntero("TOKEN_MINUTOS_EXPIRACION", 15); }
}
