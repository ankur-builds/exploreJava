/*
Problem Statement

There are N threads arranged in a logical ring. Each thread can execute only when it receives a token. After completing its work, it passes the token to the next thread.

Example (N = 4, 2 rounds):

T0
T1
T2
T3
T0
T1
T2
T3

The token continuously circulates:

T0 → T1 → T2 → T3 → T0 → ...

Pattern: Token passing / cyclic execution.

Production-grade approach: Semaphore (one semaphore per thread) or ReentrantLock + N Conditions.
*/

import java.util.concurrent.Semaphore;

public class TokenRing {

    private final Semaphore[] semaphores;
    private final int threads;
    private final int rounds;

    public TokenRing(int threads, int rounds) {
        this.threads = threads;
        this.rounds = rounds;

        semaphores = new Semaphore[threads];

        for (int i = 0; i < threads; i++) {
            semaphores[i] = new Semaphore(0);
        }

        // First thread starts with the token
        semaphores[0].release();
    }

    public void execute(int id) {
        try {
            for (int i = 0; i < rounds; i++) {
                semaphores[id].acquire();
                System.out.printf(
                        "Round %d -> Thread-%d%n",
                        i + 1,
                        id
                );
                semaphores[(id + 1) % threads].release();
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        int threads = 4;
        int rounds = 3;

        TokenRing tokenRing = new TokenRing(threads, rounds);

        for (int i = 0; i < threads; i++) {
            final int id = i; // Needed because lambdas capture variables, not their current values.
            new Thread(() -> tokenRing.execute(id),
                    "Thread-" + id).start();
        }
    }
}
