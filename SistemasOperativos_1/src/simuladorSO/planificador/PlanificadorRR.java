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
}