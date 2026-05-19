import java.util.*;

public class RequestCounter {

    private final Map<String, RequestStore> map = new HashMap<>();

    private final long windowNanos;

    public RequestCounter(int seconds) {
        this.windowNanos = seconds * 1_000_000_000L;
    }

    public void record(String ip) {
        RequestStore store;

        synchronized (map) {
            map.putIfAbsent(ip, new RequestStore());

            store = map.get(ip);
        }

        synchronized (store) {
            long now = System.nanoTime();

            cleanup(store.dq, now);

            store.dq.addLast(now);
        }
    }

    private void cleanup(Deque<Long> dq, long now) {
        while (!dq.isEmpty() && now - dq.peekFirst() > windowNanos) {
            dq.removeFirst();
        }
    }

    static class RequestStore {

        Deque<Long> dq = new ArrayDeque<>();
    }
}
