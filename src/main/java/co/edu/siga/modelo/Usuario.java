package co.edu.siga.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Usuario  -  Paquete MODELO (entidad / POJO)
 * Representa una fila de la tabla "usuario". Atributos privados + getters/setters (encapsulamiento).
 */
public class Usuario {

    /** Taller 2: las fechas viajan en el JSON como texto "AAAA-MM-DD HH:MM:SS". */
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer idUsuario;
    private String nombreCompleto;
    private String documento;
    private String correo;
    private String claveHash;
    private String tipoUsuario;
    private boolean estado = true;
    private LocalDateTime fechaCreacion;
    private LocalDateTime ultimoIngreso;

    public Usuario() { }

    public Usuario(String nombreCompleto, String documento, String correo, String claveHash, String tipoUsuario) {
        this.nombreCompleto = nombreCompleto;
        this.documento = documento;
        this.correo = correo;
        this.claveHash = claveHash;
        this.tipoUsuario = tipoUsuario;
    }

    /** Datos que se pueden enviar al navegador (nunca el claveHash). */
    public Map<String, Object> aMapaPublico() {
        Map<String, Object> datosPublicos = new LinkedHashMap<>();
        datosPublicos.put("idUsuario", idUsuario);
        datosPublicos.put("nombreCompleto", nombreCompleto);
        datosPublicos.put("documento", documento);
        datosPublicos.put("correo", correo);
        datosPublicos.put("tipoUsuario", tipoUsuario);
        return datosPublicos;
    }

    /** Taller 2: datos para la tabla de "Gestión de Usuarios" (tampoco incluye el claveHash). */
    public Map<String, Object> aMapaGestion() {
        Map<String, Object> datosGestion = aMapaPublico();
        datosGestion.put("estado", estado ? "Activo" : "Inactivo");
        datosGestion.put("fechaCreacion", fechaCreacion == null ? null : fechaCreacion.format(FORMATO_FECHA_HORA));
        datosGestion.put("ultimoIngreso", ultimoIngreso == null ? null : ultimoIngreso.format(FORMATO_FECHA_HORA));
        return datosGestion;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }
    public boolean isEstado() { return estado; }
    public void setEstado(boolean estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getUltimoIngreso() { return ultimoIngreso; }
    public void setUltimoIngreso(LocalDateTime ultimoIngreso) { this.ultimoIngreso = ultimoIngreso; }
}
