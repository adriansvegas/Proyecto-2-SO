/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Edgar
 */
public abstract class NodoFS {
    protected String nombre;
    protected Directorio padre;

    public NodoFS(String nombre, Directorio padre) {
        this.nombre = nombre;
        this.padre = padre;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Directorio getPadre() { return padre; }

    // Método abstracto: Archivos y Directorios calculan su tamaño distinto
    public abstract int getTamanoEnBloques();

    // toString es vital para que el JTree muestre el texto correcto
    @Override
    public String toString() {
        return nombre;
    }
}
