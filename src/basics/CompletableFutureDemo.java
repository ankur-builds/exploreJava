package basics;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo {

    public static void main(String[] args) throws InterruptedException {
        // We use a custom executor to easily see thread text names in the logs
        var testExecutor = Executors.newFixedThreadPool(3);

        System.out.println(
            "[Main Thread] --- STARTING ORDER SYSTEM DEMO ---\n"
        );

        // ==========================================
        // 1. supplyAsync & runAsync (The Starting Tasks)
        // ==========================================

        // runAsync: Performs an action that returns nothing (Fire-and-forget alert)
        CompletableFuture<Void> systemCheck = CompletableFuture.runAsync(() -> {
            log("System Diagnostic Check running...");
        }, testExecutor);

        // supplyAsync: Performs an action and returns a value (Fetches raw data)
        CompletableFuture<Order> fetchOrderJob = CompletableFuture.supplyAsync(
            () -> {
                log("Fetching raw order from DB...");
                return new Order(101, 49.99, "PENDING"); // Initial State
            },
            testExecutor
        );

        // ==========================================
        // 2. thenApply & thenAccept (Transforming & Consuming)
        // ==========================================

        CompletableFuture<Void> pipeline = fetchOrderJob
            // thenApply: Transforms the Order by validating and updating its state
            .thenApply(order -> {
                log(
                    "Transforming order: Validating payment and changing state to APPROVED"
                );
                order.status = "APPROVED";
                return order; // Passes the transformed order forward
            })
            // thenCompose: Uses the order to launch a SEPARATE background task (Shipping Service)
            .thenCompose(order -> {
                log("Chaining with Shipping Service via thenCompose()...");
                return getShippingLabelAsync(order, testExecutor); // Returns a new CompletableFuture
            })
            // thenCombine: Combines the shipping label with an independent warehouse box check
            .thenCombine(
                getWarehouseBoxSizeAsync(testExecutor),
                (label, boxSize) -> {
                    log(
                        String.format(
                            "Combining Label and Box: [Label: %s] + [Box Size: %s]",
                            label,
                            boxSize
                        )
                    );
                    return (
                        "READY TO SHIP: " + label + " in a " + boxSize + " box."
                    );
                }
            )
            // thenAccept: The final destination. Prints the final manifest string, returns nothing.
            .thenAccept(manifest -> {
                log("Final Receipt Manifest Processed -> " + manifest);
            });

        // ==========================================
        // 3. Exception Handling (exceptionally, handle, whenComplete)
        // ==========================================

        // Let's build a failing workflow to visualize error behaviors
        CompletableFuture.supplyAsync(() -> {
            log("Simulating an API network glitch...");
            if (true) throw new RuntimeException(
                "Connection Timeout to Payment Gateway!"
            );
            return "Payment Success";
        }, testExecutor)
            // exceptionally: Catches errors and returns a fallback value to save the pipeline
            .exceptionally(ex -> {
                log(
                    "exceptionally() caught: " +
                        ex.getMessage() +
                        " Providing a dynamic fallback payload."
                );
                return "FALLBACK_PAYMENT_SUCCESS_OFFLINE";
            })
            // handle: Runs ALWAYS (success or failure) and lets you inspect/change the result or error
            .handle((result, ex) -> {
                log("handle() running. Current result state: " + result);
                return result + "_VERIFIED";
            })
            // whenComplete: Just a listener. Lets you see the final outcome but cannot change it
            .whenComplete((result, ex) -> {
                log(
                    "whenComplete() cleanup listener triggered. Final pipeline state: " +
                        result
                );
            });

        // ==========================================
        // 4. allOf & anyOf (Orchestrating Multiple Futures)
        // ==========================================

        CompletableFuture<String> providerA = CompletableFuture.supplyAsync(
            () -> {
                sleep(100);
                return "FedEx ($15)";
            },
            testExecutor
        );
        CompletableFuture<String> providerB = CompletableFuture.supplyAsync(
            () -> {
                sleep(50);
                return "UPS ($12)";
            },
            testExecutor
        );

        // anyOf: Resolves as soon as the FASTEST task finishes
        CompletableFuture.anyOf(providerA, providerB).thenAccept(
            fastestPrice -> {
                log("anyOf() Result (Fastest Courier): " + fastestPrice);
            }
        );

        // allOf: Resolves only when EVERY single task in the arguments list finishes
        CompletableFuture.allOf(systemCheck, pipeline).thenRun(() -> {
            log(
                "allOf() Notice: Both the main order pipeline and diagnostics are 100% complete!"
            );
        });

        // Wait briefly for background threads to complete printing before application exits
        Thread.sleep(1000);
        testExecutor.shutdown();
    }

    // Helper asynchronous method used for thenCompose
    private static CompletableFuture<String> getShippingLabelAsync(
        Order order,
        ExecutorService executor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log("Shipping Service generating track ID for order #" + order.id);
            return "TRACK-USPS-" + order.id;
        }, executor);
    }

    // Helper asynchronous method used for thenCombine
    private static CompletableFuture<String> getWarehouseBoxSizeAsync(
        ExecutorService executor
    ) {
        return CompletableFuture.supplyAsync(() -> {
            log("Warehouse calculating dimensions for item...");
            return "MEDIUM_BOX";
        }, executor);
    }

    // Simple logging helper to show exact thread movements
    private static void log(String message) {
        System.out.printf(
            "[%s] %s%n",
            Thread.currentThread().getName(),
            message
        );
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    // Domain models
    static class Order {

        int id;
        double price;
        String status;

        Order(int id, double price, String status) {
            this.id = id;
            this.price = price;
            this.status = status;
        }
    }
}
