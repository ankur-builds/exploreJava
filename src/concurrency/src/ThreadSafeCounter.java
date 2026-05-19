public class ThreadSafeCounter {

    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized void decrement() {
        count--;
    }

    public synchronized int get() {
        return count;
    }

    public synchronized void reset() {
        count = 0;
    }
}
