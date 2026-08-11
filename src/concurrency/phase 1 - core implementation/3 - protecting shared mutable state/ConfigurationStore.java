/*
Problem Statement
Design a thread-safe Configuration Store used by multiple application components. Thousands of threads frequently read configuration values (database URL, timeout,
feature flags), while administrators occasionally update them at runtime.

Requirements:
Multiple threads should read configuration concurrently.
Configuration updates must be atomic and visible to all readers.
Readers should not block each other.
Writers must have exclusive access while updating.
Support reading the updated configuration immediately after an update without allowing another writer to modify it first.

Pattern
Read-Heavy Shared State

Production-grade Approach
Use a ReentrantReadWriteLock. Protect read operations with the read lock so multiple readers can proceed concurrently, and protect updates with the write lock.
Use lock downgrading (write → read) when an update must be immediately followed by a consistent read.
*/

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigurationStore {
    private final Map<String, String> config = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public ConfigurationStore() {
        config.put("db.url", "localhost");
        config.put("timeout", "30");
    }

    // Thousands of concurrent readers
    public String get(String key) {
        rwLock.readLock().lock();
        try {
            return config.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Rare configuration update
    public void update(String key, String value) {
        rwLock.writeLock().lock();
        try {
            config.put(key, value);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    // Lock Downgrading
    public String updateAndRead(String key, String value) {
        rwLock.writeLock().lock();

        try {
            // Update shared state
            config.put(key, value);
            // Acquire read lock BEFORE releasing write lock
            rwLock.readLock().lock();
        } finally {
            rwLock.writeLock().unlock();
        }

        try {
            // Safe to read updated value
            return config.get(key);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public static void main(String[] args) {
        System.out.println("Program Started");
        ConfigurationStore store = new ConfigurationStore();

        Runnable reader = () -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(
                        Thread.currentThread().getName()
                                + " -> "
                                + store.get("timeout")
                );

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        Runnable writer = () -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("\nUpdating timeout...\n");
            System.out.println(
                    "Updated value = "
                            + store.updateAndRead("timeout", "60")
            );
        };

        System.out.println("Before starting threads");
        Thread r1 = new Thread(reader, "Reader-1");
        Thread r2 = new Thread(reader, "Reader-2");
        Thread r3 = new Thread(reader, "Reader-3");
        Thread w = new Thread(writer, "Writer");

        w.start();
        r1.start();
        r2.start();
        r3.start();

        System.out.println("After starting threads");

        try {
            r1.join();
            r2.join();
            r3.join();
            w.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
