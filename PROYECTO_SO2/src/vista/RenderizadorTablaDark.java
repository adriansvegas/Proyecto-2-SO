/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vista;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Edgar
 */
public class RenderizadorTablaDark extends DefaultTableCellRenderer {
    
    private final Color COLOR_FONDO_1 = new Color(45, 45, 45);
    private final Color COLOR_FONDO_2 = new Color(55, 55, 55);
    private final Color COLOR_SELECCION = new Color(75, 0, 130); // Indigo
    private final Color COLOR_TEXTO = new Color(220, 220, 220);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, 
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        this.setForeground(COLOR_TEXTO);
        this.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        if (isSelected) {
            this.setBackground(COLOR_SELECCION);
        } else {
            this.setBackground(row % 2 == 0 ? COLOR_FONDO_1 : COLOR_FONDO_2);
        }
        
        setBorder(noFocusBorder);
        return this;
    }
}
