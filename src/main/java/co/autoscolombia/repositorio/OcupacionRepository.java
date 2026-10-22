package co.autoscolombia.repositorio;

import co.autoscolombia.dominio.Ocupacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OcupacionRepository extends JpaRepository<Ocupacion, Long> {
    Optional<Ocupacion> findByVehiculoPlacaAndFechaHoraSalidaIsNull(String placa);
    Optional<Ocupacion> findByCeldaCodigoAndFechaHoraSalidaIsNull(String codigo);
}