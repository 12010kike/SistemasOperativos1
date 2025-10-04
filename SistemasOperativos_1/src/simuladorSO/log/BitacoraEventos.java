/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.log;
import simuladorSO.nucleo.EventoSistema;
import java.util.List;
/**
 *
 * @author obelm
 */
public interface BitacoraEventos {
    void agregar(long ciclo, EventoSistema tipo, String msg, String det);
    List<RegistroEvento> todos();
}
