package co.edu.siga.controlador;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
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

    /**
     * Taller 2: lee TODO el JSON como un mapa campo -> valor (lo usan los módulos del dashboard).
     * Textos recortados (menos las claves), números como Long o Double, true/false como Boolean.
     */
    public static Map<String, Object> leerDatos(HttpServletRequest peticion) throws IOException {
        Map<String, Object> datos = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> campo : leerCuerpo(peticion).entrySet()) {
            datos.put(campo.getKey(), convertirValorJson(campo.getKey(), campo.getValue()));
        }
        return datos;
    }

    private static Object convertirValorJson(String nombreCampo, JsonElement valor) {
        if (valor == null || valor.isJsonNull()) {
            return null;
        }
        if (!valor.isJsonPrimitive()) {
            return valor.toString();                       // un objeto o una lista se trata como texto
        }
        JsonPrimitive primitivo = valor.getAsJsonPrimitive();
        if (primitivo.isBoolean()) {
            return primitivo.getAsBoolean();
        }
        if (primitivo.isNumber()) {
            BigDecimal numero = primitivo.getAsBigDecimal();
            return numero.stripTrailingZeros().scale() <= 0 ? (Object) numero.longValue() : numero.doubleValue();
        }
        String texto = primitivo.getAsString();
        return nombreCampo.toLowerCase().contains("clave") ? texto : texto.trim();
    }
}
