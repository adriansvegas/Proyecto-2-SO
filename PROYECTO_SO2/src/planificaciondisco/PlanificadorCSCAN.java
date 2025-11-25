package planificaciondisco;
import EDD.Cola;
import modelo.ProcesoIO;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Edgar
 */
public class PlanificadorCSCAN implements PlanificadorDisco {

    @Override
    public ProcesoIO seleccionarSiguiente(Cola<ProcesoIO> cola, int cabezalActual) {
        if (cola.isEmpty()) return null;

        Object[] procesos = cola.toArray();
        ProcesoIO mejor = null;
        int mejorDistancia = Integer.MAX_VALUE;

        // C-SCAN siempre asume dirección ascendente (subiendo)
        // Buscamos el requerimiento más cercano que sea MAYOR o IGUAL al cabezal actual
        for (Object obj : procesos) {
            ProcesoIO p = (ProcesoIO) obj;
            int pos = p.getCilindroPeticion();

            if (pos >= cabezalActual) {
                int dist = pos - cabezalActual;
                if (dist < mejorDistancia) {
                    mejorDistancia = dist;
                    mejor = p;
                }
            }
        }

        // Si no encontramos nada "subiendo", significa que debemos dar la vuelta al disco (ir a 0)
        // y buscar el requerimiento más pequeño disponible (el más cercano a 0)
        if (mejor == null) {
            int menorPosicion = Integer.MAX_VALUE;
            for (Object obj : procesos) {
                ProcesoIO p = (ProcesoIO) obj;
                if (p.getCilindroPeticion() < menorPosicion) {
                    menorPosicion = p.getCilindroPeticion();
                    mejor = p;
                }
            }
        }

        if (mejor != null) cola.remove(mejor);
        return mejor;
    }

    @Override
    public String getNombre() {
        return "C-SCAN";
    }
}
