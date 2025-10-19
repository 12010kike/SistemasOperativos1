/*
 * Esta clase actúa como el intermediario (Controlador) entre la Vista (SimuladorGUI)
 * y el Modelo (la lógica del simulador como CPU, Despachador, Planificadores).
 */
package simuladorSO.gui;

// --- Importaciones de Lógica ---
import simuladorSO.nucleo.CPU;
import simuladorSO.nucleo.Despachador;
import simuladorSO.planificador.PlanificadorCortoPlazo;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.metrica.InstantaneaMetricas;
import java.util.ArrayList;

// --- CORRECCIÓN: Imports necesarios para la creación de procesos ---
import simuladorSO.modelo.GeneradorProcesos;
import simuladorSO.modelo.TipoProceso;

// --- Importaciones para gestionar los planificadores y su estado ---
import java.util.Map;
import java.util.List;

/**
 * @author Santiago Castro (con la ayuda de Gemini)
 */
public class ControladorReal implements ControladorSimulador {

    // --- VARIABLES MIEMBRO ---
    private final Señalizador señalizador;
    private final CPU cpu;
    private final Despachador despachador;
    private final Map<String, PlanificadorCortoPlazo> planificadores;
    private volatile PlanificadorCortoPlazo planificadorActual;
    private volatile boolean corriendo = false;
    private volatile long tickMs = 500;
    private final List<ProcesoPlanificable> colaTerminados = new ArrayList<>();
    
    // --- CORRECCIÓN: Se añade la variable para el Generador de Procesos ---
    private final GeneradorProcesos generador;

    /**
     * Constructor del Controlador.
     * Recibe todas las piezas de la lógica y la referencia a la vista.
     */
    // --- CORRECCIÓN: Se actualiza el constructor para recibir el GeneradorProcesos ---
    public ControladorReal(Señalizador señalizador, CPU cpu, PlanificadorCortoPlazo planificadorInicial,
                           Despachador despachador, Map<String, PlanificadorCortoPlazo> planificadores,
                           GeneradorProcesos generador) { // <-- Nuevo parámetro
        this.señalizador = señalizador;
        this.cpu = cpu;
        this.planificadorActual = planificadorInicial;
        this.despachador = despachador;
        this.planificadores = planificadores;
        this.generador = generador; // <-- Nueva asignación
    }

    // --- MÉTODOS PARA EL HILO DE SIMULACIÓN (EN MAIN) ---
    public boolean estaCorriendo() { return corriendo; }
    public long getTickMs() { return tickMs; }
    public PlanificadorCortoPlazo getPlanificadorActual() { return planificadorActual; }
    
    public void actualizarVistaCicloACiclo(long cicloActual) {
        señalizador.refrescarCPU(cpu.ejecutando());

        List<ProcesoPlanificable> listos = planificadorActual.getColaListos();
        List<ProcesoPlanificable> bloqueados = planificadorActual.getColaBloqueados();
        List<ProcesoPlanificable> terminados = planificadorActual.getColaTerminados();
        
        señalizador.refrescarColas(listos, bloqueados, this.colaTerminados, null, null);

        // Se usa 'null' para las métricas temporalmente para evitar errores de compilación.
        señalizador.actualizarMetricas(cicloActual);
    }
    public void notificarProcesoTerminado(ProcesoPlanificable p) {
    if (p != null) {
        this.colaTerminados.add(p);
    }
}
    // --- MÉTODOS DE LA INTERFAZ ControladorSimulador ---
    
    @Override
    public void play() {
        this.corriendo = true;
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación iniciada.", "");
    }

    @Override
    public void pause() {
        this.corriendo = false;
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación pausada.", "");
    }

    @Override
    public void setPolitica(String nombre) {
        PlanificadorCortoPlazo nuevoPlanificador = planificadores.get(nombre);
        if (nuevoPlanificador != null) {
            this.planificadorActual = nuevoPlanificador;
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Política cambiada a " + nombre, "");
            System.out.println("Controlador: Política cambiada a " + nombre);
        }
    }
    
    // --- CORRECCIÓN: Se implementa el método para generar un proceso ---
    @Override
    public void generarProcesoDePrueba() {
        if (generador == null) {
            System.err.println("Error: El Generador de Procesos no ha sido inicializado.");
            return;
        }
        
        // 1. Usamos el generador para crear un nuevo proceso con valores de ejemplo.
        ProcesoPlanificable nuevoProceso = generador.crearManual(
            "P_" + System.currentTimeMillis() % 1000, // Nombre único
            10 + (int)(Math.random() * 20),           // Duración aleatoria entre 10 y 29
            TipoProceso.CPU_BOUND,
            0, 0, 0, 0L
        );

        // 2. Encolamos el nuevo proceso en el planificador que esté activo.
        planificadorActual.encolar(nuevoProceso, 0L); // Asumimos ciclo de llegada 0 por ahora

        // 3. Enviamos un mensaje al log para que el usuario sepa qué pasó.
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Nuevo proceso creado y encolado.", nuevoProceso.nombre());
    }

    // El resto de los métodos de la interfaz (los dejamos vacíos por ahora)
    @Override public void reset() {}
    @Override public void setTickMs(long ms) {}
    @Override public void setQuantumRR(int q) {}
    @Override public void setMLFQ(int niveles, int[] quantums) {}
    @Override public void generarProcesosAleatorios(int cantidad) {}
    @Override public void cargarConfig(String path) {}
    @Override public void guardarConfig(String path) {}
}