package co.autoscolombia.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.autoscolombia.dominio.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    // Iteración 1: vehículo por placa (ingreso).
    Optional<Vehiculo> findByPlaca(String placa);

    List<Vehiculo> findByPropietarioId(Long idUsuario);
}