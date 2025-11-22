/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package planificaciondisco;
import EDD.Cola;
import modelo.ProcesoIO;
/**
 *
 * @author adria
 */
public interface PlanifcadorDisco {
    ProcesoIO seleccionarSiguiente(Cola<ProcesoIO> cola, int cabezalActual);
    String getNombre();
}
