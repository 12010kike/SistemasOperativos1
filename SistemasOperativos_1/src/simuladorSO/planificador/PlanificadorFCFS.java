/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.planificador;
import java.util.concurrent.Semaphore;
import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author obelm
 */
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
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.FCFS; }
}

