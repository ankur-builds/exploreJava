package basics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CounterBenchmark {

    private static final int THREAD_COUNT = 4;
    private static final int INCREMENTS_PER_THREAD = 1_000_000;
    private static final int EXPECTED_TOTAL =
        THREAD_COUNT * INCREMENTS_PER_THREAD;

    // Counter targets
    private static int plainIntCounter = 0;
    private static int synchronizedCounter = 0;
    private static final Object lock = new Object();
    private static final AtomicInteger atomicCounter = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        System.out.println(
            "Expected final count for each run: " + EXPECTED_TOTAL + "\n"
        );

        // 1. Benchmark Plain int
        long timePlain = runTest(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                plainIntCounter++; // Read-modify-write (Not atomic)
            }
        });
        System.out.printf(
            "%-18s | Final Count: %-10d | Lost Updates? %-3s | Time: %d ms%n",
            "Plain int",
            plainIntCounter,
            plainIntCounter == EXPECTED_TOTAL ? "No" : "Yes",
            timePlain
        );

        // 2. Benchmark Synchronized Increment
        long timeSync = runTest(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                synchronized (lock) {
                    synchronizedCounter++; // Monitor lock coordination
                }
            }
        });
        System.out.printf(
            "%-18s | Final Count: %-10d | Lost Updates? %-3s | Time: %d ms%n",
            "synchronized",
            synchronizedCounter,
            synchronizedCounter == EXPECTED_TOTAL ? "No" : "Yes",
            timeSync
        );

        // 3. Benchmark AtomicInteger
        long timeAtomic = runTest(() -> {
            for (int i = 0; i < INCREMENTS_PER_THREAD; i++) {
                atomicCounter.incrementAndGet(); // Low-level CAS hardware instruction
            }
        });
        System.out.printf(
            "%-18s | Final Count: %-10d | Lost Updates? %-3s | Time: %d ms%n",
            "AtomicInteger",
            atomicCounter.get(),
            atomicCounter.get() == EXPECTED_TOTAL ? "No" : "Yes",
            timeAtomic
        );
    }

    /**
     * Spawns worker threads, executes the target task under contention,
     * measures elapsed time accurately, and shuts down the thread pool completely.
     */
    private static long runTest(Runnable task) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(task);
        }

        // Wait for all active tasks to complete execution before stopping the timer
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }
}
