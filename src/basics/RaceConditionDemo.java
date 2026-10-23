public class RaceConditionDemo {

    private static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        int incrementsPerThread = 1_000_000;

        Thread a = new Thread(() -> {
            for (int i = 0; i < incrementsPerThread; i++) {
                count++;
            }
        });

        Thread b = new Thread(() -> {
            for (int i = 0; i < incrementsPerThread; i++) {
                count++;
            }
        });

        a.start();
        b.start();

        a.join();
        b.join();

        System.out.println("Expected: " + 2 * incrementsPerThread);
        System.out.println("Actual:   " + count);
    }
}
