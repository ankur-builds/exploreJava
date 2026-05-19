import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BucketAggregationTest {
    public static void main(String[] args) {
        try {
            System.out.println("=== Starting Sliding Window Counter Tests ===");
            testConcurrentIncrements();
            testTimeSlidingAndEviction();
            System.out.println("\n=== All Tests Passed Successfully! ===");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test 1: Simulates 100 concurrent threads blasting the counter at the same time.
     * Verifies that the 'synchronized' blocks and LongAdder don't lose any updates.
     */
    private static void testConcurrentIncrements() throws Exception {
        System.out.print("Running Test 1 (High Concurrency Accuracy)... ");

        // Window: 60s, Bucket: 10s
        BucketAggregation counter = new BucketAggregation(60, 10);

        int numberOfThreads = 100;
        int incrementsPerThread = 5000;
        long expectedTotal = (long) numberOfThreads * incrementsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish updating
        latch.await();
        executor.shutdown();

        long actualTotal = counter.getCount();
        if (actualTotal != expectedTotal) {
            throw new AssertionError("Data loss detected! Expected: " + expectedTotal + ", Got: " + actualTotal);
        }
        System.out.println("PASSED. Counted exactly " + actualTotal + " hits.");
    }

    /**
     * Test 2: Verifies that data actually expires and drops off after the window passes.
     * Uses small window sizes so the test executes quickly without long sleep times.
     */
    private static void testTimeSlidingAndEviction() throws Exception {
        System.out.print("Running Test 2 (Sliding Window Eviction)... ");

        // Window: 3 seconds, Bucket: 1 second
        BucketAggregation counter = new BucketAggregation(3, 1);

        // 1. Log 50 viewers right now
        for (int i = 0; i < 50; i++) {
            counter.increment();
        }

        if (counter.getCount() != 50) {
            throw new AssertionError("Initial count incorrect. Expected 50, Got: " + counter.getCount());
        }

        // 2. Sleep for 1.5 seconds (Moves window forward, but old hits are still within the 3s window)
        Thread.sleep(1500);

        // Log 30 more viewers in this new bucket
        for (int i = 0; i < 30; i++) {
            counter.increment();
        }

        // Total should now be 50 + 30 = 80
        if (counter.getCount() != 80) {
            throw new AssertionError("Mid-window count incorrect. Expected 80, Got: " + counter.getCount());
        }

        // 3. Sleep another 2 seconds (Total 3.5 seconds have passed since the first 50 hits)
        // The first 50 hits are now older than the 3-second window and must be evicted!
        Thread.sleep(2000);

        // The counter should now automatically drop the first 50 hits and only return the 30 newer ones
        long finalCount = counter.getCount();
        if (finalCount != 30) {
            throw new AssertionError("Eviction failed! Old data wasn't dropped. Expected 30, Got: " + finalCount);
        }

        System.out.println("PASSED. Old data dropped automatically. Current count: " + finalCount);
    }
}
