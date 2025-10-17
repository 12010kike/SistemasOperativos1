/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.ed;
import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 *
 * @author eabdf
 * @param <T>
 */
public class ColaDobleEnlazada<T> implements ColaDoble<T> {
   private static final class Nodo<E> {
        E v; Nodo<E> prev, next;
        Nodo(E v) { this.v = v; }
    }

    private Nodo<T> head, tail;
    private int size;

    @Override public void agregarPrimero(T x) {
        Nodo<T> n = new Nodo<>(x);
        if (head == null) head = tail = n;
        else { n.next = head; head.prev = n; head = n; }
        size++;
    }

    @Override public void agregarUltimo(T x) {
        Nodo<T> n = new Nodo<>(x);
        if (tail == null) head = tail = n;
        else { tail.next = n; n.prev = tail; tail = n; }
        size++;
    }

    @Override public T sacarPrimero() {
        if (head == null) return null;
        T v = head.v;
        head = head.next;
        if (head != null) head.prev = null; else tail = null;
        size--;
        return v;
    }

    @Override public T sacarUltimo() {
        if (tail == null) return null;
        T v = tail.v;
        tail = tail.prev;
        if (tail != null) tail.next = null; else head = null;
        size--;
        return v;
    }

    @Override public T verPrimero() { return head == null ? null : head.v; }
    @Override public T verUltimo()  { return tail == null ? null : tail.v; }

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
}
