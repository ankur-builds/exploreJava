import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;

record Order(
    String orderId,
    String customerName,
    BigDecimal amount,
    boolean paid
) {}

public class JsonDemo {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Order order = new Order(
            "ORD-101",
            "Alice",
            new BigDecimal("1499.50"),
            true
        );

        String json = mapper.writeValueAsString(order);

        System.out.println(json);
    }
}
