/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author eabdf
 */
public interface PCBVista {
    long pid(); 
    String nombre(); 
    EstadoProceso estado(); 
    int pc(); 
    int mar();
    int totalInstrucciones(); 
    int instruccionesRestantes(); 
    TipoProceso tipo();
    int ioCadaK(); 
    int ioDuraM(); 
    int prioridad(); 
    long cicloLlegada();
}
