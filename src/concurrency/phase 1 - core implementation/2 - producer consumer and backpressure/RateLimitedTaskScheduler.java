/*
Problem Statement
Design a task scheduling system where clients continuously submit jobs, but downstream systems
(database, payment gateway, third-party API) can process only a limited number of requests per
second.

Requirements:
Multiple clients can submit tasks concurrently.
Tasks should be buffered in a bounded queue.
A fixed number of workers execute tasks.
Workers must not exceed a configured processing rate (e.g., 10 tasks/second).
Support graceful shutdown.

Pattern
Producer–Consumer + Backpressure + Rate Limiting

Production-grade Approach
Use a bounded BlockingQueue<Runnable> for backpressure and a Semaphore refilled periodically
(token bucket) to throttle worker throughput. In production, libraries like Guava's RateLimiter
or Resilience4j are commonly used.
*/

import java.util.concurrent.*;

public class RateLimitedTaskScheduler {

    private final BlockingQueue<Runnable> queue;
    private final Semaphore permits;

    public RateLimitedTaskScheduler(int workers, int queueCapacity, int permitsPerSecond) {
        queue = new ArrayBlockingQueue<>(queueCapacity);
        permits = new Semaphore(permitsPerSecond);

        // Refill permits every second
        ScheduledExecutorService refill = Executors.newSingleThreadScheduledExecutor();

        refill.scheduleAtFixedRate(() -> {
                permits.drainPermits();
                permits.release(permitsPerSecond);
            }, 1, 1, TimeUnit.SECONDS
        );

        for (int i = 1; i <= workers; i++) {
            Thread worker = new Thread(() -> {
                while (true) {
                    try {
                        Runnable task = queue.take();
                        permits.acquire();
                        task.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, "Worker-" + i);

            worker.start();
        }
    }

    public void submit(Runnable task) throws InterruptedException {
        queue.put(task);
    }

    public static void main(String[] args) throws Exception {
        RateLimitedTaskScheduler scheduler = new RateLimitedTaskScheduler(3, 20, 5); // Max 5 tasks/sec

        for (int i = 1; i <= 20; i++) {
            final int id = i;
            scheduler.submit(() -> {
                System.out.printf(
                    "%d : %s executed Task-%d%n",
                    System.currentTimeMillis() / 1000,
                    Thread.currentThread().getName(),
                    id
                );
            });
        }
    }
}
