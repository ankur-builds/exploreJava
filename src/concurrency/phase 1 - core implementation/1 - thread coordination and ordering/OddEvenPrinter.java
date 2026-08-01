public class OddEvenPrinter {

    private int number = 1;
    private final int limit;

    OddEvenPrinter(int limit) {
        this.limit = limit;
    }

    public synchronized void printOdd() throws InterruptedException {
        while (number <= limit) {
            while (number <= limit && number % 2 == 0) {
                wait();
            }

            if (number > limit) break;

            System.out.println(
                Thread.currentThread().getName() + " -> " + number
            );

            number++;
            notifyAll();
        }
    }

    public synchronized void printEven() throws InterruptedException {
        while (number <= limit) {
            while (number <= limit && number % 2 != 0) {
                wait();
            }

            if (number > limit) break;

            System.out.println(
                Thread.currentThread().getName() + " -> " + number
            );

            number++;
            notifyAll();
        }
    }

    public static void main(String[] args) {
        OddEvenPrinter printer = new OddEvenPrinter(10);

        Thread odd = new Thread(() -> {
            try {
                printer.printOdd();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "ODD");

        Thread even = new Thread(() -> {
            try {
                printer.printEven();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "EVEN");

        odd.start();
        even.start();
    }
}
