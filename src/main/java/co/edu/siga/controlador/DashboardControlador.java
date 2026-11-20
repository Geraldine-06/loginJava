package co.edu.siga.controlador;

import co.edu.siga.modelo.DashboardModelo;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DashboardControlador  -  Paquete CONTROLADOR (Servlet)  ·  Taller 2
 *     GET /api/dashboard/resumen  -> conteos y datos de las gráficas de inicio (requiere JWT)
 */
@WebServlet(name = "DashboardControlador", urlPatterns = {"/api/dashboard/*"})
public class DashboardControlador extends ControladorBase {

    @Override
    protected void procesarPeticion(HttpServletRequest peticion, HttpServletResponse respuesta)
            throws SQLException, IOException {
        if (!"/resumen".equals(peticion.getPathInfo())) {
            RespuestaJson.responder(respuesta, false, MENSAJE_NO_ENCONTRADO, null, 404);
        } else if (!"GET".equals(peticion.getMethod())) {
            responderMetodoNoPermitido(respuesta);
        } else {
            obtenerResumen(respuesta);
        }
    }

    private void obtenerResumen(HttpServletResponse respuesta) throws SQLException, IOException {
        DashboardModelo dashboardModelo = new DashboardModelo();
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totales", dashboardModelo.contarRegistros());
        resumen.put("materiasPorArea", dashboardModelo.materiasPorArea());
        resumen.put("usuariosPorTipo", dashboardModelo.usuariosPorTipo());
        resumen.put("alumnosPorSexo", dashboardModelo.alumnosPorSexo());
        RespuestaJson.responder(respuesta, true, "Resumen del dashboard.", resumen, 200);
    }
}
