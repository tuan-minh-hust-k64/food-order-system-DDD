package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval.RestaurantResponseMessageListener;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
public class RestaurantResponseMessageListenerImpl implements RestaurantResponseMessageListener {
    @Override
    public void orderApporoved(RestaurantApprovalResponse restaurantApprovalResponse) {

    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {

    }
}
