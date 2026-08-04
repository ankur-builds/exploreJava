
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayBlockingQueue<E> {

    private final Object[] elements;
    private final int capacity;

    private int head = 0;
    private int tail = 0;
    private int size = 0;

    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    public ArrayBlockingQueue(int capacity) {
        this(capacity, false);
    }

    public ArrayBlockingQueue(int capacity, boolean fair) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0");
        }

        this.capacity = capacity;
        this.elements = new Object[capacity];

        this.lock = new ReentrantLock(fair);
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    // Blocks if queue is full
    public void put(E item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (size == capacity) {
                notFull.await();
            }

            enqueue(item);
        } finally {
            lock.unlock();
        }
    }

    // Blocks if queue is empty
    public E take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    // Returns false immediately if queue is full
    public boolean offer(E item) {
        lock.lock();
        try {
            if (size == capacity) {
                return false;
            }
            enqueue(item);
            return true;

        } finally {
            lock.unlock();
        }
    }

    // Returns null immediately if queue is empty
    @SuppressWarnings("unchecked")
    public E poll() {
        lock.lock();
        try {
            if (size == 0) {
                return null;
            }
            return dequeue();
        } finally {
            lock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public E peek() {
        lock.lock();
        try {
            if (size == 0) {
                return null;
            }
            return (E) elements[head];
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return size == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return size == capacity;
        } finally {
            lock.unlock();
        }
    }

    private void enqueue(E item) {
        elements[tail] = item;
        tail = (tail + 1) % capacity;
        size++;
        notEmpty.signal();
    }

    @SuppressWarnings("unchecked")
    private E dequeue() {
        E item = (E) elements[head];
        elements[head] = null; // Avoid memory leak
        head = (head + 1) % capacity;
        size--;
        notFull.signal();
        return item;
    }

    public static void main(String[] args) {
        ArrayBlockingQueue<Integer> queue = new ArrayBlockingQueue<>(5);
        Runnable producer = () -> {
            int value = 1;
            while (true) {
                try {
                    queue.put(value);
                    System.out.println(
                            "Produced : " + value++
                    );
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };

        Runnable consumer = () -> {
            while (true) {
                try {
                    Integer item = queue.take();
                    System.out.println(
                            "Consumed : " + item
                    );
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };

        new Thread(producer, "Producer-1").start();
        new Thread(producer, "Producer-2").start();

        new Thread(consumer, "Consumer-1").start();
        new Thread(consumer, "Consumer-2").start();
    }
}
