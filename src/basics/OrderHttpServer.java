package lab;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class OrderHttpServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 8080),
            0
        );

        server.createContext("/orders", OrderHttpServer::handleOrders);

        // For this lab, use a virtual thread per request.
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.start();

        System.out.println("Order server listening on http://127.0.0.1:8080");
    }

    private static void handleOrders(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method) && "/orders/ORD-101".equals(path)) {
                sendResponse(exchange, 200, "Order ORD-101 is PAID");
            } else if ("GET".equals(method) && "/orders/ORD-404".equals(path)) {
                sendResponse(exchange, 404, "Order not found");
            } else if ("GET".equals(method) && "/orders/slow".equals(path)) {
                simulateSlowResponse();
                sendResponse(exchange, 200, "Slow response completed");
            } else if ("POST".equals(method) && "/orders".equals(path)) {
                String requestBody = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
                );

                if (requestBody.isBlank()) {
                    sendResponse(exchange, 400, "Request body is required");
                } else {
                    sendResponse(
                        exchange,
                        201,
                        "Order accepted: " + requestBody
                    );
                }
            } else if (!"GET".equals(method) && !"POST".equals(method)) {
                exchange.getResponseHeaders().set("Allow", "GET, POST");

                sendResponse(exchange, 405, "Method not allowed");
            } else {
                sendResponse(exchange, 404, "Resource not found");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            // The connection may already be closed by the client.
            try {
                sendResponse(exchange, 503, "Request interrupted");
            } catch (IOException ignored) {
                // The client may have disconnected.
            }
        } finally {
            exchange.close();
        }
    }

    private static void sendResponse(
        HttpExchange exchange,
        int statusCode,
        String body
    ) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        exchange
            .getResponseHeaders()
            .set("Content-Type", "text/plain; charset=utf-8");

        exchange.getResponseHeaders().set("X-Service", "order-service");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        exchange.getResponseBody().write(bytes);
    }

    private static void simulateSlowResponse() throws InterruptedException {
        Thread.sleep(5_000);
    }
}
