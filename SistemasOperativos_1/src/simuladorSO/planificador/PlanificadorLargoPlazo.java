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
public interface PlanificadorLargoPlazo {
     void admitir(ProcesoPlanificable p, long ciclo);
}
