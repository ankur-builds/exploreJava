import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceTest {

    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        CountDownLatch readyLatch = new CountDownLatch(3);
        CountDownLatch startLatch = new CountDownLatch(1);

        for (int i = 0; i < 3; ++i) {
            executor.submit(() -> {
                readyLatch.countDown();
                System.out.printf("Count : %d%n", readyLatch.getCount());
                try {
                    startLatch.await();
                    System.out.println(Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        System.out.printf("Final Count : %d%n", readyLatch.getCount());
        readyLatch.await();
        startLatch.countDown();
        executor.shutdown();
    }
}
