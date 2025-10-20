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
import simuladorSO.planificador.ImplementacionPLP.Logger;
/**
 *
 * @author eabdf
 */
public class ImplementacionPMP implements PlanificadorMedianoPlazo{
   private final Cola<ProcesoPlanificable> bloqueados = new ColaEnlazada<>();
    private final Cola<ProcesoPlanificable> bloqueadosSuspendidos = new ColaEnlazada<>();

    public void registrarBloqueado(ProcesoPlanificable p, long ciclo) {
        if (p != null && p.estado() == EstadoProceso.BLOCKED) {
            bloqueados.ofrecer(p);
        }
    }

    @Override
    public ProcesoPlanificable seleccionarParaSuspender(long ciclo) {
        ProcesoPlanificable p = bloqueados.sacar();
        if (p != null) {
            p.setEstado(EstadoProceso.SUSPENDED_BLOCKED);
            bloqueadosSuspendidos.ofrecer(p);
        }
        return p;
    }

    @Override
    public ProcesoPlanificable seleccionarParaReingresar(long ciclo) {
        ProcesoPlanificable p = bloqueadosSuspendidos.sacar();
        if (p != null) p.setEstado(EstadoProceso.BLOCKED);
        return p;
    }

    public void intentarReingresarBloqueados(GestorMemoria mem,
                                             PlanificadorCortoPlazo corto,
                                             long ciclo,
                                             Logger log) {
        while (!bloqueadosSuspendidos.vacia()) {
            ProcesoPlanificable pb = bloqueadosSuspendidos.ver();
            if (mem.reingresar(pb)) {
                bloqueadosSuspendidos.sacar();
                pb.setEstado(EstadoProceso.BLOCKED);
                if (log != null) log.log(ciclo, "REIN_BLOCKED",
                        pb.nombre() + " vuelve de SUSP_BLOCKED");
            } else {
                break;
            }
        }
    }

    public Cola<ProcesoPlanificable> getBloqueados() { return bloqueados; }
    public Cola<ProcesoPlanificable> getBloqueadosSuspendidos() { return bloqueadosSuspendidos; }
}
