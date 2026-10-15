package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.Order;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryOrderRepository implements Repository<Order, String> {
    private final ConcurrentMap<String, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void save(String id, Order order) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is required");
        if (order == null) throw new IllegalArgumentException("order is required");
        if (!id.equals(order.id())) throw new IllegalArgumentException("key must match order ID");
        storage.put(id, order);
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }
}
