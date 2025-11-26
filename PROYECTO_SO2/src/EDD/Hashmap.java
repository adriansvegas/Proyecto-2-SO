/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;


import java.util.LinkedList; 
import java.util.Set;
import java.util.HashSet;
import java.util.Map; 
import java.util.Iterator; 
import java.util.Collection; 
import java.util.List; 
import java.util.ArrayList; 
import java.util.AbstractMap; 
import java.util.NoSuchElementException; 
import java.util.function.Predicate; 


/**
 *
 * @author Edgar
 */

/**
 
 */

/**
 * Tabla Hash (Hash Table) con encadenamiento para colisiones.
 * <p>
 * Estructura clave para la <b>Tabla de Asignación de Archivos (FAT)</b>.
 * Permite búsquedas de archivos por ruta en tiempo O(1) promedio.
 * </p>
 */
public class Hashmap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private LinkedList<Entry<K, V>>[] buckets;
    private int size;
    private final float loadFactor;

    @SuppressWarnings("unchecked")
    public Hashmap() {
        this.buckets = new LinkedList[DEFAULT_CAPACITY];
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            buckets[i] = new LinkedList<>();
        }
        this.size = 0;
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public static class Entry<K, V> {
        final K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() { return key; }
        public V getValue() { return value; }
        public void setValue(V value) { this.value = value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return key.equals(entry.key); 
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }
    }

    private int getBucketIndex(K key) {
        
        int hashCode = key.hashCode();
        
        int index = hashCode % buckets.length;
        return index < 0 ? index + buckets.length : index;
        
    }


    public synchronized V put(K key, V value) {
        if (key == null) throw new NullPointerException("Key cannot be null");
        if (value == null) throw new NullPointerException("Value cannot be null"); 
        if ((float) (size + 1) / buckets.length >= loadFactor) { 
            resize();
        }

        int bucketIndex = getBucketIndex(key);
        LinkedList<Entry<K, V>> bucket = buckets[bucketIndex];
        if (bucket == null) { 
             bucket = new LinkedList<>();
             buckets[bucketIndex] = bucket;
        }


        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                V oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
        }

        bucket.add(new Entry<>(key, value));
        size++;
        return null;
    }

    public synchronized V get(K key) {
         if (key == null) return null;
         int bucketIndex = getBucketIndex(key);
         LinkedList<Entry<K, V>> bucket = buckets[bucketIndex];
         if (bucket == null) return null; 
         for (Entry<K, V> entry : bucket) {
             if (entry.key.equals(key)) {
                 return entry.value;
             }
         }
         return null;
    }

    public synchronized V remove(K key) {
        if (key == null) return null;
        int bucketIndex = getBucketIndex(key);
        LinkedList<Entry<K, V>> bucket = buckets[bucketIndex];
        if (bucket == null) return null;

       
        Iterator<Entry<K, V>> iterator = bucket.iterator();
        
        while (iterator.hasNext()) {
            Entry<K, V> entry = iterator.next();
            if (entry.key.equals(key)) {
                iterator.remove(); 
                size--;
                return entry.value;
            }
        }
        return null;
    }


    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public synchronized void clear() {
        // Clear each bucket
        for (int i = 0; i < buckets.length; i++) {
             if (buckets[i] != null) {
                 buckets[i].clear();
             }
         }
         
        size = 0;
         
    }


    public synchronized boolean containsKey(K key) {
        if (key == null) return false;
        int bucketIndex = getBucketIndex(key);
        LinkedList<Entry<K, V>> bucket = buckets[bucketIndex];
        if (bucket == null) return false;
        for (Entry<K, V> entry : bucket) {
            if (entry.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    

    public synchronized Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (LinkedList<Entry<K, V>> bucket : buckets) {
             if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    keys.add(entry.key);
                }
             }
        }
        return keys;
    }

    public synchronized Collection<V> values() {
        List<V> values = new ArrayList<>(); 
        for (LinkedList<Entry<K, V>> bucket : buckets) {
             if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    values.add(entry.value);
                }
             }
        }
        return values;
    }

     public synchronized Set<Map.Entry<K, V>> entrySet() {
         Set<Map.Entry<K, V>> entries = new HashSet<>();
         for (LinkedList<Entry<K, V>> bucket : buckets) {
              if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    entries.add(new AbstractMap.SimpleEntry<>(entry.key, entry.value));
                }
              }
         }
         return entries;
     }

    @SuppressWarnings("unchecked")
    private synchronized void resize() {
        int oldCapacity = buckets.length;
        
        if (oldCapacity >= Integer.MAX_VALUE / 2) {
             System.err.println("WARN: Max map capacity reached, not resizing.");
             return;
         }
        int newCapacity = oldCapacity * 2;
        LinkedList<Entry<K, V>>[] oldBuckets = buckets;

        buckets = new LinkedList[newCapacity];
        for (int i = 0; i < newCapacity; i++) {
            buckets[i] = new LinkedList<>();
        }
        size = 0; 

        for (LinkedList<Entry<K, V>> bucket : oldBuckets) {
             if (bucket != null) {
                for (Entry<K, V> entry : bucket) {
                    put(entry.key, entry.value); 
                }
             }
        }
         
    }

    
    public synchronized Iterator<Entry<K, V>> entryIterator() {
        return new EntryIterator();
    }

    
    public class EntryIterator implements Iterator<Entry<K, V>> {
    
        private int currentBucketIndex;
        private Iterator<Entry<K, V>> currentBucketIterator;
        private Entry<K, V> lastReturned;

        EntryIterator() {
            currentBucketIndex = -1;
            advanceToNextBucket(); 
        }

        
        private void advanceToNextBucket() {
            lastReturned = null; 
            if (currentBucketIterator != null && currentBucketIterator.hasNext()) {
                return; 
            }
            currentBucketIndex++;
            while (currentBucketIndex < buckets.length) {
                 if (buckets[currentBucketIndex] != null && !buckets[currentBucketIndex].isEmpty()) {
                    currentBucketIterator = buckets[currentBucketIndex].iterator();
                    return; 
                 }
                currentBucketIndex++;
            }
            currentBucketIterator = null; 
        }


        @Override
        public boolean hasNext() {
            
            if (currentBucketIterator == null || !currentBucketIterator.hasNext()) {
                 advanceToNextBucket(); 
             }
             return currentBucketIterator != null && currentBucketIterator.hasNext();
        }

        @Override
        public Entry<K, V> next() {
            if (!hasNext()) { 
                throw new NoSuchElementException();
            }
            lastReturned = currentBucketIterator.next();
            return lastReturned;
        }

        @Override
        public void remove() {
             if (lastReturned == null) {
                 throw new IllegalStateException("next() must be called before remove(), or remove() called twice");
             }
            

             int bucketIdx = getBucketIndex(lastReturned.key);
             LinkedList<Entry<K,V>> bucket = buckets[bucketIdx];
             if (bucket != null) {
                 Iterator<Entry<K,V>> it = bucket.iterator();
                 while (it.hasNext()) {
                     Entry<K,V> current = it.next();
                     
                     if (current == lastReturned) {
                         it.remove();
                         size--;
                         lastReturned = null; 
                         return;
                     }
                 }
             }
             
             throw new IllegalStateException("Could not remove element, possibly due to concurrent modification or internal error.");
        }
    }

    
    public synchronized boolean removeIf(Predicate<Map.Entry<K, V>> filter) {
        boolean removed = false;
        Iterator<Entry<K, V>> it = entryIterator(); 
        while (it.hasNext()) {
             Entry<K, V> internalEntry = it.next();
             
             Map.Entry<K, V> mapEntry = new AbstractMap.SimpleEntry<>(internalEntry.key, internalEntry.value);
             if (filter.test(mapEntry)) {
                 it.remove(); 
                 removed = true;
             }
         }
         return removed;
    }

     
}