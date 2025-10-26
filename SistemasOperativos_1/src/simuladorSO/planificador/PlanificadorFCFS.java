/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.planificador;
import java.util.concurrent.Semaphore;
import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
import simuladorSO.modelo.ProcesoPlanificable;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author obelm
 */
// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class PlanificadorFCFS implements PlanificadorCortoPlazo {
    private final ColaEnlazada<ProcesoPlanificable> colaListos = new ColaEnlazada<>();
    private final ColaEnlazada<ProcesoPlanificable> colaBloqueados = new ColaEnlazada<>();
    private final Semaphore mutex = new Semaphore(1, true);

    @Override
    public void registrarProcesoBloqueado(ProcesoPlanificable p) {
        try {
            mutex.acquire();
            if (!colaBloqueados.contiene(p)) {
                 colaBloqueados.ofrecer(p);
            }
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
    }

    @Override
    public void encolar(ProcesoPlanificable p, long ciclo) {
        try {
            mutex.acquire();
            colaBloqueados.remover(p); 
            if (!colaListos.contiene(p)) {
                colaListos.ofrecer(p);
            }
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
    }



    @Override
    public ProcesoPlanificable seleccionarSiguiente(long ciclo) {
        ProcesoPlanificable p = null;
        try {
            mutex.acquire();
            p = colaListos.sacar();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return p;
    }

    
    @Override public void alVencerQuantum(ProcesoPlanificable corriendo, long ciclo) { }
    @Override public void alArribar(ProcesoPlanificable p, ProcesoPlanificable corriendo, long ciclo) {  }
    @Override public void reordenarColas(long ciclo) { }
    @Override public void reconfigurar(Object delta) { }
    @Override
    public PoliticaPlanificacion politica() { return PoliticaPlanificacion.FCFS; }
    @Override
    public List<ProcesoPlanificable> getColaListos() {
        List<ProcesoPlanificable> lista = new ArrayList<>();
        try {
            mutex.acquire();
            lista = colaListos.toList();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return lista;
    }

    @Override
    public List<ProcesoPlanificable> getColaBloqueados() {
        List<ProcesoPlanificable> lista = new ArrayList<>();
        try {
            mutex.acquire();
            lista = colaBloqueados.toList();
        } catch (InterruptedException ignored) {} 
        finally { mutex.release(); }
        return lista;
    }
    @Override
    public List<ProcesoPlanificable> getColaTerminados() {
        return new ArrayList<>();
    }
    @Override
    public void clear() {
        try {
            mutex.acquire();
            colaListos.limpiar();
            colaBloqueados.limpiar();
        } catch (InterruptedException ignored) {
        } finally {
            mutex.release();
        }
    }


   }
