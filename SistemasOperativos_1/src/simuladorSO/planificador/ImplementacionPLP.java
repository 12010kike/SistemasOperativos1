/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.planificador;
import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
import simuladorSO.modelo.EstadoProceso;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.GestorMemoria;
/**
 *
 * @author eabdf
 */
public class ImplementacionPLP implements PlanificadorLargoPlazo{
  public interface Logger {
        void log(long ciclo, String evento, String detalle);
    }

    private final GestorMemoria memoria;
    private final PlanificadorCortoPlazo corto;
    private final ImplementacionPMP mediano;
    private final Logger log;

    private final Cola<ProcesoPlanificable> listosSuspendidos = new ColaEnlazada<>();

    public ImplementacionPLP(GestorMemoria memoria,
                                     PlanificadorCortoPlazo corto,
                                     ImplementacionPMP mediano,
                                     Logger log) {
        this.memoria = memoria;
        this.corto = corto;
        this.mediano = mediano;
        this.log = log;
    }

    @Override
    public void admitir(ProcesoPlanificable p, long ciclo) {
        if (memoria.intentarCargar(p)) {
            p.setEstado(EstadoProceso.READY);
            corto.encolar(p, ciclo);
            if (log != null) log.log(ciclo, "ADMIT", p.nombre() + " READY (mem ok)");
        } else {
            p.setEstado(EstadoProceso.SUSPENDED_READY);
            listosSuspendidos.ofrecer(p);
            if (log != null) log.log(ciclo, "SUSP_READY",
                    p.nombre() + " sin memoria → ListosSuspendidos");
        }
    }

    public void intentarReingresos(long ciclo) {
        while (!listosSuspendidos.vacia()) {
            ProcesoPlanificable p = listosSuspendidos.ver();
            if (memoria.reingresar(p)) {
                listosSuspendidos.sacar();
                p.setEstado(EstadoProceso.READY);
                corto.encolar(p, ciclo);
                if (log != null) log.log(ciclo, "RE-ADMIT",
                        p.nombre() + " → READY (desde SUSP_READY)");
            } else {
                break;
            }
        }
        if (mediano != null) {
            mediano.intentarReingresarBloqueados(memoria, corto, ciclo, log);
        }
    }

    public Cola<ProcesoPlanificable> getListosSuspendidos() { return listosSuspendidos; }  
}
