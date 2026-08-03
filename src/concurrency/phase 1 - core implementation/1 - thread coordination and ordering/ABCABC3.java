import java.util.concurrent.Semaphore;

public class ABCABC3 {

    private final Semaphore a = new Semaphore(1);
    private final Semaphore b = new Semaphore(0);
    private final Semaphore c = new Semaphore(0);

    private final int times;

    ABCABC3(int times) {
        this.times = times;
    }

    public void printA() {
        try {
            for (int i = 0; i < times; i++) {
                a.acquire();

                System.out.print("A");

                b.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printB() {
        try {
            for (int i = 0; i < times; i++) {
                b.acquire();

                System.out.print("B");

                c.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printC() {
        try {
            for (int i = 0; i < times; i++) {
                c.acquire();

                System.out.print("C");

                a.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        ABCABC3 abc = new ABCABC3(5);

        new Thread(abc::printA).start();
        new Thread(abc::printB).start();
        new Thread(abc::printC).start();
    }
}
