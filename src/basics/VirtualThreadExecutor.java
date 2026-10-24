package basics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VirtualThreadExecutor {

    public static void main(String[] args) throws InterruptedException {
        // Create a list to store the Future handles for each task
        List<Future<String>> futures = new ArrayList<>();

        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                int orderNumber = i;

                // submit() returns a Future, which we add to our collection
                Future<String> future = executor.submit(() -> {
                    Thread.sleep(100);
                    return "ORD-" + orderNumber;
                });

                futures.add(future);
            }
        } // The try-with-resources auto-close mechanism blocks here until all 10,000 virtual threads finish executing.

        // Create a list to collect the actual string results
        List<String> results = new ArrayList<>();

        for (Future<String> future : futures) {
            try {
                // future.get() is safe and non-blocking here because all tasks are guaranteed completed
                results.add(future.get());
            } catch (ExecutionException e) {
                System.err.println(
                    "Task failed with exception: " + e.getCause()
                );
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        // Print details to verify collection
        System.out.printf(
            "Successfully collected %d order results in %d ms.%n",
            results.size(),
            duration
        );
        System.out.println("First 5 results: " + results.subList(0, 5));
        System.out.println(
            "Last 5 results: " +
                results.subList(results.size() - 5, results.size())
        );
    }
}
