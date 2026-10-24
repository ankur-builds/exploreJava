package basics;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class VirtualThreadDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Starting Java Virtual Thread Demo ---");

        // Approach 1: Create and start a single virtual thread immediately
        Thread vt1 = Thread.ofVirtual()
            .name("single-worker-", 1)
            .start(() -> {
                printThreadInfo("Approach 1 (Started Immediately)");
            });
        vt1.join(); // Wait for it to complete, since virtual threads are daemon threads

        // Approach 2: Create a virtual thread using a ThreadFactory
        ThreadFactory factory = Thread.ofVirtual()
            .name("factory-worker-", 1)
            .factory();
        Thread vt2 = factory.newThread(() -> {
            printThreadInfo("Approach 2 (Via ThreadFactory)");
        });
        vt2.start();
        vt2.join();

        // Approach 3: Mass scale concurrency using a Virtual Thread Executor Service
        // This is the recommended approach for running many I/O-bound tasks.
        System.out.println(
            "\n--- Launching 10,000 concurrent virtual tasks ---"
        );
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                final int taskId = i;
                executor.submit(() -> {
                    // Simulate a blocking network or database operation
                    Thread.sleep(100);

                    // Periodically print progress for a few tasks
                    if (taskId % 2500 == 0) {
                        System.out.printf(
                            "Task #%d executing on: %s%n",
                            taskId,
                            Thread.currentThread()
                        );
                    }
                    return null;
                });
            }
        } // The try-with-resources statement automatically closes the executor and awaits all tasks

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf(
            "%nFinished 10,000 tasks in %d ms (Traditional thread pools would struggle or crash with this many blocking threads).%n",
            duration
        );
    }

    private static void printThreadInfo(String label) {
        Thread current = Thread.currentThread();
        System.out.printf(
            "[%s] Name: %s | Is Virtual: %b | Details: %s%n",
            label,
            current.getName(),
            current.isVirtual(),
            current
        );
    }
}
