package co.autoscolombia.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.autoscolombia.dominio.Ocupacion;

public interface OcupacionRepository extends JpaRepository<Ocupacion, Long> {

    Optional<Ocupacion> findByVehiculoPlacaAndFechaHoraSalidaIsNull(String placa);

    List<Ocupacion> findByFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc();

    Optional<Ocupacion> findByCeldaCodigoAndFechaHoraSalidaIsNull(String codigo);

    // Historial completo (HU-07): activas y finalizadas, la más reciente primero.
    List<Ocupacion> findAllByOrderByFechaHoraEntradaDesc();

    // HU-12: ¿el usuario tiene algún vehículo adentro del parqueadero?
    boolean existsByVehiculoPropietarioIdAndFechaHoraSalidaIsNull(Long idUsuario);
}