/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.nucleo;
import java.util.function.Consumer;
import simuladorSO.log.RegistroEvento;
/**
 *
 * @author obelm
 */
public interface BusEventos {
 void publicar (RegistroEvento r);
 void suscribir(Consumer<RegistroEvento> l); 
 void desuscribir(Consumer<RegistroEvento> l);
}
