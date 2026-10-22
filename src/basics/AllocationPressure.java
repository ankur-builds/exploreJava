package basics;

import java.util.ArrayList;
import java.util.List;

public class AllocationPressure {

    public static void main(String[] args) {
        List<byte[]> retained = new ArrayList<>();
        Runtime runtime = Runtime.getRuntime();
        while (true) {
            retained.add(new byte[1024 * 1024]);
            System.out.println("Retained MB: " + retained.size());
            long used = runtime.totalMemory() - runtime.freeMemory();

            System.out.printf(
                "Used: %d MB | Committed: %d MB | Max: %d MB%n",
                used / 1_048_576,
                runtime.totalMemory() / 1_048_576,
                runtime.maxMemory() / 1_048_576
            );
        }
    }
}

/*
Run it with
javac AllocationPressure.java
java -Xms32m -Xmx64m AllocationPressure

Observe GC logs => java -Xms32m -Xmx64m '-Xlog:gc*,safepoint' AllocationPressure


*/
