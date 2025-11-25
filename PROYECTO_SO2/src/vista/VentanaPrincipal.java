package vista;
import Controlador.SimuladorFS;
import EDD.Arraylist;
import EDD.Cola;
import modelo.*;
import planificaciondisco.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionListener;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Edgar
 */
public class VentanaPrincipal extends JFrame {
    private SimuladorFS simulador;
    
    private JTree treeDirectorios;
    private PanelDisco panelDisco;
    private JTable tablaFAT;
    private JTable tablaProcesos; 
    private DefaultTableModel modeloProcesos;
    private JTextArea logArea;
    
    private JComboBox<String> cmbAlgoritmo;
    private JComboBox<ModoUsuario> cmbModo;
    private JButton btnCarga, btnCrearArch, btnCrearDir, btnRenombrar, btnEliminar, btnGuardar;
    private JButton btnIniciarPausar;
    private JButton btnCambiarPass;
    
    private Timer timerEjecucion;
    private final int VELOCIDAD_EJECUCION = 1000; 
    private boolean ejecutando = false;

    final Color COLOR_FONDO = new Color(18, 18, 18);
    final Color COLOR_PANEL = new Color(30, 30, 30);
    final Color COLOR_ACENTO_VERDE = new Color(0, 230, 118);
    final Color COLOR_ACENTO_AZUL = new Color(41, 121, 255);
    final Color COLOR_TEXTO = new Color(240, 240, 240);

    public VentanaPrincipal(SimuladorFS simulador) {
        this.simulador = simulador;
        timerEjecucion = new Timer(VELOCIDAD_EJECUCION, e -> ejecutarPasoAutomatico());
        configurarVentana();
        iniciarComponentes();
        actualizarVista();
        aplicarPermisosPorRol();
    }

    private void configurarVentana() {
        setTitle("Simulador SO - [Kernel Monitor]");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO);
    }

    private void iniciarComponentes() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 15));
        sidebar.setBackground(COLOR_PANEL);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlCtrls = new JPanel(new GridLayout(6, 1, 5, 5));
        pnlCtrls.setOpaque(false);
        pnlCtrls.setBorder(crearTituloBorde("CONFIGURACIÓN"));
        
        cmbModo = new JComboBox<>(ModoUsuario.values());
        estilizarCombo(cmbModo);
        
        cmbModo.addActionListener(e -> {
            ModoUsuario seleccion = (ModoUsuario) cmbModo.getSelectedItem();
            
            if (seleccion == ModoUsuario.ADMINISTRADOR) {
                if (!simulador.isPasswordSet()) {
                    JPasswordField pf = new JPasswordField();
                    int ok = JOptionPane.showConfirmDialog(this, pf, 
                        "🆕 CONFIGURACIÓN INICIAL\nCree una contraseña para el Administrador:", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    
                    if (ok == JOptionPane.OK_OPTION) {
                        String newPass = new String(pf.getPassword());
                        if (!newPass.trim().isEmpty()) {
                            simulador.setPasswordAdmin(newPass);
                            simulador.setModoUsuario(ModoUsuario.ADMINISTRADOR);
                            log("✅ Sistema inicializado. Admin configurado.");
                            aplicarPermisosPorRol();
                        } else {
                            JOptionPane.showMessageDialog(this, "La contraseña no puede estar vacía.");
                            cmbModo.setSelectedItem(ModoUsuario.USUARIO);
                        }
                    } else {
                        cmbModo.setSelectedItem(ModoUsuario.USUARIO);
                    }
                } 
                else {
                    JPasswordField pf = new JPasswordField();
                    int ok = JOptionPane.showConfirmDialog(this, pf, 
                        "🔒 SEGURIDAD\nIngrese Contraseña Admin:", 
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    
                    if (ok == JOptionPane.OK_OPTION) {
                        String pass = new String(pf.getPassword());
                        if (simulador.loginAdmin(pass)) {
                            simulador.setModoUsuario(ModoUsuario.ADMINISTRADOR);
                            log("✅ Acceso ADMIN concedido.");
                            aplicarPermisosPorRol();
                        } else {
                            JOptionPane.showMessageDialog(this, "⛔ Contraseña Incorrecta", "Error", JOptionPane.ERROR_MESSAGE);
                            cmbModo.setSelectedItem(ModoUsuario.USUARIO);
                        }
                    } else {
                        cmbModo.setSelectedItem(ModoUsuario.USUARIO);
                    }
                }
            } else {
                simulador.setModoUsuario(ModoUsuario.USUARIO);
                log("Modo cambiado a: USUARIO");
                aplicarPermisosPorRol();
            }
        });
        
        btnCambiarPass = crearBoton("🔑 Cambiar Clave", e -> accionCambiarPassword());
        btnCambiarPass.setBackground(new Color(70, 70, 70));

        cmbAlgoritmo = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        estilizarCombo(cmbAlgoritmo);
        cmbAlgoritmo.addActionListener(e -> cambiarAlgoritmo());

        JButton btnAyuda = crearBoton("ℹ Información Roles", e -> mostrarAyudaRoles());
        btnAyuda.setBackground(new Color(100, 100, 100));

        pnlCtrls.add(crearLabel("Modo Usuario:")); pnlCtrls.add(cmbModo);
        pnlCtrls.add(btnCambiarPass);
        pnlCtrls.add(crearLabel("Planificador:")); pnlCtrls.add(cmbAlgoritmo);
        pnlCtrls.add(btnAyuda);

        treeDirectorios = new JTree();
        estilizarArbol(treeDirectorios);
        JScrollPane scrollTree = new JScrollPane(treeDirectorios);
        decorarScroll(scrollTree, "Explorador (Seleccione Destino)");

        sidebar.add(pnlCtrls, BorderLayout.NORTH);
        sidebar.add(scrollTree, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(10, 0, 10, 10));

        panelDisco = new PanelDisco(simulador.getDisco());
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        decorarScroll(scrollDisco, "MAPA DE DISCO (SD)");
        scrollDisco.setPreferredSize(new Dimension(0, 280));
        
        String[] colsProc = {"PID", "Usuario", "Operación", "Destino", "Cilindro", "Estado", "Tiempo"};
        modeloProcesos = new DefaultTableModel(colsProc, 0);
        tablaProcesos = new JTable(modeloProcesos);
        estilizarTabla(tablaProcesos);
        try {
            tablaProcesos.getColumnModel().getColumn(0).setCellRenderer(new RenderizadorID());
            tablaProcesos.getColumnModel().getColumn(5).setCellRenderer(new RenderizadorEstado());
        } catch (Exception e) {}
        JScrollPane scrollProc = new JScrollPane(tablaProcesos);
        decorarScroll(scrollProc, "GESTOR DE PROCESOS (PCB)");

        String[] colsFAT = {"Ruta", "Inicio", "Tamaño", "Dueño"};
        tablaFAT = new JTable(new DefaultTableModel(colsFAT, 0));
        estilizarTabla(tablaFAT);
        JScrollPane scrollFAT = new JScrollPane(tablaFAT);
        decorarScroll(scrollFAT, "TABLA FAT");

        JSplitPane splitTablas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollProc, scrollFAT);
        splitTablas.setResizeWeight(0.60);
        splitTablas.setDividerSize(5);
        splitTablas.setBorder(null);
        splitTablas.setBackground(COLOR_FONDO);

        center.add(scrollDisco, BorderLayout.NORTH);
        center.add(splitTablas, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBackground(COLOR_FONDO);
        footer.setBorder(new EmptyBorder(0, 15, 15, 15));

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBotones.setOpaque(false);

        btnCarga = crearBoton("⚡ Carga Masiva", e -> generarCarga());
        btnCarga.setForeground(Color.ORANGE);
        
        btnIniciarPausar = crearBoton("▶ INICIAR", e -> toggleSimulacion());
        btnIniciarPausar.setBackground(COLOR_ACENTO_VERDE);
        btnIniciarPausar.setForeground(Color.BLACK);
        
        btnCrearArch = crearBoton("Nuevo Archivo", e -> crearArchivo());
        btnCrearDir = crearBoton("Nueva Carpeta", e -> crearCarpeta());
        btnRenombrar = crearBoton("Renombrar", e -> renombrar());
        btnGuardar = crearBoton("Guardar", e -> guardar());
        btnEliminar = crearBoton("Eliminar", e -> borrar());
        btnEliminar.setForeground(new Color(255, 82, 82));

        pnlBotones.add(btnCarga);
        pnlBotones.add(Box.createHorizontalStrut(10));
        pnlBotones.add(btnIniciarPausar);
        pnlBotones.add(Box.createHorizontalStrut(10));
        pnlBotones.add(btnCrearArch);
        pnlBotones.add(btnCrearDir);
        pnlBotones.add(btnRenombrar);
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnEliminar);

        logArea = new JTextArea(3, 35);
        logArea.setBackground(new Color(15, 15, 15));
        logArea.setForeground(Color.GRAY);
        logArea.setEditable(false);
        
        footer.add(pnlBotones, BorderLayout.CENTER);
        footer.add(new JScrollPane(logArea), BorderLayout.EAST);

        add(sidebar, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void bloquearControlesDuranteEjecucion(boolean bloqueado) {
        boolean activar = !bloqueado;
        cmbModo.setEnabled(activar);
        cmbAlgoritmo.setEnabled(activar);
        btnCarga.setEnabled(activar);
        btnCambiarPass.setEnabled(activar);
        
        if (bloqueado) {
            btnCrearArch.setEnabled(false);
            btnCrearDir.setEnabled(false);
            btnRenombrar.setEnabled(false);
            btnEliminar.setEnabled(false);
            btnGuardar.setEnabled(false);
        } else {
            aplicarPermisosPorRol();
        }
    }

    private void aplicarPermisosPorRol() {
        boolean isAdmin = (simulador.getModoUsuario() == ModoUsuario.ADMINISTRADOR);
        btnCrearArch.setEnabled(isAdmin);
        btnCrearDir.setEnabled(isAdmin);
        btnRenombrar.setEnabled(isAdmin);
        btnEliminar.setEnabled(isAdmin);
        btnGuardar.setEnabled(isAdmin);
        cmbAlgoritmo.setEnabled(isAdmin); 
        btnCambiarPass.setVisible(isAdmin);
    }

    private String obtenerRutaSeleccionada() {
        TreePath path = treeDirectorios.getSelectionPath();
        if (path == null) return "root";
        Object[] nodos = path.getPath();
        StringBuilder ruta = new StringBuilder("root");
        for(int i=1; i<nodos.length; i++) ruta.append("/").append(nodos[i].toString());
        return ruta.toString();
    }
    
    private String validarDestinoSeleccionado() {
        TreePath path = treeDirectorios.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "⚠️ ERROR: Seleccione una carpeta destino.", "Validación", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return obtenerRutaSeleccionada();
    }

    private void mostrarAyudaRoles() {
        JOptionPane.showMessageDialog(this, 
            "ADMIN: Control total + Gestión de Clave.\n" +
            "USUARIO: Solo lectura y Carga Masiva.", 
            "Roles", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generarCarga() {
        String ruta = obtenerRutaSeleccionada();
        simulador.generarCargaAleatoria(ruta);
        log("Carga generada en: " + ruta);
        actualizarVista();
    }
    
    private void accionCambiarPassword() {
        JPasswordField pfOld = new JPasswordField();
        int action = JOptionPane.showConfirmDialog(this, pfOld, "Contraseña ACTUAL:", JOptionPane.OK_CANCEL_OPTION);
        if (action != JOptionPane.OK_OPTION) return;
        
        String oldPass = new String(pfOld.getPassword());
        if (!simulador.loginAdmin(oldPass)) {
            JOptionPane.showMessageDialog(this, "Clave incorrecta.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JPasswordField pfNew = new JPasswordField();
        int action2 = JOptionPane.showConfirmDialog(this, pfNew, "NUEVA contraseña:", JOptionPane.OK_CANCEL_OPTION);
        if (action2 != JOptionPane.OK_OPTION) return;
        
        String newPass = new String(pfNew.getPassword());
        if (newPass.trim().isEmpty()) return;
        
        if (simulador.cambiarPasswordAdmin(oldPass, newPass)) {
            JOptionPane.showMessageDialog(this, "✅ Clave actualizada.");
            log("Seguridad: Clave de Admin cambiada.");
        }
    }

    private void toggleSimulacion() {
        if (!ejecutando) {
            timerEjecucion.start();
            btnIniciarPausar.setText("⏸ PAUSAR");
            btnIniciarPausar.setBackground(Color.YELLOW);
            bloquearControlesDuranteEjecucion(true);
            log("Simulación iniciada.");
        } else {
            timerEjecucion.stop();
            btnIniciarPausar.setText("▶ INICIAR");
            btnIniciarPausar.setBackground(COLOR_ACENTO_VERDE);
            bloquearControlesDuranteEjecucion(false);
            log("Simulación pausada.");
        }
        ejecutando = !ejecutando;
    }

    private void ejecutarPasoAutomatico() {
        ProcesoIO p = simulador.ejecutarCiclo();
        if (p != null) {
            actualizarVista();
        } else {
            if (ejecutando) toggleSimulacion(); 
            log("Cola vacía.");
        }
    }

    private void actualizarVista() {
        DefaultMutableTreeNode root = construirArbol(simulador.getRaiz());
        treeDirectorios.setModel(new DefaultTreeModel(root));
        for(int i=0;i<treeDirectorios.getRowCount();i++) treeDirectorios.expandRow(i);

        panelDisco.repaint();

        DefaultTableModel modelFAT = (DefaultTableModel) tablaFAT.getModel();
        modelFAT.setRowCount(0);
        llenarTablaFAT(simulador.getRaiz(), "", modelFAT);

        modeloProcesos.setRowCount(0);
        Arraylist<ProcesoIO> historial = simulador.getProcesosHistoricos();
        if (historial != null) {
            for(int i = 0; i < historial.size(); i++) {
                ProcesoIO p = historial.get(i);
                modeloProcesos.addRow(new Object[]{
                    p.getId(), p.getUsuario(), p.getOperacion(), p.getRutaObjetivo(),
                    p.getCilindroPeticion(), p.getEstado().toString(), p.getTiempoRestante() + "s"
                });
            }
        }
    }

    private void crearArchivo() { 
        String ruta = validarDestinoSeleccionado();
        if (ruta == null) return;
        String n = JOptionPane.showInputDialog("Nombre Archivo:");
        if(n != null && !n.trim().isEmpty()) { 
            simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_ARCHIVO, ruta+"/"+n, 2, 3);
            actualizarVista();
        }
    }
    
    private void crearCarpeta() {
        String ruta = validarDestinoSeleccionado();
        if (ruta == null) return;
        String n = JOptionPane.showInputDialog("Nombre Carpeta:");
        if(n != null && !n.trim().isEmpty()) {
            simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_DIR, ruta+"/"+n, 0, 3);
            actualizarVista();
        }
    }
    
    private void renombrar() { 
        String ruta = validarDestinoSeleccionado();
        if (ruta == null) return;
        if (ruta.equals("root")) return;
        String n = JOptionPane.showInputDialog("Nuevo Nombre:");
        if(n != null && !n.trim().isEmpty()) { 
            simulador.renombrarArchivo(ruta, n); 
            actualizarVista(); 
        }
    }
    
    private void borrar() { 
        String ruta = validarDestinoSeleccionado();
        if (ruta == null) return;
        if (ruta.equals("root")) return;
        int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar: " + ruta + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            simulador.agregarProceso("admin", ProcesoIO.Operacion.ELIMINAR_ARCHIVO, ruta, 0, 3);
            actualizarVista();
        }
    }
    
    private void guardar() {
        simulador.guardarEstado();
        log("Estado guardado.");
    }
    
    private void cambiarAlgoritmo() {
        String sel = (String) cmbAlgoritmo.getSelectedItem();
        if(sel.equals("FIFO")) simulador.setPlanificador(new PlanificadorFIFO());
        else if(sel.equals("SSTF")) simulador.setPlanificador(new PlanificadorSSTF());
        else if(sel.equals("SCAN")) simulador.setPlanificador(new PlanificadorSCAN());
        else if(sel.equals("C-SCAN")) simulador.setPlanificador(new PlanificadorCSCAN());
        log("Algoritmo: " + sel);
    }

    private void decorarScroll(JScrollPane s, String titulo) {
        s.getViewport().setBackground(COLOR_PANEL);
        s.setBackground(COLOR_PANEL);
        s.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60,60,60)), titulo,
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 11), COLOR_ACENTO_AZUL));
    }
    
    private Border crearTituloBorde(String t) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60,60,60)), t,
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 11), COLOR_ACENTO_AZUL);
    }

    private void estilizarTabla(JTable t) {
        t.setBackground(new Color(45, 45, 45));
        t.setForeground(COLOR_TEXTO);
        t.setGridColor(new Color(60, 60, 60));
        t.setRowHeight(25);
        t.getTableHeader().setBackground(new Color(30, 30, 30));
        t.getTableHeader().setForeground(COLOR_ACENTO_AZUL);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    private void estilizarArbol(JTree t) {
        t.setBackground(COLOR_PANEL);
        t.setForeground(COLOR_TEXTO);
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        r.setBackgroundNonSelectionColor(COLOR_PANEL);
        r.setTextNonSelectionColor(COLOR_TEXTO);
        r.setBackgroundSelectionColor(COLOR_ACENTO_AZUL);
        r.setTextSelectionColor(Color.WHITE);
        t.setCellRenderer(r);
    }

    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(Color.GRAY);
        return l;
    }

    private void estilizarCombo(JComboBox box) {
        box.setBackground(new Color(60, 60, 60));
        box.setForeground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
    }

    private JButton crearBoton(String t, ActionListener l) {
        JButton b = new JButton(t);
        b.setBackground(new Color(50, 50, 50));
        b.setForeground(COLOR_TEXTO);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80,80,80)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        b.addActionListener(l);
        return b;
    }

    private void log(String m) {
        logArea.append("> " + m + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private DefaultMutableTreeNode construirArbol(Directorio d) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(d.getNombre());
        Arraylist<NodoFS> h = d.getHijos();
        for(int i=0; i<h.size(); i++) {
            NodoFS node = h.get(i);
            if(node instanceof Directorio) n.add(construirArbol((Directorio)node));
            else n.add(new DefaultMutableTreeNode(node.getNombre()));
        }
        return n;
    }
    
    private void llenarTablaFAT(Directorio d, String r, DefaultTableModel m) {
        Arraylist<NodoFS> h = d.getHijos();
        for(int i=0; i<h.size(); i++) {
            NodoFS n = h.get(i);
            String path = r + "/" + n.getNombre();
            if(n instanceof Archivo) {
                Archivo a = (Archivo) n;
                m.addRow(new Object[]{path, a.getPrimerBloque(), a.getTamanoEnBloques(), a.getCreador()});
            } else if (n instanceof Directorio) llenarTablaFAT((Directorio)n, path, m);
        }
    }
}