package simuladorSO.planificador;

import java.util.concurrent.Semaphore;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
import simuladorSO.modelo.ProcesoPlanificable;

/**
 *
 * @author obelm
 */
// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class PlanificadorRR implements PlanificadorCortoPlazo {

    private final Cola<ProcesoPlanificable> cola = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);
    private int quantum = 3;

    // --- Colas de apoyo para la GUI ---
    private final ArrayList<ProcesoPlanificable> listosSnapshot = new ArrayList<>();
    private final LinkedList<ProcesoPlanificable> bloqueados    = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados    = new LinkedList<>();

    public void setQuantum(int nuevo) { if (nuevo > 0) this.quantum = nuevo; }
    public int getQuantum() { return quantum; }


    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            // si vuelve de E/S, ya no está bloqueado
            bloqueados.remove(p);
            cola.ofrecer(p);
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
            ProcesoPlanificable x = cola.sacar();
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

    @Override
    public void alVencerQuantum(ProcesoPlanificable c, long ciclo) {
        if (c != null) {
            System.out.println("[RR] Quantum expiró para " + c.nombre() +
                               " (q=" + quantum + ") en ciclo " + ciclo);
            c.reiniciarQuantum();
            encolar(c, ciclo); // vuelve a listos
        }
    }

    @Override
    public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {
        // RR no expropia por llegada; noop
    }

    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { if (delta instanceof Integer q) setQuantum(q); }
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.RR; }

    // --- Requerido por PlanificadorCortoPlazo: registrar bloqueados ---
    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            listosSnapshot.remove(p);      // ya no está en listos
            bloqueados.remove(p);          // evita duplicados
            bloqueados.addLast(p);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    // --- Getters para la GUI ---
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
        return bloqueados;   // la GUI solo lee
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        return terminados;   // llena donde corresponda si marcas terminados aquí
    }
}
