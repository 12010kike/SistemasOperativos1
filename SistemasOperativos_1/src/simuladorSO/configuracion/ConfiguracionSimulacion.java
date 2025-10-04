/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.configuracion;

/**
 *
 * @author obelm
 */
public interface ConfiguracionSimulacion {
    long tickMs(); void setTickMs(long ms);
    String politica(); void setPolitica(String nombre);
    int quantumRR(); void setQuantumRR(int q);
    int nivelesMLFQ(); int[] quantumsMLFQ(); void setMLFQ(int niveles, int[] quantums);
    int capacidadMemoria(); void setCapacidadMemoria(int unidades);


}
