/*
 * Click nbfs://nbhostSystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
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
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Es una buena práctica ejecutar la GUI en el hilo de eventos de Swing (EDT)
        SwingUtilities.invokeLater(() -> {

            // --- 1: CREAR LOS COMPONENTES CENTRALES DE LA LÓGICA ---
            BusEventos bus = new BusEventosSimple();
            CPU cpu = new ImplementacionCPU(bus);
            Despachador despachador = new ImplementacionDespachador(cpu, bus);

            // --- 2: CREAR TODAS LAS POLÍTICAS DE PLANIFICACIÓN ---
            Map<String, PlanificadorCortoPlazo> planificadores = new HashMap<>();
            planificadores.put("FCFS", new PlanificadorFCFS());
            planificadores.put("SJF", new PlanificadorSJF());
            planificadores.put("SRTF", new PlanificadorSRTF());
            planificadores.put("Round Robin", new PlanificadorRR());
            planificadores.put("Prioridades", new PlanificadorPrioridadPreemption());
            planificadores.put("MLFQ", new PlanificadorMLFQ(3, new int[]{2, 4, 8}, 20));
            
            // Seleccionamos un planificador inicial (por ejemplo, FCFS)
            PlanificadorCortoPlazo planificadorActual = planificadores.get("FCFS");

            // --- 3: CREAR LA VISTA (LA GUI) ---
            SimuladorGUI vista = new SimuladorGUI();

            // --- 4: CREAR EL CONTROLADOR QUE UNE LA VISTA Y LA LÓGICA ---
            ControladorReal controlador = new ControladorReal(vista, cpu, planificadorActual, despachador, planificadores);

            // --- 5: CONECTAR LAS PIEZAS ---
            vista.setControlador(controlador); // La vista necesita saber quién es el controlador
            vista.configurarListeners();   // Activamos los botones para que hablen con el controlador

            // --- 6: INICIAR EL HILO DE SIMULACIÓN (EL RELOJ) ---
Thread hiloSimulacion = new Thread(() -> {
                long ciclo = 0;
                while (true) { 
                    if (controlador.estaCorriendo()) {
                        
                        // --- LÓGICA DE UN CICLO DEL SIMULADOR ---
                        
                        // ... (El resto de la lógica del ciclo se queda igual) ...
                        
                        // --- CORRECCIÓN PARA EL ERROR DE LAMBDA ---
                        // 1. Creamos una copia 'final' del valor actual de 'ciclo'
                        final long cicloActualParaGUI = ciclo;
                        
                        // 2. Usamos esa copia final dentro de la lambda
                        SwingUtilities.invokeLater(() -> controlador.actualizarVistaCicloACiclo(cicloActualParaGUI));
                        // --- FIN DE LA CORRECCIÓN ---

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

            hiloSimulacion.setDaemon(true); // El hilo termina si la aplicación se cierra
            hiloSimulacion.start();        // ¡Arrancamos el reloj!

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