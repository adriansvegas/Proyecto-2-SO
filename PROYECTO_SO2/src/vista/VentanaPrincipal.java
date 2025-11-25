package vista;
import Controlador.SimuladorFS;
import EDD.Arraylist;
import EDD.Hashmap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Iterator;
import modelo.*;
import planificaciondisco.*;

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
    private JTable tablaAsignacion;
    private JTextArea logArea;
    private JComboBox<String> comboPlanificador;
    private JComboBox<ModoUsuario> comboModo;

    public VentanaPrincipal(SimuladorFS simulador) {
        this.simulador = simulador;
        setTitle("Simulador Sistema de Archivos - Proyecto 2");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
        actualizarVista();
    }

    private void initComponents() {
        JPanel panelTop = new JPanel();
        JButton btnCrearDir = new JButton("Crear Carpeta");
        JButton btnCrearArchivo = new JButton("Crear Archivo");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRenombrar = new JButton("Renombrar");
        JButton btnEjecutar = new JButton("Ejecutar (Paso)");
        JButton btnGuardar = new JButton("Guardar");
        
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"}); // Agregado C-SCAN
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        
        comboModo = new JComboBox<>(ModoUsuario.values());
        comboModo.addActionListener(e -> {
            simulador.setModoUsuario((ModoUsuario) comboModo.getSelectedItem());
            boolean isAdmin = simulador.getModoUsuario() == ModoUsuario.ADMINISTRADOR;
            btnEliminar.setEnabled(isAdmin);
            btnCrearDir.setEnabled(isAdmin);
            btnRenombrar.setEnabled(isAdmin);
        });

        btnCrearDir.addActionListener(e -> accionCrearDir());
        btnCrearArchivo.addActionListener(e -> accionCrearArchivo());
        btnEliminar.addActionListener(e -> accionEliminar());
        btnRenombrar.addActionListener(e -> accionRenombrar());
        btnGuardar.addActionListener(e -> {
            simulador.guardarEstado();
            log("Sistema guardado en CSV.");
        });
        btnEjecutar.addActionListener(e -> {
            ProcesoIO p = simulador.ejecutarCiclo();
            if (p != null) log("Procesado: " + p);
            else log("Cola vacía.");
            actualizarVista();
        });

        panelTop.add(new JLabel("Modo:")); panelTop.add(comboModo);
        panelTop.add(new JLabel("Algoritmo:")); panelTop.add(comboPlanificador);
        panelTop.add(btnCrearDir); panelTop.add(btnCrearArchivo);
        panelTop.add(btnRenombrar); panelTop.add(btnEliminar);
        panelTop.add(btnEjecutar); panelTop.add(btnGuardar);
        
        add(panelTop, BorderLayout.NORTH);

        treeDirectorios = new JTree();
        JScrollPane scrollTree = new JScrollPane(treeDirectorios);
        scrollTree.setPreferredSize(new Dimension(200, 0));

        panelDisco = new PanelDisco(simulador.getDisco());
        JScrollPane scrollDisco = new JScrollPane(panelDisco);

        tablaAsignacion = new JTable(new DefaultTableModel(new Object[]{"Ruta", "Inicio", "Tamaño", "Usuario"}, 0));
        JScrollPane scrollTabla = new JScrollPane(tablaAsignacion);
        scrollTabla.setPreferredSize(new Dimension(300, 0));

        JSplitPane splitDerecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDisco, scrollTabla);
        splitDerecho.setResizeWeight(0.6);
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, splitDerecho);
        add(splitPrincipal, BorderLayout.CENTER);

        logArea = new JTextArea(5, 20);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
    }

    private void actualizarVista() {
        DefaultMutableTreeNode rootNode = construirArbol(simulador.getRaiz());
        treeDirectorios.setModel(new DefaultTreeModel(rootNode));
        panelDisco.repaint();
        
        DefaultTableModel model = (DefaultTableModel) tablaAsignacion.getModel();
        model.setRowCount(0);
        // Llenar tabla recorriendo el arbol (ya que Hashmap iterator es complejo sin la librería)
        llenarTablaRecursivo(simulador.getRaiz(), "", model);
    }
    
    private void llenarTablaRecursivo(Directorio dir, String ruta, DefaultTableModel model) {
        Arraylist<NodoFS> hijos = dir.getHijos();
        for(int i=0; i<hijos.size(); i++) {
            NodoFS nodo = hijos.get(i);
            String nuevaRuta = ruta.equals("/") ? "/" + nodo.getNombre() : ruta + "/" + nodo.getNombre();
            if(nodo instanceof Archivo) {
                Archivo a = (Archivo) nodo;
                model.addRow(new Object[]{nuevaRuta, a.getPrimerBloque(), a.getTamanoEnBloques(), a.getCreador()});
            } else if (nodo instanceof Directorio) {
                llenarTablaRecursivo((Directorio) nodo, nuevaRuta, model);
            }
        }
    }
    
    private DefaultMutableTreeNode construirArbol(Directorio dir) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getNombre());
        Arraylist<NodoFS> hijos = dir.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            NodoFS hijo = hijos.get(i);
            if (hijo instanceof Directorio) node.add(construirArbol((Directorio) hijo));
            else node.add(new DefaultMutableTreeNode(hijo.getNombre()));
        }
        return node;
    }

    private String obtenerRutaSeleccionada() {
        TreePath path = treeDirectorios.getSelectionPath();
        if (path == null) return "root";
        Object[] nodos = path.getPath();
        StringBuilder ruta = new StringBuilder("root");
        for(int i=1; i<nodos.length; i++) ruta.append("/").append(nodos[i].toString());
        return ruta.toString();
    }

    private void accionCrearArchivo() {
        if (simulador.getModoUsuario() == ModoUsuario.USUARIO) { log("Usuario solo puede LEER o crear Procesos, no archivos directos."); return; }
        String nombre = JOptionPane.showInputDialog("Nombre:"); if(nombre==null) return;
        String tamStr = JOptionPane.showInputDialog("Tamaño:"); int tam = Integer.parseInt(tamStr);
        String ruta = obtenerRutaSeleccionada() + "/" + nombre;
        simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_ARCHIVO, ruta, tam);
        log("Cola: Crear " + nombre);
    }

    private void accionCrearDir() {
        String nombre = JOptionPane.showInputDialog("Nombre carpeta:"); if(nombre==null) return;
        String ruta = obtenerRutaSeleccionada() + "/" + nombre;
        simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_DIR, ruta, 0);
        log("Cola: Crear Dir " + nombre);
    }

    private void accionEliminar() {
        String ruta = obtenerRutaSeleccionada();
        if (ruta.equals("root")) return;
        boolean esArchivo = simulador.getTablaAsignacion().getTabla().containsKey(ruta);
        ProcesoIO.Operacion op = esArchivo ? ProcesoIO.Operacion.ELIMINAR_ARCHIVO : ProcesoIO.Operacion.ELIMINAR_DIR;
        simulador.agregarProceso("admin", op, ruta, 0);
        log("Cola: Eliminar " + ruta);
    }
    
    private void accionRenombrar() {
        String ruta = obtenerRutaSeleccionada();
        if (ruta.equals("root")) return;
        String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre:");
        if(nuevoNombre != null) {
            simulador.renombrarArchivo(ruta, nuevoNombre);
            log("Renombrado: " + ruta + " -> " + nuevoNombre);
            actualizarVista();
        }
    }

    private void cambiarPlanificador() {
        String sel = (String) comboPlanificador.getSelectedItem();
        switch (sel) {
            case "FIFO": simulador.setPlanificador(new PlanificadorFIFO()); break;
            case "SSTF": simulador.setPlanificador(new PlanificadorSSTF()); break;
            case "SCAN": simulador.setPlanificador(new PlanificadorSCAN()); break;
            case "C-SCAN": simulador.setPlanificador(new PlanificadorCSCAN()); break;
        }
        log("Planificador: " + sel);
    }

    private void log(String msg) { logArea.append(msg + "\n"); logArea.setCaretPosition(logArea.getDocument().getLength()); }
}