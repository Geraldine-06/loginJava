package co.edu.siga.controlador;

import co.edu.siga.modelo.AreaModelo;
import co.edu.siga.modelo.Materia;
import co.edu.siga.modelo.MateriaModelo;
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
 * MateriaControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2
 * Rutas (prefijo /api/materias): GET "", GET "/{cod}", POST "", PUT "/{cod}", DELETE "/{cod}"
 * Consultar: cualquier perfil con sesión.  Crear/editar/eliminar: solo Coordinador.
 */
@WebServlet(name = "MateriaControlador", urlPatterns = {"/api/materias/*"})
public class MateriaControlador extends ControladorBase {

    @Override
    protected boolean usaLlaveNumerica() {
        return true;
    }

    private Materia buscarMateriaOFallar(MateriaModelo materiaModelo, int codMateria) throws SQLException {
        Materia materia = materiaModelo.buscarPorCodigo(codMateria);
        if (materia == null) {
            throw new ErrorNoEncontrado("No existe la materia con código " + codMateria + ".");
        }
        return materia;
    }

    /** Validación de formato + reglas que necesitan la BD (el área existe, el nombre no se repite). */
    private void validarDatosMateria(Map<String, Object> datos, MateriaModelo materiaModelo, int codMateriaExcluir)
            throws SQLException {
        List<String> errores = ValidadorDatos.validarMateria(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }
        if (new AreaModelo().buscarPorCodigo((Integer) datos.get("codArea")) == null) {
            throw new ErrorValidacion(List.of("El área seleccionada no existe."));
        }
        if (materiaModelo.existeNombre((String) datos.get("nomMateria"), codMateriaExcluir)) {
            throw new ErrorConflicto("Ya existe una materia con ese nombre.");
        }
    }

    @Override
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        List<Materia> materias = new MateriaModelo().listar();
        RespuestaJson.responder(respuesta, true, materias.size() + " materias encontradas.",
                materias.stream().map(Materia::aMapa).toList(), 200);
    }

    @Override
    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        RespuestaJson.responder(respuesta, true, "Materia consultada.",
                buscarMateriaOFallar(new MateriaModelo(), Integer.parseInt(llave)).aMapa(), 200);
    }

    @Override
    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        MateriaModelo materiaModelo = new MateriaModelo();
        validarDatosMateria(datos, materiaModelo, 0);
        materiaModelo.insertar(Materia.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Materia " + datos.get("nomMateria") + " creada.", null, 201);
    }

    @Override
    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int codMateria = Integer.parseInt(llave);
        MateriaModelo materiaModelo = new MateriaModelo();
        buscarMateriaOFallar(materiaModelo, codMateria);

        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        datos.put("codMateria", codMateria);
        validarDatosMateria(datos, materiaModelo, codMateria);
        materiaModelo.actualizar(Materia.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Materia actualizada.", null, 200);
    }

    @Override
    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String llave)
            throws SQLException, IOException {
        int codMateria = Integer.parseInt(llave);
        MateriaModelo materiaModelo = new MateriaModelo();
        Materia materia = buscarMateriaOFallar(materiaModelo, codMateria);
        materiaModelo.eliminar(codMateria);
        RespuestaJson.responder(respuesta, true, "Materia " + materia.getNomMateria() + " eliminada.", null, 200);
    }
}
