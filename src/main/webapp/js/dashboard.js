/* =====================================================================
   dashboard.js  -  Taller 2 · Capa VISTA (cliente)
   Página protegida: 1) verifica el JWT pidiendo el perfil, 2) pinta el
   usuario y su perfil, 3) muestra el INICIO (tarjetas y gráficas) y
   4) cambia de módulo con el menú lateral usando la clase CrudModulo.
   ===================================================================== */
let perfilActual = null;     // datos del usuario que inició sesión (incluye puedeGestionar)
let moduloActual = null;     // instancia de CrudModulo del módulo abierto

document.addEventListener("DOMContentLoaded", async () => {
  if (!obtenerTokenSesion()) {
    window.location.href = "login.html";
    return;
  }

  const respuesta = await enviarPeticion("perfil", "GET");
  if (!respuesta.exito) {
    terminarSesionVencida();
    return;
  }
  perfilActual = respuesta.datos;
  pintarUsuario();
  activarMenu();
  document.getElementById("botonCerrarSesion").addEventListener("click", cerrarSesion);
  document.getElementById("botonMenu").addEventListener("click", () =>
    document.getElementById("barraLateral").classList.toggle("abierta"));

  // Permite abrir un módulo directo con la URL: dashboard.html#materias
  const vistaInicial = window.location.hash.replace("#", "");
  await mostrarVista(modulosDashboard[vistaInicial] ? vistaInicial : "inicio");
});

function terminarSesionVencida() {
  mostrarNotificacion("Su sesión expiró. Ingrese nuevamente.", "advertencia");
  setTimeout(cerrarSesion, 1500);
}

function pintarUsuario() {
  const { nombreCompleto, tipoUsuario, puedeGestionar, backend } = perfilActual;
  const palabras = nombreCompleto.trim().split(/\s+/);
  document.getElementById("avatarUsuario").textContent = (palabras[0][0] + (palabras[1]?.[0] || "")).toUpperCase();
  document.getElementById("nombreUsuario").textContent = nombreCompleto;
  document.getElementById("tipoUsuario").textContent = tipoUsuario;
  document.getElementById("saludoNombre").textContent = palabras[0];
  document.getElementById("pieBackend").textContent = `Backend: ${backend}`;
  document.getElementById("piePermiso").textContent = puedeGestionar
    ? "Permiso: crear, editar y eliminar" : "Permiso: solo consulta";
  document.getElementById("insigniaPermiso").textContent = puedeGestionar
    ? `✔ ${tipoUsuario}: gestión completa (CRUD)` : `🔒 ${tipoUsuario}: modo consulta`;
  document.getElementById("textoFecha").textContent = new Date().toLocaleDateString("es-CO",
    { weekday: "long", day: "numeric", month: "long", year: "numeric" });
}

function activarMenu() {
  document.querySelectorAll("[data-vista]").forEach((enlace) => {
    enlace.addEventListener("click", () => mostrarVista(enlace.dataset.vista));
  });
}

async function mostrarVista(nombreVista) {
  document.querySelectorAll(".lateral-enlace").forEach((enlace) =>
    enlace.classList.toggle("activo", enlace.dataset.vista === nombreVista));
  document.getElementById("barraLateral").classList.remove("abierta");
  history.replaceState(null, "", nombreVista === "inicio" ? "dashboard.html" : `#${nombreVista}`);

  const esInicio = nombreVista === "inicio";
  document.getElementById("vistaInicio").hidden = !esInicio;
  document.getElementById("vistaModulo").hidden = esInicio;

  if (esInicio) {
    document.getElementById("tituloVista").textContent = "Inicio";
    moduloActual = null;
    await cargarResumen();
    return;
  }
  const configuracionModulo = modulosDashboard[nombreVista];
  document.getElementById("tituloVista").textContent = configuracionModulo.titulo;
  moduloActual = new CrudModulo(configuracionModulo, perfilActual.puedeGestionar, terminarSesionVencida);
  await moduloActual.activar();
}

/* ---------------- INICIO: tarjetas de indicadores y gráficas ---------------- */
async function cargarResumen() {
  const respuesta = await enviarPeticionModulo("dashboard/resumen");
  if (!respuesta.exito) {
    if (respuesta.codigoEstado === 401) terminarSesionVencida();
    else mostrarNotificacion(respuesta.mensaje, "error");
    return;
  }
  const { totales, materiasPorArea, usuariosPorTipo, alumnosPorSexo } = respuesta.datos;
  const indicadores = [
    { vista: "usuarios", nombre: "Usuarios", valor: totales.usuarios, detalle: "Cuentas de acceso" },
    { vista: "funcionarios", nombre: "Funcionarios", valor: totales.funcionarios, detalle: "Personal registrado" },
    { vista: "alumnos", nombre: "Alumnos", valor: totales.alumnos, detalle: `${totales.alumnosActivos} activos` },
    { vista: "areas", nombre: "Áreas", valor: totales.areas, detalle: "Del plan de estudios" },
    { vista: "materias", nombre: "Materias", valor: totales.materias, detalle: "Asignaturas" }
  ];
  const rejilla = document.getElementById("rejillaIndicadores");
  rejilla.replaceChildren();
  indicadores.forEach((indicador) => {
    const tarjeta = document.createElement("button");
    tarjeta.type = "button";
    tarjeta.className = "indicador";
    const icono = document.createElement("span");
    icono.className = "indicador-icono";
    icono.appendChild(crearIcono(modulosDashboard[indicador.vista].icono));
    const valor = document.createElement("span");
    valor.className = "indicador-valor";
    valor.textContent = indicador.valor.toLocaleString("es-CO");
    const nombre = document.createElement("span");
    nombre.className = "indicador-nombre";
    nombre.textContent = indicador.nombre;
    const detalle = document.createElement("span");
    detalle.className = "indicador-detalle";
    detalle.textContent = indicador.detalle;
    tarjeta.append(icono, valor, nombre, detalle);
    tarjeta.addEventListener("click", () => mostrarVista(indicador.vista));
    rejilla.appendChild(tarjeta);
  });

  pintarGraficaBarras("graficaMaterias", materiasPorArea, "materias");
  pintarGraficaBarras("graficaUsuarios", usuariosPorTipo, "usuarios");
  pintarGraficaBarras("graficaAlumnos", alumnosPorSexo, "alumnos");
}

function pintarGraficaBarras(idLista, datos, unidad) {
  // Barras horizontales de UNA serie: un solo color, etiqueta y valor en texto.
  const lista = document.getElementById(idLista);
  lista.replaceChildren();
  if (!datos.length) {
    const vacio = document.createElement("li");
    vacio.className = "texto-suave";
    vacio.textContent = "Sin datos todavía.";
    lista.appendChild(vacio);
    return;
  }
  const maximo = Math.max(...datos.map((dato) => Number(dato.total)), 1);
  datos.forEach((dato) => {
    const fila = document.createElement("li");
    fila.className = "barra-fila";
    fila.title = `${dato.etiqueta}: ${dato.total} ${unidad}`;     // tooltip al pasar el mouse
    const etiqueta = document.createElement("span");
    etiqueta.className = "barra-etiqueta";
    etiqueta.textContent = dato.etiqueta;
    const valor = document.createElement("span");
    valor.className = "barra-valor";
    valor.textContent = dato.total;
    const pista = document.createElement("div");
    pista.className = "barra-pista";
    const relleno = document.createElement("div");
    relleno.className = "barra-relleno";
    relleno.style.width = "0%";
    pista.appendChild(relleno);
    fila.append(etiqueta, valor, pista);
    lista.appendChild(fila);
    requestAnimationFrame(() => (relleno.style.width = `${(Number(dato.total) / maximo) * 100}%`));
  });
}
