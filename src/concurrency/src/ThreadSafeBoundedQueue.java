import java.util.*;

public class ThreadSafeBoundedQueue<T>{
    // Shared mutable state
    Queue<T> queue = new ArrayDeque<>();
    int capacity;

    ThreadSafeBoundedQueue(int capacity){
        this.capacity = capacity;
    }

    public synchronized void add(T item){
        try{
            if(this.capacity==queue.size())
                wait();

            queue.add(item);
            notifyAll();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized T remove(){
        try{
            if(queue.isEmpty())
                wait();
            T item = queue.remove();
            notifyAll();
            return item;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean contains(T item){
        if(queue.isEmpty())
            return false;

        return queue.contains(item);
    }

    public synchronized int size(){
        return queue.size();
    }
}
