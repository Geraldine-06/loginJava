package co.edu.siga.servicio.excepciones;

/** 503: no hay conexión con la base de datos (MySQL / SQL Server). */
public class ErrorBaseDatos extends ErrorAplicacion {

    public ErrorBaseDatos(String mensaje) {
        super(mensaje);
    }

    /** "causa" guarda el error original del motor (para el registro / log). */
    public ErrorBaseDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public int getCodigoEstado() { return 503; }
}
