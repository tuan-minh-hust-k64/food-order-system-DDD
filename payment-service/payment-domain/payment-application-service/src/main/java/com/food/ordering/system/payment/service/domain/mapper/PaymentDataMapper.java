package com.food.ordering.system.payment.service.domain.mapper;

import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.entity.Payment;
import com.food.ordering.system.payment.service.domain.event.PaymentEvent;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.valueobject.CustomerId;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.OrderId;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentDataMapper {
    public Payment paymentRequestModelToPayment(PaymentRequest paymentRequest) {
        return Payment.builder()
                .price(new Money(paymentRequest.getPrice()))
                .orderId(new OrderId(UUID.fromString(paymentRequest.getOrderId())))
                .customerId(new CustomerId(UUID.fromString(paymentRequest.getCustomerId())))
                .build();
    }

    public OrderEventPayload paymentEventToOrderEventPayload(PaymentEvent paymentEvent) {
        return OrderEventPayload.builder()
                .price(paymentEvent.getPayment().getPrice().getAmount())
                .paymentId(paymentEvent.getPayment().getId().getValue().toString())
                .orderId(paymentEvent.getPayment().getOrderId().getValue().toString())
                .paymentStatus(paymentEvent.getPayment().getPaymentStatus().name())
                .createdAt(paymentEvent.getCreatedAt())
                .customerId(paymentEvent.getPayment().getCustomerId().getValue().toString())
                .failureMessages(paymentEvent.getFailureMessages())
                .build();
    }
}
