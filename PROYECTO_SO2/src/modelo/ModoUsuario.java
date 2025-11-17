/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

/**
 *
 * @author Edgar
 */
public enum ModoUsuario {
    ADMINISTRADOR, // Acceso total: Crear, Borrar, Modificar, etc.
    USUARIO        // Restringido: Solo lectura y crear procesos de E/S propios
}