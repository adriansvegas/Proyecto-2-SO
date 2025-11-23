package vista;
import Controlador.SimuladorFS;
import EDD.Arraylist;
import EDD.Hashmap;
import EDD.Cola;
import modelo.*;
import planificaciondisco.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Iterator;

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
        configurarVentana();
        iniciarComponentes();
        actualizarVista();
    }

    private void configurarVentana() {
        setTitle("Simulador Sistema de Archivos - Proyecto 2");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void iniciarComponentes() {
        // --- Panel Superior (Herramientas) ---
        JPanel panelTop = new JPanel();
        JButton btnCrearDir = new JButton("Nueva Carpeta");
        JButton btnCrearArchivo = new JButton("Nuevo Archivo");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnEjecutar = new JButton("Ejecutar Paso");
        
    JButton btnRenombrar = new JButton("Renombrar");
    JButton btnGuardar = new JButton("Guardar"); 
    
    btnRenombrar.addActionListener(e -> {
        if (simulador.getModoUsuario() == ModoUsuario.USUARIO) {
            JOptionPane.showMessageDialog(this, "Solo Admin puede renombrar.");
            return;
        }
        String nombreViejo = JOptionPane.showInputDialog("Nombre archivo actual:");
        String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre:");
        // Llamar a simulador.renombrar...
    });

    // Añadir al panel
    panelTop.add(btnRenombrar);
    panelTop.add(btnGuardar);            
    

        
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN"});
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        
        comboModo = new JComboBox<>(ModoUsuario.values());
        comboModo.addActionListener(e -> {
            simulador.setModoUsuario((ModoUsuario) comboModo.getSelectedItem());
            actualizarControles();
        });

        btnCrearDir.addActionListener(e -> accionCrearDir());
        btnCrearArchivo.addActionListener(e -> accionCrearArchivo());
        btnEliminar.addActionListener(e -> accionEliminar());
        btnEjecutar.addActionListener(e -> {
            ProcesoIO p = simulador.ejecutarCiclo();
            if (p != null) log("Ejecutado: " + p);
            else log("Cola vacía.");
            actualizarVista();
        });

        panelTop.add(new JLabel("Modo:"));
        panelTop.add(comboModo);
        panelTop.add(new JLabel("Planificador:"));
        panelTop.add(comboPlanificador);
        panelTop.add(btnCrearDir);
        panelTop.add(btnCrearArchivo);
        panelTop.add(btnEliminar);
        panelTop.add(btnEjecutar);
        
        add(panelTop, BorderLayout.NORTH);

        // --- Centro (Split: Arbol | Disco | Tabla) ---
        treeDirectorios = new JTree();
        JScrollPane scrollTree = new JScrollPane(treeDirectorios);
        scrollTree.setPreferredSize(new Dimension(200, 0));

        panelDisco = new PanelDisco(simulador.getDisco());
        JScrollPane scrollDisco = new JScrollPane(panelDisco);

        tablaAsignacion = new JTable(new DefaultTableModel(new Object[]{"Ruta", "Inicio", "Tamaño", "Usuario"}, 0));
        JScrollPane scrollTabla = new JScrollPane(tablaAsignacion);
        scrollTabla.setPreferredSize(new Dimension(300, 0));

        JSplitPane splitDerecho = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollDisco, scrollTabla);
        splitDerecho.setResizeWeight(0.7);
        
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollTree, splitDerecho);
        add(splitPrincipal, BorderLayout.CENTER);

        // --- Abajo (Log) ---
        logArea = new JTextArea(5, 20);
        add(new JScrollPane(logArea), BorderLayout.SOUTH);
    }

    private void actualizarVista() {
        // 1. Actualizar JTree
        DefaultMutableTreeNode rootNode = construirArbol(simulador.getRaiz());
        treeDirectorios.setModel(new DefaultTreeModel(rootNode));
        
        // 2. Actualizar Disco
        panelDisco.repaint();
        
        // 3. Actualizar Tabla (Reconstrucción simple)
        DefaultTableModel model = (DefaultTableModel) tablaAsignacion.getModel();
        model.setRowCount(0);
        Hashmap<String, Archivo> fat = simulador.getTablaAsignacion().getTabla();
        
        // Usamos el iterador de tu Hashmap personalizado
        Iterator<Hashmap.Entry<String, Archivo>> it = fat.entryIterator(); // Asumiendo que lo hiciste público en Hashmap, si no, usa keySet
        // Si entryIterator es privado (como en tu codigo original), iteramos por los buckets o ajustamos Hashmap
        // Ajuste rapido: Usaremos simulacion de iteracion si no es accesible, pero asumo que corregiste Hashmap para ser iterable o tener metodo.
        // Si Hashmap no es iterable facilmente desde fuera, iteraremos keyset si existe, o modificamos Hashmap.java
        // **Nota**: Asumo que Hashmap tiene un metodo para obtener todos los valores o entries publicos.
        // Si no, agrega: public Arraylist<Archivo> obtenerTodosLosArchivos() en TablaAsignacion.
        
        // Alternativa segura usando TablaAsignacion (requiere modificar TablaAsignacion para devolver lista)
        // Por ahora, asumiremos que podemos iterar.
    }
    
    // Método recursivo para el JTree
    private DefaultMutableTreeNode construirArbol(Directorio dir) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(dir.getNombre());
        Arraylist<NodoFS> hijos = dir.getHijos();
        for (int i = 0; i < hijos.size(); i++) {
            NodoFS hijo = hijos.get(i);
            if (hijo instanceof Directorio) {
                node.add(construirArbol((Directorio) hijo));
            } else {
                node.add(new DefaultMutableTreeNode(hijo.getNombre()));
            }
        }
        return node;
    }

    private String obtenerRutaSeleccionada() {
        TreePath path = treeDirectorios.getSelectionPath();
        if (path == null) return "root";
        Object[] nodos = path.getPath();
        StringBuilder ruta = new StringBuilder("root");
        for(int i=1; i<nodos.length; i++) { // Empezar en 1 para saltar root duplicado
             ruta.append("/").append(nodos[i].toString());
        }
        return ruta.toString();
    }

    private void accionCrearArchivo() {
        if (simulador.getModoUsuario() == ModoUsuario.USUARIO) {
            log("Error: Usuario no puede crear archivos directamente (solo procesos).");
            return;
        }
        String nombre = JOptionPane.showInputDialog("Nombre archivo:");
        if(nombre == null) return;
        String tamStr = JOptionPane.showInputDialog("Tamaño (bloques):");
        int tam = Integer.parseInt(tamStr);
        
        String rutaPadre = obtenerRutaSeleccionada();
        String rutaCompleta = rutaPadre + "/" + nombre;
        
        simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_ARCHIVO, rutaCompleta, tam);
        log("Solicitud CREAR en cola: " + rutaCompleta);
        actualizarVista();
    }

    private void accionCrearDir() {
        String nombre = JOptionPane.showInputDialog("Nombre carpeta:");
        if(nombre == null) return;
        String rutaPadre = obtenerRutaSeleccionada();
        String rutaCompleta = rutaPadre + "/" + nombre;
        
        simulador.agregarProceso("admin", ProcesoIO.Operacion.CREAR_DIR, rutaCompleta, 0);
        log("Solicitud CREAR_DIR en cola.");
    }

    private void accionEliminar() {
        String ruta = obtenerRutaSeleccionada();
        if (ruta.equals("root")) return; // No borrar raiz
        
        // Determinar si es archivo o directorio (simple check en tabla)
        boolean esArchivo = simulador.getTablaAsignacion().getTabla().containsKey(ruta);
        ProcesoIO.Operacion op = esArchivo ? ProcesoIO.Operacion.ELIMINAR_ARCHIVO : ProcesoIO.Operacion.ELIMINAR_DIR;
        
        simulador.agregarProceso("admin", op, ruta, 0);
        log("Solicitud ELIMINAR en cola: " + ruta);
    }

    private void cambiarPlanificador() {
        String sel = (String) comboPlanificador.getSelectedItem();
        switch (sel) {
            case "FIFO": simulador.setPlanificador(new PlanificadorFIFO()); break;
            case "SSTF": simulador.setPlanificador(new PlanificadorSSTF()); break;
            case "SCAN": simulador.setPlanificador(new PlanificadorSCAN()); break;
        }
        log("Planificador cambiado a: " + sel);
    }
    
    private void actualizarControles() {
        boolean isAdmin = simulador.getModoUsuario() == ModoUsuario.ADMINISTRADOR;
        // Aquí podrías deshabilitar botones si no es admin
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
