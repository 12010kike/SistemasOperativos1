/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.planificador;
import java.util.concurrent.Semaphore;
import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
import simuladorSO.modelo.ProcesoPlanificable;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author obelm
 */
// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class PlanificadorFCFS implements PlanificadorCortoPlazo {
    private final Cola<ProcesoPlanificable> cola = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);

    @Override public void encolar(ProcesoPlanificable p, long ciclo) {
        try { mutex.acquire(); cola.ofrecer(p); } catch (InterruptedException ignored) {}
        finally { mutex.release(); }
    }

    @Override public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        try { mutex.acquire(); return cola.sacar(); } catch (InterruptedException ignored) { return null; }
        finally { mutex.release(); }
    }

    @Override public void alVencerQuantum(ProcesoPlanificable corriendo, long ciclo) { }
    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {  }
    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { }
    @Override
    public PoliticaPlanificacion politica() { return PoliticaPlanificacion.FCFS; }

    // --- INICIO DE MODIFICACIONES PARA LA GUI ---
    // Se implementan los métodos de la interfaz PlanificadorCortoPlazo
    // para exponer el estado de las colas a la interfaz gráfica.
    // Estos métodos devuelven una COPIA de los datos para no violar el encapsulamiento.
    
    @Override
    public List<ProcesoPlanificable> getColaListos() {
        // NOTA: Debes crear un método "toList()" en tu clase ColaEnlazada que 
        // recorra tus nodos y devuelva un java.util.ArrayList con los datos.
        // Asumiendo que ese método existe:
        try {
            mutex.acquire();
            if (cola instanceof ColaEnlazada) { // Para asegurar que podemos hacer la conversión
                return ((ColaEnlazada<ProcesoPlanificable>) cola).toList();
            }
        } catch (InterruptedException ignored) {
            // En caso de error, devolver una lista vacía
        } finally {
            mutex.release();
        }
        return new ArrayList<>(); // Devuelve una lista vacía si algo falla o no es del tipo esperado
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        // Este planificador no gestiona la cola de bloqueados.
        // Por lo tanto, devuelve una lista vacía.
        return new ArrayList<>();
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        // Este planificador no gestiona la cola de terminados.
        // Por lo tanto, devuelve una lista vacía.
        return new ArrayList<>();
    }
    
    // --- FIN DE MODIFICACIONES PARA LA GUI ---
}
