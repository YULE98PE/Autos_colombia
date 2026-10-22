package co.autoscolombia.controlador;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.Usuario;
import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Vehiculo;
import co.autoscolombia.repositorio.CeldaRepository;
import co.autoscolombia.servicios.ServicioParqueadero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Permite que el frontend se conecte sin bloqueos de seguridad
public class ParqueaderoController {

    private final CeldaRepository celdaRepository;
    private final ServicioParqueadero servicioParqueadero;

    public ParqueaderoController(CeldaRepository celdaRepository, ServicioParqueadero servicioParqueadero) {
        this.celdaRepository = celdaRepository;
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
    @GetMapping("/celdas")
    public List<Celda> obtenerCeldas() {
        return celdaRepository.findAll();
    }

    // 3. Recibe los datos del formulario web y registra el ingreso
    @PostMapping("/ingreso")
    public ResponseEntity<Map<String, String>> registrarIngreso(@RequestBody Map<String, String> datos) {
        Map<String, String> respuesta = new HashMap<>();

        try {
            String placa = valorRequerido(datos, "placa");
            String marca = valorRequerido(datos, "marca");
            String color = valorRequerido(datos, "color");
            String cedula = valorRequerido(datos, "cedula");
            String nombrePropietario = valorRequerido(datos, "nombrePropietario");
            TipoVehiculo tipo = TipoVehiculo.valueOf(valorRequerido(datos, "tipo").toUpperCase(Locale.ROOT));

            Usuario propietario = new Usuario(cedula, nombrePropietario);
            Vehiculo vehiculo = new Vehiculo(placa, marca, color, tipo, propietario);
            Celda celda = servicioParqueadero.registrarEntrada(vehiculo, 1);

            respuesta.put("mensaje", "Ingreso exitoso en la celda " + celda.getCodigo());
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

    @PostMapping("/salida/{codigo}")
    public ResponseEntity<Map<String, String>> registrarSalida(@PathVariable String codigo) {
        Map<String, String> respuesta = new HashMap<>();
        try {
            servicioParqueadero.registrarSalidaPorCelda(codigo);
            respuesta.put("mensaje", "Salida registrada. Celda " + codigo + " liberada.");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }
}