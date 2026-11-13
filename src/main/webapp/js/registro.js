/* =====================================================================
   registro.js  -  Lógica de la vista "Crear Cuenta"
   Flujo: valida -> POST api/autenticacion/registro -> modal de bienvenida
   ===================================================================== */
document.addEventListener("DOMContentLoaded", () => {
  const obtenerTipoUsuario = activarSelectorTipo("selectorTipo");
  const formularioRegistro = document.getElementById("formularioRegistro");
  const botonRegistrar = document.getElementById("botonRegistrar");
  const campoClave = document.getElementById("campoClave");

  // Medidor de fortaleza en tiempo real
  campoClave.addEventListener("input", () => {
    const fortaleza = evaluarFortalezaClave(campoClave.value);
    const barra = document.getElementById("medidorBarra");
    barra.style.width = `${(fortaleza.puntaje / 5) * 100}%`;
    barra.style.background = fortaleza.color;
    document.getElementById("medidorTexto").textContent = campoClave.value ? `Seguridad: ${fortaleza.texto}` : "";
  });

  formularioRegistro.addEventListener("submit", async (evento) => {
    evento.preventDefault();

    const datosRegistro = {
      tipoUsuario: obtenerTipoUsuario(),
      nombreCompleto: document.getElementById("campoNombre").value.trim(),
      documento: document.getElementById("campoDocumento").value.trim(),
      correo: document.getElementById("campoCorreo").value.trim().toLowerCase(),
      clave: campoClave.value,
      confirmarClave: document.getElementById("campoConfirmar").value
    };

    if (!validarFormulario(datosRegistro)) return;

    bloquearBoton(botonRegistrar, true, "Creando cuenta...");
    const respuesta = await enviarPeticion("registro", "POST", datosRegistro);
    bloquearBoton(botonRegistrar, false);

    if (respuesta.exito) {
      formularioRegistro.reset();
      mostrarModalBienvenida(datosRegistro.nombreCompleto);
    } else {
      mostrarNotificacion(respuesta.mensaje, "error");
    }
  });

  function validarFormulario(datos) {
    let esValido = true;
    if (!datos.tipoUsuario) {
      mostrarNotificacion("Por favor seleccione su tipo de usuario.", "advertencia");
      esValido = false;
    }
    const reglas = [
      ["campoNombre", datos.nombreCompleto.length >= 3 ? "" : "Escriba su nombre completo."],
      ["campoDocumento", /^\d{5,15}$/.test(datos.documento) ? "" : "Solo números (5 a 15 dígitos)."],
      ["campoCorreo", validarCorreo(datos.correo) ? "" : "Correo electrónico no válido."],
      ["campoClave", claveEsSegura(datos.clave) ? "" : "Mínimo 8 caracteres, mayúscula, minúscula y número."],
      ["campoConfirmar", datos.clave === datos.confirmarClave ? "" : "Las contraseñas no coinciden."]
    ];
    reglas.forEach(([idCampo, mensajeError]) => {
      marcarCampo(idCampo, mensajeError);
      if (mensajeError) esValido = false;
    });
    if (!document.getElementById("casillaTerminos").checked) {
      mostrarNotificacion("Debe aceptar los términos de servicio.", "advertencia");
      esValido = false;
    }
    return esValido;
  }

  function mostrarModalBienvenida(nombreCompleto) {
    document.getElementById("nombreBienvenida").textContent = nombreCompleto.split(" ")[0];
    document.getElementById("modalBienvenida").classList.add("visible");
  }
});
