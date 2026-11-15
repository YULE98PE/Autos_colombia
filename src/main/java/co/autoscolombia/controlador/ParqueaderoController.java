package co.autoscolombia.controlador;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.Ocupacion;
import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Usuario;
import co.autoscolombia.dominio.Vehiculo;
import co.autoscolombia.repositorio.CeldaRepository;
import co.autoscolombia.repositorio.OcupacionRepository;
import co.autoscolombia.servicios.ServicioParqueadero;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite que el frontend se conecte sin bloqueos de seguridad
public class ParqueaderoController {

    private final CeldaRepository celdaRepository;
    private final OcupacionRepository ocupacionRepository;
    private final ServicioParqueadero servicioParqueadero;

    public ParqueaderoController(CeldaRepository celdaRepository,
                                 OcupacionRepository ocupacionRepository,
                                 ServicioParqueadero servicioParqueadero) {
        this.celdaRepository = celdaRepository;
        this.ocupacionRepository = ocupacionRepository;
        this.servicioParqueadero = servicioParqueadero;
    }

    // 1. Envía el conteo de celdas disponibles para las tarjetas superiores
    @GetMapping("/resumen")
    public Map<String, Long> obtenerResumen() {
        List<Celda> todasLasCeldas = celdaRepository.findAll();
        
        // Contar las celdas que están disponibles (isDisponible() == true) según el tipo
        long carrosDisponibles = todasLasCeldas.stream()
                .filter(c -> c.isDisponible() && c.getTipoAsignado() == TipoVehiculo.CARRO)
                .count();
                
        long motosDisponibles = todasLasCeldas.stream()
                .filter(c -> c.isDisponible() && c.getTipoAsignado() == TipoVehiculo.MOTO)
                .count();

        Map<String, Long> resumen = new HashMap<>();
        resumen.put("carrosDisponibles", carrosDisponibles);
        resumen.put("motosDisponibles", motosDisponibles);
        return resumen;
    }

    // 2. Envía la lista de todas las celdas para llenar la tabla
    // (HU-13 y HU-14) Sin parámetros devuelve todas; con tipo y/o estado, las filtra.
    @GetMapping("/celdas")
    public ResponseEntity<Object> obtenerCeldas(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado) {
        try {
            return ResponseEntity.ok(servicioParqueadero.consultarCeldas(tipo, estado));
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    // Historial de movimientos (HU-07): ocupaciones activas y finalizadas
    @GetMapping("/historial")
    public List<Map<String, Object>> obtenerHistorial() {
        return servicioParqueadero.consultarHistorial();
    }

    // 3. Recibe los datos del formulario web y registra el ingreso
    @PostMapping("/ingreso")
    public ResponseEntity<Map<String, String>> registrarIngreso(@RequestBody Map<String, String> datos) {
        Map<String, String> respuesta = new HashMap<>();

        try {
            String placa = valorRequerido(datos, "placa").toUpperCase(Locale.ROOT);
            String marca = valorRequerido(datos, "marca");
            String color = valorRequerido(datos, "color");
            String cedula = valorRequerido(datos, "cedula");
            String nombrePropietario = valorRequerido(datos, "nombrePropietario");
            TipoVehiculo tipo = TipoVehiculo.valueOf(valorRequerido(datos, "tipo").toUpperCase(Locale.ROOT));
            int puerta = leerPuerta(datos);

            Usuario propietario = new Usuario(cedula, nombrePropietario);
            Vehiculo vehiculo = new Vehiculo(placa, marca, color, tipo, propietario);
            Celda celda = servicioParqueadero.registrarEntrada(vehiculo, puerta);

            // Hora real guardada en la base de datos
            LocalDateTime horaEntrada = ocupacionRepository
                    .findByVehiculoPlacaAndFechaHoraSalidaIsNull(placa)
                    .map(Ocupacion::getFechaHoraEntrada)
                    .orElse(LocalDateTime.now());

            respuesta.put("mensaje", "Ingreso exitoso en la celda " + celda.getCodigo());
            respuesta.put("celda", celda.getCodigo());
            respuesta.put("placa", placa);
            respuesta.put("puerta", String.valueOf(puerta));
            respuesta.put("horaEntrada", horaEntrada.toString());
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            respuesta.put("error", "Datos inválidos: " + e.getMessage());
            return ResponseEntity.badRequest().body(respuesta);
        } catch (Exception e) {
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    private String valorRequerido(Map<String, String> datos, String campo) {
        String valor = datos.get(campo);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("el campo " + campo + " es obligatorio.");
        }
        return valor.trim();
    }

    // La puerta solo puede ser 1 o 2 (las dos entradas del parqueadero)
    private int leerPuerta(Map<String, String> datos) {
        String valor = valorRequerido(datos, "puerta");
        try {
            int puerta = Integer.parseInt(valor);
            if (puerta == 1 || puerta == 2) {
                return puerta;
            }
        } catch (NumberFormatException e) {
            // cae al mensaje de abajo
        }
        throw new IllegalArgumentException("la puerta debe ser 1 o 2.");
    }

    @PostMapping("/salida/{codigo}")
    public ResponseEntity<Map<String, String>> registrarSalida(@PathVariable String codigo) {
        Map<String, String> respuesta = new HashMap<>();
        try {
            // Se busca la ocupación ANTES de la salida para conocer la placa y la hora de ingreso
            Optional<Ocupacion> activa = ocupacionRepository.findByCeldaCodigoAndFechaHoraSalidaIsNull(codigo);

            servicioParqueadero.registrarSalidaPorCelda(codigo);

            respuesta.put("mensaje", "Salida registrada. Celda " + codigo + " liberada.");
            respuesta.put("celda", codigo);

            if (activa.isPresent()) {
                Ocupacion ocupacion = activa.get();
                LocalDateTime horaSalida = ocupacionRepository.findById(ocupacion.getId())
                        .map(Ocupacion::getFechaHoraSalida)
                        .orElse(null);

                respuesta.put("placa", ocupacion.getVehiculo().getPlaca());
                respuesta.put("horaEntrada", ocupacion.getFechaHoraEntrada().toString());
                respuesta.put("horaSalida", (horaSalida != null ? horaSalida : LocalDateTime.now()).toString());
            }
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    // =====================================================================
    // Iteración 2 · Gestión de usuarios
    // =====================================================================

    // HU-09: registrar usuario
    @PostMapping("/usuarios")
    public ResponseEntity<Object> registrarUsuario(@RequestBody Map<String, String> datos) {
        try {
            Usuario u = servicioParqueadero.registrarUsuario(
                    datos.get("cedula"), datos.get("nombre"));
            return ResponseEntity.ok(respuestaUsuario("Usuario registrado y activo.", u));
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    // HU-10: buscar por cédula o nombre. Lista vacía = "Usuario no encontrado"
    @GetMapping("/usuarios")
    public ResponseEntity<Object> buscarUsuarios(@RequestParam String criterio) {
        try {
            return ResponseEntity.ok(servicioParqueadero.buscarUsuarios(criterio));
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    // HU-11: modificar nombre y/o cédula del usuario identificado por {cedula}
    @PostMapping("/usuarios/{cedula}/modificar")
    public ResponseEntity<Object> modificarUsuario(@PathVariable String cedula,
                                                   @RequestBody Map<String, String> datos) {
        try {
            Usuario u = servicioParqueadero.modificarUsuario(
                    cedula, datos.get("cedula"), datos.get("nombre"));
            return ResponseEntity.ok(respuestaUsuario("Cambios guardados.", u));
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    // HU-12: desactivar usuario (baja lógica)
    @PostMapping("/usuarios/{cedula}/desactivar")
    public ResponseEntity<Object> desactivarUsuario(@PathVariable String cedula) {
        try {
            Usuario u = servicioParqueadero.desactivarUsuario(cedula);
            return ResponseEntity.ok(respuestaUsuario(
                    "Usuario desactivado. Su historial se conserva.", u));
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    // =====================================================================
    // Iteración 2 · Gestión de celdas
    // =====================================================================

    // HU-15 y HU-16: cuerpo {"accion": "FUERA_DE_SERVICIO"} o {"accion": "HABILITAR"}
    @PostMapping("/celdas/{codigo}/estado")
    public ResponseEntity<Object> cambiarEstadoCelda(@PathVariable String codigo,
                                                     @RequestBody Map<String, String> datos) {
        try {
            String mensaje = servicioParqueadero.cambiarEstadoCelda(codigo, datos.get("accion"));
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("mensaje", mensaje);
            respuesta.put("celda", codigo.trim().toUpperCase(Locale.ROOT));
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return respuestaError(e);
        }
    }

    private Map<String, Object> respuestaUsuario(String mensaje, Usuario u) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensaje", mensaje);
        respuesta.put("cedula", u.getCedula());
        respuesta.put("nombre", u.getNombre());
        respuesta.put("activo", u.isActivo());
        return respuesta;
    }

    private ResponseEntity<Object> respuestaError(Exception e) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}