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

public class PlanificadorSRTF implements PlanificadorCortoPlazo {

    private final ColaPrioridad<ProcesoPlanificable> cola =
        new ColaPrincipal<>((a, b) -> Integer.compare(a.instruccionesRestantes(), b.instruccionesRestantes()));
    private final Semaphore mutex = new Semaphore(1, true);

    private final ArrayList<ProcesoPlanificable> listosSnapshot = new ArrayList<>();
    private final LinkedList<ProcesoPlanificable> bloqueados    = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados    = new LinkedList<>();

    private void refreshSnapshot_NoLock() {
        ArrayList<ProcesoPlanificable> tmp = new ArrayList<>();
        while (true) {
            ProcesoPlanificable x = cola.extraer();
            if (x == null) break;
            tmp.add(x);
        }
        for (ProcesoPlanificable x : tmp) cola.insertar(x);
        listosSnapshot.clear();
        listosSnapshot.addAll(tmp);
    }

    private void removerDeCola_NoLock(ProcesoPlanificable objetivo) {
        if (objetivo == null) return;
        ArrayList<ProcesoPlanificable> tmp = new ArrayList<>();
        while (true) {
            ProcesoPlanificable x = cola.extraer();
            if (x == null) break;
            if (!x.equals(objetivo)) tmp.add(x);
        }
        for (ProcesoPlanificable x : tmp) cola.insertar(x);
    }

    private void vaciarCola_NoLock() {
        while (cola.extraer() != null) { /* drena */ }
    }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();

            bloqueados.remove(p);
            removerDeCola_NoLock(p);
            cola.insertar(p);

            if (!listosSnapshot.contains(p)) listosSnapshot.add(p);
            refreshSnapshot_NoLock();

        } catch (InterruptedException ie) {
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
                listosSnapshot.remove(x);   
                bloqueados.remove(x);       
            }
            return x;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            mutex.release();
        }
    }

    @Override public void alVencerQuantum(ProcesoPlanificable c, long ciclo) { /* SRTF no usa quantum */ }
    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable c, long ciclo) { /* opcional/log */ }
    @Override public void reordenarColas(long ciclo) { /* no-op (lo hace la prioridad) */ }
    @Override public void reconfigurar(Object delta) { /* no-op */ }
    @Override public PoliticaPlanificacion politica() { return PoliticaPlanificacion.SRTF; }

    @Override
    public boolean debeExpropiar(ProcesoPlanificable corriendo) {
        if (corriendo == null) return false;
        try {
            mutex.acquire();
            refreshSnapshot_NoLock();
            if (listosSnapshot.isEmpty()) return false;
            ProcesoPlanificable mejor = listosSnapshot.get(0);
            return mejor.instruccionesRestantes() < corriendo.instruccionesRestantes();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }

    @Override
    public void reencolarPorPreempcion(ProcesoPlanificable p, long ciclo) {
        encolar(p, ciclo);
    }

    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            removerDeCola_NoLock(p);  
            listosSnapshot.remove(p);
            if (!bloqueados.contains(p)) bloqueados.addLast(p);
            refreshSnapshot_NoLock();
        } catch (InterruptedException ie) {
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
        } catch (InterruptedException ie) {
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
        } catch (InterruptedException ie) {
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
        } catch (InterruptedException ie) {
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
            vaciarCola_NoLock();
            bloqueados.clear();
            terminados.clear();
            listosSnapshot.clear();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
}
