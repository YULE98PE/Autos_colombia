package co.autoscolombia.dominio;

public class Celda {
    private int numero;
    private TipoVehiculo tipoAsignado;
    private boolean disponible;

    public Celda(int numero, TipoVehiculo tipoAsignado) {
        this.numero = numero;
        this.tipoAsignado = tipoAsignado;
        this.disponible = true; 
    }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public int getNumero() { return numero; }
}