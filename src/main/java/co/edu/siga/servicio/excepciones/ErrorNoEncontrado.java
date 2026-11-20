package co.edu.siga.servicio.excepciones;

/** 404: el registro (o la ruta) no existe. */
public class ErrorNoEncontrado extends ErrorAplicacion {

    public ErrorNoEncontrado(String mensaje) {
        super(mensaje);
    }

    @Override
    public int getCodigoEstado() { return 404; }
}
