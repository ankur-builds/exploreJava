/*
Problem Statement
Design a web server request dispatcher that accepts incoming HTTP requests and processes them
using a fixed pool of worker threads.

Requirements:
Incoming requests should not create a new thread.
Multiple clients may submit requests simultaneously.
Requests should be processed in FIFO order.
The server should limit concurrent request processing.
Apply backpressure when the server is overloaded.

Pattern
Request Queue + Worker Pool

Production-grade Approach
Use a bounded BlockingQueue<HttpRequest> with a fixed number of worker threads. This is the core
architecture behind Tomcat, Jetty, Undertow, Netty (with event loops), and Java's ThreadPoolExecutor.
*/

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class WebServerRequestDispatcher {
    static class HttpRequest {
        final String path;
        HttpRequest(String path) {
            this.path = path;
        }
    }

    private final BlockingQueue<HttpRequest> queue;

    public WebServerRequestDispatcher(int workers, int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
        for (int i = 1; i <= workers; ++i) {
            Thread worker = new Thread(() -> {
                while (true) {
                    try {
                        HttpRequest request = queue.take();
                        handle(request);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Worker - " + i);

            worker.start();
        }
    }

    // Requests -> queue1
    public void submit(HttpRequest request) throws InterruptedException {
        queue.put(request);
    }

    // Handle requests
    public void handle(HttpRequest request) {
        switch (request.path) {
            case "/users":
                System.out.println(
                    Thread.currentThread().getName() + " -> Fetch users"
                );
                break;
            case "/orders":
                System.out.println(
                    Thread.currentThread().getName() + " -> Create orders"
                );
                break;
            default:
                System.out.println(
                    Thread.currentThread().getName() + " -> 404 Not Found"
                );
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        WebServerRequestDispatcher server = new WebServerRequestDispatcher(3, 10);

        server.submit(new HttpRequest("/users"));
        server.submit(new HttpRequest("/orders"));
        server.submit(new HttpRequest("/users"));
        server.submit(new HttpRequest("/unknown"));
        server.submit(new HttpRequest("/orders"));
    }
}
