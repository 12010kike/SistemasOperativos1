/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package simuladorSO.configuracion;
import java.io.IOException; import java.nio.file.Path; import java.util.List;
/**
 *
 * @author obelm
 */
public interface GuardadorConfiguracion {
    void guardarConfig(Path archivo, ConfiguracionSimulacion cfg) throws IOException;
    void guardarProcesosCSV(Path archivo, List<EspecificacionProceso> specs) throws IOException;
}
