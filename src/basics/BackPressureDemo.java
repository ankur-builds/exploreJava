package basics;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

public class BackPressureDemo {

    public static void main(String[] args) throws InterruptedException {
        // Bounded queue: holds max 10 items so we can easily watch backpressure kick in
        BlockingQueue<String> taskQueue = new ArrayBlockingQueue<>(10);

        System.out.println(
            "Starting Backpressure Demo... Press Ctrl+C to stop."
        );

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // 1. CONSUMER: Runs continuously in its own virtual thread
            executor.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        String task = taskQueue.take(); // Blocks naturally if queue is empty
                        System.out.printf(
                            "[Consumer] Pulled: %s (Queue size: %d) on Thread %s [Consumer] %n",
                            task,
                            taskQueue.size(),
                            Thread.currentThread()
                        );

                        // Simulate a slow downstream task (500ms)
                        Thread.sleep(500);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // 2. PRODUCER: Runs continuously in its own virtual thread
            executor.submit(() -> {
                try {
                    int orderCount = 1;
                    while (!Thread.currentThread().isInterrupted()) {
                        String newOrder = "ORD-" + orderCount++;

                        System.out.printf(
                            "[Producer] Attempting to push: %s... on Thread %s%n",
                            newOrder,
                            Thread.currentThread()
                        );

                        // BACKPRESSURE KICKS IN HERE:
                        // If the queue has 10 items, .put() will FREEZE this virtual thread.
                        // It will unmount and sit completely idle until the consumer takes an item.
                        taskQueue.put(newOrder);

                        System.out.printf(
                            "[Producer] Successfully pushed: %s on Thread %s%n",
                            newOrder,
                            Thread.currentThread()
                        );

                        // Producer is fast (100ms), Consumer is slow (500ms)
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // Keep the main thread alive so the executor doesn't close immediately
            Thread.sleep(Long.MAX_VALUE);
        }
    }
}
