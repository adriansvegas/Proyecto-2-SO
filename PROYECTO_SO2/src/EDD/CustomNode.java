package EDD;


public class CustomNode<T> {
    private T data; 
    private CustomNode<T> next; 

    /** Constructor que inicializa el nodo con un dato genérico. */
    public CustomNode(T data) {
        this.data = data;
        this.next = null;
    }

    
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