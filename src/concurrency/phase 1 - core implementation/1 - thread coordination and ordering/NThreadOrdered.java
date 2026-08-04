public class NThreadOrdered{

    private long turn = 0;
    private int n;

    public NThreadOrdered(int threads){
        n = threads;
    }

    public synchronized void executeThread(){
        try{
            String thread =  Thread.currentThread().getName();
            String number = thread.substring(thread.lastIndexOf('-') + 1);
            int id = Integer.valueOf(number) % n;
            while(id != turn){
                wait();
            }

            turn = (turn+1)%n;
            System.out.println(Thread.currentThread().getName());
            notifyAll();
        } catch(InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }
    public static void main(String[] args) {
        NThreadOrdered threads = new NThreadOrdered(8);

        for(int i = 0; i<20; ++i){
            new Thread(threads::executeThread).start();
        }
    }
}
