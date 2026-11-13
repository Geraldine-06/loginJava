# SIGA Login · Java (Maven + Tomcat 10.1)

Proyecto completo armado a partir de la guía (cap. 7 a 10), con el frontend y los scripts de `frontendComun/` y `db/`.

Requiere JDK 25 LTS para compilar y ejecutar.

## Arranque rápido
1. Copia `configuracion.properties.example` a `src/main/resources/configuracion.properties` y configura la conexión a la base de datos. El archivo local no se sube a Git.
2. Cambia `CAMBIAR_ANTES_DE_EJECUTAR_EL_SCRIPT` en el script SQL elegido por la misma clave que pusiste en `BD_CLAVE`.
3. Ejecuta `db/scriptMysql.sql` (o `db/scriptSqlServer.sql` y cambia `MOTOR_BD=sqlserver`, `BD_PUERTO=1433`).
   **Advertencia:** ambos scripts eliminan y recrean la base de datos `dbsigalogin`; no los ejecutes si necesitas conservar sus datos.
4. `mvn clean package`
5. Copia `target/loginJava.war` a `%CATALINA_HOME%\webapps` y arranca Tomcat (`catalina.bat run`).
6. Abre http://localhost:8080/loginJava/ · usuario local de prueba: `admin@siga.edu.co` / `Admin123*` (tipo Administrador). Cambia o desactiva esta cuenta antes de desplegar en un entorno real.
7. Recuperación: con `CORREO_MODO=consola` el enlace con el token sale en la terminal de Tomcat.

## Ajustes respecto al código de la guía
- `pom.xml`: se agregó `angus-activation` (necesaria para enviar el correo HTML en modo smtp).
- `configuracion.properties.example`: plantilla sin credenciales; reemplaza sus valores de ejemplo antes de ejecutar la aplicación.
- `SeguridadServicio`: mensaje claro si `JWT_SECRETO` tiene menos de 32 caracteres; una clave demasiado larga en el login da 401 y no error 500.
- `ValidadorDatos`: límite de la clave en 71 bytes (lo máximo que admite BCrypt).
- `TokenRecuperacionModelo` + `AutenticacionControlador`: el token se marca como usado con un UPDATE condicional ANTES de cambiar la clave → un solo uso garantizado.
- `RespuestaJson.leerTexto`: si un campo llega como objeto/arreglo responde 400 y no 500.
