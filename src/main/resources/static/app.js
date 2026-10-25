document.addEventListener("DOMContentLoaded", () => {
    cargarResumen();
    cargarCeldas();
});

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

                tr.innerHTML = `
                    <td>${celda.codigo}</td>
                    <td>${celda.tipoAsignado}</td>
                    <td>${estadoBadge}</td>
                    <td>${celda.vehiculo ? celda.vehiculo.placa : '-'}</td>
                    <td>${celda.vehiculo && celda.vehiculo.propietario ? celda.vehiculo.propietario.nombre : '-'}</td>
                    <td>
                        ${!celda.disponible ? `<button class="btn btn-sm btn-outline-danger" onclick="liberarCelda('${celda.codigo}')">Salida</button>` : '-'}
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
    
    const datos = {
        placa: document.getElementById('placa').value,
        marca: document.getElementById('marca').value,
        color: document.getElementById('color').value,
        tipo: document.getElementById('tipo').value,
        cedula: document.getElementById('cedula').value,
        nombrePropietario: document.getElementById('nombrePropietario').value
    };

    fetch('/api/ingreso', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) {
            alert("⚠️ " + data.error);
        } else {
            alert("✅ " + data.mensaje);
            document.getElementById('form-ingreso').reset();
            cargarResumen(); // Actualiza los números
            cargarCeldas();  // Actualiza la tabla
        }
    })
    .catch(err => console.error("Error al registrar:", err));
});
// Función para liberar la celda
function liberarCelda(codigo) {
    if(!confirm("¿Confirmar salida del vehículo en la celda " + codigo + "?")) return;

    fetch('/api/salida/' + codigo, {
        method: 'POST'
    })
    .then(res => res.json())
    .then(data => {
        if (data.error) {
            alert("⚠️ " + data.error);
        } else {
            alert("✅ " + data.mensaje);
            cargarResumen(); // Actualiza contadores
            cargarCeldas();  // Actualiza la tabla
        }
    })
    .catch(err => console.error("Error al procesar salida:", err));
}