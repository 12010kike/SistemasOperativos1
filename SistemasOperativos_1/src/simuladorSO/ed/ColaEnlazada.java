/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.ed;
import java.util.List;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 *
 * @author eabdf
 * @param <T>
 */

// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código


public class ColaEnlazada<T> implements Cola<T> {
  private static final class Nodo<E> {
        E v; Nodo<E> prev, next;
        Nodo(E v) { this.v = v; }
    }

    private Nodo<T> head, tail;
    private int size;

    
    @Override public void ofrecer(T x) {
        Nodo<T> n = new Nodo<>(x);
        if (tail == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
        size++;
    }

    @Override public T sacar() {
        if (head == null) return null;
        T v = head.v;
        head = head.next;
        if (head != null) head.prev = null; else tail = null;
        size--;
        return v;
    }

    @Override public T ver() { return head == null ? null : head.v; }

 
    @Override public int tamano() { return size; }
    @Override public boolean vacia() { return size == 0; }
    @Override public void limpiar() { head = tail = null; size = 0; }

    
    @Override public Iterator<T> iterator() {
        return new Iterator<T>() {
            Nodo<T> cur = head;
            @Override public boolean hasNext() { return cur != null; }
            @Override public T next() {
                if (cur == null) throw new NoSuchElementException();
                T v = cur.v; cur = cur.next; return v;
            }
        };
    }  
    // --- INICIO DE MODIFICACIONES PARA LA GUI ---
    // Este método fue añadido para cumplir con el contrato de la GUI.
    // Proporciona una COPIA de los datos de la cola en un formato estándar (java.util.List)
    // para que la Vista (la GUI) pueda mostrarlos sin conocer la implementación interna de esta cola.
    // La lógica principal del simulador NO utiliza este método para gestionar el estado.
    
    public List<T> toList() {
        // 1. Se crea el ArrayList temporal que servirá como contenedor de transporte.
        List<T> listaParaGUI = new ArrayList<>();
        
        // 2. Se recorre la estructura de datos interna (la lista enlazada) desde la cabeza a la cola.
        Nodo<T> actual = this.head;
        while (actual != null) {
            // 3. Se añade cada elemento a la lista temporal.
            listaParaGUI.add(actual.v);
            actual = actual.next;
        }
        
        // 4. Se devuelve la copia en formato estándar.
        return listaParaGUI;
    }

    // --- FIN DE MODIFICACIONES PARA LA GUI ---
}
