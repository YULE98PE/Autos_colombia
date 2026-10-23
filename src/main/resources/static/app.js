document.addEventListener("DOMContentLoaded", () => {
    cargarResumen();
    cargarCeldas();
});

// ---------- Utilidades ----------

// Evita que un texto escrito por el usuario se interprete como código HTML
function esc(texto) {
    const d = document.createElement('div');
    d.textContent = texto == null ? '' : String(texto);
    return d.innerHTML.replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}

function formatoFecha(valor) {
    return new Date(valor).toLocaleString('es-CO', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
    });
}

function formatoTiempo(desde, hasta) {
    const minutos = Math.max(0, Math.round((new Date(hasta) - new Date(desde)) / 60000));
    const h = Math.floor(minutos / 60);
    const m = minutos % 60;
    return h === 0 ? m + ' min' : h + ' h ' + m + ' min';
}

// Si el servidor no manda la celda por separado, la busca dentro del mensaje (ej: "Celda asignada: C01")
function celdaDelMensaje(mensaje) {
    const r = /celda(?:\s+asignada)?[:\s]+([A-Za-z]{1,2}-?\d{1,3})/i.exec(mensaje || '');
    return r ? r[1].toUpperCase() : null;
}

// Ventana bonita de resultado: éxito o error
function mostrarResultado(opciones) {
    const ok = opciones.tipo === 'ok';

    const icono = document.getElementById('resultadoIcono');
    icono.className = 'mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle fs-1 fw-bold '
        + (ok ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger');
    icono.textContent = ok ? '✓' : '!';

    document.getElementById('resultadoTitulo').textContent = opciones.titulo;
    document.getElementById('resultadoMensaje').textContent = opciones.mensaje || '';

    const cajaCelda = document.getElementById('resultadoCelda');
    if (opciones.celda) {
        document.getElementById('resultadoCeldaEtiqueta').textContent = opciones.etiquetaCelda || 'Celda asignada';
        document.getElementById('resultadoCeldaCodigo').textContent = opciones.celda;
        cajaCelda.classList.remove('d-none');
    } else {
        cajaCelda.classList.add('d-none');
    }

    const lista = document.getElementById('resultadoDatos');
    lista.innerHTML = '';
    const datos = (opciones.datos || []).filter(d => d[1]);
    datos.forEach(([etiqueta, valor]) => {
        const fila = document.createElement('div');
        fila.className = 'd-flex justify-content-between border-bottom py-2';
        const dt = document.createElement('dt');
        dt.className = 'fw-normal text-muted';
        dt.textContent = etiqueta;
        const dd = document.createElement('dd');
        dd.className = 'fw-bold mb-0';
        dd.textContent = valor;
        fila.appendChild(dt);
        fila.appendChild(dd);
        lista.appendChild(fila);
    });
    lista.classList.toggle('d-none', datos.length === 0);

    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalResultado')).show();
}

function mostrarError(mensaje) {
    mostrarResultado({ tipo: 'error', titulo: 'No se pudo completar', mensaje: mensaje });
}

// Lee la respuesta del servidor aunque no venga en formato JSON
function leerRespuesta(res) {
    return res.json().catch(() => ({ error: 'El servidor respondió algo inesperado. Inténtalo de nuevo.' }));
}

// 1. Cargar las tarjetas superiores
function cargarResumen() {
    fetch('/api/resumen')
        .then(res => res.json())
        .then(data => {
            document.getElementById('disponibles-carro').textContent = data.carrosDisponibles !== undefined ? data.carrosDisponibles : 0;
            document.getElementById('disponibles-moto').textContent = data.motosDisponibles !== undefined ? data.motosDisponibles : 0;
        })
        .catch(err => console.error("Error al cargar resumen:", err));
}

// 2. Cargar la tabla de celdas
function cargarCeldas() {
    fetch('/api/celdas')
        .then(res => res.json())
        .then(celdas => {
            const tbody = document.getElementById('tabla-celdas');
            tbody.innerHTML = '';

            if (celdas.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center py-3 text-muted">No hay celdas registradas en la base de datos.</td></tr>';
                return;
            }

            celdas.forEach(celda => {
                const tr = document.createElement('tr');
                const estadoBadge = !celda.disponible 
                    ? '<span class="badge bg-danger">Ocupada</span>' 
                    : '<span class="badge bg-success">Disponible</span>';

                const placa = celda.vehiculo ? celda.vehiculo.placa : '';

                tr.innerHTML = `
                    <td>${esc(celda.codigo)}</td>
                    ${celda.tipoAsignado === 'CARRO' ? '<td><i class="bi bi-car-front-fill me-2"></i>Carro</td>' : `<td>${esc(celda.tipoAsignado)}</td>`}
                    <td>${estadoBadge}</td>
                    <td>${placa ? esc(placa) : '-'}</td>
                    <td>${celda.vehiculo && celda.vehiculo.propietario ? esc(celda.vehiculo.propietario.nombre) : '-'}</td>
                    <td>
                        ${!celda.disponible ? `<button class="btn btn-sm btn-outline-danger" data-codigo="${esc(celda.codigo)}" data-placa="${esc(placa)}" onclick="pedirSalida(this)">Salida</button>` : '-'}
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error("Error al cargar celdas:", err));
}

// 3. Capturar el envío del formulario
document.getElementById('form-ingreso').addEventListener('submit', function(e) {
    e.preventDefault();

    const operario = document.getElementById('operario').value;

    const datos = {
        placa: document.getElementById('placa').value,
        marca: document.getElementById('marca').value,
        color: document.getElementById('color').value,
        tipo: document.getElementById('tipo').value,
        cedula: document.getElementById('cedula').value,
        nombrePropietario: document.getElementById('nombrePropietario').value,
        puerta: Number(operario)
    };

    const boton = this.querySelector('button[type="submit"]');
    boton.disabled = true;

    fetch('/api/ingreso', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    })
    .then(leerRespuesta)
    .then(data => {
        if (data.error) {
            mostrarError(data.error);
        } else {
            mostrarResultado({
                tipo: 'ok',
                titulo: '¡Ingreso registrado!',
                mensaje: 'El vehículo ya puede pasar a su celda.',
                celda: data.celda || celdaDelMensaje(data.mensaje),
                etiquetaCelda: 'Celda asignada',
                datos: [
                    ['Placa', datos.placa.toUpperCase()],
                    ['Vehículo', datos.marca + ' · ' + datos.color + ' (' + (datos.tipo === 'CARRO' ? 'Carro' : 'Moto') + ')'],
                    ['Puerta de ingreso', 'Puerta ' + operario],
                    ['Hora de ingreso', formatoFecha(data.horaEntrada || new Date())]
                ]
            });
            document.getElementById('form-ingreso').reset();
            cargarResumen(); // Actualiza los números
            cargarCeldas();  // Actualiza la tabla
        }
    })
    .catch(err => {
        console.error("Error al registrar:", err);
        mostrarError('No se pudo conectar con el servidor. Revisa que la aplicación esté encendida.');
    })
    .finally(() => { boton.disabled = false; });
});

// 4. Salida: primero se pide confirmación, luego se registra
let salidaPendiente = null;

function pedirSalida(boton) {
    salidaPendiente = { codigo: boton.dataset.codigo, placa: boton.dataset.placa };
    const quien = salidaPendiente.placa ? 'El vehículo ' + salidaPendiente.placa : 'El vehículo';
    document.getElementById('confirmarSalidaTexto').textContent =
        quien + ' sale de la celda ' + salidaPendiente.codigo + '. La celda quedará libre.';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modalConfirmarSalida')).show();
}

document.getElementById('btnConfirmarSalida').addEventListener('click', () => {
    if (!salidaPendiente) return;
    const { codigo, placa } = salidaPendiente;
    salidaPendiente = null;

    const ventanaConfirmar = document.getElementById('modalConfirmarSalida');
    // Espera a que se cierre la ventana de confirmar antes de abrir la del resultado
    ventanaConfirmar.addEventListener('hidden.bs.modal', () => liberarCelda(codigo, placa), { once: true });
    bootstrap.Modal.getOrCreateInstance(ventanaConfirmar).hide();
});

// Función para liberar la celda
function liberarCelda(codigo, placa) {
    fetch('/api/salida/' + encodeURIComponent(codigo), {
        method: 'POST'
    })
    .then(leerRespuesta)
    .then(data => {
        if (data.error) {
            mostrarError(data.error);
        } else {
            mostrarResultado({
                tipo: 'ok',
                titulo: 'Salida registrada',
                mensaje: data.mensaje || 'La salida quedó registrada.',
                celda: codigo,
                etiquetaCelda: 'Celda liberada',
                datos: [
                    ['Placa', placa],
                    ['Ingresó', data.horaEntrada ? formatoFecha(data.horaEntrada) : ''],
                    ['Salió', data.horaSalida ? formatoFecha(data.horaSalida) : ''],
                    ['Tiempo adentro', data.horaEntrada && data.horaSalida ? formatoTiempo(data.horaEntrada, data.horaSalida) : '']
                ]
            });
            cargarResumen(); // Actualiza contadores
            cargarCeldas();  // Actualiza la tabla
        }
    })
    .catch(err => {
        console.error("Error al procesar salida:", err);
        mostrarError('No se pudo conectar con el servidor. Revisa que la aplicación esté encendida.');
    });
}