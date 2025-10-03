/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package simuladorSO.nucleo;

/**
 *
 * @author obelm
 */
public enum EventoSistema {
    TICK, ARRIVAL, DISPATCH, IO_REQUEST, IO_COMPLETE,QUANTUM_EXPIRE, 
    PREEMPT, COMPLETE, SWAP_OUT, SWAP_IN, CFG_CHANGE
}
