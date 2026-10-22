package co.autoscolombia.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "vehiculos")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long id;

    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String placa;

    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    @Column(name = "color", nullable = false, length = 40)
    private String color;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario propietario;

    // JPA exige un constructor vacío
    protected Vehiculo() {}

    public Vehiculo(String placa, String marca, String color, TipoVehiculo tipo, Usuario propietario) {
        this.placa = placa;
        this.marca = marca;
        this.color = color;
        this.tipo = tipo;
        this.propietario = propietario;
    }

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getColor() { return color; }
    public TipoVehiculo getTipo() { return tipo; }
    public Usuario getPropietario() { return propietario; }
}