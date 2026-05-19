import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CounterTest {

    public static void main(String[] args) throws InterruptedException {
        int numberOfThreads = 20;
        int incrementsPerThread = 1000;
        int expectedCount = 0; // numberOfThreads * incrementsPerThread;

        ThreadSafeCounter counter = new ThreadSafeCounter();
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        // Visual anchors to force simultaneous thread execution
        CountDownLatch readyLatch = new CountDownLatch(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                readyLatch.countDown(); // Thread is ready
                System.out.printf("Count : %d%n", readyLatch.getCount());
                try {
                    startLatch.await(); // Wait for the gun to fire
                    for (int j = 0; j < incrementsPerThread; j++) {
                        counter.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishLatch.countDown(); // Thread finished
                }
            });
        }

        readyLatch.await(); // Wait for all threads to line up
        System.out.println("🚀 Starting race condition test...");
        startLatch.countDown(); // Fire the gun! All threads start at once

        finishLatch.await(); // Wait for all threads to finish
        service.shutdown();

        System.out.println("Expected Count: " + expectedCount);
        System.out.println("Actual Count:   " + counter.get());

        if (counter.get() != expectedCount) {
            System.out.println(
                "❌ TEST PASSED: The code broke! Race condition detected."
            );
        } else {
            System.out.println(
                "⚠️ Test finished without error. Try increasing thread counts."
            );
        }
    }
}
