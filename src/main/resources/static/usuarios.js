let modo = 'nuevo';          // 'nuevo' (HU-09) o 'modificar' (HU-11)
let cedulaOriginal = null;   // cédula del usuario que se está modificando
let cedulaADesactivar = null;

const modalUsuario = () => bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-usuario'));
const modalDesactivar = () => bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-desactivar'));
const modalError = () => bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-error'));

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('form-buscar').addEventListener('submit', e => {
        e.preventDefault();
        buscar(document.getElementById('criterio').value);
    });
    document.getElementById('btn-nuevo').addEventListener('click', abrirNuevo);
    document.getElementById('form-usuario').addEventListener('submit', e => {
        e.preventDefault();
        guardar();
    });
    document.getElementById('btn-confirmar-desactivar').addEventListener('click', desactivar);

    // Botones de cada fila (Modificar / Desactivar)
    document.getElementById('tabla-usuarios').addEventListener('click', e => {
        const boton = e.target.closest('button[data-accion]');
        if (!boton) return;
        if (boton.dataset.accion === 'modificar') {
            abrirModificar(boton.dataset.cedula, boton.dataset.nombre);
        } else {
            pedirDesactivar(boton.dataset.cedula, boton.dataset.nombre);
        }
    });
});

// Evita que un texto escrito por el usuario se interprete como código HTML
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

function filaVacia(texto) {
    document.getElementById('tabla-usuarios').innerHTML =
        '<tr><td colspan="5" class="text-center py-3 text-muted">' + esc(texto) + '</td></tr>';
}

// HU-10: buscar por cédula o nombre
function buscar(criterio) {
    mostrarMensaje('', '');
    fetch('/api/usuarios?criterio=' + encodeURIComponent(criterio.trim()))
        .then(res => res.json().then(cuerpo => ({ ok: res.ok, cuerpo })))
        .then(({ ok, cuerpo }) => {
            if (!ok) {
                filaVacia('Busca por cédula o nombre para ver los usuarios.');
                mostrarMensaje(cuerpo.error || 'No se pudo buscar.', 'warning');
                return;
            }
            if (cuerpo.length === 0) {
                filaVacia('Sin resultados.');
                mostrarMensaje('Usuario no encontrado.', 'warning');
                return;
            }
            pintar(cuerpo);
        })
        .catch(() => {
            filaVacia('');
            mostrarMensaje('No se pudo conectar. Revisa que la aplicación esté encendida.', 'danger');
        });
}

function pintar(usuarios) {
    const tbody = document.getElementById('tabla-usuarios');
    tbody.innerHTML = '';
    usuarios.forEach(u => {
        const vehiculos = u.vehiculos.length
            ? u.vehiculos.length + ' · ' + u.vehiculos.map(esc).join(', ')
            : '<span class="text-muted">Sin vehículos</span>';
        const estado = u.activo
            ? '<span class="badge bg-success">Activo</span>'
            : '<span class="badge bg-secondary">Inactivo</span>';
        const atributos = 'data-cedula="' + esc(u.cedula) + '" data-nombre="' + esc(u.nombre) + '"';
        const desactivar = u.activo
            ? '<button type="button" class="btn btn-sm btn-outline-danger" data-accion="desactivar" ' + atributos + '>Desactivar</button>'
            : '';
        const tr = document.createElement('tr');
        tr.innerHTML =
            '<td class="fw-bold">' + esc(u.cedula) + '</td>' +
            '<td>' + esc(u.nombre) + '</td>' +
            '<td>' + vehiculos + '</td>' +
            '<td>' + estado + '</td>' +
            '<td class="d-flex gap-2">' +
              '<button type="button" class="btn btn-sm btn-outline-secondary" data-accion="modificar" ' + atributos + '>Modificar</button>' +
              desactivar +
            '</td>';
        tbody.appendChild(tr);
    });
}

// ---------- Formulario (registrar / modificar) ----------
function limpiarErrores() {
    ['u-cedula', 'u-nombre'].forEach(id => document.getElementById(id).classList.remove('is-invalid'));
    document.getElementById('err-cedula').textContent = '';
    document.getElementById('err-nombre').textContent = '';
    const g = document.getElementById('err-general');
    g.textContent = '';
    g.classList.add('d-none');
}

function abrirNuevo() {
    modo = 'nuevo';
    cedulaOriginal = null;
    document.getElementById('titulo-usuario').textContent = 'Registrar usuario';
    document.getElementById('btn-guardar').textContent = 'Guardar usuario';
    document.getElementById('u-cedula').value = '';
    document.getElementById('u-nombre').value = '';
    limpiarErrores();
    modalUsuario().show();
}

function abrirModificar(cedula, nombre) {
    modo = 'modificar';
    cedulaOriginal = cedula;
    document.getElementById('titulo-usuario').textContent = 'Modificar usuario';
    document.getElementById('btn-guardar').textContent = 'Guardar cambios';
    document.getElementById('u-cedula').value = cedula;
    document.getElementById('u-nombre').value = nombre;
    limpiarErrores();
    modalUsuario().show();
}

// El servidor devuelve un solo mensaje; se muestra bajo el campo que menciona.
function mostrarErrorFormulario(mensaje) {
    limpiarErrores();
    const minusculas = mensaje.toLowerCase();
    if (minusculas.includes('cédula') || minusculas.includes('cedula')) {
        document.getElementById('u-cedula').classList.add('is-invalid');
        document.getElementById('err-cedula').textContent = mensaje;
    } else if (minusculas.includes('nombre')) {
        document.getElementById('u-nombre').classList.add('is-invalid');
        document.getElementById('err-nombre').textContent = mensaje;
    } else {
        const g = document.getElementById('err-general');
        g.textContent = mensaje;
        g.classList.remove('d-none');
    }
}

function guardar() {
    limpiarErrores();
    const cedula = document.getElementById('u-cedula').value.trim();
    const nombre = document.getElementById('u-nombre').value.trim();
    const url = modo === 'nuevo'
        ? '/api/usuarios'
        : '/api/usuarios/' + encodeURIComponent(cedulaOriginal) + '/modificar';

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cedula: cedula, nombre: nombre })
    })
        .then(res => res.json().then(cuerpo => ({ ok: res.ok, cuerpo })))
        .then(({ ok, cuerpo }) => {
            if (!ok) {
                mostrarErrorFormulario(cuerpo.error || 'No se pudo guardar.');
                return;
            }
            modalUsuario().hide();
            document.getElementById('criterio').value = cuerpo.cedula;
            buscar(cuerpo.cedula);
            mostrarMensaje(cuerpo.mensaje, 'success');
        })
        .catch(() => mostrarErrorFormulario('No se pudo conectar con la aplicación.'));
}

// ---------- Desactivar (HU-12) ----------
function pedirDesactivar(cedula, nombre) {
    cedulaADesactivar = cedula;
    document.getElementById('texto-desactivar').textContent =
        nombre + ' (cédula ' + cedula + ') quedará inactivo. Su historial se conserva.';
    modalDesactivar().show();
}

function desactivar() {
    fetch('/api/usuarios/' + encodeURIComponent(cedulaADesactivar) + '/desactivar', { method: 'POST' })
        .then(res => res.json().then(cuerpo => ({ ok: res.ok, cuerpo })))
        .then(({ ok, cuerpo }) => {
            modalDesactivar().hide();
            if (!ok) {
                document.getElementById('texto-error').textContent = cuerpo.error || 'No se pudo desactivar el usuario.';
                modalError().show();
                return;
            }
            buscar(cedulaADesactivar);
            mostrarMensaje(cuerpo.mensaje, 'success');
        })
        .catch(() => {
            modalDesactivar().hide();
            document.getElementById('texto-error').textContent = 'No se pudo conectar con la aplicación.';
            modalError().show();
        });
}