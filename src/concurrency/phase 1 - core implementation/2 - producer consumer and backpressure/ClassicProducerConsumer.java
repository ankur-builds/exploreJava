/*
Problem Statement

Design a bounded producer-consumer system where multiple producer threads generate items and place them into a shared queue, while multiple consumer threads remove and process those items.

The queue has a fixed capacity.

Rules:

Producers must wait if the queue is full.
Consumers must wait if the queue is empty.
Queue size must never exceed its capacity.
Queue operations must be thread-safe.
No item should be lost or consumed twice.

Pattern
Bounded Buffer (Producer–Consumer)

Production-grade Approach
Use BlockingQueue. This implementation is for understanding how the Producer–Consumer pattern works internally using intrinsic monitors.
*/

public class ClassicProducerConsumer {

    private int head;
    private int tail;
    private final int[] queue;
    private int size;
    private final int capacity;

    public ClassicProducerConsumer(int capacity) {
        queue = new int[capacity];
        this.capacity = capacity;
        head = 0;
        tail = 0;
        size = 0;
    }

    private void enqueue(int item) {
        queue[tail] = item;
        tail = (tail + 1) % capacity;
        size++;
    }

    private int dequeue() {
        int item = queue[head];
        head = (head + 1) % capacity;
        size--;
        return item;
    }

    public synchronized void put(int item) {
        try {
            while (size == capacity) wait();

            enqueue(item);
            notifyAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized int take() {
        int item = 0;
        try {
            while (size == 0) wait();

            item = dequeue();
            notifyAll();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return item;
    }

    public synchronized boolean isEmpty(){
        return this.size==0;
    }

    public static void main(String[] args) {
        ClassicProducerConsumer classic = new ClassicProducerConsumer(10);

        new Thread(() -> {
            for (int i = 1; i < 15; ++i){
                System.out.println("Producer : " + i);
                classic.put(i);
            }
        }).start();

        new Thread(() -> {
            while(true){
                int item = classic.take();
                System.out.println("               Consumer : " + item);
            }
        }).start();
    }
}
