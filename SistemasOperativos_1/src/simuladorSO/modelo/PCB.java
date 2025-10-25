/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */
public class PCB implements ProcesoPlanificable {

    private final long pid;
    private final String nombre;

    private final int totalInstrucciones;
    private final TipoProceso tipo;
    private final int ioCadaK;      // cada K instrucciones pide E/S (solo IO_BOUND)
    private final int ioDuraM;      // duración de la E/S en ciclos (lo usa GestorES)

    private final long cicloLlegada;

    private volatile EstadoProceso estado = EstadoProceso.NEW;
    private volatile int pc = 0;
    private volatile int mar = 0;
    private volatile int restantes;

    private volatile int prioridad = 0;
    private volatile int quantumUsado = 0;

    public PCB(long pid, String nombre, int totalInstrucciones,
               TipoProceso tipo, int ioCadaK, int ioDuraM,
               int prioridad, long cicloLlegada) {

        this.pid = pid;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipo = tipo;
        this.ioCadaK = (tipo == TipoProceso.IO_BOUND) ? Math.max(1, ioCadaK) : 0;
        this.ioDuraM = (tipo == TipoProceso.IO_BOUND) ? Math.max(1, ioDuraM) : 0;
        this.prioridad = prioridad;
        this.cicloLlegada = cicloLlegada;
        this.restantes = totalInstrucciones;
    }

    // ===== PCBVista =====
    @Override public long pid() { return pid; }
    @Override public String nombre() { return nombre; }
    @Override public EstadoProceso estado() { return estado; }
    @Override public int pc() { return pc; }
    @Override public int mar() { return mar; }
    @Override public int totalInstrucciones() { return totalInstrucciones; }
    @Override public int instruccionesRestantes() { return restantes; }
    @Override public TipoProceso tipo() { return tipo; }
    @Override public int ioCadaK() { return ioCadaK; }
    @Override public int ioDuraM() { return ioDuraM; }
    @Override public int prioridad() { return prioridad; }
    @Override public long cicloLlegada() { return cicloLlegada; }

    // ===== ProcesoPlanificable =====
    @Override
    public void ejecutarUnCiclo(long ciclo) {
        if (restantes <= 0) return;
        pc++;
        mar++;
        restantes--;
        quantumUsado++;
    }

    @Override
    public boolean debeSolicitarES(long ciclo) {
        if (tipo != TipoProceso.IO_BOUND) return false;
        if (restantes <= 0) return false;
        return (ioCadaK > 0) && (pc > 0) && (pc % ioCadaK == 0);
    }

    @Override
    public boolean completo() {
        return restantes <= 0;
    }

    @Override
    public void setEstado(EstadoProceso s) {
        this.estado = s;
    }

    @Override
    public void reiniciarQuantum() {
        this.quantumUsado = 0;
    }

    @Override
    public int quantumConsumido() {
        return quantumUsado;
    }

    @Override
    public void setPrioridad(int p) {
        this.prioridad = p;
    }

    @Override
    public String toString() {
        return nombre + " (pid=" + pid + ") PC=" + pc +
               " MAR=" + mar + " Rest=" + restantes +
               (tipo == TipoProceso.IO_BOUND ? " IO[k=" + ioCadaK + ",m=" + ioDuraM + "]" : "");
    }
}
