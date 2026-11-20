package co.edu.siga.servicio.excepciones;

/** 409: la BD rechazó el dato porque apunta a un registro que no existe (FK). */
public class ErrorReferenciaInvalida extends ErrorConflicto {

    public ErrorReferenciaInvalida(String mensaje) {
        super(mensaje);
    }

    /** "causa" guarda el error original del motor (para el registro / log). */
    public ErrorReferenciaInvalida(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
