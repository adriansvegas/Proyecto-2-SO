package EDD;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Arraylist<E> implements Iterable<E> {
    private Object[] elements;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    public Arraylist() {
        elements = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public synchronized void add(E element) {
        ensureCapacity();
        elements[size++] = element;
    }

    public synchronized E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        @SuppressWarnings("unchecked")
        E element = (E) elements[index];
        return element;
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    public synchronized boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    
    public synchronized int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++)
                if (elements[i] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (o.equals(elements[i]))
                    return i;
        }
        return -1;
    }

    public synchronized boolean remove(Object o) {
        int index = indexOf(o);
        if (index >= 0) {
            remove(index);
            return true;
        }
        return false;
    }
    
    public synchronized E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        @SuppressWarnings("unchecked")
        E oldValue = (E) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null;
        return oldValue;
    }

    public synchronized Object[] toArray() {
        return Arrays.copyOf(elements, size);
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] a) {
        if (a.length < size)
            return (T[]) Arrays.copyOf(elements, size, a.getClass());
        System.arraycopy(elements, 0, a, 0, size);
        if (a.length > size)
            a[size] = null;
        return a;
    }
    
    public synchronized void addAll(Arraylist<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0) return;
        
        if (size + numNew > elements.length) {
            int newCapacity = Math.max(elements.length * 2, size + numNew);
            elements = Arrays.copyOf(elements, newCapacity);
        }

        System.arraycopy(a, 0, elements, size, numNew);
        size += numNew;
    }

    private void ensureCapacity() {
        if (size == elements.length) {
            int newCapacity = elements.length * 2;
            elements = Arrays.copyOf(elements, newCapacity);
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new CustomIterator();
    }

    private class CustomIterator implements Iterator<E> {
        private int currentIndex = 0;
        private final int expectedSize = size;

        @Override
        public boolean hasNext() {
            checkForModification();
            return currentIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            checkForModification();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return (E) elements[currentIndex++];
        }

        @Override
        public void remove() {
             throw new UnsupportedOperationException("Remove not supported by this iterator");
        }
        
        final void checkForModification() {
            if (size != expectedSize)
                throw new RuntimeException("Concurrent modification detected");
        }
    }
}