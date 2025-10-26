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
    
    public List<T> toList() {
        List<T> listaParaGUI = new ArrayList<>();
        
        Nodo<T> actual = this.head;
        while (actual != null) {
            listaParaGUI.add(actual.v);
            actual = actual.next;
        }
        
        return listaParaGUI;
    }
    public boolean contiene(T elemento) {
        Nodo<T> actual = this.head; 
        while (actual != null) {
            if (actual.v.equals(elemento)) {
                return true;
            }
            actual = actual.next; 
        }
        return false;
    }


    public void remover(T elemento) {
        if (head == null) return; 

        if (head.v.equals(elemento)) { 
            head = head.next; 
            if (head != null) {
                head.prev = null;
            } else {
                tail = null; 
            }
            size--;
            return;
        }

        Nodo<T> actual = this.head;
        while (actual.next != null) { 
            if (actual.next.v.equals(elemento)) { 
                if (actual.next == tail) {
                    tail = actual;
                }
                actual.next = actual.next.next;
                if (actual.next != null) {
                    actual.next.prev = actual;
                }
                size--;
                return;
            }
            actual = actual.next; 
        }
    }
}