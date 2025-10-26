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

    private final ColaEnlazada<ProcesoPlanificable> cola = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);
    private int quantum = 3;

    private final ArrayList<ProcesoPlanificable> listosSnapshot = new ArrayList<>();
    private final LinkedList<ProcesoPlanificable> bloqueados    = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados    = new LinkedList<>();

    private void refrescarSnapshot_NoLock() {
        listosSnapshot.clear();
        listosSnapshot.addAll(cola.toList());
    }

    public void setQuantum(int nuevo) { if (nuevo > 0) this.quantum = nuevo; }
    public int getQuantum() { return quantum; }

    @Override
    public void reconfigurar(Object delta) {
        if (delta instanceof Integer q && q > 0) setQuantum(q);
    }

    @Override
    public PoliticaPlanificacion politica() { return PoliticaPlanificacion.RR; }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            bloqueados.remove(p);
            if (!cola.contiene(p)) {
                cola.ofrecer(p);
            }
            refrescarSnapshot_NoLock();
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
                listosSnapshot.remove(x);   
                bloqueados.remove(x);       
                try { x.reiniciarQuantum(); } catch (Throwable ignore) {}
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
        if (c == null) return;
        try {
            mutex.acquire();
            try { c.reiniciarQuantum(); } catch (Throwable ignore) {}
            cola.ofrecer(c);
            refrescarSnapshot_NoLock();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) { /* no-op */ }
    @Override public void reordenarColas(long ciclo) { /* no-op */ }

    @Override public boolean debeExpropiar(ProcesoPlanificable corriendo) { return false; }
    @Override public void reencolarPorPreempcion(ProcesoPlanificable p, long ciclo) { /* no-op */ }

    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            cola.remover(p);               
            listosSnapshot.remove(p);
            if (!bloqueados.contains(p)) bloqueados.addLast(p);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override
    public List<ProcesoPlanificable> getColaListos() {
        try {
            mutex.acquire();
            return new ArrayList<>(listosSnapshot);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            mutex.release();
        }
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        try {
            mutex.acquire();
            return new ArrayList<>(bloqueados);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            mutex.release();
        }
    }

    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        try {
            mutex.acquire();
            return new ArrayList<>(terminados);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return List.of();
        } finally {
            mutex.release();
        }
    }

    @Override
    public void clear() {
        try {
            mutex.acquire();
            cola.limpiar();           
            bloqueados.clear();
            terminados.clear();
            listosSnapshot.clear();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
}
