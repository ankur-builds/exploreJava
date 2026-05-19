import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BoundedBlockingQueue<T> {

    private final LinkedList<T> queue; // Internal queue to store items
    private final int capacity; // Maximum size of the queue
    private final Lock lock; // Lock for thread synchronization
    private final Condition notFull; // Condition to signal when queue is not full
    private final Condition notEmpty; // Condition to signal when queue is not empty

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Queue capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.queue = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
    }

    // Method to add an item to the queue
    public void put(T item) throws InterruptedException {
        lock.lock(); // Acquire the lock
        try {
            // Wait until there is space in the queue
            while (queue.size() == capacity) {
                notFull.await(); // Wait for the notFull condition
            }

            // Add item to the queue
            queue.addLast(item);

            // Signal that the queue is not empty
            notEmpty.signal();
        } finally {
            lock.unlock(); // Release the lock
        }
    }

    // Method to retrieve and remove an item from the queue
    public T take() throws InterruptedException {
        lock.lock(); // Acquire the lock
        try {
            // Wait until there is at least one item in the queue
            while (queue.isEmpty()) {
                notEmpty.await(); // Wait for the notEmpty condition
            }

            // Remove the item from the front of the queue
            T item = queue.removeFirst();

            // Signal that the queue is not full
            notFull.signal();

            return item;
        } finally {
            lock.unlock(); // Release the lock
        }
    }

    // Method to get the current size of the queue
    public int size() {
        lock.lock(); // Acquire the lock
        try {
            return queue.size();
        } finally {
            lock.unlock(); // Release the lock
        }
    }
}
