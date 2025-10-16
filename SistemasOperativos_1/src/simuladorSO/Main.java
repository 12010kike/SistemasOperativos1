/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorSO;

import simuladorSO.nucleo.*;
import simuladorSO.modelo.*;


public class Main {

    public static void main(String[] args) {
        BusEventos bus = new BusEventosSimple();
        bus.suscribir(e -> System.out.println(
            "[" + e.ciclo() + "] " + e.tipo() + " :: " + e.mensaje() + " {" + e.detalles() + "}"
        ));

        Reloj reloj = new ImplementacionReloj();              
        ImplementacionCPU cpu = new ImplementacionCPU(bus);             
        reloj.suscribir(cpu);                       

        Despachador desp = new ImplementacionDespachador(cpu, bus);

        GeneradorProcesos gen = new GeneradorProcesos();

        Proceso p1 = gen.crearManual("P1", 8, TipoProceso.CPU_BOUND, 0, 0, 3, 0);

        desp.despacharACPU(p1, 0);

        reloj.setDuracionTickMs(400); 
        reloj.iniciar();
        
        try {
            Thread.sleep(3500);
        } catch (InterruptedException ignored) {}

        reloj.pausar();
        System.out.println("Fin demo PASO 2. Ciclo = " + reloj.cicloActual());
    }
}

