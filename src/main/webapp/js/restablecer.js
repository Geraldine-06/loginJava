/* =====================================================================
   restablecer.js  -  Vista "Nueva contraseña"
   Lee el token que llegó en el enlace del correo (?token=...) y lo envía
   junto con la nueva contraseña.
   ===================================================================== */
document.addEventListener("DOMContentLoaded", () => {
  const formularioRestablecer = document.getElementById("formularioRestablecer");
  const botonGuardar = document.getElementById("botonGuardar");
  const tokenRecuperacion = new URLSearchParams(window.location.search).get("token");

  if (!tokenRecuperacion) {
    mostrarNotificacion("El enlace no es válido. Solicite uno nuevo.", "error");
    botonGuardar.disabled = true;
    return;
  }

  formularioRestablecer.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    const nuevaClave = document.getElementById("campoClave").value;
    const confirmarClave = document.getElementById("campoConfirmar").value;

    marcarCampo("campoClave", claveEsSegura(nuevaClave) ? "" : "Mínimo 8 caracteres, mayúscula, minúscula y número.");
    marcarCampo("campoConfirmar", nuevaClave === confirmarClave ? "" : "Las contraseñas no coinciden.");
    if (!claveEsSegura(nuevaClave) || nuevaClave !== confirmarClave) return;

    bloquearBoton(botonGuardar, true, "Guardando...");
    const respuesta = await enviarPeticion("restablecer", "POST", { token: tokenRecuperacion, nuevaClave, confirmarClave });
    bloquearBoton(botonGuardar, false);

    if (respuesta.exito) {
      window.location.href = "login.html?mensaje=claveActualizada";
    } else {
      mostrarNotificacion(respuesta.mensaje, "error");
    }
  });
});
