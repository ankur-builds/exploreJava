package basics;

public class TemporaryAllocation {

    public static void main(String[] args) {
        long count = 0;
        Runtime runtime = Runtime.getRuntime();

        while (count < 100_000_000L) {
            byte[] temporary = new byte[1024];
            count++;

            if (count % 1_000_000 == 0) {
                System.out.println("Created: " + count);
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
}

/*
Run it with
javac TemporaryAllocation.java
java -Xms32m -Xmx64m TemporaryAllocation
*/
