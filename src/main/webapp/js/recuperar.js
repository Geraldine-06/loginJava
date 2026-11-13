/* =====================================================================
   recuperar.js  -  Vista "¿Olvidaste tu contraseña?"
   Envía el correo al backend; éste genera un token y manda el enlace.
   ===================================================================== */
document.addEventListener("DOMContentLoaded", () => {
  const formularioRecuperar = document.getElementById("formularioRecuperar");
  const botonEnviar = document.getElementById("botonEnviar");

  formularioRecuperar.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const correo = document.getElementById("campoCorreo").value.trim().toLowerCase();

    if (!validarCorreo(correo)) {
      marcarCampo("campoCorreo", "Escriba un correo electrónico válido.");
      return;
    }
    marcarCampo("campoCorreo", "");

    bloquearBoton(botonEnviar, true, "Enviando...");
    const respuesta = await enviarPeticion("recuperar", "POST", { correo });
    bloquearBoton(botonEnviar, false);

    // Por seguridad el backend responde lo mismo exista o no el correo.
    mostrarNotificacion(respuesta.mensaje, respuesta.exito ? "exito" : "error");
    if (respuesta.exito) formularioRecuperar.reset();
  });
});
