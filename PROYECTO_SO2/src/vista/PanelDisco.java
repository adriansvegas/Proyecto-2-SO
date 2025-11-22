/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;
import modelo.Bloque;
import modelo.Disco;

/**
 *
 * @author Edgar
 */
public class PanelDisco extends JPanel {
    private Disco disco;
    private final int BLOQUE_SIZE = 40;
    private final int MARGIN = 10;

    public PanelDisco(Disco disco) {
        this.disco = disco;
        this.setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (disco == null) return;

        int panelWidth = getWidth();
        int bloquesPorFila = Math.max(1, (panelWidth - MARGIN) / (BLOQUE_SIZE + MARGIN));

        for (int i = 0; i < disco.getCantidadBloques(); i++) {
            Bloque b = disco.getBloque(i);
            int row = i / bloquesPorFila;
            int col = i % bloquesPorFila;
            int x = MARGIN + col * (BLOQUE_SIZE + MARGIN);
            int y = MARGIN + row * (BLOQUE_SIZE + MARGIN);

            // Color según estado
            if (b.estaOcupado()) {
                // Generar color pseudo-aleatorio basado en ID de archivo para diferenciar
                g.setColor(new Color(Math.abs(b.getIdArchivo() * 123456) % 0xFFFFFF));
            } else {
                g.setColor(Color.LIGHT_GRAY);
            }
            g.fillRect(x, y, BLOQUE_SIZE, BLOQUE_SIZE);
            
            g.setColor(Color.BLACK);
            g.drawRect(x, y, BLOQUE_SIZE, BLOQUE_SIZE);
            g.drawString(String.valueOf(i), x + 5, y + 15);
            
            // Dibujar flecha si hay siguiente bloque (Asignación Encadenada)
            if (b.estaOcupado() && b.getSiguienteBloque() != Bloque.FIN_DE_ARCHIVO) {
                g.setColor(Color.RED);
                g.drawString("-> " + b.getSiguienteBloque(), x + 5, y + 35);
            }
        }
    }
}
