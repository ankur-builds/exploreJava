import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyLinkedBlockingQueue<E> {

    private static class Node<E> {
        E item;
        Node<E> next;

        Node(E item) {
            this.item = item;
        }
    }

    private final int capacity;

    private final ReentrantLock putLock = new ReentrantLock();
    private final Condition notFull = putLock.newCondition();

    private final ReentrantLock takeLock = new ReentrantLock();
    private final Condition notEmpty = takeLock.newCondition();

    private volatile int count = 0;

    private Node<E> head;
    private Node<E> tail;

    public MyLinkedBlockingQueue(int capacity) {
        if (capacity <= 0)
            throw new IllegalArgumentException();

        this.capacity = capacity;
        head = tail = new Node<>(null); // Dummy node
    }

    public void put(E item) throws InterruptedException {
        if (item == null)
            throw new NullPointerException();

        putLock.lockInterruptibly();
        try {
            while (count == capacity)
                notFull.await();

            Node<E> node = new Node<>(item);
            tail.next = node;
            tail = node;
            count++;
        } finally {
            putLock.unlock();
        }

        signalNotEmpty();
    }

    public E take() throws InterruptedException {
        E item;
        takeLock.lockInterruptibly();
        try {
            while (count == 0)
                notEmpty.await();

            Node<E> first = head.next;
            item = first.item;
            first.item = null;
            head.next = first.next;
            if (tail == first)
                tail = head;
            count--;
        } finally {
            takeLock.unlock();
        }

        signalNotFull();
        return item;
    }

    public int size() {
        return count;
    }

    private void signalNotEmpty() {
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    private void signalNotFull() {
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    public static void main(String[] args) {
        MyLinkedBlockingQueue<Integer> queue =
                new MyLinkedBlockingQueue<>(5);

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
                    break;
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
                    break;
                }
            }
        };

        new Thread(producer, "Producer-1").start();
        new Thread(producer, "Producer-2").start();

        new Thread(consumer, "Consumer-1").start();
        new Thread(consumer, "Consumer-2").start();
    }
}
