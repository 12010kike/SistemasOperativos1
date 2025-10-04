/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;

/**
 *
 * @author eabdf
 */
public class ImplementacionReloj implements Reloj, Runnable{
    private volatile long duracionTickMs = 500; // 0.5s por defecto
    private volatile long cicloActual = 0;
    private volatile boolean corriendo = false;

    private Thread hilo;

   
    private SuscriptorReloj[] subs = new SuscriptorReloj[8];
    private int subsCount = 0;

    @Override
    public synchronized void iniciar() {
        if (hilo == null || !hilo.isAlive()) {
            corriendo = true;
            hilo = new Thread(this, "Reloj-SO");
            hilo.setDaemon(true);
            hilo.start();
        } else {
            corriendo = true;
        }
    }

    @Override
    public void pausar() { corriendo = false; }

    @Override
    public synchronized void reiniciar() {
        corriendo = false;
        cicloActual = 0;
    }

    @Override
    public void setDuracionTickMs(long millis) {
        if (millis < 1) millis = 1;
        this.duracionTickMs = millis;
    }

    @Override
    public long getDuracionTickMs() { return duracionTickMs; }

    @Override
    public long cicloActual() { return cicloActual; }

    @Override
    public synchronized void suscribir(SuscriptorReloj s) {
        if (s == null) return;
        if (subsCount == subs.length) {
            SuscriptorReloj[] nuevo = new SuscriptorReloj[subs.length * 2];
            System.arraycopy(subs, 0, nuevo, 0, subs.length);
            subs = nuevo;
        }
        subs[subsCount++] = s;
    }

    @Override
    public synchronized void desuscribir(SuscriptorReloj s) {
        if (s == null) return;
        for (int i = 0; i < subsCount; i++) {
            if (subs[i] == s) {
                subs[i] = subs[subsCount - 1];
                subs[subsCount - 1] = null;
                subsCount--;
                break;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            if (corriendo) {
                long c = ++cicloActual;
                // snapshot de suscriptores para notificar sin bloquear
                SuscriptorReloj[] copia;
                int n;
                synchronized (this) {
                    n = subsCount;
                    copia = new SuscriptorReloj[n];
                    System.arraycopy(subs, 0, copia, 0, n);
                }
                for (int i = 0; i < n; i++) {
                    try {
                        copia[i].alTick(c);
                    } catch (Throwable t) {
                       System.err.println("Error en listener de reloj: " + t.getMessage());

                    }
                }
            }
            try { Thread.sleep(duracionTickMs); }
            catch (InterruptedException ie) 
            { Thread.currentThread().interrupt(); 
            break; }
        }
    }
}
