package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * AlumnoModelo  -  Paquete MODELO (acceso a datos de "alumno")  ·  Taller 2
 */
public class AlumnoModelo extends ConexionBd {

    private static final String COLUMNAS_ALUMNO = "docAlumno, nomAlumno, mailAlumno, direccAlumno, teleAlumno, "
            + "estadoAlumno, sexoAlumno, fechanacAlumno";

    public List<Alumno> listar() throws SQLException {
        return ejecutarConsulta("SELECT " + COLUMNAS_ALUMNO + " FROM alumno ORDER BY nomAlumno")
                .stream().map(Alumno::desdeMapa).toList();
    }

    public Alumno buscarPorDocumento(String docAlumno) throws SQLException {
        List<Map<String, Object>> filas = ejecutarConsulta(
                "SELECT " + COLUMNAS_ALUMNO + " FROM alumno WHERE docAlumno = ?", docAlumno);
        return filas.isEmpty() ? null : Alumno.desdeMapa(filas.get(0));
    }

    public boolean insertar(Alumno alumno) throws SQLException {
        // El driver JDBC convierte el LocalDate de fechanacAlumno al tipo DATE de la base de datos.
        return ejecutarComando(
                "INSERT INTO alumno (" + COLUMNAS_ALUMNO + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                alumno.getDocAlumno(), alumno.getNomAlumno(), alumno.getMailAlumno(), alumno.getDireccAlumno(),
                alumno.getTeleAlumno(), alumno.getEstadoAlumno(), alumno.getSexoAlumno(), alumno.getFechanacAlumno()) == 1;
    }

    public int actualizar(Alumno alumno) throws SQLException {
        return ejecutarComando(
                "UPDATE alumno SET nomAlumno = ?, mailAlumno = ?, direccAlumno = ?, teleAlumno = ?, "
                        + "estadoAlumno = ?, sexoAlumno = ?, fechanacAlumno = ? WHERE docAlumno = ?",
                alumno.getNomAlumno(), alumno.getMailAlumno(), alumno.getDireccAlumno(), alumno.getTeleAlumno(),
                alumno.getEstadoAlumno(), alumno.getSexoAlumno(), alumno.getFechanacAlumno(), alumno.getDocAlumno());
    }

    public boolean eliminar(String docAlumno) throws SQLException {
        // Cuando existan matrículas y calificaciones (próximo taller), sus FK
        // impedirán borrar al alumno y ConexionBd lanzará ErrorRegistroEnUso.
        return ejecutarComando("DELETE FROM alumno WHERE docAlumno = ?", docAlumno) == 1;
    }
}
