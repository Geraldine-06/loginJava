package co.edu.siga.servicio.excepciones;

/** 409: la BD rechazó el dato porque viola una llave primaria o un UNIQUE. */
public class ErrorRegistroDuplicado extends ErrorConflicto {

    public ErrorRegistroDuplicado(String mensaje) {
        super(mensaje);
    }

    /** "causa" guarda el error original del motor (para el registro / log). */
    public ErrorRegistroDuplicado(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
