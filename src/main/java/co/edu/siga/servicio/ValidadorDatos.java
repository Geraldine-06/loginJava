package co.edu.siga.servicio;
 
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
 
/**
 * ValidadorDatos  -  Paquete SERVICIO
 * Validaciones del servidor (nunca confíe solo en el JavaScript del navegador).
 */
public final class ValidadorDatos {
 
    private static final Set<String> TIPOS_USUARIO_PERMITIDOS =
            Set.of("Administrador", "Coordinador", "Directivo", "Secretario", "Docente");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{5,15}$");
    private static final Pattern PATRON_CLAVE_SEGURA = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
 
    private ValidadorDatos() { }
 
    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.length() <= 120 && PATRON_CORREO.matcher(correo).matches();
    }
 
    public static boolean esDocumentoValido(String documento) {
        return documento != null && PATRON_DOCUMENTO.matcher(documento).matches();
    }
 
    public static boolean esClaveSegura(String clave) {
        // BCrypt solo usa los primeros 71 bytes: se limita por BYTES (las tildes ocupan 2)
        return clave != null && clave.getBytes(StandardCharsets.UTF_8).length <= 71
                && PATRON_CLAVE_SEGURA.matcher(clave).matches();
    }
 
    public static boolean esTipoUsuarioValido(String tipoUsuario) {
        return tipoUsuario != null && TIPOS_USUARIO_PERMITIDOS.contains(tipoUsuario);
    }
 
    /** Devuelve la lista de errores (vacía si todo está bien). */
    public static List<String> validarRegistro(Map<String, String> datos) {
        List<String> errores = new ArrayList<>();
        if (!esTipoUsuarioValido(datos.get("tipoUsuario"))) {
            errores.add("Seleccione un tipo de usuario válido.");
        }
        String nombreCompleto = datos.getOrDefault("nombreCompleto", "");
        if (nombreCompleto.length() < 3 || nombreCompleto.length() > 120) {
            errores.add("El nombre debe tener entre 3 y 120 caracteres.");
        }
        if (!esDocumentoValido(datos.get("documento"))) {
            errores.add("El documento debe tener solo números (5 a 15 dígitos).");
        }
        if (!esCorreoValido(datos.get("correo"))) {
            errores.add("El correo electrónico no es válido.");
        }
        if (!esClaveSegura(datos.get("clave"))) {
            errores.add("La contraseña necesita mínimo 8 caracteres, una mayúscula, una minúscula y un número.");
        }
        if (!datos.getOrDefault("clave", "").equals(datos.getOrDefault("confirmarClave", ""))) {
            errores.add("Las contraseñas no coinciden.");
        }
        return errores;
    }
}
