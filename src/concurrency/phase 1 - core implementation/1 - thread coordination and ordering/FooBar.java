import java.util.concurrent.Semaphore;

public class FooBar {

    Semaphore foo = new Semaphore(1);
    Semaphore bar = new Semaphore(0);

    private int limit;
    private int iteration = 1;

    FooBar(int limit) {
        this.limit = limit;
    }

    public void printFoo() {
        try {
            for(int i = 0; i<limit; ++i) {
                foo.acquire();
                System.out.printf("foo");
                bar.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void printBar() {
        try {
            for(int i = 0; i<limit; ++i) {
                bar.acquire();
                System.out.printf("bar");
                foo.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        FooBar foobar = new FooBar(3);

        new Thread(foobar::printFoo).start();
        new Thread(foobar::printBar).start();
    }
}
