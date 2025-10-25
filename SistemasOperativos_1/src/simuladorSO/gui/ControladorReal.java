package simuladorSO.gui;

// --- Lógica ---
import simuladorSO.nucleo.CPU;
import simuladorSO.nucleo.Despachador;
import simuladorSO.planificador.PlanificadorCortoPlazo;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.nucleo.GestorEntradaSalida;

// --- Crear procesos de prueba ---
import simuladorSO.modelo.GeneradorProcesos;
import simuladorSO.modelo.TipoProceso;


// --- Colecciones ---
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// --- Map para métricas ---
import java.util.HashMap;

/**
 *
 * @author santi
 */

public class ControladorReal implements ControladorSimulador {

    // --- MIEMBROS ---
    private final Señalizador señalizador;
    private final CPU cpu;
    private final Despachador despachador;
    private final Map<String, PlanificadorCortoPlazo> planificadores;
    private volatile PlanificadorCortoPlazo planificadorActual;
    
    private GestorEntradaSalida gestorES;
    private volatile boolean corriendo = false;
    private volatile long tickMs = 500;

    private final List<ProcesoPlanificable> colaTerminados = new ArrayList<>();
    private final Set<ProcesoPlanificable> bloqueadosGUI = new LinkedHashSet<>();

    private final GeneradorProcesos generador;
    
    // MÉTRICAS 
    private static final class Stats {
        long t0 = -1;          // ciclo arribo (READY)
        long tPrimera = -1;    // primer RUNNING
        long tFin = -1;        // fin
        long cpuTicks = 0;     // ciclos ejecutados
    }
    private final Map<Long, Stats> stats = new HashMap<>();
    private Stats st(long pid) { return stats.computeIfAbsent(pid, k -> new Stats()); }

    // ======== CTOR ========
    public ControladorReal(Señalizador señalizador, CPU cpu, PlanificadorCortoPlazo planificadorInicial,
                           Despachador despachador, Map<String, PlanificadorCortoPlazo> planificadores,
                           GeneradorProcesos generador, GestorEntradaSalida gestorES) {
        this.señalizador = señalizador;
        this.cpu = cpu;
        this.planificadorActual = planificadorInicial;
        this.despachador = despachador;
        this.planificadores = planificadores;
        this.gestorES = gestorES;
        this.generador = generador;
        if (this.gestorES == null) {
        GestorEntradaSalida.Logger esLogger = (ciclo, evento, detalle) -> {
            if (señalizador != null) {
                señalizador.empujarEvento(
                    EventoSistema.DISPATCH,
                    "[" + evento + "]",
                    detalle
                );
            }
        };
        this.gestorES = new GestorEntradaSalida(this.planificadorActual, esLogger);
    }
    this.gestorES.setMsPorCiclo((int) this.tickMs);

    }

    // --- Accesores usados por el hilo de simulación ---
    public boolean estaCorriendo() { return corriendo; }
    public long getTickMs() { return tickMs; }
    public PlanificadorCortoPlazo getPlanificadorActual() { return planificadorActual; }

    // ======== Helpers de MÉTRICAS que usará Main ========
    public void marcarPrimeraRespuestaSiAplica(long pid, long ciclo) {
        Stats s = st(pid);
        if (s.tPrimera < 0) s.tPrimera = ciclo;
    }
    public void sumarCpuTick(long pid) {
        st(pid).cpuTicks++;
    }

public void actualizarVistaCicloACiclo(long cicloActual) {
    ProcesoPlanificable running = cpu.ejecutando();
    // NO sumar cpuTicks aquí si ya lo haces en el hilo principal
    señalizador.refrescarCPU(running);

    List<ProcesoPlanificable> listos = planificadorActual.getColaListos();

    List<ProcesoPlanificable> bloqueadosPlan = planificadorActual.getColaBloqueados();
    List<ProcesoPlanificable> bloqueados = new ArrayList<>();
    if (bloqueadosPlan != null && !bloqueadosPlan.isEmpty()) {
        bloqueados.addAll(bloqueadosPlan);
    } else {
        bloqueados.addAll(bloqueadosGUI);
    }
    
    // --- métricas agregadas ---
    long sumTA = 0, sumTR = 0, nTA = 0, nTR = 0;
    for (Stats s : stats.values()) {
        if (s.tFin >= 0 && s.t0 >= 0) { sumTA += (s.tFin - s.t0); nTA++; }
        if (s.tPrimera >= 0 && s.t0 >= 0) { sumTR += (s.tPrimera - s.t0); nTR++; }
    }
    long turnaroundProm = (nTA > 0 ? sumTA / nTA : 0);
    long respuestaProm  = (nTR > 0 ? sumTR / nTR : 0);

    // Enviar a la GUI (una sola vez)
    señalizador.setTextoMetricas("T.Avg=" + turnaroundProm + "  |  R.Avg=" + respuestaProm);
    señalizador.refrescarColas(listos, bloqueados, colaTerminados, null, null);
    señalizador.actualizarMetricas(cicloActual);

    System.out.println("Ciclo " + cicloActual + " -> Listos: "
            + listos.size() + ", Bloqueados: " + bloqueados.size()
            + " | T.Avg=" + turnaroundProm + " | R.Avg=" + respuestaProm);
        }

    // —— Notificaciones desde Main
    public void notificarProcesoBloqueado(ProcesoPlanificable p, long ciclo) {
        if (p != null) {
            bloqueadosGUI.add(p); // Set evita duplicados
            // métrica: no cambiamos nada aquí salvo que quieras llevar IO ticks
            señalizador.empujarEvento(
                EventoSistema.IO_REQUEST,
                "Proceso pasa a BLOQUEADOS (E/S)",
                p.nombre() + " @ciclo " + ciclo
            );
        }
    }

    public void notificarPosibleDesbloqueo(ProcesoPlanificable p) {
        if (p != null) bloqueadosGUI.remove(p);
    }

    public void notificarIOCompletaPorPid(int pid) {
        bloqueadosGUI.removeIf(px -> px.pid() == pid);
    }

    public void notificarIOCompletaPorNombre(String nombre) {
        if (nombre != null && !nombre.isEmpty()) {
            bloqueadosGUI.removeIf(px -> nombre.equals(px.nombre()));
        }
    }

    public void notificarProcesoTerminado(ProcesoPlanificable p, long ciclo) {
        if (p != null) {
            colaTerminados.add(p);
            st(p.pid()).tFin = ciclo; // métrica: fin
        }
    }

    // --- Interfaz ControladorSimulador ---
    @Override
    public void play() {
        corriendo = true;
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación iniciada.", "");
    }

    @Override
    public void pause() {
        corriendo = false;
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Simulación pausada.", "");
    }

    @Override
    public void setPolitica(String nombre) {
        PlanificadorCortoPlazo nuevo = planificadores.get(nombre);
        if (nuevo != null) {
            this.planificadorActual = nuevo;
            if (gestorES != null) gestorES.setPlanificador(planificadorActual);
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Política cambiada a " + nombre, "");
            System.out.println("Controlador: Política cambiada a " + nombre);
        }
    }

    @Override
    public void generarProcesoDePrueba() {
        if (generador == null) return;

        boolean esIO = Math.random() > 0.5;
        ProcesoPlanificable p;

        if (esIO) {
            p = generador.crearManual(
                "IO_" + System.currentTimeMillis() % 1000,
                15 + (int)(Math.random() * 20),
                TipoProceso.IO_BOUND,
                3 + (int)(Math.random() * 3),
                2 + (int)(Math.random() * 4),
                0, 0L
            );
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Nuevo proceso I/O-Bound creado.", p.nombre());
        } else {
            p = generador.crearManual(
                "CPU_" + System.currentTimeMillis() % 1000,
                10 + (int)(Math.random() * 20),
                TipoProceso.CPU_BOUND,
                0, 0, 0, 0L
            );
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Nuevo proceso CPU-Bound creado.", p.nombre());
        }

        // métrica: arribo (t0) a listos (no tenemos ciclo real aquí; usa 0 por ahora)
        st(p.pid()).t0 = (st(p.pid()).t0 < 0 ? 0L : st(p.pid()).t0);

        planificadorActual.encolar(p, 0L);
    }

     
    // Métodos no usados por ahora
    @Override public void reset() {}
    @Override
    public void setQuantumRR(int q) {
        if (q <= 0) return;
        // Si el planificador actual es RR, setéalo directo
        if (planificadorActual instanceof simuladorSO.planificador.PlanificadorRR rr) {
            rr.reconfigurar(Integer.valueOf(q)); // o rr.setQuantum(q);
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Quantum RR actualizado", "q=" + q);
            return;
        }
        // Si no está activo, pero lo tenés registrado, también actualízalo
        PlanificadorCortoPlazo p = planificadores.get("RR");
        if (p instanceof simuladorSO.planificador.PlanificadorRR rr2) {
            rr2.reconfigurar(Integer.valueOf(q)); // o rr2.setQuantum(q);
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Quantum RR actualizado (en planificador RR)", "q=" + q);
        }
    }

    @Override public void setMLFQ(int niveles, int[] quantums) {}
    @Override public void generarProcesosAleatorios(int cantidad) {}
    @Override public void cargarConfig(String path) {}
    @Override public void guardarConfig(String path) {}

    @Override
    public void generarProcesoManual(String nombre, int durTotal, simuladorSO.modelo.TipoProceso tipo,
                                     int ioCadaK, int ioDuraM, int prioridad, long t0) {
        if (nombre == null || nombre.isBlank()) {
            nombre = "P_" + (System.currentTimeMillis() % 1000);
        }
        if (durTotal < 1) durTotal = 1;

        if (tipo == simuladorSO.modelo.TipoProceso.CPU_BOUND) {
            ioCadaK = 0;
            ioDuraM = 0;
        }

        ProcesoPlanificable p = generador.crearManual(
            nombre, durTotal, tipo, ioCadaK, ioDuraM, prioridad, t0
        );

        // métrica: arribo real (t0 recibido)
        Stats s = st(p.pid());
        if (s.t0 < 0) s.t0 = t0;

        planificadorActual.encolar(p, t0);

        señalizador.empujarEvento(
            simuladorSO.nucleo.EventoSistema.DISPATCH,
            "Proceso creado manualmente",
            p.nombre() + " (pid=" + p.pid() + ", tipo=" + tipo + ", dur=" + durTotal + ")"
        );
    }

@Override
public void setMsPorCiclo(int ms) {
    if (ms < 1) ms = 1;
    this.tickMs = ms;
    if (gestorES != null) gestorES.setMsPorCiclo(ms);
}

@Override
public int getMsPorCiclo() {
    long v = (tickMs < 1 ? 1 : tickMs);
    return (int) v;
}

@Override
public void setTickMs(long ms) {
    if (ms < 1) ms = 1;
    this.tickMs = ms;
    if (gestorES != null) gestorES.setMsPorCiclo((int) ms);
}

}
