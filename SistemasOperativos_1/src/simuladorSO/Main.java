package simuladorSO;

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