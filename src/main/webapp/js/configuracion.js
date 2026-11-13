/* =====================================================================
   configuracion.js  -  Capa VISTA (cliente)
   Único lugar donde se define la dirección del backend (API REST).
   Como el HTML lo sirve el mismo backend, basta una ruta RELATIVA.
   Así el mismo frontend funciona con Flask, Java (Tomcat) y Node.js.
   ===================================================================== */
const configuracionApp = Object.freeze({
  urlApi: "api/autenticacion",       // sin "/" inicial: respeta el contexto de Tomcat
  claveAlmacenToken: "sigaToken",    // nombre con el que se guarda el JWT
  claveAlmacenUsuario: "sigaUsuario",
  claveAlmacenTema: "sigaTema",
  tiempoNotificacionMs: 4000
});
