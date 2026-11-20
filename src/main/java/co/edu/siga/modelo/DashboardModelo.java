package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DashboardModelo  -  Paquete MODELO  ·  Taller 2
 * Consultas de resumen (conteos y agrupaciones) para las tarjetas y
 * gráficas de la página de inicio del dashboard.
 */
public class DashboardModelo extends ConexionBd {

    public Map<String, Integer> contarRegistros() throws SQLException {
        Map<String, Object> fila = ejecutarConsulta(
                "SELECT "
                        + " (SELECT COUNT(*) FROM usuario) AS usuarios,"
                        + " (SELECT COUNT(*) FROM funcionario) AS funcionarios,"
                        + " (SELECT COUNT(*) FROM alumno) AS alumnos,"
                        + " (SELECT COUNT(*) FROM alumno WHERE estadoAlumno = 'Activo') AS alumnosActivos,"
                        + " (SELECT COUNT(*) FROM area) AS areas,"
                        + " (SELECT COUNT(*) FROM materia) AS materias").get(0);
        Map<String, Integer> totales = new LinkedHashMap<>();
        fila.forEach((nombre, valor) -> totales.put(nombre, ((Number) valor).intValue()));   // COUNT llega como Long
        return totales;
    }

    public List<Map<String, Object>> materiasPorArea() throws SQLException {
        return ejecutarConsulta(
                "SELECT a.nomArea AS etiqueta, COUNT(m.codMateria) AS total "
                        + "FROM area a LEFT JOIN materia m ON m.codArea = a.codArea "
                        + "GROUP BY a.nomArea ORDER BY total DESC, a.nomArea");
    }

    public List<Map<String, Object>> usuariosPorTipo() throws SQLException {
        return ejecutarConsulta(
                "SELECT tipoUsuario AS etiqueta, COUNT(*) AS total FROM usuario "
                        + "GROUP BY tipoUsuario ORDER BY total DESC");
    }

    public List<Map<String, Object>> alumnosPorSexo() throws SQLException {
        return ejecutarConsulta(
                "SELECT sexoAlumno AS etiqueta, COUNT(*) AS total FROM alumno "
                        + "GROUP BY sexoAlumno ORDER BY total DESC");
    }
}
