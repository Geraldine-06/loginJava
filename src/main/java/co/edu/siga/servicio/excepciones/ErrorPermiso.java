package co.edu.siga.servicio.excepciones;

/** 403: el perfil del usuario no puede hacer esa acción. */
public class ErrorPermiso extends ErrorAplicacion {

    public ErrorPermiso(String mensaje) {
        super(mensaje);
    }

    @Override
    public int getCodigoEstado() { return 403; }
}
