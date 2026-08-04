/*
Problem Statement

Create four threads:

Thread A prints "fizz" when the current number is divisible by 3 but not 5.
Thread B prints "buzz" when divisible by 5 but not 3.
Thread C prints "fizzbuzz" when divisible by both 3 and 5.
Thread D prints the number otherwise.

Pattern: Multiple threads compete based on a shared counter.

Production-grade approach: ReentrantLock + 4 Conditions (or 1 Condition with signalAll() for interview simplicity).
*/

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FizzBuzz {

    private int num = 1;
    private int limit;

    FizzBuzz(int limit) {
        this.limit = limit;
    }

    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();

    public void printFizz() {
        while(true) {
            lock.lock();
            try {
                while (num <= limit && !(num % 3 == 0 && num % 5 != 0))
                    condition.await();

                if (num > limit) {
                    condition.signalAll();
                    break;
                }

                System.out.println(
                    num + " : fizz -> " + Thread.currentThread().getName()
                );
                num++;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public void printBuzz() {
        while(true) {
            lock.lock();
            try {
                while (num <= limit && !(num % 5 == 0 && num % 3 != 0))
                    condition.await();

                if (num > limit) {
                    condition.signalAll();
                    break;
                }

                System.out.println(
                    num + " : buzz -> " + Thread.currentThread().getName()
                );
                num++;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public void printFizzBuzz() {
        while(true) {
            lock.lock();
            try {
                while (num <= limit && !(num % 3 == 0 && num % 5 == 0))
                    condition.await();

                if (num > limit) {
                    condition.signalAll();
                    break;
                }

                System.out.println(
                    num + " : fizzBuzz -> " + Thread.currentThread().getName()
                );
                num++;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public void printNumber() {
        while(true) {
            lock.lock();
            try {
                while (num <= limit && (num % 3 == 0 || num % 5 == 0))
                    condition.await();

                if (num > limit) {
                    condition.signalAll();
                    break;
                }

                System.out.println(
                    num + " -> " + Thread.currentThread().getName()
                );
                num++;
                condition.signalAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        FizzBuzz f = new FizzBuzz(15);

        new Thread(f::printBuzz).start();
        new Thread(f::printFizz).start();
        new Thread(f::printFizzBuzz).start();
        new Thread(f::printNumber).start();
    }
}
