package co.edu.siga.servicio.excepciones;

import java.util.List;

/** 400: datos del formulario incorrectos. Recibe la LISTA de errores del validador; el mensaje principal es el primero. */
public class ErrorValidacion extends ErrorAplicacion {

    public ErrorValidacion(List<String> errores) {
        super(errores.get(0), errores);
    }

    @Override
    public int getCodigoEstado() { return 400; }
}
