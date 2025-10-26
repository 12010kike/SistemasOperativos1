package simuladorSO;

import simuladorSO.nucleo.BusEventos;
import simuladorSO.nucleo.BusEventosSimple;
import simuladorSO.nucleo.CPU;
import simuladorSO.nucleo.ImplementacionCPU;
import simuladorSO.nucleo.Despachador;
import simuladorSO.nucleo.ImplementacionDespachador;
import simuladorSO.nucleo.GestorEntradaSalida;

import simuladorSO.planificador.PlanificadorCortoPlazo;
import simuladorSO.planificador.PlanificadorFCFS;
import simuladorSO.planificador.PlanificadorSJF;
import simuladorSO.planificador.PlanificadorSRTF;
import simuladorSO.planificador.PlanificadorRR;
import simuladorSO.planificador.PlanificadorPrioridadPreemption;
import simuladorSO.planificador.PlanificadorMLFQ;

import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.modelo.GeneradorProcesos;

import simuladorSO.gui.ControladorReal;
import simuladorSO.gui.SimuladorGUI;

import simuladorSO.metrica.RecolectorMetricas;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            BusEventos bus = new BusEventosSimple();
            CPU cpu = new ImplementacionCPU(bus);
            Despachador despachador = new ImplementacionDespachador(cpu, bus);

            Map<String, PlanificadorCortoPlazo> planificadores = new HashMap<>();
            planificadores.put("FCFS", new PlanificadorFCFS());
            planificadores.put("SJF",  new PlanificadorSJF());
            planificadores.put("SRTF", new PlanificadorSRTF());
            planificadores.put("RR",   new PlanificadorRR());
            planificadores.put("PRIORIDAD_PREEMPTIVA", new PlanificadorPrioridadPreemption());
            planificadores.put("MLFQ", new PlanificadorMLFQ(3, new int[]{2, 4, 8}, 20));

            PlanificadorCortoPlazo planificadorInicial = planificadores.get("FCFS");

            SimuladorGUI vista = new SimuladorGUI();
            String[] nombres = planificadores.keySet().stream().sorted().toArray(String[]::new);
            vista.setAlgoritmosDisponibles(nombres);

            GeneradorProcesos generador = new GeneradorProcesos();

            final AtomicReference<ControladorReal> refCtrl = new AtomicReference<>();

            GestorEntradaSalida.Logger loggerParaGUI = (ciclo, evento, detalle) -> {
                vista.empujarEvento(null, evento, detalle + " (Ciclo: " + ciclo + ")");

                if ("IO_COMPLETE".equals(evento)) {
                    ControladorReal ctrl = refCtrl.get();
                    if (ctrl != null) {
                        boolean quitado = false;

                        int idx = detalle.indexOf("pid=");
                        if (idx >= 0) {
                            try {
                                int end = detalle.indexOf(')', idx);
                                int pid = Integer.parseInt(detalle.substring(idx + 4, end));
                                ctrl.notificarIOCompletaPorPid(pid);
                                quitado = true;
                            } catch (Exception ignored) {}
                        }

                        if (!quitado) {
                            int ix = detalle.indexOf(" vuelve");
                            if (ix > 0) {
                                String nombreProc = detalle.substring(0, ix).trim();
                                ctrl.notificarIOCompletaPorNombre(nombreProc);
                            }
                        }
                    }
                }
            };

            GestorEntradaSalida gestorES = new GestorEntradaSalida(planificadorInicial, loggerParaGUI);
            gestorES.setMsPorCiclo(50); 
            ControladorReal controlador = new ControladorReal(
                vista, cpu, planificadorInicial, despachador, planificadores, generador, gestorES
            );
            refCtrl.set(controlador);

            vista.setControlador(controlador);
            vista.configurarListeners();

            RecolectorMetricas reco = new RecolectorMetricas();

            Thread hiloSimulacion = new Thread(() -> {
                long ciclo = 0;
                while (true) {

                    if (controlador.consumirResetPendiente()) {
                        ciclo = 0;
                        try { despachador.expropiarActual(ciclo); } catch (Exception ignore) {}
                        planificadores.values().forEach(p -> {
                            try { p.clear(); } catch (Exception ignore) {}
                        });
                        final long cicloGUI0 = ciclo;
                        SwingUtilities.invokeLater(() -> controlador.actualizarVistaCicloACiclo(cicloGUI0, ""));
                    }

                    if (controlador.estaCorriendo()) {

                        ProcesoPlanificable procesoEnCPU = cpu.ejecutando();

                        if (procesoEnCPU != null) {
                            if (procesoEnCPU.completo()) {
                                ProcesoPlanificable terminado = despachador.expropiarActual(ciclo);
                                controlador.notificarProcesoTerminado(terminado, ciclo);

                                controlador.actualizarVentanaMetricas(false, true);
                                reco.registrarFinalizacion();

                                procesoEnCPU = null;

                            } else if (procesoEnCPU.debeSolicitarES(ciclo)) {
                                ProcesoPlanificable p = despachador.expropiarActual(ciclo);
                                if (p != null) {
                                    controlador.notificarProcesoBloqueado(p, ciclo);
                                    controlador.getPlanificadorActual().registrarProcesoBloqueado(p);
                                    gestorES.solicitarES(p, p.ioDuraM(), ciclo);
                                }
                                procesoEnCPU = null;
                            }
                        }

                        if (procesoEnCPU == null) {
                            procesoEnCPU = controlador.getPlanificadorActual().seleccionarSiguiente(ciclo);
                            if (procesoEnCPU != null) {
                                controlador.notificarPosibleDesbloqueo(procesoEnCPU);
                                controlador.marcarPrimeraRespuestaSiAplica(procesoEnCPU.pid(), ciclo);
                                despachador.despacharACPU(procesoEnCPU, ciclo);
                            }
                        }

                        boolean huboCPU = false;
                        if (procesoEnCPU != null) {
                            cpu.paso(ciclo);
                            controlador.sumarCpuTick(procesoEnCPU.pid());
                            huboCPU = true;
                        }

                        if (procesoEnCPU != null
                                && controlador.getPlanificadorActual() instanceof PlanificadorRR rr) {
                            int q = rr.getQuantum();
                            if (procesoEnCPU.quantumConsumido() >= q) {
                                controlador.getPlanificadorActual().alVencerQuantum(procesoEnCPU, ciclo);
                                despachador.expropiarActual(ciclo);
                                procesoEnCPU = null;
                            }
                        }

                        if (procesoEnCPU != null && controlador.getPlanificadorActual().debeExpropiar(procesoEnCPU)) {
                            controlador.getPlanificadorActual().reencolarPorPreempcion(procesoEnCPU, ciclo);
                            despachador.expropiarActual(ciclo);
                            procesoEnCPU = null;

                            procesoEnCPU = controlador.getPlanificadorActual().seleccionarSiguiente(ciclo);
                            if (procesoEnCPU != null) {
                                controlador.notificarPosibleDesbloqueo(procesoEnCPU);
                                controlador.marcarPrimeraRespuestaSiAplica(procesoEnCPU.pid(), ciclo);
                                despachador.despacharACPU(procesoEnCPU, ciclo);
                            }
                        }

                        controlador.actualizarVentanaMetricas(huboCPU, false);
                        reco.registrarTick(huboCPU);
                        int nListos = controlador.getPlanificadorActual().getColaListos().size();
                        int nBloqs  = controlador.getPlanificadorActual().getColaBloqueados().size();
                        reco.registrarInstantanea(nListos, nBloqs, 0);

                        double util = reco.getUtilizacionFinal();
                        double thr  = reco.getThroughputFinal();
                        String extra = String.format(java.util.Locale.US,
                                " | Util=%.0f%% | Th=%.3f", util * 100.0, thr);

                        final long cicloGUI = ciclo;
                        final String suf = extra;
                        SwingUtilities.invokeLater(() -> controlador.actualizarVistaCicloACiclo(cicloGUI, suf));

                        ciclo++;
                    }

                    try {
                        Thread.sleep(controlador.getTickMs());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            hiloSimulacion.setDaemon(true);
            hiloSimulacion.start();

            vista.setVisible(true);
        });
    }
}

/*package simuladorSO;

// --- Importaciones de Lógica ---
import simuladorSO.nucleo.*;
import simuladorSO.planificador.*;
import simuladorSO.modelo.*;

// --- Importaciones de GUI ---
import simuladorSO.gui.ControladorReal;
import simuladorSO.gui.SimuladorGUI;
import javax.swing.SwingUtilities;

// --- Importaciones para gestionar los planificadores ---
import java.util.Map;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // --- 1: CREAR COMPONENTES CENTRALES ---
            BusEventos bus = new BusEventosSimple();
            CPU cpu = new ImplementacionCPU(bus);
            Despachador despachador = new ImplementacionDespachador(cpu, bus);

            // --- 2: CREAR PLANIFICADORES ---
            Map<String, PlanificadorCortoPlazo> planificadores = new HashMap<>();
            planificadores.put("FCFS", new PlanificadorFCFS());
            planificadores.put("SJF", new PlanificadorSJF());
            planificadores.put("SRTF", new PlanificadorSRTF());
            planificadores.put("Round Robin", new PlanificadorRR());
            planificadores.put("Prioridades", new PlanificadorPrioridadPreemption());
            planificadores.put("MLFQ", new PlanificadorMLFQ(3, new int[]{2, 4, 8}, 20));
            
            PlanificadorCortoPlazo planificadorInicial = planificadores.get("FCFS");

            // --- 3: CREAR VISTA Y GENERADOR DE PROCESOS ---
            SimuladorGUI vista = new SimuladorGUI();
            GeneradorProcesos generador = new GeneradorProcesos();

            // --- 4: CREAR CONTROLADOR ---
            ControladorReal controlador = new ControladorReal(vista, cpu, planificadorInicial, despachador, planificadores, generador);
            
            // --- 5: CONECTAR PIEZAS ---
            vista.setControlador(controlador);
            vista.configurarListeners();

            // --- 6: INICIAR HILO DE SIMULACIÓN (EL RELOJ) ---
            Thread hiloSimulacion = new Thread(() -> {
                long ciclo = 0;
                while (true) {
                    if (controlador.estaCorriendo()) {
                        
                        // --- INICIO DE LA LÓGICA DE CICLO CON FINALIZACIÓN ---
                        
                        // 1. Despachar si la CPU está vacía.
                        if (cpu.ejecutando() == null) {
                            ProcesoPlanificable proximo = controlador.getPlanificadorActual().seleccionarSiguiente(ciclo);
                            if (proximo != null) {
                                despachador.despacharACPU(proximo, ciclo);
                            }
                        }
                        
                        // 2. Ejecutar un ciclo de CPU.
                        cpu.paso(ciclo);
                        
                        // 3. Verificar si el proceso en ejecución ha terminado.
                        ProcesoPlanificable procesoActual = cpu.ejecutando();
                        if (procesoActual != null && procesoActual.completo()) {
                            ProcesoPlanificable terminado = despachador.expropiarActual(ciclo);
                            
                            // Notificamos al controlador que un proceso ha terminado.
                            controlador.notificarProcesoTerminado(terminado);
                            
                            System.out.println("Proceso " + terminado.nombre() + " ha terminado.");
                        }
                        
                        // 4. Actualizar la GUI.
                        final long cicloActualParaGUI = ciclo;
                        SwingUtilities.invokeLater(() -> controlador.actualizarVistaCicloACiclo(cicloActualParaGUI));
                        
                        // --- FIN DE LA LÓGICA DE CICLO ---

                        ciclo++;
                    }

                    // Pausa para controlar la velocidad de la simulación
                    try {
                        Thread.sleep(controlador.getTickMs());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            hiloSimulacion.setDaemon(true);
            hiloSimulacion.start();

            // --- 7: HACER VISIBLE LA APLICACIÓN ---
            vista.setVisible(true);
        });
    }
}


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
/*package simuladorSO;

import simuladorSO.planificador.*;
import simuladorSO.modelo.*;


public class Main {
    public static void main(String[] args) {
        PlanificadorMLFQ mlfq = new PlanificadorMLFQ(3, new int[]{2, 4, 8}, 6);

        GeneradorProcesos gen = new GeneradorProcesos();

        ProcesoPlanificable A = gen.crearManual("A", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);
        ProcesoPlanificable B = gen.crearManual("B", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);
        ProcesoPlanificable C = gen.crearManual("C", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);

        mlfq.encolar(A, 0);
        mlfq.encolar(B, 0);
        mlfq.encolar(C, 0);

        ProcesoPlanificable actual = null;
        int quantumSimulado = 2;

        for (int ciclo = 0; ciclo < 15; ciclo++) {
            mlfq.reordenarColas(ciclo);

            if (actual == null) {
                actual = mlfq.seleccionarSiguiente(ciclo);
                if (actual == null) {
                    System.out.println("Ciclo " + ciclo + ": CPU ocioso");
                    continue;
                }
                System.out.println("Ciclo " + ciclo + ": CPU selecciona " + actual.nombre());
            }

            actual.ejecutarUnCiclo(ciclo);

            if (ciclo % quantumSimulado == quantumSimulado - 1) {
                mlfq.alVencerQuantum(actual, ciclo);
                actual = null;
            }
        }

        System.out.println("Fin demo MLFQ.");
    }
}
*/


/*package simuladorSO;

import simuladorSO.planificador.*;
import simuladorSO.modelo.*;
import simuladorSO.nucleo.GestorEntradaSalida;

public class Main {
    public static void main(String[] args) {
        // Planificador de corto plazo (usa FCFS para la demo)
        PlanificadorCortoPlazo plan = new PlanificadorFCFS();

        // Logger mínimo para ver el timeline
        GestorEntradaSalida.Logger logger = (ciclo, evento, detalle) ->
                System.out.println("[" + ciclo + "] " + evento + " :: " + detalle);

        // Gestor de E/S
        GestorEntradaSalida gestor = new GestorEntradaSalida(plan, logger);
        gestor.setMsPorCiclo(60); // 60ms por ciclo para notar la E/S

        // Generador de procesos (usa tu clase real)
        GeneradorProcesos gen = new GeneradorProcesos();

        // P1 = CPU-bound puro (no pide E/S)
        ProcesoPlanificable P1 = gen.crearManual("CPU_Puro", 12,
                TipoProceso.CPU_BOUND, 0, 0, 1, 0L);

        // P2 = I/O-bound frecuente: cada 2 ciclos pide E/S que dura 3
        ProcesoPlanificable P2 = gen.crearManual("IO_Frecuente", 14,
                TipoProceso.IO_BOUND, 2, 3, 1, 0L);

        plan.encolar(P1, 0);
        plan.encolar(P2, 0);

        ProcesoPlanificable corriendo = null;
        int ciclosMax = 26;
        long ciclo = 0;

        while (ciclo < ciclosMax) {
            if (corriendo == null) {
                corriendo = plan.seleccionarSiguiente(ciclo);
                if (corriendo == null) {
                    System.out.println("Ciclo " + ciclo + ": CPU ocioso");
                    ciclo++;
                    dormir(50);
                    continue;
                }
                System.out.println("Ciclo " + ciclo + ": CPU selecciona " + corriendo.nombre()
                        + " (pid=" + corriendo.pid() + ")");
                corriendo.setEstado(EstadoProceso.RUNNING);
            }

            // Ejecuta 1 instrucción
            corriendo.ejecutarUnCiclo(ciclo);

            // Terminó
            if (corriendo.completo()) {
                System.out.println("Ciclo " + ciclo + ": " + corriendo.nombre() + " TERMINADO");
                corriendo.setEstado(EstadoProceso.TERMINATED);
                corriendo = null;
                ciclo++;
                continue;
            }

            // ¿Debe ir a E/S?
            if (corriendo.debeSolicitarES(ciclo)) {
                System.out.println("Ciclo " + ciclo + ": " + corriendo.nombre() + " solicita E/S");
                gestor.solicitarES(corriendo, ciclo);
                corriendo = null; // CPU libre
                ciclo++;
                continue;
            }

            ciclo++;
            dormir(50);
        }

        gestor.apagar();
        System.out.println("Fin demo PASO 5.");
    }

    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
*/


/*package simuladorSO;

import simuladorSO.modelo.*;
import simuladorSO.planificador.*;
import simuladorSO.nucleo.*;

public class Main {

    public static void main(String[] args) {
        // Corto plazo (puedes cambiar a RR/MLFQ)
        PlanificadorCortoPlazo corto = new PlanificadorFCFS();

        // Memoria pequeña para forzar suspensiones
        GestorMemoria mem = new ImplementacionGestorMemoria(20);

        // Mediano y Largo plazo (coordinados)
        ImplementacionPMP mediano = new  ImplementacionPMP();
         ImplementacionPLP  largo   = new  ImplementacionPLP(mem, corto, mediano,
                (c,e,d) -> System.out.println("[" + c + "] " + e + " :: " + d));

        // E/S (opcional para ver bloqueos reales)
        GestorEntradaSalida gestorES = new GestorEntradaSalida(corto,
                (c,e,d) -> System.out.println("[" + c + "] " + e + " :: " + d));
        gestorES.setMsPorCiclo(50);

        GeneradorProcesos gen = new GeneradorProcesos();

        ProcesoPlanificable P1 = gen.crearManual("CPU_15", 15, TipoProceso.CPU_BOUND, 0, 0, 1, 0L);
        ProcesoPlanificable P2 = gen.crearManual("IO_20",  20, TipoProceso.IO_BOUND, 3, 4, 1, 0L);
        ProcesoPlanificable P3 = gen.crearManual("MIX_18", 18, TipoProceso.IO_BOUND, 5, 3, 1, 0L);

        long ciclo = 0L;
       
        largo.admitir(P1, ciclo);
        largo.admitir(P2, ciclo);
        largo.admitir(P3, ciclo); 

        ProcesoPlanificable corriendo = null;
        int max = 90;

        while (ciclo < max) {
            
            if (corriendo == null) {
                corriendo = corto.seleccionarSiguiente(ciclo);
                if (corriendo == null) { ciclo++; dormir(30); continue; }
                System.out.println("Ciclo " + ciclo + ": CPU selecciona " + corriendo.nombre());
                corriendo.setEstado(EstadoProceso.RUNNING);
            }

            
            corriendo.ejecutarUnCiclo(ciclo);

            if (corriendo.completo()) {
                System.out.println("Ciclo " + ciclo + ": " + corriendo.nombre() + " TERMINADO");
                corriendo.setEstado(EstadoProceso.TERMINATED);
                mem.sacarDeMemoria(corriendo);   
                corriendo = null;
                largo.intentarReingresos(ciclo);  
                ciclo++; dormir(25); continue;
            }

            if (corriendo.debeSolicitarES(ciclo)) {
                System.out.println("Ciclo " + ciclo + ": " + corriendo.nombre() + " solicita E/S");
                corriendo.setEstado(EstadoProceso.BLOCKED);
                mediano.registrarBloqueado(corriendo, ciclo);
                gestorES.solicitarES(corriendo, ciclo); 

                
                if (mem.capacidad() - mem.usado() < 3) {
                    ProcesoPlanificable susp = mediano.seleccionarParaSuspender(ciclo);
                    if (susp != null) mem.sacarDeMemoria(susp);
                }
                largo.intentarReingresos(ciclo);

                corriendo = null;
                ciclo++; dormir(25); continue;
            }

            ciclo++; dormir(20);
        }

        gestorES.apagar();
        System.out.println("Fin demo PASO 6.");
    }

    private static void dormir(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}

*/