package com.food.ordering.system.restaurant.service.messaging.mapper;

import com.food.ordering.system.kafka.order.avro.model.OrderApprovalStatus;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalRequestAvroModel;
import com.food.ordering.system.kafka.order.avro.model.RestaurantApprovalResponseAvroModel;
import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovedEvent;
import com.food.ordering.system.restaurant.service.domain.event.OrderRejectedEvent;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.valueobject.ProductId;
import com.food.ordering.system.valueobject.RestaurantOrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RestaurantMessagingDataMapper {
    public RestaurantApprovalRequest orderApprovalRequestToOrderApproval(RestaurantApprovalRequestAvroModel restaurantApprovalRequestAvroModel) {
        return  RestaurantApprovalRequest.builder()
                .restaurantId(restaurantApprovalRequestAvroModel.getRestaurantId())
                .restaurantOrderStatus(RestaurantOrderStatus.valueOf(restaurantApprovalRequestAvroModel.getRestaurantOrderStatus().name()))
                .orderId(restaurantApprovalRequestAvroModel.getOrderId())
                .products(restaurantApprovalRequestAvroModel.getProducts().stream().map(product -> {
                    return Product.builder()
                            .quantity(product.getQuantity())
                            .productId(new ProductId(UUID.fromString(product.getId())))
                            .build();
                }).collect(Collectors.toList()))
                .id(restaurantApprovalRequestAvroModel.getId())
                .price(restaurantApprovalRequestAvroModel.getPrice())
                .createdAt(restaurantApprovalRequestAvroModel.getCreatedAt())
                .sagaId(restaurantApprovalRequestAvroModel.getSagaId())
                .build();
    }
    public RestaurantApprovalResponseAvroModel
    orderApprovedEventToRestaurantApprovalResponseAvroModel(OrderApprovedEvent orderApprovedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(orderApprovedEvent.getOrderApproval().getOrderId().getValue().toString())
                .setRestaurantId(orderApprovedEvent.getRestaurantId().getValue().toString())
                .setCreatedAt(orderApprovedEvent.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(orderApprovedEvent.
                        getOrderApproval().getOrderApprovalStatus().name()))
                .setFailureMessages(orderApprovedEvent.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponseAvroModel
    orderRejectedEventToRestaurantApprovalResponseAvroModel(OrderRejectedEvent orderRejectedEvent) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setSagaId("")
                .setOrderId(orderRejectedEvent.getOrderApproval().getOrderId().getValue().toString())
                .setRestaurantId(orderRejectedEvent.getRestaurantId().getValue().toString())
                .setCreatedAt(orderRejectedEvent.getCreatedAt().toInstant())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(orderRejectedEvent.
                        getOrderApproval().getOrderApprovalStatus().name()))
                .setFailureMessages(orderRejectedEvent.getFailureMessages())
                .build();
    }

    public RestaurantApprovalResponseAvroModel orderEventPayloadToRestaurantApprovalResponseAvroModel(OrderEventPayload eventPayload, String sagaId) {
        return RestaurantApprovalResponseAvroModel.newBuilder()
                .setOrderId(eventPayload.getOrderId())
                .setOrderApprovalStatus(OrderApprovalStatus.valueOf(eventPayload.getOrderApprovalStatus()))
                .setRestaurantId(eventPayload.getRestaurantId())
                .setId(String.valueOf(UUID.randomUUID()))
                .setCreatedAt(eventPayload.getCreatedAt().toInstant())
                .setFailureMessages(eventPayload.getFailureMessages())
                .setSagaId(sagaId)
                .build();
    }
}
