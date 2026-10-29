package basics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;

public class HttpCommunicationDemo {

    private static void sendResponse(
        HttpExchange exchange,
        int statusCode,
        String body
    ) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        IO.println("Exchange : %s".formatted(exchange.getRequestURI()));
        exchange
            .getResponseHeaders()
            .set("Content-Type", "text/plain; charset=utf-8");

        exchange.getResponseHeaders().set("X-Service", "demo-localhost");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        exchange.getResponseBody().write(bytes);
    }

    public static void helloWorld(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && "/demo".equals(path)) {
                sendResponse(exchange, 200, "HELLO WORLD");
            } else if ("POST".equals(method) && "/print".equals(path)) {
                String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );
                IO.println("Request Body : %s".formatted(requestBody));
                sendResponse(exchange, 201, "Printed : " + requestBody);
            } else {
                sendResponse(exchange, 404, "Bad Request");
            }
        } /* catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            // The connection may already be closed by the client.
            try {
                sendResponse(exchange, 503, "Request interrupted");
            } catch (IOException ignored) {
                sendResponse(exchange, 500, "Internal Server Exception");
            }
            } */ finally {
            exchange.close();
        }
    }

    public static HttpServer startServer() throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 8080),
            0
        );
        server.createContext("/demo", HttpCommunicationDemo::helloWorld);
        server.createContext("/print", HttpCommunicationDemo::helloWorld);

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        IO.println("Server is listening on 127.0.0.1:8080");

        return server;
    }

    public static HttpClient createHttpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
    }

    public static HttpRequest createHttpRequest(
        URI uri,
        long timeout,
        String type
    ) {
        if ("GET".equals(type)) return HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(timeout))
            .GET()
            .header("Accept", "text/json")
            .build();
        else if ("POST".equals(type)) return HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(timeout))
            .POST(HttpRequest.BodyPublishers.ofString("PRINT Hello World"))
            .header("Accept", "text/json")
            .build();
        else return null;
    }

    public static void main(String[] args) {
        try {
            // start http server
            HttpServer server = startServer();

            // create http client
            HttpClient client = createHttpClient();

            // create http request
            HttpRequest getRequest = createHttpRequest(
                URI.create("http://127.0.0.1:8080/demo"),
                2,
                "GET"
            );
            HttpRequest postRequest = createHttpRequest(
                URI.create("http://127.0.0.1:8080/print"),
                2,
                "POST"
            );

            // get http response
            HttpResponse<String> getResponse = client.send(
                getRequest,
                HttpResponse.BodyHandlers.ofString()
            );

            IO.println("Get response : %s".formatted(getResponse.body()));

            HttpResponse<String> postResponse = client.send(
                postRequest,
                HttpResponse.BodyHandlers.ofString()
            );

            IO.println("POST response : %s".formatted(postResponse.body()));

            // Stop server after 2 mins
            server.stop(120);
        } catch (Exception e) {
            IO.println("Caught exception : %s".formatted(e));
        }
    }
}
