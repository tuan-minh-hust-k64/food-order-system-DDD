package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.kafka.order.avro.model.*;
import com.food.ordering.system.order.service.domain.dto.message.CustomerModel;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.order.service.domain.outbox.model.approval.OrderApprovalEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.OrderPaymentEventPayload;
import com.food.ordering.system.order.service.domain.outbox.model.payment.PaymentOrderEventPayload;
import com.food.ordering.system.valueobject.OrderApprovalStatus;
import com.food.ordering.system.valueobject.PaymentStatus;
import debezium.payment.order_outbox.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {
    public PaymentRequestAvroModel orderCreateEventToPaymentRequestAvroModel(OrderCreateEvent orderCreateEvent) {
        Order order = orderCreateEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setOrderId(order.getId().getValue().toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .setCreatedAt(orderCreateEvent.getCreatedAt().toInstant())
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setSagaId("")
                .setId(UUID.randomUUID().toString())
                .build();
    }
    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelEvent orderCancelEvent) {
        Order order = orderCancelEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setOrderId(order.getId().getValue().toString())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .setCreatedAt(orderCancelEvent.getCreatedAt().toInstant())
                .setCustomerId(order.getCustomerId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setSagaId("")
                .setId(UUID.randomUUID().toString())
                .build();
    }
    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setRestaurantId(order.getRestaurantId().getValue().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
                .setId(UUID.randomUUID().toString())
                .setOrderId(order.getId().getValue().toString())
                .setPrice(order.getPrice().getAmount())
                .setProducts(order.getItems().stream().map(orderItem -> {
                    return Product.newBuilder()
                            .setId(orderItem.getProduct().getId().getValue().toString())
                            .setQuantity(orderItem.getQuantity())
                            .build();
                }).collect(Collectors.toList()))
                .setSagaId("")
                .build();
    }

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentOrderEventPayload paymentOrderEventPayload, Value paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .id(paymentResponseAvroModel.getId())
                .sagaId(paymentResponseAvroModel.getSagaId())
                .paymentId(paymentOrderEventPayload.getPaymentId())
                .customerId(paymentOrderEventPayload.getCustomerId())
                .orderId(paymentOrderEventPayload.getOrderId())
                .price(paymentOrderEventPayload.getPrice())
                .createdAt(Instant.parse(paymentResponseAvroModel.getCreatedAt()))
                .paymentStatus(
                        PaymentStatus.valueOf(paymentOrderEventPayload.getPaymentOrderStatus())
                )
                .failureMessages(paymentOrderEventPayload.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponse restaurantApprovalRequestAvroModelToRestaurantApprovalRequest(RestaurantApprovalResponseAvroModel restaurantApprovalResponseAvroModel) {
        return RestaurantApprovalResponse.builder()
                .restaurantId(restaurantApprovalResponseAvroModel.getRestaurantId())
                .orderApprovalStatus(OrderApprovalStatus.valueOf(restaurantApprovalResponseAvroModel.getOrderApprovalStatus().name()))
                .orderId(restaurantApprovalResponseAvroModel.getOrderId())
                .createdAt(restaurantApprovalResponseAvroModel.getCreatedAt())
                .failureMessages(restaurantApprovalResponseAvroModel.getFailureMessages())
                .id(restaurantApprovalResponseAvroModel.getId())
                .sagaId(restaurantApprovalResponseAvroModel.getSagaId())
                .build();
    }

    public PaymentRequestAvroModel orderPaymentEventPayloadToPaymentRequestAvroModel(OrderPaymentEventPayload orderPaymentEventPayload, String sagaId) {
        return PaymentRequestAvroModel.newBuilder()
                .setOrderId(orderPaymentEventPayload.getOrderId())
                .setPaymentOrderStatus(PaymentOrderStatus.valueOf(orderPaymentEventPayload.getPaymentOrderStatus()))
                .setCreatedAt(orderPaymentEventPayload.getCreatedAt().toInstant())
                .setCustomerId(orderPaymentEventPayload.getCustomerId())
                .setPrice(orderPaymentEventPayload.getPrice())
                .setSagaId(sagaId)
                .setId(UUID.randomUUID().toString())
                .build();
    }

    public RestaurantApprovalRequestAvroModel orderApprovalEventPayloadToRestaurantApprovalRequestAvroModel(OrderApprovalEventPayload orderApprovalEventPayload, String sagaId) {
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setRestaurantId(orderApprovalEventPayload.getRestaurantId())
                .setRestaurantOrderStatus(RestaurantOrderStatus.valueOf(orderApprovalEventPayload.getRestaurantOrderStatus()))
                .setCreatedAt(orderApprovalEventPayload.getCreatedAt().toInstant())
                .setId(UUID.randomUUID().toString())
                .setOrderId(orderApprovalEventPayload.getOrderId())
                .setPrice(orderApprovalEventPayload.getPrice())
                .setProducts(orderApprovalEventPayload.getProducts().stream().map(orderItem -> {
                    return Product.newBuilder()
                            .setId(orderItem.getId())
                            .setQuantity(orderItem.getQuantity())
                            .build();
                }).collect(Collectors.toList()))
                .setSagaId(sagaId)
                .build();
    }
    public CustomerModel customerAvroModeltoCustomerModel(CustomerAvroModel customerAvroModel) {
        return CustomerModel.builder()
                .id(customerAvroModel.getId())
                .username(customerAvroModel.getUsername())
                .firstName(customerAvroModel.getFirstName())
                .lastName(customerAvroModel.getLastName())
                .build();
    }
}
