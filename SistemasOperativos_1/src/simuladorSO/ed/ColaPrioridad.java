/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.ed;
import java.util.List;


/**
 *
 * @author eabdf
 * @param <T>
 */
// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public interface ColaPrioridad<T> extends EstructuraDeDatos<T> {void insertar(T x); T extraer(); T cima(); void reconstruir();
    List<T> toList();
}
