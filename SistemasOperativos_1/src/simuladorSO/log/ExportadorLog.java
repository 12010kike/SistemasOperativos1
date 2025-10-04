/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.log;
import java.io.IOException; import java.nio.file.Path;
/**
 *
 * @author obelm
 */
public interface ExportadorLog {
    void exportarCSV(BitacoraEventos log, Path f) throws IOException; 
    void exportarTexto(BitacoraEventos log, Path f) throws IOException;
}
