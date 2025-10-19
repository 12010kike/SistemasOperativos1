/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package simuladorSO.gui;
import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import java.util.List;
import simuladorSO.modelo.ProcesoPlanificable;
import simuladorSO.nucleo.EventoSistema;
import simuladorSO.metrica.InstantaneaMetricas;

/**
 *
 * @author santi
 */
public class SimuladorGUI extends javax.swing.JFrame implements Señalizador {  
    private ControladorSimulador controlador;
    private DefaultListModel<String> modelListos;
    private DefaultListModel<String> modelBloqueados;
    private DefaultListModel<String> modelTerminados;
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SimuladorGUI.class.getName());

    /**
     * Creates new form SimuladorGUI
     */
    public SimuladorGUI() {
        initComponents();
        inicializarModels();
    }
public void setControlador(ControladorSimulador controlador) {
        this.controlador = controlador;
    }

    /**
     * Configura los listeners de los botones y otros componentes interactivos.
     * Este método debe ser llamado después de setControlador.
     */
    public void configurarListeners() {
        // Listener para el ComboBox de algoritmos
        comboAlgoritmos.addActionListener(e -> {
            if (controlador != null) {
                String algoritmoSeleccionado = (String) comboAlgoritmos.getSelectedItem();
                controlador.setPolitica(algoritmoSeleccionado);
            }
        });
         // Documentación: Se añaden los listeners para los botones de control de la simulación.

        // Listener para el botón Iniciar
        btnIniciar.addActionListener(e -> {
            if (controlador != null) {
                controlador.play();
            }
        });

        // Listener para el botón Pausar
        btnPausar.addActionListener(e -> {
            if (controlador != null) {
                controlador.pause();
            }
        });
                btnCrearProceso.addActionListener(e -> {
            if (controlador != null) {
                // Le pedimos al controlador que genere un nuevo proceso de prueba.
                // Usaremos un método que AÚN NO EXISTE, pero que crearemos en el siguiente paso.
                controlador.generarProcesoDePrueba(); 
            }
        });
        // --- FIN DE CÓDIGO NUEVO ---
    }
    /**
     * Inicializa los DefaultListModel y los asigna a las JList.
     * Esto nos permite añadir y quitar elementos de las listas dinámicamente.
     */
    private void inicializarModels() {
        modelListos = new DefaultListModel<>();
        listaListos.setModel(modelListos);

        modelBloqueados = new DefaultListModel<>();
        listaBloqueados.setModel(modelBloqueados);
        
        modelTerminados = new DefaultListModel<>();
        listaTerminados.setModel(modelTerminados);
    }
    
    // --- FIN DE MODIFICACIONES ---
    
    // --- INICIO DE MODIFICACIONES: IMPLEMENTACIÓN DE LA INTERFAZ Señalizador ---

    @Override
    public void refrescarCPU(ProcesoPlanificable enEjecucion) {
        SwingUtilities.invokeLater(() -> {
            if (enEjecucion != null) {
                lblProcesoActual.setText("Proceso: " + enEjecucion.nombre() + " (PID: " + enEjecucion.pid() + ")");
                lblPCActual.setText("PC: " + enEjecucion.pc());
            } else {
                lblProcesoActual.setText("Proceso: Ninguno");
                lblPCActual.setText("PC: 0");
            }
        });
    }

    @Override
    public void refrescarColas(List<ProcesoPlanificable> listos, List<ProcesoPlanificable> bloqueados,
                               List<ProcesoPlanificable> terminados, List<ProcesoPlanificable> listosSuspendidos,
                               List<ProcesoPlanificable> bloqueadosSuspendidos) {
        SwingUtilities.invokeLater(() -> {
            modelListos.clear();
            if (listos != null) {
                for (ProcesoPlanificable p : listos) { modelListos.addElement(p.toString()); }
            }

            modelBloqueados.clear();
            if (bloqueados != null) {
                for (ProcesoPlanificable p : bloqueados) { modelBloqueados.addElement(p.toString()); }
            }
            
            modelTerminados.clear();
            if (terminados != null) {
                for (ProcesoPlanificable p : terminados) { modelTerminados.addElement(p.toString()); }
            }
        });
    }

    @Override
    public void empujarEvento(EventoSistema t, String msg, String det) {
        SwingUtilities.invokeLater(() -> {
            String tipoEvento = (t != null) ? "[" + t.toString() + "] " : "";
            areaLog.append(tipoEvento + msg + (det.isEmpty() ? "" : ": " + det) + "\n");
        });
    }

    @Override
    public void actualizarMetricas(long cicloActual) { // <-- Acepta un 'long'
        SwingUtilities.invokeLater(() -> {
            // Ahora actualiza directamente el label con el número de ciclo.
            lblCicloActual.setText("Ciclo: " + cicloActual);
        });
    }

    @Override
    public void refrescarConfig() {
        // Lógica para refrescar la configuración si fuera necesario
    }
    
    // --- FIN DE MODIFICACIONES ---

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelControl = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        comboAlgoritmos = new javax.swing.JComboBox<>();
        btnIniciar = new javax.swing.JButton();
        btnPausar = new javax.swing.JButton();
        btnCrearProceso = new javax.swing.JButton();
        panelDerecho = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lblProcesoActual = new javax.swing.JLabel();
        lblPCActual = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        lblCicloActual = new javax.swing.JLabel();
        panelLog = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        areaLog = new javax.swing.JTextArea();
        panelColas = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        listaListos = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        listaBloqueados = new javax.swing.JList<>();
        jScrollPane3 = new javax.swing.JScrollPane();
        listaTerminados = new javax.swing.JList<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Algoritmo:");

        comboAlgoritmos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "SJF", "SRT", "Round Robin", "Planificación por Prioridad", "MLFQ", " " }));

        btnIniciar.setText("Iniciar");

        btnPausar.setText("Pausar");

        btnCrearProceso.setText("Crear proceso");

        javax.swing.GroupLayout panelControlLayout = new javax.swing.GroupLayout(panelControl);
        panelControl.setLayout(panelControlLayout);
        panelControlLayout.setHorizontalGroup(
            panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnPausar)
                    .addGroup(panelControlLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(comboAlgoritmos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(69, 69, 69)
                        .addComponent(btnIniciar)))
                .addContainerGap(217, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelControlLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCrearProceso)
                .addGap(202, 202, 202))
        );
        panelControlLayout.setVerticalGroup(
            panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelControlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(comboAlgoritmos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIniciar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPausar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCrearProceso)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        getContentPane().add(panelControl, java.awt.BorderLayout.NORTH);

        panelDerecho.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("CPU - En Ejecución"));

        lblProcesoActual.setText("Proceso: Ninguno");

        lblPCActual.setText("PC: 0");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProcesoActual, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPCActual, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(126, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblProcesoActual)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblPCActual)
                .addContainerGap(101, Short.MAX_VALUE))
        );

        panelDerecho.add(jPanel1, java.awt.BorderLayout.NORTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Métricas"));

        lblCicloActual.setText("Ciclo: 0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCicloActual, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(224, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblCicloActual)
                .addContainerGap(63, Short.MAX_VALUE))
        );

        panelDerecho.add(jPanel2, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelDerecho, java.awt.BorderLayout.EAST);

        panelLog.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Log de eventos"));

        areaLog.setEditable(false);
        areaLog.setColumns(20);
        areaLog.setRows(5);
        jScrollPane4.setViewportView(areaLog);

        panelLog.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        getContentPane().add(panelLog, java.awt.BorderLayout.SOUTH);

        panelColas.setLayout(new java.awt.GridLayout(1, 3, 5, 5));

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder("Lista de procesos"));

        listaListos.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(listaListos);

        panelColas.add(jScrollPane1);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Procesos bloqueados"));

        listaBloqueados.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(listaBloqueados);

        panelColas.add(jScrollPane2);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Procesos Terminados"));

        listaTerminados.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(listaTerminados);

        panelColas.add(jScrollPane3);

        getContentPane().add(panelColas, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new SimuladorGUI().setVisible(true));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaLog;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausar;
    private javax.swing.JComboBox<String> comboAlgoritmos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel lblCicloActual;
    private javax.swing.JLabel lblPCActual;
    private javax.swing.JLabel lblProcesoActual;
    private javax.swing.JList<String> listaBloqueados;
    private javax.swing.JList<String> listaListos;
    private javax.swing.JList<String> listaTerminados;
    private javax.swing.JPanel panelColas;
    private javax.swing.JPanel panelControl;
    private javax.swing.JPanel panelDerecho;
    private javax.swing.JPanel panelLog;
    // End of variables declaration//GEN-END:variables
}
