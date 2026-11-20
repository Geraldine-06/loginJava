package co.edu.siga.servicio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ValidadorDatos  -  Paquete SERVICIO
 * Validaciones del servidor (nunca confíe solo en el JavaScript del navegador).
 */
public final class ValidadorDatos {

    // Taller 2: List (y no Set) para conservar el orden en el mensaje "Seleccione un valor válido..."
    private static final List<String> TIPOS_USUARIO_PERMITIDOS =
            List.of("Administrador", "Coordinador", "Directivo", "Secretario", "Docente");
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern PATRON_DOCUMENTO = Pattern.compile("^\\d{5,15}$");
    private static final Pattern PATRON_CLAVE_SEGURA = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    // --- Taller 2 (dashboard) ---
    private static final List<String> ESTADOS_PERMITIDOS = List.of("Activo", "Inactivo");
    private static final List<String> SEXOS_PERMITIDOS = List.of("Femenino", "Masculino", "Otro");
    private static final Pattern PATRON_TELEFONO = Pattern.compile("^[0-9+() -]{7,20}$");
    private static final Pattern PATRON_CICLO = Pattern.compile("^\\d{1,2}(-\\d{1,2})*$");
    private static final String MENSAJE_CLAVE_DEBIL =
            "La contraseña necesita mínimo 8 caracteres, una mayúscula, una minúscula y un número.";

    private ValidadorDatos() { }

    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.length() <= 120 && PATRON_CORREO.matcher(correo).matches();
    }

    public static boolean esDocumentoValido(String documento) {
        return documento != null && PATRON_DOCUMENTO.matcher(documento).matches();
    }

    public static boolean esClaveSegura(String clave) {
        // BCrypt solo usa los primeros 71 bytes: se limita por BYTES (las tildes ocupan 2)
        return clave != null && clave.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 71
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
            errores.add(MENSAJE_CLAVE_DEBIL);
        }
        if (!datos.getOrDefault("clave", "").equals(datos.getOrDefault("confirmarClave", ""))) {
            errores.add("Las contraseñas no coinciden.");
        }
        return errores;
    }

    // =================================================================
    //  TALLER 2 - Validaciones de los módulos del dashboard (CRUD)
    //  Cada método revisa los datos del formulario, los deja LISTOS para
    //  la BD (textos vacíos -> null, números -> Integer, fechas -> LocalDate)
    //  y devuelve la lista de errores (vacía si todo está bien).
    // =================================================================

    /** ¿El valor está en la lista? OJO: List.of(...).contains(null) lanza NullPointerException. */
    public static boolean estaEnLista(Object valor, List<String> opciones) {
        return valor != null && opciones.contains(valor);
    }

    /** Convierte cualquier valor del JSON en texto sin espacios ("" si es null, false o 0). */
    private static String convertirATexto(Object valor) {
        if (valor == null || Boolean.FALSE.equals(valor)
                || (valor instanceof Number numero && numero.doubleValue() == 0)) {
            return "";
        }
        return String.valueOf(valor).trim();
    }

    public static void revisarTexto(Map<String, Object> datos, String campo, String etiqueta, List<String> errores,
                                    int minimo, int maximo, boolean obligatorio) {
        String valor = convertirATexto(datos.get(campo));
        if (valor.isEmpty()) {
            datos.put(campo, null);
            if (obligatorio) {
                errores.add("El campo " + etiqueta + " es obligatorio.");
            }
            return;
        }
        if (valor.length() < minimo || valor.length() > maximo) {
            errores.add("El campo " + etiqueta + " debe tener entre " + minimo + " y " + maximo + " caracteres.");
        }
        datos.put(campo, valor);
    }

    public static void revisarOpcion(Map<String, Object> datos, String campo, String etiqueta,
                                     List<String> opcionesPermitidas, List<String> errores) {
        if (!estaEnLista(datos.get(campo), opcionesPermitidas)) {
            errores.add("Seleccione un valor válido para " + etiqueta + ": " + String.join(", ", opcionesPermitidas) + ".");
        }
    }

    public static void revisarTelefono(Map<String, Object> datos, String campo, String etiqueta, List<String> errores) {
        revisarTexto(datos, campo, etiqueta, errores, 7, 20, false);
        Object telefono = datos.get(campo);
        if (telefono != null && !PATRON_TELEFONO.matcher((String) telefono).matches()) {
            errores.add("El " + etiqueta + " solo admite números, espacios, +, ( ) y guiones.");
        }
    }

    public static void revisarEntero(Map<String, Object> datos, String campo, String etiqueta, List<String> errores,
                                     int minimo, int maximo, boolean obligatorio) {
        Object valor = datos.get(campo);
        if (valor == null || "".equals(valor)) {
            datos.put(campo, null);
            if (obligatorio) {
                errores.add("El campo " + etiqueta + " es obligatorio.");
            }
            return;
        }
        long numero;
        try {
            numero = valor instanceof Number valorNumerico
                    ? valorNumerico.longValue()                    // 12 (número en el JSON)
                    : Long.parseLong(String.valueOf(valor).trim());   // "12" (texto en el JSON)
        } catch (NumberFormatException errorFormato) {            // "abc" no es un entero
            errores.add("El campo " + etiqueta + " debe ser un número entero.");
            return;
        }
        if (numero < minimo || numero > maximo) {
            errores.add("El campo " + etiqueta + " debe estar entre " + minimo + " y " + maximo + ".");
            return;
        }
        datos.put(campo, (int) numero);
    }

    public static void revisarDocumento(Map<String, Object> datos, String campo, List<String> errores) {
        String documento = convertirATexto(datos.get(campo));
        datos.put(campo, documento);
        if (!esDocumentoValido(documento)) {
            errores.add("El documento debe tener solo números (5 a 15 dígitos).");
        }
    }

    public static void revisarCorreo(Map<String, Object> datos, String campo, List<String> errores, boolean obligatorio) {
        String correo = convertirATexto(datos.get(campo)).toLowerCase();
        datos.put(campo, correo.isEmpty() ? null : correo);
        if (correo.isEmpty()) {
            if (obligatorio) {
                errores.add("El correo electrónico es obligatorio.");
            }
        } else if (!esCorreoValido(correo)) {
            errores.add("El correo electrónico no es válido.");
        }
    }

    /** Gestión de Usuarios: la clave es obligatoria al crear y opcional al editar. */
    public static List<String> validarUsuarioGestion(Map<String, Object> datos, boolean esNuevo) {
        List<String> errores = new ArrayList<>();
        revisarTexto(datos, "nombreCompleto", "nombre completo", errores, 3, 120, true);
        revisarDocumento(datos, "documento", errores);
        revisarCorreo(datos, "correo", errores, true);
        revisarOpcion(datos, "tipoUsuario", "tipo de usuario", TIPOS_USUARIO_PERMITIDOS, errores);
        Object estado = datos.get("estado");                    // true, 1, "1", "true" o "Activo" = activo
        boolean estaActivo = Boolean.TRUE.equals(estado)
                || (estado instanceof Number numero && numero.doubleValue() == 1)
                || estaEnLista(estado, List.of("1", "true", "Activo"));
        datos.put("estado", estaActivo);
        String clave = datos.get("clave") == null ? "" : String.valueOf(datos.get("clave"));
        datos.put("clave", clave);
        if (esNuevo || !clave.isEmpty()) {
            if (!esClaveSegura(clave)) {
                errores.add(MENSAJE_CLAVE_DEBIL);
            }
        }
        return errores;
    }

    public static List<String> validarFuncionario(Map<String, Object> datos) {
        List<String> errores = new ArrayList<>();
        revisarDocumento(datos, "docFuncionario", errores);
        revisarTexto(datos, "nomFuncionario", "nombre", errores, 3, 120, true);
        revisarCorreo(datos, "mailFuncionario", errores, true);
        revisarTexto(datos, "profFuncionario", "profesión", errores, 3, 150, false);
        revisarTexto(datos, "direcFuncionario", "dirección", errores, 3, 150, false);
        revisarTelefono(datos, "teleFuncionario", "teléfono", errores);
        revisarTelefono(datos, "telemovilFuncionario", "celular", errores);
        revisarOpcion(datos, "estadoFuncionario", "estado", ESTADOS_PERMITIDOS, errores);
        revisarOpcion(datos, "cargoFuncionario", "cargo", TIPOS_USUARIO_PERMITIDOS, errores);
        revisarTexto(datos, "sedeFuncionario", "sede", errores, 2, 40, false);
        return errores;
    }

    public static List<String> validarAlumno(Map<String, Object> datos) {
        List<String> errores = new ArrayList<>();
        revisarDocumento(datos, "docAlumno", errores);
        revisarTexto(datos, "nomAlumno", "nombre", errores, 3, 120, true);
        revisarCorreo(datos, "mailAlumno", errores, false);
        revisarTexto(datos, "direccAlumno", "dirección", errores, 3, 150, false);
        revisarTelefono(datos, "teleAlumno", "teléfono", errores);
        revisarOpcion(datos, "estadoAlumno", "estado", ESTADOS_PERMITIDOS, errores);
        revisarOpcion(datos, "sexoAlumno", "sexo", SEXOS_PERMITIDOS, errores);
        String textoFecha = convertirATexto(datos.get("fechanacAlumno"));
        datos.put("fechanacAlumno", null);
        if (!textoFecha.isEmpty()) {
            try {
                LocalDate fechaNacimiento = LocalDate.parse(textoFecha);   // formato AAAA-MM-DD
                if (fechaNacimiento.isAfter(LocalDate.now()) || fechaNacimiento.getYear() < 1950) {
                    errores.add("La fecha de nacimiento debe estar entre 1950 y hoy.");
                }
                datos.put("fechanacAlumno", fechaNacimiento);
            } catch (DateTimeParseException errorFecha) {
                errores.add("La fecha de nacimiento no es válida (use AAAA-MM-DD).");
            }
        }
        return errores;
    }

    public static List<String> validarArea(Map<String, Object> datos) {
        List<String> errores = new ArrayList<>();
        revisarTexto(datos, "nomArea", "nombre del área", errores, 3, 120, true);
        revisarOpcion(datos, "estadoArea", "estado", ESTADOS_PERMITIDOS, errores);
        return errores;
    }

    public static List<String> validarMateria(Map<String, Object> datos) {
        List<String> errores = new ArrayList<>();
        revisarTexto(datos, "nomMateria", "nombre de la materia", errores, 3, 120, true);
        revisarEntero(datos, "codArea", "área", errores, 1, 999999, true);
        revisarTexto(datos, "cicloMateria", "ciclos", errores, 1, 20, true);
        Object cicloMateria = datos.get("cicloMateria");
        if (cicloMateria != null && !PATRON_CICLO.matcher((String) cicloMateria).matches()) {
            errores.add("Los ciclos se escriben con números separados por guion, ej: 1-2-3.");
        }
        revisarOpcion(datos, "estadoMateria", "estado", ESTADOS_PERMITIDOS, errores);
        revisarEntero(datos, "ordenMateria", "orden", errores, 1, 99, true);
        revisarEntero(datos, "hSemanalesMateria", "horas semanales", errores, 1, 40, false);
        return errores;
    }
}
