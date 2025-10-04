/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.gui;
import simuladorSO.nucleo.EventoSistema; 
import simuladorSO.metrica.InstantaneaMetricas;
/**
 *
 * @author obelm
 */
public interface Señalizador {
    void refrescarColas(); void refrescarCPU(); 
    void refrescarConfig();
    void empujarEvento(EventoSistema t, String msg, String det);
    void actualizarMetricas(InstantaneaMetricas snap);
}
