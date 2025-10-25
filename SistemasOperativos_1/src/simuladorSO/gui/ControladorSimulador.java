/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package simuladorSO.gui;

/**
 *
 * @author obelm
 */

import simuladorSO.modelo.TipoProceso;
import simuladorSO.planificador.PlanificadorCortoPlazo;

public interface ControladorSimulador {

    void play();
    void pause();
    void reset();


    void setMsPorCiclo(int ms);


    void setTickMs(long ms);

    /**
     * Devuelve la duración actual de ciclo en milisegundos.
     */
    int getMsPorCiclo();

    void setPolitica(String nombre);
    void setQuantumRR(int q);
    void setMLFQ(int niveles, int[] quantums);

    PlanificadorCortoPlazo getPlanificadorActual();

    void generarProcesosAleatorios(int cantidad);
    void cargarConfig(String path);
    void guardarConfig(String path);

    void generarProcesoDePrueba();

    void generarProcesoManual(String nombre,
                              int durTotal,
                              TipoProceso tipo,
                              int ioCadaK,
                              int ioDuraM,
                              int prioridad,
                              long t0);
}
