package co.edu.siga.configuracion;

import co.edu.siga.servicio.excepciones.ErrorAplicacion;
import co.edu.siga.servicio.excepciones.ErrorBaseDatos;
import co.edu.siga.servicio.excepciones.ErrorReferenciaInvalida;
import co.edu.siga.servicio.excepciones.ErrorRegistroDuplicado;
import co.edu.siga.servicio.excepciones.ErrorRegistroEnUso;
import co.edu.siga.servicio.excepciones.ErrorValidacion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * ConexionBd  -  Paquete CONFIGURACION
 * Clase base de acceso a datos (las clases *Modelo la heredan con "extends").
 * Arma la URL JDBC (cadena de conexión) para MySQL o SQL Server.
 * Usa PreparedStatement con parámetros "?" para evitar la inyección SQL.
 *
 * Taller 2 - MANEJO DE EXCEPCIONES: los errores técnicos del motor
 * (SQLException de MySQL / SQL Server) se TRADUCEN a excepciones propias de
 * SIGA (paquete servicio.excepciones). Así el resto del código no depende del
 * motor y el usuario recibe un mensaje claro en español.
 */
public class ConexionBd {

    private static final Logger registro = Logger.getLogger("siga.bd");

    // Códigos de error (getErrorCode) de MySQL/MariaDB y de SQL Server que sabemos explicar
    private static final Set<Integer> CODIGOS_DUPLICADO = Set.of(1062, 2627, 2601);  // llave primaria o UNIQUE repetido
    private static final int CODIGO_MYSQL_EN_USO = 1451;               // borrar un "padre" que tiene "hijos"
    private static final int CODIGO_MYSQL_REFERENCIA = 1452;           // el "hijo" apunta a un padre que no existe
    private static final int CODIGO_SQLSERVER_FK_O_CHECK = 547;        // SQL Server usa el mismo número para FK y CHECK
    private static final Set<Integer> CODIGOS_CHECK = Set.of(3819, 4025);           // CHECK en MySQL 8 / MariaDB
    private static final Set<Integer> CODIGOS_SIN_CONEXION = Set.of(1045, 1049, 18456, 4060);

    protected final String motorBd = Configuracion.motorBd();

    /** Construye la cadena de conexión JDBC según el motor configurado. */
    public String construirUrlJdbc() {
        if ("sqlserver".equals(motorBd)) {
            return "jdbc:sqlserver://" + Configuracion.bdServidor() + ":" + Configuracion.bdPuerto()
                    + ";databaseName=" + Configuracion.bdNombre()
                    + ";encrypt=true;trustServerCertificate=true";
        }
        // MySQL 8 autentica con caching_sha2_password: sin SSL hay que permitir
        // allowPublicKeyRetrieval=true, si no falla con "Public Key Retrieval is not allowed".
        return "jdbc:mysql://" + Configuracion.bdServidor() + ":" + Configuracion.bdPuerto()
                + "/" + Configuracion.bdNombre()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8";
    }

    /** Abre una conexión nueva. Quien la pide debe cerrarla (try-with-resources). */
    public Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("sqlserver".equals(motorBd)
                    ? "com.microsoft.sqlserver.jdbc.SQLServerDriver"
                    : "com.mysql.cj.jdbc.Driver");      // registra el driver dentro de Tomcat
        } catch (ClassNotFoundException errorDriver) {
            throw new SQLException("No se encontró el driver JDBC para " + motorBd, errorDriver);
        }
        return DriverManager.getConnection(construirUrlJdbc(), Configuracion.bdUsuario(), Configuracion.bdClave());
    }

    /** Ejecuta un SELECT y devuelve una lista de filas (mapa columna -> valor). */
    protected List<Map<String, Object>> ejecutarConsulta(String consultaSql, Object... parametros) throws SQLException {
        List<Map<String, Object>> filas = new ArrayList<>();
        try (Connection conexion = obtenerConexion();
             PreparedStatement sentencia = prepararSentencia(conexion, consultaSql, parametros);
             ResultSet resultado = sentencia.executeQuery()) {
            ResultSetMetaData metadatos = resultado.getMetaData();
            while (resultado.next()) {
                Map<String, Object> fila = new LinkedHashMap<>();
                for (int indice = 1; indice <= metadatos.getColumnCount(); indice++) {
                    Object valor = resultado.getObject(indice);
                    if (valor instanceof Timestamp marcaTiempo) {
                        valor = marcaTiempo.toLocalDateTime();   // unifica fechas de ambos motores
                    } else if (valor instanceof java.sql.Date fechaSql) {
                        valor = fechaSql.toLocalDate();          // Taller 2: DATE -> LocalDate (AAAA-MM-DD)
                    }
                    fila.put(metadatos.getColumnLabel(indice), valor);
                }
                filas.add(fila);
            }
        } catch (SQLException errorBd) {
            throw traducirOLanzar(errorBd);
        }
        return filas;
    }

    /**
     * Ejecuta INSERT/UPDATE/DELETE dentro de una TRANSACCIÓN: si todo sale bien
     * confirma (commit); si algo falla la deshace (rollback). Devuelve cuántas filas cambiaron.
     */
    protected int ejecutarComando(String consultaSql, Object... parametros) throws SQLException {
        try (Connection conexion = obtenerConexion()) {
            conexion.setAutoCommit(false);                    // la transacción la confirmamos nosotros
            try (PreparedStatement sentencia = prepararSentencia(conexion, consultaSql, parametros)) {
                int filasAfectadas = sentencia.executeUpdate();
                conexion.commit();
                return filasAfectadas;
            } catch (SQLException errorComando) {
                conexion.rollback();                          // si algo falla se deshace el cambio
                throw errorComando;
            }
        } catch (SQLException errorBd) {
            throw traducirOLanzar(errorBd);
        }   // try-with-resources: la conexión SIEMPRE se cierra, aunque ocurra un error
    }

    private PreparedStatement prepararSentencia(Connection conexion, String consultaSql, Object... parametros)
            throws SQLException {
        PreparedStatement sentencia = conexion.prepareStatement(consultaSql);
        for (int indice = 0; indice < parametros.length; indice++) {
            sentencia.setObject(indice + 1, parametros[indice]);   // los "?" se numeran desde 1
        }
        return sentencia;
    }

    /** MySQL devuelve TINYINT como Boolean/Integer y SQL Server BIT como Boolean. */
    protected static boolean convertirABooleano(Object valor) {
        if (valor instanceof Boolean valorLogico) return valorLogico;
        if (valor instanceof Number valorNumerico) return valorNumerico.intValue() == 1;
        return false;
    }

    // =================================================================
    //  TALLER 2 - Traducción de errores de la base de datos
    // =================================================================

    /** Convierte una SQLException en una excepción de SIGA (o null si no se sabe explicar). */
    public static ErrorAplicacion traducirErrorBd(SQLException errorBd) {
        int codigoError = errorBd.getErrorCode();                       // número propio del motor
        String estadoSql = errorBd.getSQLState() == null ? "" : errorBd.getSQLState();   // código estándar
        String textoError = String.valueOf(errorBd.getMessage());

        if (CODIGOS_DUPLICADO.contains(codigoError)) {
            return new ErrorRegistroDuplicado(
                    "Ya existe un registro con ese dato (código, documento, nombre o correo).", errorBd);
        }
        if (codigoError == CODIGO_MYSQL_EN_USO
                || (codigoError == CODIGO_SQLSERVER_FK_O_CHECK && textoError.contains("DELETE"))) {
            return new ErrorRegistroEnUso("No se puede eliminar: otros registros dependen de este.", errorBd);
        }
        if (codigoError == CODIGO_MYSQL_REFERENCIA
                || (codigoError == CODIGO_SQLSERVER_FK_O_CHECK && textoError.contains("FOREIGN KEY"))) {
            return new ErrorReferenciaInvalida(
                    "El registro relacionado no existe (revise el área o el usuario seleccionado).", errorBd);
        }
        if (CODIGOS_CHECK.contains(codigoError) || codigoError == CODIGO_SQLSERVER_FK_O_CHECK) {
            return new ErrorValidacion(List.of(
                    "Algún valor no cumple las reglas de la base de datos (estado, sexo u horas)."));
        }
        if (textoError.contains("Public Key Retrieval is not allowed")) {
            // MySQL 8 (AMPPS) usa caching_sha2_password: la URL JDBC necesita allowPublicKeyRetrieval=true.
            return new ErrorBaseDatos("MySQL 8 exige allowPublicKeyRetrieval=true y useSSL=false en la URL JDBC "
                    + "(revise construirUrlJdbc en ConexionBd).", errorBd);
        }
        // SQLState "08xxx" = sin conexión (servidor apagado, puerto errado); "28000" = usuario o clave incorrectos
        if (CODIGOS_SIN_CONEXION.contains(codigoError) || estadoSql.startsWith("08") || estadoSql.equals("28000")) {
            return new ErrorBaseDatos("No hay conexión con la base de datos. Revise que MySQL esté encendido "
                    + "y los datos BD_ de configuracion.properties.", errorBd);
        }
        return null;
    }

    /**
     * Si el error se puede explicar, LANZA la excepción de SIGA; si no, lo registra
     * y lo DEVUELVE para que el modelo lo relance tal cual (terminará en un error 500).
     * Uso:  throw traducirOLanzar(errorBd);
     */
    private SQLException traducirOLanzar(SQLException errorBd) {
        ErrorAplicacion errorTraducido = traducirErrorBd(errorBd);
        if (errorTraducido == null) {
            registro.severe("Error de base de datos no controlado: " + errorBd);
            return errorBd;
        }
        registro.warning("Error de base de datos controlado (" + errorTraducido.getClass().getSimpleName()
                + "): " + errorBd.getErrorCode() + " " + errorBd.getMessage());
        throw errorTraducido;
    }
}
