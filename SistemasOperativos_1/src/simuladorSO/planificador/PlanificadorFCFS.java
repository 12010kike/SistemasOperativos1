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
    private final ColaEnlazada<ProcesoPlanificable> colaListos = new ColaEnlazada<>();
    private final ColaEnlazada<ProcesoPlanificable> colaBloqueados = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);

    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        try {
            mutex.acquire();
            // Un proceso en la CPU ya no está en la cola de listos.
            // Para bloquearlo, simplemente lo añadimos a la cola de bloqueados.
            if (!colaBloqueados.contiene(p)) {
                 colaBloqueados.ofrecer(p);
            }
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
    }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        try {
            mutex.acquire();
            colaBloqueados.remover(p); // Ahora esto funcionará gracias a equals()
            if (!colaListos.contiene(p)) {
                colaListos.ofrecer(p);
            }
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
    }



    @Override
    public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        ProcesoPlanificable p = null;
        try {
            mutex.acquire();
            // SACAR el proceso de la cola es el comportamiento correcto.
            p = colaListos.sacar();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return p;
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
        List<ProcesoPlanificable> lista = new ArrayList<>();
        try {
            mutex.acquire();
            lista = colaListos.toList();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return lista;
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        List<ProcesoPlanificable> lista = new ArrayList<>();
        try {
            mutex.acquire();
            lista = colaBloqueados.toList();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return lista;
    }
    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        // Este planificador no gestiona la cola de terminados.
        // Por lo tanto, devuelve una lista vacía.
        return new ArrayList<>();
    }
    
    // --- FIN DE MODIFICACIONES PARA LA GUI ---
}
