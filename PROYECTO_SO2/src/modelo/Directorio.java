/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;
import EDD.Arraylist;

/**
 *
 * @author Edgar
 */
public class Directorio extends NodoFS {

    // Lista de hijos (pueden ser Archivos u otros Directorios)
    private Arraylist<NodoFS> hijos;

    public Directorio(String nombre, Directorio padre) {
        super(nombre, padre);
        this.hijos = new Arraylist<>();
    }

    public void agregarHijo(NodoFS nodo) {
        hijos.add(nodo);
    }

    public void eliminarHijo(NodoFS nodo) {
        // Usamos la lógica de tu Arraylist para remover objetos
        // Nota: Arraylist.remove(Object) debe estar implementado correctamente en fase 1
        // Si tu Arraylist solo tiene remove(int index), necesitaremos buscar el índice primero.
        int index = hijos.indexOf(nodo);
        if (index != -1) {
            hijos.remove(index);
        }
    }

    public Arraylist<NodoFS> getHijos() {
        return hijos;
    }

    @Override
    public int getTamanoEnBloques() {
        return 0; // Simplificación: Directorios son lógica, no ocupan bloques de datos en esta simulación
    }
}
