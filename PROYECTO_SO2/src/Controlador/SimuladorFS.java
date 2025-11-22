/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Controlador;
import EDD.Cola;
import EDD.Arraylist;
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
    private PlanifcadorDisco planificador;
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
    private Directorio navegarDirectorio(String ruta) {
        // Simplificación: Si la ruta es "/root/carpeta/archivo.txt", devuelve el obj Directorio "carpeta"
        // Asumimos que la ruta empieza con "root/"
        String[] partes = ruta.split("/");
        if (partes.length <= 1) return raiz; // Es raíz

        Directorio actual = raiz;
        // Iteramos hasta el penúltimo elemento (el padre del objetivo)
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
            if (!encontrado) return null; // Ruta no existe
        }
        return actual;
    }

    private void crearArchivoFisico(String nombre, Directorio padre, int tamano, String creador, String rutaCompleta) {
        if (disco.contarBloquesLibres() < tamano) {
            System.err.println("Error: Espacio insuficiente en disco.");
            return;
        }

        int bloquesAsignados = 0;
        int anteriorIndex = -1;
        int primerBloqueIndex = -1;

        // Asignación Encadenada
        for (int i = 0; i < disco.getCantidadBloques() && bloquesAsignados < tamano; i++) {
            if (!disco.getBloque(i).estaOcupado()) {
                if (primerBloqueIndex == -1) primerBloqueIndex = i;
                
                if (anteriorIndex != -1) {
                    disco.getBloque(anteriorIndex).setSiguienteBloque(i);
                }
                
                // Marcar bloque actual
                // Asumimos ID de archivo simple basado en hashCode del nombre para color
                disco.getBloque(i).ocupar(nombre.hashCode(), Bloque.FIN_DE_ARCHIVO); 
                
                anteriorIndex = i;
                bloquesAsignados++;
            }
        }

        if (bloquesAsignados == tamano) {
            Archivo nuevoArchivo = new Archivo(nombre, padre, primerBloqueIndex, tamano, creador);
            padre.agregarHijo(nuevoArchivo);
            tablaAsignacion.registrarArchivo(rutaCompleta, nuevoArchivo);
        } else {
            // Rollback si falló algo (no debería si chequeamos espacio antes)
            System.err.println("Error crítico en asignación.");
        }
    }

    private void eliminarArchivoFisico(String rutaCompleta, Directorio padre) {
        Archivo archivo = tablaAsignacion.obtenerArchivo(rutaCompleta);
        if (archivo == null) return;

        // Liberar bloques en disco
        int actual = archivo.getPrimerBloque();
        while (actual != Bloque.FIN_DE_ARCHIVO && actual >= 0) {
            Bloque b = disco.getBloque(actual);
            int siguiente = b.getSiguienteBloque();
            b.liberar();
            actual = siguiente;
        }

        // Eliminar de estructuras lógicas
        padre.eliminarHijo(archivo);
        tablaAsignacion.eliminarRegistro(rutaCompleta);
    }

    // Getters/Setters
    public Disco getDisco() { return disco; }
    public Directorio getRaiz() { return raiz; }
    public Cola<ProcesoIO> getColaProcesos() { return colaProcesos; }
    public void setPlanificador(PlanifcadorDisco p) { this.planificador = p; }
    public ModoUsuario getModoUsuario() { return modoUsuario; }
    public void setModoUsuario(ModoUsuario m) { this.modoUsuario = m; }
    public TablaAsignacion getTablaAsignacion() { return tablaAsignacion; }
}
