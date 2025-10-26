/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.planificador;

/**
 *
 * @author obelm
 */

// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

import java.util.List; 
import simuladorSO.modelo.ProcesoPlanificable; 

public interface PlanificadorCortoPlazo extends Planificador {
    void reordenarColas(long ciclo);
    PoliticaPlanificacion politica();
    default void clear() { /* por defecto no hace nada */ }
    
    List<ProcesoPlanificable> getColaListos();
    List<ProcesoPlanificable> getColaBloqueados();
    List<ProcesoPlanificable> getColaTerminados();
    void registrarProcesoBloqueado(ProcesoPlanificable p);
    default boolean debeExpropiar(simuladorSO.modelo.ProcesoPlanificable corriendo) { 
        return false; 
    }

    default void reencolarPorPreempcion(simuladorSO.modelo.ProcesoPlanificable p, long ciclo) {
        encolar(p, ciclo); 
    }
}
