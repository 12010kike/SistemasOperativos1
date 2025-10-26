/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.gui;

import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.metrica.InstantaneaMetricas;
import java.util.List;

/**
 * @author obelm
 */
public interface Señalizador {
    
    void refrescarCPU(ProcesoPlanificable enEjecucion);
    void refrescarColas(List<ProcesoPlanificable> listos, List<ProcesoPlanificable> bloqueados,
                        List<ProcesoPlanificable> terminados, List<ProcesoPlanificable> listosSuspendidos,
                        List<ProcesoPlanificable> bloqueadosSuspendidos);

    void empujarEvento(EventoSistema t, String msg, String det);
    void actualizarMetricas(long cicloActual);
    void refrescarConfig();
    void plotUtil(double valor01);
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
