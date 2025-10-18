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

public class PlanificadorRR implements PlanificadorCortoPlazo{
    private final Cola<ProcesoPlanificable> cola = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);
    private int quantum = 3; 

    public void setQuantum(int nuevo) { if (nuevo > 0) this.quantum = nuevo; }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        try {
            mutex.acquire();
            cola.ofrecer(p);
        } catch (InterruptedException ignored) {
        } finally {
            mutex.release();
        }
    }

    @Override
    public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        try {
            mutex.acquire();
            return cola.sacar();
        } catch (InterruptedException ignored) {
            return null;
        } finally {
            mutex.release();
        }
    }

    @Override
    public void alVencerQuantum(ProcesoPlanificable c, long ciclo) {
        if (c != null) {
            System.out.println("[RR] Quantum expiró para " + c.nombre() +
                               " (q=" + quantum + ") en ciclo " + ciclo);
            c.reiniciarQuantum();
            encolar(c, ciclo);
        }
    }

    @Override
    public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {
        
    }

    @Override
    public void reordenarColas(long ciclo) {  }

    @Override
    public void reconfigurar(Object delta) {
        if (delta instanceof Integer q) setQuantum(q);
    }

    @Override
    public PoliticaPlanificacion politica() {
        return PoliticaPlanificacion.RR;
    }
// --- INICIO DE MODIFICACIONES PARA LA GUI ---
    // Documentación: Se implementan los métodos de la interfaz PlanificadorCortoPlazo
    // para exponer el estado de las colas a la interfaz gráfica.

    @Override
    public List<ProcesoPlanificable> getColaListos() {
        // Se utiliza el método toList() de la ColaEnlazada para devolver una copia
        // de solo lectura de los datos en un formato estándar para la GUI.
        try {
            mutex.acquire();
            if (cola instanceof ColaEnlazada) {
                return ((ColaEnlazada<ProcesoPlanificable>) cola).toList();
            }
        } catch (InterruptedException ignored) {
            // En caso de error, devolver una lista vacía
        } finally {
            mutex.release();
        }
        return new ArrayList<>();
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        // Este planificador no gestiona la cola de bloqueados.
        return new ArrayList<>();
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        // Este planificador no gestiona la cola de terminados.
        return new ArrayList<>();
    }
    // --- FIN DE MODIFICACIONES PARA LA GUI ---
}
