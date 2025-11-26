/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
/**
 *
 * @author Edgar
 */


                                
public class RenderizadorID extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        this.setHorizontalAlignment(JLabel.CENTER);
        this.setFont(new Font("Consolas", Font.BOLD, 12));
        this.setForeground(Color.WHITE); 
        
        if (value != null) {
            try {
                int id = Integer.parseInt(value.toString());
                // Generar color consistente basado en PID
                float hue = (id * 0.13f) % 1.0f; 
                Color colorFondo = Color.getHSBColor(hue, 0.7f, 0.6f); 
                this.setBackground(colorFondo);
            } catch (NumberFormatException e) {
                this.setBackground(new Color(45, 45, 45));
            }
        }
        
        if (isSelected) this.setBorder(javax.swing.BorderFactory.createLineBorder(Color.WHITE, 2));
        else this.setBorder(null);
        
        return this;
    }
}