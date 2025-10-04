/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.planificador;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author obelm
 */
public interface Planificador {
   void encolar(ProcesoPlanificable p, long ciclo);
    ProcesoPlanificable seleccionarSiguiente(long ciclo);
    void alVencerQuantum(ProcesoPlanificable corriendo, long ciclo);
    void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo);
    void reconfigurar(Object delta); 
}
