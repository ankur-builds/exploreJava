
public class PrintInOrder{
    private int state = 1;
    public synchronized void first() throws InterruptedException{
        System.out.println("First method");
        state = 2;
        notifyAll();
    }

    public synchronized void second() throws InterruptedException{
        // Thread may wake up because of any condition changed and all threads were notified
        // the invariant must always be rechecked. Never use if to check state
        while(state!=2){
            wait();
        }

        System.out.println("Second method");
        state = 3;
        notifyAll();
    }

    public synchronized void third() throws InterruptedException{
        while(state!=3){
            wait();
        }
        System.out.println("Third method");
        notifyAll();
    }

    public static void main(String[] args) {
        PrintInOrder printer = new PrintInOrder();

        System.out.println("-----------------------------");
        new Thread(() -> {
            try {
                printer.first();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }).start();

        new Thread(() -> {
            try {
                printer.second();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        new Thread(() -> {
            try {
                printer.third();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        System.out.println("-----------------------------");
    }
}
