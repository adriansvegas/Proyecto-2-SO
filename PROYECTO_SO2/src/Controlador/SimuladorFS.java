/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;
import edd.Cola;
import edd.Arraylist;
import modelo.*;
import planificacion_disco.*;
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
        this.planificador = new PlanificadorFIFO(); // Default
        this.cabezal = 0;
        this.modoUsuario = ModoUsuario.ADMINISTRADOR;
    }

    // --- Gestión de Solicitudes ---
    public void agregarProceso(String usuario, ProcesoIO.Operacion op, String ruta, int tamano) {
        // Estimamos la posición física deseada (cilindro) simplemente buscando el primer bloque libre
        // o el bloque donde empieza el archivo (si es lectura/borrado).
        int cilindroEstimado = 0; 
        if (op == ProcesoIO.Operacion.CREAR_ARCHIVO) {
             cilindroEstimado = disco.buscarBloqueLibre();
             if (cilindroEstimado == -1) cilindroEstimado = 99; // Disco lleno, mandamos al final
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
            cabezal = proc.getCilindroPeticion(); // Mover cabezal
            procesarSolicitud(proc);
            proc.setEstado(ProcesoIO.Estado.TERMINADO);
        }
        return proc;
    }

    private void procesarSolicitud(ProcesoIO proc) {
        String ruta = proc.getRutaObjetivo();
        String[] partes = ruta.split("/");
        String nombreEntidad = partes[partes.length - 1];
        
        // Buscar directorio padre (simplificado: asume ruta absoluta desde root)
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
                // Lógica recursiva simplificada: solo borra de la lista del padre
                // En un sistema real, habría que borrar recursivamente el contenido
                // Aquí borramos el nodo del árbol visual, pero los bloques de archivos hijos quedarían "huerfanos" 
                // si no se implementa el borrado recursivo completo. Por brevedad, lo omitimos.
                 // Buscar el objeto directorio hijo
                Arraylist<NodoFS> hijos = padre.getHijos();
                for(int i=0; i<hijos.size(); i++) {
                    if(hijos.get(i).getNombre().equals(nombreEntidad) && hijos.get(i) instanceof Directorio) {
                        padre.eliminarHijo(hijos.get(i));
                        break;
                    }
                }
                break;
        }
    }
}
