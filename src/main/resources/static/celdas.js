let tipoActual = '';     // '' = todos, CARRO, MOTO
let estadoActual = '';   // '' = todos, DISPONIBLE, OCUPADA, FUERA_DE_SERVICIO
let celdaElegida = null;
let accionElegida = null;

const modalEstado = () => bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-estado'));
const modalError = () => bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-error'));

document.addEventListener('DOMContentLoaded', () => {
    activarFiltro('filtro-tipo', valor => { tipoActual = valor; cargar(); });
    activarFiltro('filtro-estado', valor => { estadoActual = valor; cargar(); });
    document.getElementById('btn-confirmar-estado').addEventListener('click', confirmarCambio);

    document.getElementById('grilla-celdas').addEventListener('click', e => {
        const boton = e.target.closest('button[data-codigo]');
        if (boton) pedirCambio(boton.dataset.codigo, boton.dataset.accion);
    });
    cargar();
});

function activarFiltro(idGrupo, alElegir) {
    const botones = document.querySelectorAll('#' + idGrupo + ' button');
    botones.forEach(boton => {
        boton.addEventListener('click', () => {
            botones.forEach(b => b.className = 'btn ' + (b === boton ? 'btn-primary' : 'btn-outline-primary'));
            alElegir(boton.dataset.valor);
        });
    });
}

function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function mostrarMensaje(texto, tipo) {
    document.getElementById('mensaje').innerHTML = texto
        ? '<div class="alert alert-' + tipo + ' mb-3">' + esc(texto) + '</div>'
        : '';
}

// HU-13 y HU-14: consultar celdas, con filtros opcionales de tipo y estado
function cargar() {
    const params = new URLSearchParams();
    if (tipoActual) params.set('tipo', tipoActual);
    if (estadoActual) params.set('estado', estadoActual);

    fetch('/api/celdas?' + params.toString())
        .then(res => res.json().then(cuerpo => ({ ok: res.ok, cuerpo })))
        .then(({ ok, cuerpo }) => {
            if (!ok) {
                mostrarMensaje(cuerpo.error || 'No se pudieron cargar las celdas.', 'warning');
                return;
            }
            pintar(cuerpo);
        })
        .catch(() => {
            document.getElementById('grilla-celdas').innerHTML =
                '<div class="col-12 text-center text-danger py-3">No se pudo cargar. Revisa que la aplicación esté encendida.</div>';
        });
}

const ESTILOS = {
    DISPONIBLE:        { borde: '#198754', fondo: '#e8f5ec', texto: 'Disponible' },
    OCUPADA:           { borde: '#dc3545', fondo: '#fde8ea', texto: 'Ocupada' },
    FUERA_DE_SERVICIO: { borde: '#6c757d', fondo: '#e9ecef', texto: 'Fuera de servicio' }
};

function pintar(celdas) {
    const grilla = document.getElementById('grilla-celdas');
    document.getElementById('conteo').textContent = celdas.length + ' celdas';

    if (celdas.length === 0) {
        grilla.innerHTML = '<div class="col-12 text-center text-muted py-3">No hay celdas con ese criterio.</div>';
        return;
    }

    grilla.innerHTML = '';
    celdas.forEach(c => {
        const e = ESTILOS[c.estado] || ESTILOS.DISPONIBLE;
        const boton = c.estado === 'FUERA_DE_SERVICIO'
            ? '<button type="button" class="btn btn-sm btn-success mt-2" data-codigo="' + esc(c.codigo) + '" data-accion="HABILITAR">Habilitar</button>'
            : '<button type="button" class="btn btn-sm btn-outline-secondary mt-2" data-codigo="' + esc(c.codigo) + '" data-accion="FUERA_DE_SERVICIO">Fuera de servicio</button>';

        const col = document.createElement('div');
        col.className = 'col-6 col-sm-4 col-md-3 col-lg-2';
        col.innerHTML =
            '<div class="text-center p-2" style="border: 2px solid ' + e.borde + '; background: ' + e.fondo + '; border-radius: 10px;">' +
              '<div class="fw-bold" style="color: ' + e.borde + ';">' + esc(c.codigo) + '</div>' +
              '<div class="small" style="color: ' + e.borde + ';">' + e.texto + '</div>' +
              boton +
            '</div>';
        grilla.appendChild(col);
    });
}

// HU-15 y HU-16: pedir confirmación antes de cambiar el estado
function pedirCambio(codigo, accion) {
    celdaElegida = codigo;
    accionElegida = accion;
    const boton = document.getElementById('btn-confirmar-estado');
    if (accion === 'HABILITAR') {
        document.getElementById('titulo-estado').textContent = '¿Habilitar celda?';
        document.getElementById('texto-estado').textContent =
            'La celda ' + codigo + ' pasará a disponible y volverá a asignarse en las entradas.';
        boton.textContent = 'Habilitar';
        boton.className = 'btn btn-success fw-bold';
    } else {
        document.getElementById('titulo-estado').textContent = '¿Poner fuera de servicio?';
        document.getElementById('texto-estado').textContent =
            'La celda ' + codigo + ' no se asignará a nuevas entradas mientras esté en mantenimiento.';
        boton.textContent = 'Confirmar';
        boton.className = 'btn btn-danger fw-bold';
    }
    modalEstado().show();
}

function confirmarCambio() {
    fetch('/api/celdas/' + encodeURIComponent(celdaElegida) + '/estado', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accion: accionElegida })
    })
        .then(res => res.json().then(cuerpo => ({ ok: res.ok, cuerpo })))
        .then(({ ok, cuerpo }) => {
            modalEstado().hide();
            if (!ok) {
                document.getElementById('texto-error').textContent = cuerpo.error || 'No se pudo cambiar el estado.';
                modalError().show();
                return;
            }
            mostrarMensaje(cuerpo.mensaje, 'success');
            cargar();
        })
        .catch(() => {
            modalEstado().hide();
            document.getElementById('texto-error').textContent = 'No se pudo conectar con la aplicación.';
            modalError().show();
        });
}