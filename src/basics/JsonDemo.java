import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;

// Order record
record Order(
    String orderId,
    String customerName,
    BigDecimal amount,
    boolean paid,
    Instant createdAt
) {}

/*
javac -d . -cp "lib/*" JsonDemo.java
java -cp ".:lib/*" JsonDemo
*/
public class JsonDemo {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Required for Java 8 date/time support.
        mapper.findAndRegisterModules();

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Order order = new Order(
            "ORD-101",
            "Alice",
            new BigDecimal("1499.50"),
            true,
            Instant.parse("2026-11-01T10:30:00Z")
        );

        // 1. Serialize Order
        System.out.println("\n1. SERIALIZATION");

        String json = mapper.writeValueAsString(order);
        System.out.println(json);

        // 2. Deserialize JSON
        System.out.println("\n2. DESERIALIZATION");

        Order restored = mapper.readValue(json, Order.class);
        System.out.println(restored);

        // 3. Remove customerName
        System.out.println("\n3. REMOVE customerName");

        String removedJson = json.replace("\"customerName\":\"Alice\",", "");

        System.out.println(removedJson);

        Order removedOrder = mapper.readValue(removedJson, Order.class);

        System.out.println("customerName = " + removedOrder.customerName());

        // 4. Explicitly set customerName to null
        System.out.println("\n4. customerName = null");

        String nullJson = json.replace(
            "\"customerName\":\"Alice\"",
            "\"customerName\":null"
        );

        Order nullOrder = mapper.readValue(nullJson, Order.class);

        System.out.println("customerName = " + nullOrder.customerName());

        // 5. Add unknown discountCode property
        System.out.println("\n5. UNKNOWN PROPERTY");

        String unknownJson = json.replace("{", "{\"discountCode\":\"SAVE20\",");

        Order unknownOrder = mapper.readValue(unknownJson, Order.class);

        System.out.println(unknownOrder);

        // 6. Instant field
        System.out.println("\n6. INSTANT FIELD");

        System.out.println("createdAt = " + restored.createdAt());

        // 7. Change amount from number to string
        System.out.println("\n7. AMOUNT AS STRING");

        String stringAmountJson = json.replace(
            "\"amount\":1499.50",
            "\"amount\":\"1499.50\""
        );

        try {
            Order stringAmountOrder = mapper.readValue(
                stringAmountJson,
                Order.class
            );

            System.out.println("amount = " + stringAmountOrder.amount());
            IO.println(
                "It succeeded because Jackson knows how to convert a numeric string into a Java BigDecimal."
            );
        } catch (Exception e) {
            System.out.println("Exception: " + e.getClass().getSimpleName());

            System.out.println(e.getMessage());
        }

        // 8. Observe all resulting values
        System.out.println("\n8. EXPERIMENT SUMMARY");

        System.out.println("Original: " + restored);
        System.out.println("Missing name: " + removedOrder.customerName());
        System.out.println("Explicit null: " + nullOrder.customerName());
        System.out.println("Unknown property: accepted");
        System.out.println("Instant: " + restored.createdAt());
    }
}
