package com.example.ordermanagement.util;

import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.OrderStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class OrderReports {
    private OrderReports() {}

    public static List<Order> filter(List<Order> orders, Predicate<Order> predicate) {
        return orders.stream().filter(predicate).toList();
    }

    public static Map<OrderStatus, Long> countByStatus(List<Order> orders) {
        return orders.stream().collect(
            Collectors.groupingBy(Order::status, Collectors.counting()));
    }

    public static List<com.example.ordermanagement.model.OrderItem> allItems(
            List<Order> orders) {
        return orders.stream().flatMap(order -> order.items().stream()).toList();
    }
}
