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
public interface ColaDoble<T> extends EstructuraDeDatos<T> {void agregarPrimero(T x); void agregarUltimo(T x);
    T sacarPrimero(); T sacarUltimo(); T verPrimero(); T verUltimo();
    
}
