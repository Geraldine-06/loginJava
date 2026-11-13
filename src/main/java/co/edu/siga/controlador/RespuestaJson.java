package co.edu.siga.controlador;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
 
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
 
/**
 * RespuestaJson  -  Paquete CONTROLADOR
 * Lee el JSON que envía la vista y escribe respuestas con la forma única
 * { "exito": true|false, "mensaje": "texto", "datos": {...} | null }
 */
public final class RespuestaJson {
 
    private static final Gson conversorJson = new GsonBuilder().serializeNulls().create();
 
    private RespuestaJson() { }
 
    public static void responder(HttpServletResponse respuesta, boolean exito, String mensaje,
                                 Object datos, int codigoEstado) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("exito", exito);
        cuerpo.put("mensaje", mensaje);
        cuerpo.put("datos", datos);
        respuesta.setStatus(codigoEstado);
        respuesta.setContentType("application/json;charset=UTF-8");
        respuesta.getWriter().write(conversorJson.toJson(cuerpo));
    }
 
    /** Convierte el cuerpo JSON de la petición en un JsonObject (vacío si no es válido). */
    public static JsonObject leerCuerpo(HttpServletRequest peticion) throws IOException {
        try {
            JsonElement elemento = JsonParser.parseReader(peticion.getReader());
            return elemento.isJsonObject() ? elemento.getAsJsonObject() : new JsonObject();
        } catch (RuntimeException errorFormato) {
            return new JsonObject();
        }
    }
 
    /** Lee un texto del JSON; recorta espacios salvo en contraseñas. */
    public static String leerTexto(JsonObject cuerpo, String nombreCampo) {
        if (!cuerpo.has(nombreCampo) || !cuerpo.get(nombreCampo).isJsonPrimitive()) {
            return "";
        }
        String valor = cuerpo.get(nombreCampo).getAsString();
        return nombreCampo.toLowerCase().contains("clave") ? valor : valor.trim();
    }
}
