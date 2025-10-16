/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.modelo;

/**
 *
 * @author obelm
 */
public class ImplementacionSolicitudes implements Solicitudes {
    private final long cicloSolicitud;
    private final int duracion;
    private final long pid;

    public ImplementacionSolicitudes (long cicloSolicitud, int duracion, long pid) {
        this.cicloSolicitud = cicloSolicitud;
        this.duracion = duracion;
        this.pid = pid;
    }

    @Override public long cicloSolicitud() { return cicloSolicitud; }
    @Override public int duracionCiclos() { return duracion; }
    @Override public long pid() { return pid; }
}
