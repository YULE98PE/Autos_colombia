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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ServicioParqueadero {

    // Formatos válidos (RF-03)
    private static final Pattern PLACA_CARRO = Pattern.compile("^[A-Z]{3}\\d{3}$");
    private static final Pattern PLACA_MOTO = Pattern.compile("^[A-Z]{3}\\d{2}[A-Z]?$");
    private static final Pattern CEDULA = Pattern.compile("^\\d{6,10}$");
    private static final Pattern NOMBRE = Pattern.compile("^\\p{L}[\\p{L} .'-]+$");
    private static final Pattern TEXTO = Pattern.compile("^[\\p{L}\\p{N}][\\p{L}\\p{N} .-]*$");

    private final CeldaRepository celdaRepository;
    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OcupacionRepository ocupacionRepository;

    public ServicioParqueadero(
            CeldaRepository celdaRepository,
            VehiculoRepository vehiculoRepository,
            UsuarioRepository usuarioRepository,
            OcupacionRepository ocupacionRepository) {

        this.celdaRepository = celdaRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.ocupacionRepository = ocupacionRepository;
    }

    // Crear las celdas iniciales si la tabla está vacía.
    @PostConstruct
    @Transactional
    public void inicializarCeldas() {

        if (celdaRepository.count() > 0) {
            return;
        }

        List<Celda> celdas = new ArrayList<>();

        for (int i = 1; i <= 70; i++) {
            celdas.add(
                    new Celda(
                            String.format("C-%02d", i),
                            TipoVehiculo.CARRO
                    )
            );
        }

        for (int i = 1; i <= 10; i++) {
            celdas.add(
                    new Celda(
                            String.format("M-%02d", i),
                            TipoVehiculo.MOTO
                    )
            );
        }

        celdaRepository.saveAll(celdas);
    }

    // Registrar la entrada de un vehículo.
    @Transactional(rollbackFor = Exception.class)
    public Celda registrarEntrada(
            Vehiculo vehiculo,
            int puertaIngreso) throws Exception {

        if (puertaIngreso != 1 && puertaIngreso != 2) {
            throw new Exception("La puerta debe ser 1 o 2.");
        }

        if (vehiculo == null) {
            throw new Exception("Debes ingresar los datos del vehículo.");
        }

        TipoVehiculo tipo = vehiculo.getTipo();
        if (tipo == null) {
            throw new Exception("Debes seleccionar el tipo de vehículo.");
        }

        String placa = normalizarPlaca(
                validarTexto(vehiculo.getPlaca(), "placa", 10));
        Pattern formatoPlaca = tipo == TipoVehiculo.CARRO
                ? PLACA_CARRO : PLACA_MOTO;
        if (!formatoPlaca.matcher(placa).matches()) {
            throw new Exception(
                    "La placa " + placa + " no es válida. "
                            + (tipo == TipoVehiculo.CARRO
                            ? "Un carro usa 3 letras y 3 números (ej: ABC123)."
                            : "Una moto usa 3 letras, 2 números y 1 letra (ej: ABC12D)."));
        }

        String marca = validarTexto(vehiculo.getMarca(), "marca", 60);
        if (!TEXTO.matcher(marca).matches()) {
            throw new Exception("La marca solo puede tener letras, números y espacios.");
        }
        String color = validarTexto(vehiculo.getColor(), "color", 40);
        if (!TEXTO.matcher(color).matches()) {
            throw new Exception("El color solo puede tener letras, números y espacios.");
        }

        Usuario propietarioIngresado = vehiculo.getPropietario();
        if (propietarioIngresado == null) {
            throw new Exception("Debes ingresar los datos del propietario.");
        }
        String cedula = validarTexto(
                propietarioIngresado.getCedula(), "cédula", 30);
        if (!CEDULA.matcher(cedula).matches()) {
            throw new Exception("La cédula debe tener solo números (entre 6 y 10 dígitos).");
        }
        String nombre = validarTexto(
                propietarioIngresado.getNombre(), "nombre del propietario", 120);
        if (!NOMBRE.matcher(nombre).matches()) {
            throw new Exception("El nombre solo puede tener letras y espacios.");
        }

        if (ocupacionRepository
                .findByVehiculoPlacaAndFechaHoraSalidaIsNull(
                        placa)
                .isPresent()) {
            throw new Exception(
                    "El vehículo " + placa
                            + " ya está adentro."
            );
        }

        Vehiculo vehiculoBD = vehiculoRepository
                .findByPlaca(placa)
                .orElse(null);

        TipoVehiculo tipoParaCelda = vehiculoBD == null
                ? tipo
                : vehiculoBD.getTipo();
        Usuario propietarioBD = usuarioRepository
                .findByCedula(cedula)
                .orElse(null);
        if (propietarioBD == null) {
            propietarioBD = usuarioRepository.save(
                    new Usuario(cedula, nombre)
            );
        } else if (!propietarioBD.getNombre().equals(nombre)) {
            propietarioBD.setNombre(nombre);
            propietarioBD = usuarioRepository.save(propietarioBD);
        }

        if (vehiculoBD == null) {
            vehiculoBD = new Vehiculo(
                    placa,
                    marca,
                    color,
                    tipo,
                    propietarioBD
            );
        } else {
            vehiculoBD.setPropietario(propietarioBD);
        }
        vehiculoBD = vehiculoRepository.save(vehiculoBD);

        // Se bloquea la celda justo antes de asignarla para que dos operarios
        // que registran a la vez nunca reciban la misma celda.
        Celda celda = celdaRepository
                .findFirstByTipoAsignadoAndDisponibleTrueOrderByCodigoAsc(
                        tipoParaCelda)
                .orElseThrow(
                        () -> new Exception(
                                "No hay celdas disponibles para "
                                        + tipoParaCelda
                        )
                );
        celda.setDisponible(false);
        celda.setVehiculo(vehiculoBD);
        celdaRepository.save(celda);

        ocupacionRepository.save(
                new Ocupacion(
                        vehiculoBD,
                        celda,
                        LocalDateTime.now(),
                        puertaIngreso
                )
        );

        System.out.println(
                "[PUERTA " + puertaIngreso + "] Vehículo "
                        + vehiculoBD.getPlaca()
                        + " ingresa. Celda asignada: "
                        + celda.getCodigo()
        );

        return celda;
    }

    // Registrar la salida utilizando la placa.
    @Transactional(rollbackFor = Exception.class)
    public void registrarSalida(String placa) throws Exception {
        registrarSalida(placa, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void registrarSalida(
            String placa,
            Integer puertaSalida) throws Exception {

        String placaNormalizada = normalizarPlaca(
                validarTexto(placa, "placa", 10));
        if (puertaSalida != null
                && puertaSalida != 1
                && puertaSalida != 2) {

            throw new Exception("La puerta debe ser 1 o 2.");
        }

        Ocupacion ocupacion = ocupacionRepository
                .findByVehiculoPlacaAndFechaHoraSalidaIsNull(placaNormalizada)
                .orElseThrow(
                        () -> new Exception(
                                "El vehículo " + placaNormalizada
                                        + " no está registrado "
                                        + "en el parqueadero."
                        )
                );

        ocupacion.cerrar(
                LocalDateTime.now(),
                puertaSalida
        );

        ocupacionRepository.save(ocupacion);

        Celda celda = ocupacion.getCelda();
        celda.setDisponible(true);
        celda.setVehiculo(null);
        celdaRepository.save(celda);

        System.out.println(
                "[SALIDA] Vehículo " + placaNormalizada
                        + " sale. La celda "
                        + celda.getCodigo()
                        + " queda libre."
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void registrarSalidaPorCelda(
            String codigo) throws Exception {

        String codigoNormalizado = validarTexto(codigo, "código de celda", 10)
                .toUpperCase(Locale.ROOT);
        Ocupacion ocupacion = ocupacionRepository
                .findByCeldaCodigoAndFechaHoraSalidaIsNull(codigoNormalizado)
                .orElseThrow(
                        () -> new Exception(
                                "No hay un vehículo activo "
                                        + "en la celda " + codigoNormalizado + "."
                        )
                );

        registrarSalida(
                ocupacion.getVehiculo().getPlaca()
        );
    }

    // Historial de movimientos (HU-07 / RF-13): activas y finalizadas.
    @Transactional(readOnly = true)
    public List<Map<String, Object>> consultarHistorial() {

        List<Map<String, Object>> historial = new ArrayList<>();

        for (Ocupacion o : ocupacionRepository.findAllByOrderByFechaHoraEntradaDesc()) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("placa", o.getVehiculo().getPlaca());
            fila.put("tipo", o.getVehiculo().getTipo().name());
            fila.put("propietario", o.getVehiculo().getPropietario().getNombre());
            fila.put("celda", o.getCelda().getCodigo());
            fila.put("estado", o.estaActiva() ? "ACTIVA" : "FINALIZADA");
            fila.put("horaEntrada", o.getFechaHoraEntrada().toString());
            fila.put("horaSalida", o.getFechaHoraSalida() == null
                    ? null : o.getFechaHoraSalida().toString());
            fila.put("puertaEntrada", o.getPuertaEntrada());
            fila.put("puertaSalida", o.getPuertaSalida());
            historial.add(fila);
        }

        return historial;
    }

    // Quita espacios y guiones y pasa a mayúsculas: "abc 123" -> "ABC123".
    private String normalizarPlaca(String placa) {
        return placa.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    private String validarTexto(String valor, String campo, int longitudMaxima)
            throws Exception {
        if (valor == null || valor.isBlank()) {
            throw new Exception("Debes ingresar " + campo + ".");
        }

        String valorNormalizado = valor.trim();
        if (valorNormalizado.length() > longitudMaxima) {
            throw new Exception(
                    "El campo " + campo + " no puede superar "
                            + longitudMaxima + " caracteres."
            );
        }
        return valorNormalizado;
    }

    // Mostrar el reporte en la consola.
    public void generarReporte() {

        List<Celda> celdas = celdaRepository.findAll();

        long carrosTotal = celdas.stream()
                .filter(c -> c.getTipoAsignado() == TipoVehiculo.CARRO)
                .count();

        long motosTotal = celdas.stream()
                .filter(c -> c.getTipoAsignado() == TipoVehiculo.MOTO)
                .count();

        long carrosOcupados = celdas.stream()
                .filter(c ->
                        c.getTipoAsignado() == TipoVehiculo.CARRO
                                && !c.isDisponible())
                .count();

        long motosOcupadas = celdas.stream()
                .filter(c ->
                        c.getTipoAsignado() == TipoVehiculo.MOTO
                                && !c.isDisponible())
                .count();

        System.out.println("\n=== REPORTE AUTOS COLOMBIA ===");

        System.out.println(
                "Vehículos actualmente adentro: "
                        + (carrosOcupados + motosOcupadas)
        );

        System.out.println(
                "Celdas Carro -> Ocupadas: " + carrosOcupados
                        + " | Disponibles: "
                        + (carrosTotal - carrosOcupados)
        );

        System.out.println(
                "Celdas Moto -> Ocupadas: " + motosOcupadas
                        + " | Disponibles: "
                        + (motosTotal - motosOcupadas)
        );

        System.out.println("==============================\n");
    }
}