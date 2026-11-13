/* =====================================================================
   bienvenida.js  -  Vista protegida (requiere JWT válido)
   Pide el perfil al backend enviando "Authorization: Bearer <token>".
   ===================================================================== */
document.addEventListener("DOMContentLoaded", async () => {
  if (!obtenerTokenSesion()) {
    window.location.href = "login.html";
    return;
  }

  const respuesta = await enviarPeticion("perfil", "GET");
  if (!respuesta.exito) {
    mostrarNotificacion("Su sesión expiró. Ingrese nuevamente.", "advertencia");
    setTimeout(cerrarSesion, 1500);
    return;
  }

  const datosUsuario = respuesta.datos;
  document.getElementById("nombreUsuario").textContent = datosUsuario.nombreCompleto.split(" ")[0];
  document.getElementById("datoNombre").textContent = datosUsuario.nombreCompleto;
  document.getElementById("datoCorreo").textContent = datosUsuario.correo;
  document.getElementById("datoTipo").textContent = datosUsuario.tipoUsuario;
  document.getElementById("datoBackend").textContent = datosUsuario.backend;
  document.getElementById("botonCerrarSesion").addEventListener("click", cerrarSesion);
});
