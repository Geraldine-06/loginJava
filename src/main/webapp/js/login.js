/* =====================================================================
   login.js  -  Lógica de la vista "Iniciar Sesión"
   Flujo: valida en el cliente -> POST api/autenticacion/login -> guarda JWT
          -> redirige a bienvenida.html
   ===================================================================== */
document.addEventListener("DOMContentLoaded", () => {
  const obtenerTipoUsuario = activarSelectorTipo("selectorTipo");
  const formularioLogin = document.getElementById("formularioLogin");
  const botonIngresar = document.getElementById("botonIngresar");

  // Si ya hay sesión activa, no tiene sentido volver a pedir credenciales.
  if (obtenerTokenSesion()) {
    window.location.href = "bienvenida.html";
    return;
  }

  // Mensaje que llega por la URL (ej: login.html?mensaje=claveActualizada)
  const parametrosUrl = new URLSearchParams(window.location.search);
  if (parametrosUrl.get("mensaje") === "claveActualizada") {
    mostrarNotificacion("Contraseña actualizada. Ya puede iniciar sesión.", "exito");
  }

  formularioLogin.addEventListener("submit", async (evento) => {
    evento.preventDefault();                       // evita que el navegador recargue la página

    const tipoUsuario = obtenerTipoUsuario();
    const usuario = document.getElementById("campoUsuario").value.trim();
    const clave = document.getElementById("campoClave").value;
    const recordarDispositivo = document.getElementById("casillaRecordar").checked;

    // 1) Validaciones en el cliente (rápidas, pero el backend vuelve a validar)
    if (!tipoUsuario) {
      mostrarNotificacion("Por favor seleccione su tipo de usuario antes de ingresar.", "advertencia");
      return;
    }
    if (!usuario || !clave) {
      mostrarNotificacion("Ingrese su documento o correo y su contraseña.", "advertencia");
      return;
    }

    // 2) Petición al controlador del backend
    bloquearBoton(botonIngresar, true, "Validando...");
    const respuesta = await enviarPeticion("login", "POST", { usuario, clave, tipoUsuario, recordarDispositivo });
    bloquearBoton(botonIngresar, false);

    // 3) Respuesta
    if (respuesta.exito) {
      guardarSesion(respuesta.datos.token, respuesta.datos.usuario, recordarDispositivo);
      mostrarNotificacion(respuesta.mensaje, "exito");
      setTimeout(() => (window.location.href = "bienvenida.html"), 700);
    } else {
      mostrarNotificacion(respuesta.mensaje, "error");
    }
  });
});
