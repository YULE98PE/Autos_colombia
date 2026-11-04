package co.autoscolombia.repositorio;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.TipoVehiculo;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface CeldaRepository extends JpaRepository<Celda, Long> {

    List<Celda> findByTipoAsignadoAndDisponibleTrue(TipoVehiculo tipoAsignado);

    // Elige la primera celda libre y BLOQUEA esa fila en MySQL (SELECT ... FOR UPDATE).
    // Si dos operarios piden celda al mismo tiempo, el segundo espera a que el
    // primero termine y recibe la siguiente celda libre. Debe llamarse dentro de
    // un método @Transactional.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Celda> findFirstByTipoAsignadoAndDisponibleTrueOrderByCodigoAsc(
            TipoVehiculo tipoAsignado);
}