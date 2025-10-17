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
public class PlanificadorMLFQ implements PlanificadorCortoPlazo{
    private static final class Entry {
        final ProcesoPlanificable p;
        long tArribo;
        int nivel;

        Entry(ProcesoPlanificable p, long t, int n) {
            this.p = p;
            this.tArribo = t;
            this.nivel = n;
        }
    }

    private final Cola<Entry>[] niveles;     
    private final int[] quantums;           
    private final long envejecimiento;       
    private final Semaphore mutex = new Semaphore(1, true);

    /**
     * Constructor general del MLFQ.
     *
     * @param nivelesCount 
     * @param quantumsPorNivel 
     * @param envejecimiento 
     */
    @SuppressWarnings("unchecked")
    public PlanificadorMLFQ(int nivelesCount, int[] quantumsPorNivel, long envejecimiento) {
        if (nivelesCount < 2) nivelesCount = 3;
        this.niveles = new Cola[nivelesCount];

        for (int i = 0; i < nivelesCount; i++) {
            this.niveles[i] = new ColaEnlazada<>();
        }

        if (quantumsPorNivel == null || quantumsPorNivel.length != nivelesCount) {
            this.quantums = new int[]{2, 4, 8};
        } else {
            this.quantums = quantumsPorNivel.clone();
        }

        this.envejecimiento = (envejecimiento <= 0) ? 20 : envejecimiento;
    }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        try {
            mutex.acquire();
            niveles[0].ofrecer(new Entry(p, ciclo, 0));
        } catch (InterruptedException ignored) {
        } finally {
            mutex.release();
        }
    }

    @Override
    public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        try {
            mutex.acquire();

            aplicarEnvejecimiento(ciclo); // Revisar si alguien debe subir de nivel

            for (int lvl = 0; lvl < niveles.length; lvl++) {
                Entry e = niveles[lvl].sacar();
                if (e != null) {
                    e.nivel = lvl;               
                    e.p.setPrioridad(lvl);         
                    e.p.reiniciarQuantum();        
                    e.tArribo = ciclo;             
                    return e.p;
                }
            }
            return null;

        } catch (InterruptedException ignored) {
            return null;
        } finally {
            mutex.release();
        }
    }

    @Override
    public void alVencerQuantum(ProcesoPlanificable c, long ciclo) {
        if (c == null) return;

        int nivelActual = clampNivel(c.prioridad());
        int q = quantums[Math.max(0, Math.min(nivelActual, quantums.length - 1))];

        System.out.println("[MLFQ] Quantum expiró para " + c.nombre() +
                " en nivel " + nivelActual + " (q=" + q + ") ciclo=" + ciclo);

        int nivelDestino = Math.min(nivelActual + 1, niveles.length - 1);
        c.reiniciarQuantum();
        reencolar(c, ciclo, nivelDestino);
    }

    @Override
    public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {
    }

    @Override
    public void reordenarColas(long ciclo) {
        try {
            mutex.acquire();
            aplicarEnvejecimiento(ciclo);
        } catch (InterruptedException ignored) {
        } finally {
            mutex.release();
        }
    }

    @Override
    public void reconfigurar(Object delta) {
        if (delta instanceof int[] q && q.length == quantums.length) {
            for (int i = 0; i < q.length; i++) {
                if (q[i] > 0) quantums[i] = q[i];
            }
        }
    }

    @Override
    public PoliticaPlanificacion politica() {
        return PoliticaPlanificacion.MLFQ;
    }


    private void aplicarEnvejecimiento(long ciclo) {
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

    private void reencolar(ProcesoPlanificable p, long ciclo, int nivelDestino) {
        try {
            mutex.acquire();
            nivelDestino = clampNivel(nivelDestino);
            niveles[nivelDestino].ofrecer(new Entry(p, ciclo, nivelDestino));
        } catch (InterruptedException ignored) {
        } finally {
            mutex.release();
        }
    }

    private int clampNivel(int n) {
        if (n < 0) return 0;
        if (n >= niveles.length) return niveles.length - 1;
        return n;
    }
}