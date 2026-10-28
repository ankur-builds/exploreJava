import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpCallExperiment {

    public static void main(String[] args) {
        String targetUrl = "https://example.com";
        int totalRequests = 5;

        // 1. Create a SINGLE persistent HTTP client instance.
        // This client manages an internal connection pool automatically.
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(targetUrl))
            .GET()
            .build();

        System.out.println(
            "=================================================="
        );
        System.out.println(
            "Starting Experiment: Repeated Requests via Single Client"
        );
        System.out.println("Target URL: " + targetUrl);
        System.out.println(
            "=================================================="
        );

        for (int i = 1; i <= totalRequests; i++) {
            long startTime = System.nanoTime();

            try {
                // Send the request synchronously
                HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
                );

                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;

                System.out.printf(
                    "Request #%d | Status: %d | Total Duration: %d ms%n",
                    i,
                    response.statusCode(),
                    durationMs
                );
            } catch (Exception e) {
                long endTime = System.nanoTime();
                long durationMs = (endTime - startTime) / 1_000_000;
                System.err.printf(
                    "Request #%d failed after %d ms. Error: %s%n",
                    i,
                    durationMs,
                    e.getMessage()
                );
            }

            // Optional: short sleep to represent real-world pacing (keeps connection alive)
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {}
        }

        System.out.println(
            "=================================================="
        );
    }
}
