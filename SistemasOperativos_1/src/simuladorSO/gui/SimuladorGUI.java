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
    private final javax.swing.JLabel lblMetricas = new javax.swing.JLabel("—");
    private volatile long ultimoCiclo = 0;
    private volatile String resumenMetricas = "—";
    private javax.swing.JLabel lblQuantum;
    private javax.swing.JSpinner spQuantum;
    private javax.swing.JLabel  lblMsCiclo;
    private javax.swing.JSpinner spMsCiclo;
    private javax.swing.JLabel lblMARActual;
    private javax.swing.JLabel lblStatusActual;
    private simuladorSO.metrica.GraficoMetricaSwing grafUtil;
    private volatile boolean suspendirEventosCombo = false;

    

    

    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(SimuladorGUI.class.getName());

    /**
     * Creates new form SimuladorGUI
     */
    public SimuladorGUI() {
    initComponents();
    inicializarModels();
    lblMARActual    = new javax.swing.JLabel("MAR: —");
    lblStatusActual = new javax.swing.JLabel("STATUS: —");

javax.swing.GroupLayout jPanel1Layout =
        (javax.swing.GroupLayout) jPanel1.getLayout();

jPanel1Layout.setHorizontalGroup(
    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(lblProcesoActual, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblPCActual, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(lblMARActual)     
                .addComponent(lblStatusActual)) 
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
);

jPanel1Layout.setVerticalGroup(
    jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addComponent(lblProcesoActual)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblPCActual)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblMARActual)        
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(lblStatusActual)     
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
);


    jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
    grafUtil = new simuladorSO.metrica.GraficoMetricaSwing();
    grafUtil.setTitulos("Utilización CPU", "Ciclos", "0..1");
    grafUtil.setPreferredSize(new java.awt.Dimension(260, 120));

    jPanel2.add(grafUtil);   
    jPanel2.revalidate();
    jPanel2.repaint();

    lblQuantum = new javax.swing.JLabel("Quantum RR:");
    spQuantum  = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(3, 1, 999, 1));
    lblQuantum.setVisible(false);
    spQuantum.setVisible(false);

    javax.swing.JPanel tiraRR = new javax.swing.JPanel(
        new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
    tiraRR.add(lblQuantum);
    tiraRR.add(spQuantum);

    lblMsCiclo = new javax.swing.JLabel("ms/ciclo:");
    spMsCiclo  = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(50, 1, 2000, 1));

    jPanel2.add(lblMsCiclo);
    jPanel2.add(spMsCiclo);
    jPanel2.add(tiraRR);

    setSingleChange(spMsCiclo, e -> {
        if (controlador != null) controlador.setMsPorCiclo((int) spMsCiclo.getValue());
    });

    jPanel2.revalidate();
    jPanel2.repaint();

    setSingleChange(spQuantum, e -> {
        if (controlador != null && lblQuantum.isVisible()) {
            controlador.setQuantumRR((Integer) spQuantum.getValue());
        }
    });

}

    public void setCorriendo(boolean on){
    btnIniciar.setEnabled(!on);
    btnPausar.setEnabled(on);
    comboAlgoritmos.setEnabled(!on);
    spQuantum.setEnabled(on && lblQuantum.isVisible());
    spMsCiclo.setEnabled(!on); 
}

    private void setSingleAction(javax.swing.AbstractButton b, java.awt.event.ActionListener l) {
        for (var a : b.getActionListeners()) b.removeActionListener(a);
        b.addActionListener(l);
    }

    private void setSingleChange(javax.swing.JSpinner s, javax.swing.event.ChangeListener l) {
        for (var a : s.getChangeListeners()) s.removeChangeListener(a);
        s.addChangeListener(l);
    }

public void setControlador(ControladorSimulador controlador) {
    this.controlador = controlador;
    if (controlador == null) return;

    controlador.setMsPorCiclo((int) spMsCiclo.getValue());

    String seleccion = (String) comboAlgoritmos.getSelectedItem();
    boolean esRR = "RR".equals(seleccion) || "Round Robin".equalsIgnoreCase(seleccion);
    lblQuantum.setVisible(esRR);
    spQuantum.setVisible(esRR);
    if (esRR) {
        controlador.setQuantumRR((Integer) spQuantum.getValue());
    }
}

private boolean listenersInicializados = false;

public void configurarListeners() {
    if (listenersInicializados) return;
    listenersInicializados = true;

    setSingleAction(btnIniciar, e -> {
    if (controlador != null) {
        controlador.play();
        setCorriendo(true);
    }
});
setSingleAction(btnPausar, e -> {
    if (controlador != null) {
        controlador.pause();
        setCorriendo(false);
    }
});

    setSingleAction(btnCrearProceso, e -> { if (controlador != null) mostrarDialogoNuevoProceso(); });

    for (var a : comboAlgoritmos.getActionListeners()) comboAlgoritmos.removeActionListener(a);
comboAlgoritmos.addActionListener(e -> {
    if (suspendirEventosCombo) return;   

    if (controlador == null) return;
    String seleccion = (String) comboAlgoritmos.getSelectedItem();
    controlador.setPolitica(seleccion);

    boolean esRR = "RR".equals(seleccion) || "Round Robin".equalsIgnoreCase(seleccion);
    lblQuantum.setVisible(esRR);
    spQuantum.setVisible(esRR);

    if (esRR) {
        var plan = controlador.getPlanificadorActual();
        if (plan instanceof simuladorSO.planificador.PlanificadorRR rr) {
            spQuantum.setValue(rr.getQuantum());
        }
        controlador.setQuantumRR((Integer) spQuantum.getValue());
    }
});


}

    
    private void inicializarModels() {
        modelListos = new DefaultListModel<>();
        listaListos.setModel(modelListos);

        modelBloqueados = new DefaultListModel<>();
        listaBloqueados.setModel(modelBloqueados);
        
        modelTerminados = new DefaultListModel<>();
        listaTerminados.setModel(modelTerminados);
    }
        private String pcbLine(ProcesoPlanificable p) {
        return String.format("%s(pid=%d) [%s] pc=%d mar=%d prio=%d",
                p.nombre(), p.pid(), p.estado(), p.pc(), p.mar(), p.prioridad());
    }


    @Override
    public void plotUtil(double valor01) {
        SwingUtilities.invokeLater(() -> {
            if (grafUtil != null) grafUtil.agregarPunto(valor01);
        });
    }

@Override
public void refrescarCPU(ProcesoPlanificable p) {
    javax.swing.SwingUtilities.invokeLater(() -> {
        if (p == null) {
            lblProcesoActual.setText("Proceso: Ninguno");
            lblPCActual.setText("PC: —");
            lblMARActual.setText("MAR: —");
            lblStatusActual.setText("STATUS: SO"); 
            return;
        }

        lblProcesoActual.setText("Proceso: " + p.nombre() + " (PID: " + p.pid() + ")");
        lblPCActual.setText("PC: " + p.pc());
        lblMARActual.setText("MAR: " + p.mar());

        String st = "RUNNING";
        try {
            var est = p.estado(); 
            if (est != null) {
                String n = est.name();
                if ("TERMINATED".equals(n) || n.contains("BLOCK")) st = n;
            }
        } catch (Throwable ignore) {}

        lblStatusActual.setText("STATUS: " + st);
    });
}


    
    public void setAlgoritmosDisponibles(String[] nombres) {
    comboAlgoritmos.setModel(new javax.swing.DefaultComboBoxModel<>(nombres));
}



    @Override
public void refrescarColas(List<ProcesoPlanificable> listos,
                           List<ProcesoPlanificable> bloqueados,
                           List<ProcesoPlanificable> terminados,
                           List<ProcesoPlanificable> listosSusp,
                           List<ProcesoPlanificable> bloqSusp) {
    SwingUtilities.invokeLater(() -> {
        modelListos.clear();
        for (var p : (listos != null ? listos : java.util.List.<ProcesoPlanificable>of()))
            modelListos.addElement(formateaItem(p, "READY"));

        modelBloqueados.clear();
        for (var p : (bloqueados != null ? bloqueados : java.util.List.<ProcesoPlanificable>of()))
            modelBloqueados.addElement(formateaItem(p, "BLOCKED"));

        modelTerminados.clear();
        for (var p : (terminados != null ? terminados : java.util.List.<ProcesoPlanificable>of()))
            modelTerminados.addElement(formateaItem(p, "TERMINATED"));
    });
}

private String formateaItem(ProcesoPlanificable p, String estadoFallback) {
    String estado = estadoFallback;
    try {
        var e = p.estado();               // si existe en tu modelo
        if (e != null) estado = e.name();
    } catch (Throwable ignore) {}
    return String.format("%s(pid=%d) [%s] pc=%d mar=%d",
            p.nombre(), p.pid(), estado, p.pc(), p.mar());
}


    @Override
    public void empujarEvento(EventoSistema t, String msg, String det) {
        SwingUtilities.invokeLater(() -> {
            String tipoEvento = (t != null) ? "[" + t.toString() + "] " : "";
            areaLog.append(tipoEvento + msg + (det.isEmpty() ? "" : ": " + det) + "\n");
        });
    }

    @Override
    public void setTextoMetricas(String texto) {
        resumenMetricas = (texto != null ? texto : "—");
        SwingUtilities.invokeLater(() -> 
            lblCicloActual.setText("Ciclo: " + ultimoCiclo + "  |  " + resumenMetricas)
        );
    }

    @Override
    public void actualizarMetricas(long cicloActual) {
        ultimoCiclo = cicloActual;
        SwingUtilities.invokeLater(() ->
            lblCicloActual.setText("Ciclo: " + ultimoCiclo + "  |  " + resumenMetricas)
        );
    }


    @Override
    public void refrescarConfig() {
    }
    
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
        btnReset = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnCargar = new javax.swing.JButton();
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

        comboAlgoritmos.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "FCFS", "SJF", "SRTF", "RR", "PRIORIDAD_PREEMPTIVA", "MLFQ" }));

        btnIniciar.setText("Iniciar");

        btnPausar.setText("Pausar");

        btnCrearProceso.setText("Crear proceso");

        btnReset.setText("Reset");
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnCargar.setText("Cargar");
        btnCargar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCargarActionPerformed(evt);
            }
        });

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
                .addGap(18, 18, 18)
                .addComponent(btnReset)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnGuardar)
                    .addComponent(btnCargar))
                .addContainerGap(50, Short.MAX_VALUE))
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
                    .addComponent(btnIniciar)
                    .addComponent(btnReset)
                    .addComponent(btnGuardar))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelControlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnPausar)
                    .addComponent(btnCargar))
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

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
    if (controlador != null) {
        controlador.reset();     
        setCorriendo(false);    
        areaLog.setText("");     
    }
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
    javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
    fc.setSelectedFile(new java.io.File("sim.cfg"));
    if (fc.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
        controlador.guardarConfig(fc.getSelectedFile().getAbsolutePath());
    }
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnCargarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCargarActionPerformed
    javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
    if (fc.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
        controlador.cargarConfig(fc.getSelectedFile().getAbsolutePath());
    }
    }//GEN-LAST:event_btnCargarActionPerformed

public void setAlgoritmoSeleccionado(String nombre) {
    SwingUtilities.invokeLater(() -> {
        try {
            suspendirEventosCombo = true;                
            comboAlgoritmos.setSelectedItem(nombre);
        } finally {
            suspendirEventosCombo = false;
        }

        boolean esRR = "RR".equals(nombre) || "Round Robin".equalsIgnoreCase(nombre);
        lblQuantum.setVisible(esRR);
        spQuantum.setVisible(esRR);
        if (esRR && controlador != null) {
            var plan = controlador.getPlanificadorActual();
            if (plan instanceof simuladorSO.planificador.PlanificadorRR rr) {
                spQuantum.setValue(rr.getQuantum());
            }
        }
    });
}

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
    private void mostrarDialogoNuevoProceso() {
    javax.swing.JPanel panel = new javax.swing.JPanel(new java.awt.GridBagLayout());
    java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
    gc.insets = new java.awt.Insets(4,4,4,4);
    gc.fill = java.awt.GridBagConstraints.HORIZONTAL;

    javax.swing.JTextField tfNombre = new javax.swing.JTextField("P_" + (System.currentTimeMillis() % 1000), 12);
    javax.swing.JSpinner spDur    = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(12, 1, 9999, 1));
    javax.swing.JComboBox<String> cbTipo = new javax.swing.JComboBox<>(new String[]{"CPU_BOUND","IO_BOUND"});
    javax.swing.JSpinner spIoCada = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(3, 1, 9999, 1));
    javax.swing.JSpinner spIoDura = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(2, 1, 9999, 1));
    javax.swing.JSpinner spPrio   = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
    javax.swing.JSpinner spArribo = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));

    java.awt.event.ActionListener toggleIO = e -> {
        boolean esIO = cbTipo.getSelectedIndex() == 1;
        spIoCada.setEnabled(esIO);
        spIoDura.setEnabled(esIO);
    };
    cbTipo.addActionListener(toggleIO);
    toggleIO.actionPerformed(null);

    int y = 0;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("Nombre:"), gc); gc.gridx=1; panel.add(tfNombre, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("Duración total:"), gc); gc.gridx=1; panel.add(spDur, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("Tipo:"), gc); gc.gridx=1; panel.add(cbTipo, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("I/O cada K:"), gc); gc.gridx=1; panel.add(spIoCada, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("I/O dura M:"), gc); gc.gridx=1; panel.add(spIoDura, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("Prioridad:"), gc); gc.gridx=1; panel.add(spPrio, gc); y++;
    gc.gridx=0; gc.gridy=y; panel.add(new javax.swing.JLabel("Arribo t0:"), gc); gc.gridx=1; panel.add(spArribo, gc);

    int res = javax.swing.JOptionPane.showConfirmDialog(
        this, panel, "Nuevo Proceso", javax.swing.JOptionPane.OK_CANCEL_OPTION, javax.swing.JOptionPane.PLAIN_MESSAGE
    );
    if (res != javax.swing.JOptionPane.OK_OPTION) return;

    String nombre = tfNombre.getText().trim();
    int dur       = (Integer) spDur.getValue();
    boolean esIO  = cbTipo.getSelectedIndex() == 1;
    int ioCada    = esIO ? (Integer) spIoCada.getValue() : 0;
    int ioDura    = esIO ? (Integer) spIoDura.getValue() : 0;
    int prioridad = (Integer) spPrio.getValue();
    long t0       = ((Integer) spArribo.getValue()).longValue();

    if (nombre.isEmpty()) {
        javax.swing.JOptionPane.showMessageDialog(this, "Ingresa un nombre.", "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (!esIO) { ioCada = 0; ioDura = 0; }

    simuladorSO.modelo.TipoProceso tipo = esIO
        ? simuladorSO.modelo.TipoProceso.IO_BOUND
        : simuladorSO.modelo.TipoProceso.CPU_BOUND;

    controlador.generarProcesoManual(nombre, dur, tipo, ioCada, ioDura, prioridad, t0);
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaLog;
    private javax.swing.JButton btnCargar;
    private javax.swing.JButton btnCrearProceso;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnIniciar;
    private javax.swing.JButton btnPausar;
    private javax.swing.JButton btnReset;
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
