package co.autoscolombia.principal;

import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Usuario;
import co.autoscolombia.dominio.Vehiculo;
import co.autoscolombia.servicios.ServicioParqueadero;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Main implements CommandLineRunner {

    private final ServicioParqueadero parqueadero;

    public Main(ServicioParqueadero parqueadero) {
        this.parqueadero = parqueadero;
    }

    @Override
    public void run(String... args) {
        Usuario cliente1 = new Usuario("1001", "Juan Perez");
        Vehiculo carro1 = new Vehiculo("GHP890", "Mazda", "Rojo", TipoVehiculo.CARRO, cliente1);

        try {
            System.out.println("--- PRUEBA 1: Ingreso normal ---");
            parqueadero.registrarEntrada(carro1, 1);

            System.out.println("\n--- PRUEBA 2: Intentar ingresar un carro que YA esta adentro ---");
            parqueadero.registrarEntrada(carro1, 2);

        } catch (Exception e) {
            System.out.println("ERROR CAPTURADO: " + e.getMessage());
        }

        try {
            System.out.println("\n--- PRUEBA 3: Intentar sacar un vehiculo que no existe ---");
            parqueadero.registrarSalida("XXX000");

        } catch (Exception e) {
            System.out.println("ERROR CAPTURADO: " + e.getMessage());
        }

        parqueadero.generarReporte();
    }
}