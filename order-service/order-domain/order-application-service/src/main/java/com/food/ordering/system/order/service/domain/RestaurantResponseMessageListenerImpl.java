package com.food.ordering.system.order.service.domain;

import com.food.ordering.system.order.service.domain.event.OrderCancelEvent;
import com.food.ordering.system.order.service.domain.ports.input.message.listener.restaurantApproval.RestaurantResponseMessageListener;
import com.food.ordering.system.order.service.domain.dto.message.RestaurantApprovalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Slf4j
@Validated
public class RestaurantResponseMessageListenerImpl implements RestaurantResponseMessageListener {
    private final OrderApprovalSaga orderApprovalSaga;

    public RestaurantResponseMessageListenerImpl(OrderApprovalSaga orderApprovalSaga) {
        this.orderApprovalSaga = orderApprovalSaga;
    }

    @Override
    public void orderApporoved(RestaurantApprovalResponse restaurantApprovalResponse) {
        orderApprovalSaga.process(restaurantApprovalResponse);
    }

    @Override
    public void orderRejected(RestaurantApprovalResponse restaurantApprovalResponse) {
        OrderCancelEvent orderCancelEvent = orderApprovalSaga.rollback(restaurantApprovalResponse);
        orderCancelEvent.fire();
        log.info("Rejected order with id: {}, failure message: {}",
                restaurantApprovalResponse.getOrderId(),
                String.join(", ", orderCancelEvent.getOrder().getFailureMessages())
                );
    }
}
