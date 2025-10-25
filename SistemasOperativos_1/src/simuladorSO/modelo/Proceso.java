/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */

// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class Proceso implements ProcesoPlanificable {
    private final PCB pcb;

    public Proceso(PCB pcb) { this.pcb = pcb; }

    // Delegación ProcesoPlanificable
    @Override public void ejecutarUnCiclo(long ciclo) { pcb.ejecutarUnCiclo(ciclo); }
    @Override public boolean debeSolicitarES(long ciclo) { return pcb.debeSolicitarES(ciclo); }
    @Override public boolean completo() { return pcb.completo(); }
    @Override public void setEstado(EstadoProceso s) { pcb.setEstado(s); }
    @Override public void reiniciarQuantum() { pcb.reiniciarQuantum(); }
    @Override public int quantumConsumido() { return pcb.quantumConsumido(); }
    @Override public void setPrioridad(int p) { pcb.setPrioridad(p); }

    // Delegación PCBVista
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

    @Override
    public String toString() {
        return pcb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Proceso other)) return false;
        return this.pid() == other.pid();
    }

    @Override
    public int hashCode() {
        return Long.hashCode(pid());
    }
}
