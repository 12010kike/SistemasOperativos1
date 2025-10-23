/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.metrica;

/**
 *
 * @author obelm
 */
public class InstantaneaMetricas {
    private final long ciclo;
    private final double utilizacionCPU;
    private final double throughput;
    private final int procesosListos;
    private final int procesosBloqueados;
    private final int procesosSuspendidos;

    public InstantaneaMetricas(long ciclo, double utilizacionCPU, double throughput,
                               int listos, int bloqueados, int suspendidos) {
        this.ciclo = ciclo;
        this.utilizacionCPU = utilizacionCPU;
        this.throughput = throughput;
        this.procesosListos = listos;
        this.procesosBloqueados = bloqueados;
        this.procesosSuspendidos = suspendidos;
    }

    public long getCiclo() { return ciclo; }
    public double getUtilizacionCPU() { return utilizacionCPU; }
    public double getThroughput() { return throughput; }
    public int getProcesosListos() { return procesosListos; }
    public int getProcesosBloqueados() { return procesosBloqueados; }
    public int getProcesosSuspendidos() { return procesosSuspendidos; }
}
