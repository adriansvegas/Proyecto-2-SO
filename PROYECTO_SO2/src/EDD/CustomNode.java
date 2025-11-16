package edd;

/**
 * Nodo simple genérico (usa <T>) para listas enlazadas.
 * Almacena cualquier tipo de dato (data) y una referencia al siguiente nodo.
 * (Versión genérica del CustomNode del Proyecto 1).
 */
public class CustomNode<T> {
    private T data; // Ahora almacena un tipo genérico T (en lugar de Proceso)
    private CustomNode<T> next; // Referencia al siguiente nodo genérico

    /** Constructor que inicializa el nodo con un dato genérico. */
    public CustomNode(T data) {
        this.data = data;
        this.next = null;
    }

    // --- Getters y Setters ---
    public T getData() { 
        return data; 
    }
    
    public CustomNode<T> getNext() { 
        return next; 
    }
    
    public void setNext(CustomNode<T> next) { 
        this.next = next; 
    }
}