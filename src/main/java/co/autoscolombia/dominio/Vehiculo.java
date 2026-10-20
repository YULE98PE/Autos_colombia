package co.autoscolombia.dominio;

public class Vehiculo {
    private String placa;
    private String marca;
    private String color;
    private TipoVehiculo tipo;
    private Usuario propietario; 

    public Vehiculo(String placa, String marca, String color, TipoVehiculo tipo, Usuario propietario) {
        this.placa = placa;
        this.marca = marca;
        this.color = color;
        this.tipo = tipo;
        this.propietario = propietario;
    }

    public String getPlaca() { return placa; }
    public TipoVehiculo getTipo() { return tipo; }
}