import java.util.concurrent.Semaphore;

public class OddEvenPrinter3 {

    Semaphore odd = new Semaphore(1);
    Semaphore even = new Semaphore(0);

    private int number = 1;
    private int limit;

    OddEvenPrinter3(int limit) {
        this.limit = limit;
    }

    public void printOdd() {
        try {
            while (number <= limit) {
                odd.acquire();

                if (number > limit) {
                    even.release();
                    return;
                }
                System.out.println(
                    number + " -> " + Thread.currentThread().getName()
                );
                number++;
                even.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printEven() {
        try {
            while (number <= limit) {
                even.acquire();
                if (number > limit) {
                    odd.release();
                    return;
                }

                System.out.println(
                    number + " -> " + Thread.currentThread().getName()
                );
                number++;
                odd.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        OddEvenPrinter3 oddeven = new OddEvenPrinter3(20);

        for (int i = 1; i < 5; ++i) {
            new Thread(oddeven::printOdd).start();
            new Thread(oddeven::printEven).start();
        }
    }
}
