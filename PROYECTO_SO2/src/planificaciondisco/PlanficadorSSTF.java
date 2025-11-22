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
public class PlanficadorSSTF implements PlanifcadorDisco{
    @Override
    public ProcesoIO seleccionarSiguiente(Cola<ProcesoIO> cola, int cabezalActual) {
        if (cola.isEmpty()) return null;

        Object[] procesos = cola.toArray();
        ProcesoIO mejor = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (Object obj : procesos) {
            ProcesoIO p = (ProcesoIO) obj;
            int distancia = Math.abs(p.getCilindroPeticion() - cabezalActual);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejor = p;
            }
        }
        // Removemos el seleccionado de la cola original
        if (mejor != null) cola.remove(mejor);
        return mejor;
    }
    @Override
    public String getNombre() { return "SSTF"; }
}
