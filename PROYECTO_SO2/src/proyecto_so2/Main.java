/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package proyecto_so2;
import Controlador.SimuladorFS;
import vista.VentanaPrincipal;
import javax.swing.SwingUtilities;
// import so_operativos.Logger;
/**
 *
 * @author Edgar
 */


public class Main {
    public static void main(String[] args) {
       // Opcional: Inicializar logger
        // Logger.init(); 
        
        System.out.println("Iniciando Sistema de Archivos...");
        
        SwingUtilities.invokeLater(() -> {
            SimuladorFS modelo = new SimuladorFS();
            VentanaPrincipal vista = new VentanaPrincipal(modelo);
            vista.setVisible(true);
        });
    }
}