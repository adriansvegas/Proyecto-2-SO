package EDD;

public class Cola<T> {
    private CustomNode<T> head;
    private CustomNode<T> tail;
    private int size;

    public Cola() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public synchronized void add(T data) {
        CustomNode<T> newNode = new CustomNode<>(data);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    public synchronized T poll() {
        if (head == null) {
            return null;
        }
        T data = head.getData();
        head = head.getNext();
        if (head == null) {
            tail = null;
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