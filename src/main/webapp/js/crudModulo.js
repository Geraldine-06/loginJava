/* =====================================================================
   crudModulo.js  -  Taller 2 · Capa VISTA (cliente)
   Clase GENÉRICA que hace el CRUD de cualquier módulo descrito en
   modulosDashboard.js:
     C (Create)  ventana modal con formulario  -> POST   api/<recurso>
     R (Read)    tabla + buscador + detalle    -> GET    api/<recurso>
     U (Update)  la misma modal, ya llena      -> PUT    api/<recurso>/<llave>
     D (Delete)  modal de confirmación         -> DELETE api/<recurso>/<llave>
   Seguridad: textContent (nunca innerHTML con datos) para evitar XSS.
   Los botones de crear/editar/eliminar solo se muestran si el perfil
   puede gestionar, pero QUIEN DECIDE es el backend (responde 403).
   ===================================================================== */
class CrudModulo {

  constructor(configuracionModulo, puedeGestionar, alVencerSesion) {
    this.modulo = configuracionModulo;
    this.puedeGestionar = puedeGestionar;
    this.alVencerSesion = alVencerSesion;      // función que dashboard.js ejecuta si llega un 401
    this.registros = [];
    this.textoBusqueda = "";
    this.filtroEstado = "";
    this.columnaOrden = null;
    this.ordenAscendente = true;
    this.paginaActual = 1;
    this.registroEnEdicion = null;             // null = creando, objeto = editando
    this.registroPorEliminar = null;
  }

  /* ---------------- Activar el módulo en la vista compartida ---------------- */
  async activar() {
    document.getElementById("moduloTitulo").textContent = this.modulo.titulo;
    document.getElementById("moduloDescripcion").textContent = this.modulo.descripcion;
    document.getElementById("botonNuevo").hidden = !this.puedeGestionar;
    document.getElementById("botonNuevo").querySelector("span").textContent = `${this.modulo.femenino ? "Nueva" : "Nuevo"} ${this.modulo.singular}`;
    document.getElementById("avisoConsulta").hidden = this.puedeGestionar;

    const campoBuscar = document.getElementById("campoBuscar");
    const filtroEstado = document.getElementById("filtroEstado");
    campoBuscar.value = "";
    filtroEstado.value = "";
    // Se usan propiedades on... (y no addEventListener) para que, al cambiar de
    // módulo, el manejador anterior quede REEMPLAZADO y no se acumulen.
    campoBuscar.oninput = () => { this.textoBusqueda = campoBuscar.value; this.paginaActual = 1; this.pintarTabla(); };
    filtroEstado.onchange = () => { this.filtroEstado = filtroEstado.value; this.paginaActual = 1; this.pintarTabla(); };
    document.getElementById("botonNuevo").onclick = () => this.abrirFormulario(null);
    document.getElementById("formularioModal").onsubmit = (evento) => this.guardar(evento);
    document.getElementById("botonConfirmarEliminar").onclick = () => this.eliminar();

    this.pintarEncabezado();
    await this.cargarRegistros();
  }

  /* ---------------- R: leer los registros ---------------- */
  async cargarRegistros() {
    this.pintarCargando();
    const respuesta = await enviarPeticionModulo(this.modulo.recurso);
    if (!respuesta.exito) {
      this.registros = [];
      this.pintarTabla(respuesta.mensaje);
      this.manejarFallo(respuesta);
      return;
    }
    this.registros = respuesta.datos || [];
    this.pintarTabla();
  }

  manejarFallo(respuesta) {
    if (respuesta.codigoEstado === 401) {          // JWT vencido o inválido
      this.alVencerSesion();
    } else if (respuesta.codigoEstado === 403) {   // perfil sin permiso
      mostrarNotificacion(respuesta.mensaje, "advertencia");
    } else {
      mostrarNotificacion(respuesta.mensaje, "error");
    }
  }

  /* ---------------- Tabla: encabezado, filas y paginación ---------------- */
  pintarEncabezado() {
    const filaEncabezado = document.getElementById("encabezadoTabla");
    filaEncabezado.replaceChildren();
    this.modulo.columnas.forEach((columna) => {
      const celda = document.createElement("th");
      celda.textContent = columna.titulo;
      celda.scope = "col";
      celda.title = "Clic para ordenar";
      celda.addEventListener("click", () => this.ordenarPor(columna.campo, celda));
      filaEncabezado.appendChild(celda);
    });
    const celdaAcciones = document.createElement("th");
    celdaAcciones.className = "columna-acciones";
    celdaAcciones.textContent = "Acciones";
    filaEncabezado.appendChild(celdaAcciones);
  }

  ordenarPor(campo, celdaEncabezado) {
    this.ordenAscendente = this.columnaOrden === campo ? !this.ordenAscendente : true;
    this.columnaOrden = campo;
    document.querySelectorAll("#encabezadoTabla th").forEach((th) => th.removeAttribute("aria-sort"));
    celdaEncabezado.setAttribute("aria-sort", this.ordenAscendente ? "ascending" : "descending");
    this.pintarTabla();
  }

  pintarCargando() {
    const cuerpoTabla = document.getElementById("cuerpoTabla");
    cuerpoTabla.replaceChildren();
    for (let fila = 0; fila < 5; fila++) {
      const filaTabla = cuerpoTabla.insertRow();
      for (let columna = 0; columna <= this.modulo.columnas.length; columna++) {
        const esqueleto = document.createElement("div");
        esqueleto.className = "esqueleto";
        filaTabla.insertCell().appendChild(esqueleto);
      }
    }
  }

  obtenerRegistrosVisibles() {
    const normalizar = (texto) => String(texto ?? "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
    const busqueda = normalizar(this.textoBusqueda.trim());
    let lista = this.registros.filter((registro) => {
      const cumpleEstado = !this.filtroEstado || registro[this.modulo.campoEstado] === this.filtroEstado;
      const cumpleBusqueda = !busqueda || this.modulo.columnas.some((columna) => normalizar(registro[columna.campo]).includes(busqueda));
      return cumpleEstado && cumpleBusqueda;
    });
    if (this.columnaOrden) {
      const campo = this.columnaOrden;
      lista = [...lista].sort((registroA, registroB) => {
        const valorA = registroA[campo] ?? "";
        const valorB = registroB[campo] ?? "";
        const comparacion = typeof valorA === "number" && typeof valorB === "number"
          ? valorA - valorB
          : String(valorA).localeCompare(String(valorB), "es", { numeric: true });
        return this.ordenAscendente ? comparacion : -comparacion;
      });
    }
    return lista;
  }

  pintarTabla(mensajeVacio = "No hay registros que coincidan con la búsqueda.") {
    const cuerpoTabla = document.getElementById("cuerpoTabla");
    cuerpoTabla.replaceChildren();
    const registrosVisibles = this.obtenerRegistrosVisibles();
    const porPagina = configuracionApp.registrosPorPagina;
    const totalPaginas = Math.max(1, Math.ceil(registrosVisibles.length / porPagina));
    this.paginaActual = Math.min(this.paginaActual, totalPaginas);
    const inicio = (this.paginaActual - 1) * porPagina;
    const registrosPagina = registrosVisibles.slice(inicio, inicio + porPagina);

    if (registrosPagina.length === 0) {
      const celdaVacia = cuerpoTabla.insertRow().insertCell();
      celdaVacia.colSpan = this.modulo.columnas.length + 1;
      celdaVacia.className = "tabla-vacia";
      celdaVacia.textContent = mensajeVacio;
    }

    registrosPagina.forEach((registro) => {
      const filaTabla = cuerpoTabla.insertRow();
      this.modulo.columnas.forEach((columna) => {
        const celda = filaTabla.insertCell();
        if (columna.principal) celda.className = "celda-principal";
        celda.appendChild(this.crearContenidoCelda(registro[columna.campo], columna.tipo));
      });
      const celdaAcciones = filaTabla.insertCell();
      celdaAcciones.className = "celda-acciones";
      celdaAcciones.appendChild(this.crearBotonFila("icono-ver", "Ver detalle", () => this.verDetalle(registro)));
      if (this.puedeGestionar) {
        celdaAcciones.appendChild(this.crearBotonFila("icono-editar", "Editar", () => this.abrirFormulario(registro)));
        celdaAcciones.appendChild(this.crearBotonFila("icono-eliminar", "Eliminar", () => this.confirmarEliminacion(registro), "eliminar"));
      }
    });

    document.getElementById("contadorRegistros").textContent =
      `${registrosVisibles.length} de ${this.registros.length} registros`;
    document.getElementById("textoPaginacion").textContent = registrosVisibles.length
      ? `Mostrando ${inicio + 1} - ${inicio + registrosPagina.length} de ${registrosVisibles.length}`
      : "Sin resultados";
    this.pintarPaginacion(totalPaginas);
  }

  crearContenidoCelda(valor, tipo) {
    if (valor === null || valor === undefined || valor === "") {
      const vacio = document.createElement("span");
      vacio.className = "texto-suave";
      vacio.textContent = "—";
      return vacio;
    }
    if (tipo === "estado" || tipo === "insignia") {
      const insignia = document.createElement("span");
      insignia.className = `insignia ${tipo === "insignia" ? "insignia-neutra" : valor === "Activo" ? "insignia-activo" : "insignia-inactivo"}`;
      insignia.textContent = valor;
      return insignia;
    }
    return document.createTextNode(tipo === "fecha" ? formatearFecha(valor) : String(valor));
  }

  crearBotonFila(idIcono, textoAyuda, alHacerClic, claseExtra = "") {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = `boton-fila ${claseExtra}`;
    boton.title = textoAyuda;
    boton.setAttribute("aria-label", textoAyuda);
    boton.appendChild(crearIcono(idIcono));
    boton.addEventListener("click", alHacerClic);
    return boton;
  }

  pintarPaginacion(totalPaginas) {
    const zonaBotones = document.getElementById("botonesPaginacion");
    zonaBotones.replaceChildren();
    const agregarBoton = (texto, pagina, deshabilitado = false, esActual = false) => {
      const boton = document.createElement("button");
      boton.type = "button";
      boton.textContent = texto;
      boton.disabled = deshabilitado;
      if (esActual) boton.className = "actual";
      boton.addEventListener("click", () => { this.paginaActual = pagina; this.pintarTabla(); });
      zonaBotones.appendChild(boton);
    };
    agregarBoton("‹", this.paginaActual - 1, this.paginaActual === 1);
    const primera = Math.max(1, Math.min(this.paginaActual - 2, totalPaginas - 4));
    for (let pagina = primera; pagina <= Math.min(totalPaginas, primera + 4); pagina++) {
      agregarBoton(String(pagina), pagina, false, pagina === this.paginaActual);
    }
    agregarBoton("›", this.paginaActual + 1, this.paginaActual === totalPaginas);
  }

  /* ---------------- Modal DETALLE (consultar un registro) ---------------- */
  verDetalle(registro) {
    document.getElementById("modalDetalleTitulo").textContent =
      `Detalle ${this.modulo.femenino ? "de la" : "del"} ${this.modulo.singular}: ${registro[this.modulo.campoNombre]}`;
    const listaDetalle = document.getElementById("listaDetalle");
    listaDetalle.replaceChildren();
    this.modulo.detalle.forEach(([campo, etiqueta]) => {
      const bloque = document.createElement("div");
      const termino = document.createElement("dt");
      const definicion = document.createElement("dd");
      termino.textContent = etiqueta;
      const valor = registro[campo];
      definicion.textContent = valor === null || valor === undefined || valor === ""
        ? "—" : (/fecha|Ingreso|nac/i.test(campo) ? formatearFecha(valor) : String(valor));
      bloque.append(termino, definicion);
      listaDetalle.appendChild(bloque);
    });
    abrirModal("modalDetalle");
  }

  /* ---------------- Modal FORMULARIO (C y U) ---------------- */
  async abrirFormulario(registro) {
    this.registroEnEdicion = registro;
    const esEdicion = registro !== null;
    document.getElementById("modalFormularioTitulo").textContent =
      esEdicion ? `Editar ${this.modulo.singular}` : `${this.modulo.femenino ? "Nueva" : "Nuevo"} ${this.modulo.singular}`;
    document.getElementById("modalFormularioIcono").setAttribute("href", esEdicion ? "#icono-editar" : "#icono-agregar");
    this.mostrarAlertaFormulario(null);

    const zonaCampos = document.getElementById("camposFormulario");
    zonaCampos.replaceChildren();
    for (const campo of this.modulo.campos) {
      zonaCampos.appendChild(await this.construirCampo(campo, registro));
    }
    abrirModal("modalFormulario");
  }

  async construirCampo(campo, registro) {
    const esEdicion = registro !== null;
    const contenedor = document.createElement("div");
    contenedor.className = `campo-modal${campo.completo ? " completo" : ""}`;
    const idControl = `campo_${campo.nombre}`;

    const etiqueta = document.createElement("label");
    etiqueta.htmlFor = idControl;
    etiqueta.textContent = campo.etiqueta + " ";
    if (campo.requerido || (campo.requeridoAlCrear && !esEdicion)) {
      const asterisco = document.createElement("span");
      asterisco.className = "obligatorio";
      asterisco.textContent = "*";
      etiqueta.appendChild(asterisco);
    }

    let control;
    const esLlaveBloqueada = campo.llave && esEdicion;     // la llave primaria no se cambia al editar
    if (campo.tipo === "select" && !esLlaveBloqueada) {
      control = document.createElement("select");
      await this.llenarOpciones(control, campo);
    } else {
      control = document.createElement("input");
      control.type = esLlaveBloqueada ? "text" : campo.tipo;
      if (campo.tipo === "number") { control.min = campo.minimo; control.max = campo.maximo; }
      if (campo.maximo && campo.tipo !== "number") control.maxLength = campo.maximo;
      if (campo.tipo === "password") control.autocomplete = "new-password";
    }
    control.id = idControl;
    control.name = campo.nombre;
    control.className = "control-panel";
    control.disabled = esLlaveBloqueada;
    if (!esEdicion && campo.valorInicial) control.value = campo.valorInicial;
    if (esEdicion && campo.tipo !== "password") {
      control.value = registro[campo.nombre] ?? "";
    }
    if (campo.autocompletar) {
      control.addEventListener("change", () => this.autocompletarCampos(control, campo));
    }

    const mensajeError = document.createElement("span");
    mensajeError.className = "mensaje-error";
    mensajeError.id = `${idControl}_error`;
    contenedor.append(etiqueta, control);
    if (campo.ayuda) {
      const ayuda = document.createElement("small");
      ayuda.textContent = campo.ayuda;
      contenedor.appendChild(ayuda);
    }
    contenedor.appendChild(mensajeError);
    return contenedor;
  }

  async llenarOpciones(select, campo) {
    const opcionVacia = new Option("Seleccione...", "");
    select.appendChild(opcionVacia);
    if (campo.opciones) {
      campo.opciones.forEach((texto) => select.appendChild(new Option(texto, texto)));
      return;
    }
    // Opciones que vienen de la base de datos (ej: las áreas para una materia)
    const respuesta = await enviarPeticionModulo(campo.opcionesDesde.recurso);
    if (!respuesta.exito) {
      this.mostrarAlertaFormulario([`No se pudieron cargar las opciones de "${campo.etiqueta}": ${respuesta.mensaje}`]);
      return;
    }
    select.datosOpciones = respuesta.datos;               // se guardan para autocompletar
    respuesta.datos.forEach((dato) =>
      select.appendChild(new Option(campo.opcionesDesde.texto(dato), dato[campo.opcionesDesde.valor])));
    if (respuesta.datos.length === 0) opcionVacia.textContent = "No hay opciones disponibles";
  }

  autocompletarCampos(select, campo) {
    const datoElegido = (select.datosOpciones || [])
      .find((dato) => String(dato[campo.opcionesDesde.valor]) === select.value);
    if (!datoElegido) return;
    Object.entries(campo.autocompletar).forEach(([campoDestino, campoOrigen]) => {
      const controlDestino = document.getElementById(`campo_${campoDestino}`);
      if (controlDestino && !controlDestino.value) controlDestino.value = datoElegido[campoOrigen] ?? "";
    });
  }

  /* Validación en el CLIENTE (rápida). El backend vuelve a validar todo. */
  leerYValidarFormulario() {
    const esEdicion = this.registroEnEdicion !== null;
    const datos = {};
    let hayErrores = false;
    this.modulo.campos.forEach((campo) => {
      const control = document.getElementById(`campo_${campo.nombre}`);
      const valor = campo.tipo === "password" ? control.value : control.value.trim();
      let error = "";
      const esObligatorio = campo.requerido || (campo.requeridoAlCrear && !esEdicion);
      if (esObligatorio && !valor) {
        error = "Este campo es obligatorio.";
      } else if (valor) {
        if (campo.tipo === "email" && !validarCorreo(valor)) error = "Correo no válido.";
        else if (campo.patron && !new RegExp(campo.patron).test(valor)) error = campo.ayuda || "Formato no válido.";
        else if (campo.tipo === "number" && (Number(valor) < campo.minimo || Number(valor) > campo.maximo || !Number.isInteger(Number(valor))))
          error = `Debe ser un entero entre ${campo.minimo} y ${campo.maximo}.`;
        else if (campo.minimo && campo.tipo !== "number" && valor.length < campo.minimo) error = `Mínimo ${campo.minimo} caracteres.`;
      }
      control.classList.toggle("invalido", Boolean(error));
      document.getElementById(`campo_${campo.nombre}_error`).textContent = error;
      if (error) hayErrores = true;
      datos[campo.nombre] = valor;
    });
    return hayErrores ? null : datos;
  }

  mostrarAlertaFormulario(errores) {
    const alerta = document.getElementById("alertaFormulario");
    alerta.replaceChildren();
    alerta.classList.toggle("visible", Boolean(errores && errores.length));
    if (!errores || !errores.length) return;
    const titulo = document.createElement("strong");
    titulo.textContent = "No se pudo guardar:";
    const lista = document.createElement("ul");
    errores.forEach((textoError) => {
      const elemento = document.createElement("li");
      elemento.textContent = textoError;
      lista.appendChild(elemento);
    });
    alerta.append(titulo, lista);
  }

  async guardar(evento) {
    evento.preventDefault();
    const datos = this.leerYValidarFormulario();
    if (!datos) {
      this.mostrarAlertaFormulario(["Revise los campos marcados en rojo."]);
      return;
    }
    const esEdicion = this.registroEnEdicion !== null;
    const ruta = esEdicion
      ? `${this.modulo.recurso}/${encodeURIComponent(this.registroEnEdicion[this.modulo.llave])}`
      : this.modulo.recurso;

    const botonGuardar = document.getElementById("botonGuardar");
    bloquearBoton(botonGuardar, true, "Guardando...");
    const respuesta = await enviarPeticionModulo(ruta, esEdicion ? "PUT" : "POST", datos);
    bloquearBoton(botonGuardar, false);

    if (!respuesta.exito) {
      // 400 (validación) y 409 (duplicado) se muestran DENTRO de la modal para corregir.
      if ([400, 409].includes(respuesta.codigoEstado)) {
        this.mostrarAlertaFormulario(respuesta.datos?.errores || [respuesta.mensaje]);
      } else {
        cerrarModal("modalFormulario");
        this.manejarFallo(respuesta);
      }
      return;
    }
    cerrarModal("modalFormulario");
    mostrarNotificacion(respuesta.mensaje, "exito");
    await this.cargarRegistros();
  }

  /* ---------------- Modal CONFIRMAR + D (eliminar) ---------------- */
  confirmarEliminacion(registro) {
    this.registroPorEliminar = registro;
    document.getElementById("modalConfirmarTitulo").textContent = `¿Eliminar ${this.modulo.singular}?`;
    document.getElementById("textoConfirmar").textContent =
      `Se eliminará "${registro[this.modulo.campoNombre]}". Esta acción no se puede deshacer.`;
    abrirModal("modalConfirmar");
  }

  async eliminar() {
    const registro = this.registroPorEliminar;
    if (!registro) return;
    const botonConfirmar = document.getElementById("botonConfirmarEliminar");
    bloquearBoton(botonConfirmar, true, "Eliminando...");
    const respuesta = await enviarPeticionModulo(
      `${this.modulo.recurso}/${encodeURIComponent(registro[this.modulo.llave])}`, "DELETE");
    bloquearBoton(botonConfirmar, false);
    cerrarModal("modalConfirmar");
    this.registroPorEliminar = null;

    if (!respuesta.exito) {
      this.manejarFallo(respuesta);            // ej. 409: "No se puede eliminar el área porque tiene materias"
      return;
    }
    mostrarNotificacion(respuesta.mensaje, "exito");
    await this.cargarRegistros();
  }
}

/* ---------------- Funciones de apoyo ---------------- */
function crearIcono(idIcono) {
  const espacioSvg = "http://www.w3.org/2000/svg";
  const icono = document.createElementNS(espacioSvg, "svg");
  const uso = document.createElementNS(espacioSvg, "use");
  uso.setAttribute("href", `#${idIcono}`);
  icono.appendChild(uso);
  return icono;
}

function formatearFecha(textoFecha) {
  // "2010-03-14" -> "14/03/2010"   |   "2026-11-18 16:38:36" -> "18/11/2026 16:38"
  const [parteFecha, parteHora] = String(textoFecha).replace("T", " ").split(" ");
  const [anio, mes, dia] = parteFecha.split("-");
  if (!dia) return textoFecha;
  return `${dia}/${mes}/${anio}${parteHora ? " " + parteHora.slice(0, 5) : ""}`;
}
