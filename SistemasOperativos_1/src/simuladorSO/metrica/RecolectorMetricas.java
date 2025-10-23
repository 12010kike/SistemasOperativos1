/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.metrica;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author obelm
 */
public class RecolectorMetricas {
    private final List<InstantaneaMetricas> historial = new ArrayList<>();
    private long ciclos = 0;
    private long ticksOcupados = 0;
    private long procesosCompletados = 0;

    public void registrarTick(boolean cpuOcupada) {
        ciclos++;
        if (cpuOcupada) ticksOcupados++;
    }

    public void registrarFinalizacion() { procesosCompletados++; }

    public void registrarInstantanea(int listos, int bloqueados, int suspendidos) {
        double utilizacion = ciclos == 0 ? 0 : (double) ticksOcupados / ciclos;
        double throughput  = ciclos == 0 ? 0 : (double) procesosCompletados / ciclos;
        historial.add(new InstantaneaMetricas(ciclos, utilizacion, throughput,
                                              listos, bloqueados, suspendidos));
    }

    public List<InstantaneaMetricas> getHistorial() { return historial; }
    public double getUtilizacionFinal() { return ciclos == 0 ? 0 : (double) ticksOcupados / ciclos; }
    public double getThroughputFinal()  { return ciclos == 0 ? 0 : (double) procesosCompletados / ciclos; }
    public long getCiclos() { return ciclos; }
}

