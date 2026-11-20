package co.edu.siga.modelo;

import co.edu.siga.configuracion.ConexionBd;
import co.edu.siga.servicio.excepciones.ErrorRegistroEnUso;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * AreaModelo  -  Paquete MODELO (acceso a datos de "area")  ·  Taller 2
 */
public class AreaModelo extends ConexionBd {

    private static final String CONSULTA_AREAS =
            "SELECT a.codArea, a.nomArea, a.estadoArea, COUNT(m.codMateria) AS totalMaterias "
                    + "FROM area a LEFT JOIN materia m ON m.codArea = a.codArea ";
    private static final String AGRUPAR_AREAS = " GROUP BY a.codArea, a.nomArea, a.estadoArea";

    public List<Area> listar() throws SQLException {
        return ejecutarConsulta(CONSULTA_AREAS + AGRUPAR_AREAS + " ORDER BY a.nomArea")
                .stream().map(Area::desdeMapa).toList();
    }

    public Area buscarPorCodigo(int codArea) throws SQLException {
        List<Map<String, Object>> filas = ejecutarConsulta(CONSULTA_AREAS + "WHERE a.codArea = ?" + AGRUPAR_AREAS, codArea);
        return filas.isEmpty() ? null : Area.desdeMapa(filas.get(0));
    }

    /** ¿Ya hay OTRA área con ese nombre? (al editar se excluye la propia; al crear se envía 0). */
    public boolean existeNombre(String nomArea, int codAreaExcluir) throws SQLException {
        return !ejecutarConsulta("SELECT codArea FROM area WHERE nomArea = ? AND codArea <> ?",
                nomArea, codAreaExcluir).isEmpty();
    }

    public boolean insertar(Area area) throws SQLException {
        // codArea es AUTO_INCREMENT / IDENTITY: lo asigna la base de datos.
        return ejecutarComando("INSERT INTO area (nomArea, estadoArea) VALUES (?, ?)",
                area.getNomArea(), area.getEstadoArea()) == 1;
    }

    public int actualizar(Area area) throws SQLException {
        return ejecutarComando("UPDATE area SET nomArea = ?, estadoArea = ? WHERE codArea = ?",
                area.getNomArea(), area.getEstadoArea(), area.getCodArea());
    }

    public boolean eliminar(int codArea) throws SQLException {
        try {
            return ejecutarComando("DELETE FROM area WHERE codArea = ?", codArea) == 1;
        } catch (ErrorRegistroEnUso errorEnUso) {
            throw new ErrorRegistroEnUso("No se puede eliminar el área porque tiene materias. "
                    + "Elimine o cambie esas materias, o marque el área como Inactivo.", errorEnUso);
        }
    }
}
