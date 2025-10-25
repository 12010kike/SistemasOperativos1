package simuladorSO.planificador;

import java.util.concurrent.Semaphore;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import simuladorSO.ed.ColaPrioridad;
import simuladorSO.ed.ColaPrincipal;
import simuladorSO.modelo.ProcesoPlanificable;

/**
 *
 * @author obelm
 */
// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class PlanificadorSJF implements PlanificadorCortoPlazo {

    private final ColaPrioridad<ProcesoPlanificable> cola =
        new ColaPrincipal<>((a, b) -> Integer.compare(a.instruccionesRestantes(), b.instruccionesRestantes()));
    private final Semaphore mutex = new Semaphore(1, true);

    // Colas auxiliares para la GUI 
    private final ArrayList<ProcesoPlanificable> listosSnapshot = new ArrayList<>();
    private final LinkedList<ProcesoPlanificable> bloqueados    = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados    = new LinkedList<>();

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            // si vuelve de E/S, sácalo de bloqueados
            bloqueados.remove(p);
            cola.insertar(p);
            // mantener snapshot para la GUI
            listosSnapshot.add(p);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override
    public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        try {
            mutex.acquire();
            ProcesoPlanificable x = cola.extraer();
            if (x != null) {
                listosSnapshot.remove(x); // sale de listos
                bloqueados.remove(x);     // por si quedó marcado
            }
            return x;
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    @Override public void alVencerQuantum(ProcesoPlanificable c, long ciclo) { /* SJF no usa quantum */ }
    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable c, long ciclo) { }
    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { }
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.SJF; }

    // requerido por PlanificadorCortoPlazo 
    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            listosSnapshot.remove(p);
            bloqueados.remove(p);
            bloqueados.addLast(p);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    // getters para la GUI 
    @Override
    public List<ProcesoPlanificable> getColaListos() {
        try {
            mutex.acquire();
            return new ArrayList<>(listosSnapshot); // vista estable
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } finally {
            mutex.release();
        }
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        return bloqueados;
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        return terminados; // llena donde corresponda si marcas terminados aquí
    }
}
