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

    /**
     * Parametros exactos hechos anualmente 
     * @param nombre nombre del proceso
     * @param totalInstr total de instrucciones
     * @param tipo tipo de proceso (CPU_BOUND o IO_BOUND)
     * @param ioCadaK si es IO_BOUND, cada cuántos ciclos solicita E/S (0 si no aplica)
     * @param ioDuraM duración en ciclos de la E/S (0 si no aplica)
     * @param prioridad prioridad inicial (1..N)
     * @param cicloLlegada ciclo de llegada a la simulación
     * @return Proceso creado
     */
    public Proceso crearManual(String nombre, int totalInstr,
                               TipoProceso tipo, int ioCadaK, int ioDuraM,
                               int prioridad, long cicloLlegada) {
        long pid = nextPid++;
        PCB pcb = new PCB(pid, nombre, totalInstr, tipo, ioCadaK, ioDuraM, prioridad, cicloLlegada);
        return new Proceso(pcb);
    }

    /**
     * Crea N procesos aleatorios para pruebas.
     * @param n cantidad de procesos a generar
     * @param cicloLlegadaBase ciclo de llegada común para los procesos
     * @return arreglo de procesos generados (tamaño n o 0)
     */
    public Proceso[] crearAleatorios(int n, long cicloLlegadaBase) {
        if (n <= 0) return new Proceso[0];

        Proceso[] arr = new Proceso[n];

        for (int i = 0; i < n; i++) {
            long pid = nextPid++;
            String nombre = "P" + pid;

            int total = 5 + (int)(Math.random() * 15); // 5..19 instrucciones

            boolean esIO = Math.random() < 0.5;
            TipoProceso tipo = esIO ? TipoProceso.IO_BOUND : TipoProceso.CPU_BOUND;

            int ioCadaK = esIO ? (2 + (int)(Math.random() * 4)) : 0; // 2..5
            int ioDuraM = esIO ? (1 + (int)(Math.random() * 3)) : 0; // 1..3

            int prioridad = 1 + (int)(Math.random() * 5); // 1..5

            PCB pcb = new PCB(pid, nombre, total, tipo, ioCadaK, ioDuraM, prioridad, cicloLlegadaBase);
            arr[i] = new Proceso(pcb);
        }
        return arr;
    }
}
