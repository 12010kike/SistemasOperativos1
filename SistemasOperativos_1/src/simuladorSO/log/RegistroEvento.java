/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.log;
import simuladorSO.nucleo.EventoSistema;
/**
 *
 * @author obelm
 */
public interface RegistroEvento {
    long ciclo(); EventoSistema tipo(); String mensaje(); String detalles(); 
}
