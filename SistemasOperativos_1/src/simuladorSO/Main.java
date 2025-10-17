/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package simuladorSO;

import simuladorSO.ed.*;


public class Main {

    public static void main(String[] args) {
        Cola<Integer> cola = new ColaEnlazada<>();
        cola.ofrecer(10);
        cola.ofrecer(20);
        cola.ofrecer(30);
        System.out.println("Cola (FIFO): frente=" + cola.ver() + " sacar=" + cola.sacar() + " nuevo frente=" + cola.ver());
        System.out.println("Tamano=" + cola.tamano() + " vacia=" + cola.vacia());

       
        ColaDoble<String> colaDoble = new ColaDobleEnlazada<>();
        colaDoble.agregarPrimero("A");
       colaDoble.agregarUltimo("B");
        colaDoble.agregarUltimo("C");
        System.out.println("Cola Doble: primero=" + colaDoble.verPrimero() + ", ultimo=" + colaDoble.verUltimo());
        System.out.println("SacarPrimero=" + colaDoble.sacarPrimero() + ", nuevo primero=" + colaDoble.verPrimero());
        System.out.println("SacarUltimo=" + colaDoble.sacarUltimo() + ", nuevo ultimo=" + colaDoble.verUltimo());

      
        ColaPrioridad<Integer> pq = new ColaPrincipal<>((a, b) -> a - b);
        pq.insertar(50);
        pq.insertar(10);
        pq.insertar(30);
        pq.insertar(5);
        System.out.println("Principal: cima=" + pq.cima());
        System.out.println("Extraer1=" + pq.extraer());
        System.out.println("Extraer2=" + pq.extraer());
        System.out.println("Nueva cima=" + pq.cima());
        pq.reconstruir();
        System.out.println("Principal tamano final=" + pq.tamano());

       
    }
}

