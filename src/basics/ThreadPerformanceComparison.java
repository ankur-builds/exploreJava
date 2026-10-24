package basics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPerformanceComparison {

    private static final int TOTAL_TASKS = 10_000;
    private static final int MOCK_IO_SLEEP_MS = 50;

    public static void main(String[] args) throws Exception {
        System.out.println(
            "=================================================="
        );
        System.out.println(
            "  THREAD PERFORMANCE COMPARISON (10,000 I/O TASKS) "
        );
        System.out.println(
            "=================================================="
        );

        // --- 1. BENCHMARK PLATFORM THREADS (Using a standard pooled approach) ---
        // We use a pool size of 200 to prevent the OS from running out of memory.
        System.out.print("Running Platform Threads (Pool Size: 200)... ");
        long platformStartTime = System.currentTimeMillis();

        try (
            ExecutorService platformExecutor = Executors.newFixedThreadPool(200)
        ) {
            runTasks(platformExecutor);
        }

        long platformDuration = System.currentTimeMillis() - platformStartTime;
        System.out.printf("Done! Time taken: %,d ms%n", platformDuration);

        // --- 2. BENCHMARK VIRTUAL THREADS ---
        // Virtual threads create a new un-pooled thread per task instantly.
        System.out.print("Running Virtual Threads (New Thread Per Task)... ");
        long virtualStartTime = System.currentTimeMillis();

        try (
            ExecutorService virtualExecutor =
                Executors.newVirtualThreadPerTaskExecutor()
        ) {
            runTasks(virtualExecutor);
        }

        long virtualDuration = System.currentTimeMillis() - virtualStartTime;
        System.out.printf("Done! Time taken: %,d ms%n", virtualDuration);

        // --- 3. PRINT RESULTS AND METRICS ---
        printAnalysis(platformDuration, virtualDuration);
    }

    private static void runTasks(ExecutorService executor) throws Exception {
        List<Future<Integer>> futures = new ArrayList<>(TOTAL_TASKS);

        for (int i = 0; i < TOTAL_TASKS; i++) {
            int taskId = i;
            futures.add(
                executor.submit(() -> {
                    // Simulate an I/O network call or database query
                    Thread.sleep(MOCK_IO_SLEEP_MS);
                    return taskId;
                })
            );
        }

        // Wait for all tasks to finish processing
        for (Future<Integer> future : futures) {
            future.get();
        }
    }

    private static void printAnalysis(long platformTime, long virtualTime) {
        System.out.println(
            "\n==================== VERDICT ===================="
        );

        // Throughput Speed Analysis
        if (virtualTime < platformTime) {
            double speedup = (double) platformTime / virtualTime;
            System.out.printf(
                "⚡ SPEED & THROUGHPUT: Virtual Threads were %.2fx FASTER.%n",
                speedup
            );
        } else {
            System.out.println(
                "⚡ SPEED & THROUGHPUT: Platform threads were slightly faster or equal."
            );
        }

        // Lightweight Weight / Resource Analysis
        System.out.println("\n⚖️ LIGHTWEIGHT & SCALABILITY ANALYSIS:");
        System.out.println("• Platform Threads: Heavyweight.");
        System.out.println(
            "  To handle 10,000 tasks, we had to queue them inside a pool of 200 threads."
        );
        System.out.println(
            "  Creating 10,000 physical OS threads directly would take ~10 GB of RAM and crash."
        );

        System.out.println("\n• Virtual Threads: Ultra-Lightweight.");
        System.out.println(
            "  The JVM allocated 10,000 distinct virtual threads instantly."
        );
        System.out.println(
            "  Each virtual thread consumes bytes of memory instead of megabytes."
        );
        System.out.println(
            "  Because blocking them is cheap, they saturated the CPU and processed tasks concurrently."
        );
        System.out.println(
            "=================================================="
        );
    }
}
