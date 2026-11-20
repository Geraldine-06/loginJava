package co.edu.siga.servicio.excepciones;

/** 409: la BD rechazó el borrado porque otra tabla depende del registro (FK). */
public class ErrorRegistroEnUso extends ErrorConflicto {

    public ErrorRegistroEnUso(String mensaje) {
        super(mensaje);
    }

    /** "causa" guarda el error original del motor (para el registro / log). */
    public ErrorRegistroEnUso(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
