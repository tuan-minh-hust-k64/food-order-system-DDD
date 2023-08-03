package com.food.ordering.system.order.service.messaging.mapper;

import com.food.ordering.system.kafka.order.avro.model.*;
import com.food.ordering.system.order.service.domain.dto.message.PaymentResponse;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.event.OrderCreateEvent;
import com.food.ordering.system.order.service.domain.event.OrderPaidEvent;
import com.food.ordering.system.valueobject.OrderApprovalStatus;
import com.food.ordering.system.valueobject.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OrderMessagingDataMapper {
    public PaymentRequestAvroModel orderCreateEventToPaymentRequestAvroModel(OrderCreateEvent orderCreateEvent) {
        Order order = orderCreateEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setOrderId(order.getId().toString())
                .setPaymentOrderStatus(PaymentOrderStatus.PENDING)
                .setCreatedAt(orderCreateEvent.getCreatedAt().toInstant())
                .setCustomerId(order.getCustomerId().toString())
                .setPrice(order.getPrice().getAmount())
                .setSagaId("")
                .setId(UUID.randomUUID().toString())
                .build();
    }
    public PaymentRequestAvroModel orderCancelledEventToPaymentRequestAvroModel(OrderCancelEvent orderCancelEvent) {
        Order order = orderCancelEvent.getOrder();
        return PaymentRequestAvroModel.newBuilder()
                .setOrderId(order.getId().toString())
                .setPaymentOrderStatus(PaymentOrderStatus.CANCELLED)
                .setCreatedAt(orderCancelEvent.getCreatedAt().toInstant())
                .setCustomerId(order.getCustomerId().toString())
                .setPrice(order.getPrice().getAmount())
                .setSagaId("")
                .setId(UUID.randomUUID().toString())
                .build();
    }
    public RestaurantApprovalRequestAvroModel orderPaidEventToRestaurantApprovalRequestAvroModel(OrderPaidEvent orderPaidEvent) {
        Order order = orderPaidEvent.getOrder();
        return RestaurantApprovalRequestAvroModel.newBuilder()
                .setRestaurantId(order.getRestaurantId().toString())
                .setRestaurantOrderStatus(RestaurantOrderStatus.PAID)
                .setCreatedAt(orderPaidEvent.getCreatedAt().toInstant())
                .setId(UUID.randomUUID().toString())
                .setOrderId(order.getId().toString())
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

    public PaymentResponse paymentResponseAvroModelToPaymentResponse(PaymentResponseAvroModel paymentResponseAvroModel) {
        return PaymentResponse.builder()
                .orderId(paymentResponseAvroModel.getOrderId())
                .paymentId(paymentResponseAvroModel.getPaymentId())
                .createdAt(paymentResponseAvroModel.getCreatedAt())
                .customerId(paymentResponseAvroModel.getCustomerId())
                .paymentStatus(PaymentStatus.valueOf(paymentResponseAvroModel.getPaymentStatus().name()))
                .sagaId(paymentResponseAvroModel.getSagaId())
                .id(paymentResponseAvroModel.getId())
                .failureMessages(paymentResponseAvroModel.getFailureMessages())
                .price(paymentResponseAvroModel.getPrice())
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
}
