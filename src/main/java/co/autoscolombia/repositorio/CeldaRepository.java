package co.autoscolombia.repositorio;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.TipoVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CeldaRepository extends JpaRepository<Celda, Long> {
    List<Celda> findByTipoAsignadoAndDisponibleTrue(TipoVehiculo tipo);
}