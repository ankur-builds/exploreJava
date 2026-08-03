import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ABCABC2 {

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition a = lock.newCondition();
    private final Condition b = lock.newCondition();
    private final Condition c = lock.newCondition();

    private int turn = 0;
    private final int times;

    ABCABC2(int times) {
        this.times = times;
    }

    public void printA() {

        lock.lock();

        try {

            for (int i = 0; i < times; i++) {

                while (turn != 0)
                    a.await();

                System.out.print("A");

                turn = 1;

                b.signal();
            }

        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void printB() {

        lock.lock();

        try {

            for (int i = 0; i < times; i++) {

                while (turn != 1)
                    b.await();

                System.out.print("B");

                turn = 2;

                c.signal();
            }

        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void printC() {

        lock.lock();

        try {

            for (int i = 0; i < times; i++) {

                while (turn != 2)
                    c.await();

                System.out.print("C");

                turn = 0;

                a.signal();
            }

        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        ABCABC2 abc = new ABCABC2(5);

        new Thread(abc::printA).start();
        new Thread(abc::printB).start();
        new Thread(abc::printC).start();
    }
}
