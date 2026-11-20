package co.edu.siga.controlador;

import co.edu.siga.modelo.Area;
import co.edu.siga.modelo.AreaModelo;
import co.edu.siga.servicio.ValidadorDatos;
import co.edu.siga.servicio.excepciones.ErrorConflicto;
import co.edu.siga.servicio.excepciones.ErrorNoEncontrado;
import co.edu.siga.servicio.excepciones.ErrorValidacion;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * AreaControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2
 * Rutas (prefijo /api/areas): GET "", GET "/{cod}", POST "", PUT "/{cod}", DELETE "/{cod}"
 * Consultar: cualquier perfil con sesión.  Crear/editar/eliminar: solo Coordinador.
 */
@WebServlet(name = "AreaControlador", urlPatterns = {"/api/areas/*"})
public class AreaControlador extends ControladorBase {

    @Override
    protected boolean usaLlaveNumerica() {
        return true;
    }

    private Area buscarAreaOFallar(AreaModelo areaModelo, int codArea) throws SQLException {
        Area area = areaModelo.buscarPorCodigo(codArea);
        if (area == null) {
            throw new ErrorNoEncontrado("No existe el área con código " + codArea + ".");
        }
        return area;
    }

    @Override
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        List<Area> areas = new AreaModelo().listar();
        RespuestaJson.responder(respuesta, true, areas.size() + " áreas encontradas.",
                areas.stream().map(Area::aMapa).toList(), 200);
    }

    @Override
    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        RespuestaJson.responder(respuesta, true, "Área consultada.",
                buscarAreaOFallar(new AreaModelo(), Integer.parseInt(llave)).aMapa(), 200);
    }

    @Override
    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        List<String> errores = ValidadorDatos.validarArea(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }

        AreaModelo areaModelo = new AreaModelo();
        if (areaModelo.existeNombre((String) datos.get("nomArea"), 0)) {
            throw new ErrorConflicto("Ya existe un área con ese nombre.");
        }
        areaModelo.insertar(Area.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Área " + datos.get("nomArea") + " creada.", null, 201);
    }

    @Override
    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int codArea = Integer.parseInt(llave);
        AreaModelo areaModelo = new AreaModelo();
        buscarAreaOFallar(areaModelo, codArea);

        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        datos.put("codArea", codArea);
        List<String> errores = ValidadorDatos.validarArea(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }
        if (areaModelo.existeNombre((String) datos.get("nomArea"), codArea)) {
            throw new ErrorConflicto("Ya existe otra área con ese nombre.");
        }
        areaModelo.actualizar(Area.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Área actualizada.", null, 200);
    }

    @Override
    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int codArea = Integer.parseInt(llave);
        AreaModelo areaModelo = new AreaModelo();
        Area area = buscarAreaOFallar(areaModelo, codArea);
        areaModelo.eliminar(codArea);                      // si tiene materias lanza ErrorRegistroEnUso (409)
        RespuestaJson.responder(respuesta, true, "Área " + area.getNomArea() + " eliminada.", null, 200);
    }
}
