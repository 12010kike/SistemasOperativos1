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

public class PlanificadorPrioridadPreemption implements PlanificadorCortoPlazo {

    private final ColaPrioridad<ProcesoPlanificable> cola =
        new ColaPrincipal<>((a, b) -> Integer.compare(a.prioridad(), b.prioridad()));
    private final Semaphore mutex = new Semaphore(1, true);

    // colas para la GUI 
    private final ArrayList<ProcesoPlanificable> listosSnapshot = new ArrayList<>();
    private final LinkedList<ProcesoPlanificable> bloqueados    = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados    = new LinkedList<>();

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            // si vuelve de E/S, ya no está bloqueado
            bloqueados.remove(p);
            cola.insertar(p);
            // snapshot para la GUI
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
                // sale de listos ⇒ quítalo del snapshot
                listosSnapshot.remove(x);
                // por si quedaba marcado como bloqueado
                bloqueados.remove(x);
            }
            return x;
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    @Override public void alVencerQuantum(ProcesoPlanificable c, long ciclo) { /* RR no aplica; noop */ }

    @Override
    public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {
        // aviso (solo log); la expropiación real la hace tu Despachador/CPU
        if (corriendo != null && p.prioridad() < corriendo.prioridad()) {
            System.out.println("→ Expropiación por prioridad: " + p.nombre() + " > " + corriendo.nombre());
        }
    }

    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { }
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.PRIORIDAD_PREEMPTIVA; }

    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            // evita duplicados en bloqueados y quítalo del snapshot de listos
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
            return new ArrayList<>(listosSnapshot);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } finally {
            mutex.release();
        }
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        return bloqueados;    // lista viva (la GUI solo lee)
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        return terminados;    // llénala donde corresponda si marcas terminados aquí
    }
}
