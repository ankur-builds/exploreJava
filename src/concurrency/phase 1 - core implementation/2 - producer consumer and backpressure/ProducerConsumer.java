/*
Problem Statement

Design a bounded producer-consumer system supporting multiple producers and multiple consumers.
Producers should block when the buffer is full, and consumers should block when it is empty.
Unlike the classical monitor solution, minimize unnecessary wakeups by using separate waiting
queues for producers and consumers.

Pattern
Bounded Buffer with Explicit Locks

Production-grade Approach
Protect the shared queue using one ReentrantLock and coordinate producers and consumers using
two Conditions (notFull and notEmpty)
*/
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer{
    private int head = 0;
    private int tail = 0;
    private final int[] queue;
    private int size;
    private final int capacity;

    ReentrantLock lock = new ReentrantLock();
    Condition notFull = lock.newCondition();
    Condition notEmpty = lock.newCondition();

    ProducerConsumer(int capacity){
        queue = new int[capacity];
        this.capacity = capacity;
    }

    private void enqueue(int item){
        queue[tail] = item;
        tail = (tail+1)%capacity;
        size++;
    }

    private int dequeue(){
        int item = queue[head];
        head = (head+1)%capacity;
        size--;
        return item;
    }

    public void put(int item){
        try{
            lock.lock();
            while(size==capacity)
                notFull.await();

            enqueue(item);
            notEmpty.signal();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public int take(){
        int item = -1;
        try{
            lock.lock();
            while(size==0)
                notEmpty.await();

            item = dequeue();
            notFull.signal();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
        return item;
    }

    public boolean isEmpty(){
        lock.lock();
        try{
            return size==0;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ProducerConsumer classic = new ProducerConsumer(10);

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
