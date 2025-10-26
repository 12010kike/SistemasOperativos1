/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

public class PlanificadorMLFQ implements PlanificadorCortoPlazo {

    private final LinkedList<ProcesoPlanificable> bloqueados = new LinkedList<>();
    private final LinkedList<ProcesoPlanificable> terminados = new LinkedList<>();

    private static final class Entry {
        final ProcesoPlanificable p;
        long tArribo;
        int  nivel;
        Entry(ProcesoPlanificable p, long t, int n) { this.p = p; this.tArribo = t; this.nivel = n; }
    }

    private final Cola<Entry>[] niveles; 
    private final int[] quantums;        
    private final long envejecimiento;   
    private final Semaphore mutex = new Semaphore(1, true);

    @SuppressWarnings("unchecked")
    public PlanificadorMLFQ(int nivelesCount, int[] quantumsPorNivel, long envejecimiento) {
        if (nivelesCount < 2) nivelesCount = 3;
        this.niveles = new Cola[nivelesCount];
        for (int i = 0; i < nivelesCount; i++) this.niveles[i] = new ColaEnlazada<>();

        if (quantumsPorNivel == null || quantumsPorNivel.length != nivelesCount) {
            this.quantums = new int[]{2, 4, 8};
        } else {
            this.quantums = quantumsPorNivel.clone();
        }
        this.envejecimiento = (envejecimiento <= 0) ? 20 : envejecimiento;
    }
    
    public int getQuantumNivel(int nivel) {
    return quantums[clampNivel(nivel)];
}


    private void removerDeTodosLosNiveles_NoLock(ProcesoPlanificable p) {
        if (p == null) return;
        for (int lvl = 0; lvl < niveles.length; lvl++) {
            int n = niveles[lvl].tamano();
            for (int i = 0; i < n; i++) {
                Entry e = niveles[lvl].sacar();
                if (e == null) break;
                if (!e.p.equals(p)) {
                    niveles[lvl].ofrecer(e);
                }
            }
        }
    }

    private void aplicarEnvejecimiento_NoLock(long ciclo) {
        for (int lvl = 1; lvl < niveles.length; lvl++) {
            int n = niveles[lvl].tamano();
            for (int i = 0; i < n; i++) {
                Entry e = niveles[lvl].sacar();
                if (e == null) break;

                if (ciclo - e.tArribo >= envejecimiento) {
                    int destino = lvl - 1; 
                    e.nivel = destino;
                    e.tArribo = ciclo;
                    niveles[destino].ofrecer(e);
                } else {
                    e.nivel = lvl;
                    niveles[lvl].ofrecer(e);
                }
            }
        }
    }

    private int clampNivel(int n) {
        if (n < 0) return 0;
        if (n >= niveles.length) return niveles.length - 1;
        return n;
    }

    private void reencolar_NoLock(ProcesoPlanificable p, long ciclo, int nivelDestino) {
        nivelDestino = clampNivel(nivelDestino);
        niveles[nivelDestino].ofrecer(new Entry(p, ciclo, nivelDestino));
    }


    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            removerDeTodosLosNiveles_NoLock(p);
            bloqueados.remove(p);
            reencolar_NoLock(p, ciclo, 0);
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
            aplicarEnvejecimiento_NoLock(ciclo);
            for (int lvl = 0; lvl < niveles.length; lvl++) {
                Entry e = niveles[lvl].sacar();
                if (e != null) {
                    bloqueados.remove(e.p);
                    e.nivel = lvl;
                    try { e.p.setPrioridad(lvl); } catch (Throwable ignore) {}
                    try { e.p.reiniciarQuantum(); } catch (Throwable ignore) {}
                    e.tArribo = ciclo;
                    return e.p;
                }
            }
            return null;
        } catch (InterruptedException ie) {
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
            int nivelActual = clampNivel(c.prioridad());
            int q = quantums[Math.max(0, Math.min(nivelActual, quantums.length - 1))];
            System.out.println("[MLFQ] Quantum expiró para " + c.nombre() +
                    " en nivel " + nivelActual + " (q=" + q + ") ciclo=" + ciclo);

            int nivelDestino = Math.min(nivelActual + 1, niveles.length - 1);
            try { c.reiniciarQuantum(); } catch (Throwable ignore) {}
            reencolar_NoLock(c, ciclo, nivelDestino);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) { /* opcional/log */ }

@Override
public void reordenarColas(long ciclo) {
    try {
        mutex.acquire();
        int promovidos = 0;
        for (int lvl = 1; lvl < niveles.length; lvl++) {
            int n = niveles[lvl].tamano();
            for (int i = 0; i < n; i++) {
                Entry e = niveles[lvl].sacar();
                if (e == null) break;
                if (ciclo - e.tArribo >= envejecimiento) {
                    int destino = lvl - 1;
                    e.nivel = destino;
                    e.tArribo = ciclo;
                    niveles[destino].ofrecer(e);
                    promovidos++;
                } else {
                    e.nivel = lvl;
                    niveles[lvl].ofrecer(e);
                }
            }
        }
        if (promovidos > 0) {
            System.out.println("[MLFQ] AGE: " + promovidos + " promovidos @ciclo " + ciclo);
        }
    } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
    } finally {
        mutex.release();
    }
}

    @Override
    public void reconfigurar(Object delta) {
        if (delta instanceof int[] q && q.length == quantums.length) {
            for (int i = 0; i < q.length; i++) if (q[i] > 0) quantums[i] = q[i];
        }
    }

    @Override
    public PoliticaPlanificacion politica() {
        return PoliticaPlanificacion.MLFQ;
    }


    @Override
    public boolean debeExpropiar(ProcesoPlanificable corriendo) {
        if (corriendo == null) return false;
        try {
            mutex.acquire();
            int nivelCorriendo = clampNivel(corriendo.prioridad());
            for (int lvl = 0; lvl < nivelCorriendo; lvl++) {
                if (!niveles[lvl].vacia()) return true;
            }
            return false;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }

    @Override
    public void reencolarPorPreempcion(ProcesoPlanificable p, long ciclo) {
        if (p == null) return;
        try {
            mutex.acquire();
            int nivel = clampNivel(p.prioridad());
            reencolar_NoLock(p, ciclo, nivel);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }


    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        if (p == null) return;
        try {
            mutex.acquire();
            removerDeTodosLosNiveles_NoLock(p);
            bloqueados.remove(p);
            bloqueados.addLast(p);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }


    @Override
    public List<ProcesoPlanificable> getColaListos() {
        List<ProcesoPlanificable> todosLosListos = new ArrayList<>();
        try {
            mutex.acquire();
            for (Cola<Entry> colaDeNivel : niveles) {
                for (Entry e : colaDeNivel) {
                    todosLosListos.add(e.p);
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
        return todosLosListos;
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
            for (Cola<Entry> q : niveles) q.limpiar(); 
            bloqueados.clear();
            terminados.clear();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }
}
