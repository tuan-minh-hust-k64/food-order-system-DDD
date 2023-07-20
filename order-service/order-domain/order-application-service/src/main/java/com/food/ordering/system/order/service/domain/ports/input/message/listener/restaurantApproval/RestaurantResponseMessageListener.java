package com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval;

import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;

public interface RestaurantResponseMessageListener {
    void orderApporoved(RestaurantApprovalResponse restaurantApprovalResponse);
    void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse);
}
