package co.edu.siga.modelo;

import java.util.Map;

/**
 * Funcionario  -  Paquete MODELO (entidad / POJO)  ·  Taller 2
 * Representa una fila de la tabla "funcionario". Su documento es el mismo
 * documento del usuario con el que ingresa al sistema (relación 1 a 1).
 */
public class Funcionario extends EntidadBase {

    private String docFuncionario;
    private String nomFuncionario;
    private String mailFuncionario;
    private String profFuncionario;
    private String direcFuncionario;
    private String teleFuncionario;
    private String telemovilFuncionario;
    private String estadoFuncionario;
    private String cargoFuncionario;
    private String sedeFuncionario;

    public Funcionario() { }

    /** Fila de la BD (o datos ya validados del formulario) -> Funcionario. */
    public static Funcionario desdeMapa(Map<String, Object> fila) {
        return crearDesdeMapa(Funcionario.class, fila);
    }

    public String getDocFuncionario() { return docFuncionario; }
    public void setDocFuncionario(String docFuncionario) { this.docFuncionario = docFuncionario; }
    public String getNomFuncionario() { return nomFuncionario; }
    public void setNomFuncionario(String nomFuncionario) { this.nomFuncionario = nomFuncionario; }
    public String getMailFuncionario() { return mailFuncionario; }
    public void setMailFuncionario(String mailFuncionario) { this.mailFuncionario = mailFuncionario; }
    public String getProfFuncionario() { return profFuncionario; }
    public void setProfFuncionario(String profFuncionario) { this.profFuncionario = profFuncionario; }
    public String getDirecFuncionario() { return direcFuncionario; }
    public void setDirecFuncionario(String direcFuncionario) { this.direcFuncionario = direcFuncionario; }
    public String getTeleFuncionario() { return teleFuncionario; }
    public void setTeleFuncionario(String teleFuncionario) { this.teleFuncionario = teleFuncionario; }
    public String getTelemovilFuncionario() { return telemovilFuncionario; }
    public void setTelemovilFuncionario(String telemovilFuncionario) { this.telemovilFuncionario = telemovilFuncionario; }
    public String getEstadoFuncionario() { return estadoFuncionario; }
    public void setEstadoFuncionario(String estadoFuncionario) { this.estadoFuncionario = estadoFuncionario; }
    public String getCargoFuncionario() { return cargoFuncionario; }
    public void setCargoFuncionario(String cargoFuncionario) { this.cargoFuncionario = cargoFuncionario; }
    public String getSedeFuncionario() { return sedeFuncionario; }
    public void setSedeFuncionario(String sedeFuncionario) { this.sedeFuncionario = sedeFuncionario; }
}
