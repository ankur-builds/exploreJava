# Order Management Java Lab (Java 21+)

A runnable, plain-Java lab covering execution model, OOP, types, generics, collections,
functional Java, and exception design. Spring Boot is intentionally not used yet.

## Prerequisites
- JDK 21 or newer (`java -version`, `javac -version`)
- Maven 3.9+ (`mvn -version`)

## Run
```bash
mvn test
mvn -q package
java -cp target/classes com.example.ordermanagement.OrderDemo
```

## Project map
- `model`: value objects, order item, order status, order aggregate
- `repository`: generic repository contract and in-memory implementation
- `service`: business rules and typed exceptions
- `util`: stream-based reporting
- `OrderDemo`: small runnable demonstration
- `src/test`: automated tests and failure cases

## Lab sequence and exercises

### 1.1 Execution model
1. Run `mvn package`; inspect `target/classes`.
2. Run `javap -c -p target/classes/com/example/ordermanagement/OrderDemo.class`.
3. Deliberately compile with `javac --release 17` and inspect compatibility implications.
4. Explain source -> bytecode -> class loading -> verification -> interpreter/JIT.

Production drill: run a class compiled for a newer Java release on an older runtime.
Expected symptom: `UnsupportedClassVersionError`.

### 1.2 OOP
1. Add `Customer` as a value object.
2. Add a `ShippingAddress` value object; keep state private and validate at construction.
3. Add a `DiscountPolicy` interface and two implementations.
4. Refactor `OrderService` to depend on abstractions rather than concrete repository classes.
5. Explain why composition is preferred over inheritance for unrelated capabilities.

Production drill: attempt to create an order with zero quantity. The domain object must reject it.

### 1.3 Types and object behavior
1. Add tests for `Money` equality and hash code.
2. Add a test proving two equivalent `OrderItem` values compare equal.
3. Add a test proving `Money` is immutable.
4. Add `OrderStatus` transitions and reject invalid transitions.
5. Never use `double` for money; use `BigDecimal`.

Production drill: mutate an object after using it as a `HashMap` key. Explain why mutable hash keys are dangerous.

### 1.4 Generics
1. Implement `Repository<T, ID>` with `findById`, `save`, and `deleteById`.
2. Add a second entity and repository using a different ID type.
3. Write a generic `requireNonNull` method.
4. Write a method accepting `List<? extends Number>` and another accepting `List<? super Integer>`.
5. Observe that `List<Integer>` is not a subtype of `List<Number>`.
6. Inspect generic signatures with `javap -v`; explain type erasure.

Production drill: remove type arguments from a collection, add an incompatible value, and observe the delayed `ClassCastException`.

### 1.5 Collections
1. Add duplicate order-ID handling.
2. Explain `HashMap`'s dependence on stable `equals`/`hashCode`.
3. Compare `ArrayList`, `LinkedList`, `HashMap`, `TreeMap`, and `ConcurrentHashMap` for a specific workload.
4. Add a concurrency test around a compound inventory update. Do not assume a thread-safe map makes multi-step operations atomic.
5. Explain expected O(1) hash lookup versus O(n) linear search.

Production drill: run concurrent updates against a shared mutable collection; fix it with a correct atomicity strategy.

### 1.6 Functional Java
1. Use `Predicate<Order>` to filter paid orders.
2. Use `groupingBy` and `counting` to count orders by status.
3. Use `flatMap` to flatten order items.
4. Add a `toMap` collector with a deliberate duplicate-key merge policy.
5. Explain why side effects in `parallelStream().forEach(...)` are unsafe.

Production drill: replace a shared mutable accumulator with a proper collector.

### 1.7 Exceptions
1. Add `OrderNotFoundException` and `InvalidOrderOperationException`.
2. Preserve causes when translating infrastructure exceptions.
3. Test validation, missing orders, and illegal state transitions.
4. Do not catch and silently ignore exceptions.
5. Decide which errors are retryable; never blindly retry non-idempotent payment calls.

Production drill: simulate a payment timeout after the provider may have charged the customer. Explain why idempotency and reconciliation are needed.

## Mastery gate
Do not proceed until you can:
- Explain each concept without reading the code.
- Add one feature without changing unrelated classes.
- Write tests for both success and failure paths.
- Diagnose a deliberately introduced defect.
- Describe production implications and trade-offs.

## Suggested reading
Joshua Bloch, *Effective Java*, Third Edition. Use it as a companion, not as a substitute for running the exercises.
