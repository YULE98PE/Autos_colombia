package co.autoscolombia.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.TipoVehiculo;
import jakarta.persistence.LockModeType;

public interface CeldaRepository extends JpaRepository<Celda, Long> {

    List<Celda> findByTipoAsignadoAndDisponibleTrue(TipoVehiculo tipoAsignado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Celda> findFirstByTipoAsignadoAndDisponibleTrueOrderByCodigoAsc(
            TipoVehiculo tipoAsignado);

   
    List<Celda> findAllByOrderByCodigoAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Celda> findByCodigo(String codigo);
}