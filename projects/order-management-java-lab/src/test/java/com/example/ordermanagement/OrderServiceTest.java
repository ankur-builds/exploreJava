package com.example.ordermanagement;

import com.example.ordermanagement.exception.InvalidOrderOperationException;
import com.example.ordermanagement.exception.OrderNotFoundException;
import com.example.ordermanagement.model.*;
import com.example.ordermanagement.repository.InMemoryOrderRepository;
import com.example.ordermanagement.service.OrderService;
import com.example.ordermanagement.util.OrderReports;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {
    private final InMemoryOrderRepository repository = new InMemoryOrderRepository();
    private final OrderService service = new OrderService(repository);

    private OrderItem item(String sku, int qty, String price) {
        return new OrderItem(sku, qty, new Money(new BigDecimal(price)));
    }

    @Test
    void createsOrderAndCalculatesTotal() {
        Order order = service.create("O-1", List.of(item("SKU-1", 2, "12.50")));
        assertEquals(new BigDecimal("25.00"), order.total().amount());
        assertEquals(OrderStatus.CREATED, service.findRequired("O-1").status());
    }

    @Test
    void rejectsInvalidQuantity() {
        assertThrows(IllegalArgumentException.class,
            () -> item("SKU-1", 0, "2.00"));
    }

    @Test
    void rejectsEmptyOrder() {
        assertThrows(IllegalArgumentException.class,
            () -> service.create("O-EMPTY", List.of()));
    }

    @Test
    void reportsMissingOrder() {
        assertThrows(OrderNotFoundException.class,
            () -> service.findRequired("MISSING"));
    }

    @Test
    void enforcesStatusTransitions() {
        service.create("O-2", List.of(item("SKU-2", 1, "5.00")));
        assertThrows(InvalidOrderOperationException.class, () -> service.ship("O-2"));
        service.markPaid("O-2");
        service.ship("O-2");
        assertEquals(OrderStatus.SHIPPED, service.findRequired("O-2").status());
        assertThrows(InvalidOrderOperationException.class, () -> service.cancel("O-2"));
    }

    @Test
    void moneyAndRecordEqualityWorkAsValueSemantics() {
        assertEquals(new Money(new BigDecimal("1.0")),
                     new Money(new BigDecimal("1.00")));
        assertEquals(
            item("SKU", 1, "4.00"),
            item("SKU", 1, "4.0"));
    }

    @Test
    void orderItemsListIsDefensivelyCopied() {
        var mutable = new java.util.ArrayList<>(List.of(item("SKU", 1, "3.00")));
        Order order = new Order("O-3", mutable);
        mutable.clear();
        assertEquals(1, order.items().size());
        assertThrows(UnsupportedOperationException.class,
            () -> order.items().clear());
    }

    @Test
    void genericRepositoryStoresAndRetrievesOrder() {
        Order order = service.create("O-4", List.of(item("SKU", 1, "7.00")));
        assertEquals(order, repository.findById("O-4").orElseThrow());
        assertTrue(repository.deleteById("O-4"));
        assertTrue(repository.findById("O-4").isEmpty());
    }

    @Test
    void streamReportsGroupAndFlattenItems() {
        Order a = service.create("O-5", List.of(item("SKU-A", 2, "2.00")));
        Order b = service.create("O-6", List.of(item("SKU-B", 1, "3.00")));
        service.markPaid("O-6");

        Map<OrderStatus, Long> counts =
            OrderReports.countByStatus(List.of(a, b));
        assertEquals(1L, counts.get(OrderStatus.CREATED));
        assertEquals(1L, counts.get(OrderStatus.PAID));
        assertEquals(2, OrderReports.allItems(List.of(a, b)).size());
        assertEquals(1, OrderReports.filter(List.of(a, b),
            order -> order.status() == OrderStatus.PAID).size());
    }

    @Test
    void rejectsNegativeMoney() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(new BigDecimal("-0.01")));
    }

    @Test
    void repositoryRejectsMismatchedKeyAndEntityId() {
        Order order = new Order("O-7", List.of(item("SKU", 1, "1.00")));
        assertThrows(IllegalArgumentException.class,
            () -> repository.save("OTHER", order));
    }
}
