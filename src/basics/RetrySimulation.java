import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class RetrySimulation {

    public static void main(String[] args) throws Exception {
        int port = 8081;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // 1. Endpoint: Sequence (503, 503, then 200)
        AtomicInteger seqCounter = new AtomicInteger(0);
        server.createContext("/sequence", exchange -> {
            int attempt = seqCounter.incrementAndGet();
            System.out.println(
                "[Server /sequence] Request attempt #" + attempt
            );
            if (attempt < 3) {
                sendResponse(exchange, 503, "Service Unavailable");
            } else {
                sendResponse(exchange, 200, "Success on attempt " + attempt);
            }
        });

        // 2. Endpoint: Always 503
        server.createContext("/always-503", exchange -> {
            System.out.println(
                "[Server /always-503] Returning 503 Persistent Error"
            );
            sendResponse(exchange, 503, "Persistent Service Unavailable");
        });

        // 3. Endpoint: Slow Response (Simulating Server Delay)
        server.createContext("/slow", exchange -> {
            System.out.println(
                "[Server /slow] Received request, sleeping for 2000ms..."
            );
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            sendResponse(exchange, 200, "Too late response");
        });

        // 4. Endpoint: Side Effect Tracking (Idempotency Demo)
        AtomicInteger sideEffectCounter = new AtomicInteger(0);
        server.createContext("/side-effect", exchange -> {
            String idempotencyKey = exchange
                .getRequestHeaders()
                .getFirst("X-Idempotency-Key");
            int currentSideEffect = sideEffectCounter.incrementAndGet();
            System.out.println(
                "[Server /side-effect] Side effect executed! Total count: " +
                    currentSideEffect +
                    " | Idempotency-Key: " +
                    (idempotencyKey != null ? idempotencyKey : "NONE")
            );
            sendResponse(exchange, 503, "Failed after side effect execution");
        });

        // 5. Endpoint: 400 Bad Request (Non-retryable client error)
        server.createContext("/bad-request", exchange -> {
            System.out.println(
                "[Server /bad-request] Returning 400 Bad Request"
            );
            sendResponse(exchange, 400, "Bad Request - Do Not Retry");
        });

        server.setExecutor(null);
        server.start();
        System.out.println(
            "=== Mock HTTP Server started on port " + port + " ==="
        );

        String baseUrl = "http://localhost:" + port;
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

        // --- TEST 1: Sequence (503, 503, 200) with Exponential Backoff & Jitter ---
        System.out.println(
            "\n--------------------------------------------------"
        );
        System.out.println("TEST 1: Bounded Retry with Recoverable Sequence");
        System.out.println(
            "--------------------------------------------------"
        );
        executeWithRetry(
            client,
            baseUrl + "/sequence",
            3,
            Duration.ofMillis(100),
            false,
            null
        );

        // --- TEST 2: Persistent Failure (Always 503) ---
        System.out.println(
            "\n--------------------------------------------------"
        );
        System.out.println(
            "TEST 2: Persistent Failure (Always 503) - Exhausting Retries"
        );
        System.out.println(
            "--------------------------------------------------"
        );
        executeWithRetry(
            client,
            baseUrl + "/always-503",
            3,
            Duration.ofMillis(100),
            false,
            null
        );

        // --- TEST 3: Client Deadline / Timeout Exceeded ---
        System.out.println(
            "\n--------------------------------------------------"
        );
        System.out.println("TEST 3: Client Timeout & Deadline Enforcement");
        System.out.println(
            "--------------------------------------------------"
        );
        // Client timeout set to 500ms, but server takes 2000ms
        executeWithTimeoutAndRetry(
            client,
            baseUrl + "/slow",
            3,
            Duration.ofMillis(500),
            Duration.ofMillis(100)
        );

        // --- TEST 4: Non-Retryable Error (400 Bad Request) ---
        System.out.println(
            "\n--------------------------------------------------"
        );
        System.out.println(
            "TEST 4: Non-Retryable Client Error (400 Bad Request)"
        );
        System.out.println(
            "--------------------------------------------------"
        );
        executeWithRetry(
            client,
            baseUrl + "/bad-request",
            3,
            Duration.ofMillis(100),
            false,
            null
        );

        // --- TEST 5: Side Effects & Idempotency Key ---
        System.out.println(
            "\n--------------------------------------------------"
        );
        System.out.println("TEST 5: Side Effects & Idempotency Handling");
        System.out.println(
            "--------------------------------------------------"
        );
        String idempotencyKey = "txn-" + UUID.randomUUID().toString();
        executeWithRetry(
            client,
            baseUrl + "/side-effect",
            3,
            Duration.ofMillis(100),
            true,
            idempotencyKey
        );

        // Shutdown server
        server.stop(0);
        System.out.println("\n=== Simulation Complete ===");
    }

    private static void sendResponse(
        HttpExchange exchange,
        int statusCode,
        String response
    ) throws IOException {
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // Bounded Retry Policy with Exponential Backoff and Jitter
    public static HttpResponse<String> executeWithRetry(
        HttpClient client,
        String url,
        int maxAttempts,
        Duration initialBackoff,
        boolean useIdempotencyKey,
        String idempotencyKey
    ) {
        Duration backoff = initialBackoff;
        Random random = new Random();

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println(
                    "[Client] Attempt " +
                        attempt +
                        " of " +
                        maxAttempts +
                        " for " +
                        url
                );

                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder().uri(
                    URI.create(url)
                );
                if (useIdempotencyKey && idempotencyKey != null) {
                    reqBuilder.header("X-Idempotency-Key", idempotencyKey);
                }
                // FIXED: added .build() here
                HttpRequest request = reqBuilder.GET().build();

                Instant start = Instant.now();
                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );
                Instant end = Instant.now();

                int status = response.statusCode();
                System.out.println(
                    "[Client] Received status " +
                        status +
                        " in " +
                        Duration.between(start, end).toMillis() +
                        "ms"
                );

                // Retry only transient server errors (5xx)
                if (status >= 500) {
                    if (attempt == maxAttempts) {
                        System.out.println(
                            "[Client] Max retry attempts (" +
                                maxAttempts +
                                ") reached. Giving up."
                        );
                        return response;
                    }
                    long jitter = random.nextInt(50); // add jitter to prevent thundering herd
                    long sleepMs = backoff.toMillis() + jitter;
                    System.out.println(
                        "[Client] Transient error (" +
                            status +
                            "). Retrying in " +
                            sleepMs +
                            "ms..."
                    );
                    Thread.sleep(sleepMs);
                    backoff = backoff.multipliedBy(2); // exponential backoff
                } else if (status >= 400) {
                    // Client errors (4xx) should NOT be retried automatically
                    System.out.println(
                        "[Client] Client error (" +
                            status +
                            "). Non-retryable. Aborting immediately."
                    );
                    return response;
                } else {
                    System.out.println(
                        "[Client] Success! Response: " + response.body()
                    );
                    return response;
                }
            } catch (Exception e) {
                System.out.println(
                    "[Client] Exception encountered: " + e.getMessage()
                );
                if (attempt == maxAttempts) break;
                try {
                    Thread.sleep(backoff.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                backoff = backoff.multipliedBy(2);
            }
        }
        return null;
    }

    // Client Timeout & Deadline Enforcement
    public static void executeWithTimeoutAndRetry(
        HttpClient client,
        String url,
        int maxAttempts,
        Duration requestTimeout,
        Duration initialBackoff
    ) {
        Duration backoff = initialBackoff;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println(
                    "[Client Timeout Test] Attempt " +
                        attempt +
                        " with per-request timeout " +
                        requestTimeout.toMillis() +
                        "ms"
                );
                // FIXED: added .build() here
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(requestTimeout)
                    .GET()
                    .build();

                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );
                System.out.println(
                    "[Client] Success: " + response.statusCode()
                );
                return;
            } catch (java.net.http.HttpTimeoutException e) {
                System.out.println(
                    "[Client] Request timed out after " +
                        requestTimeout.toMillis() +
                        "ms!"
                );
                if (attempt == maxAttempts) {
                    System.out.println(
                        "[Client] Max retry attempts reached on timeout. Giving up."
                    );
                    return;
                }
                long sleepMs = backoff.toMillis();
                System.out.println(
                    "[Client] Retrying after timeout in " + sleepMs + "ms..."
                );
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                backoff = backoff.multipliedBy(2);
            } catch (Exception e) {
                System.out.println(
                    "[Client] Other exception: " + e.getMessage()
                );
                return;
            }
        }
    }
}
