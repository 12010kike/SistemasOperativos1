/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.gui;

/**
 *
 * @author obelm
 */
public interface ControladorSimulador {
    void play(); 
    void pause(); 
    void reset();
    void setTickMs(long ms); 
    void setPolitica(String nombre);
    void setQuantumRR(int q); 
    void setMLFQ(int niveles, int[] quantums);
    void generarProcesosAleatorios(int cantidad);
    void cargarConfig(String path); 
    void guardarConfig(String path);
}
