/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */
public class GeneradorProcesos {
    private long nextPid = 1;

    public Proceso crearManual(String nombre, int totalInstr,
                               TipoProceso tipo, int ioCadaK, int ioDuraM,
                               int prioridad, long cicloLlegada) {
        long pid = nextPid++;
        PCB pcb = new PCB(pid, nombre, totalInstr, tipo, ioCadaK, ioDuraM, prioridad, cicloLlegada);
        return new Proceso(pcb);
    }

    public Proceso[] crearAleatorios(int n, long cicloLlegadaBase) {
        if (n <= 0) return new Proceso[0];

        Proceso[] arr = new Proceso[n];

        for (int i = 0; i < n; i++) {
            long pid = nextPid++;
            String nombre = "P" + pid;

            int total = 5 + (int)(Math.random() * 15);

            boolean esIO = Math.random() < 0.5;
            TipoProceso tipo = esIO ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;

            int ioCadaK = esIO ? (2 + (int)(Math.random() * 4)) : 0; 
            int ioDuraM = esIO ? (1 + (int)(Math.random() * 3)) : 0; 

            int prioridad = 1 + (int)(Math.random() * 5); 

            PCB pcb = new PCB(pid, nombre, total, tipo, ioCadaK, ioDuraM, prioridad, cicloLlegadaBase);
            arr[i] = new Proceso(pcb);
        }
        return arr;
    }
}
