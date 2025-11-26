package EDD;
/**
 * Implementación manual de una Cola (Queue) FIFO (First-In, First-Out).
 * <p>
 * Esta estructura es fundamental para la gestión de procesos (Planificación de Disco),
 * asegurando que las solicitudes se atiendan o se ordenen según su llegada.
 * </p>
 * * <b>Nota de Defensa:</b> Se utiliza la palabra clave <code>synchronized</code> en los métodos
 * modificadores para garantizar la seguridad en entornos concurrentes (Thread-Safety),
 * evitando condiciones de carrera si múltiples hilos acceden a la cola simultáneamente.
 * * @param <T> Tipo de elementos en la cola.
 */
public class Cola<T> {
    /** Puntero al inicio de la cola (donde se extraen elementos). */
    private CustomNode<T> head;
    /** Puntero al final de la cola (donde se insertan elementos). */
    private CustomNode<T> tail;
    /** Contador de elementos para acceso rápido al tamaño (O(1)). */
    private int size;

    public Cola() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
/* Inserta un elemento al final de la cola (Enqueue).*/
    public synchronized void add(T data) {
        CustomNode<T> newNode = new CustomNode<>(data);
        if (tail == null) {
            // Caso base: La cola estaba vacía
            head = newNode;
            tail = newNode;
        } else {
            // Caso general: Enlazar al final y actualizar tail
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }
//Extrae y elimina el elemento del frente de la cola (Dequeue).
    public synchronized T poll() {
        if (head == null) {
            return null;// Protección contra Underflow
        }
        T data = head.getData();
        head = head.getNext();// Avanzar puntero head
        if (head == null) {
            tail = null;// Si la cola quedó vacía, tail también debe ser null
        }
        size--;
        return data;
    }

    public T peek() {
        return (head != null) ? head.getData() : null;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        return size;
    }
/**
     * Convierte la cola a un arreglo nativo.
     * Útil para algoritmos que necesitan acceso aleatorio o iteración sin modificar la cola
     * (como los planificadores SSTF, SCAN, C-SCAN).
     * @return Arreglo de objetos con el contenido de la cola.
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
     * Elimina una instancia específica de un objeto en la cola.
     * Necesario para el algoritmo SSTF que extrae el "más cercano" aunque no sea el primero.
     */
    public synchronized T remove(T target) {
        if (head == null) return null;

        if (head.getData() == target || (head.getData() != null && head.getData().equals(target))) {
            return poll();
        }

        CustomNode<T> current = head;
        while (current.getNext() != null) {
            T dataSiguiente = current.getNext().getData();
            
            if (dataSiguiente == target || (dataSiguiente != null && dataSiguiente.equals(target))) {
                T removedData = dataSiguiente;
                current.setNext(current.getNext().getNext());
                if (current.getNext() == null) {
                    tail = current;
                }
                size--;
                return removedData;
            }
            current = current.getNext();
        }
        return null;
    }
}