public class ABCABC {

    private char state = 'A';
    private int iteration = 0;
    private int limit;

    public ABCABC(int limit){
        this.limit = limit;
    }

    public synchronized void printA() {
        try {
            while (iteration < limit) {
                while (state != 'A' && iteration < limit) {
                    wait();
                }
                if(iteration >= limit)
                    break;

                System.out.println("count :" + iteration + " -> A");
                state = 'B';
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void printB() {
        try {
            while (iteration < limit) {
                while (state != 'B' && iteration < limit) {
                    wait();
                }

                if(iteration >= limit)
                    break;

                System.out.println("count :" + iteration + " -> B");
                state = 'C';
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void printC() {
        try {
            while (iteration < limit) {
                while (state != 'C') {
                    wait();
                }
                System.out.println("count :" + iteration + " -> C");
                state = 'A';
                iteration++;
                notifyAll();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ABCABC abc = new ABCABC(5);

        new Thread(abc::printA).start();
        new Thread(abc::printB).start();
        new Thread(abc::printC).start();
    }
}
