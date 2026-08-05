/*
Problem Statement
Design an asynchronous logging framework where multiple application threads generate log messages,
while dedicated logger threads write them to disk.

Requirements:
Application threads should never wait for slow disk I/O.
Log messages must be processed in FIFO order.
Multiple threads should be able to log concurrently.
Logging should support graceful shutdown.
Prevent unbounded memory growth.

Pattern
Asynchronous Producer–Consumer

Production-grade Approach
Use a bounded BlockingQueue<LogMessage> with dedicated logger thread(s). This is the architecture
used by Log4j2's AsyncAppender, Logback's AsyncAppender, and many high-performance logging systems.
*/

import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AsyncLogger {
    private final BlockingQueue<LogMessage> queue;
    private final Thread loggerThread;

    public AsyncLogger(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
        loggerThread = new Thread(() -> {
            try {
                while (true) {
                    LogMessage message = queue.take();
                    if (message == LogMessage.POISON_PILL)
                        break;
                    writeToDisk(message);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Flush remaining logs before exiting
            while (!queue.isEmpty()) {
                writeToDisk(queue.poll());
            }

        }, "Logger-Thread");

        loggerThread.start();
    }

    public void log(String message) throws InterruptedException {
        queue.put(new LogMessage(
                LocalDateTime.now(),
                Thread.currentThread().getName(),
                message
        ));
    }

    public void shutdown() throws InterruptedException {
        queue.put(LogMessage.POISON_PILL);
        loggerThread.join();
    }

    private void writeToDisk(LogMessage message) {
        // Simulate slow disk I/O
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf(
                "[%s] [%s] %s%n",
                message.timestamp,
                message.threadName,
                message.message
        );
    }

    private static class LogMessage {
        static final LogMessage POISON_PILL =
                new LogMessage(null, null, null);

        final LocalDateTime timestamp;
        final String threadName;
        final String message;

        LogMessage(
                LocalDateTime timestamp,
                String threadName,
                String message) {

            this.timestamp = timestamp;
            this.threadName = threadName;
            this.message = message;
        }
    }

    public static void main(String[] args) throws Exception {

        AsyncLogger logger = new AsyncLogger(10);
        Runnable application = () -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    logger.log(
                            "Processing Order-" + i
                    );
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        new Thread(application, "Order-Service-1").start();
        new Thread(application, "Order-Service-2").start();
        new Thread(application, "Order-Service-3").start();

        Thread.sleep(5000);
        logger.shutdown();
    }
}
