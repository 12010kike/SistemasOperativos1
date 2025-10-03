/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author obelm
 */
public interface GestorMemoria {
    boolean intentarCargar(ProcesoPlanificable p); 
    void sacarDeMemoria(ProcesoPlanificable p);
    boolean reingresar(ProcesoPlanificable p); 
    int capacidad();
    int usado();
}
