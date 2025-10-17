/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.planificador;
import java.util.concurrent.Semaphore;
import simuladorSO.ed.ColaPrioridad;
import simuladorSO.ed.ColaPrincipal;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author obelm
 */
public class PlanificadorPrioridadPreemption implements PlanificadorCortoPlazo{
    private final ColaPrioridad<ProcesoPlanificable> cola =
        new ColaPrincipal<>((a,b) -> Integer.compare(a.prioridad(), b.prioridad()));
    private final Semaphore mutex = new Semaphore(1, true);

    @Override public void encolar(ProcesoPlanificable p, long ciclo) {
        try { mutex.acquire(); cola.insertar(p); } catch (InterruptedException ignored) {}
        finally { mutex.release(); }
    }

    @Override public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        try { mutex.acquire(); return cola.extraer(); } catch (InterruptedException ignored) { return null; }
        finally { mutex.release(); }
    }

    @Override public void alVencerQuantum(ProcesoPlanificable c, long ciclo) { }
    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {
        if (corriendo != null && p.prioridad() < corriendo.prioridad()) {
            System.out.println("→ Expropiación por prioridad: " + corriendo.nombre() + " < " + p.nombre());
        }
    }

    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { }
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.PRIORIDAD_PREEMPTIVA; }
}

