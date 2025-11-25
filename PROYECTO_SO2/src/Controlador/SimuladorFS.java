/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;
import EDD.Cola;
import EDD.Arraylist;
import EDD.Hashmap;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import modelo.*;
import planificaciondisco.*;
/**
 *
 * @author adria
 */
public class SimuladorFS {
    private Disco disco;
    private TablaAsignacion tablaAsignacion;
    private Directorio raiz;
    private Cola<ProcesoIO> colaProcesos;
    private PlanificadorDisco planificador;
    private int cabezal;
    private ModoUsuario modoUsuario;

    public SimuladorFS() {
        this.disco = new Disco();
        this.tablaAsignacion = new TablaAsignacion();
        this.raiz = new Directorio("root", null);
        this.colaProcesos = new Cola<>();
        this.planificador = new PlanificadorFIFO(); 
        this.cabezal = 0;
        this.modoUsuario = ModoUsuario.ADMINISTRADOR;
        
        
        disco.getBloque(0).ocupar(0, Bloque.FIN_DE_ARCHIVO);
    }

    public void agregarProceso(String usuario, ProcesoIO.Operacion op, String ruta, int tamano) {
        int cilindroEstimado = 0;
        if (op == ProcesoIO.Operacion.CREAR_ARCHIVO) {
             cilindroEstimado = disco.buscarBloqueLibre();
             if (cilindroEstimado == -1) cilindroEstimado = disco.getCantidadBloques() - 1; 
        } else {
             Archivo a = tablaAsignacion.obtenerArchivo(ruta);
             if (a != null) cilindroEstimado = a.getPrimerBloque();
        }
        
        ProcesoIO p = new ProcesoIO(usuario, op, ruta, tamano, cilindroEstimado);
        p.setEstado(ProcesoIO.Estado.LISTO);
        colaProcesos.add(p);
    }

    public ProcesoIO ejecutarCiclo() {
        if (colaProcesos.isEmpty()) return null;

        ProcesoIO proc = planificador.seleccionarSiguiente(colaProcesos, cabezal);
        if (proc != null) {
            proc.setEstado(ProcesoIO.Estado.EJECUCION);
            cabezal = proc.getCilindroPeticion(); 
            procesarSolicitud(proc);
            proc.setEstado(ProcesoIO.Estado.TERMINADO);
        }
        return proc;
    }

    private void procesarSolicitud(ProcesoIO proc) {
        String ruta = proc.getRutaObjetivo();
        String[] partes = ruta.split("/");
        String nombreEntidad = partes[partes.length - 1];
        
        Directorio padre = navegarDirectorio(ruta); 

        if (padre == null) {
            System.err.println("Error: Ruta inválida " + ruta);
            return;
        }

        switch (proc.getOperacion()) {
            case CREAR_ARCHIVO:
                crearArchivoFisico(nombreEntidad, padre, proc.getTamano(), proc.getUsuario(), ruta);
                break;
            case ELIMINAR_ARCHIVO:
                eliminarArchivoFisico(ruta, padre);
                break;
            case CREAR_DIR:
                Directorio nuevoDir = new Directorio(nombreEntidad, padre);
                padre.agregarHijo(nuevoDir);
                break;
            case ELIMINAR_DIR:
                eliminarDirectorioRecursivo(nombreEntidad, padre, ruta);
                break;
        }
    }

    private Directorio navegarDirectorio(String ruta) {
        String[] partes = ruta.split("/");
        if (partes.length <= 1) return raiz; 

        Directorio actual = raiz;
        for (int i = 1; i < partes.length - 1; i++) {
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

    private void crearArchivoFisico(String nombre, Directorio padre, int tamano, String creador, String rutaCompleta) {
        if (disco.contarBloquesLibres() < tamano) {
            System.err.println("Error: Espacio insuficiente.");
            return;
        }

        int bloquesAsignados = 0;
        int anteriorIndex = -1;
        int primerBloqueIndex = -1;

        for (int i = 0; i < disco.getCantidadBloques() && bloquesAsignados < tamano; i++) {
            if (!disco.getBloque(i).estaOcupado()) {
                if (primerBloqueIndex == -1) primerBloqueIndex = i;
                
                if (anteriorIndex != -1) {
                    disco.getBloque(anteriorIndex).setSiguienteBloque(i);
                }
                
                disco.getBloque(i).ocupar(Math.abs(nombre.hashCode()), Bloque.FIN_DE_ARCHIVO); 
                anteriorIndex = i;
                bloquesAsignados++;
            }
        }

        if (bloquesAsignados == tamano) {
            Archivo nuevoArchivo = new Archivo(nombre, padre, primerBloqueIndex, tamano, creador);
            padre.agregarHijo(nuevoArchivo);
            tablaAsignacion.registrarArchivo(rutaCompleta, nuevoArchivo);
        }
    }

    private void eliminarArchivoFisico(String rutaCompleta, Directorio padre) {
        Archivo archivo = tablaAsignacion.obtenerArchivo(rutaCompleta);
        if (archivo == null) return;

        int actual = archivo.getPrimerBloque();
        while (actual != Bloque.FIN_DE_ARCHIVO && actual >= 0) {
            Bloque b = disco.getBloque(actual);
            int siguiente = b.getSiguienteBloque();
            b.liberar();
            actual = siguiente;
        }

        padre.eliminarHijo(archivo);
        tablaAsignacion.eliminarRegistro(rutaCompleta);
    }
    
 
    private void eliminarDirectorioRecursivo(String nombreDir, Directorio padre, String rutaDir) {
        // Buscar el objeto directorio
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
            if (nodo instanceof Archivo) {
                eliminarArchivoFisico(subRuta, aBorrar);
            } else if (nodo instanceof Directorio) {
                eliminarDirectorioRecursivo(nodo.getNombre(), aBorrar, subRuta);
            }
        }
      
        padre.eliminarHijo(aBorrar);
    }

   
    public void renombrarArchivo(String rutaVieja, String nuevoNombre) {
        Archivo archivo = tablaAsignacion.obtenerArchivo(rutaVieja);
        if (archivo == null) return;

        
        tablaAsignacion.eliminarRegistro(rutaVieja);
        archivo.setNombre(nuevoNombre);
        
        
        String pathPadre = rutaVieja.substring(0, rutaVieja.lastIndexOf("/"));
        String nuevaRuta = pathPadre + "/" + nuevoNombre;
        
        tablaAsignacion.registrarArchivo(nuevaRuta, archivo);
    }

    
    public void guardarEstado() {
        try (FileWriter writer = new FileWriter("filesystem_dump.csv")) {
            writer.write("RUTA,PRIMER_BLOQUE,TAMANO,CREADOR\n");
            
            guardarRecursivo(raiz, "", writer);
            System.out.println("Estado guardado en filesystem_dump.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void guardarRecursivo(Directorio dir, String rutaActual, FileWriter writer) throws IOException {
        Arraylist<NodoFS> hijos = dir.getHijos();
        for(int i=0; i<hijos.size(); i++) {
            NodoFS nodo = hijos.get(i);
            String nuevaRuta = rutaActual.equals("/") ? "/" + nodo.getNombre() : rutaActual + "/" + nodo.getNombre();
            
            if(nodo instanceof Archivo) {
                Archivo a = (Archivo) nodo;
                writer.write(String.format("%s,%d,%d,%s\n", nuevaRuta, a.getPrimerBloque(), a.getTamanoEnBloques(), a.getCreador()));
            } else if (nodo instanceof Directorio) {
                guardarRecursivo((Directorio) nodo, nuevaRuta, writer);
            }
        }
    }

    public Disco getDisco() { return disco; }
    public Directorio getRaiz() { return raiz; }
    public Cola<ProcesoIO> getColaProcesos() { return colaProcesos; }
    public void setPlanificador(PlanificadorDisco p) { this.planificador = p; }
    public ModoUsuario getModoUsuario() { return modoUsuario; }
    public void setModoUsuario(ModoUsuario m) { this.modoUsuario = m; }
    public TablaAsignacion getTablaAsignacion() { return tablaAsignacion; }
}