/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.ed;

/**
 *
 * @author eabdf
 * @param <T>
 */
public interface ColaPrioridad<T> extends EstructuraDeDatos<T> {void insertar(T x); T extraer(); T cima(); void reconstruir();
}
