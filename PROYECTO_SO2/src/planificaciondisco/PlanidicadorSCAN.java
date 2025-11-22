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
public class PlanidicadorSCAN implements PlanifcadorDisco{
    private boolean subiendo = true; // Dirección del elevador

    @Override
    public ProcesoIO seleccionarSiguiente(Cola<ProcesoIO> cola, int cabezalActual) {
        if (cola.isEmpty()) return null;

        Object[] procesos = cola.toArray();
        ProcesoIO mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;

        // Buscar el más cercano en la dirección actual
        for (Object obj : procesos) {
            ProcesoIO p = (ProcesoIO) obj;
            int pos = p.getCilindroPeticion();
            
            if (subiendo && pos >= cabezalActual) {
                int dist = pos - cabezalActual;
                if (dist < mejorDistancia) { mejorDistancia = dist; mejor = p; }
            } else if (!subiendo && pos <= cabezalActual) {
                int dist = cabezalActual - pos;
                if (dist < mejorDistancia) { mejorDistancia = dist; mejor = p; }
            }
        }

        // Si no hay nadie en esa dirección, cambiamos de sentido y buscamos de nuevo
        if (mejor == null) {
            subiendo = !subiendo;
            return seleccionarSiguiente(cola, cabezalActual);
        }

        if (mejor != null) cola.remove(mejor);
        return mejor;
    }
    @Override
    public String getNombre() { return "SCAN"; }
}
