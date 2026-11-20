package co.edu.siga.modelo;

import java.time.LocalDate;
import java.util.Map;

/**
 * Alumno  -  Paquete MODELO (entidad / POJO)  ·  Taller 2
 * Representa una fila de la tabla "alumno". La fecha de nacimiento es un
 * LocalDate (solo fecha, sin hora) y viaja en el JSON como "AAAA-MM-DD".
 */
public class Alumno extends EntidadBase {

    private String docAlumno;
    private String nomAlumno;
    private String mailAlumno;
    private String direccAlumno;
    private String teleAlumno;
    private String estadoAlumno;
    private String sexoAlumno;
    private LocalDate fechanacAlumno;

    public Alumno() { }

    /** Fila de la BD (o datos ya validados del formulario) -> Alumno. */
    public static Alumno desdeMapa(Map<String, Object> fila) {
        return crearDesdeMapa(Alumno.class, fila);
    }

    public String getDocAlumno() { return docAlumno; }
    public void setDocAlumno(String docAlumno) { this.docAlumno = docAlumno; }
    public String getNomAlumno() { return nomAlumno; }
    public void setNomAlumno(String nomAlumno) { this.nomAlumno = nomAlumno; }
    public String getMailAlumno() { return mailAlumno; }
    public void setMailAlumno(String mailAlumno) { this.mailAlumno = mailAlumno; }
    public String getDireccAlumno() { return direccAlumno; }
    public void setDireccAlumno(String direccAlumno) { this.direccAlumno = direccAlumno; }
    public String getTeleAlumno() { return teleAlumno; }
    public void setTeleAlumno(String teleAlumno) { this.teleAlumno = teleAlumno; }
    public String getEstadoAlumno() { return estadoAlumno; }
    public void setEstadoAlumno(String estadoAlumno) { this.estadoAlumno = estadoAlumno; }
    public String getSexoAlumno() { return sexoAlumno; }
    public void setSexoAlumno(String sexoAlumno) { this.sexoAlumno = sexoAlumno; }
    public LocalDate getFechanacAlumno() { return fechanacAlumno; }
    public void setFechanacAlumno(LocalDate fechanacAlumno) { this.fechanacAlumno = fechanacAlumno; }
}
