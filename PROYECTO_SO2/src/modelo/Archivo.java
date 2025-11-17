/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Edgar
 */


public class Archivo extends NodoFS {
    
    private int primerBloque; // Inicio de la lista enlazada en el Disco
    private int numBloques;   // Tamaño total
    private String creador;   // Usuario propietario ("admin" o nombre de usuario)
    private String extension; // Opcional, por si queremos filtrar

    public Archivo(String nombre, Directorio padre, int primerBloque, int numBloques, String creador) {
        super(nombre, padre);
        this.primerBloque = primerBloque;
        this.numBloques = numBloques;
        this.creador = creador;
        this.extension = "";
        if(nombre.contains(".")) {
            String[] partes = nombre.split("\\.");
            if(partes.length > 1) this.extension = partes[partes.length - 1];
        }
    }

    public int getPrimerBloque() { return primerBloque; }
    public void setPrimerBloque(int primerBloque) { this.primerBloque = primerBloque; }
    public String getCreador() { return creador; }

    @Override
    public int getTamanoEnBloques() {
        return numBloques;
    }
}
