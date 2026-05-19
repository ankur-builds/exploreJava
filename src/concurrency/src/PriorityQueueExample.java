import java.util.concurrent.PriorityBlockingQueue;

// Elements must implement Comparable to define priority
class PriorityTask implements Comparable<PriorityTask> {
    private final int priority;
    private final String taskName;

    public PriorityTask(int priority, String taskName) {
        this.priority = priority;
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    // Lower numbers mean higher priority (e.g., 1 is urgent, 5 is low)
    @Override
    public int compareTo(PriorityTask other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public String toString() {
        return "Task[Priority=" + priority + ", Name='" + taskName + "']";
    }
}

public class PriorityQueueExample {
    public static void main(String[] args) throws InterruptedException {
        // Create the thread-safe priority blocking queue
        PriorityBlockingQueue<PriorityTask> queue = new PriorityBlockingQueue<>();

        // 1. Producer Thread: Adds tasks to the queue asynchronously
        Thread producer = new Thread(() -> {
            System.out.println("[Producer] Adding tasks...");
            queue.put(new PriorityTask(3, "Low Priority Task A"));
            queue.put(new PriorityTask(1, "URGENT Task B")); // Highest priority
            queue.put(new PriorityTask(5, "Very Low Priority Task C"));
            queue.put(new PriorityTask(2, "High Priority Task D"));
            System.out.println("[Producer] Finished adding 4 tasks.");
        });

        // 2. Consumer Thread: Safely processes tasks based on priority
        Thread consumer = new Thread(() -> {
            try {
                // Process 4 tasks
                for (int i = 0; i < 4; i++) {
                    // take() blocks automatically if the queue is empty
                    PriorityTask task = queue.take();
                    System.out.println("[Consumer] Processed: " + task);

                    // Simulate processing time
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start both threads
        consumer.start();
        producer.start();

        // Wait for both threads to finish
        producer.join();
        consumer.join();

        System.out.println("All tasks processed successfully.");
    }
}
