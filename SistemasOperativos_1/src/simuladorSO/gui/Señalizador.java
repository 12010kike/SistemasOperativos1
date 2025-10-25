/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.gui;

// --- INICIO DE MODIFICACIONES PARA LA GUI ---
// Documentación: Se añaden los imports necesarios para que los métodos del contrato
// puedan recibir la información del estado del simulador.
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.metrica.InstantaneaMetricas;
import java.util.List;
// --- FIN DE MODIFICACIONES PARA LA GUI ---

/**
 * Interfaz que define el "contrato" de comunicación desde la Lógica hacia la GUI.
 * La GUI implementará esta interfaz para poder recibir señales y refrescarse.
 * @author obelm
 */
public interface Señalizador {
    
    // --- CONTRATO MODIFICADO ---
    
    /**
     * Señal para que la GUI actualice la información del panel de la CPU.
     * @param enEjecucion El proceso que se encuentra actualmente en la CPU (puede ser null).
     */
    void refrescarCPU(ProcesoPlanificable enEjecucion);

    /**
     * Señal para que la GUI actualice todas las listas de procesos.
     * @param listos La lista de procesos en estado "Listo".
     * @param bloqueados La lista de procesos en estado "Bloqueado".
     * @param terminados La lista de procesos en estado "Terminado".
     * @param listosSuspendidos La lista de procesos en estado "Listo/Suspendido".
     * @param bloqueadosSuspendidos La lista de procesos en estado "Bloqueado/Suspendido".
     */
    void refrescarColas(List<ProcesoPlanificable> listos, List<ProcesoPlanificable> bloqueados,
                        List<ProcesoPlanificable> terminados, List<ProcesoPlanificable> listosSuspendidos,
                        List<ProcesoPlanificable> bloqueadosSuspendidos);

    // Los siguientes métodos se mantienen según el diseño original de tu equipo.
    void empujarEvento(EventoSistema t, String msg, String det);
    void actualizarMetricas(long cicloActual);
    void refrescarConfig();
    default void setTextoMetricas(String texto) { /* no-op */ }
}

/*package simuladorSO.gui;
import simuladorSO.nucleo.EventoSistema; 
import simuladorSO.metrica.InstantaneaMetricas;
/**
 *
 * @author obelm
 */
/*public interface Señalizador {
    void refrescarColas(); void refrescarCPU(); 
    void refrescarConfig();
    void empujarEvento(EventoSistema t, String msg, String det);
    void actualizarMetricas(InstantaneaMetricas snap);
}*/
