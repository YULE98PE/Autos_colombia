

package co.autoscolombia.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ocupaciones")
public class Ocupacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ocupacion")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_celda", nullable = false)
    private Celda celda;

    @Column(name = "fecha_hora_entrada", nullable = false)
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    @Column(name = "puerta_entrada", nullable = false)
    private Integer puertaEntrada;

    @Column(name = "puerta_salida")
    private Integer puertaSalida;

    protected Ocupacion() {
    }

    public Ocupacion(
            Vehiculo vehiculo,
            Celda celda,
            LocalDateTime fechaHoraEntrada,
            Integer puertaEntrada) {

        this.vehiculo = vehiculo;
        this.celda = celda;
        this.fechaHoraEntrada = fechaHoraEntrada;
        this.puertaEntrada = puertaEntrada;
    }

    public void cerrar(LocalDateTime fechaHoraSalida,
                       Integer puertaSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
        this.puertaSalida = puertaSalida;
    }

    public Long getId() {
        return id;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public Celda getCelda() {
        return celda;
    }

    public LocalDateTime getFechaHoraEntrada() {
        return fechaHoraEntrada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public Integer getPuertaEntrada() {
        return puertaEntrada;
    }

    public Integer getPuertaSalida() {
        return puertaSalida;
    }

    public boolean estaActiva() {
        return fechaHoraSalida == null;
    }
}
