/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;
import EDD.Cola;
import EDD.Arraylist;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import modelo.*;
import planificaciondisco.*;
/**
 *
 * @author adria
 */

/**
 * Controlador Principal. Orquesta Disco, FAT y Planificación.
 */

public class SimuladorFS {
    private Disco disco;
    private TablaAsignacion tablaAsignacion;
    private Directorio raiz;
    
    private Cola<ProcesoIO> colaProcesos;           
    private Arraylist<ProcesoIO> procesosHistoricos; 
    private ProcesoIO procesoEnEjecucion = null; 
    
    private PlanificadorDisco planificador;
    private int cabezal;
    private ModoUsuario modoUsuario;
    
    // --- SEGURIDAD ---
    private String passwordAdmin = null; // Inicia SIN contraseña

    private final String[] NOMBRES = {"system", "data", "log", "config", "user", "bin", "temp", "swap"};
    private final String[] EXTS = {".dat", ".log", ".sys", ".txt", ".bin"};

    public SimuladorFS() {
        this.disco = new Disco();
        this.tablaAsignacion = new TablaAsignacion();
        this.raiz = new Directorio("root", null);
        this.colaProcesos = new Cola<>();
        this.procesosHistoricos = new Arraylist<>();
        this.planificador = new PlanificadorFIFO(); 
        this.cabezal = 0;
        this.modoUsuario = ModoUsuario.USUARIO; 
        
        // Sistema ocupa el bloque 0
        disco.getBloque(0).ocupar(0, Bloque.FIN_DE_ARCHIVO);
    }

    // --- GESTIÓN DE SEGURIDAD ---
    public boolean isPasswordSet() {
        return passwordAdmin != null && !passwordAdmin.isEmpty();
    }

    public void setPasswordAdmin(String newPass) {
        this.passwordAdmin = newPass;
    }

    public boolean loginAdmin(String input) {
        if (passwordAdmin == null) return false;
        return passwordAdmin.equals(input);
    }

    public boolean cambiarPasswordAdmin(String oldPass, String newPass) {
        if (loginAdmin(oldPass)) {
            this.passwordAdmin = newPass;
            return true;
        }
        return false;
    }

    // --- VALIDACIONES (AQUÍ ESTABA EL ERROR) ---
    
    public boolean existeArchivo(String rutaCompleta) {
        return tablaAsignacion.obtenerArchivo(rutaCompleta) != null;
    }
    
    public boolean esDirectorio(String ruta) {
        if (ruta.equals("root")) return true;
        Directorio d = navegar(ruta);
        return d != null; 
    }

    // [CORRECCIÓN] Este es el método que faltaba y causaba el error
    public boolean existeElemento(String ruta) {
        return existeArchivo(ruta) || esDirectorio(ruta);
    }

    // --- CARGA MASIVA ---
    public void generarCargaAleatoria(String rutaBase) {
        this.procesosHistoricos = new Arraylist<>();
        this.colaProcesos = new Cola<>(); 
        this.procesoEnEjecucion = null; 
        
        Random rand = new Random();
        int cantidad = 10; 
        
        if (rutaBase == null || rutaBase.trim().isEmpty()) rutaBase = "root";
        
        // Si seleccionó un archivo, usamos su carpeta padre
        if (existeArchivo(rutaBase)) {
            if (rutaBase.contains("/")) rutaBase = rutaBase.substring(0, rutaBase.lastIndexOf("/"));
            else rutaBase = "root";
        }
        
        for (int i = 0; i < cantidad; i++) {
            int tipo = rand.nextInt(100);
            String nombre = NOMBRES[rand.nextInt(NOMBRES.length)] + "_" + rand.nextInt(99) + EXTS[rand.nextInt(EXTS.length)];
            String ruta = rutaBase + "/" + nombre;
            int duracion = 3 + rand.nextInt(5); 

            // Usamos el método corregido para validar
            if (!existeElemento(ruta)) {
                if (tipo < 60) {
                    int tam = 1 + rand.nextInt(4);
                    agregarProceso("auto", ProcesoIO.Operacion.CREAR_ARCHIVO, ruta, tam, duracion);
                } else if (tipo < 80) {
                    agregarProceso("auto", ProcesoIO.Operacion.ELIMINAR_ARCHIVO, ruta, 0, duracion);
                } else {
                    String nombreDir = "dir_" + rand.nextInt(100);
                    // Validamos también para directorios
                    if (!existeElemento(rutaBase + "/" + nombreDir)) {
                        agregarProceso("auto", ProcesoIO.Operacion.CREAR_DIR, rutaBase + "/" + nombreDir, 0, duracion);
                    }
                }
            }
        }
    }

    public void agregarProceso(String usuario, ProcesoIO.Operacion op, String ruta, int tamano, int duracion) {
        int cil = 0;
        if (op == ProcesoIO.Operacion.CREAR_ARCHIVO) cil = disco.buscarBloqueLibre();
        else {
             Archivo a = tablaAsignacion.obtenerArchivo(ruta);
             if(a != null) cil = a.getPrimerBloque();
        }
        if(cil == -1) cil = 0;
        
        ProcesoIO p = new ProcesoIO(usuario, op, ruta, tamano, cil, duracion);
        p.setEstado(ProcesoIO.Estado.LISTO);
        
        colaProcesos.add(p);       
        procesosHistoricos.add(p); 
    }

    // --- MOTOR DE EJECUCIÓN ---
    public ProcesoIO ejecutarCiclo() {
        if (procesoEnEjecucion == null) {
            if (colaProcesos.isEmpty()) return null;
            procesoEnEjecucion = planificador.seleccionarSiguiente(colaProcesos, cabezal);
        }

        if (procesoEnEjecucion != null) {
            procesoEnEjecucion.setEstado(ProcesoIO.Estado.EJECUCION);
            procesoEnEjecucion.ejecutarPaso();
            cabezal = procesoEnEjecucion.getCilindroPeticion();

            if (procesoEnEjecucion.esTerminado()) {
                procesarSolicitud(procesoEnEjecucion);
                procesoEnEjecucion.setEstado(ProcesoIO.Estado.TERMINADO);
                ProcesoIO terminado = procesoEnEjecucion;
                procesoEnEjecucion = null; 
                return terminado;
            }
            return procesoEnEjecucion;
        }
        return null;
    }

    private void procesarSolicitud(ProcesoIO proc) {
        String ruta = proc.getRutaObjetivo();
        String[] partes = ruta.split("/");
        String nombre = partes[partes.length - 1];
        
        String rutaPadre = ruta.contains("/") ? ruta.substring(0, ruta.lastIndexOf("/")) : "root";
        Directorio padre = navegar(rutaPadre); 
        
        if (padre == null) return;

        switch (proc.getOperacion()) {
            case CREAR_ARCHIVO: 
                if (!existeArchivo(ruta)) 
                    crearArchivoFisico(nombre, padre, proc.getTamano(), proc.getUsuario(), ruta, proc.getId());
                break;
            case ELIMINAR_ARCHIVO: 
                if (existeArchivo(ruta)) eliminarArchivoFisico(ruta, padre);
                else eliminarDirectorioRecursivo(nombre, padre, ruta);
                break;
            case CREAR_DIR: 
                if (!existeElemento(ruta)) padre.agregarHijo(new Directorio(nombre, padre)); 
                break;
            case ELIMINAR_DIR:
                eliminarDirectorioRecursivo(nombre, padre, ruta);
                break;
        }
    }
    
    private void crearArchivoFisico(String n, Directorio p, int t, String u, String r, int pidProceso) {
         if (disco.contarBloquesLibres() < t) return;
         int start = -1; 
         int anterior = -1;

         for(int i=0; i<t; i++) {
             int b = disco.buscarBloqueLibre();
             if(b != -1) {
                 if (anterior != -1) disco.getBloque(anterior).setSiguienteBloque(b);
                 disco.getBloque(b).ocupar(pidProceso, Bloque.FIN_DE_ARCHIVO);
                 if(start == -1) start = b;
                 anterior = b;
             }
         }
         if(start != -1) {
             Archivo arch = new Archivo(n, p, start, t, u);
             p.agregarHijo(arch);
             tablaAsignacion.registrarArchivo(r, arch);
         }
    }

    private void eliminarArchivoFisico(String r, Directorio p) {
        Archivo a = tablaAsignacion.obtenerArchivo(r);
        if(a!=null) {
            int actual = a.getPrimerBloque();
            while(actual != -1 && actual >= 0) {
                Bloque b = disco.getBloque(actual);
                int sig = b.getSiguienteBloque();
                b.liberar();
                actual = sig;
            }
            tablaAsignacion.eliminarRegistro(r);
            p.eliminarHijo(a);
        }
    }
    
    private void eliminarDirectorioRecursivo(String nombreDir, Directorio padre, String rutaDir) {
        Directorio aBorrar = null;
        Arraylist<NodoFS> hijosPadre = padre.getHijos();
        for(int i=0; i<hijosPadre.size(); i++) {
            if(hijosPadre.get(i).getNombre().equals(nombreDir) && hijosPadre.get(i) instanceof Directorio) {
                aBorrar = (Directorio) hijosPadre.get(i);
                break;
            }
        }
        if (aBorrar == null) return;

        Object[] contenido = aBorrar.getHijos().toArray();
        for (Object obj : contenido) {
            NodoFS nodo = (NodoFS) obj;
            String subRuta = rutaDir + "/" + nodo.getNombre();
            if (nodo instanceof Archivo) eliminarArchivoFisico(subRuta, aBorrar);
            else if (nodo instanceof Directorio) eliminarDirectorioRecursivo(nodo.getNombre(), aBorrar, subRuta);
        }
        padre.eliminarHijo(aBorrar);
    }

    public void renombrarArchivo(String rutaVieja, String nuevoNombre) {
        Archivo archivo = tablaAsignacion.obtenerArchivo(rutaVieja);
        if (archivo == null) return;
        
        tablaAsignacion.eliminarRegistro(rutaVieja);
        archivo.setNombre(nuevoNombre);
        
        String directorioPadre = rutaVieja.contains("/") ? rutaVieja.substring(0, rutaVieja.lastIndexOf("/")) : "root";
        String nuevaRuta = directorioPadre + "/" + nuevoNombre;
        tablaAsignacion.registrarArchivo(nuevaRuta, archivo);
    }

    public void guardarEstado() {
        try (FileWriter fw = new FileWriter("estado_disco.csv")) {
            fw.write("REPORTE_ESTADO_SISTEMA\n");
            fw.write("Bloques_Totales: " + disco.getCantidadBloques() + "\n");
            fw.write("Bloques_Libres: " + disco.contarBloquesLibres() + "\n");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Directorio navegar(String ruta) {
        if (ruta.equals("root") || ruta.equals("/")) return raiz;
        String[] partes = ruta.split("/");
        Directorio actual = raiz;
        int start = partes[0].equals("root") ? 1 : 0;
        for (int i = start; i < partes.length; i++) {
            boolean encontrado = false;
            Arraylist<NodoFS> hijos = actual.getHijos();
            for (int j = 0; j < hijos.size(); j++) {
                NodoFS nodo = hijos.get(j);
                if (nodo instanceof Directorio && nodo.getNombre().equals(partes[i])) {
                    actual = (Directorio) nodo;
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) return null;
        }
        return actual;
    }

    public Cola<ProcesoIO> getColaProcesos() { return colaProcesos; }
    public Arraylist<ProcesoIO> getProcesosHistoricos() { return procesosHistoricos; }
    public Disco getDisco() { return disco; }
    public Directorio getRaiz() { return raiz; }
    public TablaAsignacion getTablaAsignacion() { return tablaAsignacion; }
    public void setPlanificador(PlanificadorDisco p) { this.planificador = p; }
    public void setModoUsuario(ModoUsuario m) { this.modoUsuario = m; }
    public ModoUsuario getModoUsuario() { return modoUsuario; }
}