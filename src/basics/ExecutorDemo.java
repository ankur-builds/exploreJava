import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorDemo {

    public static void main(String[] args) throws Exception {
        // ==========================================
        // 3. TIMING: Fair Sequential Execution
        // ==========================================
        System.out.println("=== Starting Fair Sequential Execution ===");
        long startSeq = System.currentTimeMillis();
        for (int i = 1; i <= 10; ++i) {
            try {
                String result = processOrder(i);
                System.out.println("Result: " + result);
            } catch (Exception e) {
                System.out.println(
                    "Sequential task " +
                        i +
                        " failed natively: " +
                        e.getMessage()
                );
            }
        }
        long endSeq = System.currentTimeMillis();
        long seqDuration = endSeq - startSeq;
        System.out.println("Sequential work took: " + seqDuration + " ms\n");

        // ==========================================
        // 1 & 2. Concurrent Execution & Thread Names
        // ==========================================
        System.out.println("=== Starting Concurrent Execution ===");
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<String>> list = new ArrayList<>();

        // Start timing ONLY the concurrent workload submission and resolution
        long startCon = System.currentTimeMillis();

        for (int i = 1; i <= 10; ++i) {
            final int taskId = i;
            list.add(executor.submit(() -> processOrder(taskId)));
        }

        // 4. Inspecting Future.get() Exception Behavior (Still within timed block)
        for (int i = 0; i < 10; ++i) {
            try {
                System.out.println("Result: " + list.get(i).get());
            } catch (ExecutionException e) {
                System.out.println(
                    "Task " + (i + 1) + " failed via Future.get()!"
                );
                System.out.println("Root Cause: " + e.getCause());
            }
        }

        // Stop timing IMMEDIATELY after the 10 tasks are finished resolving
        long endCon = System.currentTimeMillis();
        long conDuration = endCon - startCon;

        // ==========================================
        // 5. Interruption and Orderly Shutdown (OUTSIDE TIMING)
        // ==========================================
        System.out.println(
            "\n=== Experimenting with Shutdown & Interruption (Post-Benchmark) ==="
        );

        // Submit an isolated long-running task to test interruption
        Future<?> longTask = executor.submit(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(
                    Thread.currentThread().getName() +
                        " caught InterruptedException actively!"
                );
            }
        });

        // This sleep and cancel operation now happens outside the timed benchmark block
        Thread.sleep(50);
        longTask.cancel(true);

        // Clean shutdown
        executor.shutdown();

        // Print the accurate, isolated benchmark scores
        System.out.println("\n=== Final Analysis ===");
        System.out.println("Sequential Duration: " + seqDuration + " ms");
        System.out.println("Concurrent Duration: " + conDuration + " ms");
        System.out.println(
            "Speedup factor: " +
                String.format("%.2f", (double) seqDuration / conDuration) +
                "x"
        );
    }

    private static String processOrder(int id) throws Exception {
        String threadName = Thread.currentThread().getName();
        if (id == 5) {
            throw new RuntimeException("Explosion in order " + id);
        }
        Thread.sleep(200);
        return threadName + " processed order " + id;
    }
}
