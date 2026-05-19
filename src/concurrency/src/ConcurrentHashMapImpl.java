import java.util.*;

public class ConcurrentHashMapImpl{
    // Shared mutable state
    // Lock Striping
}

class MyConcurrentHashMap<K, V> {

    // Linked-list node
    static class Node<K, V> {

        final K key;

        V value;

        Node<K, V> next;

        Node(
                K key,
                V value) {

            this.key = key;
            this.value = value;
        }
    }

    private final int bucketCount;

    private final Node<K, V>[] buckets;

    // Lock striping
    private final Object[] locks;

    public MyConcurrentHashMap(
            int bucketCount,
            int lockCount) {

        this.bucketCount =
                bucketCount;

        this.buckets =
                new Node[bucketCount];

        this.locks =
                new Object[lockCount];

        for(int i = 0;
            i < lockCount;
            i++) {

            locks[i] = new Object();
        }
    }

    private int bucketIndex(K key) {

        return Math.abs(
                key.hashCode()
        ) % bucketCount;
    }

    private int lockIndex(int bucketIndex) {

        return bucketIndex
                % locks.length;
    }

    public void put(K key, V value) {

        int bucketIdx =
                bucketIndex(key);

        int lockIdx =
                lockIndex(bucketIdx);

        synchronized(
                locks[lockIdx]) {

            Node<K, V> head =
                    buckets[bucketIdx];

            Node<K, V> curr =
                    head;

            while(curr != null) {

                if(curr.key.equals(key)) {

                    curr.value = value;

                    return;
                }

                curr = curr.next;
            }

            Node<K, V> newNode =
                    new Node<>(key, value);

            newNode.next = head;

            buckets[bucketIdx] =
                    newNode;
        }
    }

    public V get(K key) {

        int bucketIdx =
                bucketIndex(key);

        int lockIdx =
                lockIndex(bucketIdx);

        synchronized(
                locks[lockIdx]) {

            Node<K, V> curr =
                    buckets[bucketIdx];

            while(curr != null) {

                if(curr.key.equals(key)) {
                    return curr.value;
                }

                curr = curr.next;
            }

            return null;
        }
    }

    public void remove(K key) {

        int bucketIdx =
                bucketIndex(key);

        int lockIdx =
                lockIndex(bucketIdx);

        synchronized(
                locks[lockIdx]) {

            Node<K, V> curr =
                    buckets[bucketIdx];

            Node<K, V> prev =
                    null;

            while(curr != null) {

                if(curr.key.equals(key)) {

                    if(prev == null) {

                        buckets[bucketIdx] =
                                curr.next;

                    } else {

                        prev.next =
                                curr.next;
                    }

                    return;
                }

                prev = curr;

                curr = curr.next;
            }
        }
    }
}
