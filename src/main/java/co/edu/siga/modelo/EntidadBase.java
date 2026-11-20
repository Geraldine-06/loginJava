package co.edu.siga.modelo;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * EntidadBase  -  Paquete MODELO  ·  Taller 2
 * Comportamiento común de las entidades del dashboard (Funcionario, Alumno,
 * Area, Materia). Como los atributos se llaman IGUAL que las columnas de la
 * tabla, un solo método sirve para convertir cualquier fila en objeto.
 *
 * Usa REFLEXIÓN (java.lang.reflect): el programa revisa sus propios atributos
 * en tiempo de ejecución, así no hay que escribir un "set" por cada columna.
 */
public abstract class EntidadBase {

    /** Mapa (fila de la BD o datos del formulario) -> objeto. Ignora claves sobrantes. */
    protected static <T extends EntidadBase> T crearDesdeMapa(Class<T> claseEntidad, Map<String, Object> fila) {
        if (fila == null) {
            return null;
        }
        try {
            T entidad = claseEntidad.getDeclaredConstructor().newInstance();
            for (Field atributo : claseEntidad.getDeclaredFields()) {
                if (Modifier.isStatic(atributo.getModifiers())) {
                    continue;
                }
                atributo.setAccessible(true);                   // permite escribir el atributo privado
                atributo.set(entidad, convertirValor(fila.get(atributo.getName()), atributo.getType()));
            }
            return entidad;
        } catch (ReflectiveOperationException errorReflexion) {
            throw new IllegalStateException("No se pudo crear " + claseEntidad.getSimpleName(), errorReflexion);
        }
    }

    /** Ajusta el tipo: COUNT(*) llega como Long y TINYINT como Integer o Short; el atributo es Integer. */
    private static Object convertirValor(Object valor, Class<?> tipoAtributo) {
        if (valor == null || tipoAtributo.isInstance(valor)) {
            return valor;   // si la clave no vino, queda null (por eso los atributos usan Integer y no int)
        }
        if (valor instanceof Number numero && tipoAtributo == Integer.class) {
            return numero.intValue();
        }
        if (tipoAtributo == String.class) {
            return String.valueOf(valor);
        }
        return null;        // un dato sobrante con un tipo que no corresponde se descarta
    }

    /** Objeto -> mapa listo para JSON (las fechas viajan como texto AAAA-MM-DD). */
    public Map<String, Object> aMapa() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        try {
            for (Field atributo : getClass().getDeclaredFields()) {
                if (Modifier.isStatic(atributo.getModifiers())) {
                    continue;
                }
                atributo.setAccessible(true);
                Object valor = atributo.get(this);
                mapa.put(atributo.getName(), valor instanceof LocalDate fecha ? fecha.toString() : valor);
            }
        } catch (IllegalAccessException errorReflexion) {
            throw new IllegalStateException("No se pudo leer " + getClass().getSimpleName(), errorReflexion);
        }
        return mapa;
    }
}
