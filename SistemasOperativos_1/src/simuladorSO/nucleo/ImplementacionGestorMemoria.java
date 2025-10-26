/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author eabdf
 */
public class ImplementacionGestorMemoria implements GestorMemoria{
   private final Semaphore mutex = new Semaphore(1, true);
    private final int capacidad;
    private int usado = 0;

    private final Map<Long, Integer> huella = new HashMap<>();

    public ImplementacionGestorMemoria(int capacidad) {
        this.capacidad = Math.max(1, capacidad);
    }

    private int estimar(ProcesoPlanificable p) {
        return Math.max(1, p.totalInstrucciones() / 2);
    }

    @Override
    public boolean intentarCargar(ProcesoPlanificable p) {
        try {
            mutex.acquire();
            int s = estimar(p);
            if (s <= (capacidad - usado)) {
                usado += s;
                huella.put(p.pid(), s);
                return true;
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            mutex.release();
        }
    }

    @Override
    public void sacarDeMemoria(ProcesoPlanificable p) {
        try {
            mutex.acquire();
            Integer s = huella.remove(p.pid());
            if (s != null) usado -= s;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    @Override
    public boolean reingresar(ProcesoPlanificable p) {
        return intentarCargar(p);
    }

    @Override public int capacidad() { return capacidad; }
    @Override public int usado() { return usado; } 
}
