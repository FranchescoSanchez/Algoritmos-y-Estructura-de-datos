/* ==========================================================
   Mi Portafolio UPLA - portafolio público
   Pinta: curso -> 4 unidades -> 4 semanas -> tareas
   ========================================================== */

let cursoActivo = null;

document.addEventListener("DOMContentLoaded", () => {
    fetch("visitar?pagina=index").catch(() => {});

    const botones = document.querySelectorAll(".curso-btn");
    botones.forEach((boton) => {
        boton.addEventListener("click", () => {
            botones.forEach((b) => b.classList.remove("activo"));
            boton.classList.add("activo");
            cargarCurso(boton.dataset.curso);
        });
    });

    if (botones.length > 0) {
        botones[0].classList.add("activo");
        cargarCurso(botones[0].dataset.curso);
    }
});

function cargarCurso(curso) {
    cursoActivo = curso;
    const destino = document.getElementById("unidades");
    if (!destino) return;

    destino.innerHTML = '<div class="vacio">Cargando el curso...</div>';

    fetch("archivos?curso=" + encodeURIComponent(curso))
        .then((res) => res.json())
        .then((datos) => {
            if (datos.error) {
                destino.innerHTML =
                    '<div class="vacio"><strong>No se pudo cargar el curso</strong>' +
                    '<br>' + escapar(datos.error) +
                    '<br><a href="estado">Revisar el estado del sistema</a></div>';
                return;
            }

            actualizarAvance(datos);
            destino.innerHTML = datos.unidades.map(dibujarUnidad).join("");
            activarAcordeon();
        })
        .catch(() => {
            destino.innerHTML =
                '<div class="vacio"><strong>No se pudo conectar con el servidor</strong>' +
                'Vuelve a intentarlo en unos segundos.</div>';
        });
}

function actualizarAvance(datos) {
    const avance = document.getElementById("avance");
    if (avance) {
        avance.textContent = datos.semanasEntregadas + "/" + datos.totalSemanas;
    }
    const titulo = document.getElementById("curso-titulo");
    if (titulo) {
        titulo.textContent = datos.nombre;
    }
}

function dibujarUnidad(unidad) {
    const completa = unidad.semanasEntregadas === 4;
    const hexClase = unidad.semanasEntregadas > 0 ? "hexagono" : "hexagono vacio";

    const resumen = unidad.semanasEntregadas === 0
        ? "Sin tareas subidas todavía"
        : unidad.semanasEntregadas + " de 4 semanas con tareas";

    return `
      <section class="unidad${completa ? " abierta" : ""}">
        <button class="unidad-cabecera" type="button" aria-expanded="${completa}">
          <span class="${hexClase}">${unidad.numero}</span>
          <span class="unidad-titulo">
            <strong>Unidad ${unidad.numero}</strong>
            <span>${resumen}</span>
          </span>
          <svg class="flecha" width="18" height="18" viewBox="0 0 24 24" fill="none"
               stroke="currentColor" stroke-width="2.4" aria-hidden="true">
            <path d="M6 9l6 6 6-6"/>
          </svg>
        </button>
        <div class="unidad-cuerpo">
          ${unidad.semanas.map((s) => dibujarSemana(unidad.numero, s)).join("")}
        </div>
      </section>`;
}

function dibujarSemana(numeroUnidad, semana) {
    const tiene = semana.archivos.length > 0;

    const contenido = tiene
        ? `<div class="lista-tareas">${semana.archivos.map(dibujarTarea).join("")}</div>`
        : `<div class="semana-vacia">Aún no hay tareas subidas en esta semana.</div>`;

    const insignia = tiene
        ? `<span class="pin pin-ok">${semana.archivos.length} ${semana.archivos.length === 1 ? "tarea" : "tareas"}</span>`
        : `<span class="pin pin-pendiente">Pendiente</span>`;

    return `
      <div class="semana">
        <div class="semana-cabecera">
          <span class="semana-numero">Semana ${semana.numero}</span>
          ${insignia}
        </div>
        ${contenido}
      </div>`;
}

function dibujarTarea(archivo) {
    return `
      <div class="tarea">
        <span class="tarea-icono">${extension(archivo.nombreOriginal)}</span>
        <span class="tarea-info">
          <span class="titulo">${escapar(archivo.titulo || archivo.nombreOriginal)}</span>
          <span class="meta">${escapar(archivo.nombreOriginal)} &middot; ${pesoLegible(archivo.tamano)}</span>
        </span>
        <span class="tarea-acciones">
          <a class="btn btn-linea btn-mini" href="visualizar?id=${archivo.id}" target="_blank"
             rel="noopener">Ver</a>
          <a class="btn btn-mini" href="descargar?id=${archivo.id}">Descargar</a>
        </span>
      </div>`;
}

function activarAcordeon() {
    document.querySelectorAll(".unidad-cabecera").forEach((cabecera) => {
        cabecera.addEventListener("click", () => {
            const unidad = cabecera.closest(".unidad");
            const abierta = unidad.classList.toggle("abierta");
            cabecera.setAttribute("aria-expanded", abierta);
        });
    });
}

/* ---------- utilidades ---------- */

function extension(nombre) {
    if (!nombre || !nombre.includes(".")) return "DOC";
    return nombre.split(".").pop().slice(0, 4).toUpperCase();
}

function pesoLegible(bytes) {
    if (!bytes) return "0 KB";
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1048576) return Math.round(bytes / 1024) + " KB";
    return (bytes / 1048576).toFixed(1) + " MB";
}

function escapar(texto) {
    const div = document.createElement("div");
    div.textContent = texto == null ? "" : texto;
    return div.innerHTML;
}

function confirmarEliminacion(nombre) {
    return confirm('¿Eliminar "' + nombre + '"? Esta acción no se puede deshacer.');
}
