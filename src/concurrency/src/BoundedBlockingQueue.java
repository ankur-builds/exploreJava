import java.lang.Object;
import java.util.*;

public class BoundedBlockingQueue<T> {

    private Queue<T> queue = new LinkedList<T>();
    private int capacity = 100;

    public BoundedBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void write(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }

        queue.add(item);
        notifyAll();
    }

    public synchronized T read() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }

        T item = queue.remove();
        notifyAll();
        return item;
    }
}
