package co.edu.siga.servicio.excepciones;

/** 401: falta iniciar sesión o el JWT venció. */
public class ErrorNoAutorizado extends ErrorAplicacion {

    public ErrorNoAutorizado(String mensaje) {
        super(mensaje);
    }

    @Override
    public int getCodigoEstado() { return 401; }
}
