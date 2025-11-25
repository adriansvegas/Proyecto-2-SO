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
        for (Object obj : procesos) {
            ProcesoIO p = (ProcesoIO) obj;
            int pos = p.getCilindroPeticion();
            if (pos >= cabezalActual) {
                int dist = pos - cabezalActual;
                if (dist < mejorDistancia) { mejorDistancia = dist; mejor = p; }
            }
        }
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
    public String getNombre() { return "C-SCAN"; }
}