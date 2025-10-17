/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorSO;

import simuladorSO.planificador.*;
import simuladorSO.modelo.*;


public class Main {
    public static void main(String[] args) {
        PlanificadorMLFQ mlfq = new PlanificadorMLFQ(3, new int[]{2, 4, 8}, 6);

        GeneradorProcesos gen = new GeneradorProcesos();

        ProcesoPlanificable A = gen.crearManual("A", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);
        ProcesoPlanificable B = gen.crearManual("B", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);
        ProcesoPlanificable C = gen.crearManual("C", 10, TipoProceso.CPU_BOUND, 0, 0, 0, 0L);

        mlfq.encolar(A, 0);
        mlfq.encolar(B, 0);
        mlfq.encolar(C, 0);

        ProcesoPlanificable actual = null;
        int quantumSimulado = 2;

        for (int ciclo = 0; ciclo < 15; ciclo++) {
            mlfq.reordenarColas(ciclo);

            if (actual == null) {
                actual = mlfq.seleccionarSiguiente(ciclo);
                if (actual == null) {
                    System.out.println("Ciclo " + ciclo + ": CPU ocioso");
                    continue;
                }
                System.out.println("Ciclo " + ciclo + ": CPU selecciona " + actual.nombre());
            }

            actual.ejecutarUnCiclo(ciclo);

            if (ciclo % quantumSimulado == quantumSimulado - 1) {
                mlfq.alVencerQuantum(actual, ciclo);
                actual = null;
            }
        }

        System.out.println("Fin demo MLFQ.");
    }
}
