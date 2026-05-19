import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implement following method of ScheduledExecutorService interface in Java

 schedule(Runnable command, long delay, TimeUnit unit)
 Creates and executes a one-shot action that becomes enabled after the given delay.

 scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit)
 Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently
 with the given period; that is executions will commence after initialDelay then initialDelay+period, then
 initialDelay + 2 * period, and so on.

 scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit)
 Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently
 with the given delay between the termination of one execution and the commencement of the next.
 */

 public class ScheduledExecutorImpl{
     class ScheduledTask{
        Runnable command;
        long executeAt;
        long offset;
        enum TaskType{
            ONE_SHOT,
            FIXED_RATE,
            FIXED_DELAY
        };

        TaskType taskType;

        ScheduledTask(Runnable command, long delay, long offset, TaskType type){
            this.command = command;
            this.executeAt = System.nanoTime() + delay;
            this.taskType = type;
            this.offset = offset;
        }
    }

    // Shared mutable state
    PriorityQueue<ScheduledTask> pq = new PriorityQueue<>((p1,p2) -> Long.compare(p1.executeAt, p2.executeAt));

    ScheduledExecutorImpl(){
        Thread thread = new Thread(this::workLoop);
        thread.start();
    }

    public synchronized void schedule(Runnable command, long delay, TimeUnit unit){
        pq.add(new ScheduledTask(command, unit.toNanos(delay), 0, ScheduledTask.TaskType.ONE_SHOT));
        notifyAll();
    }

    public synchronized void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        pq.add(new ScheduledTask(command, unit.toNanos(initialDelay), unit.toNanos(period), ScheduledTask.TaskType.FIXED_RATE));
        notifyAll();
    }

    public synchronized void scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        pq.add(new ScheduledTask(command, unit.toNanos(initialDelay), unit.toNanos(delay), ScheduledTask.TaskType.FIXED_DELAY));
        notifyAll();
    }

    public synchronized void schedulePeriodicTasks(ScheduledTask taskToRun){
        switch(taskToRun.taskType){
            case ScheduledTask.TaskType.ONE_SHOT:
                 return;
             case ScheduledTask.TaskType.FIXED_RATE:
                 taskToRun.executeAt += taskToRun.offset;
                 pq.add(taskToRun);
                 break;
             case ScheduledTask.TaskType.FIXED_DELAY:
                 taskToRun.executeAt = System.nanoTime() + taskToRun.offset;
                 pq.add(taskToRun);
                 break;
        }

        notifyAll();
    }

    public void workLoop(){
        while(true){
            try{
                ScheduledTask taskToRun;

                synchronized(this){
                    while(pq.isEmpty())
                       wait();

                       long now = System.nanoTime();
                       ScheduledTask next = pq.peek();
                       if(next.executeAt > now){
                           long waitTimeInMillis = Math.max(1,(next.executeAt - now) / 1_000_000L);
                           wait(waitTimeInMillis);
                           continue;
                       }
                       taskToRun = pq.poll();
                }
                taskToRun.command.run();
                schedulePeriodicTasks(taskToRun);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
