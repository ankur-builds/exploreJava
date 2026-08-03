import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class OddEvenPrinter2 {

    private int number = 1;
    private int limit;

    private final ReentrantLock lock = new ReentrantLock();
    Condition odd = lock.newCondition();
    Condition even = lock.newCondition();

    OddEvenPrinter2(int limit) {
        this.limit = limit;
    }

    public void printOdd() {
        lock.lock();
        try {
            while (true) {
                while (number <= limit && number % 2 == 0)
                    odd.await();

                if (number > limit) {
                    even.signalAll();
                    return;
                }

                System.out.println(
                    number + " -> " + Thread.currentThread().getName()
                );
                number++;
                even.signalAll();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void printEven() {
        lock.lock();
        try {
            while (true) {
                while (number <= limit && number % 2 == 1)
                    even.await();

                if (number > limit) {
                    odd.signalAll();
                    return;
                }

                System.out.println(
                    number + " -> " + Thread.currentThread().getName()
                );
                number++;
                odd.signalAll();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        OddEvenPrinter2 oddeven = new OddEvenPrinter2(20);

        for(int i = 1; i< 5; ++i){
            new Thread(oddeven::printOdd).start();
            new Thread(oddeven::printEven).start();
        }

        /*
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }

        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getName().startsWith("Thread-")) {

                System.out.println(
                    "\n" + t.getName() + " : " + t.getState()
                );

                for (StackTraceElement e : t.getStackTrace())
                    System.out.println("\t" + e);
            }
        }
        */
    }
}
