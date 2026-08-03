import java.util.concurrent.Semaphore;

public class PrintInOrder3{
    Semaphore second = new Semaphore(0);
    Semaphore third = new Semaphore(0);

    // This approach
    public void first(){
        System.out.println("First :: " + Thread.currentThread().getName());
        second.release(); // Increments the permit count from 0 to 1 and wakes up thread
    }

    public void second(){
        try{
            second.acquire();
            System.out.println("Second :: " + Thread.currentThread().getName());
            third.release();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public void third(){
        try {
            third.acquire();
            System.out.println("Third :: " + Thread.currentThread().getName());
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        PrintInOrder3 printer = new PrintInOrder3();

        System.out.println("-----------------------------");
        new Thread(printer::first).start();

        new Thread(printer::second).start();

        new Thread(printer::third).start();
        System.out.println("-----------------------------");
    }
}
