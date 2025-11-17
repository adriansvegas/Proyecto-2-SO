/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import EDD.Hashmap;

/**
 *
 * @author Edgar
 */
public class TablaAsignacion {

    // Mapea: Ruta Completa (String) -> Objeto Archivo
    // Ej: "/home/usuario/texto.txt" -> Objeto Archivo
    private Hashmap<String, Archivo> tabla;

    public TablaAsignacion() {
        this.tabla = new Hashmap<>();
    }

    public void registrarArchivo(String rutaCompleta, Archivo archivo) {
        tabla.put(rutaCompleta, archivo);
    }

    public Archivo obtenerArchivo(String rutaCompleta) {
        return tabla.get(rutaCompleta);
    }

    public void eliminarRegistro(String rutaCompleta) {
        tabla.remove(rutaCompleta);
    }
    
    public Hashmap<String, Archivo> getTabla() {
        return tabla;
    }
    
    /**
     * Verifica si existe un archivo con esa ruta.
     */
    public boolean existe(String rutaCompleta) {
        return tabla.containsKey(rutaCompleta);
    }
}
