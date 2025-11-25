/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author adria
 */
public class ProcesoIO {
    public enum Operacion { CREAR_ARCHIVO, ELIMINAR_ARCHIVO, CREAR_DIR, ELIMINAR_DIR }
    public enum Estado { NUEVO, LISTO, EJECUCION, BLOQUEADO, TERMINADO }

    private static int nextId = 1;
    private int id;
    private String usuario;
    private Operacion operacion;
    private String rutaObjetivo;
    private int tamano;
    private Estado estado;
    private int cilindroPeticion;
    
    private int tiempoTotal;
    private int tiempoRestante;

    public ProcesoIO(String usuario, Operacion operacion, String rutaObjetivo, int tamano, int cilindroPeticion, int tiempoDuracion) {
        this.id = nextId++;
        this.usuario = usuario;
        this.operacion = operacion;
        this.rutaObjetivo = rutaObjetivo;
        this.tamano = tamano;
        this.cilindroPeticion = cilindroPeticion;
        this.estado = Estado.NUEVO;
        this.tiempoTotal = tiempoDuracion;
        this.tiempoRestante = tiempoDuracion;
    }

    public void ejecutarPaso() {
        if (tiempoRestante > 0) tiempoRestante--;
    }

    public boolean esTerminado() { return tiempoRestante <= 0; }

    public int getId() { return id; }
    public String getUsuario() { return usuario; }
    public Operacion getOperacion() { return operacion; }
    public String getRutaObjetivo() { return rutaObjetivo; }
    public int getTamano() { return tamano; }
    public int getCilindroPeticion() { return cilindroPeticion; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public int getTiempoTotal() { return tiempoTotal; }
    public int getTiempoRestante() { return tiempoRestante; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%ds)", operacion, rutaObjetivo, tiempoRestante);
    }
}