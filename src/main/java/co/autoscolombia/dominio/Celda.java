package co.autoscolombia.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "celdas")
public class Celda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_celda")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false)
    private TipoVehiculo tipoAsignado;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;

    @OneToOne
    @JoinColumn(name = "vehiculo_id", nullable = true)
    private Vehiculo vehiculo;

    protected Celda() {}

    public Celda(String codigo, TipoVehiculo tipoAsignado) {
        this.codigo = codigo;
        this.tipoAsignado = tipoAsignado;
        this.disponible = true;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoVehiculo getTipoAsignado() { return tipoAsignado; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
}