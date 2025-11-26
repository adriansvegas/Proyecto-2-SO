package modelo;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Edgar
 */
public class Disco {
    public static final int CANTIDAD_BLOQUES = 100; 
    private Bloque[] bloques;

    public Disco() {
        bloques = new Bloque[CANTIDAD_BLOQUES];
        for (int i = 0; i < CANTIDAD_BLOQUES; i++) {
            bloques[i] = new Bloque();
        }
    }

    public Bloque getBloque(int indice) {
        if (indice >= 0 && indice < CANTIDAD_BLOQUES) {
            return bloques[indice];
        }
        return null;
    }

    public int getCantidadBloques() { return CANTIDAD_BLOQUES; }

    public int buscarBloqueLibre() {
        for (int i = 0; i < CANTIDAD_BLOQUES; i++) {
            if (!bloques[i].estaOcupado()) {
                return i; 
            }
        }
        return -1; 
    }

    public int contarBloquesLibres() {
        int libres = 0;
        for (Bloque b : bloques) {
            if (!b.estaOcupado()) libres++;
        }
        return libres;
    }
}