import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ScheduledExecutorTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Starting ScheduledExecutorImpl Tests ===");
        testOneShotTask();
        testFixedRateTask();
        testFixedDelayTask();
        testEdgeCaseZeroDelay();
        testEdgeCaseTaskThrowsException();
        testEdgeCaseMultipleOverlappingTasks();
        System.out.println("\n=== All Tests Passed Successfully! ===");
    }

    // Test 1: Simple One-Shot Execution
    private static void testOneShotTask() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(latch::countDown, 100, TimeUnit.MILLISECONDS);

        boolean completed = latch.await(500, TimeUnit.MILLISECONDS);
        assertResult("Test One-Shot Task", completed);
    }

    // Test 2: Fixed Rate execution runs multiple times
    private static void testFixedRateTask() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        CountDownLatch latch = new CountDownLatch(3); // Wait for 3 executions

        executor.scheduleAtFixedRate(latch::countDown, 10, 50, TimeUnit.MILLISECONDS);

        boolean completed = latch.await(300, TimeUnit.MILLISECONDS);
        assertResult("Test Fixed Rate Task", completed);
    }

    // Test 3: Fixed Delay execution runs multiple times
    private static void testFixedDelayTask() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        CountDownLatch latch = new CountDownLatch(3);

        executor.scheduleWithFixedDelay(() -> {
            try { Thread.sleep(20); } catch (InterruptedException ignored) {} // Simulate task duration
            latch.countDown();
        }, 10, 50, TimeUnit.MILLISECONDS);

        boolean completed = latch.await(400, TimeUnit.MILLISECONDS);
        assertResult("Test Fixed Delay Task", completed);
    }

    // Edge Case 1: Zero delay should execute immediately
    private static void testEdgeCaseZeroDelay() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        CountDownLatch latch = new CountDownLatch(1);

        executor.schedule(latch::countDown, 0, TimeUnit.MILLISECONDS);

        boolean completed = latch.await(50, TimeUnit.MILLISECONDS);
        assertResult("Edge Case: Zero Delay", completed);
    }

    // Edge Case 2: A failing task should not crash the work loop
    private static void testEdgeCaseTaskThrowsException() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        CountDownLatch latch = new CountDownLatch(1);

        // This task crashes
        executor.schedule(() -> { throw new RuntimeException("Simulated Crash"); }, 10, TimeUnit.MILLISECONDS);
        // This subsequent task should still succeed
        executor.schedule(latch::countDown, 50, TimeUnit.MILLISECONDS);

        boolean completed = latch.await(200, TimeUnit.MILLISECONDS);
        assertResult("Edge Case: Task Throws Exception", completed);
    }

    // Edge Case 3: Multiple tasks scheduled concurrently should execute in order
    private static void testEdgeCaseMultipleOverlappingTasks() throws InterruptedException {
        ScheduledExecutorImpl executor = new ScheduledExecutorImpl();
        AtomicInteger orderTracker = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(2);

        // Task 2 has longer delay, scheduled first
        executor.schedule(() -> {
            orderTracker.compareAndSet(1, 2); // Should run second
            latch.countDown();
        }, 100, TimeUnit.MILLISECONDS);

        // Task 1 has shorter delay, scheduled second
        executor.schedule(() -> {
            orderTracker.compareAndSet(0, 1); // Should run first
            latch.countDown();
        }, 10, TimeUnit.MILLISECONDS);

        latch.await(300, TimeUnit.MILLISECONDS);
        assertResult("Edge Case: Chronological Task Ordering", orderTracker.get() == 2);
    }

    private static void assertResult(String testName, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + testName);
        } else {
            System.err.println("[FAIL] " + testName);
            throw new AssertionError(testName + " failed!");
        }
    }
}
