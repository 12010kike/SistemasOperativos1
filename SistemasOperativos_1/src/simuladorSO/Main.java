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


package simuladorSO;

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

