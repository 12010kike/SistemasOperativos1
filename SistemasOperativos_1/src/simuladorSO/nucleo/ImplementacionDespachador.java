/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import java.util.concurrent.Semaphore;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author eabdf
 */
public class ImplementacionDespachador implements Despachador {
   private final CPU cpu;
    private final BusEventos bus;
    private final Semaphore contextMutex = new Semaphore(1, true);

    public ImplementacionDespachador (CPU cpu, BusEventos bus) {
        this.cpu = cpu;
        this.bus = bus;
    }

    @Override
    public void despacharACPU(ProcesoPlanificable siguiente, long ciclo) {
        try {
            contextMutex.acquire();
            cpu.descargar();
            cpu.cargar(siguiente);
            if (bus != null && siguiente != null) {
                bus.publicar(BusEventosSimple.registro(ciclo, EventoSistema.DISPATCH,
                        "CPU selecciona " + siguiente.nombre(), "pid=" + siguiente.pid()));
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            contextMutex.release();
        }
    }

    @Override
    public ProcesoPlanificable expropiarActual(long ciclo) {
        ProcesoPlanificable desalojado = null;
        try {
            contextMutex.acquire();
            desalojado = cpu.descargar();
            if (bus != null && desalojado != null) {
                bus.publicar(BusEventosSimple.registro(ciclo, EventoSistema.PREEMPT,
                        "Expropiado " + desalojado.nombre(), "pid=" + desalojado.pid()));
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            contextMutex.release();
        }
        return desalojado;
    } 
}
