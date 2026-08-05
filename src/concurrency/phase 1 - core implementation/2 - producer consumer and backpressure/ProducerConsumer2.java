/*
Problem Statement

Design a bounded producer-consumer system where multiple producers generate tasks and multiple
consumers process them. The implementation should be production-ready, avoiding manual
synchronization with synchronized, ReentrantLock, or Condition.

Pattern
Bounded Buffer using BlockingQueue

Production-grade Approach
Use ArrayBlockingQueue (or another appropriate BlockingQueue). Let the JDK handle synchronization,
waiting, signaling, and memory visibility.
*/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ProducerConsumer2 {
    private final BlockingQueue<Integer> queue;

    public ProducerConsumer2(int capacity){
        queue = new ArrayBlockingQueue<>(capacity);
    }
    public void put(int item){
        try{
            queue.put(item);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public int take(){
        int item = -1;
        try{
            item = queue.take();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        return item;
    }

    public static void main(String[] args) {
        ProducerConsumer2 production = new ProducerConsumer2(10);

        new Thread(() -> {
            for (int i = 1; i < 100; ++i){
                System.out.println("Producer : " + i);
                production.put(i);
            }
        }).start();

        new Thread(() -> {
            while(true){
                int item = production.take();
                System.out.println("               Consumer : " + item);
            }
        }).start();
    }
}
