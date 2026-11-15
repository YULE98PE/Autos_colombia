package co.autoscolombia.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.autoscolombia.dominio.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Iteración 1: usuario por cédula (ingreso de vehículos).
    Optional<Usuario> findByCedula(String cedula);

    List<Usuario> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    boolean existsByCedulaAndIdNot(String cedula, Long id);
}