/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.nucleo;
import simuladorSO.modelo.ProcesoPlanificable;
/**
 *
 * @author obelm
 */
public interface CPU {
  void cargar(ProcesoPlanificable p); 
  ProcesoPlanificable descargar();
  ProcesoPlanificable ejecutando();
  void paso(long ciclo);
  boolean modoSO(); 
  void setModoSO(boolean so);  
}
