/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorSO;

import simuladorSO.nucleo.*;
import simuladorSO.modelo.*;


public class Main {

    // Proceso mínimo temporal SOLO para probar
    static class ProcesoDummy implements ProcesoPlanificable {
        private final long pid; private final String nombre;
        private final int total; private int pc=0, mar=0, rest;
        ProcesoDummy(long pid, String nombre, int total) {
            this.pid = pid; this.nombre = nombre; this.total = total; this.rest = total;
        }
        @Override public void ejecutarUnCiclo(long ciclo) {
            if (rest > 0) {
                pc++; mar++; rest--;
                System.out.println("Ciclo " + ciclo + " → " + nombre + " ejecuta. PC=" + pc + " MAR=" + mar + " Rest=" + rest);
            }
        }
        @Override public boolean debeSolicitarES(long ciclo) { return false; }
        @Override public boolean completo() { return rest <= 0; }
        @Override public void setEstado(EstadoProceso nuevo) { /* no-op */ }
        @Override public void reiniciarQuantum() { /* no-op */ }
        @Override public int quantumConsumido() { return 0; }
        @Override public void setPrioridad(int nueva) { /* no-op */ }
        @Override public long pid() { return pid; }
        @Override public String nombre() { return nombre; }
        @Override public EstadoProceso estado() { return EstadoProceso.RUNNING; }
        @Override public int pc() { return pc; }
        @Override public int mar() { return mar; }
        @Override public int totalInstrucciones() { return total; }
        @Override public int instruccionesRestantes() { return rest; }
        @Override public TipoProceso tipo() { return TipoProceso.CPU_BOUND; }
        @Override public int ioCadaK() { return 0; }
        @Override public int ioDuraM() { return 0; }
        @Override public int prioridad() { return 0; }
        @Override public long cicloLlegada() { return 0; }
    }

    public static void main(String[] args) {
        BusEventos bus = new BusEventosSimple();
        bus.suscribir(e -> System.out.println("[" + e.ciclo() + "] " + e.tipo() + " :: " + e.mensaje() + " {" + e.detalles() + "}"));

        Reloj reloj = new ImplementacionReloj();
        ImplementacionCPU cpu = new ImplementacionCPU(bus);
        reloj.suscribir(cpu); // CPU avanza 1 instrucción por tick

        Despachador desp = new ImplementacionDespachador(cpu, bus);

        // Proceso de prueba con 6 instrucciones
        ProcesoPlanificable p1 = new ProcesoDummy(1, "P1", 6);

        // Despachar a CPU y arrancar reloj
        desp.despacharACPU(p1, 0);
        reloj.setDuracionTickMs(300); // tick de 300ms (ajustable)
        reloj.iniciar();

        // Dejar correr ~2.5s
        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
        reloj.pausar();

        System.out.println("Fin demo PASO 1. Ciclo=" + reloj.cicloActual());
    }
}

