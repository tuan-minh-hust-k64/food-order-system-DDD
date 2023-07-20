package com.food.ordering.system.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    private final BigDecimal amount;
    public static final Money ZERO = new Money(BigDecimal.ZERO);
    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isGreatThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    public boolean isGreatThan(Money input) {
        return this.amount != null && this.amount.compareTo(input.getAmount()) > 0;
    }

    public Money addMoney(Money money) {
        return new Money(setScale(this.amount.add(money.getAmount())));
    }
    public Money subMoney(Money money) {
        return new Money(setScale(this.amount.subtract(money.getAmount())));
    }
    public Money multiplyMoney(int input) {
        return new Money(setScale(this.amount.multiply(BigDecimal.valueOf(input))));
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }

    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }
}
