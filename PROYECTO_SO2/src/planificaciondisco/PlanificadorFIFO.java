/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package planificaciondisco;

import EDD.Cola;
import modelo.ProcesoIO;
/**
 *
 * @author adria
 */
/** Algoritmo First-In First-Out: Atiende en orden de llegada. */
public class PlanificadorFIFO implements PlanificadorDisco {
    @Override
    public ProcesoIO seleccionarSiguiente(Cola<ProcesoIO> cola, int cabezalActual) {
        return cola.poll();
    }
    @Override
    public String getNombre() { return "FIFO"; }
}