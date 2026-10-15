package com.example.ordermanagement.service;

import com.example.ordermanagement.exception.OrderNotFoundException;
import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.OrderItem;
import com.example.ordermanagement.repository.Repository;

import java.util.List;

public class OrderService {
    private final Repository<Order, String> repository;

    public OrderService(Repository<Order, String> repository) {
        this.repository = repository;
    }

    public Order create(String id, List<OrderItem> items) {
        if (repository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Order already exists: " + id);
        }
        Order order = new Order(id, items);
        repository.save(id, order);
        return order;
    }

    public Order findRequired(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public void markPaid(String id) {
        Order order = findRequired(id);
        order.markPaid();
        repository.save(id, order);
    }

    public void ship(String id) {
        Order order = findRequired(id);
        order.ship();
        repository.save(id, order);
    }

    public void cancel(String id) {
        Order order = findRequired(id);
        order.cancel();
        repository.save(id, order);
    }
}
