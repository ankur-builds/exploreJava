package com.example.ordermanagement;

import com.example.ordermanagement.model.Money;
import com.example.ordermanagement.model.Order;
import com.example.ordermanagement.model.OrderItem;
import com.example.ordermanagement.model.OrderStatus;
import com.example.ordermanagement.repository.InMemoryOrderRepository;
import com.example.ordermanagement.service.OrderService;
import com.example.ordermanagement.util.OrderReports;

import java.math.BigDecimal;
import java.util.List;

public class OrderDemo {
    public static void main(String[] args) {
        var repository = new InMemoryOrderRepository();
        var service = new OrderService(repository);

        Order order = service.create(
            "ORD-100",
            List.of(new OrderItem("SKU-1", 2, new Money(new BigDecimal("19.99"))))
        );

        service.markPaid(order.id());
        System.out.println(service.findRequired(order.id()));
        System.out.println("Paid count: " +
            OrderReports.countByStatus(List.of(service.findRequired(order.id())))
                .get(OrderStatus.PAID));
    }
}
