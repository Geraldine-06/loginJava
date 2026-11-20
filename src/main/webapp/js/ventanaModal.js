/* =====================================================================
   ventanaModal.js  -  Taller 2 · Capa VISTA (cliente)
   Abre y cierra las VENTANAS MODALES del dashboard (formulario, detalle
   y confirmación). Una modal es un <div class="ventana-modal"> que se
   muestra encima de la página; mientras está abierta la página no se usa.
   Se cierra con: botón ×, botón Cancelar, tecla Escape o clic en el fondo.
   ===================================================================== */
let elementoConFocoAnterior = null;   // para devolver el foco al cerrar (accesibilidad)

function abrirModal(idModal) {
  const ventana = document.getElementById(idModal);
  elementoConFocoAnterior = document.activeElement;
  ventana.classList.add("abierta");
  document.body.style.overflow = "hidden";             // evita que la página de fondo se desplace
  const primerControl = ventana.querySelector("input:not([disabled]), select:not([disabled]), button:not(.modal-cerrar)");
  if (primerControl) setTimeout(() => primerControl.focus(), 50);
}

function cerrarModal(idModal) {
  document.getElementById(idModal).classList.remove("abierta");
  if (!document.querySelector(".ventana-modal.abierta")) {
    document.body.style.overflow = "";
  }
  if (elementoConFocoAnterior) elementoConFocoAnterior.focus();
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll(".ventana-modal").forEach((ventana) => {
    // Botones con el atributo data-cerrar-modal (× y Cancelar)
    ventana.querySelectorAll("[data-cerrar-modal]").forEach((boton) =>
      boton.addEventListener("click", () => cerrarModal(ventana.id)));
    // Clic en el fondo oscuro (fuera de la caja)
    ventana.addEventListener("mousedown", (evento) => {
      if (evento.target === ventana) cerrarModal(ventana.id);
    });
  });
  // Tecla Escape: cierra la modal abierta
  document.addEventListener("keydown", (evento) => {
    const ventanaAbierta = document.querySelector(".ventana-modal.abierta");
    if (evento.key === "Escape" && ventanaAbierta) cerrarModal(ventanaAbierta.id);
  });
});
