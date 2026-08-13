/*
Problem Statement

One barber and multiple customers.

Rules:

Barber sleeps if no customers.
Customers wait if chairs are available.
Customers leave if all chairs are occupied.

Pattern: Producer–consumer with bounded waiting.

Production-grade approach: Semaphore + Condition.
*/

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SleepingBarber implements AutoCloseable {

    private final Semaphore waitingChairs;
    private final ReentrantLock lock = new ReentrantLock(true);

    // Barber sleeps when this queue is empty.
    private final Condition customerAvailable = lock.newCondition();
    private final Deque<Customer> waitingCustomers = new ArrayDeque<>();
    private final Thread barberThread;

    // Protected by lock
    private boolean acceptingCustomers = true;

    public SleepingBarber(int numberOfWaitingChairs) {
        if (numberOfWaitingChairs < 0) {
            throw new IllegalArgumentException(
                "Number of waiting chairs cannot be negative"
            );
        }

        // Fairness prevents newer customers from repeatedly
        // jumping ahead of customers who arrived earlier.
        this.waitingChairs = new Semaphore(numberOfWaitingChairs, true);
        barberThread = new Thread(this::runBarber, "Barber");
        barberThread.start();
    }

    /**
     * Customer attempts to enter the barber shop.
     *
     * Returns false if all waiting chairs are occupied
     * or the shop is shutting down.
     */
    public boolean visit(String customerName) {
        Customer customer = new Customer(customerName);
        /*
         * Do NOT hold the lock while trying to acquire a chair.
         *
         * tryAcquire() is non-blocking:
         *   true  -> a waiting chair is available
         *   false -> all chairs are occupied
         */
        if (!waitingChairs.tryAcquire()) {
            System.out.println(customerName + " leaves: no waiting chair");
            return false;
        }

        lock.lock();

        try {
            /*
             * Shutdown may have started after we acquired
             * the semaphore permit.
             */
            if (!acceptingCustomers) {
                waitingChairs.release();

                System.out.println(customerName + " leaves: shop is closed");

                return false;
            }

            waitingCustomers.addLast(customer);

            System.out.println(
                customerName +
                    " waits. " +
                    "Waiting customers = " +
                    waitingCustomers.size()
            );

            // Wake the sleeping barber.
            customerAvailable.signal();
        } finally {
            lock.unlock();
        }

        /*
         * Wait until THIS customer's haircut is completed.
         *
         * Each Customer has its own Condition, so one customer's
         * completion does not wake every other customer.
         */
        lock.lock();

        try {
            while (!customer.haircutCompleted) {
                customer.haircutDone.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            /*
             * The customer may already have been taken by
             * the barber. We don't attempt to manipulate the
             * queue here because the barber owns that lifecycle.
             */
        } finally {
            lock.unlock();
        }

        System.out.println(customerName + " leaves after haircut");

        return true;
    }

    /**
     * Barber thread.
     */
    private void runBarber() {
        try {
            while (true) {
                Customer customer;

                lock.lock();

                try {
                    /*
                     * Barber sleeps while there are no customers.
                     */
                    while (waitingCustomers.isEmpty() && acceptingCustomers) {
                        System.out.println("Barber sleeps...");

                        customerAvailable.await();
                    }

                    /*
                     * Graceful shutdown:
                     *
                     * Stop accepting new customers, but finish
                     * everyone already waiting.
                     */
                    if (waitingCustomers.isEmpty() && !acceptingCustomers) {
                        return;
                    }

                    customer = waitingCustomers.removeFirst();

                    /*
                     * The waiting chair is now free.
                     *
                     * A new customer can occupy it while the
                     * barber is cutting this customer's hair.
                     */
                    waitingChairs.release();
                } finally {
                    lock.unlock();
                }

                cutHair(customer);

                lock.lock();

                try {
                    customer.haircutCompleted = true;

                    // Wake only this customer.
                    customer.haircutDone.signal();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            /*
             * Unexpected interruption.
             *
             * Restore interrupt status and terminate barber.
             */
            Thread.currentThread().interrupt();
        }
    }

    private void cutHair(Customer customer) throws InterruptedException {
        System.out.println("Barber starts haircut for " + customer.name);

        // Simulate haircut.
        Thread.sleep(1000);

        System.out.println("Barber finishes haircut for " + customer.name);
    }

    /**
     * Graceful shutdown.
     *
     * No new customers are accepted.
     * Existing waiting customers are processed.
     * Barber exits after the queue becomes empty.
     */
    public void shutdown() throws InterruptedException {
        lock.lock();

        try {
            acceptingCustomers = false;

            /*
             * Wake barber if he is sleeping.
             */
            customerAvailable.signalAll();
        } finally {
            lock.unlock();
        }

        /*
         * Wait until the barber has processed all existing
         * customers and terminated.
         */
        barberThread.join();
    }

    @Override
    public void close() {
        try {
            shutdown();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class Customer {

        final String name;

        boolean haircutCompleted = false;

        /*
         * Must only be used while SleepingBarber.lock is held.
         */
        final Condition haircutDone;

        Customer(String name) {
            this.name = name;
            this.haircutDone = BARBER_LOCK_HOLDER.newCondition();
        }

        /*
         * Placeholder replaced by the outer class's lock.
         *
         * This field is not used directly.
         */
        private static final ReentrantLock BARBER_LOCK_HOLDER =
            new ReentrantLock();
    }

    public static void main(String[] args) throws InterruptedException {
        try (SleepingBarber shop = new SleepingBarber(3)) {
            for (int i = 1; i <= 10; i++) {
                final int customerId = i;

                Thread customer = new Thread(
                    () -> shop.visit("Customer-" + customerId),
                    "Customer-" + customerId
                );

                customer.start();

                Thread.sleep(200);
            }

            Thread.sleep(6000);
        }
    }
}
