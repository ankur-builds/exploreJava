public class TokenBucketRateLimiter {

    private final long capacity;
    private final long refillTokensPerSecond;
    private long availableTokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter(long capacity, long refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.availableTokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    /*
    1. calculate elapsed time
    2. refill tokens
    3. cap at capacity
    4. if token available:
          consume token
          allow request
       else:
          reject request
    */
    public synchronized boolean allowRequest() {
        refill();

        if (availableTokens > 0) {
            availableTokens--;

            return true;
        }

        return false;
    }

    private void refill() {
        long now = System.nanoTime();

        long elapsedNanos = now - lastRefillTime;

        long tokensToAdd =
            (elapsedNanos * refillTokensPerSecond) / 1_000_000_000L;

        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);

            lastRefillTime = now;
        }
    }
}
