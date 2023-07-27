package com.food.ordering.system.order.service.dataaccess.order.mapper;

import com.food.ordering.system.order.service.dataaccess.order.entity.OrderAddressEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderEntity;
import com.food.ordering.system.order.service.dataaccess.order.entity.OrderItemEntity;
import com.food.ordering.system.order.service.domain.entity.Order;
import com.food.ordering.system.order.service.domain.entity.OrderItem;
import com.food.ordering.system.order.service.domain.entity.Product;
import com.food.ordering.system.order.service.domain.valueobject.OrderItemId;
import com.food.ordering.system.order.service.domain.valueobject.StreetAddress;
import com.food.ordering.system.order.service.domain.valueobject.TrackingId;
import com.food.ordering.system.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderDataAccessMapper {
    public OrderEntity orderToOrderEntity(Order order) {
        OrderEntity orderEntity = OrderEntity.builder()
                .price(order.getPrice().getAmount())
                .customerId(order.getCustomerId().getValue())
                .restaurantId(order.getRestaurantId().getValue())
                .id(order.getId().getValue())
                .trackingId(order.getTrackingId().getValue())
                .address(deliveryAddressToAddressEntity(order.getStreetAddress()))
                .items(orderItemToOrderItemEntity(order.getItems()))
                .orderStatus(order.getOrderStatus())
                .failureMessages(order.getFailureMessages() != null? String.join(",", order.getFailureMessages()) : "")
                .build();
        orderEntity.getAddress().setOrder(orderEntity);
        orderEntity.getItems().stream().map(orderItemEntity -> {
            orderItemEntity.setOrder(orderEntity);
            return null;
        });
        return orderEntity;
    }

    public Order orderEntityToOrder(OrderEntity orderEntity) {
        return Order.builder()
                .orderId(new OrderId(orderEntity.getId()))
                .customerId(new CustomerId(orderEntity.getCustomerId()))
                .restaurantId(new RestaurantId(orderEntity.getRestaurantId()))
                .orderStatus(orderEntity.getOrderStatus())
                .price(new Money(orderEntity.getPrice()))
                .streetAddress(addressEntityToStreetAddress(orderEntity.getAddress()))
                .items(orderItemEntityToOrderItem(orderEntity.getItems()))
                .trackingId(new TrackingId(orderEntity.getTrackingId()))
                .failureMessages(orderEntity.getFailureMessages().isEmpty()? new ArrayList<>() : new ArrayList<>(Arrays.asList(orderEntity.getFailureMessages().split(","))))
                .build();
    }

    private List<OrderItem> orderItemEntityToOrderItem(List<OrderItemEntity> items) {
        return items.stream().map(orderItemEntity -> {
            return OrderItem.builder()
                    .orderItemId(new OrderItemId(orderItemEntity.getId()))
                    .product(new Product(new ProductId(orderItemEntity.getProductId())))
                    .price(new Money(orderItemEntity.getPrice()))
                    .quantity(orderItemEntity.getQuantity())
                    .subTotal(new Money(orderItemEntity.getSubTotal()))
                    .build();
        }).collect(Collectors.toList());
    }

    private StreetAddress addressEntityToStreetAddress(OrderAddressEntity address) {
        return new StreetAddress(
                address.getId(),
                address.getStreet(),
                address.getPostalCode(),
                address.getCity()
        );
    }

    private List<OrderItemEntity> orderItemToOrderItemEntity(List<OrderItem> items) {
        return items.stream().map(orderItem -> {
            return OrderItemEntity.builder()
                    .id(orderItem.getId().getValue())
                    .productId(orderItem.getProduct().getId().getValue())
                    .subTotal(orderItem.getSubTotal().getAmount())
                    .quantity(orderItem.getQuantity())
                    .price(orderItem.getPrice().getAmount())
                    .build();
        }).collect(Collectors.toList());
    }

    private OrderAddressEntity deliveryAddressToAddressEntity(StreetAddress streetAddress) {
        return OrderAddressEntity.builder()
                .city(streetAddress.getCity())
                .postalCode(streetAddress.getPostalCode())
                .street(streetAddress.getStreet())
                .id(streetAddress.getId())
                .build();
    }
}
