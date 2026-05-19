import java.util.concurrent.atomic.LongAdder;

// Problem Statement - Concurrent viewers count
public class BucketAggregation{
    // Shared mutable state
    private final long timeInSecondsFactor = 1_000_000_000L;
    // window size in seconds
    private final long windowSizeInSeconds;
    // bucket size in seconds
    private final long bucketSizeInSeconds;
    // buckets to count number of requests for particular ip
    private final Bucket[] buckets;
    // total buckets
    private final int numberOfBuckets;

    class Bucket{
        private long bucketStartSecond = 0;
        private final LongAdder counter = new LongAdder();

        // Synchronized method to safely update time and reset if old
        public synchronized void updateAndIncrement(long currentBucketStart) {
            if (this.bucketStartSecond != currentBucketStart) {
                this.counter.reset(); // Clear old data from past cycles
                this.bucketStartSecond = currentBucketStart;
            }
            this.counter.increment();
        }

        // Synchronized read to ensure we read accurate data relative to time
        public synchronized long getCountIfValid(long oldestValidSecond) {
            if (this.bucketStartSecond >= oldestValidSecond) {
                return this.counter.sum();
            }
            return 0; // Data is older than the window, ignore it
        }

        public synchronized void explicitReset() {
            this.counter.reset();
            this.bucketStartSecond = 0;
        }
    }

    BucketAggregation(long windowSizeInSeconds, long bucketSizeInSeconds) throws Exception{
        if(windowSizeInSeconds%bucketSizeInSeconds!=0)
            throw new Exception("Bucket size is invalid");

        this.windowSizeInSeconds = windowSizeInSeconds;
        this.bucketSizeInSeconds = bucketSizeInSeconds;

        this.numberOfBuckets = (int)(this.windowSizeInSeconds/this.bucketSizeInSeconds);
        this.buckets = new Bucket[numberOfBuckets];

        for(int i = 0; i<this.numberOfBuckets; ++i){
            buckets[i] = new Bucket();
        }
    }

    // increment
    public void increment(){
        long currentTimeSeconds = System.nanoTime() / timeInSecondsFactor;

        // Find the boundary start second of the current bucket (e.g., time 43 becomes 40 if bucket is 10s)
        long currentBucketStart = (currentTimeSeconds / bucketSizeInSeconds) * bucketSizeInSeconds;

        // FIXED: Divide by bucketSizeInSeconds to use all buckets evenly
        int index = (int) ((currentTimeSeconds / bucketSizeInSeconds) % numberOfBuckets);

        // Let the bucket handle its own thread-safe resetting and counting
        buckets[index].updateAndIncrement(currentBucketStart);
    }

    // return count
    public long getCount() {
        long currentTimeSeconds = System.nanoTime() / timeInSecondsFactor;
        long oldestValidSecond = currentTimeSeconds - windowSizeInSeconds;
        long sum = 0;

        // Only sum buckets that contain active, non-expired window data
        for (int i = 0; i < numberOfBuckets; ++i) {
            sum += buckets[i].getCountIfValid(oldestValidSecond);
        }

        return sum;
    }

    // reset
    public void reset(){
        for(int i = 0; i<numberOfBuckets; ++i){
            buckets[i].explicitReset();
        }
    }
}
