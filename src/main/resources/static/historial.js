let movimientos = [];
let filtroActual = 'TODOS';

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('#filtros button').forEach(boton => {
        boton.addEventListener('click', () => {
            filtroActual = boton.dataset.filtro;
            pintar();
        });
    });
    document.getElementById('buscar').addEventListener('input', pintar);
    cargarHistorial();
});

// Evita que un texto escrito por el usuario se interprete como código HTML
function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function formatoFecha(valor) {
    return new Date(valor).toLocaleString('es-CO', {
        day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function mensajeEnTabla(texto, clase) {
    document.getElementById('tabla-historial').innerHTML =
        '<tr><td colspan="7" class="text-center py-3 ' + clase + '">' + esc(texto) + '</td></tr>';
}

function cargarHistorial() {
    fetch('/api/historial')
        .then(res => {
            if (!res.ok) throw new Error('Respuesta ' + res.status);
            return res.json();
        })
        .then(datos => {
            movimientos = datos;
            pintar();
        })
        .catch(err => {
            console.error('Error al cargar el historial:', err);
            mensajeEnTabla('No se pudo cargar el historial. Revisa que la aplicación esté encendida.', 'text-danger');
        });
}

function pintar() {
    const activas = movimientos.filter(m => m.estado === 'ACTIVA').length;
    const finalizadas = movimientos.length - activas;
    const conteos = { TODOS: movimientos.length, ACTIVA: activas, FINALIZADA: finalizadas };
    const etiquetas = { TODOS: 'Todos', ACTIVA: 'Activas', FINALIZADA: 'Finalizadas' };

    // Botones de filtro: el seleccionado va relleno
    document.querySelectorAll('#filtros button').forEach(boton => {
        const f = boton.dataset.filtro;
        boton.textContent = etiquetas[f] + ' (' + conteos[f] + ')';
        boton.className = 'btn ' + (f === filtroActual ? 'btn-primary' : 'btn-outline-primary');
    });

    const texto = document.getElementById('buscar').value.trim().toUpperCase();
    const filas = movimientos.filter(m =>
        (filtroActual === 'TODOS' || m.estado === filtroActual) &&
        (texto === '' || String(m.placa).toUpperCase().includes(texto))
    );

    if (movimientos.length === 0) {
        mensajeEnTabla('Todavía no hay movimientos registrados.', 'text-muted');
        return;
    }
    if (filas.length === 0) {
        mensajeEnTabla('No hay movimientos que coincidan con el filtro.', 'text-muted');
        return;
    }

    const tbody = document.getElementById('tabla-historial');
    tbody.innerHTML = '';
    filas.forEach(m => {
        const activa = m.estado === 'ACTIVA';
        const badge = activa
            ? '<span class="badge bg-success">Activa</span>'
            : '<span class="badge bg-secondary">Finalizada</span>';

        const tr = document.createElement('tr');
        if (activa) tr.style.backgroundColor = '#f1faf4';
        tr.innerHTML = `
            <td class="fw-bold">${esc(m.placa)}</td>
            <td>${m.tipo === 'CARRO' ? 'Carro' : (m.tipo === 'MOTO' ? 'Moto' : esc(m.tipo))}</td>
            <td>${esc(m.propietario)}</td>
            <td>${esc(m.celda)}</td>
            <td>${badge}</td>
            <td>${m.horaEntrada ? esc(formatoFecha(m.horaEntrada)) : '-'}</td>
            <td>${m.horaSalida ? esc(formatoFecha(m.horaSalida)) : '<span class="text-muted">—</span>'}</td>
        `;
        tbody.appendChild(tr);
    });
}