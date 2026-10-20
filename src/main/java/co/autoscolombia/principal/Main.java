package co.autoscolombia.principal;

import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Usuario;
import co.autoscolombia.dominio.Vehiculo;
import co.autoscolombia.servicios.ServicioParqueadero;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando Sistema Autos Colombia...\n");
        ServicioParqueadero parqueadero = new ServicioParqueadero();

        // Creamos los clientes y sus vehículos que pidió el cliente en los requerimientos
        Usuario cliente1 = new Usuario("1001", "Juan Perez");
        Vehiculo carro1 = new Vehiculo("GHP890", "Mazda", "Rojo", TipoVehiculo.CARRO, cliente1);

        Usuario cliente2 = new Usuario("1002", "Maria Gomez");
        Vehiculo carro2 = new Vehiculo("DDY765", "Renault", "Gris", TipoVehiculo.CARRO, cliente2);

        Usuario cliente3 = new Usuario("1003", "Carlos Ruiz");
        Vehiculo moto1 = new Vehiculo("ZXC12D", "Yamaha", "Negra", TipoVehiculo.MOTO, cliente3);

        try {
            // Simulamos llegadas
            parqueadero.registrarEntrada(carro1, 1); // Entra por Puerta 1
            parqueadero.registrarEntrada(carro2, 2); // Entra por Puerta 2
            parqueadero.registrarEntrada(moto1, 1);  // Entra por Puerta 1

            // Vemos cómo quedó el parqueadero
            parqueadero.generarReporte();

            // Simulamos que el primer carro sale y libera su celda
            parqueadero.registrarSalida("GHP890");

            // Volvemos a ver el parqueadero para confirmar que se liberó el cupo
            parqueadero.generarReporte();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}