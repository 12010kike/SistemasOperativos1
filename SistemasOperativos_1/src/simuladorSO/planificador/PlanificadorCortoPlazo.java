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

import java.util.List; // NUEVO: Importamos la List de Java
import simuladorSO.modelo.ProcesoPlanificable; // Necesitamos importar esto si no está ya

public interface PlanificadorCortoPlazo extends Planificador {
    void reordenarColas(long ciclo);
    PoliticaPlanificacion politica();

    // --- INICIO DE MODIFICACIONES PARA LA GUI ---
    // Estos métodos permiten a la interfaz gráfica consultar el estado de las colas.
    // Devuelven una copia en un formato estándar para no exponer la estructura interna.
    
    List<ProcesoPlanificable> getColaListos();
    List<ProcesoPlanificable> getColaBloqueados();
    List<ProcesoPlanificable> getColaTerminados();
    void registrarProcesoBloqueado(ProcesoPlanificable p);
        // Añade aquí los getters para las colas de suspendidos si tu planificador las maneja
    
    // --- FIN DE MODIFICACIONES PARA LA GUI ---
}
