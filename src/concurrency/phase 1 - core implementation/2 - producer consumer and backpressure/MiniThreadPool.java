/*
Problem Statement
Design a Mini Thread Pool that accepts tasks from multiple clients and executes them using a fixed
number of worker threads.

Requirements:
Clients submit tasks asynchronously.
Submitted tasks should be placed into a shared work queue.
A fixed number of worker threads continuously pick tasks from the queue.
Each task must execute exactly once.
Workers should automatically pick the next available task after completing the current one.

Pattern
Worker Pool / Task Scheduler

Production-grade Approach
Use a BlockingQueue<Runnable> as the work queue and dedicate a fixed number of worker threads to
continuously execute queued tasks. This is the core architecture behind Java's ThreadPoolExecutor.

Level 1 — Queue-level distribution => competing consumers, no scheduling
Level 2 - Worker Pool => Consumers executing arbitrary tasks like Executor Service
Level 3 - Kafka => Distributed Partition management. Each consumer maintains Offset for every
assigned partition

*/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MiniThreadPool {

    private final BlockingQueue<Runnable> queue;

    MiniThreadPool(int workers, int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
        for (int i = 1; i <= workers; ++i) {
            Thread worker = new Thread(() -> {
                // can also use while(true) and break when catching interrupt exception
                // or this approach. This is called production shutdown pattern
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable task = queue.take();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Worker-" + i);

            worker.start();
        }
    }

    public void submit(Runnable task) throws InterruptedException {
        queue.put(task);
    }

    public static void main(String[] args) {
        MiniThreadPool pool = new MiniThreadPool(10, 10);
        for (int i = 1; i <= 100; ++i) {
            final int taskId = i;
            try {
                pool.submit(() -> {
                    System.out.println(
                        Thread.currentThread().getName() +
                            " executing task " +
                            taskId
                    );
                });
            } catch (InterruptedException e) {
                System.out.println("---------- Caught Interrupted Exception ----------");
                Thread.currentThread().interrupt();
            }
        }
    }
}
