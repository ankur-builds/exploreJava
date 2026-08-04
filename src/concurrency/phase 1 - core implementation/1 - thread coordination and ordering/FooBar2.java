import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FooBar2 {

    ReentrantLock lock = new ReentrantLock();
    Condition fooCondition = lock.newCondition();
    Condition barCondition = lock.newCondition();

    private boolean fooTurn = true;
    private int limit;

    FooBar2(int limit) {
        this.limit = limit;
    }

    public void printFoo() {
        for (int i = 0; i < limit; ++i) {
            lock.lock();
            try {
                while (fooTurn) {
                    barCondition.await();
                }

                System.out.printf("bar");
                fooTurn = true;
                fooCondition.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public void printBar() {
        for (int i = 0; i < limit; ++i) {
            lock.lock();
            try {
                while (!fooTurn) {
                    fooCondition.await();
                }

                System.out.printf("foo");
                fooTurn = false;
                barCondition.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        FooBar2 foobar2 = new FooBar2(3);

        new Thread(foobar2::printFoo).start();
        new Thread(foobar2::printBar).start();
    }
}
