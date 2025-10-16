/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */
public class Proceso implements ProcesoPlanificable {
    private final PCB pcb;
    private int quantumConsumido = 0;
    private int ejecutadasDesdeUltimaES = 0;

    public Proceso(PCB pcb) {
        this.pcb = pcb;
    }

    @Override
    public void ejecutarUnCiclo(long ciclo) {
        if (pcb.instruccionesRestantes() <= 0) return;
        pcb.avanzarInstruccion();
        quantumConsumido++;
        ejecutadasDesdeUltimaES++;
        System.out.println("Ciclo " + ciclo + " -> " + nombre()
                + " ejecuta. PC=" + pc() + " MAR=" + mar()
                + " Rest=" + instruccionesRestantes());
    }

    @Override
    public boolean debeSolicitarES(long ciclo) {
        if (pcb.tipo() == TipoProceso.IO_BOUND && pcb.ioCadaK() > 0) {
            if (ejecutadasDesdeUltimaES >= pcb.ioCadaK()) {
                ejecutadasDesdeUltimaES = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean completo() { return pcb.instruccionesRestantes() <= 0; }

    @Override
    public void setEstado(EstadoProceso nuevo) { pcb.setEstado(nuevo); }

    @Override
    public void reiniciarQuantum() { quantumConsumido = 0; }

    @Override
    public int quantumConsumido() { return quantumConsumido; }

    @Override
    public void setPrioridad(int nueva) { pcb.setPrioridad(nueva); }

    @Override public long pid() { return pcb.pid(); }
    @Override public String nombre() { return pcb.nombre(); }
    @Override public EstadoProceso estado() { return pcb.estado(); }
    @Override public int pc() { return pcb.pc(); }
    @Override public int mar() { return pcb.mar(); }
    @Override public int totalInstrucciones() { return pcb.totalInstrucciones(); }
    @Override public int instruccionesRestantes() { return pcb.instruccionesRestantes(); }
    @Override public TipoProceso tipo() { return pcb.tipo(); }
    @Override public int ioCadaK() { return pcb.ioCadaK(); }
    @Override public int ioDuraM() { return pcb.ioDuraM(); }
    @Override public int prioridad() { return pcb.prioridad(); }
    @Override public long cicloLlegada() { return pcb.cicloLlegada(); }
}
