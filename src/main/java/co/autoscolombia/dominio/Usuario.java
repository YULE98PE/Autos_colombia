package co.autoscolombia.dominio;

import jakarta.persistence.*;

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

    protected Usuario() {}

    public Usuario(String cedula, String nombre) {
        this.cedula = cedula;
        this.nombre = nombre;
    }

    public Long getId() { return id; }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
}