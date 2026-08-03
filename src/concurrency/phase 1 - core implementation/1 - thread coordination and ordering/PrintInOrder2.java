import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class PrintInOrder2{
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    private int state = 1;
    public void first(){
        lock.lock();
        try{
            while(state!=1)
                condition.await();

            System.out.println("First :: " + Thread.currentThread().getName());
            state = 2;
            condition.signalAll();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } finally{
            lock.unlock();
        }
    }

    public void second(){
        lock.lock();
        try{
            while(state!=2)
                condition.await();
            System.out.println("Second :: " + Thread.currentThread().getName());
            state = 3;
            condition.signalAll();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public void third(){
        lock.lock();
        try{
            while(state!=3)
                condition.await();
            System.out.println("Third :: " + Thread.currentThread().getName());
            state = 1;
            condition.signalAll();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        PrintInOrder2 printer = new PrintInOrder2();

        System.out.println("-----------------------------");
        new Thread(printer::first).start();

        new Thread(printer::second).start();

        new Thread(printer::third).start();
        System.out.println("-----------------------------");
    }
}
