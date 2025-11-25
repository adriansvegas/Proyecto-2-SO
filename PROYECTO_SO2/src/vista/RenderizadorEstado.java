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



public class RenderizadorEstado extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String estado = (value != null) ? value.toString() : "";
        this.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.setHorizontalAlignment(JLabel.CENTER);
        if (estado.equals("EJECUCION")) this.setForeground(new Color(0, 230, 118));
        else if (estado.equals("LISTO")) this.setForeground(new Color(255, 193, 7));
        else if (estado.equals("TERMINADO")) this.setForeground(Color.GRAY);
        else this.setForeground(Color.WHITE);
        if (!isSelected) this.setBackground(row % 2 == 0 ? new Color(35, 35, 35) : new Color(45, 45, 45));
        else this.setBackground(new Color(60, 60, 60));
        return this;
    }
}
