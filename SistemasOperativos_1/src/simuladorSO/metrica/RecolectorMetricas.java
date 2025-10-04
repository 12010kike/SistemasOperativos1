/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.metrica;
import simuladorSO.modelo.PCBVista;
/**
 *
 * @author eabdf
 */
public interface RecolectorMetricas {
    void alLlegar(PCBVista pcb, long ciclo); 
    void alDespachar(PCBVista pcb, long ciclo);
    void alBloquear(PCBVista pcb, long ciclo); 
    void alDesbloquear(PCBVista pcb, long ciclo);
    void alCompletar(PCBVista pcb, long ciclo); 
    void alTick(boolean cpuOcupada, long ciclo);
    InstantaneaMetricas snapshot();
}
