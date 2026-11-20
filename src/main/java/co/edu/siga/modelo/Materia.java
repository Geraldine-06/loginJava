package co.edu.siga.modelo;

import java.util.Map;

/**
 * Materia  -  Paquete MODELO (entidad / POJO)  ·  Taller 2
 * Representa una fila de la tabla "materia". "nomArea" no es columna de
 * materia: llega con un JOIN a la tabla "area" para mostrar el nombre.
 */
public class Materia extends EntidadBase {

    private Integer codMateria;
    private String nomMateria;
    private Integer codArea;
    private String cicloMateria;
    private String estadoMateria;
    private Integer ordenMateria;
    private Integer hSemanalesMateria;
    private String nomArea;

    public Materia() { }

    /** Fila de la BD (o datos ya validados del formulario) -> Materia. */
    public static Materia desdeMapa(Map<String, Object> fila) {
        return crearDesdeMapa(Materia.class, fila);
    }

    public Integer getCodMateria() { return codMateria; }
    public void setCodMateria(Integer codMateria) { this.codMateria = codMateria; }
    public String getNomMateria() { return nomMateria; }
    public void setNomMateria(String nomMateria) { this.nomMateria = nomMateria; }
    public Integer getCodArea() { return codArea; }
    public void setCodArea(Integer codArea) { this.codArea = codArea; }
    public String getCicloMateria() { return cicloMateria; }
    public void setCicloMateria(String cicloMateria) { this.cicloMateria = cicloMateria; }
    public String getEstadoMateria() { return estadoMateria; }
    public void setEstadoMateria(String estadoMateria) { this.estadoMateria = estadoMateria; }
    public Integer getOrdenMateria() { return ordenMateria; }
    public void setOrdenMateria(Integer ordenMateria) { this.ordenMateria = ordenMateria; }
    public Integer getHSemanalesMateria() { return hSemanalesMateria; }
    public void setHSemanalesMateria(Integer hSemanalesMateria) { this.hSemanalesMateria = hSemanalesMateria; }
    public String getNomArea() { return nomArea; }
    public void setNomArea(String nomArea) { this.nomArea = nomArea; }
}
