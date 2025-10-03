/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author eabdf
 */
public interface ProcesoPlanificable {
    void ejecutarUnCiclo(long ciclo); 
    boolean debeSolicitarES(long ciclo); 
    boolean completo();
    void setEstado(EstadoProceso s); 
    void reiniciarQuantum(); 
    int quantumConsumido(); 
    void setPrioridad(int p);
}
