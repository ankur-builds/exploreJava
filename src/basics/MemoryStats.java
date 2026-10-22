package basics;

public class MemoryStats {

    public static void main(String[] args) throws InterruptedException {
        Runtime runtime = Runtime.getRuntime();

        for (int i = 0; i < 5; i++) {
            long used = runtime.totalMemory() - runtime.freeMemory();

            System.out.printf(
                "Used: %d MB | Committed: %d MB | Max: %d MB%n",
                used / 1_048_576,
                runtime.totalMemory() / 1_048_576,
                runtime.maxMemory() / 1_048_576
            );

            Thread.sleep(1000);
        }
    }
}

/*
Run it with
javac MemoryStats.java
java -Xms128m -Xmx256m MemoryStats
*/
