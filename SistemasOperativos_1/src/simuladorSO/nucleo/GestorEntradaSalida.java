/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import simuladorSO.modelo.EstadoProceso;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.planificador.PlanificadorCortoPlazo;
/**
 *
 * @author eabdf
 */
public class GestorEntradaSalida {
   public interface Logger {
        void log(long ciclo, String evento, String detalle);
    }

    private final EntradaSalida dispositivo;
    private final PlanificadorCortoPlazo cortoPlazo;
    private final Logger logger;
    private final Semaphore mutex = new Semaphore(1, true);
    private volatile int msPorCiclo = 50;

    public GestorEntradaSalida(PlanificadorCortoPlazo cortoPlazo, Logger logger) {
        this.cortoPlazo = cortoPlazo;
        this.logger = logger;
        this.dispositivo = new EntradaSalida("EntradaSalida-1");
        this.dispositivo.setMsPorCiclo(msPorCiclo);
        this.dispositivo.iniciar();
    }

    public void setMsPorCiclo(int ms) {
        this.msPorCiclo = Math.max(1, ms);
        this.dispositivo.setMsPorCiclo(this.msPorCiclo);
    }

    public void apagar() { dispositivo.detener(); }

    // --- OPCIÓN A: firma antigua, pero sin depender de ioDurM() en compilación
    public void solicitarES(ProcesoPlanificable p, long cicloActual) {
        int durTicks = extraerDuracionES(p); // intenta ioDurM()/ioDur()/getIoDur() por reflexión; si no, 1
        solicitarES(p, durTicks, cicloActual);
    }

    // --- OPCIÓN B: firma preferida (pásame la duración explícita desde el intérprete)
    public void solicitarES(ProcesoPlanificable p, int durTicks, long cicloActual) {
        try {
            mutex.acquire();
            p.setEstado(EstadoProceso.BLOCKED);
            if (logger != null) {
                logger.log(cicloActual, "IO_REQUEST",
                        p.nombre() + " (pid=" + p.pid() + ") dura=" + durTicks);
            }
            dispositivo.encolar(new TrabajoESProceso(p, durTicks, cicloActual));
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    
    private int extraerDuracionES(ProcesoPlanificable p) {
        String[] candidatos = {"ioDurM", "ioDur", "getIoDur"};
        for (String nombre : candidatos) {
            try {
                Method m = p.getClass().getMethod(nombre);
                Object v = m.invoke(p);
                if (v instanceof Number n) return Math.max(1, n.intValue());
            } catch (IllegalAccessException | NoSuchMethodException | SecurityException | InvocationTargetException ignored) {  }
        }
        return 1;
    }

 
    private final class TrabajoESProceso implements EntradaSalida.TrabajoES {
        private final ProcesoPlanificable p;
        private final int durTicks;
        private final long cicloSolicitud;

        TrabajoESProceso(ProcesoPlanificable p, int durTicks, long cicloSolicitud) {
            this.p = p;
            this.durTicks = Math.max(1, durTicks);
            this.cicloSolicitud = cicloSolicitud;
        }

        @Override public int duracionCiclos() { return durTicks; }

        @Override public void onComplete(long tsMillis) {
            try {
                mutex.acquire();
                p.setEstado(EstadoProceso.READY);
                if (logger != null) {
                    logger.log(cicloSolicitud, "IO_COMPLETE",
                            p.nombre() + " vuelve a LISTOS");
                }
                cortoPlazo.encolar(p, cicloSolicitud);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            } finally {
                mutex.release();
            }
        }
    }
}
