package co.autoscolombia.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "documento", nullable = false, unique = true, length = 30)
    private String cedula;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    // HU-12: baja lógica. true = activo, false = inactivo (el historial se conserva).
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    protected Usuario() {}

    public Usuario(String cedula, String nombre) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.activo = true;
    }

    public Long getId() { return id; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}