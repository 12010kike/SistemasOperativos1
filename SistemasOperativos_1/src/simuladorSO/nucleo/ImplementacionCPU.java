/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.modelo.ProcesoPlanificable;

/**
 *
 * @author obelm
 */
public class ImplementacionCPU implements CPU, SuscriptorReloj {

    private final BusEventos bus;
    private volatile boolean modoSO = false;
    private ProcesoPlanificable ejecutando; // null si ociosa

    public ImplementacionCPU (BusEventos busEventos) {
        this.bus = busEventos;
    }

    @Override
    public synchronized void cargar(ProcesoPlanificable p) {
        this.ejecutando = p;
    }

    @Override
    public synchronized ProcesoPlanificable descargar() {
        ProcesoPlanificable prev = this.ejecutando;
        this.ejecutando = null;
        return prev;
    }

    @Override
    public synchronized ProcesoPlanificable ejecutando() { return this.ejecutando; }

    @Override
    public void paso(long ciclo) {
        ProcesoPlanificable p;
        synchronized (this) { p = ejecutando; }
        if (p != null) {
            p.ejecutarUnCiclo(ciclo); // la lógica PC/MAR/IO se implementará en PASO 2
        }
    }

    @Override
    public boolean modoSO() { return modoSO; }

    @Override
    public void setModoSO(boolean so) { this.modoSO = so; }

    @Override
    public void alTick(long ciclo) { paso(ciclo); }
}
