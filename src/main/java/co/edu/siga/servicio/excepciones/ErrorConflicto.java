package co.edu.siga.servicio.excepciones;

/** 409: el dato choca con otro registro (duplicado o en uso). */
public class ErrorConflicto extends ErrorAplicacion {

    public ErrorConflicto(String mensaje) {
        super(mensaje);
    }

    /** "causa" guarda el error original del motor (para el registro / log). */
    public ErrorConflicto(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public int getCodigoEstado() { return 409; }
}
