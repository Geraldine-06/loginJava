package co.edu.siga.filtro;
 
import co.edu.siga.controlador.RespuestaJson;
import co.edu.siga.servicio.SeguridadServicio;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import java.io.IOException;
 
/**
 * FiltroJwt  -  Filtro de seguridad (se ejecuta ANTES del servlet).
 * Protege las rutas privadas exigiendo:  Authorization: Bearer <jwt>
 */
@WebFilter(urlPatterns = {"/api/autenticacion/perfil"})
public class FiltroJwt extends HttpFilter {
 
    @Override
    protected void doFilter(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadenaFiltros)
            throws IOException, ServletException {
        String cabeceraAutorizacion = peticion.getHeader("Authorization");
        if (cabeceraAutorizacion == null || !cabeceraAutorizacion.startsWith("Bearer ")) {
            RespuestaJson.responder(respuesta, false, "Debe iniciar sesión.", null, 401);
            return;
        }
        Claims cargaUtil = SeguridadServicio.verificarJwt(cabeceraAutorizacion.substring(7));
        if (cargaUtil == null) {
            RespuestaJson.responder(respuesta, false, "Sesión inválida o expirada.", null, 401);
            return;
        }
        peticion.setAttribute("cargaUtilJwt", cargaUtil);   // disponible para el controlador
        cadenaFiltros.doFilter(peticion, respuesta);
    }
}
