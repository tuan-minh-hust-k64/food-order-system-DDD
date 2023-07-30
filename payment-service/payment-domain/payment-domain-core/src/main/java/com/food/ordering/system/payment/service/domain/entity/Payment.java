package com.food.ordering.system.payment.service.domain.entity;

import com.food.ordering.system.entity.BaseEntity;
import com.food.ordering.system.payment.service.domain.valueobject.PaymentId;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.PaymentStatus;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class Payment extends BaseEntity<PaymentId> {
    private final OrderId orderId;
    private final CustomerId customerId;
    private final Money price;
    private PaymentStatus paymentStatus;
    private ZonedDateTime createAt;

    private Payment(Builder builder) {
        setId(builder.paymentId);
        orderId = builder.orderId;
        customerId = builder.customerId;
        price = builder.price;
        paymentStatus = builder.paymentStatus;
        createAt = builder.createAt;
    }
    public void initializePayment() {
        setId(new PaymentId(UUID.randomUUID()));
        createAt = ZonedDateTime.now(ZoneId.of("UTC"));
    }
    public void validatePayment(List<String> failureMessages) {
        if(price == null || !price.isGreatThanZero()) {
            failureMessages.add("Price must great than zero!");
        }
    }
    public void updatePaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public static Builder builder   () {
        return new Builder();
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getPrice() {
        return price;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public ZonedDateTime getCreateAt() {
        return createAt;
    }

    public static final class Builder {
        private PaymentId paymentId;
        private OrderId orderId;
        private CustomerId customerId;
        private Money price;
        private PaymentStatus paymentStatus;
        private ZonedDateTime createAt;

        private Builder() {
        }

        public Builder paymentId(PaymentId val) {
            paymentId = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder paymentStatus(PaymentStatus val) {
            paymentStatus = val;
            return this;
        }

        public Builder createAt(ZonedDateTime val) {
            createAt = val;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }
}
