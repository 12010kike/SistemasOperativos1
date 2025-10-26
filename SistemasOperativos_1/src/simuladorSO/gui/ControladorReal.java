package simuladorSO.gui;

import simuladorSO.nucleo.CPU;
import simuladorSO.nucleo.Despachador;
import simuladorSO.planificador.PlanificadorCortoPlazo;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.nucleo.GestorEntradaSalida;

import simuladorSO.modelo.GeneradorProcesos;
import simuladorSO.modelo.TipoProceso;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.HashMap;

public class ControladorReal implements ControladorSimulador {

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
    private final simuladorSO.ed.Cola<ProcesoPlanificable> listosSusp = new simuladorSO.ed.ColaEnlazada<>();
    private final simuladorSO.ed.Cola<ProcesoPlanificable> bloqSusp  = new simuladorSO.ed.ColaEnlazada<>();

    private int capResidentes = 10;  
    public void setCapResidentes(int cap) { this.capResidentes = Math.max(1, cap); }
    
    private int residentesActuales() {
    int r = 0;
    if (cpu.ejecutando() != null) r++;
    var L = planificadorActual.getColaListos();
    var B = planificadorActual.getColaBloqueados();
    if (L != null) r += L.size();
    if (B != null) r += B.size();
    return r;
}
    private ProcesoPlanificable victimaParaSuspenderDeListos() {
    var L = planificadorActual.getColaListos();
    if (L == null || L.isEmpty()) return null;
    return L.get(0); 
}

private ProcesoPlanificable victimaParaSuspenderDeBloqueados() {
    var B = planificadorActual.getColaBloqueados();
    if (B == null || B.isEmpty()) return null;
    return B.get(0);
}

private java.util.List<ProcesoPlanificable> snapshotCola(simuladorSO.ed.Cola<ProcesoPlanificable> c) {
    java.util.ArrayList<ProcesoPlanificable> out = new java.util.ArrayList<>();
    for (ProcesoPlanificable p : c) out.add(p);
    return out;
}




    private static final class Stats {
        long t0 = -1;          
        long tPrimera = -1;    
        long tFin = -1;        
        long cpuTicks = 0;     
    }
    private final Map<Long, Stats> stats = new HashMap<>();
    private Stats st(long pid) { return stats.computeIfAbsent(pid, k -> new Stats()); }

    private long totalCiclos = 0;
    private long ciclosConCPUGlobal = 0;

    private static final int VENTANA = 100;
    private final java.util.ArrayDeque<Boolean> histCPU = new java.util.ArrayDeque<>(VENTANA);
    private final java.util.ArrayDeque<Boolean> histTerminados = new java.util.ArrayDeque<>(VENTANA);
    private int ciclosConCPUVentana = 0;
    private int terminadosEnVentana = 0;
    private Long ultimoPidEnCPU = null;


    private final Object lockReset = new Object();
    private volatile boolean resetPendiente = false;
    private void limpiarPlanificadores() {
    if (planificadores == null) return;
    for (PlanificadorCortoPlazo p : planificadores.values()) {
        try { p.clear(); } catch (Exception ignore) {}
    }
}


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
    private void migrarColas(PlanificadorCortoPlazo desde, PlanificadorCortoPlazo hacia, long ciclo) {
        if (desde == null || hacia == null || desde == hacia) return;

        var listos = desde.getColaListos();
        if (listos != null) for (var p : listos) if (p != null) hacia.encolar(p, ciclo);

        var bloqs = desde.getColaBloqueados();
        if (bloqs != null) for (var p : bloqs) if (p != null) hacia.registrarProcesoBloqueado(p);
    }

    public boolean estaCorriendo() { return corriendo; }
    public long getTickMs() { return tickMs; }
    public PlanificadorCortoPlazo getPlanificadorActual() { return planificadorActual; }

    public void marcarPrimeraRespuestaSiAplica(long pid, long ciclo) {
        Stats s = st(pid);
        if (s.tPrimera < 0) s.tPrimera = ciclo;
    }
    public void sumarCpuTick(long pid) {
        st(pid).cpuTicks++;
        ciclosConCPUGlobal++;
    }

    public void actualizarVistaCicloACiclo(long cicloActual, String sufijoExtra) {
    ProcesoPlanificable running = cpu.ejecutando();
    señalizador.refrescarCPU(running);
    boolean cambioContexto =
            (running == null && ultimoPidEnCPU != null) ||
            (running != null && !Long.valueOf(running.pid()).equals(ultimoPidEnCPU));

    if (cambioContexto) {
        if (running != null) {
            señalizador.empujarEvento(
                EventoSistema.DISPATCH,
                "RUN",
                running.nombre() + " @ciclo " + cicloActual
            );
            ultimoPidEnCPU = running.pid();
        } else {
            señalizador.empujarEvento(
                EventoSistema.DISPATCH,
                "SO",
                "CPU libre @ciclo " + cicloActual
            );
            ultimoPidEnCPU = null;
        }
    }

        List<ProcesoPlanificable> listos = planificadorActual.getColaListos();

        List<ProcesoPlanificable> bloqueadosPlan = planificadorActual.getColaBloqueados();
        List<ProcesoPlanificable> bloqueados = new ArrayList<>();
        if (bloqueadosPlan != null && !bloqueadosPlan.isEmpty()) {
            bloqueados.addAll(bloqueadosPlan);
        } else {
            bloqueados.addAll(bloqueadosGUI);
        }

        long sumTA = 0, sumTR = 0, nTA = 0, nTR = 0;
        for (Stats s : stats.values()) {
            if (s.tFin >= 0 && s.t0 >= 0) { sumTA += (s.tFin - s.t0); nTA++; }
            if (s.tPrimera >= 0 && s.t0 >= 0) { sumTR += (s.tPrimera - s.t0); nTR++; }
        }
        long turnaroundProm = (nTA > 0 ? sumTA / nTA : 0);
        long respuestaProm  = (nTR > 0 ? sumTR / nTR : 0);

        int tamVent = Math.max(1, histCPU.size());
        int porcentajeCPU = (int) Math.round(
                100.0 * (tamVent == 0 ? 0.0 : (ciclosConCPUVentana * 1.0 / tamVent))
        );
        double thPorCiclo = (tamVent > 0) ? (terminadosEnVentana * 1.0 / tamVent) : 0.0;
        double thPor100 = thPorCiclo * 100.0;
        
        double util01 = (tamVent == 0) ? 0.0 : (ciclosConCPUVentana * 1.0 / tamVent);
        señalizador.plotUtil(util01);


        String texto = "T.Avg=" + turnaroundProm +
                       " | R.Avg=" + respuestaProm +
                       " | Th/100=" + String.format(java.util.Locale.US, "%.1f", thPor100) +
                       " | %CPU=" + porcentajeCPU;

        if (sufijoExtra != null && !sufijoExtra.isBlank()) {
            texto += sufijoExtra;   
        }

        señalizador.setTextoMetricas(texto);
        señalizador.refrescarColas(
            listos, 
            bloqueados, 
            colaTerminados, 
            snapshotCola(listosSusp),
            snapshotCola(bloqSusp)
        );
        señalizador.actualizarMetricas(cicloActual);
    }

    public void notificarProcesoBloqueado(ProcesoPlanificable p, long ciclo) {
        if (p != null) {
            bloqueadosGUI.add(p);
            señalizador.empujarEvento(
                EventoSistema.IO_REQUEST,
                "Proceso pasa a BLOQUEADOS (E/S)",
                p.nombre() + " @ciclo " + ciclo
            );
        suspenderSiExceso(ciclo);}
    }

    /** Actualiza contadores de la ventana deslizante (para %CPU y Th/100). */
    public void actualizarVentanaMetricas(boolean huboCPU, boolean termino) {
        totalCiclos++;

        histCPU.addLast(huboCPU);
        if (huboCPU) ciclosConCPUVentana++;
        if (histCPU.size() > VENTANA) {
            boolean sale = histCPU.removeFirst();
            if (sale) ciclosConCPUVentana--;
        }

        histTerminados.addLast(termino);
        if (termino) terminadosEnVentana++;
        if (histTerminados.size() > VENTANA) {
            if (histTerminados.removeFirst()) terminadosEnVentana--;
        }
    }

    public void notificarPosibleDesbloqueo(ProcesoPlanificable p) {
        if (p != null) bloqueadosGUI.remove(p);
    }

    public void notificarIOCompletaPorPid(int pid) {
        bloqueadosGUI.removeIf(px -> px.pid() == pid);
        reanudarSiHayHueco(0L);

    }

    public void notificarIOCompletaPorNombre(String nombre) {
        if (nombre != null && !nombre.isEmpty()) {
            bloqueadosGUI.removeIf(px -> nombre.equals(px.nombre()));
            reanudarSiHayHueco(0L);
        }
    }

    private void suspenderSiExceso(long ciclo) {
    while (residentesActuales() > capResidentes) {
        ProcesoPlanificable v = victimaParaSuspenderDeListos();
        boolean deListos = true;
        if (v == null) { 
            v = victimaParaSuspenderDeBloqueados(); 
            deListos = false;
        }
        if (v == null) break;

        if (deListos) {
            var L = planificadorActual.getColaListos();
            java.util.ArrayList<ProcesoPlanificable> tmp = new java.util.ArrayList<>(L);
            tmp.remove(v);
            planificadorActual.clear();
            for (ProcesoPlanificable p : tmp) planificadorActual.encolar(p, ciclo);
            listosSusp.ofrecer(v);
            señalizador.empujarEvento(EventoSistema.DISPATCH, "SUSPEND_READY", v.nombre()+" @ciclo "+ciclo);
        } else {
            var B = planificadorActual.getColaBloqueados();
            java.util.ArrayList<ProcesoPlanificable> tmp = new java.util.ArrayList<>(B);
            tmp.remove(v);
            bloqSusp.ofrecer(v);
            señalizador.empujarEvento(EventoSistema.DISPATCH, "SUSPEND_BLOCKED", v.nombre()+" @ciclo "+ciclo);
        }
    }
}

    private void reanudarSiHayHueco(long ciclo) {
    while (residentesActuales() < capResidentes) {
        ProcesoPlanificable p = null;

        if (listosSusp.ver() != null) {               
            p = listosSusp.sacar();                   
            planificadorActual.encolar(p, ciclo);     
            señalizador.empujarEvento(EventoSistema.DISPATCH,
                    "RESUME_READY", p.nombre() + " @ciclo " + ciclo);
            continue;
        }

        if (bloqSusp.ver() != null) {
            p = bloqSusp.sacar();
            planificadorActual.registrarProcesoBloqueado(p);
            señalizador.empujarEvento(EventoSistema.DISPATCH,
                    "RESUME_BLOCKED", p.nombre() + " @ciclo " + ciclo);
            continue;
        }

        break;
    }
}

    public void notificarProcesoTerminado(ProcesoPlanificable p, long ciclo) {
    if (p != null) {
        colaTerminados.add(p);
        st(p.pid()).tFin = ciclo;

        try {
            p.setEstado(simuladorSO.modelo.EstadoProceso.TERMINATED);
        } catch (Throwable ignore) { /* si no existe setEstado, no pasa nada */ }

        señalizador.empujarEvento(
            EventoSistema.DISPATCH,
            "EXIT",
            p.nombre() + " (pid=" + p.pid() + ") @ciclo " + ciclo
        );
        reanudarSiHayHueco(ciclo);
        if (ultimoPidEnCPU != null && ultimoPidEnCPU.equals(p.pid())) {
            ultimoPidEnCPU = null;
        }
    }
}


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
    if (nuevo == null) return;

    ProcesoPlanificable corriendo = cpu.ejecutando();
    if (corriendo != null) {
        try { despachador.expropiarActual(0L); } catch (Exception ignore) {}
        planificadorActual.reencolarPorPreempcion(corriendo, 0L);
    }

    PlanificadorCortoPlazo viejo = this.planificadorActual;

    migrarColas(viejo, nuevo, 0L);

    this.planificadorActual = nuevo;
    if (gestorES != null) gestorES.setPlanificador(planificadorActual);

    señalizador.empujarEvento(EventoSistema.DISPATCH, "Política cambiada a " + nombre, "");
    System.out.println("Controlador: Política cambiada a " + nombre);

    if (señalizador instanceof SimuladorGUI gui) {
        gui.setAlgoritmoSeleccionado(nombre);
    }

    reanudarSiHayHueco(0L);
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

        st(p.pid()).t0 = (st(p.pid()).t0 < 0 ? 0L : st(p.pid()).t0);
        planificadorActual.encolar(p, 0L);
    }
    
    @Override
public void reset() {
    pause();

    synchronized (lockReset) {
        limpiarPlanificadores();

        colaTerminados.clear();
        bloqueadosGUI.clear();
        stats.clear();

        histCPU.clear();
        histTerminados.clear();
        terminadosEnVentana = 0;
        totalCiclos = 0;
        ciclosConCPUGlobal = 0;
        ciclosConCPUVentana = 0;

        señalizador.refrescarColas(java.util.List.of(), java.util.List.of(), java.util.List.of(), null, null);
        señalizador.refrescarCPU(null);
        señalizador.setTextoMetricas("—");

        resetPendiente = estaCorriendo(); 
    }
}



    public boolean consumirResetPendiente() {
        synchronized (lockReset) {
            if (resetPendiente) { resetPendiente = false; return true; }
            return false;
        }
    }

    @Override
    public void setQuantumRR(int q) {
        if (q <= 0) return;
        if (planificadorActual instanceof simuladorSO.planificador.PlanificadorRR rr) {
            rr.reconfigurar(Integer.valueOf(q));
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Quantum RR actualizado", "q=" + q);
            return;
        }
        PlanificadorCortoPlazo p = planificadores.get("RR");
        if (p instanceof simuladorSO.planificador.PlanificadorRR rr2) {
            rr2.reconfigurar(Integer.valueOf(q));
            señalizador.empujarEvento(EventoSistema.DISPATCH, "Quantum RR actualizado (en planificador RR)", "q=" + q);
        }
    }

@Override
public void setMLFQ(int niveles, int[] quantums) {
    if (niveles < 2) niveles = 3;
    if (quantums == null || quantums.length != niveles) quantums = new int[]{2,4,8};
    final long AGE = 20L;

    ProcesoPlanificable corriendo = cpu.ejecutando();
    if (corriendo != null) {
        try { despachador.expropiarActual(0L); } catch (Exception ignore) {}
        planificadorActual.reencolarPorPreempcion(corriendo, 0L);
    }

    PlanificadorCortoPlazo viejo = this.planificadorActual;
    PlanificadorCortoPlazo mlfq = planificadores.get("MLFQ");
    if (!(mlfq instanceof simuladorSO.planificador.PlanificadorMLFQ)) {
        mlfq = new simuladorSO.planificador.PlanificadorMLFQ(niveles, quantums, AGE);
        planificadores.put("MLFQ", mlfq);
    } else {
        mlfq.reconfigurar(quantums.clone());
    }

    migrarColas(viejo, mlfq, 0L);

    this.planificadorActual = mlfq;
    if (gestorES != null) gestorES.setPlanificador(planificadorActual);

    señalizador.empujarEvento(EventoSistema.DISPATCH,
            "Política cambiada a MLFQ",
            "niveles=" + niveles + " | q=" + java.util.Arrays.toString(quantums));

    if (señalizador instanceof SimuladorGUI gui) gui.setAlgoritmoSeleccionado("MLFQ");

    reanudarSiHayHueco(0L);
}
    @Override public void generarProcesosAleatorios(int cantidad) {}
@Override
public void guardarConfig(String path) {
    if (path == null || path.isBlank()) return;

    try (var bw = java.nio.file.Files.newBufferedWriter(
             java.nio.file.Path.of(path), java.nio.charset.StandardCharsets.UTF_8);
         var pw = new java.io.PrintWriter(bw)) {

        pw.println("# Sim config");
        pw.println("policy=" + getPlanificadorActual().politica().name());
        pw.println("ms_per_tick=" + getMsPorCiclo());

        var prr = planificadores.get("RR");
        if (prr instanceof simuladorSO.planificador.PlanificadorRR rr) {
            pw.println("quantum_rr=" + rr.getQuantum());
        } else {
            pw.println("quantum_rr=0");
        }

        pw.println("mlfq_levels=3");
        pw.println("mlfq_quantums=2,4,8");
        pw.println("mlfq_age=20");

        var listos = getPlanificadorActual().getColaListos();
        var bloqs  = getPlanificadorActual().getColaBloqueados();
        var todos  = new java.util.ArrayList<simuladorSO.modelo.ProcesoPlanificable>();
        if (listos != null) todos.addAll(listos);
        if (bloqs  != null) todos.addAll(bloqs);

        for (var p : todos) {
            String tipo = (p.tipo() == simuladorSO.modelo.TipoProceso.CPU_BOUND ? "CPU" : "IO");
            int durAprox = Math.max(1, p.pc() + p.instruccionesRestantes());
            pw.printf(java.util.Locale.US,
                "process=nombre:%s,dur:%d,tipo:%s,ioCada:%d,ioDura:%d,prio:%d,t0:%d%n",
                p.nombre(), durAprox, tipo, p.ioCadaK(), p.ioDuraM(), p.prioridad(), 0L
            );
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        señalizador.empujarEvento(simuladorSO.nucleo.EventoSistema.DISPATCH,
                "Error guardando config", ex.getMessage());
    }
}

        private void esperarAplicacionReset() {
        for (int i = 0; i < 25; i++) {         
            try { Thread.sleep(10); } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); break;
            }
            try {
                var plan = getPlanificadorActual();
                if (plan.getColaListos().isEmpty() && plan.getColaBloqueados().isEmpty()) break;
            } catch (Exception ignore) { /* no-op */ }
        }
    }

@Override
public void cargarConfig(String path) {
    if (path == null || path.isBlank()) return;

    var props = new java.util.Properties();
    var processLines = new java.util.ArrayList<String>();

    try (var br = java.nio.file.Files.newBufferedReader(
            java.nio.file.Path.of(path), java.nio.charset.StandardCharsets.UTF_8)) {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith("process=")) {
                processLines.add(line.substring("process=".length()));
            } else {
                int eq = line.indexOf('=');
                if (eq > 0) props.setProperty(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
            }
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        señalizador.empujarEvento(EventoSistema.DISPATCH, "Error leyendo config", ex.getMessage());
        return;
    }

    pause();
    reset();                     

    setPolitica(props.getProperty("policy", "FCFS"));

    try { setMsPorCiclo(Integer.parseInt(props.getProperty("ms_per_tick", "50"))); }
    catch (NumberFormatException ignore) {}

    try {
        int q = Integer.parseInt(props.getProperty("quantum_rr", "0"));
        if (q > 0) setQuantumRR(q);
    } catch (NumberFormatException ignore) {}

    for (String pl : processLines) {
        var kv     = parseProcessLine(pl);
        if (kv == null) continue;

        String nombre = kv.getOrDefault("nombre", "P_" + (System.currentTimeMillis()%1000));
        int dur       = parseIntSafe(kv.get("dur"), 10);
        String tipoS  = kv.getOrDefault("tipo", "CPU");
        int ioCada    = parseIntSafe(kv.get("ioCada"), 0);
        int ioDura    = parseIntSafe(kv.get("ioDura"), 0);
        int prio      = parseIntSafe(kv.get("prio"), 0);
        long t0       = parseLongSafe(kv.get("t0"), 0L);

        var tipo = "IO".equalsIgnoreCase(tipoS)
            ? simuladorSO.modelo.TipoProceso.IO_BOUND
            : simuladorSO.modelo.TipoProceso.CPU_BOUND;

        generarProcesoManual(nombre, dur, tipo, ioCada, ioDura, prio, t0);
    }
    suspenderSiExceso(0L);
    reanudarSiHayHueco(0L);
    javax.swing.SwingUtilities.invokeLater(() -> actualizarVistaCicloACiclo(0L, ""));
    señalizador.empujarEvento(EventoSistema.DISPATCH, "Config cargada", path);
}

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }
    private static long parseLongSafe(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception e) { return def; }
    }
    private static java.util.Map<String,String> parseProcessLine(String line) {
        java.util.Map<String,String> m = new java.util.HashMap<>();
        try {
            for (String part : line.split(",")) {
                int p = part.indexOf(':');
                if (p <= 0) continue;
                String k = part.substring(0, p).trim();
                String v = part.substring(p + 1).trim();
                m.put(k, v);
            }
            return m;
        } catch (Exception e) {
            return null;
        }
    }

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

        Stats s = st(p.pid());
        if (s.t0 < 0) s.t0 = t0;

        planificadorActual.encolar(p, t0);
        suspenderSiExceso(t0);


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
