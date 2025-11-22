package EDD;

/**
 * Implementación de una Cola (Queue) FIFO genérica (usa <T>).
 * Utiliza CustomNode<T> para almacenar cualquier tipo de elemento.
 * (Versión genérica de la Cola del Proyecto 1).
 */
public class Cola<T> {
    private CustomNode<T> head; // Referencia al primer nodo (frente de la cola)
    private CustomNode<T> tail; // Referencia al último nodo (final de la cola)
    private int size; // Número de elementos en la cola

    /** Constructor que inicializa una cola vacía. */
    public Cola() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Añade un elemento genérico al final de la cola (enqueue).
     * @param data El elemento a añadir.
     */
    public synchronized void add(T data) {
        CustomNode<T> newNode = new CustomNode<>(data);
        if (tail == null) { // Si la cola está vacía
            head = newNode;
            tail = newNode;
        } else { // Si la cola no está vacía
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    /**
     * Saca y remueve el elemento del frente de la cola (dequeue).
     * @return El elemento del frente, o null si la cola está vacía.
     */
    public synchronized T poll() {
        if (head == null) {
            return null; // Cola vacía
        }
        T data = head.getData();
        head = head.getNext(); // Avanza el head
        if (head == null) {
            tail = null; // Si se vació la cola, tail también es null
        }
        size--;
        return data;
    }

    /**
     * Retorna el elemento del frente sin removerlo (peek).
     * @return El elemento del frente, o null si la cola está vacía.
     */
    public T peek() {
        return (head != null) ? head.getData() : null;
    }

    /** Verifica si la cola está vacía. */
    public boolean isEmpty() {
        return head == null;
    }

    /** Retorna el número de elementos en la cola. */
    public int size() {
        return size;
    }

    /**
     * Convierte la cola en un arreglo de tipo Object.
     * Útil para iterar o visualizar la cola (ej. para planificadores SSTF o SCAN).
     * @return Un array con los elementos en el orden de la cola.
     */
    public Object[] toArray() {
        Object[] array = new Object[size];
        CustomNode<T> current = head;
        int index = 0;
        while (current != null) {
            array[index++] = current.getData();
            current = current.getNext();
        }
        return array;
    }

    /**
     * Encuentra y remueve un elemento específico de la cola.
     * @param target El elemento a remover.
     * @return El elemento removido, o null si no se encontró.
     */
    public synchronized T remove(T target) {
        if (head == null) return null; // Cola vacía

        // Comprobar si es el head
        // Compara por referencia (==) o por valor (.equals())
        if (head.getData() == target || (head.getData() != null && head.getData().equals(target))) {
            return poll(); // Usa poll() que ya maneja la lógica de head/tail
        }

        CustomNode<T> current = head;
        while (current.getNext() != null) {
            T dataSiguiente = current.getNext().getData();
            
            if (dataSiguiente == target || (dataSiguiente != null && dataSiguiente.equals(target))) {
                T removedData = dataSiguiente;
                current.setNext(current.getNext().getNext()); // Enlaza el anterior con el siguiente del objetivo
                if (current.getNext() == null) {
                    tail = current; // Si se eliminó el último, actualiza tail
                }
                size--;
                return removedData;
            }
            current = current.getNext();
        }
        return null; // No se encontró
    }
}