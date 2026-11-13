/* =====================================================================
   utilidades.js  -  Capa VISTA (cliente)
   Funciones reutilizables por todas las páginas.
   Nomenclatura: funciones y variables en camelCase y en español.
   ===================================================================== */

/* ---------- Tema claro / oscuro ---------- */
function aplicarTemaGuardado() {
  const temaGuardado = localStorage.getItem(configuracionApp.claveAlmacenTema) || "oscuro";
  document.documentElement.setAttribute("data-tema", temaGuardado);
  actualizarTextoBotonTema(temaGuardado);
}

function alternarTema() {
  const temaActual = document.documentElement.getAttribute("data-tema") === "claro" ? "claro" : "oscuro";
  const temaNuevo = temaActual === "claro" ? "oscuro" : "claro";
  document.documentElement.setAttribute("data-tema", temaNuevo);
  localStorage.setItem(configuracionApp.claveAlmacenTema, temaNuevo);
  actualizarTextoBotonTema(temaNuevo);
}

function actualizarTextoBotonTema(temaActual) {
  const botonTema = document.getElementById("botonTema");
  if (botonTema) {
    botonTema.querySelector("span").textContent = temaActual === "claro" ? "Modo Oscuro" : "Modo Claro";
  }
}

/* ---------- Notificaciones (toast) ---------- */
function mostrarNotificacion(mensaje, tipoNotificacion = "info") {
  let zonaNotificaciones = document.getElementById("zonaNotificaciones");
  if (!zonaNotificaciones) {
    zonaNotificaciones = document.createElement("div");
    zonaNotificaciones.id = "zonaNotificaciones";
    zonaNotificaciones.className = "zona-notificaciones";
    document.body.appendChild(zonaNotificaciones);
  }
  const notificacion = document.createElement("div");
  notificacion.className = `notificacion notificacion-${tipoNotificacion}`;
  notificacion.setAttribute("role", "alert");
  notificacion.textContent = mensaje;          // textContent evita inyección de HTML (XSS)
  zonaNotificaciones.appendChild(notificacion);

  setTimeout(() => {
    notificacion.classList.add("saliendo");
    setTimeout(() => notificacion.remove(), 300);
  }, configuracionApp.tiempoNotificacionMs);
}

/* ---------- Comunicación con el backend (fetch + JSON) ---------- */
async function enviarPeticion(rutaRecurso, metodoHttp = "GET", cuerpoDatos = null) {
  const cabeceras = { "Content-Type": "application/json" };
  const tokenSesion = obtenerTokenSesion();
  if (tokenSesion) {
    cabeceras["Authorization"] = `Bearer ${tokenSesion}`;
  }
  try {
    const respuesta = await fetch(`${configuracionApp.urlApi}/${rutaRecurso}`, {
      method: metodoHttp,
      headers: cabeceras,
      body: cuerpoDatos ? JSON.stringify(cuerpoDatos) : null
    });
    const datosRespuesta = await respuesta.json().catch(() => ({}));
    return {
      exito: respuesta.ok && datosRespuesta.exito === true,
      codigoEstado: respuesta.status,
      mensaje: datosRespuesta.mensaje || "Respuesta inesperada del servidor.",
      datos: datosRespuesta.datos || null
    };
  } catch (errorRed) {
    return { exito: false, codigoEstado: 0, mensaje: "No hay conexión con el servidor. ¿Está encendido el backend?", datos: null };
  }
}

/* ---------- Sesión (JWT en el navegador) ---------- */
function guardarSesion(tokenSesion, datosUsuario, recordarDispositivo) {
  // Si marca "Recordar dispositivo" se usa localStorage (persiste); si no, sessionStorage (se borra al cerrar).
  const almacen = recordarDispositivo ? localStorage : sessionStorage;
  almacen.setItem(configuracionApp.claveAlmacenToken, tokenSesion);
  almacen.setItem(configuracionApp.claveAlmacenUsuario, JSON.stringify(datosUsuario));
}

function obtenerTokenSesion() {
  return sessionStorage.getItem(configuracionApp.claveAlmacenToken)
      || localStorage.getItem(configuracionApp.claveAlmacenToken);
}

function cerrarSesion() {
  [localStorage, sessionStorage].forEach((almacen) => {
    almacen.removeItem(configuracionApp.claveAlmacenToken);
    almacen.removeItem(configuracionApp.claveAlmacenUsuario);
  });
  window.location.href = "login.html";
}

/* ---------- Validaciones del lado del cliente ---------- */
function validarCorreo(correo) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(correo);
}

function evaluarFortalezaClave(clave) {
  let puntaje = 0;
  if (clave.length >= 8) puntaje++;
  if (/[A-Z]/.test(clave)) puntaje++;
  if (/[a-z]/.test(clave)) puntaje++;
  if (/\d/.test(clave)) puntaje++;
  if (/[^A-Za-z0-9]/.test(clave)) puntaje++;
  const niveles = [
    { texto: "Muy débil", color: "#c8413b" },
    { texto: "Muy débil", color: "#c8413b" },
    { texto: "Débil", color: "#d9700f" },
    { texto: "Aceptable", color: "#d6b10f" },
    { texto: "Fuerte", color: "#1f9d63" },
    { texto: "Muy fuerte", color: "#15b36f" }
  ];
  return { puntaje, ...niveles[puntaje] };
}

function claveEsSegura(clave) {
  // Regla compartida con el backend: mínimo 8, una mayúscula, una minúscula y un número.
  return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/.test(clave);
}

function marcarCampo(idCampo, mensajeError) {
  const campo = document.getElementById(idCampo);
  const mensajeCampo = document.getElementById(`${idCampo}Mensaje`);
  if (campo) campo.classList.toggle("campo-invalido", Boolean(mensajeError));
  if (mensajeCampo) mensajeCampo.textContent = mensajeError || "";
}

/* ---------- Mostrar / ocultar contraseña (icono del ojo) ---------- */
function activarBotonesOjo() {
  document.querySelectorAll("[data-ojo]").forEach((botonOjo) => {
    botonOjo.addEventListener("click", () => {
      const campoClave = document.getElementById(botonOjo.dataset.ojo);
      campoClave.type = campoClave.type === "password" ? "text" : "password";
    });
  });
}

/* ---------- Selector de tipo de usuario (chips) ---------- */
function activarSelectorTipo(idSelector) {
  let tipoSeleccionado = "";
  const selector = document.getElementById(idSelector);
  selector.querySelectorAll(".chip-tipo").forEach((chip) => {
    chip.addEventListener("click", () => {
      selector.querySelectorAll(".chip-tipo").forEach((otroChip) => {
        otroChip.classList.remove("chip-activo");
        otroChip.setAttribute("aria-pressed", "false");
      });
      chip.classList.add("chip-activo");
      chip.setAttribute("aria-pressed", "true");
      tipoSeleccionado = chip.dataset.tipo;
    });
  });
  return () => tipoSeleccionado;   // devuelve una función para consultar la selección
}

function bloquearBoton(boton, estaBloqueado, textoCargando = "Procesando...") {
  if (estaBloqueado) {
    boton.dataset.textoOriginal = boton.innerHTML;
    boton.textContent = textoCargando;
    boton.disabled = true;
  } else {
    boton.innerHTML = boton.dataset.textoOriginal || boton.innerHTML;
    boton.disabled = false;
  }
}

/* ---------- Inicialización común ---------- */
document.addEventListener("DOMContentLoaded", () => {
  aplicarTemaGuardado();
  const botonTema = document.getElementById("botonTema");
  if (botonTema) botonTema.addEventListener("click", alternarTema);
  activarBotonesOjo();
});
