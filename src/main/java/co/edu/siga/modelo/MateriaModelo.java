package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * MateriaModelo  -  Paquete MODELO (acceso a datos de "materia")  ·  Taller 2
 */
public class MateriaModelo extends ConexionBd {

    private static final String CONSULTA_MATERIAS =
            "SELECT m.codMateria, m.nomMateria, m.codArea, m.cicloMateria, m.estadoMateria, "
                    + "m.ordenMateria, m.hSemanalesMateria, a.nomArea "
                    + "FROM materia m INNER JOIN area a ON a.codArea = m.codArea ";

    public List<Materia> listar() throws SQLException {
        return ejecutarConsulta(CONSULTA_MATERIAS + "ORDER BY m.ordenMateria, m.nomMateria")
                .stream().map(Materia::desdeMapa).toList();
    }

    public Materia buscarPorCodigo(int codMateria) throws SQLException {
        List<Map<String, Object>> filas = ejecutarConsulta(CONSULTA_MATERIAS + "WHERE m.codMateria = ?", codMateria);
        return filas.isEmpty() ? null : Materia.desdeMapa(filas.get(0));
    }

    public boolean existeNombre(String nomMateria, int codMateriaExcluir) throws SQLException {
        return !ejecutarConsulta("SELECT codMateria FROM materia WHERE nomMateria = ? AND codMateria <> ?",
                nomMateria, codMateriaExcluir).isEmpty();
    }

    public boolean insertar(Materia materia) throws SQLException {
        return ejecutarComando(
                "INSERT INTO materia (nomMateria, codArea, cicloMateria, estadoMateria, ordenMateria, hSemanalesMateria) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                materia.getNomMateria(), materia.getCodArea(), materia.getCicloMateria(), materia.getEstadoMateria(),
                materia.getOrdenMateria(), materia.getHSemanalesMateria()) == 1;
    }

    public int actualizar(Materia materia) throws SQLException {
        return ejecutarComando(
                "UPDATE materia SET nomMateria = ?, codArea = ?, cicloMateria = ?, estadoMateria = ?, "
                        + "ordenMateria = ?, hSemanalesMateria = ? WHERE codMateria = ?",
                materia.getNomMateria(), materia.getCodArea(), materia.getCicloMateria(), materia.getEstadoMateria(),
                materia.getOrdenMateria(), materia.getHSemanalesMateria(), materia.getCodMateria());
    }

    public boolean eliminar(int codMateria) throws SQLException {
        return ejecutarComando("DELETE FROM materia WHERE codMateria = ?", codMateria) == 1;
    }
}
