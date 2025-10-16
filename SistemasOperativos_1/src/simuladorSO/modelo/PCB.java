/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */
public class PCB implements PCBVista{
    private final String nombre;
    private final int totalInstrucciones;
    private final TipoProceso tipo;
    private final int ioCadaK;
    private final int ioDuraM;
    private int prioridad;
    private final long cicloLlegada;
    private final long pid;
            
    private volatile EstadoProceso estado;
    private volatile int pc;
    private volatile int mar;
    private volatile int restantes;

    public PCB(long pid, String nombre, int totalInstrucciones,
               TipoProceso tipo, int ioCadaK, int ioDuraM,
               int prioridad, long cicloLlegada) {
        this.pid = pid;
        this.nombre = nombre;
        this.totalInstrucciones = totalInstrucciones;
        this.tipo = tipo;
        this.ioCadaK = ioCadaK;
        this.ioDuraM = ioDuraM;
        this.prioridad = prioridad;
        this.cicloLlegada = cicloLlegada;

        this.estado = EstadoProceso.NEW;
        this.pc = 0;
        this.mar = 0;
        this.restantes = totalInstrucciones;
    }

    public void setEstado(EstadoProceso nuevo) { this.estado = nuevo; }
    public void setPrioridad(int nueva) { this.prioridad = nueva; }

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

    public void avanzarInstruccion() {
        if (restantes > 0) {
            pc++;
            mar++;
            restantes--;
        }

    
}
}