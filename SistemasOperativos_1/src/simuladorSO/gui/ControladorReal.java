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

// --- Importaciones para gestionar los planificadores y su estado ---
import java.util.Map;
import java.util.List;

/**
 * @author Santiago Castro (con la ayuda de Gemini)
 */
public class ControladorReal implements ControladorSimulador {

    // ... (Las variables miembro y el constructor se quedan igual) ...
    private final Señalizador señalizador;
    private final CPU cpu;
    private final Despachador despachador;
    private final Map<String, PlanificadorCortoPlazo> planificadores;
    private volatile PlanificadorCortoPlazo planificadorActual;
    private volatile boolean corriendo = false;
    private volatile long tickMs = 500;

    public ControladorReal(Señalizador señalizador, CPU cpu, PlanificadorCortoPlazo planificadorInicial,
                           Despachador despachador, Map<String, PlanificadorCortoPlazo> planificadores) {
        this.señalizador = señalizador;
        this.cpu = cpu;
        this.planificadorActual = planificadorInicial;
        this.despachador = despachador;
        this.planificadores = planificadores;
    }

    public boolean estaCorriendo() { return corriendo; }
    public long getTickMs() { return tickMs; }
    public PlanificadorCortoPlazo getPlanificadorActual() { return planificadorActual; }
    
    
    public void actualizarVistaCicloACiclo(long cicloActual) {
        señalizador.refrescarCPU(cpu.ejecutando());

        List<ProcesoPlanificable> listos = planificadorActual.getColaListos();
        List<ProcesoPlanificable> bloqueados = planificadorActual.getColaBloqueados();
        List<ProcesoPlanificable> terminados = planificadorActual.getColaTerminados();
        
        señalizador.refrescarColas(listos, bloqueados, terminados, null, null);

        // --- CORRECCIÓN #1: Usar 'null' para las métricas temporalmente ---
        // Esto evita el error de la clase abstracta y permite que el programa compile.
        señalizador.actualizarMetricas(null);
    }
    
    @Override
    public void play() {
        this.corriendo = true;
        // --- CORRECCIÓN #2: Usar un EventoSistema que sí existe (ej. DISPATCH) ---
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación iniciada.", "");
    }

    @Override
    public void pause() {
        this.corriendo = false;
        // --- CORRECCIÓN #2: Usar un EventoSistema que sí existe ---
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación pausada.", "");
    }

    @Override
    public void setPolitica(String nombre) {
        PlanificadorCortoPlazo nuevoPlanificador = planificadores.get(nombre);
        if (nuevoPlanificador != null) {
            this.planificadorActual = nuevoPlanificador;
            // --- CORRECCIÓN #2: Usar un EventoSistema que sí existe ---
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Política cambiada a " + nombre, "");
            System.out.println("Controlador: Política cambiada a " + nombre);
        }
    }
    
    @Override public void reset() {}
    @Override public void setTickMs(long ms) {}
    @Override public void setQuantumRR(int q) {}
    @Override public void setMLFQ(int niveles, int[] quantums) {}
    @Override public void generarProcesosAleatorios(int cantidad) {}
    @Override public void cargarConfig(String path) {}
    @Override public void guardarConfig(String path) {}
}