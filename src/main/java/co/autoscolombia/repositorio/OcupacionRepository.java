package co.autoscolombia.repositorio;

import co.autoscolombia.dominio.Ocupacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OcupacionRepository extends JpaRepository<Ocupacion, Long> {

    Optional<Ocupacion> findByVehiculoPlacaAndFechaHoraSalidaIsNull(String placa);

    List<Ocupacion> findByFechaHoraSalidaIsNullOrderByFechaHoraEntradaDesc();

    Optional<Ocupacion> findByCeldaCodigoAndFechaHoraSalidaIsNull(String codigo);

    // Historial completo (HU-07): activas y finalizadas, la más reciente primero.
    List<Ocupacion> findAllByOrderByFechaHoraEntradaDesc();
}