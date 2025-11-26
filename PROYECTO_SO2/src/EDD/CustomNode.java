package EDD;
/* Funciona como un contenedor que almacena un dato y una referencia al siguiente nodo,
 * permitiendo la creación de Listas Enlazadas, Colas y Pilas dinámicas.
 * </p>
 * @param <T> El tipo de dato que almacenará el nodo (Genericidad).
 */

public class CustomNode<T> {
    /** El dato almacenado en el nodo. */
    private T data; 
    /** Referencia al siguiente nodo en la secuencia (o null si es el último). */
    private CustomNode<T> next; 

    /** Constructor que inicializa el nodo con un dato genérico. */
    public CustomNode(T data) {
        this.data = data;
        this.next = null;
    }
// --- Getters y Setters ---

    /**
     * Obtiene el dato almacenado.
     * @return El objeto de tipo T.
     */
    
    public T getData() { 
        return data; 
    }
    /**
     * Obtiene el siguiente nodo enlazado.
     * @return El nodo siguiente o null.
     */
    public CustomNode<T> getNext() { 
        return next; 
    }
    /**
     * Establece el enlace al siguiente nodo.
     * @param next El nuevo nodo siguiente.
     */
    public void setNext(CustomNode<T> next) { 
        this.next = next; 
    }
}