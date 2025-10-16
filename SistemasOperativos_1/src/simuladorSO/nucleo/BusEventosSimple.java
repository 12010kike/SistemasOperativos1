/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.log.RegistroEvento;
import java.util.function.Consumer;
/**
 *
 * @author eabdf
 */
public class BusEventosSimple implements BusEventos {
  private Consumer<RegistroEvento>[] listeners = new Consumer[8];
    private int count = 0;

    @Override
    public synchronized void publicar(RegistroEvento registro) {
        Consumer<RegistroEvento>[] copia = new Consumer[count];
        System.arraycopy(listeners, 0, copia, 0, count);
      for (Consumer<RegistroEvento> copia1 : copia) {
          try {
              copia1.accept(registro);
          }catch (Throwable t) { System.err.println("Error en listener de bus: " + t.getMessage()); }
      }
    }

    @Override
    public synchronized void suscribir(Consumer<RegistroEvento> listener) {
        if (listener == null) return;
        if (count == listeners.length) {
            Consumer<RegistroEvento>[] nuevo = new Consumer[listeners.length * 2];
            System.arraycopy(listeners, 0, nuevo, 0, listeners.length);
            listeners = nuevo;
        }
        listeners[count++] = listener;
    }

    @Override
    public synchronized void desuscribir(Consumer<RegistroEvento> listener) {
        if (listener == null) return;
        for (int i = 0; i < count; i++) {
            if (listeners[i] == listener) {
                listeners[i] = listeners[count - 1];
                listeners[count - 1] = null;
                count--;
                break;
            }
        }
    }
    public static RegistroEvento registro(long ciclo, EventoSistema tipo, String msg, String det) {
        return new RegistroEvento() {
            @Override public long ciclo() { return ciclo; }
            @Override public EventoSistema tipo() { return tipo; }
            @Override public String mensaje() { return msg; }
            @Override public String detalles() { return det; }
        };
    }
  
}
