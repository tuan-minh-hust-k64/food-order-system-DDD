package com.food.ordering.system.payment.service.messaging.mapper;

import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.valueobject.PaymentOrderStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {
    public PaymentResponseAvroModel paymentCompleteToPaymentRequestAvroModel(PaymentCompletedEvent paymentCompletedEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setPaymentId(paymentCompletedEvent.getPayment().getId().toString())
                .setCreatedAt(paymentCompletedEvent.getCreatedAt().toInstant())
                .setCustomerId(paymentCompletedEvent.getPayment().getCustomerId().getValue().toString())
                .setFailureMessages(paymentCompletedEvent.getFailureMessages())
                .setId(UUID.randomUUID().toString())
                .setPaymentStatus(PaymentStatus.valueOf(paymentCompletedEvent.getPayment().getPaymentStatus().name()))
                .setOrderId(paymentCompletedEvent.getPayment().getOrderId().getValue().toString())
                .setPrice(paymentCompletedEvent.getPayment().getPrice().getAmount())
                .setSagaId("")
                .build();
    }
    public PaymentResponseAvroModel paymentCancelledToPaymentRequestAvroModel(PaymentCancelledEvent paymentCancelledEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setPaymentId(paymentCancelledEvent.getPayment().getId().toString())
                .setCreatedAt(paymentCancelledEvent.getCreatedAt().toInstant())
                .setCustomerId(paymentCancelledEvent.getPayment().getCustomerId().getValue().toString())
                .setFailureMessages(paymentCancelledEvent.getFailureMessages())
                .setId(UUID.randomUUID().toString())
                .setPaymentStatus(PaymentStatus.valueOf(paymentCancelledEvent.getPayment().getPaymentStatus().name()))
                .setOrderId(paymentCancelledEvent.getPayment().getOrderId().getValue().toString())
                .setPrice(paymentCancelledEvent.getPayment().getPrice().getAmount())
                .setSagaId("")
                .build();
    }
    public PaymentResponseAvroModel paymentFailedToPaymentRequestAvroModel(PaymentFailedEvent paymentFailedEvent) {
        return PaymentResponseAvroModel.newBuilder()
                .setPaymentId(paymentFailedEvent.getPayment().getId().toString())
                .setCreatedAt(paymentFailedEvent.getCreatedAt().toInstant())
                .setCustomerId(paymentFailedEvent.getPayment().getCustomerId().getValue().toString())
                .setFailureMessages(paymentFailedEvent.getFailureMessages())
                .setId(UUID.randomUUID().toString())
                .setPaymentStatus(PaymentStatus.valueOf(paymentFailedEvent.getPayment().getPaymentStatus().name()))
                .setOrderId(paymentFailedEvent.getPayment().getOrderId().getValue().toString())
                .setPrice(paymentFailedEvent.getPayment().getPrice().getAmount())
                .setSagaId("")
                .build();
    }
    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentRequestAvroModel paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getId())
                .createdAt(paymentRequestAvroModel.getCreatedAt())
                .customerId(paymentRequestAvroModel.getCustomerId())
                .orderId(paymentRequestAvroModel.getOrderId())
                .paymentOrderStatus(PaymentOrderStatus.valueOf(paymentRequestAvroModel.getPaymentOrderStatus().name()))
                .price(paymentRequestAvroModel.getPrice())
                .sagaId(paymentRequestAvroModel.getSagaId())
                .build();
    }
}
