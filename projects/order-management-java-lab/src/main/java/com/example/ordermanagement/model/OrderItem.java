package com.example.ordermanagement.model;

import java.util.Objects;

public record OrderItem(String productId, int quantity, Money unitPrice) {
    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("productId is required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        Objects.requireNonNull(unitPrice, "unitPrice");
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
