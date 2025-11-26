/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;
import java.awt.*;
import javax.swing.JPanel;
import modelo.Bloque;
import modelo.Disco;

/**
 *
 * @author Edgar
 */
/** Panel gráfico que dibuja el estado de los bloques del disco. */
public class PanelDisco extends JPanel {
    private Disco disco;
    private final int BLOQUE_SIZE = 42;
    private final int MARGIN = 10;
    private final int HEADER_HEIGHT = 35;
    
    private final Color COLOR_FONDO = new Color(18, 18, 18);
    private final Color COLOR_LIBRE = new Color(35, 35, 35);
    private final Color COLOR_BLOQUE_OCUPADO = new Color(41, 98, 255);
    private final Color COLOR_TEXTO = new Color(220, 220, 220);

    public PanelDisco(Disco disco) {
        this.disco = disco;
        this.setBackground(COLOR_FONDO);
        this.setFont(new Font("Segoe UI", Font.BOLD, 11));
    }

    @Override
    public Dimension getPreferredSize() {
        if (disco == null) return new Dimension(600, 300);
        int parentWidth = getParent() != null ? getParent().getWidth() : 600;
        int cols = Math.max(1, (parentWidth - MARGIN) / (BLOQUE_SIZE + MARGIN));
        int rows = (int) Math.ceil((double) disco.getCantidadBloques() / cols);
        return new Dimension(parentWidth, HEADER_HEIGHT + (rows * (BLOQUE_SIZE + MARGIN)) + MARGIN * 2);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (disco == null) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        dibujarStats(g2);

        int startY = HEADER_HEIGHT + 15;
        int width = getWidth();
        int cols = Math.max(1, (width - MARGIN) / (BLOQUE_SIZE + MARGIN));

        for (int i = 0; i < disco.getCantidadBloques(); i++) {
            Bloque b = disco.getBloque(i);
            int row = i / cols;
            int col = i % cols;
            int x = MARGIN + col * (BLOQUE_SIZE + MARGIN);
            int y = startY + row * (BLOQUE_SIZE + MARGIN);

            if (b.estaOcupado()) {
                // Fondo Azul
                g2.setColor(COLOR_BLOQUE_OCUPADO);
                g2.fillRoundRect(x, y, BLOQUE_SIZE, BLOQUE_SIZE, 10, 10);
                
                // Círculo de Identidad (Color del Proceso)
                Color idColor = generarColorPID(b.getIdArchivo());
                int dotSize = 14;
                int dotX = x + BLOQUE_SIZE - dotSize - 3;
                int dotY = y + 3;
                
                g2.setColor(idColor);
                g2.fillOval(dotX, dotY, dotSize, dotSize);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(dotX, dotY, dotSize, dotSize);

                // Enlace
                if (b.getSiguienteBloque() != -1) {
                    g2.setColor(Color.WHITE);
                    g2.fillPolygon(new int[]{x+5, x+15, x+5}, new int[]{y+BLOQUE_SIZE-15, y+BLOQUE_SIZE-10, y+BLOQUE_SIZE-5}, 3);
                }
            } else {
                g2.setColor(COLOR_LIBRE);
                g2.fillRoundRect(x, y, BLOQUE_SIZE, BLOQUE_SIZE, 10, 10);
            }
            g2.setColor(COLOR_TEXTO);
            g2.drawString(String.valueOf(i), x + 5, y + 15);
        }
    }
    
    private Color generarColorPID(int pid) {
        if (pid <= 0) return Color.GRAY;
        float hue = (pid * 0.13f) % 1.0f;
        return Color.getHSBColor(hue, 0.7f, 1.0f);
    }

    private void dibujarStats(Graphics2D g2) {
        int total = disco.getCantidadBloques();
        int usados = 0;
        for(int i=0; i<total; i++) if(disco.getBloque(i).estaOcupado()) usados++;
        
        g2.setColor(new Color(30,30,30));
        g2.fillRect(0,0,getWidth(), HEADER_HEIGHT);
        g2.setColor(Color.WHITE);
        g2.drawString("TOTAL: " + total + " | USADOS: " + usados + " | LIBRES: " + (total-usados), 15, 22);
    }
}