package co.edu.siga.controlador;

import co.edu.siga.modelo.Alumno;
import co.edu.siga.modelo.AlumnoModelo;
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
 * AlumnoControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2
 * Rutas (prefijo /api/alumnos): GET "", GET "/{doc}", POST "", PUT "/{doc}", DELETE "/{doc}"
 * Consultar: cualquier perfil con sesión.  Crear/editar/eliminar: solo Coordinador.
 */
@WebServlet(name = "AlumnoControlador", urlPatterns = {"/api/alumnos/*"})
public class AlumnoControlador extends ControladorBase {

    private Alumno buscarAlumnoOFallar(AlumnoModelo alumnoModelo, String docAlumno) throws SQLException {
        Alumno alumno = alumnoModelo.buscarPorDocumento(docAlumno);
        if (alumno == null) {
            throw new ErrorNoEncontrado("No existe el alumno con documento " + docAlumno + ".");
        }
        return alumno;
    }

    @Override
    protected void listar(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        List<Alumno> alumnos = new AlumnoModelo().listar();
        RespuestaJson.responder(respuesta, true, alumnos.size() + " alumnos encontrados.",
                alumnos.stream().map(Alumno::aMapa).toList(), 200);
    }

    @Override
    protected void consultar(HttpServletRequest peticion, HttpServletResponse respuesta, String docAlumno)
            throws SQLException, IOException {
        RespuestaJson.responder(respuesta, true, "Alumno consultado.",
                buscarAlumnoOFallar(new AlumnoModelo(), docAlumno).aMapa(), 200);
    }

    @Override
    protected void crear(HttpServletRequest peticion, HttpServletResponse respuesta) throws SQLException, IOException {
        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        List<String> errores = ValidadorDatos.validarAlumno(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }

        AlumnoModelo alumnoModelo = new AlumnoModelo();
        if (alumnoModelo.buscarPorDocumento((String) datos.get("docAlumno")) != null) {
            throw new ErrorConflicto("Ya existe un alumno con ese documento.");
        }
        alumnoModelo.insertar(Alumno.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Alumno " + datos.get("nomAlumno") + " creado.", null, 201);
    }

    @Override
    protected void actualizar(HttpServletRequest peticion, HttpServletResponse respuesta, String docAlumno)
            throws SQLException, IOException {
        AlumnoModelo alumnoModelo = new AlumnoModelo();
        buscarAlumnoOFallar(alumnoModelo, docAlumno);

        Map<String, Object> datos = RespuestaJson.leerDatos(peticion);
        datos.put("docAlumno", docAlumno);                        // la llave viene de la URL
        List<String> errores = ValidadorDatos.validarAlumno(datos);
        if (!errores.isEmpty()) {
            throw new ErrorValidacion(errores);
        }
        alumnoModelo.actualizar(Alumno.desdeMapa(datos));
        RespuestaJson.responder(respuesta, true, "Alumno actualizado.", null, 200);
    }

    @Override
    protected void eliminar(HttpServletRequest peticion, HttpServletResponse respuesta, String docAlumno)
            throws SQLException, IOException {
        AlumnoModelo alumnoModelo = new AlumnoModelo();
        Alumno alumno = buscarAlumnoOFallar(alumnoModelo, docAlumno);
        alumnoModelo.eliminar(docAlumno);
        RespuestaJson.responder(respuesta, true, "Alumno " + alumno.getNomAlumno() + " eliminado.", null, 200);
    }
}
