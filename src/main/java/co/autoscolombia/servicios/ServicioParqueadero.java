package co.autoscolombia.servicios;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.Ocupacion;
import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Usuario;
import co.autoscolombia.dominio.Vehiculo;
import co.autoscolombia.repositorio.CeldaRepository;
import co.autoscolombia.repositorio.OcupacionRepository;
import co.autoscolombia.repositorio.UsuarioRepository;
import co.autoscolombia.repositorio.VehiculoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioParqueadero {

    private final CeldaRepository celdaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OcupacionRepository ocupacionRepository;

    public ServicioParqueadero(CeldaRepository celdaRepository,
                               VehiculoRepository vehiculoRepository,
                               UsuarioRepository usuarioRepository,
                               OcupacionRepository ocupacionRepository) {
        this.celdaRepository = celdaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ocupacionRepository = ocupacionRepository;
    }

    @PostConstruct
    @Transactional
    public void inicializarCeldas() {
        if (celdaRepository.count() > 0) {
            return;
        }
        List<Celda> celdas = new ArrayList<>();
        for (int i = 1; i <= 70; i++) {
            celdas.add(new Celda(String.format("C-%02d", i), TipoVehiculo.CARRO));
        }
        for (int i = 1; i <= 10; i++) {
            celdas.add(new Celda(String.format("M-%02d", i), TipoVehiculo.MOTO));
        }
        celdaRepository.saveAll(celdas);
    }

    @Transactional
    public Celda registrarEntrada(Vehiculo vehiculo, int puertaIngreso) throws Exception {
        if (puertaIngreso != 1 && puertaIngreso != 2) {
            throw new Exception("La puerta debe ser 1 o 2.");
        }

        if (ocupacionRepository.findByVehiculoPlacaAndFechaHoraSalidaIsNull(vehiculo.getPlaca()).isPresent()) {
            throw new Exception("El vehículo " + vehiculo.getPlaca() + " ya está adentro.");
        }

        Vehiculo vehiculoBD = vehiculoRepository.findByPlaca(vehiculo.getPlaca()).orElse(null);
        if (vehiculoBD == null) {
            Usuario propietario = usuarioRepository
                    .findByCedula(vehiculo.getPropietario().getCedula()).orElse(null);
            if (propietario == null) {
                propietario = usuarioRepository.save(vehiculo.getPropietario());
            }
            vehiculoBD = vehiculoRepository.save(new Vehiculo(
                    vehiculo.getPlaca(), vehiculo.getMarca(), vehiculo.getColor(),
                    vehiculo.getTipo(), propietario));
        }

        List<Celda> disponibles = celdaRepository.findByTipoAsignadoAndDisponibleTrue(vehiculoBD.getTipo());
        if (disponibles.isEmpty()) {
            throw new Exception("No hay celdas disponibles para " + vehiculoBD.getTipo());
        }

        Celda celda = disponibles.get(0);
        celda.setDisponible(false);
        celda.setVehiculo(vehiculoBD);
        celdaRepository.save(celda);

        ocupacionRepository.save(new Ocupacion(vehiculoBD, celda, LocalDateTime.now(), puertaIngreso));

        System.out.println("✅ [PUERTA " + puertaIngreso + "] Vehículo " + vehiculoBD.getPlaca()
                + " ingresa. Celda asignada: " + celda.getCodigo());
        return celda;
    }

    @Transactional
    public void registrarSalida(String placa) throws Exception {
        registrarSalida(placa, null);
    }

    @Transactional
    public void registrarSalida(String placa, Integer puertaSalida) throws Exception {
        if (puertaSalida != null && puertaSalida != 1 && puertaSalida != 2) {
            throw new Exception("La puerta debe ser 1 o 2.");
        }

        Ocupacion ocupacion = ocupacionRepository.findByVehiculoPlacaAndFechaHoraSalidaIsNull(placa)
                .orElseThrow(() -> new Exception("El vehículo " + placa + " no está registrado en el parqueadero."));

        ocupacion.cerrar(LocalDateTime.now(), puertaSalida);
        ocupacionRepository.save(ocupacion);

        Celda celda = ocupacion.getCelda();
        celda.setDisponible(true);
        celda.setVehiculo(null);
        celdaRepository.save(celda);

        System.out.println("🚪 [SALIDA] Vehículo " + placa + " sale. La celda " + celda.getCodigo() + " queda libre.");
    }

    @Transactional
    public void registrarSalidaPorCelda(String codigo) throws Exception {
        Ocupacion ocupacion = ocupacionRepository.findByCeldaCodigoAndFechaHoraSalidaIsNull(codigo)
                .orElseThrow(() -> new Exception("No hay un vehículo activo en la celda " + codigo + "."));

        registrarSalida(ocupacion.getVehiculo().getPlaca());
    }

    public void generarReporte() {
        List<Celda> celdas = celdaRepository.findAll();

        long carrosTotal = celdas.stream().filter(c -> c.getTipoAsignado() == TipoVehiculo.CARRO).count();
        long motosTotal = celdas.stream().filter(c -> c.getTipoAsignado() == TipoVehiculo.MOTO).count();
        long carrosOcupados = celdas.stream()
                .filter(c -> c.getTipoAsignado() == TipoVehiculo.CARRO && !c.isDisponible()).count();
        long motosOcupadas = celdas.stream()
                .filter(c -> c.getTipoAsignado() == TipoVehiculo.MOTO && !c.isDisponible()).count();

        System.out.println("\n=== REPORTE AUTOS COLOMBIA ===");
        System.out.println("Vehículos actualmente adentro: " + (carrosOcupados + motosOcupadas));
        System.out.println("Celdas Carro -> Ocupadas: " + carrosOcupados + " | Disponibles: " + (carrosTotal - carrosOcupados));
        System.out.println("Celdas Moto -> Ocupadas: " + motosOcupadas + " | Disponibles: " + (motosTotal - motosOcupadas));
        System.out.println("==============================\n");
    }
}