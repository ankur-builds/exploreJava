import java.util.concurrent.Delayed;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyDelayQueue<E extends Delayed> {

    private final PriorityBlockingQueue<E> queue = new PriorityBlockingQueue<>();

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition available = lock.newCondition();

    // Leader-Follower Optimization
    // Only one thread waits with timeout.
    // Others wait indefinitely.
    private Thread leader;

    public void put(E item) {

        lock.lock();

        try {

            queue.offer(item);

            // New item expires earlier than current head.
            if (queue.peek() == item) {

                leader = null;

                available.signal();
            }

        } finally {
            lock.unlock();
        }
    }

    public E take() throws InterruptedException {

        lock.lockInterruptibly();

        try {

            while (true) {

                E first = queue.peek();

                if (first == null) {

                    available.await();

                    continue;
                }

                long delay = first.getDelay(TimeUnit.NANOSECONDS);

                if (delay <= 0) {

                    return queue.poll();
                }

                // Another thread is already the leader.
                if (leader != null) {

                    available.await();

                    continue;
                }

                Thread thisThread = Thread.currentThread();

                leader = thisThread;

                try {

                    available.awaitNanos(delay);

                } finally {

                    if (leader == thisThread)
                        leader = null;
                }
            }

        } finally {

            // Elect next leader.
            if (leader == null && queue.peek() != null)
                available.signal();

            lock.unlock();
        }
    }

    public int size() {

        lock.lock();

        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public static class DelayedTask implements Delayed {

        private final String task;
        private final long triggerTime;

        public DelayedTask(String task, long delay, TimeUnit unit) {

            this.task = task;

            this.triggerTime =
                    System.nanoTime() + unit.toNanos(delay);
        }

        @Override
        public long getDelay(TimeUnit unit) {

            return unit.convert(
                    triggerTime - System.nanoTime(),
                    TimeUnit.NANOSECONDS
            );
        }

        @Override
        public int compareTo(Delayed other) {

            return Long.compare(
                    this.getDelay(TimeUnit.NANOSECONDS),
                    other.getDelay(TimeUnit.NANOSECONDS)
            );
        }

        @Override
        public String toString() {
            return task;
        }
    }

    public static void main(String[] args) {

        MyDelayQueue<DelayedTask> queue = new MyDelayQueue<>();

        queue.put(new DelayedTask("Task-5", 5, TimeUnit.SECONDS));
        queue.put(new DelayedTask("Task-2", 2, TimeUnit.SECONDS));
        queue.put(new DelayedTask("Task-8", 8, TimeUnit.SECONDS));
        queue.put(new DelayedTask("Task-1", 1, TimeUnit.SECONDS));

        Runnable consumer = () -> {

            while (true) {

                try {

                    DelayedTask task = queue.take();

                    System.out.println(
                            System.currentTimeMillis()
                                    + " -> "
                                    + Thread.currentThread().getName()
                                    + " processed "
                                    + task
                    );

                } catch (InterruptedException e) {

                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        new Thread(consumer, "Consumer-1").start();
        new Thread(consumer, "Consumer-2").start();
        new Thread(consumer, "Consumer-3").start();
    }
}
