package co.edu.siga.configuracion;
 
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
 
/**
 * ConexionBd  -  Paquete CONFIGURACION
 * Clase base de acceso a datos (las clases *Modelo la heredan con "extends").
 * Arma la URL JDBC (cadena de conexión) para MySQL o SQL Server.
 * Usa PreparedStatement con parámetros "?" para evitar la inyección SQL.
 */
public class ConexionBd {
 
    protected final String motorBd = Configuracion.motorBd();
 
    /** Construye la cadena de conexión JDBC según el motor configurado. */
    public String construirUrlJdbc() {
        if ("sqlserver".equals(motorBd)) {
            return "jdbc:sqlserver://" + Configuracion.bdServidor() + ":" + Configuracion.bdPuerto()
                    + ";databaseName=" + Configuracion.bdNombre()
                    + ";encrypt=true;trustServerCertificate=true";
        }
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
                    }
                    fila.put(metadatos.getColumnLabel(indice), valor);
                }
                filas.add(fila);
            }
        }
        return filas;
    }
 
    /** Ejecuta INSERT/UPDATE/DELETE y devuelve cuántas filas cambiaron. */
    protected int ejecutarComando(String consultaSql, Object... parametros) throws SQLException {
        try (Connection conexion = obtenerConexion();
             PreparedStatement sentencia = prepararSentencia(conexion, consultaSql, parametros)) {
            return sentencia.executeUpdate();
        }
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
}
