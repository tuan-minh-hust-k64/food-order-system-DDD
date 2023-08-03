package com.food.ordering.system.restaurant.service.domain.entity;

import com.food.ordering.system.entity.BaseEntity;
import com.food.ordering.system.restaurant.service.domain.valueobject.OrderApprovalId;
import com.food.ordering.system.valueobject.OrderApprovalStatus;
import com.food.ordering.system.valueobject.OrderId;
import com.food.ordering.system.valueobject.RestaurantId;

public class OrderApproval extends BaseEntity<OrderApprovalId> {
    private final RestaurantId restaurantId;
    private final OrderId orderId;
    private final OrderApprovalStatus orderApprovalStatus;

    public OrderApproval(RestaurantId restaurantId, OrderId orderId, OrderApprovalStatus orderApprovalStatus) {
        this.restaurantId = restaurantId;
        this.orderId = orderId;
        this.orderApprovalStatus = orderApprovalStatus;
    }

}
