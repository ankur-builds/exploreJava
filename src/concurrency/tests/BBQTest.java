import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BBQTest {

    public static void main(String[] args) throws InterruptedException {
        int numberOfThreads = 100;
        int numberOfIterationPerThread = 1000;

        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        executor.submit(() -> {
            System.out.println(Thread.currentThread().getName());
        });

        executor.shutdown();
    }
}
