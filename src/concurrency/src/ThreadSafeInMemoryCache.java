import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

class ThreadSafeInMemoryCache {
    private final Map<String, String> map = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void put(String key, String value) {
        lock.writeLock().lock(); // Exclusive lock (Blocks readers and other writers)
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String get(String key) {
        lock.readLock().lock(); // Shared lock (Multiple readers can hold this together!)
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }
}

// Best Solution
class Cache {
    // Thread-safe out of the box, optimized for high read volume
    private final Map<String, String> map = new ConcurrentHashMap<>();

    public void put(String key, String value) {
        map.put(key, value); // Internal fine-grained locking uses lock striping
    }

    public String get(String key) {
        return map.get(key); // Completely lock-free! uses volatile
    }
}

class CacheUsingObjectLock{
    private final Map<String, String> map1 = new HashMap<>();
    private final Map<String, String> map2 = new HashMap<>();

    // Create two completely independent lock targets
    private final Object lock1 = new Object();
    private final Object lock2 = new Object();

    public void putInMap1(String key, String value) {
        synchronized(lock1) { // Only locks map1 operations
            map1.put(key, value);
        }
    }

    public String getFromMap2(String key) {
        synchronized(lock2) { // Only locks map2 operations. Runs completely parallel to lock1!
            return map2.get(key);
        }
    }
}
