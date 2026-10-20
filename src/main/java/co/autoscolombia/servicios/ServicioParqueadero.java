package co.autoscolombia.servicios;

import co.autoscolombia.dominio.Celda;
import co.autoscolombia.dominio.TipoVehiculo;
import co.autoscolombia.dominio.Vehiculo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicioParqueadero {
    
    private List<Celda> celdasCarro;
    private List<Celda> celdasMoto;
    private Map<String, Celda> vehiculosEnParqueadero;

    public ServicioParqueadero() {
        this.celdasCarro = new ArrayList<>();
        this.celdasMoto = new ArrayList<>();
        this.vehiculosEnParqueadero = new HashMap<>();
        inicializarCeldas();
    }

    private void inicializarCeldas() {
        for (int i = 1; i <= 70; i++) {
            celdasCarro.add(new Celda(i, TipoVehiculo.CARRO));
        }
        for (int i = 71; i <= 80; i++) {
            celdasMoto.add(new Celda(i, TipoVehiculo.MOTO));
        }
    }

    public synchronized Celda registrarEntrada(Vehiculo vehiculo, int puertaIngreso) throws Exception {
        if (vehiculosEnParqueadero.containsKey(vehiculo.getPlaca())) {
            throw new Exception("El vehículo " + vehiculo.getPlaca() + " ya está adentro.");
        }

        List<Celda> celdasDisponibles = (vehiculo.getTipo() == TipoVehiculo.CARRO) ? celdasCarro : celdasMoto;

        for (Celda celda : celdasDisponibles) {
            if (celda.isDisponible()) {
                celda.setDisponible(false);
                vehiculosEnParqueadero.put(vehiculo.getPlaca(), celda);
                System.out.println("✅ [PUERTA " + puertaIngreso + "] Vehículo " + vehiculo.getPlaca() + " ingresa. Celda asignada: " + celda.getNumero());
                return celda;
            }
        }
        throw new Exception("No hay celdas disponibles para " + vehiculo.getTipo());
    }

    public synchronized void registrarSalida(String placa) throws Exception {
        if (!vehiculosEnParqueadero.containsKey(placa)) {
            throw new Exception("El vehículo " + placa + " no está registrado en el parqueadero.");
        }

        Celda celdaLiberada = vehiculosEnParqueadero.get(placa);
        celdaLiberada.setDisponible(true);
        vehiculosEnParqueadero.remove(placa);
        
        System.out.println("🚪 [SALIDA] Vehículo " + placa + " sale. La celda " + celdaLiberada.getNumero() + " queda libre.");
    }

    public void generarReporte() {
        long carrosOcupados = celdasCarro.stream().filter(c -> !c.isDisponible()).count();
        long motosOcupadas = celdasMoto.stream().filter(c -> !c.isDisponible()).count();
        
        System.out.println("\n=== REPORTE AUTOS COLOMBIA ===");
        System.out.println("Vehículos actualmente adentro: " + vehiculosEnParqueadero.size());
        System.out.println("Celdas Carro -> Ocupadas: " + carrosOcupados + " | Disponibles: " + (70 - carrosOcupados));
        System.out.println("Celdas Moto -> Ocupadas: " + motosOcupadas + " | Disponibles: " + (10 - motosOcupadas));
        System.out.println("==============================\n");
    }
}