/*
Problem Statement
Design a file processing system that reads a large file, transforms its contents, and writes the
processed data to another file. Reading, processing, and writing should happen concurrently so
that slow I/O or CPU-intensive processing in one stage doesn't block the others.

Requirements:
Read file line by line.
Process each line independently.
Write processed output in the same order.
Different pipeline stages should execute concurrently.
Support graceful shutdown.

Pattern
Multi-stage Pipeline (Producer → Processor → Consumer)

Production-grade Approach
Use multiple BlockingQueues, one between each stage. Each stage runs on its own thread (or thread
pool), communicating only through queues.
*/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class FileProcessingPipeline {

    private static final String POISON = "__EOF__";
    private final BlockingQueue<String> readQueue;
    private final BlockingQueue<String> processedQueue;

    public FileProcessingPipeline(int capacity) {
        readQueue = new ArrayBlockingQueue<>(capacity);
        processedQueue = new ArrayBlockingQueue<>(capacity);
    }

    // Producer => Read from file => write to queue1
    public void producer(String[] lines) {
        try {
            for (String line : lines) {
                readQueue.put(line);
            }

            readQueue.put(POISON);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Processor => Read from Queue => Write to queue2
    public void processor() {
        int count = 1;
        try {
            while (true) {
                String line = readQueue.take(); // Thread sleep efficiently until data arrives
                if (line.equals(POISON)) {
                    processedQueue.put(POISON);
                    break;
                }

                line = "Line " + count + " : " + line;
                count++;
                processedQueue.put(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Consumer => Read from queue2 => write to file in same order
    public void consumer() {
        try {
            while (true) {
                String line = processedQueue.take();

                if (line.equals(POISON)) break;

                System.out.println(line);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FileProcessingPipeline pipeline = new FileProcessingPipeline(10);

        String[] file = { "apple", "banana", "orange", "grapes", "mango" };
        Thread reader = new Thread(() -> pipeline.producer(file));
        Thread processor = new Thread(pipeline::processor);
        Thread writer = new Thread(pipeline::consumer);

        reader.start();
        processor.start();
        writer.start();

        // Graceful shutdown - wait for threads to complete or throw exception when flag is set ?
        // flag will be shared mutable state in that case.
        reader.join();
        processor.join();
        writer.join();
    }
}
