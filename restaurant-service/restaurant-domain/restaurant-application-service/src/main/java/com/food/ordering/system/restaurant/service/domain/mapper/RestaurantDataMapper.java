package com.food.ordering.system.restaurant.service.domain.mapper;

import com.food.ordering.system.restaurant.service.domain.dto.RestaurantApprovalRequest;
import com.food.ordering.system.restaurant.service.domain.entity.OrderDetail;
import com.food.ordering.system.restaurant.service.domain.entity.Product;
import com.food.ordering.system.restaurant.service.domain.entity.Restaurant;
import com.food.ordering.system.restaurant.service.domain.event.OrderApprovalEvent;
import com.food.ordering.system.restaurant.service.domain.outbox.model.OrderEventPayload;
import com.food.ordering.system.valueobject.Money;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.OrderStatus;
import com.food.ordering.system.valueobject.RestaurantId;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RestaurantDataMapper {
    public Restaurant restaurantRequestToRestaurantEntity(RestaurantApprovalRequest restaurantApprovalRequest) {
        return Restaurant.builder()
                .restaurantId(new RestaurantId(UUID.fromString(restaurantApprovalRequest.getRestaurantId())))
                .orderDetail(OrderDetail.builder()
                        .orderId(new OrderId(UUID.fromString(restaurantApprovalRequest.getOrderId())))
                        .orderStatus(OrderStatus.valueOf(restaurantApprovalRequest.getRestaurantOrderStatus().name()))
                        .products(restaurantApprovalRequest.getProducts().stream().map(product -> {
                            return Product.builder()
                                    .productId(product.getId())
                                    .quantity(product.getQuantity())
                                    .build();
                        }).collect(Collectors.toList()))
                        .totalAmount(new Money(restaurantApprovalRequest.getPrice()))
                        .build())
                .build();
    }

    public OrderEventPayload orderApprovalEventToOrderOutboxPayload(OrderApprovalEvent orderApprovalEvent) {
        return OrderEventPayload.builder()
                .orderApprovalStatus(orderApprovalEvent.getOrderApproval().getOrderApprovalStatus().name())
                .restaurantId(orderApprovalEvent.getOrderApproval().getRestaurantId().getValue().toString())
                .orderId(orderApprovalEvent.getOrderApproval().getOrderId().getValue().toString())
                .createdAt(orderApprovalEvent.getCreatedAt())
                .failureMessages(orderApprovalEvent.getFailureMessages())
                .build();
    }
}
