package basics;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeoutException;

/*
javac --enable-preview --release 27 CoordinatedRequestExperiment.java
java --enable-preview CoordinatedRequestExperiment
*/
public class CoordinatedRequestExperiment {

    public record OrderDetails(String customer, String shipping) {}

    private static String fetchCustomer(int delayMs, boolean shouldFail)
        throws InterruptedException {
        try {
            Thread.sleep(delayMs);

            if (shouldFail) {
                System.out.println("[Subtask A] Customer lookup failed!");
                throw new RuntimeException("Customer database offline");
            }

            System.out.println("[Subtask A] Customer lookup succeeded.");
            return "Alice Smith";
        } catch (InterruptedException e) {
            System.out.println("[Subtask A] Customer lookup interrupted.");
            throw e;
        }
    }

    private static String fetchShipping(int delayMs, boolean shouldFail)
        throws InterruptedException {
        try {
            Thread.sleep(delayMs);

            if (shouldFail) {
                System.out.println("[Subtask B] Shipping lookup failed!");
                throw new RuntimeException("Shipping API gateway timeout");
            }

            System.out.println("[Subtask B] Shipping lookup succeeded.");
            return "Express Delivery (2 days)";
        } catch (InterruptedException e) {
            System.out.println("[Subtask B] Shipping lookup interrupted.");
            throw e;
        }
    }

    public static OrderDetails executeOrderRequest(
        int customerDelay,
        boolean customerFail,
        int shippingDelay,
        boolean shippingFail,
        Duration deadline
    ) throws Exception {
        try (
            var scope = StructuredTaskScope.<String>open(config ->
                deadline == null ? config : config.withTimeout(deadline)
            )
        ) {
            var customer = scope.fork(() ->
                fetchCustomer(customerDelay, customerFail)
            );

            var shipping = scope.fork(() ->
                fetchShipping(shippingDelay, shippingFail)
            );

            // Wait for both successes, or stop when a task fails/times out.
            scope.join();

            return new OrderDetails(customer.get(), shipping.get());
        } catch (ExecutionException e) {
            if (
                e.getCause() instanceof
                    StructuredTaskScope.CancelledByTimeoutException
            ) {
                throw new TimeoutException(
                    "Order request exceeded deadline: " + deadline
                );
            }

            // Preserve the original task failure and its cause.
            throw e;
        }
    }

    public static void runScenario(String description, Runnable scenario) {
        System.out.println("\n--- " + description + " ---");

        try {
            scenario.run();
        } catch (RuntimeException e) {
            System.err.println("Unexpected failure: " + e);
        }
    }

    private static void reportFailure(Exception e) {
        System.out.println("Caller caught: " + e);
        if (e instanceof ExecutionException && e.getCause() != null) {
            System.out.println("Underlying cause: " + e.getCause());
        }
        if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        runScenario("Scenario 1: Both succeed", () -> {
            try {
                System.out.println(
                    "Result: " +
                        executeOrderRequest(100, false, 150, false, null)
                );
            } catch (Exception e) {
                reportFailure(e);
            }
        });

        runScenario("Scenario 2: Customer lookup fails", () -> {
            try {
                executeOrderRequest(10, true, 300, false, null);
            } catch (Exception e) {
                reportFailure(e);
            }
        });

        runScenario("Scenario 3: Shipping lookup fails", () -> {
            try {
                executeOrderRequest(200, false, 50, true, null);
            } catch (Exception e) {
                reportFailure(e);
            }
        });

        runScenario("Scenario 4: Request deadline exceeded", () -> {
            try {
                executeOrderRequest(
                    50,
                    false,
                    1000,
                    false,
                    Duration.ofMillis(200)
                );
            } catch (Exception e) {
                reportFailure(e);
            }
        });

        runScenario("Scenario 5: Parent interrupted", () -> {
            Thread mainThread = Thread.currentThread();

            Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(50);
                    mainThread.interrupt();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });

            try {
                executeOrderRequest(500, false, 500, false, null);
            } catch (Exception e) {
                reportFailure(e);
            }
        });
    }
}
