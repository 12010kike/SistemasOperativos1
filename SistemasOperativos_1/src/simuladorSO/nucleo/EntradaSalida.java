/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.ed.Cola;
import simuladorSO.ed.ColaEnlazada;
/**
 *
 * @author eabdf
 */
public class EntradaSalida implements Runnable {
   interface TrabajoES {
        int duracionCiclos();
        void onComplete(long timestampFinMs);
    }

    private static final class Peticion {
        final TrabajoES trabajo;
        Peticion(TrabajoES t) { this.trabajo = t; }
    }

    private final String nombre;
    private final Thread hilo;
    private final Cola<Peticion> cola = new ColaEnlazada<>();
    private final Object notEmpty = new Object();

    private volatile boolean activo = true;
    private volatile int msPorCiclo = 50;

    EntradaSalida(String nombre) {
        this.nombre = nombre;
        this.hilo = new Thread(this, nombre);
        this.hilo.setDaemon(true);
    }

    void setMsPorCiclo(int ms) {
        this.msPorCiclo = Math.max(1, ms);
    }

    void iniciar() { hilo.start(); }

    void detener() {
        activo = false;
        synchronized (notEmpty) { notEmpty.notifyAll(); }
        hilo.interrupt();
    }

    void encolar(TrabajoES t) {
        cola.ofrecer(new Peticion(t));
        synchronized (notEmpty) { notEmpty.notifyAll(); }
    }

    @Override
    public void run() {
        while (activo) {
            try {
                synchronized (notEmpty) {
                    while (activo && cola.vacia()) notEmpty.wait();
                    if (!activo) break;
                }
                Peticion p = cola.sacar();
                if (p == null) continue;

                int d = Math.max(1, p.trabajo.duracionCiclos());
                Thread.sleep((long) d * msPorCiclo);   
                p.trabajo.onComplete(System.currentTimeMillis());

            } catch (InterruptedException ie) {
                if (!activo) break;
                Thread.currentThread().interrupt();
            }
        }
    }

    String getNombre() { return nombre; }
}
