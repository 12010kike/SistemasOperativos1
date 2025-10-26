/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.ed;
import java.util.Iterator;
import java.util.NoSuchElementException;

import java.util.List;
import java.util.ArrayList;

/**
 *
 * @author eabdf
 * @param <T>
 */

// Modificado por Santiago. Dejo todos estos comentarios para
// saber lo que cambié yo del código

public class ColaPrincipal<T> implements ColaPrioridad<T>{
   private Object[] heap = new Object[8];
    private int size = 0;
    private final ComparadorClave<? super T> cmp;

    public ColaPrincipal(ComparadorClave<? super T> comparador) {
        this.cmp = comparador;
    }

 
    @Override public void insertar(T x) {
        ensure(size + 1);
        heap[size] = x;
        siftUp(size++);
    }

    @Override public T extraer() {
        if (size == 0) return null;
        T top = (T) heap[0];
        heap[0] = heap[--size];
        heap[size] = null;
        if (size > 0) siftDown(0);
        return top;
    }

    @Override public T cima() { return size == 0 ? null : (T) heap[0]; }

    
    @Override public void reconstruir() {
        for (int i = (size >>> 1) - 1; i >= 0; i--) siftDown(i);
    }


    @Override public int tamano() { return size; }
    @Override public boolean vacia() { return size == 0; }
    @Override public void limpiar() { for (int i = 0; i < size; i++) heap[i] = null; size = 0; }

    
    @Override public Iterator<T> iterator() {
        return new Iterator<T>() {
            int i = 0;
            @Override public boolean hasNext() { return i < size; }
            @Override public T next() {
                if (i >= size) throw new NoSuchElementException();
                return (T) heap[i++];
            }
        };
    }

 
    private void siftUp(int i) {
        while (i > 0) {
            int p = (i - 1) >>> 1;
            if (cmp.comparar((T) heap[i], (T) heap[p]) < 0) { swap(i, p); i = p; }
            else break;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int l = (i << 1) + 1;
            if (l >= size) break;
            int r = l + 1;
            int m = (r < size && cmp.comparar((T) heap[r], (T) heap[l]) < 0) ? r : l;
            if (cmp.comparar((T) heap[m], (T) heap[i]) < 0) { swap(i, m); i = m; }
            else break;
        }
    }

    private void swap(int a, int b) { Object t = heap[a]; heap[a] = heap[b]; heap[b] = t; }

    private void ensure(int need) {
        if (need <= heap.length) return;
        Object[] n = new Object[heap.length << 1];
        System.arraycopy(heap, 0, n, 0, size);
        heap = n;
    } 
    public List<T> toList() {
        List<T> listaParaGUI = new ArrayList<>();
        for (int i = 0; i < this.size; i++) {
            listaParaGUI.add((T) this.heap[i]);
        }
        return listaParaGUI;
    }
}
