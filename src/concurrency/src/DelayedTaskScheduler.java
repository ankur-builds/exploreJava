import java.lang.Runnable;
import java.util.PriorityQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class ScheduledTask {
    Runnable command;
    long executeAt;

    ScheduledTask(Runnable cmd, long delay){
        this.command = cmd;
        this.executeAt = System.currentTimeMillis() + delay;
    }
}

public class DelayedTaskScheduler {
    private final PriorityQueue<ScheduledTask> pq = new PriorityQueue<>((t1, t2) -> Long.compare(t1.executeAt, t2.executeAt));

    public DelayedTaskScheduler() {
        Thread worker = new Thread(this::workerLoop);
        worker.start();
    }

    public synchronized void schedule(Runnable task, long delayMillis){
        pq.add(new ScheduledTask(task, delayMillis));
        notifyAll();
    }

    /**
     * Note that this ScheduledTask object obtained from pq.poll() is no longer shared after poll()
     * Why because pq.poll() REMOVES ownership from queue.
     * After poll:
     *  scheduler thread exclusively owns reference.
     *
     * This is VERY important concurrency principle: ownership transfer
     *
     * Why synchronized(this) instead of ReentrantLock?
     *      because wait()/notify() integrate naturally with monitor locks.
     */
    public void workerLoop(){
        while(true){
            try{
            ScheduledTask taskToRun;
            synchronized(this){
                while(pq.isEmpty())
                    wait();

                long now = System.currentTimeMillis();
                ScheduledTask task = pq.peek();

                if(task.executeAt>now){
                    wait(task.executeAt-now);
                    continue;
                }

                taskToRun = pq.poll();
            }

            taskToRun.command.run();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
