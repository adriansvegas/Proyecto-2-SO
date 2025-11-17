/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Edgar
 */
public class Bloque {
    // Punteros especiales para la lista enlazada del disco
    public static final int BLOQUE_LIBRE = -2;
    public static final int FIN_DE_ARCHIVO = -1;

    private boolean ocupado;
    private int idArchivo;      // ID del archivo dueño (para visualizar colores en el disco)
    private int siguienteBloque; // Puntero al índice del siguiente bloque físico

    public Bloque() {
        this.ocupado = false;
        this.idArchivo = 0;
        this.siguienteBloque = BLOQUE_LIBRE;
    }

    public void ocupar(int idArchivo, int siguiente) {
        this.ocupado = true;
        this.idArchivo = idArchivo;
        this.siguienteBloque = siguiente;
    }

    public void liberar() {
        this.ocupado = false;
        this.idArchivo = 0;
        this.siguienteBloque = BLOQUE_LIBRE;
    }

    public boolean estaOcupado() { return ocupado; }
    public int getIdArchivo() { return idArchivo; }
    public int getSiguienteBloque() { return siguienteBloque; }
    public void setSiguienteBloque(int siguiente) { this.siguienteBloque = siguiente; }
}
