package co.edu.siga.modelo;

import java.util.Map;

/**
 * Area  -  Paquete MODELO (entidad / POJO)  ·  Taller 2
 * Representa una fila de la tabla "area". "totalMaterias" no es columna:
 * lo calcula la consulta (COUNT) para mostrarlo en la tabla del dashboard.
 */
public class Area extends EntidadBase {

    private Integer codArea;
    private String nomArea;
    private String estadoArea;
    private Integer totalMaterias;

    public Area() { }

    /** Fila de la BD (o datos ya validados del formulario) -> Area. */
    public static Area desdeMapa(Map<String, Object> fila) {
        return crearDesdeMapa(Area.class, fila);
    }

    public Integer getCodArea() { return codArea; }
    public void setCodArea(Integer codArea) { this.codArea = codArea; }
    public String getNomArea() { return nomArea; }
    public void setNomArea(String nomArea) { this.nomArea = nomArea; }
    public String getEstadoArea() { return estadoArea; }
    public void setEstadoArea(String estadoArea) { this.estadoArea = estadoArea; }
    public Integer getTotalMaterias() { return totalMaterias; }
    public void setTotalMaterias(Integer totalMaterias) { this.totalMaterias = totalMaterias; }
}
