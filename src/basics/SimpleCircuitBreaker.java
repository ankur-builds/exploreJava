package basics;

import java.time.Duration;
import java.util.concurrent.Callable;

public class SimpleCircuitBreaker {

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN,
    }

    private final int failureThreshold;
    private final long openDurationNanos;

    private State state = State.CLOSED;
    private int consecutiveFailures;
    private long openedAtNanos;
    private boolean probeInFlight;

    public SimpleCircuitBreaker(int failureThreshold, Duration openDuration) {
        if (failureThreshold < 1) {
            throw new IllegalArgumentException(
                "failureThreshold must be positive"
            );
        }

        if (openDuration.isNegative() || openDuration.isZero()) {
            throw new IllegalArgumentException("openDuration must be positive");
        }

        this.failureThreshold = failureThreshold;
        this.openDurationNanos = openDuration.toNanos();
    }

    public <T> T execute(Callable<T> operation) throws Exception {
        acquirePermission();

        try {
            T result = operation.call();
            recordSuccess();
            return result;
        } catch (Exception | Error e) {
            recordFailure();
            throw e;
        }
    }

    private synchronized void acquirePermission() {
        if (state == State.OPEN) {
            long elapsed = System.nanoTime() - openedAtNanos;

            if (elapsed < openDurationNanos) {
                throw new CircuitBreakerOpenException(
                    "Circuit breaker is OPEN"
                );
            }

            state = State.HALF_OPEN;
            probeInFlight = false;
        }

        if (state == State.HALF_OPEN) {
            if (probeInFlight) {
                throw new CircuitBreakerOpenException(
                    "Half-open probe already in progress"
                );
            }

            probeInFlight = true;
        }
    }

    private synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state = State.CLOSED;
            consecutiveFailures = 0;
            probeInFlight = false;
            return;
        }

        consecutiveFailures = 0;
    }

    private synchronized void recordFailure() {
        if (state == State.HALF_OPEN) {
            open();
            return;
        }

        consecutiveFailures++;

        if (consecutiveFailures >= failureThreshold) {
            open();
        }
    }

    private void open() {
        state = State.OPEN;
        openedAtNanos = System.nanoTime();
        probeInFlight = false;
    }

    public synchronized State getState() {
        return state;
    }

    public static class CircuitBreakerOpenException extends RuntimeException {

        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }
}
