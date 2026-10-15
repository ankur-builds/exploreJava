package com.example.ordermanagement.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount) {
    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }

    public Money multiply(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("quantity cannot be negative");
        return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        return new Money(amount.add(other.amount));
    }
}
