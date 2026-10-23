package basics;

public class DeadlockDemo {

    private static final Object LOCK_A = new Object();
    private static final Object LOCK_B = new Object();

    public static void main(String[] args) {
        Thread a = new Thread(() -> {
            synchronized (LOCK_A) {
                System.out.println("LOCK_A acquired");
                sleepBriefly();

                System.out.println("Thread a Waiting for LOCK_B");
                synchronized (LOCK_B) {
                    System.out.println("A acquired both locks");
                }
            }
        }, "worker-A");

        Thread b = new Thread(() -> {
            synchronized (LOCK_B) {
                System.out.println("LOCK_B acquired");
                sleepBriefly();

                System.out.println("Thread b Waiting for LOCK_A");
                synchronized (LOCK_A) {
                    System.out.println("B acquired both locks");
                }
            }
        }, "worker-B");

        a.start();
        b.start();
    }

    private static void sleepBriefly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
