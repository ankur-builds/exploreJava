import java.util.PriorityQueue;
import java.util.Comparator;

public class PriorityQueueImpl{
    public static void main(String[] args) {
        // 1. Default Min heap
        // This requires no arguments. It automatically sorts integers from smallest to largest.
        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        // 2. Reverse Order (Max-Heap)
        // Use Java's built-in factory method.
        // This avoids lambda expressions entirely and sorts integers from largest to smallest.
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

        // 3. Anonymous Class (Traditional Way)
        // Before lambdas existed, this was the standard way to define custom sorting.
        // It explicitly shows the underlying compare method.
        PriorityQueue<Integer> customHeap = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer a, Integer b) {
                return Integer.compare(b, a); // Descending order (Max-Heap)
            }
        });

        // 4. Method Reference (Cleanest Modern Way)
        // If you want to use modern Java syntax but find lambdas confusing, method references are highly readable.
        PriorityQueue<Integer> methodRefHeap = new PriorityQueue<>(Integer::compare);

        // 5. Lambda Expression
        // A lambda expression is just a shortcut for the anonymous class.
        // It strips away all the wordy boilerplate code and only keeps the input variables and the math logic.
        PriorityQueue<Integer>lambdaHeap = new PriorityQueue<>(
            (a,b) -> Integer.compare(a,b)
        );
    }
}
