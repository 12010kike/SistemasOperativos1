/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.modelo.ProcesoPlanificable ;

/**
 *
 * @author eabdf
 */
public interface Despachador {
    void despacharACPU(ProcesoPlanificable siguiente, long ciclo);
    ProcesoPlanificable expropiarActual(long ciclo);
}
