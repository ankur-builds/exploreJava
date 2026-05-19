import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DelayedTaskSchedulerTest {

    public static void main(String[] args) {
        System.out.println("=== STARTING SCHEDULER TESTS ===");

        try {
            testTaskExecutesAfterDelay();
            testTasksExecuteInCorrectOrder();
            testConcurrentScheduling();

            System.out.println("\n✅ ALL TESTS PASSED SUCCESSFULLY!");
        } catch (Throwable e) {
            System.err.println("\n❌ TEST FAILED!");
            e.printStackTrace();
        } finally {
            System.out.println("Forcing shutdown of worker thread...");
            // (2) Force exit to stop the background infinite loop
            System.exit(0);
        }
    }

    static void testTaskExecutesAfterDelay() throws InterruptedException {
        System.out.print("Running: testTaskExecutesAfterDelay... ");
        DelayedTaskScheduler scheduler = new DelayedTaskScheduler();
        CountDownLatch latch = new CountDownLatch(1);
        long startTime = System.currentTimeMillis();
        AtomicLong executionTime = new AtomicLong(0);

        scheduler.schedule(() -> {
            executionTime.set(System.currentTimeMillis());
            latch.countDown();
        }, 500);

        boolean finished = latch.await(1000, TimeUnit.MILLISECONDS);
        if (!finished) throw new RuntimeException("Task timed out!");

        long actualDelay = executionTime.get() - startTime;
        if (actualDelay < 490) {
            throw new RuntimeException("Executed too early! Delay was: " + actualDelay + "ms");
        }
        System.out.println("Passed.");
    }

    static void testTasksExecuteInCorrectOrder() throws InterruptedException {
        System.out.print("Running: testTasksExecuteInCorrectOrder... ");
        DelayedTaskScheduler scheduler = new DelayedTaskScheduler();
        CountDownLatch latch = new CountDownLatch(3);
        StringBuilder executionOrder = new StringBuilder();

        scheduler.schedule(() -> {
            executionOrder.append("C");
            latch.countDown();
        }, 600);

        scheduler.schedule(() -> {
            executionOrder.append("A");
            latch.countDown();
        }, 100);

        scheduler.schedule(() -> {
            executionOrder.append("B");
            latch.countDown();
        }, 350);

        boolean finished = latch.await(1500, TimeUnit.MILLISECONDS);
        if (!finished) throw new RuntimeException("Tasks timed out!");

        if (!executionOrder.toString().equals("ABC")) {
            throw new RuntimeException("Wrong order! Got: " + executionOrder);
        }
        System.out.println("Passed.");
    }

    static void testConcurrentScheduling() throws InterruptedException {
        System.out.print("Running: testConcurrentScheduling... ");
        DelayedTaskScheduler scheduler = new DelayedTaskScheduler();
        int threadCount = 10;
        CountDownLatch finishedLatch = new CountDownLatch(threadCount);
        AtomicInteger completedTasks = new AtomicInteger(0);

        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                scheduler.schedule(() -> {
                    completedTasks.incrementAndGet();
                    finishedLatch.countDown();
                }, 100);
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        boolean finished = finishedLatch.await(1000, TimeUnit.MILLISECONDS);
        if (!finished) throw new RuntimeException("Concurrent tasks timed out!");
        if (completedTasks.get() != threadCount) {
            throw new RuntimeException("Lost tasks! Count: " + completedTasks.get());
        }
        System.out.println("Passed.");
    }
}
