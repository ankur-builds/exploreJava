import java.util.*;

public class SlidingWindowRequestCounter {

    private final Map<String, Deque<Long>> map = new HashMap<>();

    private final long windowNanos;

    public SlidingWindowRequestCounter(int windowSeconds) {
        this.windowNanos = windowSeconds * 1_000_000_000L;
    }

    public synchronized void recordRequest(String ip) {
        long now = System.nanoTime();

        map.putIfAbsent(ip, new ArrayDeque<>());

        Deque<Long> dq = map.get(ip);

        cleanup(dq, now);

        dq.addLast(now);
    }

    public synchronized int getRequestCount(String ip) {
        if (!map.containsKey(ip)) {
            return 0;
        }

        long now = System.nanoTime();

        Deque<Long> dq = map.get(ip);

        cleanup(dq, now);

        return dq.size();
    }

    private void cleanup(Deque<Long> dq, long now) {
        while (!dq.isEmpty() && now - dq.peekFirst() > windowNanos) {
            dq.removeFirst();
        }
    }
}
