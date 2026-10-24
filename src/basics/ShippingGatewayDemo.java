package basics;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ShippingGatewayDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Starting ShippingGateway Experiment ===");

        ShippingGateway gateway = new ShippingGateway();
        int totalTasks = 100;

        long startTime = System.currentTimeMillis();

        // Submit 100 tasks using a virtual-thread-per-task executor
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= totalTasks; i++) {
                final String orderId = "ORD-" + i;

                executor.submit(() -> {
                    try {
                        // Experiment variation toggles (Change parameters here to test)
                        // Variation A: Standard acquire (Points 1 - 3)
                        // Variation B: Timed permit acquisition (Point 4)
                        ShippingGateway.ShippingQuote quote = gateway.getQuote(
                            orderId
                        );

                        if (quote != null) {
                            // Quote successfully processed
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // try-with-resources blocks here until all 100 virtual threads terminate

        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("%n=== Experiment Completed in %d ms ===", duration);
        System.out.printf(
            "%nTotal successful quotes: %d",
            gateway.getSuccessfulCount()
        );
        System.out.printf(
            "%nTotal rejected/overloaded quotes: %d%n",
            gateway.getRejectedCount()
        );
    }
}

class ShippingGateway {

    // Modified to acquire only five permits
    private final Semaphore permits = new Semaphore(5);

    // Diagnostic counters
    private final AtomicInteger activeCalls = new AtomicInteger(0);
    private final AtomicInteger successfulCount = new AtomicInteger(0);
    private final AtomicInteger rejectedCount = new AtomicInteger(0);

    public ShippingQuote getQuote(String orderId) throws InterruptedException {
        // Toggle this boolean to switch between standard block and timed acquisition (Point 4)
        boolean useTimedAcquisition = false;

        if (useTimedAcquisition) {
            // Point 4: Attempt to get a permit within 50ms, otherwise drop the task
            if (!permits.tryAcquire(50, TimeUnit.MILLISECONDS)) {
                rejectedCount.incrementAndGet();
                System.out.printf(
                    "[OVERLOAD] %s rejected immediately. Gateway busy.%n",
                    orderId
                );
                return null;
            }
        } else {
            // Points 1-3: Standard blocking acquire
            permits.acquire();
        }

        try {
            // Track active concurrency metrics
            int currentActive = activeCalls.incrementAndGet();
            int waitingThreads = permits.getQueueLength(); // Tracks waiting virtual threads

            System.out.printf(
                "[Active Call Started] %s | Active: %d | Virtual Threads Waiting: %d%n",
                orderId,
                currentActive,
                waitingThreads
            );

            return callShippingProvider(orderId);
        } finally {
            activeCalls.decrementAndGet();
            permits.release();
        }
    }

    private ShippingQuote callShippingProvider(String orderId)
        throws InterruptedException {
        // Simulated slow call with a short sleep (e.g., 200ms)
        Thread.sleep(200);
        successfulCount.incrementAndGet();
        return new ShippingQuote(orderId, "STANDARD");
    }

    // Metric Getters
    public int getSuccessfulCount() {
        return successfulCount.get();
    }

    public int getRejectedCount() {
        return rejectedCount.get();
    }

    public record ShippingQuote(String orderId, String service) {}
}
