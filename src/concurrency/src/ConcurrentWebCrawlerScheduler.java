import java.util.*;
import java.util.concurrent.*;
import java.lang.Thread;

public class ConcurrentWebCrawlerScheduler{
    // Shared mutable state
    private final Queue<ScheduledTask> queue = new ArrayDeque<>();
    private final Set<String> scheduledUrls= new HashSet<>();
    private final ExecutorService service = Executors.newFixedThreadPool(100);

    class ScheduledTask{
        String url;
        Runnable task;

        ScheduledTask(String url){
            this.url = url;
            task = () -> {
                // Call url via this crawler
            };
        }
    }

    ConcurrentWebCrawlerScheduler(){
        Thread thread = new Thread(this::processURLs);
        thread.start();
    }

    public synchronized void submitURLs(String url){
        if (!scheduledUrls.contains(url)){
            queue.add(new ScheduledTask(url));
            scheduledUrls.add(url);
            notifyAll();
        }
    }

    public void processURLs(){
        while(true){
            try{
                ScheduledTask taskToRun;
                synchronized(this){
                    while(queue.isEmpty())
                        wait();

                        taskToRun = queue.poll();
                }
                service.submit(taskToRun.task);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * ExecutorService is already internally thread-safe.
     * If shutdown occurs:
     *      existing running tasks continue
     *      queued tasks continue
     *      NEW submissions rejected
     *
     * This is controlled internally by executor state machine.
     */
    public void shutdown(){
        service.shutdown();
    }
}

// Alternate solution
class ConcurWebCrawlerScheduler {
    // 1. Thread-safe, lock-free set for deduplication
    private final Set<String> scheduledUrls = ConcurrentHashMap.newKeySet();

    // 2. Thread pool with a bounded queue to prevent OutOfMemoryErrors
    private final ExecutorService executorService;

    public ConcurWebCrawlerScheduler() {
        // 100 threads, maximum 10,000 tasks queued in memory at once
        this.executorService = new ThreadPoolExecutor(
            100, 100,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(10000),
            new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure: Producer slows down if queue is full
        );
    }

    public void submitURL(final String url) {
        // add() returns true only if the URL was NOT already in the set
        if (scheduledUrls.add(url)) {
            executorService.submit(() -> crawl(url));
        }
    }

    private void crawl(String url) {
        try {
            System.out.println(Thread.currentThread().getName() + " is crawling: " + url);
            // Put your actual network request / parsing logic here

        } catch (Exception e) {
            System.err.println("Error crawling " + url + ": " + e.getMessage());
        }
    }

    // Clean shutdown mechanism
    public void shutdown() {
        executorService.shutdown();
    }
}
