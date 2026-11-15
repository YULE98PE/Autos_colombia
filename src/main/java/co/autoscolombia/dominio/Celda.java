package co.autoscolombia.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

    @Column(name = "fuera_de_servicio", nullable = false)
    private boolean fueraDeServicio = false;

    protected Celda() {}

    public Celda(String codigo, TipoVehiculo tipoAsignado) {
        this.codigo = codigo;
        this.tipoAsignado = tipoAsignado;
        this.disponible = true;
        this.fueraDeServicio = false;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public TipoVehiculo getTipoAsignado() { return tipoAsignado; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public Vehiculo getVehiculo() { return vehiculo; }
    public void setVehiculo(Vehiculo vehiculo) { this.vehiculo = vehiculo; }
    public boolean isFueraDeServicio() { return fueraDeServicio; }
    public void setFueraDeServicio(boolean fueraDeServicio) { this.fueraDeServicio = fueraDeServicio; }

    // Estado que ve el usuario (HU-13): se calcula, no se guarda.
    public String getEstado() {
        if (fueraDeServicio) return "FUERA_DE_SERVICIO";
        return disponible ? "DISPONIBLE" : "OCUPADA";
    }
}