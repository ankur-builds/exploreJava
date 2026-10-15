package com.example.ordermanagement.model;

import com.example.ordermanagement.exception.InvalidOrderOperationException;

import java.util.List;
import java.util.Objects;

public final class Order {
    private final String id;
    private final List<OrderItem> items;
    private OrderStatus status;

    public Order(String id, List<OrderItem> items) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is required");
        if (items == null || items.isEmpty()) throw new IllegalArgumentException("order needs items");
        this.id = id;
        this.items = List.copyOf(items);
        this.status = OrderStatus.CREATED;
    }

    public String id() { return id; }
    public List<OrderItem> items() { return items; }
    public OrderStatus status() { return status; }

    public Money total() {
        return items.stream().map(OrderItem::subtotal)
            .reduce(new Money(java.math.BigDecimal.ZERO), Money::add);
    }

    public void markPaid() {
        requireStatus(OrderStatus.CREATED);
        status = OrderStatus.PAID;
    }

    public void ship() {
        requireStatus(OrderStatus.PAID);
        status = OrderStatus.SHIPPED;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.CANCELLED) {
            throw new InvalidOrderOperationException(
                "Cannot cancel order in status " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new InvalidOrderOperationException(
                "Expected " + expected + " but was " + status);
        }
    }

    @Override
    public String toString() {
        return "Order{id='%s', status=%s, total=%s}".formatted(id, status, total().amount());
    }

    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof Order order
            && id.equals(order.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
