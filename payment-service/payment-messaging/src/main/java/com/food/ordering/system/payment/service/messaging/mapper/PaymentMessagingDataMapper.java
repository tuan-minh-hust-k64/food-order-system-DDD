package com.food.ordering.system.payment.service.messaging.mapper;

import com.food.ordering.system.event.payload.PaymentOrderEventPayload;
import com.food.ordering.system.kafka.order.avro.model.PaymentRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentResponseAvroModel;
import com.food.ordering.system.kafka.order.avro.model.PaymentStatus;
import com.food.ordering.system.payment.service.domain.dto.PaymentRequest;
import com.food.ordering.system.payment.service.domain.event.PaymentCancelledEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentCompletedEvent;
import com.food.ordering.system.payment.service.domain.event.PaymentFailedEvent;
import com.food.ordering.system.payment.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.valueobject.PaymentOrderStatus;
import debezium.order.payment_outbox.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentMessagingDataMapper {
    public PaymentRequest paymentRequestAvroModelToPaymentRequest(PaymentOrderEventPayload paymentOrderEventPayload, Value paymentRequestAvroModel) {
        return PaymentRequest.builder()
                .id(paymentRequestAvroModel.getId())
                .sagaId(paymentRequestAvroModel.getSagaId())
                .customerId(paymentOrderEventPayload.getCustomerId())
                .orderId(paymentOrderEventPayload.getOrderId())
                .price(paymentOrderEventPayload.getPrice())
                .createdAt(Instant.parse(paymentRequestAvroModel.getCreatedAt()))
                .paymentOrderStatus(PaymentOrderStatus.valueOf(paymentOrderEventPayload.getPaymentOrderStatus()))
                .build();
    }
    public PaymentResponseAvroModel orderEventPayloadToPaymentResponseAvroModel(OrderEventPayload orderEventPayload, String sagaId) {
        return PaymentResponseAvroModel.newBuilder()
                .setPaymentId(orderEventPayload.getPaymentId())
                .setOrderId(orderEventPayload.getOrderId())
                .setPaymentStatus(PaymentStatus.valueOf(orderEventPayload.getPaymentOrderStatus()))
                .setCreatedAt(orderEventPayload.getCreatedAt().toInstant())
                .setId(UUID.randomUUID().toString())
                .setCustomerId(orderEventPayload.getCustomerId())
                .setPrice(orderEventPayload.getPrice())
                .setSagaId(sagaId)
                .setFailureMessages(orderEventPayload.getFailureMessages())
                .build();
    }
}
